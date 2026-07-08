package controller.staff.exam;

import controller.staff.exam.support.CallBoardHttpFacade;
import controller.staff.exam.support.ExamStaffHttpSupport;
import controller.staff.exam.support.ExamStaffPageBinder;
import dto.SessionDTO;
import dto.exam.ExamRegistrationDTO;
import dto.examstaff.CandidateQueueSnapshotDTO;
import dto.examstaff.ExamStaffPageContextDTO;
import dto.examstaff.ExamStaffPagePrepareInput;
import dto.examstaff.ExamStaffSelectionResolveInput;
import dto.examstaff.ExamStaffSelectionStateDTO;
import dto.examstaff.ExamStaffQueueRefreshInput;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.view.CallBoardState;
import service.CandidateCallingService;
import service.CandidateQueueService;
import service.ExamStaffPageService;
import service.ExamStaffSelectionService;
import service.ExamStaffServices;
import util.SessionUserHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Facade HTTP cho exam staff — delegate sang {@link ExamStaffServices} + {@link ExamStaffPageBinder}.
 * Giữ API static để servlet/filter hiện tại không phải đổi import hàng loạt.
 */
public final class ExamStaffViewHelper {

    private static ExamStaffPageService page() {
        return ExamStaffServices.get().page();
    }

    private static CandidateQueueService queue() {
        return ExamStaffServices.get().candidateQueue();
    }

    private static CandidateCallingService calling() {
        return ExamStaffServices.get().calling();
    }

    private static ExamStaffSelectionService selection() {
        return ExamStaffServices.get().selection();
    }

    private ExamStaffViewHelper() {
    }

    public static final class ExamStaffPageContext {
        private final int examId;
        private final int sessionId;
        private final List<SessionDTO> allSessions;
        private final List<ExamRegistrationDTO> candidates;

        public ExamStaffPageContext(int examId, int sessionId, List<SessionDTO> allSessions,
                List<ExamRegistrationDTO> candidates) {
            this.examId = examId;
            this.sessionId = sessionId;
            this.allSessions = allSessions != null ? allSessions : List.of();
            this.candidates = candidates != null ? candidates : List.of();
        }

        public int getExamId() {
            return examId;
        }

        public int getSessionId() {
            return sessionId;
        }

        public List<SessionDTO> getAllSessions() {
            return allSessions;
        }

        public List<ExamRegistrationDTO> getCandidates() {
            return candidates;
        }
    }

    public static ExamStaffPageContext prepareExamStaffPage(HttpServletRequest request, HttpSession session,
            String webRoot) {
        return prepareExamStaffPage(request, session, webRoot, true);
    }

    public static ExamStaffPageContext prepareExamStaffPage(HttpServletRequest request, HttpSession session,
            String webRoot, boolean loadCandidates) {
        applyUtf8Request(request);
        int urlSessionId = parseSessionIdParam(request);
        Integer previousExamId = session != null ? (Integer) session.getAttribute("selectedExamId") : null;
        Integer previousSessionId = session != null ? (Integer) session.getAttribute("selectedSessionId") : null;

        if (urlSessionId > 0 && session != null) {
            Integer loadedSession = (Integer) session.getAttribute("examStaffLoadedSessionId");
            if (loadedSession == null || loadedSession != urlSessionId) {
                ExamStaffPageBinder.clearCandidateCache(session);
            }
            SessionDTO urlSession = resolveSessionById(urlSessionId, loadAllSessions());
            if (urlSession != null && urlSession.getExamId() > 0
                    && previousExamId != null && previousExamId > 0
                    && !previousExamId.equals(urlSession.getExamId())) {
                clearProcedureStateOnExamChange(request, session, previousExamId, previousSessionId,
                        urlSession.getExamId(), urlSessionId);
            }
            applySessionIdFromRequest(request, session, loadAllSessions());
        }

        ExamStaffPagePrepareInput input = buildPagePrepareInput(request, session, webRoot, loadCandidates, urlSessionId);
        ExamStaffPageContextDTO ctx = page().preparePageContext(input);

        if (hasSessionIdParam(request) && ctx.getExamId() <= 0 && request != null) {
            request.setAttribute("sessionSelectError",
                    "Không tìm thấy kỳ thi (sessionId=" + urlSessionId + ").");
        }

        if (ctx.getExamId() > 0 && ctx.getSessionId() > 0 && session != null) {
            ExamStaffPageBinder.persistExamSelection(session, ctx.getSessionId(), ctx.getExamId());
        }

        if (ctx.getPickerView() != null) {
            ExamStaffPageBinder.bindPickerView(request, ctx.getPickerView());
        }

        CandidateQueueSnapshotDTO snapshot = queue().buildSnapshot(
                ctx.getCandidates(), ctx.getExamId(), ctx.getSessionId());
        ExamStaffPageBinder.publishQueue(request, session, snapshot);

        return new ExamStaffPageContext(ctx.getExamId(), ctx.getSessionId(),
                ctx.getAllSessions(), ctx.getCandidates());
    }

