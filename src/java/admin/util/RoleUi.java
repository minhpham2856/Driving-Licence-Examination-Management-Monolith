package admin.util;

import shared.enums.RoleType;

/**
 * Role trong DB (tên tiếng Việt theo {@link RoleType}) &lt;-&gt; UI code.
 * Vẫn chấp nhận tên tiếng Anh cũ (Admin/ExamStaff/...) để tương thích ngược.
 */
public final class RoleUi {
    private RoleUi() {}

    public static final String ADMIN = "admin";
    public static final String COI_THI = "coi_thi";
    public static final String CHAM_THI = "cham_thi";
    public static final String MANAGING = "managing";
    public static final String POLICE = "police";
    public static final String CANDIDATE = "candidate";

    public static String toUiCode(String dbRole) {
        if (dbRole == null) return CANDIDATE;

        // Ưu tiên tên tiếng Việt chuẩn của hệ thống
        RoleType type = RoleType.fromValue(dbRole.trim());
        if (type != null) {
            switch (type) {
                case ADMIN:          return ADMIN;
                case EXAM_STAFF:     return COI_THI;
                case EXAMINER:       return CHAM_THI;
                case MANAGING_STAFF: return MANAGING;
                case POLICE_STAFF:   return POLICE;
                default:             return CANDIDATE;
            }
        }

        // Fallback: tên tiếng Anh cũ
        switch (dbRole.trim().toLowerCase()) {
            case "admin":         return ADMIN;
            case "examstaff":     return COI_THI;
            case "examiner":      return CHAM_THI;
            case "managingstaff": return MANAGING;
            case "policestaff":   return POLICE;
            default:              return CANDIDATE;
        }
    }

    /** true nếu vai trò này là Quản trị viên. */
    public static boolean isAdminRole(String dbRole) {
        return ADMIN.equals(toUiCode(dbRole));
    }

    public static String toDbRole(String uiCode) {
        if (uiCode == null) return RoleType.REGISTRANT.getValue();
        switch (uiCode.trim().toLowerCase()) {
            case ADMIN:     return RoleType.ADMIN.getValue();
            case COI_THI:   return RoleType.EXAM_STAFF.getValue();
            case CHAM_THI:  return RoleType.EXAMINER.getValue();
            case MANAGING:  return RoleType.MANAGING_STAFF.getValue();
            case POLICE:    return RoleType.POLICE_STAFF.getValue();
            default:        return RoleType.REGISTRANT.getValue();
        }
    }
}
