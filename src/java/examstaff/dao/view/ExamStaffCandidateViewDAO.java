package examstaff.dao.view;

import examstaff.dto.view.ExamStaffCandidate;

import java.util.List;

/** View query thí sinh theo kỳ thi cho exam staff. */
public interface ExamStaffCandidateViewDAO {

    /**
     * Liệt kê thí sinh thuộc kỳ thi.
     *
     * @param examId mã kỳ thi
     * @return danh sách thí sinh view
     */
    List<ExamStaffCandidate> findByExamId(int examId);

    /**
     * Tìm thí sinh theo kỳ thi và số báo danh.
     *
     * @param examId mã kỳ thi
     * @param sbd    số báo danh
     * @return thí sinh hoặc null
     */
    ExamStaffCandidate findByExamIdAndSbd(int examId, String sbd);
}