    public static void applyNoCacheHeaders(HttpServletResponse response) {
        ExamStaffHttpSupport.applyNoCacheHeaders(response);
    }

    public static int parseSessionIdParam(HttpServletRequest request) {
        return ExamStaffHttpSupport.parseSessionIdParam(request);
    }

    public static boolean hasSessionIdParam(HttpServletRequest request) {
        return parseSessionIdParam(request) > 0;
    }

    public static List<SessionDTO> loadAllSessions() {
        return page().listAllSessions();
    }

    public static void clearCandidateCache(HttpSession session) {
        ExamStaffPageBinder.clearCandidateCache(session);
    }

    public static int applySessionIdFromRequest(HttpServletRequest request, HttpSession session,
            List<SessionDTO> allSessions) {
        int sessionId = parseSessionIdParam(request);
        if (sessionId <= 0) {
            return resolveExamId(request, session, allSessions, 0);
        }
        int examId = selection().resolveExamFromSessionUrl(sessionId, allSessions);
        if (examId <= 0) {
            return 0;
        }
        ExamStaffPageBinder.persistExamSelection(session, sessionId, examId);
        return examId;
    }

    public static int resolveExamId(HttpServletRequest request, HttpSession session,
            List<SessionDTO> allSessions, int defaultId) {
        return selection().resolveExamId(buildSelectionInput(request, session, allSessions, defaultId, 0));
    }

    public static int ensureExamId(HttpServletRequest request, HttpSession session, List<SessionDTO> allSessions) {
        ExamStaffSelectionResolveInput input = buildSelectionInput(request, session, allSessions, 0, 0);
        int examId = selection().ensureExamId(input);
        if (examId > 0 && session != null) {
            int sessionId = page().resolvePrimarySessionId(input.getAllSessions(), examId);
            ExamStaffPageBinder.persistExamSelection(session, sessionId, examId);
        }
        return examId;
    }

    public static int resolveSessionId(HttpServletRequest request, HttpSession session,
            List<SessionDTO> allSessions, int defaultId) {
        return selection().resolveSessionId(
                buildSelectionInput(request, session, allSessions, 0, defaultId));
    }

    public static void syncExamSelection(HttpSession session, List<SessionDTO> allSessions, int examId) {
        if (session == null || examId <= 0) {
            return;
        }
        Integer currentSession = (Integer) session.getAttribute("selectedSessionId");
        ExamStaffSelectionStateDTO state = selection().syncExamSelection(examId, currentSession, allSessions);
        session.setAttribute("selectedExamId", state.getExamId());
        if (state.getSessionId() > 0) {
            session.setAttribute("selectedSessionId", state.getSessionId());
        }
    }

    public static List<ExamRegistrationDTO> refreshCandidateQueue(HttpSession session, int examId, String webRoot) {
        return refreshCandidateQueue(session, examId, webRoot, null);
    }

    public static List<ExamRegistrationDTO> refreshCandidateQueue(HttpSession session, int examId, String webRoot,
            List<SessionDTO> allSessions) {
        int sessionId = 0;
        if (session != null) {
            Integer picked = (Integer) session.getAttribute("selectedSessionId");
            if (picked != null && picked > 0) {
                sessionId = picked;
            }
        }
        return refreshCandidateQueue(session, examId, sessionId, webRoot, allSessions);
    }

    public static List<ExamRegistrationDTO> refreshCandidateQueue(HttpSession session, int examId, int sessionId,
            String webRoot, List<SessionDTO> allSessions) {
        if (session == null) {
            return new ArrayList<>();
        }
        ExamStaffQueueRefreshInput input = new ExamStaffQueueRefreshInput();
        input.setExamId(examId);
        input.setSessionId(sessionId);
        input.setWebRoot(webRoot);
        input.setAllSessions(allSessions);
        Integer selected = (Integer) session.getAttribute("selectedSessionId");
        input.setSelectedSessionId(selected);
        @SuppressWarnings("unchecked")
        List<String> order = (List<String>) session.getAttribute("callQueueOrder");
        input.setCallQueueOrder(order);
        input.setCallQueueOrderSessionId((Integer) session.getAttribute("callQueueOrderSessionId"));

        CandidateQueueSnapshotDTO snapshot = queue().refreshQueue(input);
        ExamStaffPageBinder.publishQueue(null, session, snapshot);
        return snapshot.getFullQueue();
    }

