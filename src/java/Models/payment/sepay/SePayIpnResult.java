package Models.payment.sepay;

/** Kết quả xử lý IPN — servlet gọi service rồi trả JSON {@code {"success":true}}. */
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
