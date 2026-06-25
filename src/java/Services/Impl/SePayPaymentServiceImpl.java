package Services.Impl;

import Models.payment.sepay.SePayCheckoutRequest;
import Models.payment.sepay.SePayCheckoutSession;
import Models.payment.sepay.SePayIpnEvent;
import Models.payment.sepay.SePayIpnResult;
import Models.payment.sepay.SePayPaymentException;
import Services.SePayPaymentService;
import Utils.payment.sepay.SePayConfig;
import Utils.payment.sepay.SePayConstants;
import Utils.payment.sepay.SePayIpnParser;
import Utils.payment.sepay.SePaySignature;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class SePayPaymentServiceImpl implements SePayPaymentService {

    private static final long WEBHOOK_MAX_SKEW_SECONDS = 300L;

    @Override
    public boolean isConfigured() {
        return SePayConfig.isConfigured();
    }

    @Override
    public boolean sandbox() {
        return SePayConfig.sandbox();
    }

    @Override
    public SePayCheckoutSession createCheckout(SePayCheckoutRequest request) throws SePayPaymentException {
        if (!isConfigured()) {
            throw new SePayPaymentException(
                    "SePay chưa cấu hình (SEPAY_MERCHANT_ID, SEPAY_SECRET_KEY trong web/WEB-INF/.env).");
        }
        validateCheckoutRequest(request);

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

        String signature = SePaySignature.signCheckout(fields, SePayConfig.secretKey());
        fields.put("signature", signature);

        SePayCheckoutSession session = new SePayCheckoutSession();
        session.setCheckoutUrl(SePayConfig.checkoutInitUrl());
        session.setOrderInvoiceNumber(request.getOrderInvoiceNumber().trim());
        SePaySignature.orderedCheckoutFields(fields).forEach(session::putFormField);
        return session;
    }

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

    @Override
    public String generateInvoiceNumber(String businessPrefix, long internalOrderId) {
        String prefix = businessPrefix == null || businessPrefix.isBlank()
                ? "PAY" : businessPrefix.trim().toUpperCase(Locale.ROOT);
        return "DLEM-" + prefix + "-" + internalOrderId + "-" + System.currentTimeMillis();
    }

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
        return SePayIpnResult.ok(event);
    }

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
        return true;
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
