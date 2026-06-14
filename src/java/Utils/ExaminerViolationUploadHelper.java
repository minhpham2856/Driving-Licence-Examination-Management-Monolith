package Utils;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

public final class ExaminerViolationUploadHelper {

    private static final long MAX_BYTES = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/webp");

    private ExaminerViolationUploadHelper() {
    }

    public static String saveEvidence(HttpServletRequest request, int sessionId, String sbd)
            throws IOException, ServletException {
        Part part = request.getPart("evidenceFile");
        if (part == null || part.getSize() <= 0) {
            return null;
        }
        if (part.getSize() > MAX_BYTES) {
            throw new IOException("File minh chứng vượt quá 5MB.");
        }

        String contentType = part.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            throw new IOException("Chỉ chấp nhận ảnh JPG, PNG hoặc WEBP.");
        }

        String ext = switch (contentType.toLowerCase()) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };

        String safeSbd = sbd != null ? sbd.replaceAll("[^a-zA-Z0-9\\-_]", "") : "unknown";
        String fileName = "violation_" + sessionId + "_" + safeSbd + "_"
                + UUID.randomUUID().toString().substring(0, 8) + ext;

        Path uploadDir = Path.of(request.getServletContext().getRealPath("/assets/uploads/violations"));
        Files.createDirectories(uploadDir);
        Path target = uploadDir.resolve(fileName);
        try (var in = part.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return "assets/uploads/violations/" + fileName;
    }
}
