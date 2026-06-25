package enums;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public enum ViolationReason {
    QUY_CHE("quy-che", "Vi phạm quy chế phòng thi"),
    GIAN_LAN("gian-lan", "Gian lận / sao chép"),
    DEVICES("devices", "Sử dụng thiết bị cấm"),
    RA_VAO("ra-vao", "Ra vào phòng thi trái quy định"),
    KHAC("khac", "Lý do khác");

    private final String code;
    private final String label;

    ViolationReason(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static String violationLabel(String code) {
        if (code == null || code.isBlank()) return "";
        for (ViolationReason reason : values()) {
            if (reason.code.equalsIgnoreCase(code.trim())) {
                return reason.label;
            }
        }
        return code.trim();
    }

    public static Map<String, String> violationMap() {
        Map<String, String> map = new LinkedHashMap<>();
        for (ViolationReason reason : values()) {
            map.put(reason.code, reason.label);
        }
        return map;
    }

    public static List<Map<String, String>> violationOptionList() {
        List<Map<String, String>> list = new ArrayList<>();
        for (ViolationReason reason : values()) {
            Map<String, String> row = new LinkedHashMap<>();
            row.put("code", reason.code);
            row.put("label", reason.label);
            list.add(row);
        }
        return list;
    }
}
