package examstaff.dao;

import examstaff.dto.ExamSummaryDTO;

import examstaff.model.Session;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

public interface ExamSessionDAO {

    // Lay dang ky theo id
    ExamSummaryDTO getById(int id);
    // Lay model dang ky theo id

    // Lay active sessions
    Session findById(int id);
    // Lay tat ca sessions

    // Lay tat ca sessions basic
    List<ExamSummaryDTO> getActiveSessions();
    // Lay exam day picker options

    // Lay sessions by exam date
    List<ExamSummaryDTO> getAllSessions();
    // Cap nhat status

    List<ExamSummaryDTO> getAllSessionsBasic();

    List<ExamSummaryDTO> getExamDayPickerOptions();

    List<ExamSummaryDTO> getSessionsByExamDate(Date examDate);

    boolean updateStatus(int sessionId, String status);

    /** Cập nhật trạng thái và ghi thời điểm kết thúc kỳ thi (EndTime). */
    boolean finishSession(int sessionId, String status, Timestamp endTime);
}
