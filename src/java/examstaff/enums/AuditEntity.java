package examstaff.enums;

/** Nhãn entity nghiệp vụ dùng trong nhật ký audit ExamStaff. */
public enum AuditEntity {
    /** Thí sinh. */
    THI_SINH("Thí sinh"),
    /** Kết quả thi. */
    KET_QUA_THI("Kết quả thi"),
    /** Phòng thi. */
    PHONG_THI("Phòng thi"),
    /** Thiết bị thi. */
    THIET_BI_THI("Thiết bị thi"),
    /** Ca thi. */
    CA_THI("Ca thi"),
    /** Phân công sát hạch viên. */
    PHAN_CONG_SAT_HACH_VIEN("Phân công sát hạch viên"),
    /** Gọi thí sinh. */
    GOI_THI_SINH("Gọi thí sinh"),
    /** Hồ sơ thủ tục. */
    HO_SO("Hồ sơ"),
    /** Thanh toán lệ phí. */
    THANH_TOAN("Thanh toán"),
    /** Điểm thi. */
    DIEM_THI("Điểm thi"),
    /** Hàng đợi nhập điểm. */
    HANG_DOI_NHAP_DIEM("Hàng đợi nhập điểm");
    private final String displayName;

    AuditEntity(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /** Đổi tên entity thô sang nhãn tiếng Việt; giữ nguyên nếu không khớp. */
    public static String resolveLabel(String entityName) {
        if (entityName == null || entityName.isBlank()) {
            return "-";
        }
        String trimmed = entityName.trim();
        for (AuditEntity entity : values()) {
            if (entity.displayName.equalsIgnoreCase(trimmed)) {
                return entity.displayName;
            }
        }
        return trimmed;
    }
}
