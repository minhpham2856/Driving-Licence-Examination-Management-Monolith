package controller.staff.managing;

import dto.ServiceResult;
import dto.payload.CreateManagedUserCommand;
import dto.payload.CreateUserData;
import dto.payload.ManagedDossierCommand;
import enums.UserRole;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import model.User;
import service.RoleService;
import service.UserManagementService;
import service.impl.RoleServiceImpl;
import service.impl.UserManagementServiceImpl;
import util.CredentialsUtil;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@WebServlet("/manager/create-user")
@MultipartConfig(maxFileSize = 5 * 1024 * 1024, maxRequestSize = 22 * 1024 * 1024)
public class CreateUserServlet extends HttpServlet {

    private static final String VIEW = "/views/staff/managing/create-user.jsp";
    private static final Set<String> LICENCE_CLASSES = Set.of(
            "A1", "A2", "B1", "B2", "C1", "C", "D1", "D2", "D");
    private static final Map<String, String> DOSSIER_PARTS = Map.of(
            "portrait", "PORTRAIT",
            "idFront", "ID_FRONT",
            "idBack", "ID_BACK",
            "healthCertificate", "HEALTH_CERTIFICATE");
    private static final String GRADUATION_PART = "graduationCertificate";
    private static final String GRADUATION_DOCUMENT_TYPE = "GRADUATION_CERTIFICATE";
    private final UserManagementService userManagementService = new UserManagementServiceImpl();
    private final RoleService roleService = new RoleServiceImpl();

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
        String sex = trim(request.getParameter("sex"));
        if (sex.isEmpty()) {
            sex = trim(request.getParameter("gender"));
        }
        String address = trim(request.getParameter("address"));
        String userType = trim(request.getParameter("userType"));
        String licenseClass = CredentialsUtil.normalizeLicenceClass(request.getParameter("licenseClass"));

