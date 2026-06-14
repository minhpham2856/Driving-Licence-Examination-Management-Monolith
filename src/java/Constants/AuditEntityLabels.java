package Constants;

import java.util.Locale;
import java.util.Map;

/**
 * Ánh xạ tên đối tượng Audit (EntityName) sang tiếng Việt.
 */
public final class AuditEntityLabels {

    private static final Map<String, String> LABELS = Map.ofEntries(
            Map.entry("CANDIDATE", "Thí sinh"),
            Map.entry("THÍ SINH", "Thí sinh"),
            Map.entry("EXAMREGISTRATION", "Thí sinh"),
            Map.entry("HỒ SƠ ĐĂNG KÝ", "Thí sinh"),
            Map.entry("PROFILE", "Hồ sơ"),
            Map.entry("PAYMENT", "Thanh toán"),
            Map.entry("EXAMSCORE", "Điểm thi"),
            Map.entry("EXAMDEVICE", "Thiết bị thi"),
            Map.entry("SESSION", "Ca thi"),
            Map.entry("SESSION_EXAMINER", "Phân công giám khảo"),
            Map.entry("SESSION_EXAMINERAREA", "Phân công phòng giám khảo"),
            Map.entry("CANDIDATECALL", "Gọi thí sinh"),
            Map.entry("KẾT QUẢ THI", "Kết quả thi"),
            Map.entry("PHÒNG THI", "Phòng thi"),
            Map.entry("SCOREENTRYQUEUE", "Hàng đợi nhập điểm")
    );

    private AuditEntityLabels() {
    }

    public static String toVietnamese(String entityName) {
        if (entityName == null || entityName.isBlank()) {
            return "—";
        }
        String trimmed = entityName.trim();
        String key = trimmed.toUpperCase(Locale.ROOT);
        return LABELS.getOrDefault(key, trimmed);
    }
}
