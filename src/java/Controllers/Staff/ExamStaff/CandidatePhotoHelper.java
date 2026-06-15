package Controllers.Staff.ExamStaff;

import DAO.ExamRegistrationDAO;
import Models.ExamRegistration;
import java.io.File;
import java.util.List;

public final class CandidatePhotoHelper {

    private CandidatePhotoHelper() {
    }

    public static boolean isValidPhotoFile(String webRoot, String photoUrl) {
        if (photoUrl == null || photoUrl.trim().isEmpty() || webRoot == null) {
            return false;
        }
        String normalized = photoUrl.trim().replace("\\", "/");
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        File file = new File(webRoot, normalized);
        return file.isFile() && file.length() > 0;
    }

    /** Ảnh đã lưu URL trong DB (bước chụp tại bàn thủ tục). */
    public static boolean hasPhotoRecord(ExamRegistration reg) {
        if (reg == null) {
            return false;
        }
        String photoUrl = reg.getPhotoUrl();
        return photoUrl != null && !photoUrl.trim().isEmpty();
    }

    /** Có ảnh hợp lệ để in/xuất — bắt buộc file thật trên đĩa. */
    public static boolean hasCapturedPhoto(String webRoot, ExamRegistration reg) {
        return reg != null && isValidPhotoFile(webRoot, reg.getPhotoUrl());
    }

    /** Đã chụp ảnh thủ tục: URL trong DB hoặc file tồn tại (không xóa URL khi file tạm thiếu). */
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
        if (qList == null || webRoot == null) {
            return;
        }
        for (ExamRegistration reg : qList) {
            reg.setValidCapturedPhoto(resolveCapturedPhoto(webRoot, reg));
        }
    }
}
