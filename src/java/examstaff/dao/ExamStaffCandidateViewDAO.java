package examstaff.dao;

import examstaff.dto.ExamStaffCandidate;

import java.util.List;

/**
 * View DAO thí sinh theo kỳ thi cho exam staff.
 * Thực thi SELECT VIEW / JOIN đọc-only (Candidate, ExamEnrollment, trạng thái section…)
 * map về {@link ExamStaffCandidate}.
 */
public interface ExamStaffCandidateViewDAO {

    /**
     * Liệt kê thí sinh thuộc một kỳ thi.
     * Thực thi SELECT JOIN thí sinh / ghi danh theo {@code ExamId},
     * trả danh sách view phục vụ màn hình exam staff.
     *
     * @param examId mã kỳ thi cần liệt kê thí sinh
     * @return danh sách {@link ExamStaffCandidate}; rỗng nếu kỳ thi không có thí sinh
     */
    List<ExamStaffCandidate> findByExamId(int examId);

    /**
     * Tìm thí sinh theo kỳ thi và số báo danh.
     * Thực thi SELECT tương tự {@link #findByExamId(int)} với thêm điều kiện SBD /
     * {@code CandidateNumber}.
     *
     * @param examId mã kỳ thi
     * @param sbd    số báo danh cần tìm
     * @return {@link ExamStaffCandidate} nếu khớp; {@code null} nếu không tìm thấy
     */
    ExamStaffCandidate findByExamIdAndSbd(int examId, String sbd);
}
