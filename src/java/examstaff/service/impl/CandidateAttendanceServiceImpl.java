package examstaff.service.impl;

import examstaff.dto.exam.ExamRegistrationDTO;
import examstaff.service.CandidateAttendanceService;
import examstaff.service.ExamRegistrationService;
import examstaff.service.impl.ExamRegistrationServiceImpl;

import java.util.ArrayList;
import java.util.List;

/** Implementation: đánh dấu / khôi phục vắng mặt thí sinh. */
public class CandidateAttendanceServiceImpl implements CandidateAttendanceService {

    private final ExamRegistrationService registrationService = new ExamRegistrationServiceImpl();

    /**
     * Đánh dấu vắng mặt cố định (permanent absent) cho thí sinh.
     *
     * @param candidateId mã đăng ký thí sinh
     * @return true nếu đánh dấu thành công
     */
    @Override
    public boolean markPermanentAbsent(int candidateId) {
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
    @Override
    public boolean restoreAbsentCandidate(ExamRegistrationDTO profile) {
        if (profile == null) {
            return false;
        }
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
    @Override
    public List<ExamRegistrationDTO> markIncompleteAsAbsentAtEndShift(List<ExamRegistrationDTO> activeQueue) {
        List<ExamRegistrationDTO> marked = new ArrayList<>();
        if (activeQueue == null) {
            return marked;
        }
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
