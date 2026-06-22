package Controllers.Auth;

import Models.User;
import Services.AuthService;
import Services.ExaminerSessionContextService;
import Services.Impl.AuthServiceImpl;
import Services.Impl.ExaminerSessionContextServiceImpl;
import Utils.RoleConstants;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/staff/login")
public class StaffLoginServlet extends HttpServlet {

    private final AuthService authService = new AuthServiceImpl();
    private final ExaminerSessionContextService examinerSessionContext = new ExaminerSessionContextServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();

        String errorMsg = (String) session.getAttribute("errorMessage");
        if (errorMsg != null) {
            request.setAttribute("error", errorMsg);
            session.removeAttribute("errorMessage");
        }

        request.getRequestDispatcher("/views/staff/auth/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String identifier = request.getParameter("identifier");
        String password = request.getParameter("password");

        if (identifier == null || identifier.trim().isEmpty() 
                || password == null || password.trim().isEmpty()) {
            request.setAttribute("error", "Vui lòng nhập tên đăng nhập/email/số căn cước và mật khẩu.");
            request.getRequestDispatcher("/views/staff/auth/login.jsp").forward(request, response);
            return;
        }

        try {
            User user = authService.login(identifier.trim(), password.trim());
            if (user == null) {
                request.setAttribute("error", "Tên đăng nhập/email/số căn cước hoặc mật khẩu không chính xác.");
                request.getRequestDispatcher("/views/staff/auth/login.jsp").forward(request, response);
            } else {
                HttpSession session = request.getSession();
                session.setAttribute("user", user);

                String roleName = user.getRole().getRoleName();
                if (RoleConstants.isRole(roleName, RoleConstants.EXAMINER)) {
                    examinerSessionContext.refresh(session, user.getUserId());
                }
                String redirectPath = RoleConstants.getRedirectPath(roleName);
                if (redirectPath == null) {
                    redirectPath = "/staff/login";
                }
                response.sendRedirect(request.getContextPath() + redirectPath);
            }
        } catch (SecurityException ex) {
            request.setAttribute("error", ex.getMessage());
            request.getRequestDispatcher("/views/staff/auth/login.jsp").forward(request, response);
        }
    }
}
