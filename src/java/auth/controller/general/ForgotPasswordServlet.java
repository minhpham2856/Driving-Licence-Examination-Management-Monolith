package auth.controller.general;

import auth.service.AuthService;
import auth.service.impl.AuthServiceImpl;
import auth.dto.ServiceResult;
import auth.util.ValidationUtil;
import shared.Attributes;
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
        request.getRequestDispatcher("/views/auth/general/forgot-password.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // get recovery email
        String email = request.getParameter("email");

        if (!ValidationUtil.isValidEmail(email)) {
            request.setAttribute(Attributes.Request.ERROR, "Địa chỉ email không hợp lệ.");
            request.getRequestDispatcher("/views/auth/general/forgot-password.jsp").forward(request, response);
            return;
        }

        // recover password
        ServiceResult<Void> result = authService.forgotPassword(email);

        if (result.isSuccess()) {
            request.setAttribute(Attributes.Request.SUCCESS, result.getMessage() != null
                    ? result.getMessage()
                    : "Mật khẩu tạm thời đã được gửi tới email của bạn.");
        } else {
            request.setAttribute(Attributes.Request.ERROR, result.getMessage());
        }

        request.getRequestDispatcher("/views/auth/general/forgot-password.jsp").forward(request, response);
    }
}
