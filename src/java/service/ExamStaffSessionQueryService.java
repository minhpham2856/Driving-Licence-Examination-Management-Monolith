package service;

import dto.SessionDTO;

import java.sql.Date;
import java.util.List;

public interface ExamStaffSessionQueryService {

    List<SessionDTO> listAllSessions();

    SessionDTO findBySessionId(int sessionId);

    List<SessionDTO> listSessionsByExamDate(Date examDate);

    List<SessionDTO> listSessionsForExam(List<SessionDTO> allSessions, int examId);

    int resolvePrimarySessionId(List<SessionDTO> allSessions, int examId);
}
