package service.impl;

import dao.DocumentDAO;
import dao.ExamRegistrationDAO;
import dao.LicenceDAO;
import dao.ProfileDAO;
import dao.impl.DocumentDAOImpl;
import dao.impl.ExamRegistrationDAOImpl;
import dao.impl.LicenceDAOImpl;
import dao.impl.ProfileDAOImpl;
import dto.ServiceResult;
import dto.payload.CreateManagedUserCommand;
import dto.payload.CreateUserData;
import dto.payload.ManagedDossierCommand;
import dto.payload.RegisterData;
import enums.ErrorType;
import enums.RegistrationStatus;
import enums.Sex;
import model.Document;
import model.ExamRegistration;
import model.Licence;
import model.Profile;
import service.AuthService;
import service.EmailService;
import service.UserManagementService;
import util.CredentialsUtil;
import java.time.LocalDate;
import java.time.Period;
import java.util.Map;
import java.util.Set;

public class UserManagementServiceImpl implements UserManagementService {

    private static final Set<String> REQUIRED_DOCUMENTS = Set.of(
            "PORTRAIT", "ID_FRONT", "ID_BACK", "HEALTH_CERTIFICATE");
    private final AuthService authService = new AuthServiceImpl();
    private final EmailService emailService = new EmailServiceImpl();
    private final ProfileDAO profileDAO = new ProfileDAOImpl();
    private final ExamRegistrationDAO examRegistrationDAO = new ExamRegistrationDAOImpl();
    private final DocumentDAO documentDAO = new DocumentDAOImpl();
    private final LicenceDAO licenceDAO = new LicenceDAOImpl();

