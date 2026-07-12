package examstaff.service;

import examstaff.dto.exam.ExamRegistrationDTO;

import java.util.List;

public interface CandidateAttendanceService {

    boolean markPermanentAbsent(int candidateId);

    boolean restoreAbsentCandidate(ExamRegistrationDTO profile);

    List<ExamRegistrationDTO> markIncompleteAsAbsentAtEndShift(List<ExamRegistrationDTO> activeQueue);
}
