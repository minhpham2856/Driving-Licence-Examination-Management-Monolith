package auth.controller.internal;

import auth.service.AuthService;
import auth.model.User;
import auth.service.impl.AuthServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import enums.RoleType;

@WebServlet("/staff/login")
public class LoginServlet extends HttpServlet {

    private final AuthService authService = new AuthServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        transferSessionMessagesToRequest(request);
        request.getRequestDispatcher("/views/auth/internal/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String identifier = request.getParameter("identifier");
        String password = request.getParameter("password");

        if (isInputInvalid(identifier, password)) {
            forwardWithError(request, response, "Vui lòng nhập tên đăng nhập/email/số căn cước và mật khẩu.");
            return;
        }

        User user = authService.login(identifier.trim(), password.trim());
        
        if (user == null || user.getRole() == null) {
            forwardWithError(request, response, "Tên đăng nhập/email/số căn cước hoặc mật khẩu không chính xác.");
            return;
        }

        handleSuccessfulLogin(request, response, user);
    }

    private void handleSuccessfulLogin(HttpServletRequest request, HttpServletResponse response, User user) throws IOException, ServletException {
        RoleType role = RoleType.fromValue(user.getRole().getRoleName());
        if (role == null) {
            forwardWithError(request, response, "Tên đăng nhập/email/số căn cước hoặc mật khẩu không chính xác.");
            return;
        }

        request.getSession().setAttribute("user", user);
        
        switch (role) {
            case MANAGING_STAFF -> response.sendRedirect(request.getContextPath() + "/views/staff/managing/dashboard");
            case EXAM_STAFF -> response.sendRedirect(request.getContextPath() + "/views/staff/exam/dashboard");
            case EXAMINER -> response.sendRedirect(request.getContextPath() + "/views/examiner/exam");
            case ADMIN -> response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            default -> forwardWithError(request, response, "Tên đăng nhập/email/số căn cước hoặc mật khẩu không chính xác.");
        }
    }

    private boolean isInputInvalid(String identifier, String password) {
        return identifier == null || identifier.isBlank() || password == null || password.isBlank();
    }

    private void forwardWithError(HttpServletRequest request, HttpServletResponse response, String errorMessage) throws ServletException, IOException {
        request.setAttribute("error", errorMessage);
        request.getRequestDispatcher("/views/auth/internal/login.jsp").forward(request, response);
    }

    private void transferSessionMessagesToRequest(HttpServletRequest request) {
        HttpSession session = request.getSession();
        transferAttribute(request, session, "successMessage", "success");
        transferAttribute(request, session, "errorMessage", "error");
    }

    private void transferAttribute(HttpServletRequest request, HttpSession session, String sessionAttr, String requestAttr) {
        String value = (String) session.getAttribute(sessionAttr);
        if (value != null) {
            request.setAttribute(requestAttr, value);
            session.removeAttribute(sessionAttr);
        }
    }
}

