package DAO;

import Models.ExamSession;
import java.util.List;

public interface ExamSessionDAO {
    ExamSession getById(int id);
    List<ExamSession> getActiveSessions();
    List<ExamSession> getAllSessions();
    boolean updateStatus(int sessionId, String status);
}
