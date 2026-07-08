package service;

import dto.SessionDTO;
import dto.examstaff.ExamStaffSelectionResolveInput;
import dto.examstaff.ExamStaffSelectionStateDTO;

import java.util.List;

public interface ExamStaffSelectionService {

    int resolveExamId(ExamStaffSelectionResolveInput input);

    int resolveSessionId(ExamStaffSelectionResolveInput input);

    int ensureExamId(ExamStaffSelectionResolveInput input);

    int resolveExamFromSessionUrl(int urlSessionId, List<SessionDTO> allSessions);

    ExamStaffSelectionStateDTO syncExamSelection(int examId, Integer currentSessionId, List<SessionDTO> allSessions);
}
