package examstaff.dao;

import examstaff.dto.ExamSummaryDTO;
import java.sql.Timestamp;

/**
 * DAO kỳ thi ({@code Exam}) — đọc tóm tắt và cập nhật trạng thái / thời gian kết thúc.
 * SELECT JOIN {@code Exam}/{@code Licence}; UPDATE trên bảng {@code Exam}.
 */
public interface ExamDAO {

    /**
     * Lấy tóm tắt kỳ thi theo mã.
     * Thực thi SELECT trên {@code Exam} JOIN {@code Licence} với {@code WHERE ExamId = ?}
     * (ngày thi, trạng thái, hạng GPLX, thời gian bắt đầu/kết thúc…).
     *
     * @param id mã kỳ thi ({@code Exam.ExamId})
     * @return {@link ExamSummaryDTO} nếu tìm thấy; {@code null} nếu không có
     */
    ExamSummaryDTO getById(int id);

    /**
     * Cập nhật trạng thái kỳ thi.
     * Thực thi {@code UPDATE Exam SET [Status] = ? WHERE ExamId = ?}.
     *
     * @param examId mã kỳ thi cần cập nhật
     * @param status trạng thái mới (ví dụ: Ongoing, Finished…)
     * @return {@code true} nếu cập nhật thành công (có hàng bị ảnh hưởng); {@code false} nếu thất bại
     */
    boolean updateStatus(int examId, String status);

    /**
     * Kết thúc kỳ thi: ghi trạng thái kết thúc và thời điểm {@code EndTime}.
     * Thực thi {@code UPDATE Exam SET [Status] = ?, EndTime = ? WHERE ExamId = ?}.
     *
     * @param examId  mã kỳ thi cần kết thúc
     * @param status  trạng thái kết thúc (ví dụ: Finished)
     * @param endTime thời điểm kết thúc; {@code null} → dùng thời điểm hiện tại
     * @return {@code true} nếu cập nhật thành công; {@code false} nếu thất bại
     */
    boolean finishExam(int examId, String status, Timestamp endTime);
}
