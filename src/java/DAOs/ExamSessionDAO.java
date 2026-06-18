package DAOs;

import DTOs.SessionDTO;
import java.sql.Date;
import java.util.List;

public interface ExamSessionDAO {
    SessionDTO getById(int id);
    List<SessionDTO> getActiveSessions();
    List<SessionDTO> getAllSessions();
    List<SessionDTO> getSessionsByExamDate(Date examDate);
    boolean updateStatus(int sessionId, String status);
}
