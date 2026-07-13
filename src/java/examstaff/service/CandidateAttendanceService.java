package examstaff.service;

import dto.exam.ExamRegistrationDTO;

import java.util.List;

public interface CandidateAttendanceService {

    boolean markTemporaryAbsent(int candidateId);

    boolean markPermanentAbsent(int candidateId);

    boolean restoreAbsentCandidate(ExamRegistrationDTO profile);

    List<ExamRegistrationDTO> markIncompleteAsAbsentAtEndShift(List<ExamRegistrationDTO> activeQueue);
}
