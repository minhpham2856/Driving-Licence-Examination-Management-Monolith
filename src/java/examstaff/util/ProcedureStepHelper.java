package examstaff.util;

import dto.exam.ExamRegistrationDTO;

public final class ProcedureStepHelper {

    private ProcedureStepHelper() {
    }

    public static String resolveStep(String requestedStep, boolean sbdChanged,
            ExamRegistrationDTO profile, boolean hasValidPhoto) {
        String step = requestedStep;
        if (sbdChanged && (step == null || step.trim().isEmpty())) {
            return "1";
        }
        if (step != null && !step.trim().isEmpty()) {
            return step.trim();
        }
        if (profile == null) {
            return "1";
        }
        if (profile.isPaymentCompleted()) {
            return "3";
        }
        if (hasValidPhoto) {
            return "2";
        }
        return "1";
    }

    public static String photoRequiredForStep3Message() {
        return "Bắt buộc chụp ảnh chân dung trước khi thu lệ phí. Quay lại Bước 2 để chụp hoặc chụp lại nếu đã lưu ảnh.";
    }

    public static String paymentBlockedNoPhotoMessage() {
        return "Không thể thu lệ phí: thí sinh chưa chụp ảnh chân dung tại bàn thủ tục.";
    }
}
