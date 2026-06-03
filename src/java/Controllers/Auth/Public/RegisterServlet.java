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
        // get attributes
        String govIdNo = request.getParameter("govIdNo");
        String fullName = request.getParameter("fullName");
        String phoneNo = request.getParameter("phoneNo");
        String dateOfBirth = request.getParameter("dateOfBirth");
        String address = request.getParameter("address");
        String email = request.getParameter("email");
        String genderParam = request.getParameter("gender");
        String terms = request.getParameter("terms");

        // blank inputs
        if (isBlank(govIdNo) || isBlank(fullName) || isBlank(phoneNo)
                || isBlank(dateOfBirth) || isBlank(address) || isBlank(email)) {
            request.setAttribute("error", "Vui lòng nhập đầy đủ thông tin.");
            forwardRegister(request, response);
            return;
        }

        // terms and condition validation
        if (terms == null) {
            request.setAttribute("error", "Bạn phải đồng ý với Điều khoản và Chính sách bảo mật.");
            forwardRegister(request, response);
            return;
        }

        boolean gender = "1".equals(genderParam);
     
        // attempt to register
        String registerError = authService.register(
                govIdNo.trim(),
                fullName.trim(),
                phoneNo.trim(),
                dateOfBirth.trim(),
                address.trim(),
                email.trim(),
                gender
        );

        if (registerError != null) {
            request.setAttribute("error", registerError);
            forwardRegister(request, response);
        } else {
            HttpSession session = request.getSession();
            session.setAttribute("successMessage",
                    "Đăng ký thành công! Truy cập email của bạn để xác minh đăng ký.");
            response.sendRedirect(request.getContextPath() + "/login");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void forwardRegister(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/views/public/register.jsp").forward(request, response);
    }
}
