package controller.auth.internal;

import service.AuthService;
import model.User;
import service.impl.AuthServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import enums.UserRole;
import service.RoleService;
import service.impl.RoleServiceImpl;

@WebServlet("/staff/login")
public class LoginServlet extends HttpServlet {

    private final AuthService authService = new AuthServiceImpl();
    private final RoleService roleService = new RoleServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // get session
        HttpSession session = request.getSession();
        // remove msgs if not in session
        String successMsg = (String) session.getAttribute("successMessage");
        if (successMsg != null) {
            request.setAttribute("success", successMsg);
            session.removeAttribute("successMessage");
        }
        String errorMsg = (String) session.getAttribute("errorMessage");
        if (errorMsg != null) {
            request.setAttribute("error", errorMsg);
            session.removeAttribute("errorMessage");
        }
        request.getRequestDispatcher("/views/auth/internal/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // get attributes
        String identifier = request.getParameter("identifier");
        String password = request.getParameter("password");
        // case 1: blank inputs
        if (identifier == null || identifier.isBlank()
                || password == null || password.isBlank()) {
            request.setAttribute("error", "Vui lòng nhập tên đăng nhập/email/số căn cước và mật khẩu.");
            request.getRequestDispatcher("/views/auth/internal/login.jsp").forward(request, response);
            return;
        }
        // case 2: invalid inputs
        User user = authService.login(identifier.trim(), password.trim());
        if (user == null) {
            request.setAttribute("error", "Tên đăng nhập/email/số căn cước hoặc mật khẩu không chính xác.");
            request.getRequestDispatcher("/views/auth/internal/login.jsp").forward(request, response);
        } else {
            // set user for session
            HttpSession session = request.getSession();
            session.setAttribute("user", user);
            // switch dashboards based on role
            String roleName = roleService.getRoleNameById(user.getRoleId());
            UserRole role = UserRole.fromValue(roleName);
            if (null == role) {
                request.setAttribute("error", "Tên đăng nhập/email/số căn cước hoặc mật khẩu không chính xác.");
                request.getRequestDispatcher("/views/auth/internal/login.jsp").forward(request, response);
            } else {
                switch (role) {
                    case MANAGING_STAFF ->
                        response.sendRedirect(request.getContextPath() + "/views/staff/managing/dashboard.jsp");
                    case EXAM_STAFF ->
                        response.sendRedirect(request.getContextPath() + "/views/staff/exam/dashboard.jsp");
                    case EXAMINER ->
                        response.sendRedirect(request.getContextPath() + "/views/examiner/session");
                    case ADMIN ->
                        response.sendRedirect(request.getContextPath() + "/views/admin/dashboard.jsp");
                    default -> {
                        request.setAttribute("error", "Tên đăng nhập/email/số căn cước hoặc mật khẩu không chính xác.");
                        request.getRequestDispatcher("/views/auth/internal/login.jsp").forward(request, response);
                    }
                }
            }
        }
    }
}
