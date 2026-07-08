package dao.view;

import model.view.ExamStaffCandidate;

import java.util.List;

/** SELECT JOIN — danh sách thí sinh cho màn exam staff / public call. */
public interface ExamStaffCandidateViewDAO {

    List<ExamStaffCandidate> findBySessionId(int sessionId);

    List<ExamStaffCandidate> findByExamId(int examId);

    ExamStaffCandidate findByExamIdAndSbd(int examId, String sbd);
}
