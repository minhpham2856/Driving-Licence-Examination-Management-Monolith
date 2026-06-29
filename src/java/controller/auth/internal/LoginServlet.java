package controller.auth.internal;

import service.AuthService;
import service.ExaminerSessionContextService;
import model.User;
import service.impl.AuthServiceImpl;
import service.impl.ExaminerSessionContextServiceImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import service.RoleService;
import service.impl.RoleServiceImpl;

@WebServlet("/staff/login")
public class LoginServlet extends HttpServlet {

    private final AuthService authService = new AuthServiceImpl();
    private final ExaminerSessionContextService examinerSessionContext = new ExaminerSessionContextServiceImpl();
    private final RoleService roleService = new RoleServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();

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

        String registrationUsername = (String) session.getAttribute("registrationUsername");
        String registrationPassword = (String) session.getAttribute("registrationPassword");
        if (registrationUsername != null) {
            request.setAttribute("registrationUsername", registrationUsername);
            request.setAttribute("registrationPassword", registrationPassword);
            session.removeAttribute("registrationUsername");
            session.removeAttribute("registrationPassword");
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
        if (identifier == null || identifier.trim().isEmpty()
                || password == null || password.trim().isEmpty()) {
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
            HttpSession session = request.getSession();
            session.setAttribute("user", user);

            String roleName = roleService.getRoleNameById(user.getRoleId());
            if ("ManagingStaff".equalsIgnoreCase(roleName)) {
                response.sendRedirect(request.getContextPath() + "/views/staff/managing/dashboard.jsp");
            } else if ("ExamStaff".equalsIgnoreCase(roleName)) {
                response.sendRedirect(request.getContextPath() + "/views/staff/exam/dashboard.jsp");
            } else if ("Examiner".equalsIgnoreCase(roleName)) {
                examinerSessionContext.refresh(session, user.getUserId());
                response.sendRedirect(request.getContextPath() + "/views/examiner/dashboard.jsp");
            } else if ("Admin".equalsIgnoreCase(roleName)) {
                response.sendRedirect(request.getContextPath() + "/views/admin/dashboard.jsp");
            } else {
                request.setAttribute("error", "Tên đăng nhập/email/số căn cước hoặc mật khẩu không chính xác.");
                request.getRequestDispatcher("/views/auth/internal/login.jsp").forward(request, response);
            }
        }
    }
}
