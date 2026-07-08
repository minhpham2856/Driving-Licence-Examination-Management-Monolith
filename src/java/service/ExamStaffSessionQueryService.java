package service;

import dto.SessionDTO;

import java.util.List;

public interface ExamStaffSessionQueryService {

    List<SessionDTO> listAllSessions();

    SessionDTO findBySessionId(int sessionId);

    List<SessionDTO> listSessionsForExam(List<SessionDTO> allSessions, int examId);

    int resolvePrimarySessionId(List<SessionDTO> allSessions, int examId);
}
