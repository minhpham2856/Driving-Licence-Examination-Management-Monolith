package examstaff.dao;

import examstaff.dto.ExamStaffCandidate;

import java.util.List;

/**
 * View DAO đọc-only — thí sinh theo kỳ thi map về ExamStaffCandidate.
 *
 * Vai trò trong kiến trúc:
 * Read model nhẹ cho màn hình cần list/tra SBD nhanh mà không kéo theo toàn bộ
 * logic ghi của ExamRegistrationDAO. Dùng chung SQL Db2CandidateSql
 * với fallback CANDIDATE_SELECT_MINIMAL.
 * <pre>
 *   Servlet / service cần list thí sinh
 *            │  findByExamId / findByExamIdAndSbd
 *            ▼
 *      ExamStaffCandidateViewDAO  ◄── ExamStaffCandidateViewDAOImpl
 *            │
 *            ▼  Db2CandidateSql.CANDIDATE_SELECT + WHERE ex.ExamId = ?
 *      List<ExamStaffCandidate>
 * </pre>
 *
 * Khác ExamRegistrationDAO:
 * Không có method ghi; DTO ExamStaffCandidate gọn hơn ExamRegistrationDTO.
 * Cùng nguồn SQL nhưng tách interface để caller read-only không phụ thuộc DAO ghi.
 *
 * Triển khai mặc định:
 * examstaff.dao.impl.ExamStaffCandidateViewDAOImpl.
 */
public interface ExamStaffCandidateViewDAO {

    /**
     * Liệt kê thí sinh thuộc một kỳ thi.
     * Thực thi SELECT JOIN thí sinh / ghi danh theo ExamId,
     * trả danh sách view phục vụ màn hình exam staff.
     * @param examId mã kỳ thi cần liệt kê thí sinh
     * @return danh sách ExamStaffCandidate; rỗng nếu kỳ thi không có thí sinh
     */
    List<ExamStaffCandidate> findByExamId(int examId);

    /**
     * Tìm thí sinh theo kỳ thi và số báo danh.
     * Thực thi SELECT tương tự findByExamId(int) với thêm điều kiện SBD /
     * CandidateNumber.
     * @param examId mã kỳ thi
     * @param sbd    số báo danh cần tìm
     * @return ExamStaffCandidate nếu khớp; null nếu không tìm thấy
     */
    ExamStaffCandidate findByExamIdAndSbd(int examId, String sbd);
}
