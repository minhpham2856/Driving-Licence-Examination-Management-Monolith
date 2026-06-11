package Utils;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <b>Đọc JSON body từ webhook IPN</b> mà không cần thư viện Jackson/Gson.
 *
 * <p>IPN gửi JSON dạng:</p>
 * <pre>
 * {"notification_type":"ORDER_PAID","order_invoice_number":"DLEM-5-1714...","order_amount":"1200000", ...}
 * </pre>
 *
 * <p>Class này dùng regex tìm 3 field cần xử lý. Đủ cho luồng hiện tại; nếu SEPay thêm field bắt buộc
 * thì mở rộng pattern hoặc chuyển sang parser JSON đầy đủ.</p>
 */
public final class SepayIpnParser {

    private static final Pattern NOTIFICATION_TYPE = Pattern.compile(
            "\"notification_type\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern INVOICE_NUMBER = Pattern.compile(
            "\"order_invoice_number\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern ORDER_AMOUNT = Pattern.compile(
            "\"order_amount\"\\s*:\\s*\"?([0-9.]+)\"?");

    private SepayIpnParser() {
    }

    /**
     * Trích xuất dữ liệu từ body POST thô.
     *
     * @return {@link Optional#empty()} nếu thiếu notification_type hoặc invoice (không xử lý được)
     */
    public static Optional<SepayIpnData> parse(String jsonBody) {
        if (jsonBody == null || jsonBody.isBlank()) {
            return Optional.empty();
        }

        String notificationType = match(NOTIFICATION_TYPE, jsonBody);
        String invoiceNumber = match(INVOICE_NUMBER, jsonBody);
        String orderAmount = match(ORDER_AMOUNT, jsonBody);

        if (notificationType == null || invoiceNumber == null) {
            return Optional.empty();
        }

        return Optional.of(new SepayIpnData(notificationType, invoiceNumber, orderAmount));
    }

    private static String match(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group(1) : null;
    }

    /**
     * Dữ liệu đã parse từ một lần gọi IPN.
     *
     * @param notificationType   thường là ORDER_PAID khi chuyển khoản thành công
     * @param orderInvoiceNumber khớp Payment.transactionReference (DLEM-...)
     * @param orderAmount        số tiền VND (có thể null nếu JSON không có)
     */
    public record SepayIpnData(String notificationType, String orderInvoiceNumber, String orderAmount) {
    }
}
