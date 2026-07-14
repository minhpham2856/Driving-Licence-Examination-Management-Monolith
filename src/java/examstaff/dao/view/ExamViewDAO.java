package examstaff.dao.view;

import examstaff.dto.view.ExamSummaryRow;

import java.util.List;

/**
 * View DAO — ca thi / kỳ thi cho exam staff (SELECT JOIN).
 */
public interface ExamViewDAO {

    /**
     * Lấy tất cả kỳ thi, sắp xếp theo ngày/giờ giảm dần.
     *
     * @return danh sách tóm tắt kỳ thi
     */
    List<ExamSummaryRow> findAllOrdered();

    /**
     * Lấy một kỳ thi theo mã.
     *
     * @param examId mã kỳ thi
     * @return hàng tóm tắt hoặc {@code null}
     */
    ExamSummaryRow findByExamId(int examId);
}