    public static void publishCandidateQueue(HttpServletRequest request, HttpSession session,
            List<ExamRegistrationDTO> qList, int examId, int sessionId) {
        CandidateQueueSnapshotDTO snapshot = queue().buildSnapshot(qList, examId, sessionId);
        SessionDTO current = resolveSessionById(sessionId, loadAllSessions());
        if (current == null && examId > 0) {
            current = representativeSessionForExam(loadAllSessions(), examId);
        }
        ExamStaffPageBinder.publishQueue(request, session, snapshot.getFullQueue(), snapshot.getActiveQueue(),
                snapshot.getProcedureDone(), examId, sessionId, current);
    }

    public static List<ExamRegistrationDTO> filterActiveCallQueue(List<ExamRegistrationDTO> queue) {
        return queue().filterPendingForCall(queue);
    }

    public static boolean isCallablePending(ExamRegistrationDTO c) {
        return queue().isCallablePending(c);
    }

    public static ExamRegistrationDTO findBySbd(List<ExamRegistrationDTO> queue, String sbd) {
        return queue().findBySbd(queue, sbd);
    }

    public static String findNextPendingSbd(List<ExamRegistrationDTO> queue, String afterSbd) {
        return queue().findNextPendingSbd(queue, afterSbd);
    }

    public static String resolveNextCallingSbd(List<ExamRegistrationDTO> fullQueue, String afterSbd) {
        return queue().resolveNextCallingSbd(fullQueue, afterSbd);
    }

    public static boolean moveCallableCandidateToFront(List<ExamRegistrationDTO> queue, String sbd) {
        return queue().moveCallableCandidateToFront(queue, sbd);
    }

    public static boolean moveCallableCandidateToBottom(List<ExamRegistrationDTO> queue, String sbd) {
        return queue().moveCallableCandidateToBottom(queue, sbd);
    }

    public static void syncCallQueueOrderFromQueue(HttpSession session, int sessionId,
            List<ExamRegistrationDTO> queue) {
        ExamStaffPageBinder.syncCallQueueOrder(session, sessionId, queue);
    }

    public static List<ExamRegistrationDTO> listSuspendedInSession(List<ExamRegistrationDTO> queue) {
        return queue().listSuspendedInSession(queue);
    }

    public static List<ExamRegistrationDTO> listProcedureDoneNewestFirst(List<ExamRegistrationDTO> queue) {
        return queue().listProcedureDoneNewestFirst(queue);
    }

    public static ExamRegistrationDTO resolveCandidateBySbd(HttpServletRequest request, HttpSession session,
            String sbd) {
        if (sbd == null || sbd.isBlank()) {
            return null;
        }
        String trimmed = sbd.trim();
        @SuppressWarnings("unchecked")
        List<ExamRegistrationDTO> cached = session != null
                ? (List<ExamRegistrationDTO>) session.getAttribute("candidateQueue") : null;
        ExamRegistrationDTO fromQueue = findBySbd(cached, trimmed);
        if (fromQueue != null) {
            return fromQueue;
        }
        int examId = resolveExamId(request, session, null, 0);
        int sessionId = resolveSessionId(request, session, null, 0);
        return queue().findByExamOrSession(examId, sessionId, trimmed);
    }

    public static ExamRegistrationDTO resolveCallingCandidate(HttpSession session, List<ExamRegistrationDTO> qList) {
        if (session == null) {
            return null;
        }
        String callingSbd = (String) session.getAttribute("callingSbd");
        ExamRegistrationDTO calling = calling().resolveCallingCandidate(callingSbd, qList);
        if (calling != null && callingSbd != null && !callingSbd.equals(calling.getSbd())) {
            session.setAttribute("callingSbd", calling.getSbd());
        } else if (calling == null && callingSbd != null) {
            session.removeAttribute("callingSbd");
        }
        return calling;
    }

    public static String syncCallingSbd(HttpSession session, ServletContext application,
            int sessionId, List<ExamRegistrationDTO> qList, boolean shiftEnded) {
        String sessionCalling = session != null ? (String) session.getAttribute("callingSbd") : null;
        CallBoardState callBoard = application != null
                ? CallBoardHttpFacade.getState(application, sessionId) : null;
        String callingSbd = calling().resolveSyncedCallingSbd(sessionCalling, callBoard, qList);
        if (session != null) {
            if (callingSbd != null && !callingSbd.isBlank()) {
                session.setAttribute("callingSbd", callingSbd);
            } else {
                session.removeAttribute("callingSbd");
            }
        }
        if (application != null) {
            CallBoardHttpFacade.sync(application, sessionId, callingSbd, qList, shiftEnded);
        }
        return callingSbd;
    }

