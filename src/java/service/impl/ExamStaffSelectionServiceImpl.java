package service.impl;

import dto.SessionDTO;
import dto.examstaff.ExamStaffPageTransitionInput;
import dto.examstaff.ExamStaffPageTransitionStateDTO;
import dto.examstaff.ExamStaffSelectionResolveInput;
import dto.examstaff.ExamStaffSelectionStateDTO;
import service.ExamStaffPageService;
import service.ExamStaffSelectionService;
import util.examstaff.ExamStaffSessionRules;

import java.util.List;

public class ExamStaffSelectionServiceImpl implements ExamStaffSelectionService {

    private final ExamStaffPageService pageService;

    public ExamStaffSelectionServiceImpl() {
        this(new ExamStaffPageServiceImpl());
    }

    public ExamStaffSelectionServiceImpl(ExamStaffPageService pageService) {
        this.pageService = pageService;
    }

    @Override
    public int resolveExamId(ExamStaffSelectionResolveInput input) {
        if (input == null) {
            return 0;
        }
        Integer selectedExam = input.getSelectedExamId();
        if (selectedExam != null && selectedExam > 0) {
            return selectedExam;
        }

        int sessionId = input.getUrlSessionId();
        if (sessionId <= 0 && input.getSelectedSessionId() != null && input.getSelectedSessionId() > 0) {
            sessionId = input.getSelectedSessionId();
        }
        if (sessionId > 0) {
            SessionDTO current = pageService.findSessionById(sessionId, input.getAllSessions());
            if (current != null && current.getExamId() > 0) {
                return current.getExamId();
            }
        }

        String examIdParam = input.getExamIdParam();
        if (examIdParam != null && !examIdParam.isBlank()) {
            try {
                int parsed = Integer.parseInt(examIdParam.trim());
                if (parsed > 0) {
                    return parsed;
                }
            } catch (NumberFormatException ignored) {
            }
        }

        if (input.getDefaultExamId() > 0) {
            return input.getDefaultExamId();
        }

        List<SessionDTO> allSessions = input.getAllSessions();
        if (allSessions != null && !allSessions.isEmpty()) {
            return pageService.resolveDefaultExamId(allSessions);
        }
        return 0;
    }

    @Override
    public int resolveSessionId(ExamStaffSelectionResolveInput input) {
        if (input == null) {
            return 0;
        }
        if (input.getUrlSessionId() > 0) {
            return input.getUrlSessionId();
        }
        Integer selected = input.getSelectedSessionId();
        if (selected != null && selected > 0) {
            return selected;
        }
        if (input.getDefaultSessionId() > 0) {
            return input.getDefaultSessionId();
        }
        Integer examId = input.getSelectedExamId();
        if (examId != null && examId > 0 && input.getAllSessions() != null) {
            return pageService.resolvePrimarySessionId(input.getAllSessions(), examId);
        }
        return 0;
    }

    @Override
    public int ensureExamId(ExamStaffSelectionResolveInput input) {
        int examId = resolveExamId(input);
        if (examId > 0) {
            return examId;
        }
        List<SessionDTO> allSessions = input.getAllSessions();
        if (allSessions == null || allSessions.isEmpty()) {
            allSessions = pageService.listAllSessions();
            input.setAllSessions(allSessions);
        }
        return pageService.resolveDefaultExamId(allSessions);
    }

    @Override
    public int resolveExamFromSessionUrl(int urlSessionId, List<SessionDTO> allSessions) {
        if (urlSessionId <= 0) {
            return 0;
        }
        SessionDTO picked = pageService.findSessionById(urlSessionId, allSessions);
        if (picked == null || picked.getExamId() <= 0) {
            return 0;
        }
        return picked.getExamId();
    }

    @Override
    public ExamStaffSelectionStateDTO syncExamSelection(int examId, Integer currentSessionId,
            List<SessionDTO> allSessions) {
        ExamStaffSelectionStateDTO state = new ExamStaffSelectionStateDTO();
        state.setExamId(examId);
        if (examId <= 0) {
            return state;
        }

        int sessionId = currentSessionId != null ? currentSessionId : 0;
        if (sessionId <= 0) {
            sessionId = ExamStaffSessionRules.resolvePrimarySessionId(allSessions, examId);
        } else if (allSessions != null) {
            SessionDTO picked = ExamStaffSessionRules.findSessionById(allSessions, sessionId);
            if (picked == null || picked.getExamId() != examId) {
                sessionId = ExamStaffSessionRules.resolvePrimarySessionId(allSessions, examId);
            }
        }
        state.setSessionId(sessionId);
        return state;
    }

    @Override
    public ExamStaffPageTransitionStateDTO preparePageTransition(ExamStaffPageTransitionInput input) {
        ExamStaffPageTransitionStateDTO state = new ExamStaffPageTransitionStateDTO();
        if (input == null || input.getUrlSessionId() <= 0) {
            return state;
        }

        List<SessionDTO> allSessions = input.getAllSessions();
        SessionDTO urlSession = pageService.findSessionById(input.getUrlSessionId(), allSessions);
        if (urlSession == null || urlSession.getExamId() <= 0) {
            return state;
        }

        state.setExamId(urlSession.getExamId());
        state.setSessionId(input.getUrlSessionId());
        state.setPersistSelection(true);

        Integer loadedSessionId = input.getLoadedSessionId();
        if (loadedSessionId == null || loadedSessionId != input.getUrlSessionId()) {
            state.setClearCandidateCache(true);
        }

        Integer previousExamId = input.getPreviousExamId();
        if (previousExamId != null && previousExamId > 0 && !previousExamId.equals(urlSession.getExamId())) {
            state.setClearProcedureState(true);
        }

        return state;
    }

    @Override
    public int resolveActiveSessionId(int urlSessionId, Integer selectedSessionId,
            Integer runtimeActiveSessionId) {
        if (urlSessionId > 0) {
            return urlSessionId;
        }
        if (selectedSessionId != null && selectedSessionId > 0) {
            return selectedSessionId;
        }
        if (runtimeActiveSessionId != null && runtimeActiveSessionId > 0) {
            return runtimeActiveSessionId;
        }
        return 0;
    }
}
