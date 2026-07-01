package Constants;

/**
 * Maps between the real DB role strings (Admin/Examiner/ExamStaff/ManagingStaff/
 * Registrant) and the UI badge codes used by accounts.jsp
 * (admin/coi_thi/cham_thi/candidate/managing).
 *
 * Case-insensitive: the DB may contain values like "ADMIN" or "admin" depending
 * on how the row was seeded, so we normalise before matching.
 */
public final class RoleUi {

    private RoleUi() {}

    /** DB role -> UI code (for the badge/filter in the JSP). */
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

    /** UI code -> DB role (when saving from the form). Returns canonical casing. */
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