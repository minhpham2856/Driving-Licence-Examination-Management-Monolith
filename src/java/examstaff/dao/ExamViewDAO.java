package examstaff.dao;

import examstaff.dto.ExamSummaryDTO;

import java.util.List;

/**
 * View DAO đọc-only — tóm tắt kỳ thi / ca thi (ExamSummaryDTO).
 *
 * Vai trò trong kiến trúc:
 * Read model cho sidebar picker, danh sách kỳ thi và màn chọn ca. Chỉ SELECT —
 * mọi cập nhật Exam.Status đi qua ExamDAO.
 * <pre>
 *   Layout / ExamPicker / sidebar servlets
 *            │  findAllOrdered / findByExamId
 *            ▼
 *      ExamViewDAO  ◄── ExamViewDAOImpl
 *            │
 *            ▼  Db2ExamSummarySql.EXAM_SUMMARY_SELECT (+ ORDER BY / WHERE)
 *      List<ExamSummaryDTO>
 * </pre>
 *
 * Chia tách với ExamDAO:
 * Cùng hằng SQL Db2ExamSummarySql để cột map DTO không lệch;
 * ExamViewDAO thiên <b>list / view</b>, ExamDAO thiên <b>getById + UPDATE</b>.
 *
 * Triển khai mặc định:
 * examstaff.dao.impl.ExamViewDAOImpl.
 */
public interface ExamViewDAO {

    /**
     * Liệt kê mọi kỳ thi theo thứ tự ngày / ca.
     * Thực thi SELECT trên Exam JOIN Licence lấy mã kỳ thi,
     * ngày thi, trạng thái, hạng GPLX…; sắp xếp theo lịch thi.
     * @return danh sách ExamSummaryDTO đã sắp xếp; rỗng nếu không có kỳ thi
     */
    List<ExamSummaryDTO> findAllOrdered();

    /**
     * Tìm một kỳ thi theo mã.
     * Thực thi SELECT trên Exam JOIN Licence với điều kiện ExamId = ?.
     * @param examId mã kỳ thi (Exam.ExamId) cần tìm
     * @return ExamSummaryDTO nếu tìm thấy; null nếu không có
     */
    ExamSummaryDTO findByExamId(int examId);
}
