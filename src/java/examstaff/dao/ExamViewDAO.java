package examstaff.dao;

import examstaff.dto.ExamSummaryDTO;

import java.util.List;

/**
 * View DAO đọc-only — tóm tắt kỳ thi / ca thi ({@link ExamSummaryDTO}).
 *
 * Vai trò trong kiến trúc:
 * Read model cho sidebar picker, danh sách kỳ thi và màn chọn ca. Chỉ SELECT —
 * mọi cập nhật {@code Exam.Status} đi qua {@link ExamDAO}.
 * <pre>
 *   Layout / ExamPicker / sidebar servlets
 *            │  findAllOrdered / findByExamId
 *            ▼
 *      ExamViewDAO  ◄── ExamViewDAOImpl
 *            │
 *            ▼  Db2ExamSummarySql.EXAM_SUMMARY_SELECT (+ ORDER BY / WHERE)
 *      List&lt;ExamSummaryDTO&gt;
 * </pre>
 *
 * Chia tách với ExamDAO:
 * Cùng hằng SQL {@link Db2ExamSummarySql} để cột map DTO không lệch;
 * {@code ExamViewDAO} thiên <b>list / view</b>, {@code ExamDAO} thiên <b>getById + UPDATE</b>.
 *
 * Triển khai mặc định:
 * {@link examstaff.dao.impl.ExamViewDAOImpl}.
 */
public interface ExamViewDAO {

    /**
     * Liệt kê mọi kỳ thi theo thứ tự ngày / ca.
     * Thực thi SELECT trên {@code Exam} JOIN {@code Licence} lấy mã kỳ thi,
     * ngày thi, trạng thái, hạng GPLX…; sắp xếp theo lịch thi.
     * @return danh sách {@link ExamSummaryDTO} đã sắp xếp; rỗng nếu không có kỳ thi
     */
    List<ExamSummaryDTO> findAllOrdered();

    /**
     * Tìm một kỳ thi theo mã.
     * Thực thi SELECT trên {@code Exam} JOIN {@code Licence} với điều kiện {@code ExamId = ?}.
     * @param examId mã kỳ thi ({@code Exam.ExamId}) cần tìm
     * @return {@link ExamSummaryDTO} nếu tìm thấy; {@code null} nếu không có
     */
    ExamSummaryDTO findByExamId(int examId);
}
