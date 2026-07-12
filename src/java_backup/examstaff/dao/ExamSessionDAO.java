package examstaff.dao;

import examstaff.dto.ExamSummaryDTO;

import examstaff.dto.Session;
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

    /** Cáº­p nháº­t tráº¡ng thÃ¡i vÃ  ghi thá»i Ä‘iá»ƒm káº¿t thÃºc ká»³ thi (EndTime). */
    boolean finishSession(int sessionId, String status, Timestamp endTime);
}

