package Controllers.Auth;

import Services.AuthService;
import Services.Impl.AuthServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/legacy/forgot-password")
public class ForgotPasswordServlet extends HttpServlet {

    private final AuthService authService = new AuthServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/views/landing/forgot-password.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = request.getParameter("email");

        if (email == null || email.trim().isEmpty()) {
            request.setAttribute("error", "Vui lòng nhập địa chỉ email.");
            request.getRequestDispatcher("/views/landing/forgot-password.jsp").forward(request, response);
            return;
        }

        String resetError = authService.forgotPassword(email.trim());
        if (resetError != null) {
            request.setAttribute("error", resetError);
        } else {
            request.setAttribute("success", "Mật khẩu tạm thời đã được gửi tới email của bạn.");
        }

        request.getRequestDispatcher("/views/landing/forgot-password.jsp").forward(request, response);
    }
}
