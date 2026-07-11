package examstaff.dao.view;

import examstaff.model.view.ExamSessionSummary;

import java.util.List;

/** SELECT JOIN — ca thi / kỳ thi cho exam staff. */
public interface ExamSessionViewDAO {

    List<ExamSessionSummary> findAllOrdered();

    ExamSessionSummary findByExamId(int sessionId);
}
