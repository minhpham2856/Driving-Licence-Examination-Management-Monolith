package auth.enums;

import shared.enums.RoleType;

public enum RoleRoute {

    EXAM_STAFF(RoleType.EXAM_STAFF,
            "examstaff",
            "/examstaff/profile",
            "/examstaff/change-password",
            "/examstaff/dashboard"),
    EXAMINER(RoleType.EXAMINER,
            "examiner",
            "/examiner/profile",
            "/examiner/change-password",
            "/examiner/exam"),
    MANAGING_STAFF(RoleType.MANAGING_STAFF,
            "managingstaff",
            "/managingstaff/profile",
            "/managingstaff/change-password",
            "/managingstaff/dashboard"),
    ADMIN(RoleType.ADMIN,
            "admin",
            "/admin/profile",
            "/admin/change-password",
            "/admin/dashboard");

    private final RoleType roleType;
    private final String slug;
    private final String profilePath;
    private final String changePasswordPath;
    private final String homePath;

    RoleRoute(RoleType roleType, String slug, String profilePath,
            String changePasswordPath, String homePath) {
        this.roleType = roleType;
        this.slug = slug;
        this.profilePath = profilePath;
        this.changePasswordPath = changePasswordPath;
        this.homePath = homePath;
    }

    public RoleType getRoleType() {
        return roleType;
    }

    public String getSlug() {
        return slug;
    }

    public String getProfilePath() {
        return profilePath;
    }

    public String getChangePasswordPath() {
        return changePasswordPath;
    }

    public String getHomePath() {
        return homePath;
    }

    public static RoleRoute fromSlug(String slug) {
        if (slug == null || slug.isBlank()) {
            return null;
        }
        String trimmed = slug.trim();
        for (RoleRoute route : values()) {
            if (route.slug.equals(trimmed)) {
                return route;
            }
        }
        return null;
    }

    public static RoleRoute fromRole(RoleType role) {
        if (role == null) {
            return null;
        }
        for (RoleRoute route : values()) {
            if (route.roleType == role) {
                return route;
            }
        }
        return null;
    }
}
