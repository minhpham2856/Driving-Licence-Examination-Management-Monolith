package controller.registrant;

import util.registrant.RegistrantUploadStorage;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Phục vụ file upload <strong>legacy</strong> từ thư mục local ({@code catalina.base/dlem-uploads/registrant}).
 * Hồ sơ mới được lưu trên Cloudinary; servlet này chỉ phục vụ dữ liệu cũ chưa migrate.
 */
@WebServlet(urlPatterns = {"/uploads/registrant/*"})
public class RegistrantUploadFileServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.length() <= 1) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String fileName = RegistrantUploadStorage.sanitizeFileName(pathInfo.substring(1));
        Path file = RegistrantUploadStorage.resolveStoredFile(getServletContext(), fileName);
        if (!Files.isRegularFile(file)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.setContentType(probeContentType(fileName));
        response.setHeader("Cache-Control", "private, max-age=3600");
        response.setContentLengthLong(Files.size(file));

        try (OutputStream out = response.getOutputStream()) {
            Files.copy(file, out);
        }
    }

    private static String probeContentType(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".png")) {
            return "image/png";
        }
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (lower.endsWith(".pdf")) {
            return "application/pdf";
        }
        return "application/octet-stream";
    }
}
