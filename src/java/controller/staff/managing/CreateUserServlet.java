package controller.staff.managing;
import dto.CreateUserResultDTO;
import model.User;
import service.RoleService;
import service.UserManagementService;
import service.impl.RoleServiceImpl;
import service.impl.UserManagementServiceImpl;
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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import enums.UserRole;
@WebServlet("/manager/create-user")
@MultipartConfig(maxFileSize = 5 * 1024 * 1024, maxRequestSize = 22 * 1024 * 1024)
public class CreateUserServlet extends HttpServlet {
    private static final String VIEW = "/views/staff/managing/create-user.jsp";
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
        String licenseClass = normalizeLicenceClass(request.getParameter("licenseClass"));
        try {
            validateDossierParts(request, licenseClass);
        } catch (IllegalArgumentException ex) {
            forwardError(request, response, ex.getMessage());
            return;
        }
        CreateUserResultDTO result = userManagementService.createUser(
                fullName, cccd, phone, email, dob, sex, address, userType, licenseClass);
        if (!result.isSuccess()) {
            forwardError(request, response, result.getMessage());
            return;
        }
        if (result.getProfileId() == null || result.getUserId() == null) {
            forwardError(request, response,
                    "Tài khoản đã được tạo nhưng không tìm thấy hồ sơ người dùng để lưu giấy tờ.");
            return;
        }
        Map<String, String> documents;
        try {
            documents = saveDossierParts(request, result.getProfileId(), result.getUserId(), licenseClass);
        } catch (IllegalArgumentException | IOException | ServletException ex) {
            forwardError(request, response,
                    "Tài khoản đã được tạo nhưng không thể lưu tệp hồ sơ: " + ex.getMessage());
            return;
        }
        HttpSession session = request.getSession();
        User actor = (User) session.getAttribute("user");
        CreateUserResultDTO dossierResult = userManagementService.saveManagedDossier(
                result.getProfileId(),
                licenseClass,
                userType,
                documents,
                actor.getUserId());
        if (!dossierResult.isSuccess()) {
            forwardError(request, response,
                    "Tài khoản đã được tạo nhưng " + dossierResult.getMessage());
            return;
        }
        session.setAttribute("createUserSuccess", result.getMessage());
        if (result.getUsername() != null) {
            session.setAttribute("createdUsername", result.getUsername());
            session.setAttribute("createdPassword", result.getPassword());
        }
        response.sendRedirect(request.getContextPath() + "/manager/create-user");
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
        if (requiresGraduationCertificate(licenseClass)) {
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
        } else if (requiresGraduationCertificate(licenseClass)) {
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
        if (!UserRole.isManagingStaff(roleName) && !UserRole.isAdmin(roleName)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        return true;
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
