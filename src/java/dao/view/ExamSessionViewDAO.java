package dao.view;

import model.view.ExamSessionSummary;

import java.sql.Date;
import java.util.List;

/** SELECT JOIN — ca thi / kỳ thi cho exam staff. */
public interface ExamSessionViewDAO {

    List<ExamSessionSummary> findAllOrdered();

    List<ExamSessionSummary> findAllBasicOrdered();

    ExamSessionSummary findBySessionId(int sessionId);

    List<ExamSessionSummary> findByExamDate(Date examDate);
}
