package auth.controller.internal;

import shared.model.User;
import auth.service.AuthService;
import auth.service.impl.AuthServiceImpl;
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

        // store logged in user
        HttpSession session = request.getSession();
        session.setAttribute("user", user);

        // redirect by role
        switch (role) {
            case MANAGING_STAFF:
                response.sendRedirect(request.getContextPath() + "/views/staff/managing/dashboard");
                break;
            case EXAM_STAFF:
                response.sendRedirect(request.getContextPath() + "/views/staff/exam/dashboard");
                break;
            case EXAMINER:
                response.sendRedirect(request.getContextPath() + "/views/examiner/exam");
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

        request.setAttribute("error", errorMessage);
        request.getRequestDispatcher("/views/auth/internal/login.jsp").forward(request, response);
    }

    // move session messages to request
    private void sessionToRequest(HttpServletRequest request) {
        HttpSession session = request.getSession();
        transferAttribute(request, session, "successMessage", "success");
        transferAttribute(request, session, "errorMessage", "error");
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

