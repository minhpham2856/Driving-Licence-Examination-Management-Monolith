package util.examstaff;

import dto.exam.ExamRegistrationDTO;

import java.io.File;
import java.util.List;

/** Chuẩn hóa đường dẫn ảnh nội bộ — controller dùng {@link service.CandidatePhotoService#normalizePhotoPaths}. */
public final class CandidatePhotoPathUtil {

    private CandidatePhotoPathUtil() {
    }

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

    public static void normalizeQueue(String webRootPath, List<ExamRegistrationDTO> queue) {
        if (queue == null || queue.isEmpty()) {
            return;
        }
        for (ExamRegistrationDTO c : queue) {
            if (c == null) {
                continue;
            }
            String normalized = normalizePhotoUrl(webRootPath, c.getPhotoUrl());
            if (normalized != null) {
                c.setPhotoUrl(normalized);
            }
        }
    }

    public static String normalizePhotoUrl(String webRootPath, String photoUrl) {
        if (photoUrl == null || photoUrl.isBlank()) {
            return photoUrl;
        }
        String trimmed = photoUrl.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed;
        }
        if (trimmed.startsWith("/")) {
            return trimmed;
        }
        File file = new File(trimmed);
        if (file.isAbsolute() && file.exists()) {
            return trimmed;
        }
        File inData = new File(photoDir(), new File(trimmed).getName());
        if (inData.exists()) {
            return inData.getAbsolutePath();
        }
        if (webRootPath != null && !webRootPath.isBlank()) {
            File underWeb = new File(webRootPath, trimmed.replace('/', File.separatorChar));
            if (underWeb.exists()) {
                return underWeb.getAbsolutePath();
            }
        }
        return trimmed;
    }

    public static boolean photoFileExists(String webRootPath, String photoUrl) {
        if (photoUrl == null || photoUrl.isBlank()) {
            return false;
        }
        String path = normalizePhotoUrl(webRootPath, photoUrl);
        return path != null && new File(path).isFile();
    }
}
