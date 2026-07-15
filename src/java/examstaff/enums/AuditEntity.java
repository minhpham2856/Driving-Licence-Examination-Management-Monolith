package examstaff.enums;

public enum AuditEntity {
    THI_SINH("Thí sinh"),
    KET_QUA_THI("Kết quả thi"),
    PHONG_THI("Phòng thi"),
    THIET_BI_THI("Thiết bị thi"),
    CA_THI("Ca thi"),
    PHAN_CONG_SAT_HACH_VIEN("Phân công sát hạch viên"),
    GOI_THI_SINH("Gọi thí sinh"),
    HO_SO("Hồ sơ"),
    THANH_TOAN("Thanh toán"),
    DIEM_THI("Điểm thi"),
    HANG_DOI_NHAP_DIEM("Hàng đợi nhập điểm");
    private final String displayName;

    AuditEntity(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

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
