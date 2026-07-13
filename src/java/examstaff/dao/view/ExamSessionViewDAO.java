package examstaff.dao.view;

import examstaff.dto.view.ExamSessionSummary;

import java.util.List;

/** SELECT JOIN â€” ca thi / ká»³ thi cho exam staff. */
public interface ExamSessionViewDAO {

    List<ExamSessionSummary> findAllOrdered();

    ExamSessionSummary findByExamId(int sessionId);
}

