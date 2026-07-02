package controller.staff.exam;

import dto.exam.ExamRegistrationDTO;
import jakarta.servlet.ServletContext;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public final class CandidatePhotoHelper {

    private CandidatePhotoHelper() {
    }

    // photo dir
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
    // Xac dinh candidates upload dir

    public static File resolveCandidatesUploadDir(ServletContext ctx) {
        File dir = photoDir();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    // extract file name
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
    // write photo file
                : normalized;
            // ioexception
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
    // to web photo path
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(imageBytes);
        }
    // Kiem tra valid photo file
    }

    public static String toWebPhotoPath(String fileName) {
    // Tim photo file
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
        File inDataDir = new File(photoDir(), fileName);
        if (inDataDir.isFile() && inDataDir.length() > 0) {
            return inDataDir;
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
    // Co photo record hay khong
                if (viaCtx.isFile() && viaCtx.length() > 0) {
                    return viaCtx;
                }
            }
        }
        return null;
    }
    // Co captured photo hay khong

    public static boolean hasPhotoRecord(ExamRegistrationDTO reg) {
        if (reg == null) {
    // Xac dinh captured photo
            return false;
        }
        String photoUrl = reg.getPhotoUrl();
        return photoUrl != null && !photoUrl.trim().isEmpty();
    }
    // normalize queue

    public static boolean hasCapturedPhoto(String webRoot, ExamRegistrationDTO reg) {
        return reg != null && isValidPhotoFile(webRoot, reg.getPhotoUrl());
    }
            // Xac dinh captured photo

    public static boolean resolveCapturedPhoto(String webRoot, ExamRegistrationDTO reg) {
        boolean valid = hasPhotoRecord(reg) || hasCapturedPhoto(webRoot, reg);
        reg.setValidCapturedPhoto(valid);
        return valid;
    }

    public static void normalizeQueue(String webRoot, List<ExamRegistrationDTO> qList) {
        if (qList == null) {
            return;
        }
        for (ExamRegistrationDTO reg : qList) {
            resolveCapturedPhoto(webRoot, reg);
        }
    }
}