    public static boolean isCallShiftEnded(HttpSession session) {
        return session != null && "true".equals(session.getAttribute("shiftEnded"));
    }

    public static void resumeCallShift(ServletContext application, HttpSession session, int sessionId) {
        if (session != null) {
            session.removeAttribute("shiftEnded");
        }
        if (application != null && sessionId > 0) {
            CallBoardHttpFacade.resumeShift(application, sessionId);
        }
    }

    public static void bindCandidateCallPageAttributes(HttpServletRequest request,
            HttpSession session, int examId, List<ExamRegistrationDTO> qList) {
        ExamRegistrationDTO calling = resolveCallingCandidate(session, qList);
        int sessionId = resolveSessionId(request, session, null, 0);
        SessionDTO current = resolveSessionById(sessionId, loadAllSessions());
        if (current == null && examId > 0) {
            current = representativeSessionForExam(loadAllSessions(), examId);
        }
        int suspendedCount = queue().listSuspendedInSession(qList).size();
        ExamStaffPageBinder.bindCandidateCallPage(request, examId, calling, sessionId, suspendedCount, current);
    }

    public static void bindProcedureFeeAttributes(HttpServletRequest request, ExamRegistrationDTO profile) {
        ExamStaffPageBinder.bindProcedureFees(
                request, ExamStaffServices.get().procedureFees().resolveProcedureFees(profile));
    }

    public static void bindImportExamAttributes(HttpServletRequest request, SessionDTO currentSession, int examId) {
        if (currentSession == null && examId > 0) {
            int primarySessionId = page().resolvePrimarySessionId(loadAllSessions(), examId);
            if (primarySessionId > 0) {
                currentSession = page().findSessionById(primarySessionId, loadAllSessions());
            }
        }
        ExamStaffPageBinder.bindImportExam(request, currentSession, examId);
    }

    public static void bindSidebarIfNeeded(HttpServletRequest request, HttpSession session) {
        if (request == null || request.getAttribute("examOptions") != null) {
            return;
        }
        List<SessionDTO> allSessions = loadAllSessions();
        int examId = resolveExamId(request, session, allSessions, 0);
        ExamStaffPageBinder.bindPickerView(request, page().buildPickerView(allSessions, examId, 0));
        if (session != null) {
            @SuppressWarnings("unchecked")
            List<SessionDTO> options = (List<SessionDTO>) request.getAttribute("examOptions");
            if (options != null) {
                session.setAttribute("examStaffExamOptions", options);
            }
        }
    }

    public static void clearProcedureStateOnExamChange(HttpServletRequest request, HttpSession session,
            int previousExamId, Integer previousSessionId, int newExamId, int newSessionId) {
        ExamStaffPageBinder.clearProcedureStateOnExamChange(session, newExamId, newSessionId);
    }

    public static void consumeFlash(HttpSession session, String sessionKey, HttpServletRequest request,
            String requestKey) {
        ExamStaffHttpSupport.consumeFlash(session, sessionKey, request, requestKey);
    }

    public static int resolveStaffId(HttpSession session) {
        return SessionUserHelper.resolveUserId(session);
    }

    public static List<SessionDTO> buildExamOptions(List<SessionDTO> allSessions) {
        return page().buildPickerView(allSessions, 0, 0).getExamOptions();
    }

    public static int resolvePickerOptionSessionId(List<SessionDTO> options, int examId) {
        if (options == null || examId <= 0) {
            return 0;
        }
        for (SessionDTO opt : options) {
            if (opt.getExamId() == examId) {
                return opt.getId();
            }
        }
        return 0;
    }

    public static List<SessionDTO> sortExamDaysForSidebar(List<SessionDTO> options) {
        return util.examstaff.ExamStaffSessionRules.sortExamDaysForSidebar(options);
    }

    public static SessionDTO firstPickerOption(List<SessionDTO> allSessions) {
        List<SessionDTO> options = sortExamDaysForSidebar(buildExamOptions(allSessions));
        return options.isEmpty() ? null : options.get(0);
    }

    public static int resolveDefaultExamId(List<SessionDTO> allSessions) {
        return page().resolveDefaultExamId(allSessions);
    }

