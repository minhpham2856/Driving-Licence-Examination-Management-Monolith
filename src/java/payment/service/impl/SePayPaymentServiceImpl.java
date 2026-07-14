package payment.service.impl;

import payment.dto.sepay.SePayCheckoutRequest;
import payment.dto.sepay.SePayCheckoutSession;
import payment.dto.sepay.SePayIpnEvent;
import payment.dto.sepay.SePayIpnResult;
import payment.dto.sepay.SePayPaymentException;
import payment.dao.PaymentDAO;
import payment.dao.impl.PaymentDAOImpl;
import payment.dto.PaymentRecord;
import payment.service.SePayPaymentService;
import payment.util.sepay.SePayConfig;
import payment.util.sepay.SePayConstants;
import payment.util.sepay.SePayIpnParser;
import payment.util.sepay.SePaySignature;
import examstaff.enums.PaymentStatus;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/** SePay: checkout (createCheckout → buildAutoSubmitHtml) rồi IPN (handleIpn → recordPaidIpn idempotent). Invoice DLEM-{prefix}-{candidateId}-{timestamp}. */
public class SePayPaymentServiceImpl implements SePayPaymentService {

    /** Cho phép lệch tối đa 5 phút giữa timestamp webhook và đồng hồ server (chống replay). */
    private static final long WEBHOOK_MAX_SKEW_SECONDS = 300L;

    private final PaymentDAO paymentdao = new PaymentDAOImpl();

    @Override
    public boolean isConfigured() {
        return SePayConfig.isConfigured();
    }

    @Override
    public boolean sandbox() {
        return SePayConfig.sandbox();
    }

    /** Bước 1 checkout: ghép form fields + ký HMAC theo SIGN_FIELD_ORDER (sai thứ tự thì SePay từ chối). */
    @Override
    public SePayCheckoutSession createCheckout(SePayCheckoutRequest request) throws SePayPaymentException {
        if (!isConfigured()) {
            throw new SePayPaymentException(
                    "SePay chưa cấu hình (SEPAY_MERCHANT_ID, SEPAY_SECRET_KEY trong web/WEB-INF/.env).");
        }
        validateCheckoutRequest(request);

        // Field bắt buộc theo spec SePay PG — LinkedHashMap giữ thứ tự chèn (debug dễ đọc)
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("merchant", SePayConfig.merchantId());
        fields.put("operation", SePayConstants.OPERATION_PURCHASE);
        fields.put("currency", SePayConstants.CURRENCY_VND);
        fields.put("order_amount", String.valueOf(request.getAmountVnd()));
        fields.put("order_invoice_number", request.getOrderInvoiceNumber().trim());
        fields.put("order_description", request.getOrderDescription().trim());
        if (request.getCustomerId() != null && !request.getCustomerId().isBlank()) {
            fields.put("customer_id", request.getCustomerId().trim());
        }
        if (request.getPaymentMethod() != null && !request.getPaymentMethod().isBlank()) {
            fields.put("payment_method", request.getPaymentMethod().trim());
        }
        fields.put("success_url", resolveUrl(request.getSuccessUrl(), SePayConfig.defaultSuccessUrl()));
        fields.put("error_url", resolveUrl(request.getErrorUrl(), SePayConfig.defaultErrorUrl()));
        fields.put("cancel_url", resolveUrl(request.getCancelUrl(), SePayConfig.defaultCancelUrl()));

        // Ký HMAC-SHA256 → Base64, gắn vào form trước khi POST
        String signature = SePaySignature.signCheckout(fields, SePayConfig.secretKey());
        fields.put("signature", signature);

        SePayCheckoutSession session = new SePayCheckoutSession();
        session.setCheckoutUrl(SePayConfig.checkoutInitUrl());
        session.setOrderInvoiceNumber(request.getOrderInvoiceNumber().trim());
        // Chỉ đưa field theo đúng thứ tự ký (kể cả signature) để POST ổn định
        SePaySignature.orderedCheckoutFields(fields).forEach(session::putFormField);
        return session;
    }

