package enums;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
public enum ViolationReason {
    VI_PHAM_QUY_CHE("Vi phạm quy chế phòng thi"),
    GIAN_LAN("Gian lận / sao chép"),
    THIET_BI_CAM("Sử dụng thiết bị cấm"),
    RA_VAO_TRAI_QUY_DINH("Ra vào phòng thi trái quy định"),
    LY_DO_KHAC("Lý do khác");
    private final String displayName;
    ViolationReason(String displayName) {
        this.displayName = displayName;
    }
    public String getDisplayName() {
        return displayName;
    }
    public boolean matches(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        return displayName.equalsIgnoreCase(value.trim());
    }
    public static ViolationReason fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        for (ViolationReason reason : values()) {
            if (reason.matches(value)) {
                return reason;
            }
        }
        return null;
    }
    public static String resolveLabel(String value) {
        if (value == null || value.isBlank()) {
            return "Chưa chọn lý do";
        }
        ViolationReason reason = fromValue(value);
        return reason != null ? reason.getDisplayName() : value.trim();
    }
    public static List<Map<String, String>> optionList() {
        List<Map<String, String>> list = new ArrayList<>();
        for (ViolationReason reason : values()) {
            Map<String, String> row = new LinkedHashMap<>();
            row.put("code", reason.getDisplayName());
            row.put("label", reason.getDisplayName());
            list.add(row);
        }
        return list;
    }
}
