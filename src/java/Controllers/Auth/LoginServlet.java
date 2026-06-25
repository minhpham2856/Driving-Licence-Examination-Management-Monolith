package Controllers.Auth;

import Models.User;
import Services.AuthService;
import Services.ExaminerSessionContextService;
import Services.Impl.AuthServiceImpl;
import Services.Impl.ExaminerSessionContextServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final AuthService authService = new AuthServiceImpl();
    private final ExaminerSessionContextService examinerSessionContext = new ExaminerSessionContextServiceImpl();

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

        request.getRequestDispatcher("/views/landing/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // get attributes
        String identifier = request.getParameter("identifier"); // lets user login with 
        String password = request.getParameter("password");

        // case 1: blank inputs
        if (identifier == null || identifier.trim().isEmpty() 
                || password == null || password.trim().isEmpty()) {
            request.setAttribute("error", "Vui lòng nhập tên đăng nhập/email/số căn cước và mật khẩu.");
            request.getRequestDispatcher("/views/landing/login.jsp").forward(request, response);
            return;
        }

        // case 2: invalid inputs
        User user = authService.login(identifier.trim(), password.trim());
        if (user == null) {
            request.setAttribute("error", "Tên đăng nhập/email/số căn cước hoặc mật khẩu không chính xác.");
            request.getRequestDispatcher("/views/landing/login.jsp").forward(request, response);
        } else {
            HttpSession session = request.getSession();
            session.setAttribute("user", user);

            String roleName = user.getRole() != null ? user.getRole().getRoleName() : "Registrant";
            if ("ManagingStaff".equalsIgnoreCase(roleName)) {
                response.sendRedirect(request.getContextPath() + "/views/staff/managingstaff/dashboard.jsp");
            } else if ("ExamStaff".equalsIgnoreCase(roleName)) {
                response.sendRedirect(request.getContextPath() + "/views/staff/examstaff/dashboard");
            } else if ("Examiner".equalsIgnoreCase(roleName)) {
                examinerSessionContext.refresh(session, user.getId());
                response.sendRedirect(request.getContextPath() + "/views/examiner/dashboard");
            } else if ("Admin".equalsIgnoreCase(roleName)) {
                response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            } else {
                response.sendRedirect(request.getContextPath() + "/views/registrant/dashboard.jsp");
            }
        }
    }
}
