package Controllers.Auth.Public;

import Services.AuthService;
import Services.Impl.AuthServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private final AuthService authService = new AuthServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/views/public/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        String terms = request.getParameter("terms");

        if (username == null || username.trim().isEmpty()
                || email == null || email.trim().isEmpty()
                || password == null || password.trim().isEmpty()
                || confirmPassword == null || confirmPassword.trim().isEmpty()) {
            request.setAttribute("error", "Vui lòng nhập đầy đủ thông tin.");
            request.getRequestDispatcher("/views/public/register.jsp").forward(request, response);
            return;
        }

        if (terms == null) {
            request.setAttribute("error", "Bạn phải đồng ý với Điều khoản và Chính sách bảo mật.");
            request.getRequestDispatcher("/views/public/register.jsp").forward(request, response);
            return;
        }

        if (!password.equals(confirmPassword)) {
            request.setAttribute("error", "Mật khẩu nhập lại không khớp.");
            request.getRequestDispatcher("/views/public/register.jsp").forward(request, response);
            return;
        }

        String registerError = authService.register(username.trim(), email.trim(), password);
        if (registerError != null) {
            request.setAttribute("error", registerError);
            request.getRequestDispatcher("/views/public/register.jsp").forward(request, response);
        } else {
            HttpSession session = request.getSession();
            session.setAttribute("successMessage", "Đăng ký tài khoản thành công!");
            response.sendRedirect(request.getContextPath() + "/login");
        }
    }
}
