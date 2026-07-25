package examstaff.util;

import examstaff.dto.ExamRegistrationDTO;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility thuần quản lý lưu/đọc ảnh thí sinh trên đĩa data runtime — không phụ thuộc webRoot.
 * Đồng bộ PhotoImageUrl trên CSDL với file thật dưới candidate-photos/.
 *
 * Vai trò trong luồng examstaff:
 * Bàn thủ tục chụp ảnh (ProcedureWorkflowServiceImpl) ghi bytes qua writePhotoFile;
 * path lưu DB dạng STORED_PHOTO_PREFIXfileName qua toWebPhotoPath.
 * Servlet ảnh và hàng đợi gọi findPhotoFile / normalizePhotoUrl để phục vụ JSP/TV.
 *
 * Thư mục và cách hoạt động:
 * Thứ tự ưu tiên photoDir(): -Ddlem.photos.dir →
 * $catalina.base/dlem-data/candidate-photos → $user.home/.dlem/candidate-photos.
 * URL http(s) giữ nguyên; local → resolve absolute path nếu file tồn tại.
 * normalizeQueue mutate photoUrl trên list ExamRegistrationDTO tại chỗ.
 *
 * Ai gọi:
 * ProcedureWorkflowServiceImpl, CandidatePhotoServiceImpl,
 * CandidatePhotoServlet, CandidateQueueQueryServiceImpl,
 * DocumentServiceImpl — chụp, hiển thị và validate ảnh thí sinh.
 */
public final class CandidatePhotoFiles {

    /** Prefix tham chiếu lưu DB cho ảnh chụp thủ tục. */
    public static final String STORED_PHOTO_PREFIX = "candidate-photos/";

    private CandidatePhotoFiles() {
    }

    /**
     * Thư mục gốc lưu ảnh theo JVM / Tomcat / user home.
     * @return thư mục gốc ảnh (chưa đảm bảo đã tồn tại)
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

    /**
     * Chuẩn hóa photoUrl trên hàng đợi (mutate tại chỗ).
     * @param queue danh sách đăng ký (null/rỗng → no-op)
     */
    public static void normalizeQueue(List<ExamRegistrationDTO> queue) {
        if (queue == null || queue.isEmpty()) {
            return;
        }
        for (ExamRegistrationDTO c : queue) {
            if (c == null) {
                continue;
            }
            String normalized = normalizePhotoUrl(c.getPhotoUrl());
            if (normalized != null) {
                c.setPhotoUrl(normalized);
            }
        }
    }

    /**
     * Chuẩn hóa tham chiếu ảnh: http(s) giữ nguyên; local → absolute path nếu tìm thấy file.
     * @param photoUrl tham chiếu DB / URL
     * @return absolute path nếu tìm thấy; ngược lại chuỗi trim / null
     */
    public static String normalizePhotoUrl(String photoUrl) {
        if (photoUrl == null || photoUrl.isBlank()) {
            return photoUrl;
        }
        String trimmed = photoUrl.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed;
        }
        File found = findPhotoFile(trimmed);
        if (found != null) {
            return found.getAbsolutePath();
        }
        return trimmed;
    }

    /**
     * Basename từ URL/path ảnh.
     * @param photoUrl URL hoặc path
     * @return tên file, hoặc null nếu blank
     */
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

    /**
     * Ghi bytes ảnh vào thư mục data runtime.
     * @param fileName   basename
     * @param imageBytes nội dung ảnh
     * @throws IOException dữ liệu/thư mục không hợp lệ
     */
    public static void writePhotoFile(String fileName, byte[] imageBytes) throws IOException {
        if (fileName == null || fileName.isBlank() || imageBytes == null || imageBytes.length == 0) {
            throw new IOException("Dữ liệu ảnh không hợp lệ");
        }
        File dir = ensureDir(photoDir());
        File file = new File(dir, fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(imageBytes);
        }
    }

    /**
     * Tham chiếu lưu Candidate.PhotoImageUrl: candidate-photos/{fileName}.
     * Hàng cũ assets/imgs/candidates/... vẫn đọc được nhờ extractFileName.
     * @param fileName basename, ví dụ 001_captured.jpg
     * @return candidate-photos/ + fileName
     */
    public static String toWebPhotoPath(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return STORED_PHOTO_PREFIX;
        }
        String base = extractFileName(fileName);
        return STORED_PHOTO_PREFIX + (base != null ? base : fileName.trim());
    }

    /**
     * Tìm file theo basename trong photoSearchDirs().
     * @param photoUrl tham chiếu DB / path
     * @return file hợp lệ hoặc null
     */
    public static File findPhotoFile(String photoUrl) {
        String fileName = extractFileName(photoUrl);
        if (fileName == null || fileName.isBlank()) {
            return null;
        }
        for (File dir : photoSearchDirs()) {
            File candidate = new File(dir, fileName);
            if (candidate.isFile() && candidate.length() > 0) {
                return candidate;
            }
        }
        return null;
    }

    /** Thư mục quét: dlem.photos.dir (nếu có) rồi photoDir(). */
    private static List<File> photoSearchDirs() {
        Set<File> dirs = new LinkedHashSet<>();
        String configured = System.getProperty("dlem.photos.dir");
        if (configured != null && !configured.isBlank()) {
            dirs.add(new File(configured.trim()));
        }
        dirs.add(photoDir());
        return new ArrayList<>(dirs);
    }

    private static File ensureDir(File dir) throws IOException {
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Không tạo được thư mục lưu ảnh: " + dir.getAbsolutePath());
        }
        return dir;
    }
}
