package payment.util.sepay;

import payment.dto.sepay.SePayIpnEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Bộ phân tích JSON webhook IPN từ SePay (regex + đếm ngoặc lồng nhau, không dùng thư viện JSON).
 * Phục vụ bước IPN trong luồng checkout → IPN → return: đọc raw body POST,
 * trích notification_type, order.* (invoice, amount, status) và
 * transaction.* (transaction_id, payment_method).
 * Đánh dấu paid=true khi ORDER_PAID kèm order_status=CAPTURED
 * để SePayPaymentServiceImpl ghi bảng Payment.
 */
public final class SePayIpnParser {

    private SePayIpnParser() {
    }

    /** Parse raw JSON → SePayIpnEvent; isPaid khi notification_type=ORDER_PAID và order_status=CAPTURED. */
    public static SePayIpnEvent parse(String rawJson) {
        SePayIpnEvent event = new SePayIpnEvent();
        if (rawJson == null || rawJson.isBlank()) {
            return event;
        }
        event.setRawBody(rawJson);
        event.setNotificationType(readTopLevelString(rawJson, "notification_type"));
        event.setOrderInvoiceNumber(readNestedString(rawJson, "order", "order_invoice_number"));
        event.setOrderStatus(readNestedString(rawJson, "order", "order_status"));
        event.setOrderAmount(readNestedString(rawJson, "order", "order_amount"));
        event.setOrderDescription(readNestedString(rawJson, "order", "order_description"));
        event.setSePayOrderId(readNestedString(rawJson, "order", "order_id"));
        event.setTransactionId(readNestedString(rawJson, "transaction", "transaction_id"));
        event.setTransactionStatus(readNestedString(rawJson, "transaction", "transaction_status"));
        event.setPaymentMethod(readNestedString(rawJson, "transaction", "payment_method"));
        // Điều kiện "đã thanh toán thành công" theo SePay
        event.setPaid(SePayConstants.NOTIFICATION_ORDER_PAID.equalsIgnoreCase(event.getNotificationType())
                && "CAPTURED".equalsIgnoreCase(nullToEmpty(event.getOrderStatus())));
        return event;
    }

    /** Đọc field string ở root JSON: "key": "value". */
    private static String readTopLevelString(String json, String key) {
        Pattern p = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher m = p.matcher(json);
        return m.find() ? m.group(1) : null;
    }

    /** Đọc field trong object con (vd. order.order_amount) — thử string rồi số. */
    private static String readNestedString(String json, String objectKey, String fieldKey) {
        String block = extractObject(json, objectKey);
        if (block == null) {
            return null;
        }
        Pattern stringPat = Pattern.compile("\"" + Pattern.quote(fieldKey) + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher m = stringPat.matcher(block);
        if (m.find()) {
            return m.group(1);
        }
        Pattern numberPat = Pattern.compile("\"" + Pattern.quote(fieldKey) + "\"\\s*:\\s*([0-9.]+)");
        m = numberPat.matcher(block);
        return m.find() ? m.group(1) : null;
    }

    /** Cắt object JSON "objectKey": { ... } bằng đếm depth dấu ngoặc; trả substring gồm cả cặp ngoặc nhọn. */
    private static String extractObject(String json, String objectKey) {
        Pattern p = Pattern.compile("\"" + Pattern.quote(objectKey) + "\"\\s*:\\s*\\{");
        Matcher m = p.matcher(json);
        if (!m.find()) {
            return null;
        }
        int start = m.end() - 1; // vị trí '{'
        int depth = 0;
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0) {
                    return json.substring(start, i + 1);
                }
            }
        }
        return null;
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
