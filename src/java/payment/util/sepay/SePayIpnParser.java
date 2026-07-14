package payment.util.sepay;

import payment.dto.sepay.SePayIpnEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Parser JSON IPN tối giản — không phụ thuộc thư viện JSON bên ngoài. */
public final class SePayIpnParser {

    private SePayIpnParser() {
    }

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
        event.setPaid(SePayConstants.NOTIFICATION_ORDER_PAID.equalsIgnoreCase(event.getNotificationType())
                && "CAPTURED".equalsIgnoreCase(nullToEmpty(event.getOrderStatus())));
        return event;
    }

    private static String readTopLevelString(String json, String key) {
        Pattern p = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher m = p.matcher(json);
        return m.find() ? m.group(1) : null;
    }

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

    private static String extractObject(String json, String objectKey) {
        Pattern p = Pattern.compile("\"" + Pattern.quote(objectKey) + "\"\\s*:\\s*\\{");
        Matcher m = p.matcher(json);
        if (!m.find()) {
            return null;
        }
        int start = m.end() - 1;
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
