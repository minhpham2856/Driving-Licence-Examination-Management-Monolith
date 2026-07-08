package service.impl;

import dto.SessionDTO;
import dto.examstaff.SessionSelectRequestDTO;
import dto.examstaff.SessionSelectResultDTO;
import enums.ExamStaffMessage;
import service.ExamStaffPageService;
import service.ExamStaffSelectionService;
import service.ExamStaffSessionQueryService;
import service.SessionSelectService;

import java.util.List;

public class SessionSelectServiceImpl implements SessionSelectService {

    private final ExamStaffSessionQueryService sessionQuery = new ExamStaffSessionQueryServiceImpl();
    private final ExamStaffSelectionService selectionService = new ExamStaffSelectionServiceImpl();
    private final ExamStaffPageService pageService = new ExamStaffPageServiceImpl();

    @Override
    public SessionSelectResultDTO processSelection(SessionSelectRequestDTO request) {
        SessionSelectResultDTO result = new SessionSelectResultDTO();
        result.setPreviousExamId(request.getPreviousExamId());
        result.setPreviousSessionId(request.getPreviousSessionId());

        List<SessionDTO> allSessions = sessionQuery.listAllSessions();
        int urlSessionId = request.getUrlSessionId();
        int examId = selectionService.resolveExamFromSessionUrl(urlSessionId, allSessions);

        if (examId <= 0) {
            result.setSuccess(false);
            String param = urlSessionId > 0 ? String.valueOf(urlSessionId) : null;
            result.setErrorMessage(ExamStaffMessage.EXAM_NOT_FOUND_PREFIX.formatExamNotFound(param));
            return result;
        }

        int sessionId = urlSessionId > 0
                ? urlSessionId
                : pageService.resolvePrimarySessionId(allSessions, examId);

        result.setSuccess(true);
        result.setExamId(examId);
        result.setSessionId(sessionId);
        result.setNewExamId(examId);
        result.setNewSessionId(sessionId);

        Integer previousExamId = request.getPreviousExamId();
        Integer previousSessionId = request.getPreviousSessionId();
        if (previousExamId != null && previousExamId > 0 && !previousExamId.equals(examId)) {
            result.setClearProcedureOnExamChange(true);
        } else if (previousSessionId != null && sessionId > 0 && !previousSessionId.equals(sessionId)) {
            result.setClearCandidateCache(true);
        }
        return result;
    }
}