        Map<String, String> errors = validateInput(
                fullName, cccd, phone, email, dob, sex, address, userType, licenseClass);
        if (!errors.isEmpty()) {
            forwardFieldErrors(request, response, errors);
            return;
        }
        try {
            validateDossierParts(request, licenseClass);
        } catch (IllegalArgumentException ex) {
            forwardError(request, response, ex.getMessage());
            return;
        }
        CreateManagedUserCommand command = new CreateManagedUserCommand(
                fullName, cccd, phone, email, dob, sex, address, userType, licenseClass);
        ServiceResult<CreateUserData> result = userManagementService.createUser(command);
        if (!result.isSuccess()) {
            forwardError(request, response, result.getMessage());
            return;
        }
        CreateUserData data = result.getData();
        if (data.getProfileId() == null || data.getUserId() == null) {
            forwardError(request, response,
                    "Tài khoản đã được tạo nhưng không tìm thấy hồ sơ người dùng để lưu giấy tờ.");
            return;
        }
        Map<String, String> documents;
        try {
            documents = saveDossierParts(request, data.getProfileId(), data.getUserId(), licenseClass);
        } catch (IllegalArgumentException | IOException | ServletException ex) {
            forwardError(request, response,
                    "Tài khoản đã được tạo nhưng không thể lưu tệp hồ sơ: " + ex.getMessage());
            return;
        }
        HttpSession session = request.getSession();
        User actor = (User) session.getAttribute("user");
        ManagedDossierCommand dossierCommand = new ManagedDossierCommand(
                data.getProfileId(), licenseClass, userType, documents, actor.getUserId());
        ServiceResult<Void> dossierResult = userManagementService.saveManagedDossier(dossierCommand);
        if (!dossierResult.isSuccess()) {
            forwardError(request, response,
                    "Tài khoản đã được tạo nhưng " + dossierResult.getMessage());
            return;
        }
        session.setAttribute("createUserSuccess", result.getMessage());
        if (data.getUsername() != null) {
            session.setAttribute("createdUsername", data.getUsername());
            session.setAttribute("createdPassword", data.getPassword());
        }
        response.sendRedirect(request.getContextPath() + "/manager/create-user");
    }

    private Map<String, String> validateInput(String fullName, String cccd, String phone, String email,
            String dob, String sex, String address, String userType, String licenseClass) {
        Map<String, String> errors = new LinkedHashMap<>();
        if (CredentialsUtil.isBlank(fullName)) {
            errors.put("fullName", "Vui lòng nhập họ và tên.");
        } else if (!CredentialsUtil.isLengthInRange(fullName, 3, 50)) {
            errors.put("fullName", "Họ và tên phải có từ 3 đến 50 ký tự.");
        }
        if (CredentialsUtil.isBlank(cccd)) {
            errors.put("cccd", "Vui lòng nhập số CCCD.");
        } else if (!CredentialsUtil.isValidCccd(cccd)) {
            errors.put("cccd", "Số CCCD phải gồm đúng 12 chữ số.");
        }
        if (CredentialsUtil.isBlank(phone)) {
            errors.put("phone", "Vui lòng nhập số điện thoại.");
        } else if (!CredentialsUtil.isValidPhone(phone)) {
            errors.put("phone", "Số điện thoại phải bắt đầu bằng 0 và gồm đúng 10 chữ số.");
        }
        if (CredentialsUtil.isBlank(email)) {
            errors.put("email", "Vui lòng nhập email.");
        } else if (!CredentialsUtil.isValidEmail(email)) {
            errors.put("email", "Địa chỉ email không hợp lệ.");
        }
        if (CredentialsUtil.isBlank(dob)) {
            errors.put("dob", "Vui lòng nhập ngày sinh.");
        } else {
            LocalDate dateOfBirth = CredentialsUtil.parseIsoDate(dob).orElse(null);
            if (dateOfBirth == null) {
                errors.put("dob", "Ngày sinh không hợp lệ.");
            } else if (dateOfBirth.isAfter(LocalDate.now())) {
                errors.put("dob", "Ngày sinh không được nằm trong tương lai.");
            }
        }
        if (CredentialsUtil.isBlank(sex)) {
            errors.put("sex", "Vui lòng chọn giới tính.");
        } else if (!CredentialsUtil.isValidSex(sex)) {
            errors.put("sex", "Giới tính không hợp lệ.");
        }
        if (CredentialsUtil.isBlank(address)) {
            errors.put("address", "Vui lòng nhập địa chỉ.");
        } else if (!CredentialsUtil.isLengthInRange(address, 5, 150)) {
            errors.put("address", "Địa chỉ phải có từ 5 đến 150 ký tự.");
        }
        if (CredentialsUtil.isBlank(userType)) {
            errors.put("userType", "Vui lòng chọn phân loại học viên.");
        } else if (!CredentialsUtil.isValidManagedUserType(userType)) {
            errors.put("userType", "Phân loại học viên không hợp lệ.");
        }
        if (CredentialsUtil.isBlank(licenseClass)) {
            errors.put("licenseClass", "Vui lòng chọn hạng GPLX.");
        } else if (!LICENCE_CLASSES.contains(licenseClass)) {
            errors.put("licenseClass", "Hạng GPLX không hợp lệ.");
        }
        return errors;
    }

    private void validateDossierParts(HttpServletRequest request, String licenseClass)
            throws IOException, ServletException {
        for (String partName : DOSSIER_PARTS.keySet()) {
            Part part = request.getPart(partName);
            if (part == null || part.getSize() == 0) {
                throw new IllegalArgumentException(
                        "Vui lòng tải đủ ảnh chân dung, hai mặt CCCD và giấy khám sức khỏe.");
            }
            validateUploadFile(part);
        }
        Part graduation = request.getPart(GRADUATION_PART);
        if (UserManagementServiceImpl.requiresGraduationCertificate(licenseClass)) {
            if (graduation == null || graduation.getSize() == 0) {
                throw new IllegalArgumentException(
                        "Hồ sơ hạng ô tô phải có giấy tốt nghiệp/chứng chỉ đào tạo từ trung tâm.");
            }
            validateUploadFile(graduation);
        } else if (graduation != null && graduation.getSize() > 0) {
            validateUploadFile(graduation);
        }
    }

    private Map<String, String> saveDossierParts(
            HttpServletRequest request, int profileId, int userId, String licenseClass)
            throws IOException, ServletException {
        String root = getServletContext().getRealPath("/uploads/dossiers/" + userId);
        if (root == null) {
            throw new IllegalArgumentException("Không xác định được thư mục lưu hồ sơ.");
        }
        Path directory = Paths.get(root).toAbsolutePath().normalize();
        Files.createDirectories(directory);
        Map<String, String> documents = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : DOSSIER_PARTS.entrySet()) {
            Part part = request.getPart(entry.getKey());
            String fileName = storePart(part, directory, entry.getValue());
            String url = "/uploads/dossiers/" + userId + "/" + fileName;
            documents.put(entry.getValue(), url);
        }
        Part graduation = request.getPart(GRADUATION_PART);
        if (graduation != null && graduation.getSize() > 0) {
            String fileName = storePart(graduation, directory, GRADUATION_DOCUMENT_TYPE);
            documents.put(GRADUATION_DOCUMENT_TYPE,
                    "/uploads/dossiers/" + userId + "/" + fileName);
        } else if (UserManagementServiceImpl.requiresGraduationCertificate(licenseClass)) {
            throw new IllegalArgumentException(
                    "Hồ sơ hạng ô tô phải có giấy tốt nghiệp/chứng chỉ đào tạo từ trung tâm.");
        }
        return documents;
    }

    private String storePart(Part part, Path directory, String documentType) throws IOException {
        String extension = extension(part.getSubmittedFileName());
        String fileName = documentType.toLowerCase() + "-" + UUID.randomUUID() + extension;
        Path target = directory.resolve(fileName).normalize();
        if (!target.startsWith(directory)) {
            throw new IllegalArgumentException("Tên tệp không hợp lệ.");
        }
        part.write(target.toString());
        persistAcrossCleanBuild(target, Integer.parseInt(directory.getFileName().toString()), fileName);
        return fileName;
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

    private static void validateUploadFile(Part part) {
        String contentType = part.getContentType() == null ? "" : part.getContentType();
        if (!(contentType.startsWith("image/") || "application/pdf".equals(contentType))) {
            throw new IllegalArgumentException("Hồ sơ chỉ chấp nhận tệp ảnh hoặc PDF.");
        }
        if (extension(part.getSubmittedFileName()).isEmpty()) {
            throw new IllegalArgumentException("Định dạng tệp phải là JPG, JPEG, PNG hoặc PDF.");
        }
    }

    private static String extension(String name) {
        if (name == null) {
            return "";
        }
        int dot = name.lastIndexOf('.');
        if (dot < 0) {
            return "";
        }
        String ext = name.substring(dot).toLowerCase();
        return Set.of(".jpg", ".jpeg", ".png", ".pdf").contains(ext) ? ext : "";
    }

    private void forwardError(HttpServletRequest request, HttpServletResponse response, String message)
            throws ServletException, IOException {
        request.setAttribute("createUserError", message);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    private void forwardFieldErrors(HttpServletRequest request, HttpServletResponse response,
            Map<String, String> errors) throws ServletException, IOException {
        request.setAttribute("errors", errors);
        if (!errors.isEmpty()) {
            request.setAttribute("createUserError", errors.values().iterator().next());
        }
        request.getRequestDispatcher(VIEW).forward(request, response);
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
        String roleName = roleService.getRoleNameById(user.getRoleId());
        UserRole role = UserRole.fromValue(roleName);
        if (role != UserRole.MANAGING_STAFF && role != UserRole.ADMIN) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
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
