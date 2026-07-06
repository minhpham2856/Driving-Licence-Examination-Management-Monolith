package controller.auth.general;

import dto.ServiceResult;
import dto.payload.RegisterData;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Profile;
import service.AuthService;
import service.impl.AuthServiceImpl;
import util.CredentialsUtil;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private final AuthService authService = new AuthServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/views/auth/general/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String govIdNo = trim(request.getParameter("govIdNo"));
        String fullName = trim(request.getParameter("fullName"));
        String phoneNo = trim(request.getParameter("phoneNo"));
        String dateOfBirth = trim(request.getParameter("dateOfBirth"));
        String address = trim(request.getParameter("address"));
        String email = trim(request.getParameter("email"));
        String sexParam = request.getParameter("sex");
        String terms = request.getParameter("terms");

        if (CredentialsUtil.isBlank(govIdNo) || CredentialsUtil.isBlank(fullName)
                || CredentialsUtil.isBlank(phoneNo) || CredentialsUtil.isBlank(dateOfBirth)
                || CredentialsUtil.isBlank(address) || CredentialsUtil.isBlank(email)) {
            request.setAttribute("error", "Vui lòng nhập đầy đủ thông tin.");
            forwardRegister(request, response);
            return;
        }
        if (terms == null) {
            request.setAttribute("error", "Bạn phải đồng ý với Điều khoản và Chính sách bảo mật.");
            forwardRegister(request, response);
            return;
        }
        if (!CredentialsUtil.isValidCccd(govIdNo)) {
            request.setAttribute("error", "Số CCCD phải gồm đúng 12 chữ số.");
            forwardRegister(request, response);
            return;
        }
        if (!CredentialsUtil.isValidPhone(phoneNo)) {
            request.setAttribute("error", "Số điện thoại phải bắt đầu bằng 0 và gồm đúng 10 chữ số.");
            forwardRegister(request, response);
            return;
        }
        if (!CredentialsUtil.isValidEmail(email)) {
            request.setAttribute("error", "Địa chỉ email không hợp lệ.");
            forwardRegister(request, response);
            return;
        }
        LocalDate dob = CredentialsUtil.parseIsoDate(dateOfBirth).orElse(null);
        if (dob == null) {
            request.setAttribute("error", "Ngày sinh không hợp lệ.");
            forwardRegister(request, response);
            return;
        }
        if (dob.isAfter(LocalDate.now())) {
            request.setAttribute("error", "Ngày sinh không được nằm trong tương lai.");
            forwardRegister(request, response);
            return;
        }

        Profile profile = new Profile();
        profile.setGovernmentIdNumber(govIdNo);
        profile.setFullName(fullName);
        profile.setPhoneNumber(phoneNo);
        profile.setAddress(address);
        profile.setSex("1".equals(sexParam));
        profile.setDateOfBirth(Timestamp.valueOf(dob.atStartOfDay()));

        ServiceResult<RegisterData> result = authService.register(profile, email);
        if (!result.isSuccess()) {
            request.setAttribute("error", result.getMessage());
            forwardRegister(request, response);
            return;
        }
        RegisterData data = result.getData();
        HttpSession session = request.getSession();
        if (data.isEmailSent()) {
            session.setAttribute("successMessage",
                    "Đăng ký thành công! Kiểm tra email để lấy tên đăng nhập và mật khẩu.");
        } else {
            session.setAttribute("successMessage",
                    "Đăng ký thành công! Không gửi được email - vui lòng lưu thông tin đăng nhập bên dưới.");
            session.setAttribute("registrationUsername", data.getUsername());
            session.setAttribute("registrationPassword", data.getPassword());
        }
        response.sendRedirect(request.getContextPath() + "/login");
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private void forwardRegister(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/views/auth/general/register.jsp").forward(request, response);
    }
}
