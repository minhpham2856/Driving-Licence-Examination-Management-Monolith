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

    /** {@inheritDoc} */
    @Override
    public boolean markPermanentAbsent(int candidateId) {
        registrationService.updateScores(candidateId, 0, "failed", 0, "failed");
        registrationService.markSuspended(candidateId);
        return registrationService.markAbsent(candidateId);
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
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
