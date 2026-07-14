package auth.controller.general;

import auth.dto.RegisterResultDTO;
import shared.Attributes;
import shared.model.Profile;
import auth.service.AuthService;
import auth.service.impl.AuthServiceImpl;
import static auth.util.FormatUtil.formatString;
import auth.dto.ServiceResult;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import auth.util.ValidationUtil;

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

        // get form values
        String govIdNo = formatString(request.getParameter("govIdNo"));
        String fullName = formatString(request.getParameter("fullName"));
        String phoneNo = formatString(request.getParameter("phoneNo"));
        String dateOfBirth = formatString(request.getParameter("dateOfBirth"));
        String address = formatString(request.getParameter("address"));
        String email = formatString(request.getParameter("email"));
        String sexParam = request.getParameter("sex");
        String terms = request.getParameter("terms");

        // validate required fields
        if (ValidationUtil.isBlank(govIdNo)
                || ValidationUtil.isBlank(fullName)
                || ValidationUtil.isBlank(phoneNo)
                || ValidationUtil.isBlank(dateOfBirth)
                || ValidationUtil.isBlank(address)
                || ValidationUtil.isBlank(email)) {
            forwardWithError(request, response, "Vui lòng nhập đầy đủ thông tin.");
            return;
        }

        // validate terms agreement
        if (terms == null) {
            forwardWithError(request, response, "Bạn phải đồng ý với Điều khoản và Chính sách bảo mật.");
            return;
        }

        // validate government id
        if (!ValidationUtil.isValidCccd(govIdNo)) {
            forwardWithError(request, response, "Số căn cước phải gồm đúng 12 chữ số.");
            return;
        }

        // validate phone number
        if (!ValidationUtil.isValidPhone(phoneNo)) {
            forwardWithError(request, response, "Số điện thoại không hợp lệ.");
            return;
        }

        // validate email
        if (!ValidationUtil.isValidEmail(email)) {
            forwardWithError(request, response, "Địa chỉ email không hợp lệ.");
            return;
        }

        // parse date of birth
        LocalDate dob = ValidationUtil.parseDate(dateOfBirth);

        // validate date format
        if (dob == null) {
            forwardWithError(request, response, "Ngày sinh không hợp lệ.");
            return;
        }

        // validate future date
        if (dob.isAfter(LocalDate.now())) {
            forwardWithError(request, response, "Ngày sinh không thể chọn.");
            return;
        }

        // build profile
        Profile profile = new Profile();
        profile.setGovernmentIdNumber(govIdNo);
        profile.setFullName(fullName);
        profile.setPhoneNumber(phoneNo);
        profile.setAddress(address);
        profile.setSex("1".equals(sexParam));
        profile.setDateOfBirth(Timestamp.valueOf(dob.atStartOfDay()));

        // register account
        ServiceResult<RegisterResultDTO> result = authService.register(profile, email);

        // registration failed
        if (!result.isSuccess()) {
            forwardWithError(request, response, result.getMessage());
            return;
        }

        // get registration result
        RegisterResultDTO data = result.getData();

        // store success message
        HttpSession session = request.getSession();

        if (data.isEmailSent()) {
            session.setAttribute(Attributes.Session.SUCCESS_MESSAGE,
                    "Đăng ký thành công! Kiểm tra email để lấy thông tin đăng nhập.");
        } else {
            session.setAttribute(
                    Attributes.Session.SUCCESS_MESSAGE,
                    "Đăng ký thành công! Không gửi được email - vui lòng lưu thông tin đăng nhập bên dưới.");
            session.setAttribute(Attributes.Session.REGISTRATION_USERNAME, data.getUsername());
            session.setAttribute(Attributes.Session.REGISTRATION_PASSWORD, data.getPassword());
        }

        // redirect to login page
        response.sendRedirect(request.getContextPath() + "/login");
    }

    // forward back to register page with an error
    private void forwardWithError(HttpServletRequest request, HttpServletResponse response, String errorMessage)
            throws ServletException, IOException {
        request.setAttribute(Attributes.Request.ERROR, errorMessage);
        request.getRequestDispatcher("/views/auth/general/register.jsp").forward(request, response);
    }
}

