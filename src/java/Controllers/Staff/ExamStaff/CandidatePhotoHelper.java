package Controllers.Staff.ExamStaff;

import DAO.ExamRegistrationDAO;
import Models.ExamRegistration;
import jakarta.servlet.ServletContext;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public final class CandidatePhotoHelper {

    private CandidatePhotoHelper() {
    }

    /**
     * Thư mục duy nhất lưu ảnh thí sinh (ưu tiên: JVM property → Tomcat → user home).
     * Không ghi mirror sang chỗ khác.
     */
    public static File photoDir() {
        String configured = System.getProperty("dlem.photos.dir");
        if (configured != null && !configured.isBlank()) {
            return new File(configured.trim());
        }
        String catalina = System.getProperty("catalina.base");
        if (catalina != null && !catalina.isBlank()) {
            return new File(catalina, "dlem-data" + File.separator + "candidate-photos");
        }
        return new File(System.getProperty("user.home", "."), ".dlem" + File.separator + "candidate-photos");
    }

    public static File resolveCandidatesUploadDir(ServletContext ctx) {
        File dir = photoDir();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static String extractFileName(String photoUrl) {
        if (photoUrl == null || photoUrl.isBlank()) {
            return null;
        }
        String normalized = photoUrl.trim().replace("\\", "/");
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        return normalized.contains("/")
                ? normalized.substring(normalized.lastIndexOf('/') + 1)
                : normalized;
    }

    public static void writePhotoFile(ServletContext ctx, String fileName, byte[] imageBytes) throws IOException {
        if (fileName == null || fileName.isBlank() || imageBytes == null || imageBytes.length == 0) {
            throw new IOException("Dữ liệu ảnh không hợp lệ");
        }
        File dir = photoDir();
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Không tạo được thư mục lưu ảnh: " + dir.getAbsolutePath());
        }
        File file = new File(dir, fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(imageBytes);
        }
    }

    /** Đường dẫn logic lưu trong DB — file thật nằm trong {@link #photoDir()}. */
    public static String toWebPhotoPath(String fileName) {
        return "assets/imgs/candidates/" + fileName;
    }

    public static boolean isValidPhotoFile(String webRoot, String photoUrl) {
        return findPhotoFile(null, webRoot, photoUrl) != null;
    }

    public static File findPhotoFile(ServletContext ctx, String webRoot, String photoUrl) {
        String fileName = extractFileName(photoUrl);
        if (fileName == null) {
            return null;
        }
        File file = new File(photoDir(), fileName);
        return file.isFile() && file.length() > 0 ? file : null;
    }

    public static boolean hasPhotoRecord(ExamRegistration reg) {
        if (reg == null) {
            return false;
        }
        String photoUrl = reg.getPhotoUrl();
        return photoUrl != null && !photoUrl.trim().isEmpty();
    }

    public static boolean hasCapturedPhoto(String webRoot, ExamRegistration reg) {
        return reg != null && isValidPhotoFile(webRoot, reg.getPhotoUrl());
    }

    public static boolean resolveCapturedPhoto(String webRoot, ExamRegistration reg) {
        return hasPhotoRecord(reg) || hasCapturedPhoto(webRoot, reg);
    }

    public static void clearInvalidPhotoReference(ExamRegistration reg, String webRoot) {
        if (reg == null) {
            return;
        }
        String photoUrl = reg.getPhotoUrl();
        if (photoUrl != null && !photoUrl.isEmpty() && !isValidPhotoFile(webRoot, photoUrl)) {
            reg.setPhotoUrl("");
        }
    }

    public static void normalizeQueue(String webRoot, List<ExamRegistration> qList, ExamRegistrationDAO regDAO) {
        if (qList == null) {
            return;
        }
        for (ExamRegistration reg : qList) {
            boolean hasFile = findPhotoFile(null, webRoot, reg.getPhotoUrl()) != null;
            reg.setValidCapturedPhoto(hasFile || hasPhotoRecord(reg));
        }
    }
}
