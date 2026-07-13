package examstaff.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Lưu trữ ảnh thí sinh — không dùng Servlet API. */
public final class CandidatePhotoStorageUtil {

    private CandidatePhotoStorageUtil() {
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

    public static void writePhotoFile(String webRoot, String fileName, byte[] imageBytes) throws IOException {
        if (fileName == null || fileName.isBlank() || imageBytes == null || imageBytes.length == 0) {
            throw new IOException("Dữ liệu ảnh không hợp lệ");
        }
        String configured = System.getProperty("dlem.photos.dir");
        File dir;
        if (configured != null && !configured.isBlank()) {
            dir = ensureDir(new File(configured.trim()));
        } else {
            dir = ensureDir(photoDir());
        }
        File file = new File(dir, fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(imageBytes);
        }
    }

    public static String toWebPhotoPath(String fileName) {
        return "assets/imgs/candidates/" + fileName;
    }

    public static File findPhotoFile(String webRoot, String photoUrl) {
        String fileName = extractFileName(photoUrl);
        if (fileName == null) {
            return null;
        }
        for (File dir : collectPhotoSearchDirs(webRoot)) {
            if (dir == null) {
                continue;
            }
            File candidate = new File(dir, fileName);
            if (candidate.isFile() && candidate.length() > 0) {
                return candidate;
            }
        }
        if (webRoot != null && !webRoot.isBlank()) {
            File inWebAssets = new File(webRoot, "assets" + File.separator + "imgs"
                    + File.separator + "candidates" + File.separator + fileName);
            if (inWebAssets.isFile() && inWebAssets.length() > 0) {
                return inWebAssets;
            }
            if (photoUrl != null && photoUrl.contains("/")) {
                String relative = photoUrl.trim().replace("\\", "/").replaceFirst("^/+", "");
                File viaUrl = new File(webRoot, relative.replace("/", File.separator));
                if (viaUrl.isFile() && viaUrl.length() > 0) {
                    return viaUrl;
                }
            }
        }
        return null;
    }

    private static File resolveWritablePhotoDir(String webRoot) throws IOException {
        String configured = System.getProperty("dlem.photos.dir");
        if (configured != null && !configured.isBlank()) {
            return ensureDir(new File(configured.trim()));
        }
        File inProject = projectPhotoDir(webRoot);
        if (inProject != null) {
            return ensureDir(inProject);
        }
        return ensureDir(photoDir());
    }

    private static File projectPhotoDir(String webRoot) {
        File projectRoot = resolveProjectRootFromWebRoot(webRoot);
        if (projectRoot != null) {
            return new File(projectRoot, "candidate-photos");
        }
        return null;
    }

    private static File resolveProjectRootFromWebRoot(String webRoot) {
        if (webRoot == null || webRoot.isBlank()) {
            return null;
        }
        File webRootDir = new File(webRoot);
        File parent = webRootDir.getParentFile();
        if (parent == null) {
            return null;
        }
        if ("web".equalsIgnoreCase(webRootDir.getName())) {
            if ("build".equalsIgnoreCase(parent.getName())) {
                return parent.getParentFile();
            }
            return parent;
        }
        if ("build".equalsIgnoreCase(parent.getName())) {
            return parent.getParentFile();
        }
        return parent;
    }

    private static List<File> collectPhotoSearchDirs(String webRoot) {
        Set<File> dirs = new LinkedHashSet<>();
        String configured = System.getProperty("dlem.photos.dir");
        if (configured != null && !configured.isBlank()) {
            dirs.add(new File(configured.trim()));
        }
        File projectDir = projectPhotoDir(webRoot);
        if (projectDir != null) {
            dirs.add(projectDir);
        }
        if (webRoot != null && !webRoot.isBlank()) {
            File webRootDir = new File(webRoot);
            File parent = webRootDir.getParentFile();
            if (parent != null && "web".equalsIgnoreCase(webRootDir.getName())) {
                dirs.add(new File(parent, "candidate-photos"));
            }
        }
        try {
            dirs.add(resolveWritablePhotoDir(webRoot));
        } catch (IOException ignored) {
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
