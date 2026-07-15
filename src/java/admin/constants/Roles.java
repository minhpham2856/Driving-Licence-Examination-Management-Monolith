package admin.constants;

/**
 * Role name constants matching Role.getRoleName() values used across the project.
 * Compared case-insensitively, so casing here is just for readability.
 */
public final class Roles {
    public static final String ADMIN = "Admin";
    public static final String EXAMINER = "Examiner";
    public static final String EXAM_STAFF = "ExamStaff";
    public static final String MANAGING_STAFF = "ManagingStaff";
    public static final String REGISTRANT = "Registrant";

    private Roles() {}
}
