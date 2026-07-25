package payment.util.sepay;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;

/**
 * Tiện ích ký và xác thực chữ ký SePay cho hai giai đoạn luồng thanh toán.
 * Checkout: ghép field theo SIGN_FIELD_ORDER → HMAC-SHA256 → Base64
 * (form POST lên pay.sepay.vn). IPN: HMAC timestamp + "." + body dạng hex,
 * kiểm tra lệch thời gian chống replay. Không thao tác bảng DB — phục vụ
 * SePayPaymentServiceImpl và SePayIpnServlet.
 */
public final class SePaySignature {

    /** Thứ tự field ký checkout theo spec SePay PG — field thiếu trong map bị bỏ qua. */
    private static final String[] SIGN_FIELD_ORDER = {
            "order_amount", "merchant", "currency", "operation",
            "order_description", "order_invoice_number", "customer_id",
            "payment_method", "success_url", "error_url", "cancel_url"
    };

    private SePaySignature() {
    }

    /** Ký form checkout: signedString → HMAC-SHA256 → Base64. */
    public static String signCheckout(Map<String, String> fields, String secretKey) {
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalArgumentException("SePay secret key is required.");
        }
        String signedString = buildSignedString(fields);
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] raw = mac.doFinal(signedString.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeBase64String(raw);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot sign SePay checkout payload", e);
        }
    }

    /** Ghép chuỗi ký theo SIGN_FIELD_ORDER: key=value,... chỉ gồm field có trong map. */
    public static String buildSignedString(Map<String, String> fields) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String key : SIGN_FIELD_ORDER) {
            if (!fields.containsKey(key)) {
                continue;
            }
            String value = fields.get(key);
            if (value == null) {
                continue;
            }
            if (!first) {
                sb.append(',');
            }
            sb.append(key).append('=').append(value);
            first = false;
        }
        return sb.toString();
    }

    /** Xác thực webhook HMAC: reject nếu lệch timestamp quá skew; strip sha256=; so hex constant-time. */
    public static boolean verifyWebhookHmac(String rawBody, String timestamp, String signatureHeader,
            String webhookSecret, long maxSkewSeconds) {
        if (rawBody == null || signatureHeader == null || webhookSecret == null || webhookSecret.isBlank()) {
            return false;
        }
        // Replay protection: timestamp phải nằm trong cửa sổ cho phép
        if (timestamp != null && !timestamp.isBlank() && maxSkewSeconds > 0) {
            try {
                long ts = Long.parseLong(timestamp.trim());
                long now = System.currentTimeMillis() / 1000L;
                if (Math.abs(now - ts) > maxSkewSeconds) {
                    return false;
                }
            } catch (NumberFormatException ignored) {
                return false;
            }
        }
        String provided = signatureHeader.trim();
        if (provided.startsWith("sha256=")) {
            provided = provided.substring(7);
        }
        // Spec SePay: HMAC(timestamp + "." + body)
        String payload = (timestamp != null ? timestamp.trim() : "") + "." + rawBody;
        String expected = hmacSha256Hex(payload, webhookSecret);
        return constantTimeEquals(expected.toLowerCase(), provided.toLowerCase());
    }

    /** Sắp map form theo SIGN_FIELD_ORDER + signature cuối — thứ tự POST ổn định. */
    public static Map<String, String> orderedCheckoutFields(Map<String, String> source) {
        Map<String, String> ordered = new LinkedHashMap<>();
        for (String key : SIGN_FIELD_ORDER) {
            if (source.containsKey(key) && source.get(key) != null) {
                ordered.put(key, source.get(key));
            }
        }
        if (source.containsKey("signature")) {
            ordered.put("signature", source.get("signature"));
        }
        return ordered;
    }

    private static String hmacSha256Hex(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(raw.length * 2);
            for (byte b : raw) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot compute webhook HMAC", e);
        }
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
