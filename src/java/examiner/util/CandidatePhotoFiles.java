package examiner.util;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

// Resolve candidate portrait files stored under candidate-photos/ (local runtime data).
public final class CandidatePhotoFiles {

    public static final String STORED_PHOTO_PREFIX = "candidate-photos/";

    private CandidatePhotoFiles() {
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

    public static boolean isRemoteUrl(String photoUrl) {
        if (photoUrl == null || photoUrl.isBlank()) {
            return false;
        }
        String trimmed = photoUrl.trim();
        return trimmed.startsWith("http://") || trimmed.startsWith("https://");
    }

    private static List<File> photoSearchDirs() {
        Set<File> dirs = new LinkedHashSet<>();
        String configured = System.getProperty("dlem.photos.dir");
        if (configured != null && !configured.isBlank()) {
            dirs.add(new File(configured.trim()));
        }
        dirs.add(photoDir());
        return new ArrayList<>(dirs);
    }
}
