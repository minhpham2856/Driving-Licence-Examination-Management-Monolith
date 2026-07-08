package service;

import dto.SessionDTO;
import dto.examstaff.ExamStaffPageContextDTO;
import dto.examstaff.ExamStaffPagePrepareInput;
import dto.examstaff.ExamStaffPickerViewDTO;

import java.util.List;

public interface ExamStaffPageService {

    List<SessionDTO> listAllSessions();

    SessionDTO findSessionById(int sessionId, List<SessionDTO> allSessions);

    SessionDTO representativeSessionForExam(List<SessionDTO> allSessions, int examId);

    List<SessionDTO> sessionsForExam(List<SessionDTO> allSessions, int examId);

    int resolvePrimarySessionId(List<SessionDTO> allSessions, int examId);

    int resolveDefaultExamId(List<SessionDTO> allSessions);

    int resolveDefaultSessionId(List<SessionDTO> allSessions);

    ExamStaffPickerViewDTO buildPickerView(List<SessionDTO> allSessions, int examId, int urlSessionId);

    ExamStaffPageContextDTO preparePageContext(ExamStaffPagePrepareInput input);
}
