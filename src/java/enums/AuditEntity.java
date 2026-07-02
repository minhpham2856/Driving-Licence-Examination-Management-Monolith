package enums;

import java.util.Locale;

public enum AuditEntity {
    CANDIDATE("Thí sinh"),
    THI_SINH("Thí sinh"),
    EXAMREGISTRATION("Thí sinh"),
    HO_SO_DANG_KY("Thí sinh"),
    PROFILE("Hồ sơ"),
    PAYMENT("Thanh toán"),
    EXAMSCORE("Điểm thi"),
    EXAMDEVICE("Thiết bị thi"),
    SESSION("Ca thi"),
    SESSION_EXAMINER("Phân công sát hạch viên"),
    SESSION_EXAMINERAREA("Phân công phòng sát hạch viên"),
    CANDIDATECALL("Gọi thí sinh"),
    KET_QUA_THI("Kết quả thi"),
    PHONG_THI("Phòng thi"),
    SCOREENTRYQUEUE("Hàng đợi nhập điểm");

    private final String labelVi;

    AuditEntity(String labelVi) {
        this.labelVi = labelVi;
    }

    public static String auditLabel(String entityName) {
        if (entityName == null || entityName.isBlank()) return "-";
        String trimmed = entityName.trim();
        String key = trimmed.toUpperCase(Locale.ROOT).replace(" ", "_").replace("Í", "I").replace("Ồ", "O").replace("Ơ", "O").replace("Đ", "D").replace("Ă", "A").replace("Ỳ", "Y").replace("Ế", "E").replace("Ậ", "A");
        
        for (AuditEntity e : values()) {
            if (e.name().equals(key)) {
                return e.labelVi;
            }
        }
        
        // Exact mapping from original AuditEntityLabels
        return switch (trimmed.toUpperCase(Locale.ROOT)) {
            case "HỒ SƠ ĐĂNG KÝ", "THÍ SINH" -> "Thí sinh";
            case "KẾT QUẢ THI" -> "Kết quả thi";
            case "PHÒNG THI" -> "Phòng thi";
            default -> trimmed;
        };
    }
}
