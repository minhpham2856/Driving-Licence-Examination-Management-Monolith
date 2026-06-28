package Controllers.Staff.ManagingStaff;

import DAOs.Impl.ProfileDAOImpl;
import DAOs.DossierDAO;
import DAOs.Impl.DossierDAOImpl;
import DTOs.RegisterResultDTO;
import Models.Profile;
import Models.User;
import Services.AuthService;
import Services.EmailService;
import Services.Impl.AuthServiceImpl;
import Services.Impl.EmailServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Period;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

@WebServlet("/manager/create-user")
@MultipartConfig(maxFileSize = 5 * 1024 * 1024, maxRequestSize = 22 * 1024 * 1024)
public class CreateUserServlet extends HttpServlet {

    private static final String VIEW = "/views/staff/managingstaff/create-user.jsp";
    private static final Pattern CCCD_PATTERN = Pattern.compile("\\d{12}");
    private static final Pattern PHONE_PATTERN = Pattern.compile("0\\d{9}");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Set<String> LICENSE_CLASSES = Set.of("A1", "A2", "B1", "B2", "C1", "C", "D1", "D2", "D");
    private static final Map<String, String> DOSSIER_PARTS = Map.of(
            "portrait", "PORTRAIT",
            "idFront", "ID_FRONT",
            "idBack", "ID_BACK",
            "healthCertificate", "HEALTH_CERTIFICATE");
    private static final String GRADUATION_PART = "graduationCertificate";
    private static final String GRADUATION_DOCUMENT_TYPE = "GRADUATION_CERTIFICATE";

    private final AuthService authService = new AuthServiceImpl();
    private final EmailService emailService = new EmailServiceImpl();
    private final ProfileDAOImpl profileDAO = new ProfileDAOImpl();
    private final DossierDAO dossierDAO = new DossierDAOImpl();

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
        String licenseClass = normalizeLicenceClass(request.getParameter("licenseClass"));

        String validationError = validate(
                fullName, cccd, phone, email, dob, gender, address, licenseClass);
        if (validationError != null) {
            request.setAttribute("createUserError", validationError);
            request.getRequestDispatcher(VIEW).forward(request, response);
            return;
        }

