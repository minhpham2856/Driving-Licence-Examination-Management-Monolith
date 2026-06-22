package Utils;

public final class RoleConstants {

    private RoleConstants() {
    }

    public static final String ADMIN = "Admin";
    public static final String EXAMINER = "Examiner";
    public static final String EXAM_STAFF = "ExamStaff";
    public static final String MANAGING_STAFF = "ManagingStaff";
    public static final String REGISTRANT = "Registrant";

    // Returns true if the given role name represents a staff-level user
    // (Admin, ExamStaff, ManagingStaff, or Examiner).
    public static boolean isStaffRole(String roleName) {
        if (roleName == null || roleName.trim().isEmpty()) {
            return false;
        }
        String name = roleName.trim();
        return ADMIN.equalsIgnoreCase(name)
                || EXAMINER.equalsIgnoreCase(name)
                || EXAM_STAFF.equalsIgnoreCase(name)
                || MANAGING_STAFF.equalsIgnoreCase(name);
    }

    // Returns the redirect path for a given role name.
    // Returns null for registrant/unknown roles (caller should handle fallback).
    public static String getRedirectPath(String roleName) {
        if (isRole(roleName, MANAGING_STAFF)) {
            return "/views/staff/managingstaff/dashboard.jsp";
        } else if (isRole(roleName, EXAM_STAFF)) {
            return "/views/admin/examstaff/dashboard.jsp";
        } else if (isRole(roleName, EXAMINER)) {
            return "/views/examiner/dashboard";
        } else if (isRole(roleName, ADMIN)) {
            return "/admin/dashboard";
        }
        return null;
    }

    // Returns true if the given role name is the specified role (case-insensitive).
    public static boolean isRole(String roleName, String expected) {
        if (roleName == null || expected == null) {
            return false;
        }
        return expected.equalsIgnoreCase(roleName.trim());
    }
}
