package auth.controller.internal;

import auth.dto.UserDTO;
import shared.Attributes;
import shared.model.Profile;
import shared.model.User;
import auth.service.AuthService;
import auth.service.ProfileService;
import auth.service.impl.AuthServiceImpl;
import auth.service.impl.ProfileServiceImpl;
import static auth.util.FormatUtil.formatString;
import shared.enums.RoleType;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/staff/login")
public class LoginServlet extends HttpServlet {

    private final AuthService authService = new AuthServiceImpl();
    private final ProfileService profileService = new ProfileServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // transfer session messages
        sessionToRequest(request);

        request.getRequestDispatcher("/views/auth/internal/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // get inputs
        String identifier = formatString(request.getParameter("identifier"));
        String password = formatString(request.getParameter("password"));

        // validate required inputs
        if (identifier == null || password == null) {
            forwardWithError(request, response, "Vui lòng nhập tên đăng nhập/email/số căn cước và mật khẩu.");
            return;
        }

        // authenticate user
        User user = authService.login(identifier, password);

        // validate credentials
        if (user == null || user.getRole() == null) {
            forwardWithError(request, response, "Tên đăng nhập/email/số căn cước hoặc mật khẩu không chính xác.");
            return;
        }

        // complete login
        login(request, response, user);
    }

    // complete successful login
    private void login(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {

        // get user role
        RoleType role = RoleType.fromValue(user.getRole().getRoleName());

        // validate role
        if (role == null) {
            forwardWithError(request, response, "Tên đăng nhập/email/số căn cước hoặc mật khẩu không chính xác.");
            return;
        }

        // store session identity as UserDTO (no password hash)
        HttpSession session = request.getSession();
        UserDTO userDto = UserDTO.fromUser(user);
        Profile profile = profileService.getByUserId(user.getUserId());
        if (profile != null) {
            userDto.setProfile(profile);
            session.setAttribute(Attributes.Session.USER_PROFILE, profile);
        }
        session.setAttribute(Attributes.Session.USER, userDto);

        // redirect by role
        switch (role) {
            case MANAGING_STAFF:
                response.sendRedirect(request.getContextPath() + "/managingstaff/dashboard");
                break;
            case EXAM_STAFF:
                response.sendRedirect(request.getContextPath() + "/examstaff/dashboard");
                break;
            case EXAMINER:
                response.sendRedirect(request.getContextPath() + "/examiner/exam");
                break;
            case ADMIN:
                response.sendRedirect(request.getContextPath() + "/admin/dashboard");
                break;
            default:
                forwardWithError(request, response, "Tên đăng nhập/email/số căn cước hoặc mật khẩu không chính xác.");
                break;
        }
    }

    // forward with an error message
    private void forwardWithError(HttpServletRequest request, HttpServletResponse response, String errorMessage)
            throws ServletException, IOException {

        request.setAttribute(Attributes.Request.ERROR, errorMessage);
        request.getRequestDispatcher("/views/auth/internal/login.jsp").forward(request, response);
    }

    // move session messages to request
    private void sessionToRequest(HttpServletRequest request) {
        HttpSession session = request.getSession();
        transferAttribute(request, session, Attributes.Session.SUCCESS_MESSAGE, Attributes.Request.SUCCESS);
        transferAttribute(request, session, Attributes.Session.ERROR_MESSAGE, Attributes.Request.ERROR);
    }

    // move one session attribute to request
    private void transferAttribute(HttpServletRequest request, HttpSession session,
            String sessionAttribute, String requestAttribute) {
        Object value = session.getAttribute(sessionAttribute);

        if (value != null) {
            request.setAttribute(requestAttribute, value);
            session.removeAttribute(sessionAttribute);
        }
    }
}

