package service.impl;
import dao.DossierDAO;
import dao.ProfileDAO;
import dao.impl.DossierDAOImpl;
import dao.impl.ProfileDAOImpl;
import dto.CreateUserResultDTO;
import dto.RegisterResultDTO;
import model.Profile;
import service.AuthService;
import service.EmailService;
import service.UserManagementService;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Period;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import enums.RegistrationStatus;
public class UserManagementServiceImpl implements UserManagementService {
    private static final Pattern CCCD_PATTERN = Pattern.compile("\\d{12}");
    private static final Pattern PHONE_PATTERN = Pattern.compile("0\\d{9}");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Set<String> USER_TYPES = Set.of("student", "free");
    private static final Set<String> LICENSE_CLASSES = Set.of(
            "A1", "A2", "B1", "B2", "C1", "C", "D1", "D2", "D");
    private static final Set<String> REQUIRED_DOCUMENTS = Set.of(
            "PORTRAIT", "ID_FRONT", "ID_BACK", "HEALTH_CERTIFICATE");
    private final AuthService authService = new AuthServiceImpl();
    private final EmailService emailService = new EmailServiceImpl();
    private final ProfileDAO profileDAO = new ProfileDAOImpl();
    private final DossierDAO dossierDAO = new DossierDAOImpl();
    @Override
    public CreateUserResultDTO createUser(
            String fullName, String cccd, String phone, String email,
            String dob, String sex, String address, String userType, String licenseClass) {
        String normalizedClass = normalizeLicenceClass(licenseClass);
        String validationError = validate(
                fullName, cccd, phone, email, dob, sex, address, userType, normalizedClass);
        if (validationError != null) {
            return new CreateUserResultDTO(false, validationError, null, null);
        }
        if (!emailService.isConfigured()) {
            return new CreateUserResultDTO(false, "Chưa cấu hình email gửi đi. Vui lòng cấu hình MAIL_SENDER_USERNAME "
                    + "và MAIL_SENDER_PASSWORD trong file .env, sau đó khởi động lại Tomcat.", null, null);
        }
        boolean female = "female".equals(sex);
        RegisterResultDTO result = authService.register(
                cccd, fullName, phone, dob, address, email, female);
        if (!result.isSuccess()) {
            return new CreateUserResultDTO(false, result.getErrorMessage(), null, null);
        }
        Profile profile = profileDAO.getByGovIdNo(cccd);
        Integer profileId = profile == null ? null : profile.getId();
        Integer userId = profile == null ? null : profile.getUserId();
        if (result.isEmailSent()) {
            String message = "Tạo tài khoản thành công. Thông tin đăng nhập đã được gửi đến " + email + ".";
            return new CreateUserResultDTO(true, message, null, null, profileId, userId);
        }
        String message = "Tạo tài khoản thành công nhưng chưa gửi được email. "
                + "Hãy bàn giao thông tin đăng nhập bên dưới cho học viên.";
        return new CreateUserResultDTO(true, message, result.getUsername(), result.getPassword(), profileId, userId);
    }
    @Override
    public CreateUserResultDTO saveManagedDossier(
            int profileId, String licenseClass, String applicantType,
            Map<String, String> documentsByType, int actorUserId) {
        String normalizedClass = normalizeLicenceClass(licenseClass);
        String dossierError = validateDossierDocuments(normalizedClass, documentsByType);
        if (dossierError != null) {
            return new CreateUserResultDTO(false, dossierError, null, null);
        }
        int registrationId = dossierDAO.ensureRegistration(
                profileId, normalizedClass, "STAFF", applicantType);
        if (registrationId <= 0) {
            return new CreateUserResultDTO(false,
                    "Chưa thể tạo hồ sơ GPLX tự động; vui lòng kiểm tra lại cấu hình hạng GPLX.",
                    null, null);
        }
        for (Map.Entry<String, String> entry : documentsByType.entrySet()) {
            if (entry.getValue() == null || entry.getValue().isBlank()) {
                continue;
            }
            if (!dossierDAO.saveDocument(profileId, entry.getKey(), entry.getValue())) {
                return new CreateUserResultDTO(false,
                        "Không thể ghi thông tin tài liệu vào cơ sở dữ liệu.", null, null);
            }
        }
        if (!dossierDAO.updateStatus(registrationId, RegistrationStatus.DUYET.getDisplayName(),
                "Hồ sơ bản giấy và tệp đính kèm đã được Managing Staff đối chiếu khi tạo tài khoản",
                actorUserId)) {
            return new CreateUserResultDTO(false,
                    "Không thể cập nhật trạng thái hồ sơ.", null, null);
        }
        return new CreateUserResultDTO(true, "Hồ sơ đã được lưu và xác minh.", null, null);
    }
    private String validateDossierDocuments(String licenseClass, Map<String, String> documentsByType) {
        if (documentsByType == null) {
            return "Vui lòng tải đủ ảnh chân dung, hai mặt CCCD và giấy khám sức khỏe.";
        }
        for (String documentType : REQUIRED_DOCUMENTS) {
            String url = documentsByType.get(documentType);
            if (url == null || url.isBlank()) {
                return "Vui lòng tải đủ ảnh chân dung, hai mặt CCCD và giấy khám sức khỏe.";
            }
        }
        if (requiresGraduationCertificate(licenseClass)) {
            String graduation = documentsByType.get("GRADUATION_CERTIFICATE");
            if (graduation == null || graduation.isBlank()) {
                return "Hồ sơ hạng ô tô phải có giấy tốt nghiệp/chứng chỉ đào tạo từ trung tâm.";
            }
        }
        return null;
    }
    private String validate(String fullName, String cccd, String phone, String email,
            String dob, String sex, String address, String userType, String licenseClass) {
        if (fullName.isEmpty() || cccd.isEmpty() || phone.isEmpty() || email.isEmpty()
                || dob.isEmpty() || sex.isEmpty() || address.isEmpty()
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
        if (!Set.of("male", "female").contains(sex)) {
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
            int minimumAge = minimumAgeFor(licenseClass);
            int age = Period.between(dateOfBirth, LocalDate.now()).getYears();
            if (age < minimumAge) {
                return "Học viên phải đủ " + minimumAge + " tuổi để đăng ký hạng " + licenseClass + ".";
            }
        } catch (DateTimeException ex) {
            return "Ngày sinh không hợp lệ.";
        }
        return null;
    }
    static String normalizeLicenceClass(String value) {
        if (value == null) {
            return "";
        }
        String licenseClass = value.trim().toUpperCase();
        return switch (licenseClass) {
            case "A" -> "A2";
            case "B" -> "B2";
            default -> licenseClass;
        };
    }
    static boolean requiresGraduationCertificate(String licenseClass) {
        return !Set.of("A1", "A2").contains(normalizeLicenceClass(licenseClass));
    }
    private static int minimumAgeFor(String licenseClass) {
        return switch (normalizeLicenceClass(licenseClass)) {
            case "C1", "C" -> 21;
            case "D1", "D2", "D" -> 24;
            default -> 18;
        };
    }
}
