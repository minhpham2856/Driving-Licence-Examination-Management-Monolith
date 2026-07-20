package admin.util;

/** DB role (Admin/Examiner/ExamStaff/ManagingStaff/Registrant) <-> UI code. Không phân biệt hoa thường. */
public final class RoleUi {
    private RoleUi() {}
    public static String toUiCode(String dbRole) {
        if (dbRole == null) return "candidate";
        switch (dbRole.trim().toLowerCase()) {
            case "admin":         return "admin";
            case "examstaff":     return "coi_thi";
            case "examiner":      return "cham_thi";
            case "managingstaff": return "managing";
            case "registrant":    return "candidate";
            default:              return "candidate";
        }
    }
    public static String toDbRole(String uiCode) {
        if (uiCode == null) return "Registrant";
        switch (uiCode.trim().toLowerCase()) {
            case "admin":     return "Admin";
            case "coi_thi":   return "ExamStaff";
            case "cham_thi":  return "Examiner";
            case "managing":  return "ManagingStaff";
            case "candidate": return "Registrant";
            default:          return "Registrant";
        }
    }
}
