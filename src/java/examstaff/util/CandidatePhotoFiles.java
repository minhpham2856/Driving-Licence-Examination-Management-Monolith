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
 * Helper thuần quản lý lưu/đọc ảnh thí sinh trên đĩa data (không phụ thuộc webRoot).
 * <p>
 * <b>DB vs đĩa:</b> {@code PhotoImageUrl} lưu {@code candidate-photos/{file}} (hoặc URL
 * {@code http(s)} / basename). File thật nằm dưới thư mục data runtime.
 * <p>
 * Thứ tự thư mục ghi/đọc:
 * <ol>
 *   <li>{@code -Ddlem.photos.dir}</li>
 *   <li>{@code $catalina.base/dlem-data/candidate-photos}</li>
 *   <li>{@code $user.home/.dlem/candidate-photos}</li>
 * </ol>
 */
public final class CandidatePhotoFiles {

    /** Prefix tham chiếu lưu DB cho ảnh chụp thủ tục. */
    public static final String STORED_PHOTO_PREFIX = "candidate-photos/";

    private CandidatePhotoFiles() {
    }

    /**
     * Thư mục gốc lưu ảnh theo JVM / Tomcat / user home.
     *
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
     * Chuẩn hóa {@code photoUrl} trên hàng đợi (mutate tại chỗ).
     *
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
     * Chuẩn hóa tham chiếu ảnh: {@code http(s)} giữ nguyên; local → absolute path nếu tìm thấy file.
     *
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
     *
     * @param photoUrl URL hoặc path
     * @return tên file, hoặc {@code null} nếu blank
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
     *
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
     * Tham chiếu lưu {@code Candidate.PhotoImageUrl}: {@code candidate-photos/{fileName}}.
     * Hàng cũ {@code assets/imgs/candidates/...} vẫn đọc được nhờ {@link #extractFileName}.
     *
     * @param fileName basename, ví dụ {@code 001_captured.jpg}
     * @return {@code candidate-photos/} + fileName
     */
    public static String toWebPhotoPath(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return STORED_PHOTO_PREFIX;
        }
        String base = extractFileName(fileName);
        return STORED_PHOTO_PREFIX + (base != null ? base : fileName.trim());
    }

    /**
     * Tìm file theo basename trong {@link #photoSearchDirs()}.
     *
     * @param photoUrl tham chiếu DB / path
     * @return file hợp lệ hoặc {@code null}
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

    /** Thư mục quét: {@code dlem.photos.dir} (nếu có) rồi {@link #photoDir()}. */
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