    public static int resolveDefaultSessionId(List<SessionDTO> allSessions) {
        return page().resolveDefaultSessionId(allSessions);
    }

    public static SessionDTO findSessionById(List<SessionDTO> allSessions, int sessionId) {
        return page().findSessionById(sessionId, allSessions);
    }

    public static SessionDTO representativeSessionForExam(List<SessionDTO> allSessions, int examId) {
        return page().representativeSessionForExam(allSessions, examId);
    }

    public static List<SessionDTO> sessionsForExam(List<SessionDTO> allSessions, int examId) {
        return page().sessionsForExam(allSessions, examId);
    }

    public static int resolvePrimarySessionId(List<SessionDTO> allSessions, int examId) {
        return page().resolvePrimarySessionId(allSessions, examId);
    }

    public static SessionDTO resolveSessionFromRequest(HttpServletRequest request, HttpSession httpSession,
            List<SessionDTO> allSessions) {
        int sessionId = parseSessionIdParam(request);
        if (sessionId <= 0) {
            return null;
        }
        SessionDTO picked = resolveSessionById(sessionId, allSessions);
        if (picked != null && picked.getExamId() > 0) {
            ExamStaffPageBinder.persistExamSelection(httpSession, sessionId, picked.getExamId());
            return picked;
        }
        return null;
    }

    public static String resolveSafeRedirect(HttpServletRequest request, String fallbackPath) {
        return ExamStaffHttpSupport.resolveSafeRedirect(request, fallbackPath);
    }

    public static String stripQueryString(String url) {
        return ExamStaffHttpSupport.stripQueryString(url);
    }

    public static String upsertQueryParam(String url, String key, String value) {
        return ExamStaffHttpSupport.upsertQueryParam(url, key, value);
    }

    public static SessionDTO resolveSessionById(int sessionId, List<SessionDTO> allSessions) {
        return page().findSessionById(sessionId, allSessions);
    }

    private static ExamStaffSelectionResolveInput buildSelectionInput(HttpServletRequest request, HttpSession session,
            List<SessionDTO> allSessions, int defaultExamId, int defaultSessionId) {
        ExamStaffSelectionResolveInput input = new ExamStaffSelectionResolveInput();
        input.setUrlSessionId(parseSessionIdParam(request));
        input.setAllSessions(allSessions);
        input.setDefaultExamId(defaultExamId);
        input.setDefaultSessionId(defaultSessionId);
        if (request != null) {
            input.setExamIdParam(request.getParameter("examId"));
        }
        if (session != null) {
            input.setSelectedExamId((Integer) session.getAttribute("selectedExamId"));
            input.setSelectedSessionId((Integer) session.getAttribute("selectedSessionId"));
        }
        return input;
    }

    private static ExamStaffPagePrepareInput buildPagePrepareInput(HttpServletRequest request, HttpSession session,
            String webRoot, boolean loadCandidates, int urlSessionId) {
        ExamStaffPagePrepareInput input = new ExamStaffPagePrepareInput();
        input.setUrlSessionId(urlSessionId);
        input.setWebRoot(webRoot);
        input.setLoadCandidates(loadCandidates);
        input.setHasSessionIdParam(hasSessionIdParam(request));
        input.setAllSessions(loadAllSessions());
        if (request != null) {
            input.setExamIdParam(request.getParameter("examId"));
        }
        if (session != null) {
            input.setPreviousExamId((Integer) session.getAttribute("selectedExamId"));
            input.setPreviousSessionId((Integer) session.getAttribute("selectedSessionId"));
            input.setSelectedExamId((Integer) session.getAttribute("selectedExamId"));
            input.setSelectedSessionId((Integer) session.getAttribute("selectedSessionId"));
            input.setLoadedExamId((Integer) session.getAttribute("examStaffLoadedExamId"));
            input.setLoadedSessionId((Integer) session.getAttribute("examStaffLoadedSessionId"));
            @SuppressWarnings("unchecked")
            List<ExamRegistrationDTO> cached = (List<ExamRegistrationDTO>) session.getAttribute("candidateQueue");
            input.setCachedQueue(cached);
            @SuppressWarnings("unchecked")
            List<String> order = (List<String>) session.getAttribute("callQueueOrder");
            input.setCallQueueOrder(order);
            input.setCallQueueOrderSessionId((Integer) session.getAttribute("callQueueOrderSessionId"));
        }
        return input;
    }

    private static void applyUtf8Request(HttpServletRequest request) {
        if (request == null) {
            return;
        }
        try {
            request.setCharacterEncoding("UTF-8");
        } catch (Exception ignored) {
        }
    }
}
