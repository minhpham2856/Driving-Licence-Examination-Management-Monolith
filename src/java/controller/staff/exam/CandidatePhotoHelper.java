package controller.staff.exam;

import dao.ExamRegistrationDAO;
import model.exam.ExamRegistration;
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

    public static boolean hasCapturedPhoto(String webRoot, ExamRegistration reg) {
        return reg != null && isValidPhotoFile(webRoot, reg.getPhotoUrl());
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

    public static void normalizeQueue(String webRoot, List<ExamRegistration> qList, ExamRegistrationDAO regdao) {
        if (qList == null || webRoot == null) {
            return;
        }
        for (ExamRegistration reg : qList) {
            boolean valid = hasCapturedPhoto(webRoot, reg);
            reg.setValidCapturedPhoto(valid);
            if (!valid && reg.getPhotoUrl() != null && !reg.getPhotoUrl().isEmpty()) {
                if (regdao != null) {
                    regdao.updatePhoto(reg.getId(), null);
                }
                reg.setPhotoUrl("");
            }
        }
    }
}
