package examstaff.util;

import examstaff.dto.ExamRegistrationDTO;

/** Suy luận bước thủ tục (1–3) và thông báo lỗi ảnh/thu phí. */
public final class ProcedureStepHelper {

    private ProcedureStepHelper() {
    }

    /**
     * Chọn bước hiện tại: ưu tiên {@code requestedStep}, rồi SBD đổi, rồi profile/ảnh/thanh toán.
     *
     * @param requestedStep bước client gửi (có thể blank)
     * @param sbdChanged    vừa đổi SBD → ép về bước 1 nếu chưa có step
     * @param profile       hồ sơ đăng ký (null = bước 1)
     * @param hasValidPhoto đã có ảnh chân dung hợp lệ
     * @return mã bước {@code "1"}, {@code "2"} hoặc {@code "3"}
     */
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

    /**
     * Thông báo bắt buộc chụp ảnh trước bước thu phí.
     *
     * @return nội dung tiếng Việt
     */
    public static String photoRequiredForStep3Message() {
        return "Bắt buộc chụp ảnh chân dung trước khi thu lệ phí. Quay lại Bước 2 để chụp hoặc chụp lại nếu đã lưu ảnh.";
    }

    /**
     * Thông báo chặn thu phí khi chưa có ảnh.
     *
     * @return nội dung tiếng Việt
     */
    public static String paymentBlockedNoPhotoMessage() {
        return "Không thể thu lệ phí: thí sinh chưa chụp ảnh chân dung tại bàn thủ tục.";
    }
}