    @Override
    public ServiceResult<CreateUserData> createUser(CreateManagedUserCommand command) {
        String normalizedClass = CredentialsUtil.normalizeLicenceClass(command.getLicenceClass());
        // Business rule: minimum age depends on licence class
        String ageError = validateMinimumAge(command.getDob(), normalizedClass);
        if (ageError != null) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, ageError);
        }
        if (!emailService.isConfigured()) {
            return ServiceResult.fail(ErrorType.NOT_CONFIGURED,
                    "Chưa cấu hình email gửi đi. Vui lòng cấu hình MAIL_SENDER_USERNAME "
                    + "và MAIL_SENDER_PASSWORD trong file .env, sau đó khởi động lại Tomcat.");
        }
        Profile profile = buildProfileFromCommand(command);
        ServiceResult<RegisterData> registerResult = authService.register(profile, command.getEmail());
        if (!registerResult.isSuccess()) {
            return ServiceResult.fail(registerResult.getErrorType(), registerResult.getMessage());
        }
        RegisterData registerData = registerResult.getData();
        Profile savedProfile = profileDAO.getByGovIdNo(command.getCccd());
        Integer profileId = savedProfile == null ? null : savedProfile.getProfileId();
        Integer userId = savedProfile == null ? null : savedProfile.getUserId();
        CreateUserData data = new CreateUserData(profileId, userId, null, null);
        if (registerData.isEmailSent()) {
            String message = "Tạo tài khoản thành công. Thông tin đăng nhập đã được gửi đến "
                    + command.getEmail() + ".";
            return ServiceResult.ok(data, message);
        }
        data.setUsername(registerData.getUsername());
        data.setPassword(registerData.getPassword());
        String message = "Tạo tài khoản thành công nhưng chưa gửi được email. "
                + "Hãy bàn giao thông tin đăng nhập bên dưới cho học viên.";
        return ServiceResult.ok(data, message);
    }

    @Override
    public ServiceResult<Void> saveManagedDossier(ManagedDossierCommand command) {
        String normalizedClass = CredentialsUtil.normalizeLicenceClass(command.getLicenceClass());
        String dossierError = validateDossierDocuments(normalizedClass, command.getDocuments());
        if (dossierError != null) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, dossierError);
        }
        Licence licence = licenceDAO.getByLicenceClass(normalizedClass);
        if (licence == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND,
                    "Chưa thể tạo hồ sơ GPLX tự động; vui lòng kiểm tra lại cấu hình hạng GPLX.");
        }
        int registrationId = examRegistrationDAO.getLatestIdByProfileAndLicence(
                command.getProfileId(), licence.getLicenceId());
        if (registrationId <= 0) {
            ExamRegistration registration = new ExamRegistration();
            registration.setRegistrationStatus(RegistrationStatus.PENDING.getValue());
            registration.setNotes("SOURCE=STAFF;APPLICANT_TYPE=" + command.getApplicantType());
            registration.setProfileId(command.getProfileId());
            registration.setLicenceId(licence.getLicenceId());
            registrationId = examRegistrationDAO.add(registration);
        }
        if (registrationId <= 0) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED,
                    "Chưa thể tạo hồ sơ GPLX tự động; vui lòng kiểm tra lại cấu hình hạng GPLX.");
        }
        String uploadedNote = "Đã tải lên";
        for (Map.Entry<String, String> entry : command.getDocuments().entrySet()) {
            if (entry.getValue() == null || entry.getValue().isBlank()) {
                continue;
            }
            Document document = new Document();
            document.setProfileId(command.getProfileId());
            document.setDocumentType(entry.getKey());
            document.setDocumentUrl(entry.getValue());
            document.setNotes(uploadedNote);
            if (!documentDAO.upsertByProfileAndType(document)) {
                return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED,
                        "Không thể ghi thông tin tài liệu vào cơ sở dữ liệu.");
            }
        }
        if (!examRegistrationDAO.updateStatusWithReviewNote(registrationId,
                RegistrationStatus.APPROVED.getValue(),
                "Hồ sơ bản giấy và tệp đính kèm đã được Managing Staff đối chiếu khi tạo tài khoản",
                command.getActorUserId())) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED,
                    "Không thể cập nhật trạng thái hồ sơ.");
        }
        return ServiceResult.ok(null, "Hồ sơ đã được lưu và xác minh.");
    }

    public static boolean requiresGraduationCertificate(String licenseClass) {
        String normalized = CredentialsUtil.normalizeLicenceClass(licenseClass);
        return !Set.of("A1", "A2").contains(normalized);
    }

    private Profile buildProfileFromCommand(CreateManagedUserCommand command) {
        Profile profile = new Profile();
        profile.setGovernmentIdNumber(command.getCccd());
        profile.setFullName(command.getFullName());
        profile.setPhoneNumber(command.getPhone());
        profile.setAddress(command.getAddress());
        Sex sex = Sex.fromValue(command.getSex());
        profile.setSex(sex != null && sex.toDbBit());
        LocalDate dob = CredentialsUtil.parseIsoDate(command.getDob()).orElse(null);
        if (dob != null) {
            profile.setDateOfBirth(java.sql.Timestamp.valueOf(dob.atStartOfDay()));
        }
        return profile;
    }

    private String validateMinimumAge(String dob, String licenseClass) {
        LocalDate dateOfBirth = CredentialsUtil.parseIsoDate(dob).orElse(null);
        if (dateOfBirth == null) {
            return "Ngày sinh không hợp lệ.";
        }
        int minimumAge = minimumAgeFor(licenseClass);
        int age = Period.between(dateOfBirth, LocalDate.now()).getYears();
        if (age < minimumAge) {
            return "Học viên phải đủ " + minimumAge + " tuổi để đăng ký hạng " + licenseClass + ".";
        }
        return null;
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

    private static int minimumAgeFor(String licenseClass) {
        String normalized = CredentialsUtil.normalizeLicenceClass(licenseClass);
        if ("C1".equals(normalized) || "C".equals(normalized)) {
            return 21;
        }
        if ("D1".equals(normalized) || "D2".equals(normalized) || "D".equals(normalized)) {
            return 24;
        }
        return 18;
    }
}
