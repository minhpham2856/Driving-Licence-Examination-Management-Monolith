package shared.enums;

public enum UserRole {
    QUAN_TRI_VIEN("Quáº£n trá»‹ viÃªn"),
    SAT_HACH_VIEN("SÃ¡t háº¡ch viÃªn"),
    CAN_BO_QUAN_LY("CÃ¡n bá»™ quáº£n lÃ½"),
    CAN_BO_KY_THI("CÃ¡n bá»™ ká»³ thi"),
    THI_SINH("ThÃ­ sinh"),
    NGUOI_DANG_KY_THI("NgÆ°á»i Ä‘Äƒng kÃ½ thi");

    private final String value;

    private UserRole(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static UserRole fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (UserRole status : values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }
}
