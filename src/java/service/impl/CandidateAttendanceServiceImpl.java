package service.impl;

import dto.exam.ExamRegistrationDTO;
import service.CandidateAttendanceService;
import service.ExamRegistrationService;

import java.util.ArrayList;
import java.util.List;

public class CandidateAttendanceServiceImpl implements CandidateAttendanceService {

    private final ExamRegistrationService registrationService = new ExamRegistrationServiceImpl();

    @Override
    public boolean markTemporaryAbsent(int candidateId) {
        return registrationService.markAbsent(candidateId);
    }

    @Override
    public boolean markPermanentAbsent(int candidateId) {
        registrationService.updateScores(candidateId, 0, "failed", 0, "failed");
        registrationService.markSuspended(candidateId);
        return registrationService.markAbsent(candidateId);
    }

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
