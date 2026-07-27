package examiner.service;

import examiner.dto.ExamDispatchResult;
import examiner.dto.ServiceResult;
import shared.enums.SectionType;
import shared.model.Candidate;

// Service contract for routing candidates between exam areas and room queues after section events.
public interface DispatchService {

    // Assigns a candidate to the first applicable section queue (theory or layout).
    ServiceResult<ExamDispatchResult> passOn(Candidate candidate, int examId);

    // Assigns a candidate to a specific target section queue and exam area.
    ServiceResult<ExamDispatchResult> passOn(Candidate candidate, int examId, SectionType targetSection);

    // TBD: return candidate to examstaff procedure queue.
    ServiceResult<Void> passBack(Candidate candidate, int examId);

    // Promotes the next waiting candidate when a section completes in an exam area.
    ServiceResult<Integer> onSectionComplete(int examId, int sbd, SectionType sectionType, int examAreaId);
}
