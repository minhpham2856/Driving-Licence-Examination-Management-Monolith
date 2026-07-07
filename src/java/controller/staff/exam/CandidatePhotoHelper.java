package controller.staff.exam;

import dto.exam.ExamRegistrationDTO;
import jakarta.servlet.ServletContext;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class CandidatePhotoHelper {

    private CandidatePhotoHelper() {
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

    private static File resolveWritablePhotoDir(ServletContext ctx) throws IOException {
        String configured = System.getProperty("dlem.photos.dir");
        if (configured != null && !configured.isBlank()) {
            return ensureDir(new File(configured.trim()));
        }
        String webRoot = ctx != null ? ctx.getRealPath("/") : null;
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

    private static List<File> collectPhotoSearchDirs(ServletContext ctx, String webRoot) {
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
        if (ctx != null) {
            try {
                dirs.add(resolveWritablePhotoDir(ctx));
            } catch (IOException ignored) {
            }
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

    public static File resolveCandidatesUploadDir(ServletContext ctx) {
        try {
            return resolveWritablePhotoDir(ctx);
        } catch (IOException e) {
            File dir = photoDir();
            if (!dir.exists()) {
                dir.mkdirs();
            }
            return dir;
        }
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

    public static boolean isValidPhotoFile(String webRoot, String photoUrl) {
        return findPhotoFile(null, webRoot, photoUrl) != null;
    }

    public static File findPhotoFile(ServletContext ctx, String webRoot, String photoUrl) {
        String fileName = extractFileName(photoUrl);
        if (fileName == null) {
            return null;
        }
        for (File dir : collectPhotoSearchDirs(ctx, webRoot)) {
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
        if (ctx != null && photoUrl != null && !photoUrl.isBlank()) {
            String relative = photoUrl.trim().replace("\\", "/").replaceFirst("^/+", "");
            String realPath = ctx.getRealPath("/" + relative);
            if (realPath != null) {
                File viaCtx = new File(realPath);
                if (viaCtx.isFile() && viaCtx.length() > 0) {
                    return viaCtx;
                }
            }
        }
        return null;
    }

    public static boolean hasPhotoRecord(ExamRegistrationDTO reg) {
        if (reg == null) {
            return false;
        }
        String photoUrl = reg.getPhotoUrl();
        return photoUrl != null && !photoUrl.trim().isEmpty();
    }

    public static boolean hasCapturedPhoto(String webRoot, ExamRegistrationDTO reg) {
        return reg != null && isValidPhotoFile(webRoot, reg.getPhotoUrl());
    }

    public static boolean hasCapturedPhoto(ServletContext ctx, String webRoot, ExamRegistrationDTO reg) {
        return reg != null && findPhotoFile(ctx, webRoot, reg.getPhotoUrl()) != null;
    }

    public static boolean resolveCapturedPhoto(String webRoot, ExamRegistrationDTO reg) {
        if (reg == null) {
            return false;
        }
        boolean valid = hasCapturedPhoto(webRoot, reg);
        reg.setValidCapturedPhoto(valid);
        return valid;
    }

    public static boolean resolveCapturedPhoto(ServletContext ctx, String webRoot, ExamRegistrationDTO reg) {
        if (reg == null) {
            return false;
        }
        boolean valid = hasCapturedPhoto(ctx, webRoot, reg);
        reg.setValidCapturedPhoto(valid);
        return valid;
    }

    public static void normalizeQueue(String webRoot, List<ExamRegistrationDTO> qList) {
        normalizeQueue(null, webRoot, qList);
    }

    public static void normalizeQueue(ServletContext ctx, String webRoot, List<ExamRegistrationDTO> qList) {
        if (qList == null) {
            return;
        }
        for (ExamRegistrationDTO reg : qList) {
            resolveCapturedPhoto(ctx, webRoot, reg);
        }
    }
}
