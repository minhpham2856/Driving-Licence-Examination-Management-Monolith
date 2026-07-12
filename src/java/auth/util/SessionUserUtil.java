package auth.util;

import shared.model.User;

public final class SessionUserUtil {

    private SessionUserUtil() {
    }

    // Return a session-safe user copy without password hash
    public static User forSession(User user) {
        if (user == null) {
            return null;
        }
        User safe = new User();
        safe.setUserId(user.getUserId());
        safe.setUsername(user.getUsername());
        safe.setEmail(user.getEmail());
        safe.setActive(user.isActive());
        safe.setRoleId(user.getRoleId());
        safe.setRole(user.getRole());
        safe.setProfileId(user.getProfileId());
        safe.setProfile(user.getProfile());
        return safe;
    }
}
