package examstaff.util;

import java.util.Locale;

/** Mô tả audit gọi thí sinh bằng câu tiếng Việt, thay cho key=value máy móc. */
public final class CallAuditFormatter {

    private CallAuditFormatter() {
    }

    /**
     * Ghép đích gọi và kết quả thành câu tiếng Việt ngắn.
     *
     * @param calledTo đích / bàn gọi (có thể blank)
     * @param result   mã kết quả ({@code calling}, {@code absent}, …)
     * @return chuỗi mô tả thao tác
     */
    public static String formatDetail(String calledTo, String result) {
        String destination = calledTo == null ? "" : calledTo.trim();
        String outcome = result == null ? "" : result.trim();
        if (outcome.isEmpty()) {
            return destination.isEmpty() ? "Gọi thí sinh" : "Gọi lên " + destination;
        }
        return switch (outcome.toLowerCase(Locale.ROOT)) {
            case "calling" -> destination.isEmpty()
                    ? "Gọi thí sinh lên bảng điện tử"
                    : "Gọi lên " + destination;
            case "absent" -> destination.isEmpty()
                    ? "Đánh dấu vắng mặt"
                    : "Vắng mặt — không lên " + destination;
            default -> destination.isEmpty()
                    ? outcome
                    : destination + " — " + outcome;
        };
    }
}
