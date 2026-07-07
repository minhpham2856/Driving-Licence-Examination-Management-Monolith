package shared.enums;

public enum AuditAction {
    UPDATE("Cập nhật"),
    CREATE("Thêm"),
    DELETE("Xóa"),
    EXPORT("Xuất file"),
    IMPORT("Nhập");

    private final String value;

    private AuditAction(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static AuditAction fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (AuditAction action : values()) {
            if (action.getValue().equals(value)) {
                return action;
            }
        }
        return null;
    }
}
