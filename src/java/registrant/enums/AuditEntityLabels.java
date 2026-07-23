package registrant.enums;

import java.util.Locale;
import java.util.Map;

/**
 * Hằng số ánh xạ tên đối tượng {@code Audit.EntityName} sang nhãn tiếng Việt trên UI.
 * <p>
 * Dùng khi hiển thị timeline track-profile và log thao tác (Profile, Document, Candidate, Payment…).
 */
public final class AuditEntityLabels {

    private static final Map<String, String> LABELS = Map.ofEntries(
            Map.entry("CANDIDATE", "Thí sinh"),
            Map.entry("THÍ SINH", "Thí sinh"),
            Map.entry("EXAMREGISTRATION", "Thí sinh"),
            Map.entry("HỒ SƠ ĐĂNG KÝ", "Thí sinh"),
            Map.entry("PROFILE", "Hồ sơ"),
            Map.entry("RegistrantPayment", "Thanh toán"),
            Map.entry("EXAMSCORE", "Điểm thi"),
            Map.entry("EXAMDEVICE", "Thiết bị thi"),
            Map.entry("SESSION", "Ca thi"),
            Map.entry("SESSION_EXAMINER", "Phân công sát hạch viên"),
            Map.entry("SESSION_EXAMINERAREA", "Phân công phòng sát hạch viên"),
            Map.entry("CANDIDATECALL", "Gọi thí sinh"),
            Map.entry("KẾT QUẢ THI", "Kết quả thi"),
            Map.entry("PHÒNG THI", "Phòng thi"),
            Map.entry("SCOREENTRYQUEUE", "Hàng đợi nhập điểm"),
            Map.entry("DOCUMENT", "Tài liệu hồ sơ"),
            Map.entry("TÀI LIỆU HỒ SƠ", "Tài liệu hồ sơ")
    );

    private AuditEntityLabels() {
    }

    /** Đổi EntityName audit sang nhãn tiếng Việt; giữ nguyên nếu không map. */
    public static String toVietnamese(String entityName) {
        if (entityName == null || entityName.isBlank()) {
            return "-";
        }
        String trimmed = entityName.trim();
        String key = trimmed.toUpperCase(Locale.ROOT);
        return LABELS.getOrDefault(key, trimmed);
    }
}
