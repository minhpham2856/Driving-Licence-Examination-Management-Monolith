package service;

import dto.SessionDTO;
import dto.examstaff.ExamStaffSelectionDTO;
import dto.examstaff.ExamStaffSelectionStateDTO;

import java.util.List;

public interface ExamStaffSelectionService {

    int resolveExamId(ExamStaffSelectionDTO input);

    int resolveSessionId(ExamStaffSelectionDTO input);

    int ensureExamId(ExamStaffSelectionDTO input);

    int resolveExamFromSessionUrl(int urlSessionId, List<SessionDTO> allSessions);

    ExamStaffSelectionStateDTO syncExamSelection(int examId, Integer currentSessionId, List<SessionDTO> allSessions);
}