    /** Bước 2 checkout: HTML form hidden + JS auto-submit POST thẳng lên SePay. */
    @Override
    public String buildAutoSubmitHtml(SePayCheckoutSession session) {
        if (session == null || session.getCheckoutUrl() == null) {
            return "";
        }
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html lang=\"vi\"><head><meta charset=\"UTF-8\">");
        html.append("<title>Đang chuyển tới SePay...</title></head><body>");
        html.append("<p>Đang chuyển tới cổng thanh toán SePay, vui lòng đợi...</p>");
        html.append("<form id=\"sepay-checkout\" method=\"POST\" action=\"")
                .append(escapeHtml(session.getCheckoutUrl())).append("\">");
        for (Map.Entry<String, String> entry : session.getFormFields().entrySet()) {
            html.append("<input type=\"hidden\" name=\"")
                    .append(escapeHtml(entry.getKey()))
                    .append("\" value=\"")
                    .append(escapeHtml(entry.getValue()))
                    .append("\"/>");
        }
        html.append("</form><script>document.getElementById('sepay-checkout').submit();</script>");
        html.append("</body></html>");
        return html.toString();
    }

    /** Mã hóa đơn DLEM-{prefix}-{internalOrderId}-{timestamp}; internalOrderId thường là CandidateId cho IPN. */
    @Override
    public String generateInvoiceNumber(String businessPrefix, long internalOrderId) {
        String prefix = businessPrefix == null || businessPrefix.isBlank()
                ? "PAY" : businessPrefix.trim().toUpperCase(Locale.ROOT);
        return "DLEM-" + prefix + "-" + internalOrderId + "-" + System.currentTimeMillis();
    }

    /** IPN: auth → parse → ORDER_PAID+CAPTURED thì ghi Payment → OK/reject. */
    @Override
    public SePayIpnResult handleIpn(String rawBody, String secretHeader,
            String signatureHeader, String timestampHeader) {
        if (!isConfigured()) {
            return SePayIpnResult.reject("SePay not configured");
        }
        if (!verifyIpnAuth(rawBody, secretHeader, signatureHeader, timestampHeader)) {
            return SePayIpnResult.reject("Unauthorized IPN");
        }
        if (rawBody == null || rawBody.isBlank()) {
            return SePayIpnResult.reject("Empty IPN body");
        }

        SePayIpnEvent event = SePayIpnParser.parse(rawBody);
        if (event.getOrderInvoiceNumber() == null || event.getOrderInvoiceNumber().isBlank()) {
            return SePayIpnResult.reject("Missing order_invoice_number");
        }
        // Chỉ ghi DB khi notification = ORDER_PAID và order_status = CAPTURED
        if (event.isPaid()) {
            recordPaidIpn(event);
        }
        return SePayIpnResult.ok(event);
    }

    /** Ghi Payment idempotent; bỏ qua nếu TransactionReference đã có (SePay retry). */
    private void recordPaidIpn(SePayIpnEvent event) {
        String transactionRef = resolveTransactionReference(event);
        if (transactionRef != null && paymentdao.existsCompletedByTransactionReference(transactionRef)) {
            return; // đã ghi → không insert trùng
        }
        Integer candidateId = parseCandidateIdFromInvoice(event.getOrderInvoiceNumber());
        if (candidateId == null || candidateId <= 0) {
            return; // invoice không parse được CandidateId
        }
        double amount = parseAmountVnd(event.getOrderAmount());
        if (amount <= 0) {
            return;
        }
        PaymentRecord payment = new PaymentRecord();
        payment.setCandidateId(candidateId);
        payment.setAmount(amount);
        payment.setPaymentStatus(PaymentStatus.HOAN_TAT.getDisplayName());
        payment.setPaymentMethod(event.getPaymentMethod() != null && !event.getPaymentMethod().isBlank()
                ? event.getPaymentMethod().trim() : "SePay");
        payment.setTransactionReference(transactionRef);
        // insert cần ExamEnrollment sẵn; DAO resolve từ CandidateId
        paymentdao.insert(payment);
    }

    /** Ưu tiên transaction_id → order_id → invoice number làm khóa idempotent. */
    private static String resolveTransactionReference(SePayIpnEvent event) {
        if (event.getTransactionId() != null && !event.getTransactionId().isBlank()) {
            return event.getTransactionId().trim();
        }
        if (event.getSePayOrderId() != null && !event.getSePayOrderId().isBlank()) {
            return event.getSePayOrderId().trim();
        }
        return event.getOrderInvoiceNumber() != null ? event.getOrderInvoiceNumber().trim() : null;
    }

    /** Tách CandidateId từ invoice DLEM-{prefix}-{candidateId}-{timestamp} (parts[2]). */
    static Integer parseCandidateIdFromInvoice(String invoice) {
        if (invoice == null || invoice.isBlank()) {
            return null;
        }
        String[] parts = invoice.trim().split("-");
        if (parts.length < 4 || !"DLEM".equalsIgnoreCase(parts[0])) {
            return null;
        }
        try {
            return Integer.parseInt(parts[2]);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static double parseAmountVnd(String raw) {
        if (raw == null || raw.isBlank()) {
            return 0;
        }
        try {
            return Double.parseDouble(raw.trim());
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    /** Xác thực IPN: X-Secret-Key = SEPAY_IPN_SECRET, hoặc HMAC timestamp.body; không có secret → cho qua (dev). */
    private static boolean verifyIpnAuth(String rawBody, String secretHeader,
            String signatureHeader, String timestampHeader) {
        String expectedSecret = SePayConfig.ipnSecret();
        if (expectedSecret != null && !expectedSecret.isBlank()) {
            if (secretHeader != null && constantTimeEquals(secretHeader.trim(), expectedSecret.trim())) {
                return true;
            }
            if (signatureHeader != null && !signatureHeader.isBlank()) {
                return SePaySignature.verifyWebhookHmac(
                        rawBody, timestampHeader, signatureHeader, expectedSecret, WEBHOOK_MAX_SKEW_SECONDS);
            }
            return false;
        }
        return true; // chưa set secret → bỏ qua auth (chỉ dùng khi dev)
    }

    private static void validateCheckoutRequest(SePayCheckoutRequest request) throws SePayPaymentException {
        if (request == null) {
            throw new SePayPaymentException("Checkout request is required.");
        }
        if (request.getAmountVnd() <= 0) {
            throw new SePayPaymentException("order_amount phải lớn hơn 0 VND.");
        }
        if (request.getOrderInvoiceNumber() == null || request.getOrderInvoiceNumber().isBlank()) {
            throw new SePayPaymentException("order_invoice_number là bắt buộc và phải unique.");
        }
        if (request.getOrderDescription() == null || request.getOrderDescription().isBlank()) {
            throw new SePayPaymentException("order_description là bắt buộc.");
        }
    }

    private static String resolveUrl(String preferred, String fallback) throws SePayPaymentException {
        String url = preferred != null && !preferred.isBlank() ? preferred.trim() : fallback;
        if (url == null || url.isBlank()) {
            throw new SePayPaymentException(
                    "Thiếu callback URL. Cấu hình SEPAY_APP_BASE_URL hoặc SEPAY_SUCCESS_URL / ERROR / CANCEL.");
        }
        // SePay chỉ chấp nhận URL tuyệt đối (public) để redirect sau thanh toán
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            throw new SePayPaymentException("Callback URL phải là URL công khai: " + url);
        }
        return url;
    }

    private static String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    /** So sánh chuỗi constant-time (XOR toàn bộ byte) — chống timing attack khi đối chiếu secret. */
    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}
