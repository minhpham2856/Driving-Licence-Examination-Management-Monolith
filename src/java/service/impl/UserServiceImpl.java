package service.impl;

import dao.DocumentDAO;
import dao.ExamRegistrationDAO;
import dao.LicenceDAO;
import dao.ProfileDAO;
import dao.UserDAO;
import dao.impl.DocumentDAOImpl;
import dao.impl.ExamRegistrationDAOImpl;
import dao.impl.LicenceDAOImpl;
import dao.impl.ProfileDAOImpl;
import dao.impl.UserDAOImpl;
import dto.CreateUserResultDTO;
import dto.RegisterResultDTO;
import dto.ServiceResult;
import enums.ErrorType;
import enums.RegistrationStatus;
import enums.Sex;
import enums.UserRole;
import model.Document;
import model.ExamRegistration;
import model.Licence;
import model.Profile;
import model.User;
import service.AuthService;
import service.EmailService;
import service.RoleService;
import service.UserService;
import util.CredentialsUtil;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UserServiceImpl implements UserService {

    private static final Set<String> REQUIRED_DOCUMENTS = Set.of(
            "PORTRAIT", "ID_FRONT", "ID_BACK", "HEALTH_CERTIFICATE");

    private final UserDAO userDAO = new UserDAOImpl();
    private final ProfileDAO profileDAO = new ProfileDAOImpl();
    private final RoleService roleService = new RoleServiceImpl();
    private final AuthService authService = new AuthServiceImpl();
    private final EmailService emailService = new EmailServiceImpl();
    private final ExamRegistrationDAO examRegistrationDAO = new ExamRegistrationDAOImpl();
    private final DocumentDAO documentDAO = new DocumentDAOImpl();
    private final LicenceDAO licenceDAO = new LicenceDAOImpl();

    @Override
    public List<Map<String, Object>> searchAccounts(String keyword, String roleFilter, String statusFilter) {
        List<User> users = userDAO.searchForAdmin(keyword, roleFilter, statusFilter);
        List<Integer> userIds = new ArrayList<>();
        for (User user : users) {
            userIds.add(user.getUserId());
        }
        Map<Integer, Profile> profiles = new HashMap<>();
        for (Profile profile : profileDAO.getAllByUserIds(userIds)) {
            profiles.put(profile.getUserId(), profile);
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        for (User user : users) {
            Profile profile = profiles.get(user.getUserId());
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", String.valueOf(user.getUserId()));
            row.put("username", user.getUsername());
            row.put("fullName", profile != null && profile.getFullName() != null && !profile.getFullName().isBlank()
                    ? profile.getFullName() : user.getUsername());
            row.put("email", user.getEmail() != null ? user.getEmail() : "");
            row.put("phone", profile != null && profile.getPhoneNumber() != null ? profile.getPhoneNumber() : "");
            String roleKey = mapRoleKey(user.getRoleId());
            row.put("role", roleKey);
            row.put("department", mapDepartment(roleKey));
            row.put("createdAt", user.getUserId() > 0 ? new java.util.Date() : null);
            row.put("status", user.isActive() ? "active" : "locked");
            rows.add(row);
        }
        return rows;
    }

    @Override
    public int countByRoleKey(String roleKey) {
        int count = 0;
        for (Map<String, Object> row : searchAccounts(null, roleKey, null)) {
            count++;
        }
        return count;
    }

    private String mapRoleKey(int roleId) {
        String roleName = roleService.getRoleNameById(roleId);
        UserRole role = UserRole.fromValue(roleName);
        if (role == UserRole.ADMIN) {
            return "admin";
        }
        if (role == UserRole.EXAM_STAFF) {
            return "coi_thi";
        }
        if (role == UserRole.EXAMINER) {
            return "cham_thi";
        }
        if (role == UserRole.CANDIDATE || role == UserRole.REGISTRANT) {
            return "candidate";
        }
        return "candidate";
    }

    private static String mapDepartment(String roleKey) {
        if ("admin".equals(roleKey)) {
            return "Phòng Quản lý Sát hạch";
        }
        if ("coi_thi".equals(roleKey)) {
            return "Ban Sát Hạch";
        }
        if ("cham_thi".equals(roleKey)) {
            return "Hội đồng Sát hạch";
        }
        return "Thí sinh";
    }

    @Override
    public ServiceResult<CreateUserResultDTO> createUser(String fullName, String cccd, String phone, String email,
            String dob, String sex, String address, String userType, String licenceClass) {
        String normalizedClass = CredentialsUtil.normalizeLicenceClass(licenceClass);
        String ageError = validateMinimumAge(dob, normalizedClass);
        if (ageError != null) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, ageError);
        }
        if (!emailService.isConfigured()) {
            return ServiceResult.fail(ErrorType.NOT_CONFIGURED,
                    "Chưa cấu hình email gửi đi. Vui lòng cấu hình MAIL_SENDER_USERNAME "
                    + "và MAIL_SENDER_PASSWORD trong file .env, sau đó khởi động lại Tomcat.");
        }
        Profile profile = buildProfile(fullName, cccd, phone, address, sex, dob);
        ServiceResult<RegisterResultDTO> registerResult = authService.register(profile, email);
        if (!registerResult.isSuccess()) {
            return ServiceResult.fail(registerResult.getErrorType(), registerResult.getMessage());
        }
        RegisterResultDTO registerData = registerResult.getData();
        Profile savedProfile = profileDAO.getByGovIdNo(cccd);
        Integer profileId = savedProfile == null ? null : savedProfile.getProfileId();
        Integer userId = savedProfile == null ? null : savedProfile.getUserId();
        CreateUserResultDTO data = new CreateUserResultDTO();
        data.setProfileId(profileId);
        data.setUserId(userId);
        if (registerData.isEmailSent()) {
            String message = "Tạo tài khoản thành công. Thông tin đăng nhập đã được gửi đến "
                    + email + ".";
            return ServiceResult.ok(data, message);
        }
        data.setUsername(registerData.getUsername());
        data.setPassword(registerData.getPassword());
        String message = "Tạo tài khoản thành công nhưng chưa gửi được email. "
                + "Hãy bàn giao thông tin đăng nhập bên dưới cho học viên.";
        return ServiceResult.ok(data, message);
    }

    @Override
    public ServiceResult<Void> saveManagedDossier(int profileId, String licenceClass, String applicantType,
            Map<String, String> documents, int actorUserId) {
        String normalizedClass = CredentialsUtil.normalizeLicenceClass(licenceClass);
        String dossierError = validateDossierDocuments(normalizedClass, documents);
        if (dossierError != null) {
            return ServiceResult.fail(ErrorType.VALIDATION_FAILED, dossierError);
        }
        Licence licence = licenceDAO.getByLicenceClass(normalizedClass);
        if (licence == null) {
            return ServiceResult.fail(ErrorType.NOT_FOUND,
                    "Chưa thể tạo hồ sơ GPLX tự động; vui lòng kiểm tra lại cấu hình hạng GPLX.");
        }
        int registrationId = examRegistrationDAO.getLatestIdByProfileAndLicence(
                profileId, licence.getLicenceId());
        if (registrationId <= 0) {
            ExamRegistration registration = new ExamRegistration();
            registration.setRegistrationStatus(RegistrationStatus.PENDING.getValue());
            registration.setNotes("SOURCE=STAFF;APPLICANT_TYPE=" + applicantType);
            registration.setProfileId(profileId);
            registration.setLicenceId(licence.getLicenceId());
            registrationId = examRegistrationDAO.add(registration);
        }
        if (registrationId <= 0) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED,
                    "Chưa thể tạo hồ sơ GPLX tự động; vui lòng kiểm tra lại cấu hình hạng GPLX.");
        }
        String uploadedNote = "Đã tải lên";
        for (Map.Entry<String, String> entry : documents.entrySet()) {
            if (entry.getValue() == null || entry.getValue().isBlank()) {
                continue;
            }
            Document document = new Document();
            document.setProfileId(profileId);
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
                actorUserId)) {
            return ServiceResult.fail(ErrorType.PERSISTENCE_FAILED,
                    "Không thể cập nhật trạng thái hồ sơ.");
        }
        return ServiceResult.ok(null, "Hồ sơ đã được lưu và xác minh.");
    }

    public static boolean requiresGraduationCertificate(String licenseClass) {
        String normalized = CredentialsUtil.normalizeLicenceClass(licenseClass);
        return !Set.of("A1", "A2").contains(normalized);
    }

    private Profile buildProfile(String fullName, String cccd, String phone, String address, String sex,
            String dob) {
        Profile profile = new Profile();
        profile.setGovernmentIdNumber(cccd);
        profile.setFullName(fullName);
        profile.setPhoneNumber(phone);
        profile.setAddress(address);
        Sex sexEnum = Sex.fromValue(sex);
        profile.setSex(sexEnum != null && sexEnum.toDbBit());
        LocalDate dateOfBirth = CredentialsUtil.parseIsoDate(dob).orElse(null);
        if (dateOfBirth != null) {
            profile.setDateOfBirth(java.sql.Timestamp.valueOf(dateOfBirth.atStartOfDay()));
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
