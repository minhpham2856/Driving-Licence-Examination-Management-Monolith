package examstaff.dao;

import examstaff.dto.ExamSummaryRow;

import java.util.List;

/** SELECT JOIN - ca thi / kỳ thi cho exam staff. */
public interface ExamViewDAO {

    /**
     * Liệt kê mọi kỳ thi theo thứ tự (ngày / ca).
     *
     * @return danh sách hàng tóm tắt kỳ thi
     */
    List<ExamSummaryRow> findAllOrdered();

    /**
     * Tìm một kỳ thi theo mã.
     *
     * @param examId mã kỳ thi
     * @return hàng tóm tắt hoặc null
     */
    ExamSummaryRow findByExamId(int examId);
}
