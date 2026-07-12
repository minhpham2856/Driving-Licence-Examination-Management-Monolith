package examstaff.dao.view;

import examstaff.dto.view.ExamStaffCandidate;

import java.util.List;

/** SELECT JOIN â€” danh sÃ¡ch thÃ­ sinh cho mÃ n exam staff / public call. */
public interface ExamStaffCandidateViewDAO {

    List<ExamStaffCandidate> findByExamId(int examId);

    ExamStaffCandidate findByExamIdAndSbd(int examId, String sbd);
}

