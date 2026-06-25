package enums;

import model.user.Role;

public enum UserRole {
    ADMIN(1, "Admin"),
    EXAMINER(2, "Examiner"),
    MANAGING_STAFF(3, "ManagingStaff"),
    EXAM_STAFF(4, "ExamStaff"),
    CANDIDATE(5, "Candidate"),
    REGISTRANT(6, "Registrant");

    private final int id;
    private final String roleName;

    UserRole(int id, String roleName) {
        this.id = id;
        this.roleName = roleName;
    }

    public int getId() {
        return id;
    }

    public String getRoleName() {
        return roleName;
    }

    public static int roleIdFromName(String roleName) {
        if (roleName == null) return 0;
        for (UserRole role : values()) {
            if (role.roleName.equalsIgnoreCase(roleName)) {
                return role.id;
            }
        }
        return 0;
    }

    public static String roleNameFromId(int roleId) {
        for (UserRole role : values()) {
            if (role.id == roleId) {
                return role.roleName;
            }
        }
        return REGISTRANT.roleName;
    }

    public static Role roleFromName(String roleName) {
        return new Role(roleIdFromName(roleName), roleName);
    }
}
