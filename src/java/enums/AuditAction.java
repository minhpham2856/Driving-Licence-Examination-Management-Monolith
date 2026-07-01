package enums;

public enum AuditAction {
    CAP_NHAT("Cập nhật"),
    THEM("Thêm"),
    XOA("Xóa"),
    XUAT("Xuất"),
    NHAP("Nhập"),
    PHAN_CONG("Phân công"),
    CANH_BAO("Cảnh báo"),
    DUYET("Duyệt"),
    HE_THONG("Hệ thống");
    private final String displayName;

    AuditAction(String displayName) {
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

    public static AuditAction normalize(String rawAction) {
        if (rawAction == null || rawAction.isBlank()) {
            return CAP_NHAT;
        }
        String trimmed = rawAction.trim();
        for (AuditAction action : values()) {
            if (action.matches(trimmed)) {
                return action;
            }
        }
        if (trimmed.contains(NHAP.getDisplayName())) {
            return NHAP;
        }
        if (trimmed.contains(THEM.getDisplayName())) {
            return THEM;
        }
        if (trimmed.contains(XOA.getDisplayName())) {
            return XOA;
        }
        if (trimmed.contains(XUAT.getDisplayName())) {
            return XUAT;
        }
        if (trimmed.contains(PHAN_CONG.getDisplayName())) {
            return PHAN_CONG;
        }
        if (trimmed.contains(CANH_BAO.getDisplayName())) {
            return CANH_BAO;
        }
        if (trimmed.contains(DUYET.getDisplayName())) {
            return DUYET;
        }
        if (trimmed.contains(HE_THONG.getDisplayName())) {
            return HE_THONG;
        }
        if (trimmed.contains(CAP_NHAT.getDisplayName())) {
            return CAP_NHAT;
        }
        return CAP_NHAT;
    }
}
