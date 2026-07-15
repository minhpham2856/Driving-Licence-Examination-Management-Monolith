package examstaff.service.impl.support.call;

import examstaff.service.RegistrationService;
import examstaff.service.impl.RegistrationServiceImpl;

import examstaff.dto.ExamRegistrationDTO;
import java.util.ArrayList;
import java.util.List;

/** Implementation: đánh dấu / khôi phục vắng mặt thí sinh. */
public class CandidateAttendanceServiceImpl {

    private final RegistrationService registrationService = new RegistrationServiceImpl();

    /**
     * Đánh dấu vắng mặt cố định (permanent absent) cho thí sinh.
     *
     * @param candidateId mã đăng ký thí sinh
     * @return true nếu đánh dấu thành công
     */
    public boolean markPermanentAbsent(int candidateId) {
        // Mutate: fail điểm → đình chỉ → đánh vắng
        registrationService.updateScores(candidateId, 0, "failed", 0, "failed");
        registrationService.markSuspended(candidateId);
        return registrationService.markAbsent(candidateId);
    }

    /**
     * Khôi phục thí sinh đã bị đánh vắng về trạng thái có thể gọi lại.
     *
     * @param profile hồ sơ thí sinh
     * @return true nếu khôi phục thành công
     */
    public boolean restoreAbsentCandidate(ExamRegistrationDTO profile) {
        // Validate
        if (profile == null) {
            return false;
        }
        // Mutate DB + DTO in-memory
        registrationService.undoSuspension(profile.getId());
        registrationService.clearAbsentMarking(profile.getId());
        profile.setSuspended(false);
        profile.setAbsent(false);
        profile.setTheoryPassed("none");
        profile.setPracticalPassed("none");
        profile.setTheoryScore(null);
        profile.setPracticalScore(null);
        return true;
    }

    /**
     * Khi kết thúc ca: đánh vắng các thí sinh còn dở trong hàng đợi active.
     *
     * @param activeQueue hàng đợi còn pending khi đóng ca
     * @return danh sách đã được đánh vắng
     */
    public List<ExamRegistrationDTO> markIncompleteAsAbsentAtEndShift(List<ExamRegistrationDTO> activeQueue) {
        List<ExamRegistrationDTO> marked = new ArrayList<>();
        // Validate
        if (activeQueue == null) {
            return marked;
        }
        // Mutate từng thí sinh chưa xong thủ tục (chưa thanh toán + ảnh)
        for (ExamRegistrationDTO c : activeQueue) {
            boolean isDone = c.isPaymentCompleted() && c.isValidCapturedPhoto();
            if (isDone) {
                continue;
            }
            c.setAbsent(true);
            c.setTheoryPassed("failed");
            c.setPracticalPassed("failed");
            c.setTheoryScore(0);
            c.setPracticalScore(0);
            registrationService.updateScores(c.getId(), 0, "failed", 0, "failed");
            registrationService.markAbsent(c.getId());
            marked.add(c);
        }
        return marked;
    }
}
