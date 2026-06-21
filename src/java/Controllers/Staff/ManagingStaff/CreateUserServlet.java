package Controllers.Staff.ManagingStaff;

import DAOs.Impl.ProfileDAOImpl;
import DAOs.Impl.RegistrantApplicationDAOImpl;
import DTOs.RegisterResultDTO;
import Models.Profile;
import Models.User;
import Services.AuthService;
import Services.EmailService;
import Services.Impl.AuthServiceImpl;
import Services.Impl.EmailServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Period;
import java.util.Set;
import java.util.regex.Pattern;

@WebServlet("/manager/create-user")
public class CreateUserServlet extends HttpServlet {

    private static final String VIEW = "/views/staff/managingstaff/create-user.jsp";
    private static final Pattern CCCD_PATTERN = Pattern.compile("\\d{12}");
    private static final Pattern PHONE_PATTERN = Pattern.compile("0\\d{9}");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Set<String> USER_TYPES = Set.of("student", "free");
    private static final Set<String> LICENSE_CLASSES = Set.of("A1", "A2", "B1", "B2", "C");

    private final AuthService authService = new AuthServiceImpl();
    private final EmailService emailService = new EmailServiceImpl();
    private final ProfileDAOImpl profileDAO = new ProfileDAOImpl();
    private final RegistrantApplicationDAOImpl applicationDAO = new RegistrantApplicationDAOImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!hasAccess(request, response)) {
            return;
        }

        HttpSession session = request.getSession();
        moveFlashAttribute(session, request, "createUserSuccess");
        moveFlashAttribute(session, request, "createdUsername");
        moveFlashAttribute(session, request, "createdPassword");
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!hasAccess(request, response)) {
            return;
        }

        String fullName = trim(request.getParameter("fullName"));
        String cccd = trim(request.getParameter("cccd"));
        String phone = trim(request.getParameter("phone"));
        String email = trim(request.getParameter("email")).toLowerCase();
        String dob = trim(request.getParameter("dob"));
        String gender = trim(request.getParameter("gender"));
        String address = trim(request.getParameter("address"));
        String userType = trim(request.getParameter("userType"));
        String licenseClass = trim(request.getParameter("licenseClass")).toUpperCase();

        String validationError = validate(
                fullName, cccd, phone, email, dob, gender, address, userType, licenseClass);
        if (validationError != null) {
            request.setAttribute("createUserError", validationError);
            request.getRequestDispatcher(VIEW).forward(request, response);
            return;
        }

        if (!emailService.isConfigured()) {
            request.setAttribute("createUserError",
                    "Chưa cấu hình email gửi đi. Vui lòng cấu hình MAIL_SENDER_USERNAME "
                    + "và MAIL_SENDER_PASSWORD trong file .env, sau đó khởi động lại Tomcat.");
            request.getRequestDispatcher(VIEW).forward(request, response);
            return;
        }

        boolean female = "female".equals(gender);
        RegisterResultDTO result = authService.register(
                cccd, fullName, phone, dob, address, email, female);

        if (!result.isSuccess()) {
            request.setAttribute("createUserError", result.getErrorMessage());
            request.getRequestDispatcher(VIEW).forward(request, response);
            return;
        }

        HttpSession session = request.getSession();
        Profile profile = profileDAO.getByGovIdNo(cccd);
        boolean applicationCreated = profile != null
                && applicationDAO.insertPending(profile.getId(), licenseClass, userType);

        if (result.isEmailSent()) {
            String message = "Tạo tài khoản thành công. Thông tin đăng nhập đã được gửi đến " + email + ".";
            if (!applicationCreated) {
                message += " Tuy nhiên, chưa thể tạo hồ sơ GPLX tự động; vui lòng kiểm tra lại cấu hình hạng GPLX.";
            }
            session.setAttribute("createUserSuccess", message);
        } else {
            String message = "Tạo tài khoản thành công nhưng chưa gửi được email. "
                    + "Hãy bàn giao thông tin đăng nhập bên dưới cho học viên.";
            if (!applicationCreated) {
                message += " Hồ sơ GPLX cũng chưa được tạo tự động; vui lòng kiểm tra cấu hình hạng GPLX.";
            }
            session.setAttribute("createUserSuccess", message);
            session.setAttribute("createdUsername", result.getUsername());
            session.setAttribute("createdPassword", result.getPassword());
        }
        response.sendRedirect(request.getContextPath() + "/manager/create-user");
    }

    private String validate(String fullName, String cccd, String phone, String email,
            String dob, String gender, String address, String userType, String licenseClass) {
        if (fullName.isEmpty() || cccd.isEmpty() || phone.isEmpty() || email.isEmpty()
                || dob.isEmpty() || gender.isEmpty() || address.isEmpty()
                || userType.isEmpty() || licenseClass.isEmpty()) {
            return "Vui lòng nhập đầy đủ thông tin bắt buộc.";
        }
        if (fullName.length() < 3 || fullName.length() > 50) {
            return "Họ và tên phải có từ 3 đến 50 ký tự.";
        }
        if (!CCCD_PATTERN.matcher(cccd).matches()) {
            return "Số CCCD phải gồm đúng 12 chữ số.";
        }
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            return "Số điện thoại phải bắt đầu bằng 0 và gồm đúng 10 chữ số.";
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return "Địa chỉ email không hợp lệ.";
        }
        if (!Set.of("male", "female").contains(gender)) {
            return "Giới tính không hợp lệ.";
        }
        if (address.length() < 5 || address.length() > 150) {
            return "Địa chỉ phải có từ 5 đến 150 ký tự.";
        }
        if (!USER_TYPES.contains(userType)) {
            return "Phân loại học viên không hợp lệ.";
        }
        if (!LICENSE_CLASSES.contains(licenseClass)) {
            return "Hạng GPLX không hợp lệ.";
        }

        try {
            LocalDate dateOfBirth = LocalDate.parse(dob);
            if (dateOfBirth.isAfter(LocalDate.now())) {
                return "Ngày sinh không được nằm trong tương lai.";
            }
            int minimumAge = "C".equals(licenseClass) ? 21 : 18;
            int age = Period.between(dateOfBirth, LocalDate.now()).getYears();
            if (age < minimumAge) {
                return "Học viên phải đủ " + minimumAge + " tuổi để đăng ký hạng " + licenseClass + ".";
            }
        } catch (DateTimeException ex) {
            return "Ngày sinh không hợp lệ.";
        }

        return null;
    }

    private boolean hasAccess(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("user");
        if (user == null) {
            request.getSession(true).setAttribute("errorMessage",
                    "Bạn cần đăng nhập để truy cập chức năng này.");
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }

        String roleName = user.getRole() == null ? "" : user.getRole().getRoleName();
        if (!"ManagingStaff".equalsIgnoreCase(roleName) && !"Admin".equalsIgnoreCase(roleName)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Bạn không có quyền tạo tài khoản học viên.");
            return false;
        }
        return true;
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private static void moveFlashAttribute(HttpSession session, HttpServletRequest request,
            String attributeName) {
        Object value = session.getAttribute(attributeName);
        if (value != null) {
            request.setAttribute(attributeName, value);
            session.removeAttribute(attributeName);
        }
    }
}
