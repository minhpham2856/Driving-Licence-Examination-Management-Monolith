package payment.dto.sepay;

/**
 * Kết quả xử lý webhook IPN trả về cho SePayIpnServlet.
 * Thuộc bước IPN sau khi service xác thực X-Secret-Key/HMAC và parse payload:
 * accepted=true → HTTP 200 + JSON success cho SePay;
 * rejected → 401 (auth) hoặc 400 (payload) kèm JSON lỗi.
 * Không ghi DB trực tiếp — chỉ đóng gói trạng thái chấp nhận và SePayIpnEvent (nếu OK).
 */
public class SePayIpnResult {

    private final boolean accepted;
    private final String errorMessage;
    private final SePayIpnEvent event;

    private SePayIpnResult(boolean accepted, String errorMessage, SePayIpnEvent event) {
        this.accepted = accepted;
        this.errorMessage = errorMessage;
        this.event = event;
    }

    public static SePayIpnResult ok(SePayIpnEvent event) {
        return new SePayIpnResult(true, null, event);
    }

    public static SePayIpnResult reject(String message) {
        return new SePayIpnResult(false, message, null);
    }

    public boolean isAccepted() {
        return accepted;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public SePayIpnEvent getEvent() {
        return event;
    }

    public String responseJson() {
        return accepted ? "{\"success\":true}" : "{\"success\":false,\"error\":\"" + escape(errorMessage) + "\"}";
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
