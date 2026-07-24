package registrant.util;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Lưu file upload legacy ngoài thư mục WAR — phục vụ dữ liệu cũ trước khi migrate Cloudinary.
 * <p>
 * Thư mục {@code catalina.base/dlem-uploads/registrant}; URL công khai {@code /uploads/registrant/*}
 * qua {@link registrant.controller.RegistrantUploadFileServlet}. Hồ sơ mới dùng {@link CloudinaryDocumentStorage}.
 */
public final class RegistrantUploadStorage {

    private static final String SUBDIR = "registrant";
    private static final String PUBLIC_PREFIX = "/uploads/registrant/";

    private RegistrantUploadStorage() {
    }

    /** Thư mục lưu upload registrant (tạo nếu chưa có). */
    public static Path resolveUploadDirectory(ServletContext ctx) throws IOException {
        Path base = resolveConfiguredDirectory(ctx);
        Files.createDirectories(base);
        return base;
    }

    /** Path file vật lý trong thư mục upload theo fileName. */
    public static Path resolveStoredFile(ServletContext ctx, String fileName) throws IOException {
        return resolveUploadDirectory(ctx).resolve(sanitizeFileName(fileName));
    }

    /** Chuẩn hóa URL đã lưu trong DB thành đường dẫn public qua servlet /uploads/registrant/*. */
    public static String normalizePublicUrl(HttpServletRequest request, String storedUrl) {
        if (storedUrl == null || storedUrl.isBlank()) {
            return storedUrl;
        }
        if (storedUrl.startsWith("http://") || storedUrl.startsWith("https://")) {
            return storedUrl;
        }
        String ctx = request.getContextPath();
        String path = storedUrl.trim();
        if (path.startsWith(ctx)) {
            path = path.substring(ctx.length());
        }
        if (path.startsWith(PUBLIC_PREFIX)) {
            return ctx + path;
        }
        int slash = path.lastIndexOf('/');
        String fileName = slash >= 0 ? path.substring(slash + 1) : path;
        if (fileName.isBlank()) {
            return storedUrl;
        }
        return ctx + PUBLIC_PREFIX + sanitizeFileName(fileName);
    }

    /** Xóa file vật lý nếu tồn tại (bỏ qua lỗi I/O). */
    public static void deleteStoredFile(ServletContext ctx, String storedUrl) {
        String fileName = extractFileName(storedUrl);
        if (fileName.isBlank()) {
            return;
        }
        try {
            Path file = resolveStoredFile(ctx, fileName);
            Files.deleteIfExists(file);
        } catch (IOException ignored) {
        }
    }

    /** Tách tên file an toàn từ URL/path đã lưu. */
    public static String extractFileName(String storedUrl) {
        if (storedUrl == null || storedUrl.isBlank()) {
            return "";
        }
        String path = storedUrl.trim();
        int query = path.indexOf('?');
        if (query >= 0) {
            path = path.substring(0, query);
        }
        int slash = path.lastIndexOf('/');
        return slash >= 0 ? sanitizeFileName(path.substring(slash + 1)) : sanitizeFileName(path);
    }

    private static Path resolveConfiguredDirectory(ServletContext ctx) {
        String configured = ctx.getInitParameter("registrantUploadDirectory");
        if (configured != null && !configured.isBlank()) {
            return Paths.get(configured.trim());
        }
        String catalina = System.getProperty("catalina.base");
        if (catalina != null && !catalina.isBlank()) {
            return Paths.get(catalina, "dlem-uploads", SUBDIR);
        }
        String realPath = ctx.getRealPath(PUBLIC_PREFIX);
        if (realPath != null && !realPath.isBlank()) {
            return Paths.get(realPath);
        }
        return Paths.get(System.getProperty("java.io.tmpdir"), "dlem-uploads", SUBDIR);
    }

    /** Làm sạch tên file (chỉ giữ ký tự an toàn). */
    public static String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "file";
        }
        String cleaned = fileName.replace("\\", "/");
        int slash = cleaned.lastIndexOf('/');
        if (slash >= 0) {
            cleaned = cleaned.substring(slash + 1);
        }
        cleaned = cleaned.replaceAll("[^a-zA-Z0-9._-]", "_");
        return cleaned.isBlank() ? "file" : cleaned;
    }
}
