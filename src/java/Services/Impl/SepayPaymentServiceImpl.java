package Services.Impl;

import Config.EnvLoader;
import Config.SepayConfig;
import DAO.ExamRegistrationDAO;
import DAO.ExamSessionDAO;
import DAO.PaymentDAO;
import DAO.Impl.ExamRegistrationDAOImpl;
import DAO.Impl.ExamSessionDAOImpl;
import DAO.Impl.PaymentDAOImpl;
import Models.PaymentRecord;
import Models.User;
import Services.SepayPaymentService;
import Utils.SepaySignatureUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * <b>Lớp trung tâm tích hợp SEPay</b> — nằm giữa servlet và database.
 *
 * <p><b>3 việc chính:</b></p>
 * <ol>
 *   <li>Kiểm tra .env đủ điều kiện ({@link #isReady()})</li>
 *   <li>Tạo dữ liệu form checkout + chữ ký ({@link #buildCheckoutFields})</li>
 *   <li>Xử lý webhook IPN khi khách đã chuyển tiền ({@link #handleOrderPaid})</li>
 * </ol>
 *
 * <p><b>Luồng dữ liệu (đọc theo thứ tự):</b></p>
 * <pre>
 * registerExam() tạo Payment Pending + invoice DLEM-...
 *        ↓
 * buildCheckoutFields() → map field + signature
 *        ↓
 * Browser POST sang pay.sepay.vn → khách chuyển khoản
 *        ↓
 * SepayIpnServlet nhận ORDER_PAID → handleOrderPaid()
 *        ↓
 * Payment=Completed, ExamRegistration đã thanh toán, registeredCount++
 * </pre>
 *
 * <p>Trang success/error/cancel <b>không</b> gọi class này — chỉ IPN mới cập nhật DB.</p>
 */
public class SepayPaymentServiceImpl implements SepayPaymentService {

    private static final Logger LOG = Logger.getLogger(SepayPaymentServiceImpl.class.getName());

    /** Thông báo chung cho thí sinh — chi tiết lỗi .env chỉ ghi log, không hiện UI. */
    public static final String USER_MSG_PAYMENT_UNAVAILABLE =
            "Hệ thống thanh toán tạm thời không khả dụng. Vui lòng thử lại sau hoặc liên hệ trung tâm.";

    /** Giá trị cột Payment.paymentMethod — phải khớp CHECK constraint DB. */
    private static final String PAYMENT_METHOD = "SEPay";
    /** Tiền tố mã hóa đơn — SEPay và IPN dùng chuỗi này để tìm lại bản ghi Payment. */
    private static final String INVOICE_PREFIX = "DLEM-";

    private final PaymentDAO paymentDAO = new PaymentDAOImpl();
    private final ExamRegistrationDAO examRegistrationDAO = new ExamRegistrationDAOImpl();
    private final ExamSessionDAO examSessionDAO = new ExamSessionDAOImpl();

    /**
     * {@code true} khi merchant, secret, appBaseUrl OK và không lệch sandbox/production.
     * RegisterExam gọi trước khi tạo Payment — tránh đăng ký treo không thanh toán được.
     */
    @Override
    public boolean isReady() {
        return configurationError() == null;
    }

    /**
     * Chi tiết lỗi kỹ thuật — chỉ dùng log server hoặc trang /sepay-status (debug).
     * Thí sinh không thấy chuỗi này (dùng {@link #configurationErrorForUser()}).
     */
    @Override
    public String configurationError() {
        if (!EnvLoader.hasSepayConfig() && !SepayConfig.isConfigured()) {
            return "Không đọc được file .env. " + SepayConfig.describeLoadState();
        }
        if (!SepayConfig.isConfigured()) {
            return "Thiếu sepay.merchantId hoặc sepay.secretKey. " + SepayConfig.describeLoadState();
        }
        // appBaseUrl bắt buộc: không có thì IPN không tới server → thanh toán xong DB vẫn Pending
        if (SepayConfig.getAppBaseUrl().isBlank()) {
            return "Thiếu sepay.appBaseUrl (URL public cho IPN). " + SepayConfig.describeLoadState();
        }
        return SepayConfig.environmentMismatchWarning();
    }

    /**
     * Phiên bản an toàn cho UI — luôn trả message chung, ghi chi tiết vào log.
     *
     * @return {@code null} nếu cấu hình OK; ngược lại message hiển thị trên register-exam.jsp
     */
    @Override
    public String configurationErrorForUser() {
        if (configurationError() != null) {
            LOG.warning("SEPay configuration: " + configurationError());
            return USER_MSG_PAYMENT_UNAVAILABLE;
        }
        return null;
    }

    /**
     * Tạo toàn bộ field cho form POST sang SEPay (hidden input + chữ ký).
     *
     * <p><b>Cách hoạt động:</b> gom field vào LinkedHashMap → ký bằng secret → thêm field signature.
     * Thứ tự key quan trọng khi ký (xem {@link SepaySignatureUtil}).</p>
     *
     * @param orderInvoiceNumber mã đã lưu Payment.transactionReference (vd. DLEM-12-1714567890)
     * @param licenceCode        hạng GPLX — đưa vào order_description
     * @param sessionName        tên đợt thi (tham số giữ cho mở rộng sau; hiện không gửi SEPay)
     * @param amount             tổng phí VND từ ExamSection
     */
    @Override
    public Map<String, String> buildCheckoutFields(
            HttpServletRequest request,
            User user,
            String orderInvoiceNumber,
            String licenceCode,
            String sessionName,
            BigDecimal amount) {

        String orderAmount = toSepayAmount(amount);
        String returnBaseUrl = resolveReturnBaseUrl(request).trim();
        String description = "Thanh toan phi thi hang " + licenceCode;
        // SEPay cần customer_id — ưu tiên personId (hồ sơ thí sinh), không có thì userId
        String customerId = user.getPersonId() != null
                ? "CUST_" + user.getPersonId()
                : "USER_" + user.getId();

        // Các field dưới đây (trừ signature) đều tham gia chuỗi HMAC — không đổi tên key
        Map<String, String> signPayload = new LinkedHashMap<>();
        signPayload.put("merchant", SepayConfig.getMerchantId());
        signPayload.put("currency", "VND");
        signPayload.put("order_amount", orderAmount);
        signPayload.put("operation", "PURCHASE");
        signPayload.put("order_description", description);
        signPayload.put("payment_method", "BANK_TRANSFER"); // VietQR / chuyển khoản
        signPayload.put("order_invoice_number", orderInvoiceNumber);
        signPayload.put("customer_id", customerId);
        // Ba URL dưới dùng returnBaseUrl (localhost) — tách khỏi appBaseUrl (ngrok IPN)
        signPayload.put("success_url", returnBaseUrl + "/registrant/payment/sepay-success");
        signPayload.put("error_url", returnBaseUrl + "/registrant/payment/sepay-error");
        signPayload.put("cancel_url", returnBaseUrl + "/registrant/payment/sepay-cancel");

        Map<String, String> fields = new LinkedHashMap<>(signPayload);
        fields.put("signature", SepaySignatureUtil.sign(signPayload, SepayConfig.getSecretKey()));
        return fields;
    }

    /** URL đích của thẻ {@code <form action="...">} — từ sepay.checkoutUrl hoặc mặc định theo env. */
    @Override
    public String getCheckoutUrl() {
        return SepayConfig.getCheckoutUrl();
    }

    /**
     * Xác thực webhook IPN — so sánh header {@code X-Secret-Key} với sepay.secretKey.
     * Chặn kẻ giả mạo POST /sepay-ipn nếu không biết secret.
     */
    @Override
    public boolean verifyIpnSecret(String headerSecret) {
        if (headerSecret == null || headerSecret.isBlank()) {
            return false;
        }
        return SepayConfig.getSecretKey().equals(headerSecret.trim());
    }

    /**
     * <b>Cập nhật database khi SEPay báo đã nhận tiền</b> (notification ORDER_PAID).
     *
     * <p>Tìm Payment theo invoice → kiểm tra Pending + đúng số tiền → đánh dấu hoàn tất → tăng chỗ đợt thi.</p>
     * <p>Gọi nhiều lần với cùng invoice đã Completed vẫn OK (idempotent) — SEPay có thể retry IPN.</p>
     *
     * @param orderInvoiceNumber khớp Payment.transactionReference
     * @param orderAmountRaw     số tiền từ JSON IPN (có thể null — khi đó bỏ qua so khớp)
     * @return {@code true} nếu xử lý thành công hoặc đã xử lý trước đó
     */
    @Override
    public boolean handleOrderPaid(String orderInvoiceNumber, String orderAmountRaw) {
        if (orderInvoiceNumber == null || orderInvoiceNumber.isBlank()) {
            return false;
        }

        Optional<PaymentRecord> paymentOpt = paymentDAO.findByTransactionReference(orderInvoiceNumber.trim());
        if (paymentOpt.isEmpty()) {
            LOG.warning("SEPay IPN: không tìm thấy payment invoice=" + orderInvoiceNumber);
            return false;
        }

        PaymentRecord payment = paymentOpt.get();

        if (payment.isRegistrationCancelled()) {
            LOG.warning("SEPay IPN: registration cancelled invoice=" + orderInvoiceNumber);
            return false;
        }

        // Đã thanh toán rồi — trả true để SEPay ngừng gửi lại IPN
        if ("Completed".equalsIgnoreCase(payment.getPaymentStatus())) {
            return true;
        }

        if ("Cancelled".equalsIgnoreCase(payment.getPaymentStatus())) {
            LOG.warning("SEPay IPN: payment cancelled/expired invoice=" + orderInvoiceNumber);
            return false;
        }

        if (!"Pending".equalsIgnoreCase(payment.getPaymentStatus())) {
            return false;
        }

        Timestamp expiresAt = payment.getPaymentExpiresAt();
        if (expiresAt != null && expiresAt.toInstant().isBefore(Instant.now())) {
            LOG.warning("SEPay IPN: payment expired invoice=" + orderInvoiceNumber);
            return false;
        }
        if (!PAYMENT_METHOD.equalsIgnoreCase(payment.getPaymentMethod())) {
            return false;
        }
        if (!amountMatches(payment.getAmount(), orderAmountRaw)) {
            LOG.warning("SEPay IPN: số tiền không khớp invoice=" + orderInvoiceNumber);
            return false;
        }

        // Thứ tự: đánh dấu registration trước, payment sau, cuối cùng tăng slot đợt thi
        boolean registrationUpdated = examRegistrationDAO.markPaymentCompleted(payment.getExamRegistrationId());
        boolean paymentUpdated = paymentDAO.markCompleted(payment.getId());
        if (!paymentUpdated) {
            return false;
        }
        if (!registrationUpdated) {
            // Payment đã Completed nhưng registration không đổi — vẫn coi IPN thành công
            return true;
        }

        return examSessionDAO.incrementRegisteredCount(payment.getExamSessionId());
    }

    /**
     * Sinh mã hóa đơn unique mỗi lần đăng ký.
     * Format: DLEM-{registrationId}-{timestampMs} — lưu DB và gửi SEPay cùng giá trị.
     */
    public static String invoiceNumberForRegistration(int registrationId) {
        return INVOICE_PREFIX + registrationId + "-" + System.currentTimeMillis();
    }

    /** So sánh số tiền IPN với Payment.amount; nếu IPN không gửi amount thì chấp nhận (tương thích cũ). */
    private boolean amountMatches(BigDecimal expected, String rawFromIpn) {
        if (expected == null) {
            return false;
        }
        if (rawFromIpn == null || rawFromIpn.isBlank()) {
            return true;
        }
        try {
            BigDecimal received = new BigDecimal(rawFromIpn.trim());
            return expected.setScale(0, RoundingMode.HALF_UP)
                    .compareTo(received.setScale(0, RoundingMode.HALF_UP)) == 0;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    /** SEPay chỉ chấp nhận order_amount dạng chuỗi số nguyên, không có dấu phẩy/thập phân. */
    private String toSepayAmount(BigDecimal amount) {
        return amount.setScale(0, RoundingMode.HALF_UP).toPlainString();
    }

    /**
     * Base URL cho success/error/cancel trong form checkout.
     * Ưu tiên sepay.returnBaseUrl; không có thì ghép từ request hiện tại (scheme + host + context).
     */
    private String resolveReturnBaseUrl(HttpServletRequest request) {
        String configured = SepayConfig.getReturnBaseUrl();
        if (!configured.isBlank()) {
            return configured;
        }
        String context = request.getContextPath();
        return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
                + (context != null ? context : "");
    }
}
