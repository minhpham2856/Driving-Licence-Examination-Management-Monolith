package managingstaff.util;

import auth.dto.UserDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import shared.Attributes;
import shared.enums.RoleType;

public final class SessionUtil {
    private SessionUtil() { }

    public static UserDTO getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return null;
        Object raw = session.getAttribute(Attributes.Session.USER);
        return raw instanceof UserDTO ? (UserDTO) raw : null;
    }

    public static int currentUserId(HttpSession session) {
        if (session == null) return 0;
        Object raw = session.getAttribute(Attributes.Session.USER);
        return raw instanceof UserDTO ? ((UserDTO) raw).getUserId() : 0;
    }

    public static boolean isManager(UserDTO user) {
        if (user == null || user.getRole() == null) return false;
        RoleType role = RoleType.fromValue(user.getRole().getRoleName());
        if (role == RoleType.MANAGING_STAFF || role == RoleType.ADMIN) return true;
        String value = user.getRole().getRoleName();
        return "ManagingStaff".equalsIgnoreCase(value) || "Admin".equalsIgnoreCase(value);
    }
}