        try {
            validateDossierParts(request, licenseClass);
        } catch (IllegalArgumentException ex) {
            request.setAttribute("createUserError", ex.getMessage());
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
        int registrationId = profile == null ? 0
                : dossierDAO.ensureRegistration(profile.getId(), licenseClass, "STAFF", "managed");
        boolean applicationCreated = registrationId > 0;
        if (applicationCreated) {
            try {
                saveDossierParts(request, profile.getId(), profile.getUserId(), licenseClass);
                applicationCreated = dossierDAO.updateStatus(registrationId, "Approved",
                        "Hồ sơ bản giấy và tệp đính kèm đã được Managing Staff đối chiếu khi tạo tài khoản",
                        ((User) session.getAttribute("user")).getId());
            } catch (IllegalArgumentException | IOException | ServletException ex) {
                request.setAttribute("createUserError",
                        "Tài khoản đã được tạo nhưng không thể lưu tệp hồ sơ: " + ex.getMessage());
                request.getRequestDispatcher(VIEW).forward(request, response);
                return;
            }
        }

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
            String dob, String gender, String address, String licenseClass) {
        if (fullName.isEmpty() || cccd.isEmpty() || phone.isEmpty() || email.isEmpty()
                || dob.isEmpty() || gender.isEmpty() || address.isEmpty()
                || licenseClass.isEmpty()) {
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

    private void validateDossierParts(HttpServletRequest request, String licenseClass)
            throws IOException, ServletException {
        for (String partName : DOSSIER_PARTS.keySet()) {
            Part part = request.getPart(partName);
            if (part == null || part.getSize() == 0) {
                throw new IllegalArgumentException(
                        "Vui lòng tải đủ ảnh chân dung, hai mặt CCCD và giấy khám sức khỏe.");
            }
            String contentType = part.getContentType() == null ? "" : part.getContentType();
            if (!(contentType.startsWith("image/") || "application/pdf".equals(contentType))) {
                throw new IllegalArgumentException("Hồ sơ chỉ chấp nhận tệp ảnh hoặc PDF.");
            }
            if (extension(part.getSubmittedFileName()).isEmpty()) {
                throw new IllegalArgumentException("Định dạng tệp phải là JPG, JPEG, PNG hoặc PDF.");
            }
        }
        Part graduation = request.getPart(GRADUATION_PART);
        if (requiresGraduationCertificate(licenseClass)) {
            if (graduation == null || graduation.getSize() == 0) {
                throw new IllegalArgumentException("Hồ sơ hạng ô tô phải có giấy tốt nghiệp/chứng chỉ đào tạo từ trung tâm.");
            }
            validateOptionalFile(graduation);
        } else if (graduation != null && graduation.getSize() > 0) {
            validateOptionalFile(graduation);
        }
    }

    private void saveDossierParts(HttpServletRequest request, int profileId, int userId, String licenseClass)
            throws IOException, ServletException {
        String root = getServletContext().getRealPath("/uploads/dossiers/" + userId);
        if (root == null) {
            throw new IllegalArgumentException("Không xác định được thư mục lưu hồ sơ.");
        }
        Path directory = Paths.get(root).toAbsolutePath().normalize();
        Files.createDirectories(directory);

        for (Map.Entry<String, String> entry : DOSSIER_PARTS.entrySet()) {
            Part part = request.getPart(entry.getKey());
            String extension = extension(part.getSubmittedFileName());
            String fileName = entry.getValue().toLowerCase() + "-" + UUID.randomUUID() + extension;
            Path target = directory.resolve(fileName).normalize();
            if (!target.startsWith(directory)) {
                throw new IllegalArgumentException("Tên tệp không hợp lệ.");
            }
            part.write(target.toString());
            persistAcrossCleanBuild(target, userId, fileName);
            if (!dossierDAO.saveDocument(profileId, entry.getValue(),
                    "/uploads/dossiers/" + userId + "/" + fileName)) {
                throw new IllegalArgumentException("Không thể ghi thông tin tài liệu vào cơ sở dữ liệu.");
            }
        }
        saveGraduationCertificateIfPresent(request, directory, profileId, userId);
    }

    private void saveGraduationCertificateIfPresent(HttpServletRequest request, Path directory, int profileId, int userId)
            throws IOException, ServletException {
        Part part = request.getPart(GRADUATION_PART);
        if (part == null || part.getSize() == 0) {
            return;
        }
        String extension = extension(part.getSubmittedFileName());
        String fileName = GRADUATION_DOCUMENT_TYPE.toLowerCase() + "-" + UUID.randomUUID() + extension;
        Path target = directory.resolve(fileName).normalize();
        if (!target.startsWith(directory)) {
            throw new IllegalArgumentException("TÃªn tá»‡p khÃ´ng há»£p lá»‡.");
        }
        part.write(target.toString());
        persistAcrossCleanBuild(target, userId, fileName);
        if (!dossierDAO.saveDocument(profileId, GRADUATION_DOCUMENT_TYPE,
                "/uploads/dossiers/" + userId + "/" + fileName)) {
            throw new IllegalArgumentException("KhÃ´ng thá»ƒ ghi thÃ´ng tin tÃ i liá»‡u vÃ o cÆ¡ sá»Ÿ dá»¯ liá»‡u.");
        }
    }

    private void persistAcrossCleanBuild(Path runtimeFile, int userId, String fileName)
            throws IOException {
        Path runtimeWebRoot = Paths.get(getServletContext().getRealPath("/")).toAbsolutePath().normalize();
        Path buildDir = runtimeWebRoot.getParent();
        if (buildDir == null || !"build".equalsIgnoreCase(buildDir.getFileName().toString())) {
            return;
        }
        Path projectRoot = buildDir.getParent();
        if (projectRoot == null) {
            return;
        }
        Path webRoot = projectRoot.resolve("web").normalize();
        Path sourceDirectory = webRoot.resolve("uploads/dossiers/" + userId).normalize();
        if (!sourceDirectory.startsWith(webRoot)) {
            return;
        }
        Files.createDirectories(sourceDirectory);
        Files.copy(runtimeFile, sourceDirectory.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
    }

    private static String extension(String name) {
        if (name == null) {
            return "";
        }
        int dot = name.lastIndexOf('.');
        if (dot < 0) {
            return "";
        }
        String extension = name.substring(dot).toLowerCase();
        return Set.of(".jpg", ".jpeg", ".png", ".pdf").contains(extension) ? extension : "";
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

    private static String normalizeLicenceClass(String value) {
        String licenseClass = trim(value).toUpperCase();
        return switch (licenseClass) {
            case "A" -> "A2";
            case "B" -> "B2";
            default -> licenseClass;
        };
    }

    private static boolean requiresGraduationCertificate(String licenseClass) {
        return !Set.of("A1", "A2").contains(normalizeLicenceClass(licenseClass));
    }

    private static int minimumAgeFor(String licenseClass) {
        return switch (normalizeLicenceClass(licenseClass)) {
            case "C1", "C" -> 21;
            case "D1", "D2", "D" -> 24;
            default -> 18;
        };
    }

    private static void validateOptionalFile(Part part) {
        String contentType = part.getContentType() == null ? "" : part.getContentType();
        if (!(contentType.startsWith("image/") || "application/pdf".equals(contentType))) {
            throw new IllegalArgumentException("Hồ sơ chỉ chấp nhận tệp ảnh hoặc PDF.");
        }
        if (extension(part.getSubmittedFileName()).isEmpty()) {
            throw new IllegalArgumentException("Định dạng tệp phải là JPG, JPEG, PNG hoặc PDF.");
        }
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
