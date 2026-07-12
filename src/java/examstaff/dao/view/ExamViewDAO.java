package examstaff.dao.view;

import examstaff.model.view.ExamSummaryRow;

import java.util.List;

/** SELECT JOIN — ca thi / kỳ thi cho exam staff. */
public interface ExamViewDAO {

    List<ExamSummaryRow> findAllOrdered();

    ExamSummaryRow findByExamId(int examId);
}
