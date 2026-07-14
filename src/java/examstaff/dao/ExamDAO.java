package examstaff.dao;

import examstaff.dto.ExamSummaryDTO;
import java.sql.Timestamp;

/**
 * DAO kỳ thi ({@code Exam}) — đọc tóm tắt và cập nhật trạng thái.
 */
public interface ExamDAO {

    /**
     * Lấy tóm tắt kỳ thi theo mã.
     *
     * @param id mã kỳ thi
     * @return DTO hoặc {@code null}
     */
    ExamSummaryDTO getById(int id);

    /**
     * Cập nhật trạng thái kỳ thi.
     *
     * @param examId mã kỳ thi
     * @param status trạng thái mới
     * @return {@code true} nếu cập nhật thành công
     */
    boolean updateStatus(int examId, String status);

    /**
     * Kết thúc kỳ thi: cập nhật trạng thái và {@code EndTime}.
     *
     * @param examId  mã kỳ thi
     * @param status  trạng thái kết thúc
     * @param endTime thời điểm kết thúc (null → thời điểm hiện tại)
     * @return {@code true} nếu cập nhật thành công
     */
    boolean finishExam(int examId, String status, Timestamp endTime);
}
