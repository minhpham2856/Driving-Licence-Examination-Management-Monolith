package controller.registrant;

import dao.DossierDAO;
import dao.impl.DossierDAOImpl;
import dto.DossierDTO;
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
import service.impl.RoleServiceImpl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@WebServlet("/registrant/dossier")
@MultipartConfig(maxFileSize = 5 * 1024 * 1024, maxRequestSize = 22 * 1024 * 1024)
public class DossierServlet extends HttpServlet {

    private static final Set<String> LICENCE_CLASSES = Set.of("A1", "A2", "B1", "B2", "C");
    private static final Map<String, String> PART_TYPES = Map.of(
            "portrait", "PORTRAIT",
            "idFront", "ID_FRONT",
            "idBack", "ID_BACK",
            "healthCertificate", "HEALTH_CERTIFICATE");

    private final DossierDAO dossierDAO = new DossierDAOImpl();
    private final RoleService roleService = new RoleServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = requireRegistrant(request, response);
        if (user == null) {
            return;
        }
        DossierDTO dossier = dossierDAO.findByUserId(user.getUserId());
        request.setAttribute("dossier", dossier);
        String view = request.getParameter("view");
        if ("profile".equals(view)) {
            request.getRequestDispatcher("/views/registrant/profile.jsp").forward(request, response);
        } else if ("track".equals(view)) {
            request.getRequestDispatcher("/views/registrant/track-profile.jsp").forward(request, response);
        } else {
            request.getRequestDispatcher("/views/registrant/upload-documents.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = requireRegistrant(request, response);
        if (user == null) {
            return;
        }
        DossierDTO current = dossierDAO.findByUserId(user.getUserId());
        if (current == null || current.getProfile() == null) {
            sendError(request, response, "Không tìm thấy hồ sơ cá nhân.");
            return;
        }
        if ("Approved".equalsIgnoreCase(current.getStatus())) {
            sendError(request, response, "Hồ sơ đã được xác minh, không cần nộp lại.");
            return;
        }

        String licenceClass = trim(request.getParameter("licenceClass")).toUpperCase();
        String applicantType = trim(request.getParameter("applicantType"));
        if (!LICENCE_CLASSES.contains(licenceClass)
                || !Set.of("student", "free").contains(applicantType)) {
            sendError(request, response, "Vui lòng chọn phân loại học viên và hạng GPLX hợp lệ.");
            return;
        }
        int registrationId = dossierDAO.ensureRegistration(
                current.getProfile().getId(), licenceClass, "SELF", applicantType);
        if (registrationId <= 0) {
            sendError(request, response, "Không thể khởi tạo hồ sơ cho hạng GPLX đã chọn.");
            return;
        }

        try {
            saveUploadedParts(request, current.getProfile().getId(), user.getUserId());
        } catch (IllegalArgumentException ex) {
            sendError(request, response, ex.getMessage());
            return;
        }

        String action = trim(request.getParameter("action"));
        DossierDTO refreshed = dossierDAO.findByRegistrationId(registrationId);
        if ("submit".equals(action)) {
            if (refreshed == null || !refreshed.isComplete()) {
                sendError(request, response,
                        "Cần tải đủ ảnh chân dung, hai mặt CCCD và giấy khám sức khỏe trước khi gửi duyệt.");
                return;
            }
            dossierDAO.updateStatus(registrationId, "Submitted",
                    "Thí sinh đã nộp hồ sơ trực tuyến", user.getUserId());
            request.getSession().setAttribute("dossierSuccess",
                    "Hồ sơ đã được gửi đến Ban quản lý để thẩm định.");
        } else {
            request.getSession().setAttribute("dossierSuccess",
                    "Đã lưu tài liệu. Bạn có thể tiếp tục bổ sung trước khi gửi duyệt.");
        }
        response.sendRedirect(request.getContextPath() + "/registrant/dossier");
    }

    private void saveUploadedParts(HttpServletRequest request, int profileId, int userId)
            throws IOException, ServletException {
        String root = getServletContext().getRealPath("/uploads/dossiers/" + userId);
        if (root == null) {
            throw new IllegalArgumentException("Không xác định được thư mục lưu hồ sơ.");
        }
        Path directory = Paths.get(root).normalize();
        Files.createDirectories(directory);

        for (Map.Entry<String, String> entry : PART_TYPES.entrySet()) {
            Part part = request.getPart(entry.getKey());
            if (part == null || part.getSize() == 0) {
                continue;
            }
            String contentType = part.getContentType() == null ? "" : part.getContentType();
            if (!(contentType.startsWith("image/") || "application/pdf".equals(contentType))) {
                throw new IllegalArgumentException("Chỉ chấp nhận tệp ảnh hoặc PDF.");
            }
            String extension = extension(part.getSubmittedFileName());
            String fileName = entry.getValue().toLowerCase() + "-" + UUID.randomUUID() + extension;
            Path target = directory.resolve(fileName).normalize();
            if (!target.startsWith(directory)) {
                throw new IllegalArgumentException("Tên tệp không hợp lệ.");
            }
            part.write(target.toString());
            persistAcrossCleanBuild(target, userId, fileName);
            dossierDAO.saveDocument(profileId, entry.getValue(),
                    "/uploads/dossiers/" + userId + "/" + fileName);
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
        Path sourceDirectory = projectRoot.resolve("web/uploads/dossiers/" + userId).normalize();
        if (!sourceDirectory.startsWith(projectRoot.resolve("web").normalize())) {
            return;
        }
        Files.createDirectories(sourceDirectory);
        Files.copy(runtimeFile, sourceDirectory.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
    }

    private User requireRegistrant(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        User user = currentUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return null;
        }
        if (!"Registrant".equalsIgnoreCase(roleService.getRoleNameById(user.getRoleId()))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        return user;
    }

    private void sendError(HttpServletRequest request, HttpServletResponse response, String message)
            throws ServletException, IOException {
        request.setAttribute("dossierError", message);
        User user = currentUser(request);
        if (user != null) {
            request.setAttribute("dossier", dossierDAO.findByUserId(user.getUserId()));
        }
        request.getRequestDispatcher("/views/registrant/upload-documents.jsp").forward(request, response);
    }

    private static User currentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute("user");
        return value instanceof User ? (User) value : null;
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

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
