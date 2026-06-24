package DAO;

import Models.ExamSession;
import java.sql.Date;
import java.util.List;


public interface ExamSessionDAO {
    ExamSession getById(int id);
    List<ExamSession> getActiveSessions();
    List<ExamSession> getAllSessions();
    List<ExamSession> getSessionsByExamDate(Date examDate);
    List<ExamSession> getSessionsByExamId(int examId);
    boolean updateStatus(int sessionId, String status);
}
