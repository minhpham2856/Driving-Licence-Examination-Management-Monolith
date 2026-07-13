package auth.controller.general;

import auth.dto.ServiceResult;
import auth.dto.StaffAccountViewDTO;
import auth.dto.UpdateProfileDTO;
import auth.dto.UserDTO;
import auth.service.AuditService;
import auth.service.ProfileService;
import auth.service.impl.AuditServiceImpl;
import auth.service.impl.ProfileServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import shared.Attributes;
import shared.enums.AuditAction;
import shared.enums.AuditEntity;
import shared.enums.RoleType;
import shared.model.Profile;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@WebServlet("/profile")
public class ProfileServlet extends HttpServlet {

    private final ProfileService profileService = new ProfileServiceImpl();
    private final AuditService auditService = new AuditServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        UserDTO sessionUser = requireUser(request, response);
        if (sessionUser == null) {
            return;
        }
        publishAccount(request, sessionUser);
        request.getRequestDispatcher("/views/auth/general/profile.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        UserDTO sessionUser = requireUser(request, response);
        if (sessionUser == null) {
            return;
        }

        UpdateProfileDTO input = new UpdateProfileDTO();
        input.setFullName(request.getParameter("fullName"));
        input.setPhoneNumber(request.getParameter("phoneNumber"));
        input.setAddress(request.getParameter("address"));
        input.setDateOfBirth(parseDateOfBirth(request.getParameter("dateOfBirth")));
        String sexRaw = request.getParameter("sex");
        if (sexRaw != null && !sexRaw.isBlank()) {
            input.setSex("1".equals(sexRaw) || "true".equalsIgnoreCase(sexRaw)
                    || "male".equalsIgnoreCase(sexRaw));
        }

        ServiceResult<Profile> result = profileService.updateMyProfile(sessionUser.getUserId(), input);
        if (result.isSuccess()) {
            auditService.logAction(sessionUser.getUserId(), AuditAction.UPDATE, AuditEntity.DOSSIER,
                    "Cập nhật hồ sơ cá nhân", sessionUser.getUserId());
            request.setAttribute(Attributes.Request.MESSAGE_TYPE, "success");
        } else {
            request.setAttribute(Attributes.Request.MESSAGE_TYPE, "danger");
        }
        request.setAttribute(Attributes.Request.MESSAGE,
                result.getMessage() != null ? result.getMessage() : "Không cập nhật được hồ sơ.");

        publishAccount(request, sessionUser);
        request.getRequestDispatcher("/views/auth/general/profile.jsp").forward(request, response);
    }

    private UserDTO requireUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        Object raw = session == null ? null : session.getAttribute(Attributes.Session.USER);
        if (!(raw instanceof UserDTO)) {
            response.sendRedirect(request.getContextPath() + resolveLoginPath(null));
            return null;
        }
        return (UserDTO) raw;
    }

    private void publishAccount(HttpServletRequest request, UserDTO sessionUser) {
        StaffAccountViewDTO view = profileService.getAccountView(sessionUser.getUserId());
        request.setAttribute(Attributes.Request.ACCOUNT_USER, view.getUser() != null ? view.getUser() : sessionUser);
        request.setAttribute(Attributes.Request.ACCOUNT_PROFILE, view.getProfile());

        HttpSession session = request.getSession(false);
        if (session != null && view.getProfile() != null) {
            session.setAttribute(Attributes.Session.USER_PROFILE, view.getProfile());
            sessionUser.setProfile(view.getProfile());
            session.setAttribute(Attributes.Session.USER, sessionUser);
        }

        request.setAttribute(Attributes.Request.BACK_URL, resolveHomePath(sessionUser));
    }

    private static String resolveLoginPath(UserDTO user) {
        if (user == null || user.getRole() == null) {
            return "/staff/login";
        }
        RoleType role = RoleType.fromValue(user.getRole().getRoleName());
        if (role == RoleType.REGISTRANT) {
            return "/login";
        }
        return "/staff/login";
    }

    private static String resolveHomePath(UserDTO user) {
        if (user == null || user.getRole() == null) {
            return "/home";
        }
        RoleType role = RoleType.fromValue(user.getRole().getRoleName());
        if (role == null) {
            return "/home";
        }
        return switch (role) {
            case EXAM_STAFF -> "/views/staff/examstaff/dashboard";
            case EXAMINER -> "/views/examiner/dashboard";
            case MANAGING_STAFF -> "/views/staff/managing/dashboard";
            case ADMIN -> "/admin/dashboard";
            case REGISTRANT, CANDIDATE -> "/views/registrant/dashboard.jsp";
            default -> "/home";
        };
    }

    private static Timestamp parseDateOfBirth(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Timestamp.valueOf(LocalDate.parse(raw.trim()).atStartOfDay());
        } catch (DateTimeParseException ex) {
            return null;
        }
    }
}
