package examstaff.dao;

import examstaff.dto.ExamSummaryDTO;
import java.sql.Timestamp;

/**
 * Cổng truy cập <b>ghi</b> và đọc một kỳ thi (Exam) — tóm tắt + lifecycle status.
 *
 * Vai trò trong kiến trúc:
 * Khác ExamViewDAO (read model list/picker), interface này phục vụ
 * cập nhật trạng thái kỳ thi và kết thúc ca thi. Đọc tóm tắt dùng chung SQL
 * Db2ExamSummarySql.EXAM_SUMMARY_SELECT với ExamViewDAOImpl.
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
 * updateStatus / finishExam ghi Exam.Status và EndTime trên DB.
 * Trạng thái <b>pause gọi số</b> runtime nằm trên CallBoardState — không qua DAO này.
 *
 * Triển khai mặc định:
 * examstaff.dao.impl.ExamDAOImpl.
 */
public interface ExamDAO {

    /**
     * Lấy tóm tắt kỳ thi theo mã.
     * Thực thi SELECT trên Exam JOIN Licence với WHERE ExamId = ?
     * (ngày thi, trạng thái, hạng GPLX, thời gian bắt đầu/kết thúc…).
     * @param id mã kỳ thi (Exam.ExamId)
     * @return ExamSummaryDTO nếu tìm thấy; null nếu không có
     */
    ExamSummaryDTO getById(int id);

    /**
     * Cập nhật trạng thái kỳ thi.
     * Thực thi UPDATE Exam SET [Status] = ? WHERE ExamId = ?.
     * @param examId mã kỳ thi cần cập nhật
     * @param status trạng thái mới (ví dụ: Ongoing, Finished…)
     * @return true nếu cập nhật thành công (có hàng bị ảnh hưởng); false nếu thất bại
     */
    boolean updateStatus(int examId, String status);

    /**
     * Kết thúc kỳ thi: ghi trạng thái kết thúc và thời điểm EndTime.
     * Thực thi UPDATE Exam SET [Status] = ?, EndTime = ? WHERE ExamId = ?.
     * @param examId  mã kỳ thi cần kết thúc
     * @param status  trạng thái kết thúc (ví dụ: Finished)
     * @param endTime thời điểm kết thúc; null → dùng thời điểm hiện tại
     * @return true nếu cập nhật thành công; false nếu thất bại
     */
    boolean finishExam(int examId, String status, Timestamp endTime);

    /**
     * Ghi mật khẩu máy thi (OTP kiosk) cho kỳ thi.
     * @param examId   mã kỳ thi
     * @param password mật khẩu plain text (OTP 6 số)
     * @return true nếu cập nhật thành công
     */
    boolean updateExamPassword(int examId, String password);
}
