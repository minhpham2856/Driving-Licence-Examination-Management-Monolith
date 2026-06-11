package Utils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * <b>Tạo chữ ký (signature) cho form thanh toán SEPay.</b>
 *
 * <p><b>Chữ ký là gì?</b> SEPay cần chứng minh request checkout do merchant thật gửi, không bị sửa amount/invoice.
 * Server ghép các field thành một chuỗi, mã hóa bằng {@code sepay.secretKey} (HMAC-SHA256), encode Base64
 * → gán vào field {@code signature}. SEPay tính lại và so — khác là từ chối ("Yêu cầu không hợp lệ").</p>
 *
 * <p><b>Quy tắc bắt buộc</b> (copy từ SDK PHP chính thức sepay-pg-php):</p>
 * <ul>
 *   <li>Thứ tự field khi ghép chuỗi: {@link #PREPARE_FIELD_ORDER}</li>
 *   <li>Định dạng: {@code field1=value1,field2=value2,...} (dấu phẩy, không space)</li>
 *   <li>Field {@code signature} không tự ký chính nó</li>
 * </ul>
 *
 * <p>Ví dụ chuỗi trước khi HMAC:</p>
 * <pre>
 * merchant=SP-LIVE-XXX,currency=VND,order_amount=1200000,operation=PURCHASE,...
 * </pre>
 */
public final class SepaySignatureUtil {

    /**
     * Thứ tự field khi build chuỗi ký — <b>không được đổi</b> trừ khi SEPay cập nhật SDK.
     * Đổi thứ tự = chữ ký sai = checkout bị từ chối.
     */
    public static final List<String> PREPARE_FIELD_ORDER = Arrays.asList(
            "merchant",
            "currency",
            "order_amount",
            "operation",
            "order_description",
            "payment_method",
            "order_invoice_number",
            "customer_id",
            "success_url",
            "error_url",
            "cancel_url"
    );

    /** Whitelist: chỉ các tên field này mới được đưa vào chuỗi ký (phòng field lạ). */
    private static final List<String> ALLOWED_SIGN_FIELDS = Arrays.asList(
            "merchant", "env", "operation", "payment_method", "order_amount", "currency",
            "order_invoice_number", "order_description", "customer_id",
            "agreement_id", "agreement_name", "agreement_type",
            "agreement_payment_frequency", "agreement_amount_per_payment",
            "success_url", "error_url", "cancel_url"
    );

    /**
     * Thứ tự {@code <input type="hidden">} trong HTML form POST.
     * Giống PREPARE_FIELD_ORDER + signature ở cuối (SEPay đọc form theo thứ tự này).
     */
    public static final List<String> FORM_FIELD_ORDER;

    static {
        List<String> order = new ArrayList<>(PREPARE_FIELD_ORDER);
        order.add("signature");
        FORM_FIELD_ORDER = List.copyOf(order);
    }

    private SepaySignatureUtil() {
    }

    /**
     * Ký map field checkout → chuỗi Base64 đưa vào hidden input {@code signature}.
     *
     * @param fields    các field đã điền (chưa có signature)
     * @param secretKey {@code sepay.secretKey} từ .env
     */
    public static String sign(Map<String, String> fields, String secretKey) {
        return hmacBase64(buildSignPayload(fields), secretKey);
    }

    /**
     * Xem chuỗi plaintext trước HMAC — chỉ dùng trang debug /sepay-status khi so sánh với tài liệu SEPay.
     */
    public static String buildSignPayloadPreview(Map<String, String> fields) {
        return buildSignPayload(fields);
    }

    /**
     * Ghép chuỗi ký: duyệt PREPARE_FIELD_ORDER, bỏ qua field không có trong map hoặc không trong whitelist.
     */
    private static String buildSignPayload(Map<String, String> fields) {
        List<String> signedParts = new ArrayList<>();
        for (String field : PREPARE_FIELD_ORDER) {
            if (!fields.containsKey(field) || !ALLOWED_SIGN_FIELDS.contains(field)) {
                continue;
            }
            String value = fields.get(field);
            if (value == null) {
                continue;
            }
            signedParts.add(field + "=" + value);
        }
        return String.join(",", signedParts);
    }

    /** HMAC-SHA256(payload, secret) → bytes → Base64 string (giống PHP hash_hmac + base64_encode). */
    private static String hmacBase64(String payload, String secretKey) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot sign SEPay checkout fields", e);
        }
    }
}
