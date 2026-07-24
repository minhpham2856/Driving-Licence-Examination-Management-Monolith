package examstaff.service.impl.support.procedure;

import examstaff.dto.ExamRegistrationDTO;

/**
 * Suy luận bước thủ tục (1–3) và thông báo lỗi UI cho màn bàn thủ tục.
 * <p>
 * <b>Không</b> gọi DAO / HTTP — chỉ đọc ExamRegistrationDTO + cờ client
 * (requestedStep, sbdChanged, hasValidPhoto).
 * Servlet/consolidator gọi resolveStep trước khi bind JSP.
 *
 * Thứ tự ưu tiên bước (resolveStep):
 * - requestedStep từ form (nếu có)
 * - Đổi SBD → ép bước 1 nếu chưa có step
 * - Suy từ profile: chưa có → 1; đã trả phí → 3; đã có ảnh → 2; còn lại → 1
 *
 * Thông báo validation:
 * - photoRequiredForStep3Message — bắt buộc chụp ảnh trước bước thu phí
 * - paymentBlockedNoPhotoMessage — chặn thu phí khi chưa có ảnh
 */
public final class ProcedureStepHelper {

    /** Utility class — không khởi tạo. */
    private ProcedureStepHelper() {
    }

    /**
     * Chọn bước hiện tại: ưu tiên requestedStep, rồi SBD đổi, rồi profile/ảnh/thanh toán.
     * @param requestedStep bước client gửi (có thể blank)
     * @param sbdChanged    vừa đổi SBD → ép về bước 1 nếu chưa có step
     * @param profile       hồ sơ đăng ký (null = bước 1)
     * @param hasValidPhoto đã có ảnh chân dung hợp lệ
     * @return mã bước "1", "2" hoặc "3"
     */
    public static String resolveStep(String requestedStep, boolean sbdChanged,
            ExamRegistrationDTO profile, boolean hasValidPhoto) {
        String step = requestedStep;
        // Validate / ưu tiên client + đổi SBD
        if (sbdChanged && (step == null || step.trim().isEmpty())) {
            return "1";
        }
        if (step != null && !step.trim().isEmpty()) {
            return step.trim();
        }
        // Suy từ trạng thái hồ sơ: chưa có / đã trả phí / đã có ảnh
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
     * @return nội dung tiếng Việt
     */
    public static String photoRequiredForStep3Message() {
        return "Bắt buộc chụp ảnh chân dung trước khi thu lệ phí. Quay lại Bước 2 để chụp hoặc chụp lại nếu đã lưu ảnh.";
    }

    /**
     * Thông báo chặn thu phí khi chưa có ảnh.
     * @return nội dung tiếng Việt
     */
    public static String paymentBlockedNoPhotoMessage() {
        return "Không thể thu lệ phí: thí sinh chưa chụp ảnh chân dung tại bàn thủ tục.";
    }
}
