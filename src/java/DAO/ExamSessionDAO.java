package DAO;

import Models.ExamSession;
import java.sql.Date;
import java.util.List;

public interface ExamSessionDAO {
    ExamSession getById(int id);
    List<ExamSession> getActiveSessions();
    List<ExamSession> getAllSessions();
    List<ExamSession> getSessionsByExamDate(Date examDate);
    boolean updateStatus(int sessionId, String status);

    /**
     * Mở ca thi: chuyển InProgress và ghi nhận StartTime/EndTime thực tế (giờ ca hiển thị cho thí sinh).
     */
    boolean openSession(int sessionId);
}
