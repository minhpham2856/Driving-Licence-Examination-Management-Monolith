package examstaff.dao;

import examstaff.dto.ExamSummaryDTO;
import java.sql.Timestamp;

/**
 * Cổng truy cập <b>ghi</b> và đọc một kỳ thi ({@code Exam}) — tóm tắt + lifecycle status.
 *
 * Vai trò trong kiến trúc:
 * Khác {@link ExamViewDAO} (read model list/picker), interface này phục vụ
 * cập nhật trạng thái kỳ thi và kết thúc ca thi. Đọc tóm tắt dùng chung SQL
 * {@link Db2ExamSummarySql#EXAM_SUMMARY_SELECT} với {@code ExamViewDAOImpl}.
 * <pre>
 *   ExamControlServlet / AllocationServlet
 *            │  updateStatus / finishExam
 *            ▼
 *      ExamDAO  ◄── ExamDAOImpl
 *            │
 *            ▼  UPDATE Exam SET Status, EndTime
 *         DLEM_DB_2.Exam
 * </pre>
 *
 * Status DB vs Call Board:
 * {@link #updateStatus} / {@link #finishExam} ghi {@code Exam.Status} và {@code EndTime} trên DB.
 * Trạng thái <b>pause gọi số</b> runtime nằm trên {@code CallBoardState} — không qua DAO này.
 *
 * Triển khai mặc định:
 * {@link examstaff.dao.impl.ExamDAOImpl}.
 */
public interface ExamDAO {

    /**
     * Lấy tóm tắt kỳ thi theo mã.
     * Thực thi SELECT trên {@code Exam} JOIN {@code Licence} với {@code WHERE ExamId = ?}
     * (ngày thi, trạng thái, hạng GPLX, thời gian bắt đầu/kết thúc…).
     * @param id mã kỳ thi ({@code Exam.ExamId})
     * @return {@link ExamSummaryDTO} nếu tìm thấy; {@code null} nếu không có
     */
    ExamSummaryDTO getById(int id);

    /**
     * Cập nhật trạng thái kỳ thi.
     * Thực thi {@code UPDATE Exam SET [Status] = ? WHERE ExamId = ?}.
     * @param examId mã kỳ thi cần cập nhật
     * @param status trạng thái mới (ví dụ: Ongoing, Finished…)
     * @return {@code true} nếu cập nhật thành công (có hàng bị ảnh hưởng); {@code false} nếu thất bại
     */
    boolean updateStatus(int examId, String status);

    /**
     * Kết thúc kỳ thi: ghi trạng thái kết thúc và thời điểm {@code EndTime}.
     * Thực thi {@code UPDATE Exam SET [Status] = ?, EndTime = ? WHERE ExamId = ?}.
     * @param examId  mã kỳ thi cần kết thúc
     * @param status  trạng thái kết thúc (ví dụ: Finished)
     * @param endTime thời điểm kết thúc; {@code null} → dùng thời điểm hiện tại
     * @return {@code true} nếu cập nhật thành công; {@code false} nếu thất bại
     */
    boolean finishExam(int examId, String status, Timestamp endTime);
}
