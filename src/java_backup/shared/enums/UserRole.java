package shared.enums;
public enum UserRole {
    QUAN_TRI_VIEN("Quáº£n trá»‹ viÃªn"),
    SAT_HACH_VIEN("SÃ¡t háº¡ch viÃªn"),
    CAN_BO_QUAN_LY("CÃ¡n bá»™ quáº£n lÃ½"),
    CAN_BO_KY_THI("CÃ¡n bá»™ ká»³ thi"),
    THI_SINH("ThÃ­ sinh"),
    NGUOI_DANG_KY_THI("NgÆ°á»i Ä‘Äƒng kÃ½ thi");
    private final String displayName;
    UserRole(String displayName) {
        this.displayName = displayName;
    }
    public String getDisplayName() {
        return displayName;
    }
    public boolean matches(String roleName) {
        return roleName != null && displayName.equalsIgnoreCase(roleName.trim());
    }
    public static boolean isAdmin(String roleName) {
        return QUAN_TRI_VIEN.matches(roleName) || equalsRole(roleName, "Admin");
    }
    public static boolean isExaminer(String roleName) {
        return SAT_HACH_VIEN.matches(roleName) || equalsRole(roleName, "Examiner");
    }
    public static boolean isManagingStaff(String roleName) {
        return CAN_BO_QUAN_LY.matches(roleName) || equalsRole(roleName, "ManagingStaff");
    }
    public static boolean isExamStaff(String roleName) {
        return CAN_BO_KY_THI.matches(roleName) || equalsRole(roleName, "ExamStaff");
    }
    public static boolean isRegistrant(String roleName) {
        return NGUOI_DANG_KY_THI.matches(roleName) || equalsRole(roleName, "Registrant");
    }
    private static boolean equalsRole(String roleName, String englishName) {
        return roleName != null && englishName.equalsIgnoreCase(roleName.trim());
    }
    public static boolean isStaffPortalRole(String roleName) {
        return isAdmin(roleName) || isExaminer(roleName) || isManagingStaff(roleName) || isExamStaff(roleName);
    }
}

