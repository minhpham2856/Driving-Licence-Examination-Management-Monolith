package examstaff.dao;

import examstaff.dto.ExamSummaryDTO;

import java.util.List;

/**
 * View DAO đọc tóm tắt kỳ thi / ca thi cho exam staff.
 * Thực thi SELECT JOIN bảng {@code Exam} và {@code Licence} (không ghi dữ liệu).
 */
public interface ExamViewDAO {

    /**
     * Liệt kê mọi kỳ thi theo thứ tự ngày / ca.
     * Thực thi SELECT trên {@code Exam} JOIN {@code Licence} lấy mã kỳ thi,
     * ngày thi, trạng thái, hạng GPLX…; sắp xếp theo lịch thi.
     *
     * @return danh sách {@link ExamSummaryDTO} đã sắp xếp; rỗng nếu không có kỳ thi
     */
    List<ExamSummaryDTO> findAllOrdered();

    /**
     * Tìm một kỳ thi theo mã.
     * Thực thi SELECT trên {@code Exam} JOIN {@code Licence} với điều kiện {@code ExamId = ?}.
     *
     * @param examId mã kỳ thi ({@code Exam.ExamId}) cần tìm
     * @return {@link ExamSummaryDTO} nếu tìm thấy; {@code null} nếu không có
     */
    ExamSummaryDTO findByExamId(int examId);
}
