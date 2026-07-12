package auth.controller.general;

import auth.service.AuthService;
import auth.service.impl.AuthServiceImpl;
import dto.ServiceResult;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/forgot-password")
public class ForgotPasswordServlet extends HttpServlet {

    private final AuthService authService = new AuthServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/views/auth/general/forgot-password.jsp")
                .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");

        ServiceResult<Void> result = authService.forgotPassword(email);

        if (result.isSuccess()) {
            request.setAttribute("success", result.getMessage() != null
                    ? result.getMessage()
                    : "Mật khẩu tạm thời đã được gửi tới email của bạn.");
        } else {
            request.setAttribute("error", result.getMessage());
        }

        request.getRequestDispatcher("/views/auth/general/forgot-password.jsp")
                .forward(request, response);
    }
}
