// Forced recompilation trigger
package service.impl;

import dao.ProfileDAO;
import dao.impl.ProfileDAOImpl;
import dao.impl.RegistrantApplicationDAOImpl;
import dto.registration.RegisterResultDTO;
import model.user.Profile;
import service.AuthService;
import service.EmailService;
import service.UserManagementService;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Period;
import java.util.Set;
import java.util.regex.Pattern;

public class UserManagementServiceImpl implements UserManagementService {

    private static final Pattern CCCD_PATTERN = Pattern.compile("\\d{12}");
    private static final Pattern PHONE_PATTERN = Pattern.compile("0\\d{9}");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Set<String> USER_TYPES = Set.of("student", "free");
    private static final Set<String> LICENSE_CLASSES = Set.of("A1", "A2", "B1", "B2", "C");

    private final AuthService authService = new AuthServiceImpl();
    private final EmailService emailService = new EmailServiceImpl();
    private final ProfileDAO profileDAO = new ProfileDAOImpl();
    private final dao.impl.RegistrantApplicationDAOImpl applicationDAO = new RegistrantApplicationDAOImpl();

    @Override
    public CreateUserResult createUser(
            String fullName, String cccd, String phone, String email,
            String dob, String gender, String address, String userType, String licenseClass) {

        String validationError = validate(fullName, cccd, phone, email, dob, gender, address, userType, licenseClass);
        if (validationError != null) {
            return new CreateUserResult(false, validationError, null, null);
        }

        if (!emailService.isConfigured()) {
            return new CreateUserResult(false, "Chưa cấu hình email gửi đi. Vui lòng cấu hình MAIL_SENDER_USERNAME "
                    + "và MAIL_SENDER_PASSWORD trong file .env, sau đó khởi động lại Tomcat.", null, null);
        }

        boolean female = "female".equals(gender);
        RegisterResultDTO result = authService.register(
                cccd, fullName, phone, dob, address, email, female);

        if (!result.isSuccess()) {
            return new CreateUserResult(false, result.getErrorMessage(), null, null);
        }

        Profile profile = profileDAO.getByGovIdNo(cccd);
        boolean applicationCreated = profile != null
                && applicationDAO.insertPending(profile.getId(), licenseClass, userType);

        if (result.isEmailSent()) {
            String message = "Tạo tài khoản thành công. Thông tin đăng nhập đã được gửi đến " + email + ".";
            if (!applicationCreated) {
                message += " Tuy nhiên, chưa thể tạo hồ sơ GPLX tự động; vui lòng kiểm tra lại cấu hình hạng GPLX.";
            }
            return new CreateUserResult(true, message, null, null);
        } else {
            String message = "Tạo tài khoản thành công nhưng chưa gửi được email. "
                    + "Hãy bàn giao thông tin đăng nhập bên dưới cho học viên.";
            if (!applicationCreated) {
                message += " Hồ sơ GPLX cũng chưa được tạo tự động; vui lòng kiểm tra cấu hình hạng GPLX.";
            }
            return new CreateUserResult(true, message, result.getUsername(), result.getPassword());
        }
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
}

