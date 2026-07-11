package dao;

import dto.SessionDTO;

import model.Session;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

public interface ExamSessionDAO {

    // Lay dang ky theo id
    SessionDTO getById(int id);
    // Lay model dang ky theo id

    // Lay active sessions
    Session findById(int id);
    // Lay tat ca sessions

    // Lay tat ca sessions basic
    List<SessionDTO> getActiveSessions();
    // Lay exam day picker options

    // Lay sessions by exam date
    List<SessionDTO> getAllSessions();
    // Cap nhat status

    List<SessionDTO> getAllSessionsBasic();

    List<SessionDTO> getExamDayPickerOptions();

    List<SessionDTO> getSessionsByExamDate(Date examDate);

    boolean updateStatus(int sessionId, String status);

    /** Cập nhật trạng thái và ghi thời điểm kết thúc kỳ thi (EndTime). */
    boolean finishSession(int sessionId, String status, Timestamp endTime);
}
