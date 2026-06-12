package Constants;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ViolationReasonCodes {

    public record Reason(String code, String label) {
    }

    private static final List<Reason> REASONS = List.of(
            new Reason("quy-che", "Vi phạm quy chế phòng thi"),
            new Reason("gian-lan", "Gian lận / sao chép"),
            new Reason("thiet-bi", "Sử dụng thiết bị cấm"),
            new Reason("ra-vao", "Ra vào phòng thi trái quy định"),
            new Reason("khac", "Lý do khác"));

    private ViolationReasonCodes() {
    }

    public static List<Reason> all() {
        return REASONS;
    }

    public static String labelOf(String code) {
        if (code == null || code.isBlank()) {
            return "";
        }
        for (Reason reason : REASONS) {
            if (reason.code().equalsIgnoreCase(code.trim())) {
                return reason.label();
            }
        }
        return code.trim();
    }

    public static Map<String, String> asMap() {
        Map<String, String> map = new LinkedHashMap<>();
        for (Reason reason : REASONS) {
            map.put(reason.code(), reason.label());
        }
        return map;
    }

    /** JSP-friendly list (EL cannot read Java record accessors reliably). */
    public static List<Map<String, String>> asOptionList() {
        List<Map<String, String>> list = new ArrayList<>();
        for (Reason reason : REASONS) {
            Map<String, String> row = new LinkedHashMap<>();
            row.put("code", reason.code());
            row.put("label", reason.label());
            list.add(row);
        }
        return list;
    }
}
