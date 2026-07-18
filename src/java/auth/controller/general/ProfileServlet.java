package auth.controller.general;

import auth.dto.ServiceResult;
import auth.dto.AccountDTO;
import auth.dto.UpdateProfileDTO;
import auth.dto.UserDTO;
import auth.service.AuditService;
import auth.service.ProfileService;
import auth.service.impl.AuditServiceImpl;
import auth.service.impl.ProfileServiceImpl;
import auth.util.FormatUtil;
import auth.util.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import shared.Attributes;
import shared.enums.AuditAction;
import shared.enums.AuditEntity;
import shared.model.Profile;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;

@WebServlet(urlPatterns = {
    "/examstaff/profile",
    "/examiner/profile",
    "/managingstaff/profile",
    "/admin/profile"
})
public class ProfileServlet extends HttpServlet {

    private final ProfileService profileService = new ProfileServiceImpl();
    private final AuditService auditService = new AuditServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // filter already validated session + role; load account for JSP
        UserDTO sessionUser = sessionUser(request);
        publishAccount(request, sessionUser);
        request.getRequestDispatcher("/views/auth/general/profile.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        UserDTO sessionUser = sessionUser(request);

        // bind form fields into update DTO
        UpdateProfileDTO input = new UpdateProfileDTO();
        input.setUsername(request.getParameter("username"));
        input.setEmail(request.getParameter("email"));
        input.setFullName(request.getParameter("fullName"));
        input.setPhoneNumber(request.getParameter("phoneNumber"));
        input.setGovernmentIdNumber(request.getParameter("governmentIdNumber"));
        input.setAddress(request.getParameter("address"));
        LocalDate dob = ValidationUtil.parseDate(request.getParameter("dateOfBirth"));
        if (dob != null) {
            input.setDateOfBirth(Timestamp.valueOf(dob.atStartOfDay()));
        }
        String sex = request.getParameter("sex");
        if (FormatUtil.formatString(sex) != null) {
            input.setSex("1".equals(sex)
                    || "true".equalsIgnoreCase(sex)
                    || "male".equalsIgnoreCase(sex));
        }

        // persist profile changes
        ServiceResult<Profile> result = profileService.updateProfile(sessionUser.getUserId(), input);
        if (result.isSuccess()) {
            auditService.logAction(sessionUser.getUserId(),
                    AuditAction.UPDATE,
                    AuditEntity.DOSSIER,
                    "Cập nhật hồ sơ cá nhân",
                    sessionUser.getUserId());
            request.setAttribute(Attributes.Request.MESSAGE_TYPE, "success");
        } else {
            request.setAttribute(Attributes.Request.MESSAGE_TYPE, "danger");
        }

        request.setAttribute(Attributes.Request.MESSAGE,
                result.getMessage() != null
                ? result.getMessage()
                : "Không cập nhật được hồ sơ.");

        // refresh account data after save attempt
        publishAccount(request, sessionUser);
        request.getRequestDispatcher("/views/auth/general/profile.jsp").forward(request, response);
    }

    // session user set by login; filter guarantees non-null here
    private static UserDTO sessionUser(HttpServletRequest request) {
        return (UserDTO) request.getSession(false).getAttribute(Attributes.Session.USER);
    }

    // load account from DB and sync session profile for sidebar display
    private void publishAccount(HttpServletRequest request, UserDTO sessionUser) {
        AccountDTO account = profileService.getAccount(sessionUser.getUserId());

        UserDTO accountUser = account.getUser() != null ? account.getUser() : sessionUser;
        // getById does not load Role - keep role from the logged-in session
        if (accountUser.getRole() == null && sessionUser.getRole() != null) {
            accountUser.setRole(sessionUser.getRole());
        }
        if (accountUser.getEmail() == null || accountUser.getEmail().isBlank()) {
            accountUser.setEmail(sessionUser.getEmail());
        }
        request.setAttribute(Attributes.Request.ACCOUNT_USER, accountUser);
        request.setAttribute(Attributes.Request.ACCOUNT_PROFILE, account.getProfile());

        HttpSession session = request.getSession(false);
        if (session != null) {
            if (account.getProfile() != null) {
                session.setAttribute(Attributes.Session.USER_PROFILE, account.getProfile());
                sessionUser.setProfile(account.getProfile());
            }
            // keep login role; refresh username/email from DB after save
            sessionUser.setUsername(accountUser.getUsername());
            sessionUser.setEmail(accountUser.getEmail());
            session.setAttribute(Attributes.Session.USER, sessionUser);
        }
    }
}
