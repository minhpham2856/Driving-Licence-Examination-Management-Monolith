package examstaff.dao.view;

import examstaff.dto.view.ExamStaffCandidate;

import java.util.List;

/**
 * View DAO — danh sách thí sinh cho màn exam staff / public call (SELECT JOIN).
 */
public interface ExamStaffCandidateViewDAO {

    /**
     * Lấy toàn bộ thí sinh của một kỳ thi.
     *
     * @param examId mã kỳ thi
     * @return danh sách view thí sinh
     */
    List<ExamStaffCandidate> findByExamId(int examId);

    /**
     * Tìm thí sinh theo kỳ thi và số báo danh.
     *
     * @param examId mã kỳ thi
     * @param sbd    số báo danh (chuỗi)
     * @return view thí sinh hoặc {@code null}
     */
    ExamStaffCandidate findByExamIdAndSbd(int examId, String sbd);
}
