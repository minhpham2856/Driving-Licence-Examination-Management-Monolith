package controller.staff.exam;

import dto.SessionDTO;
import dto.exam.ExamRegistrationDTO;
import dto.examstaff.CandidateQueueSnapshotDTO;
import dto.examstaff.ExamStaffPageContextDTO;
import dto.examstaff.ExamStaffPagePrepareInput;
import dto.examstaff.ExamStaffSelectionDTO;
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
import util.SessionUserUtil;
import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.http.HttpServlet;
import dto.examstaff.AllocationActionResultDTO;
import dto.examstaff.AllocationStageViewDTO;
import repository.CallBoardRepository;
import repository.ServletContextCallBoardRepository;
import service.CallBoardSyncService;
import util.examstaff.CallQueueRules;
import dto.examstaff.CandidateDossierViewDTO;
import dto.examstaff.ExaminerAllocationViewDTO;
import dto.examstaff.ExamStaffDashboardViewDTO;
import dto.examstaff.ExamStaffPickerViewDTO;
import dto.examstaff.ProcedureFeeResultDTO;
import dto.examstaff.ExamReportProcedureStatusDTO;
import dto.examstaff.ExamReportStatsDTO;
import service.StaffAuditLogService;
import service.impl.StaffAuditLogServiceImpl;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import dto.examstaff.StaffAuditPageViewDTO;





/**
 * Base Servlet cho exam staff — delegate sang {@link ExamStaffServices} + {@link ExamStaffPageBinder}.
 */
public abstract class BaseExamStaffServlet extends HttpServlet {

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

    protected BaseExamStaffServlet() {
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
                BaseExamStaffServlet.clearCandidateCache(session);
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
            BaseExamStaffServlet.persistExamSelection(session, ctx.getSessionId(), ctx.getExamId());
        }

        if (ctx.getPickerView() != null) {
            BaseExamStaffServlet.bindPickerView(request, ctx.getPickerView());
        }

        CandidateQueueSnapshotDTO snapshot = queue().buildSnapshot(
                ctx.getCandidates(), ctx.getExamId(), ctx.getSessionId());
        BaseExamStaffServlet.publishQueue(request, session, snapshot);

        return new ExamStaffPageContext(ctx.getExamId(), ctx.getSessionId(),
                ctx.getAllSessions(), ctx.getCandidates());
    }

    public static void applyNoCacheHeaders(HttpServletResponse response) {
        BaseExamStaffServlet.applyNoCacheHeaders(response);
    }

    public static int parseSessionIdParam(HttpServletRequest request) {
        return BaseExamStaffServlet.parseSessionIdParam(request);
    }

    public static boolean hasSessionIdParam(HttpServletRequest request) {
        return parseSessionIdParam(request) > 0;
    }

    public static List<SessionDTO> loadAllSessions() {
        return page().listAllSessions();
    }

    public static void clearCandidateCache(HttpSession session) {
        BaseExamStaffServlet.clearCandidateCache(session);
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
        BaseExamStaffServlet.persistExamSelection(session, sessionId, examId);
        return examId;
    }

    public static int resolveExamId(HttpServletRequest request, HttpSession session,
            List<SessionDTO> allSessions, int defaultId) {
        return selection().resolveExamId(buildSelectionInput(request, session, allSessions, defaultId, 0));
    }

    public static int ensureExamId(HttpServletRequest request, HttpSession session, List<SessionDTO> allSessions) {
        ExamStaffSelectionDTO input = buildSelectionInput(request, session, allSessions, 0, 0);
        int examId = selection().ensureExamId(input);
        if (examId > 0 && session != null) {
            int sessionId = page().resolvePrimarySessionId(input.getAllSessions(), examId);
            BaseExamStaffServlet.persistExamSelection(session, sessionId, examId);
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
        BaseExamStaffServlet.publishQueue(null, session, snapshot);
        return snapshot.getFullQueue();
    }

    public static void publishCandidateQueue(HttpServletRequest request, HttpSession session,
            List<ExamRegistrationDTO> qList, int examId, int sessionId) {
        CandidateQueueSnapshotDTO snapshot = queue().buildSnapshot(qList, examId, sessionId);
        SessionDTO current = resolveSessionById(sessionId, loadAllSessions());
        if (current == null && examId > 0) {
            current = representativeSessionForExam(loadAllSessions(), examId);
        }
        BaseExamStaffServlet.publishQueue(request, session, snapshot.getFullQueue(), snapshot.getActiveQueue(),
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
        BaseExamStaffServlet.syncCallQueueOrder(session, sessionId, queue);
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
                ? BaseExamStaffServlet.getState(application, sessionId) : null;
        String callingSbd = calling().resolveSyncedCallingSbd(sessionCalling, callBoard, qList);
        if (session != null) {
            if (callingSbd != null && !callingSbd.isBlank()) {
                session.setAttribute("callingSbd", callingSbd);
            } else {
                session.removeAttribute("callingSbd");
            }
        }
        if (application != null) {
            BaseExamStaffServlet.sync(application, sessionId, callingSbd, qList, shiftEnded);
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
            BaseExamStaffServlet.resumeShift(application, sessionId);
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
        BaseExamStaffServlet.bindCandidateCallPage(request, examId, calling, sessionId, suspendedCount, current);
    }

    public static void bindProcedureFeeAttributes(HttpServletRequest request, ExamRegistrationDTO profile) {
        BaseExamStaffServlet.bindProcedureFees(
                request, ExamStaffServices.get().procedureFees().resolveProcedureFees(profile));
    }

    public static void bindImportExamAttributes(HttpServletRequest request, SessionDTO currentSession, int examId) {
        if (currentSession == null && examId > 0) {
            int primarySessionId = page().resolvePrimarySessionId(loadAllSessions(), examId);
            if (primarySessionId > 0) {
                currentSession = page().findSessionById(primarySessionId, loadAllSessions());
            }
        }
        BaseExamStaffServlet.bindImportExam(request, currentSession, examId);
    }

    public static void bindSidebarIfNeeded(HttpServletRequest request, HttpSession session) {
        if (request == null || request.getAttribute("examOptions") != null) {
            return;
        }
        List<SessionDTO> allSessions = loadAllSessions();
        int examId = resolveExamId(request, session, allSessions, 0);
        BaseExamStaffServlet.bindPickerView(request, page().buildPickerView(allSessions, examId, 0));
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
        BaseExamStaffServlet.clearProcedureStateOnExamChange(session, newExamId, newSessionId);
    }

    public static void consumeFlash(HttpSession session, String sessionKey, HttpServletRequest request,
            String requestKey) {
        BaseExamStaffServlet.consumeFlash(session, sessionKey, request, requestKey);
    }

    public static int resolveStaffId(HttpSession session) {
        return SessionUserUtil.resolveUserId(session);
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
            BaseExamStaffServlet.persistExamSelection(httpSession, sessionId, picked.getExamId());
            return picked;
        }
        return null;
    }

    public static String resolveSafeRedirect(HttpServletRequest request, String fallbackPath) {
        return BaseExamStaffServlet.resolveSafeRedirect(request, fallbackPath);
    }

    public static String stripQueryString(String url) {
        return BaseExamStaffServlet.stripQueryString(url);
    }

    public static String upsertQueryParam(String url, String key, String value) {
        return BaseExamStaffServlet.upsertQueryParam(url, key, value);
    }

    public static SessionDTO resolveSessionById(int sessionId, List<SessionDTO> allSessions) {
        return page().findSessionById(sessionId, allSessions);
    }

    private static ExamStaffSelectionDTO buildSelectionInput(HttpServletRequest request, HttpSession session,
            List<SessionDTO> allSessions, int defaultExamId, int defaultSessionId) {
        ExamStaffSelectionDTO input = new ExamStaffSelectionDTO();
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

    // --- From BaseExamStaffServlet.java ---


    

    public static void apply(HttpServletRequest request, HttpSession session, AllocationActionResultDTO result) {
        if (request == null || result == null) {
            return;
        }
        if (result.getErrorMsg() != null) {
            request.setAttribute("errorMsg", result.getErrorMsg());
        }
        if (result.getWarningMsg() != null) {
            request.setAttribute("warningMsg", result.getWarningMsg());
        }
        if (result.getAlertMsg() != null) {
            request.setAttribute("alertMsg", result.getAlertMsg());
        }
        if (session != null && result.isSyncCallBoard() && result.getCallingSbd() != null) {
            session.setAttribute("callingSbd", result.getCallingSbd());
        }
        if (session != null && result.hasAuditLog()) {
            BaseExamStaffServlet.persist(session, result.getAuditAction(), result.getAuditDetails(),
                    result.getAuditRecordId());
        }
    }

    // --- From BaseExamStaffServlet.java ---


    

    public static void bind(HttpServletRequest request, AllocationStageViewDTO view) {
        if (request == null || view == null) {
            return;
        }
        request.setAttribute("allocationPracticalStageIds", view.getPracticalStageIds());
        request.setAttribute("allocationNoRoadTestIds", view.getNoRoadTestIds());
        request.setAttribute("allocationStageCounts", view.getStageCounts());
        request.setAttribute("allocationStageList", view.getStageList());
        request.setAttribute("allocationPageSlice", view.getPageSlice());
        request.setAttribute("allocationOverviewHits", view.getOverviewSearchHits());
    }

    // --- From BaseExamStaffServlet.java ---


    

    private static CallBoardSyncService sync() {
        return ExamStaffServices.get().callBoardSync();
    }

    public static CallBoardRepository repository(ServletContext ctx) {
        return new ServletContextCallBoardRepository(ctx);
    }

    public static CallBoardState getState(ServletContext ctx, int examSessionId) {
        if (ctx == null || examSessionId <= 0) {
            return null;
        }
        return sync().getState(repository(ctx), examSessionId);
    }

    public static void sync(ServletContext ctx, int examSessionId, String callingSbd,
            List<ExamRegistrationDTO> queue, boolean shiftEnded) {
        if (ctx == null || examSessionId <= 0) {
            return;
        }
        sync().sync(repository(ctx), examSessionId, callingSbd, queue, shiftEnded);
    }

    public static void occupyDesk(ServletContext ctx, int examSessionId, String deskSbd,
            List<ExamRegistrationDTO> queue, boolean shiftEnded) {
        if (ctx == null || examSessionId <= 0) {
            return;
        }
        sync().occupyDesk(repository(ctx), examSessionId, deskSbd, queue, shiftEnded);
    }

    public static void releaseDeskAndCall(ServletContext ctx, int examSessionId, String callingSbd,
            List<ExamRegistrationDTO> queue, boolean shiftEnded) {
        if (ctx == null || examSessionId <= 0) {
            return;
        }
        sync().releaseDeskAndCall(repository(ctx), examSessionId, callingSbd, queue, shiftEnded);
    }

    public static void syncFromSession(ServletContext ctx, int examSessionId, String callingSbd,
            boolean shiftEnded, List<ExamRegistrationDTO> queue) {
        sync(ctx, examSessionId, callingSbd, queue, shiftEnded);
    }

    public static void resumeShift(ServletContext ctx, int examSessionId) {
        CallBoardState state = getState(ctx, examSessionId);
        if (state != null) {
            state.setShiftEnded(false);
            repository(ctx).saveState(examSessionId, state);
        }
    }

    public static List<ExamRegistrationDTO> applyQueueOrder(List<ExamRegistrationDTO> queue,
            List<String> orderSbds) {
        return CallQueueRules.applyQueueOrder(queue, orderSbds);
    }

    public static ExamRegistrationDTO findBySbd(List<ExamRegistrationDTO> queue, String sbd) {
        return CallQueueRules.findBySbd(queue, sbd);
    }

    // --- From BaseExamStaffServlet.java ---


    

    public static void bind(HttpServletRequest request, CandidateDossierViewDTO view, boolean autoPrint) {
        if (request == null || view == null || view.getProfile() == null) {
            return;
        }
        request.setAttribute("profile", view.getProfile());
        request.setAttribute("examSession", view.getExamSession());
        request.setAttribute("hasPhotoFile", view.isHasPhotoFile());
        request.setAttribute("payment", null);
        if (view.getFees() != null) {
            request.setAttribute("feeLines", view.getFees().getFeeLines());
            request.setAttribute("feeTotal", view.getFees().getFeeTotal());
            request.setAttribute("feesFromPayment", view.getFees().isFeesFromPayment());
        }
        request.setAttribute("dossierTitle", view.getDossierTitle());
        request.setAttribute("dossierSubtitle", view.getDossierSubtitle());
        request.setAttribute("autoPrint", autoPrint);
    }

    // --- From BaseExamStaffServlet.java ---


    

    public static void bind(HttpServletRequest request, ExaminerAllocationViewDTO view, int examId) {
        if (request == null || view == null) {
            return;
        }
        request.setAttribute("daySessions", view.getDaySessions());
        request.setAttribute("examSessions", view.getDaySessions());
        request.setAttribute("dayAssignments", view.getDayAssignments());
        request.setAttribute("examAssignments", view.getDayAssignments());
        request.setAttribute("sessionAssignments", view.getSessionAssignments());
        request.setAttribute("allExaminers", view.getAllExaminers());
        request.setAttribute("availableExaminers", view.getAvailableExaminers());
        request.setAttribute("busyExaminers", view.getBusyExaminers());
        request.setAttribute("sessionAreas", view.getSessionAreas());
        request.setAttribute("devicesByArea", view.getDevicesByArea());
        request.setAttribute("areasBySession", view.getAreasBySession());
        request.setAttribute("areaAssignOptions", view.getAreaAssignOptions());
        request.setAttribute("examStaffLoadedExamId", examId);
    }

    // --- From BaseExamStaffServlet.java ---


    

    public static void bind(HttpServletRequest request, ExamStaffDashboardViewDTO view) {
        if (request == null || view == null) {
            return;
        }
        request.setAttribute("examSessions", view.getDaySessions());
        request.setAttribute("assignedExaminerUniqueCount", view.getAssignedExaminerUniqueCount());
        request.setAttribute("totalActiveExaminerCount", view.getTotalActiveExaminerCount());
    }

    // --- From BaseExamStaffServlet.java ---


    

    public static void applyNoCacheHeaders(HttpServletResponse response) {
        if (response == null) {
            return;
        }
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
    }

    public static int parseSessionIdParam(HttpServletRequest request) {
        if (request == null) {
            return 0;
        }
        String[] values = request.getParameterValues("sessionId");
        if (values == null || values.length == 0) {
            values = request.getParameterValues("examSessionId");
        }
        if (values == null || values.length == 0) {
            return 0;
        }
        for (int i = values.length - 1; i >= 0; i--) {
            if (values[i] == null || values[i].isBlank()) {
                continue;
            }
            try {
                int parsed = Integer.parseInt(values[i].trim());
                if (parsed > 0) {
                    return parsed;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return 0;
    }

    public static int resolveActiveSessionId(HttpServletRequest request) {
        int fromParam = parseSessionIdParam(request);
        if (fromParam > 0) {
            return fromParam;
        }
        HttpSession session = request != null ? request.getSession(false) : null;
        if (session != null) {
            Object selected = session.getAttribute("selectedSessionId");
            if (selected instanceof Integer id && id > 0) {
                return id;
            }
        }
        CallBoardRepository repository = new ServletContextCallBoardRepository(request.getServletContext());
        Integer active = repository.getActiveSessionId();
        return active != null ? active : 0;
    }

    public static void consumeFlash(HttpSession session, String sessionKey,
            HttpServletRequest request, String attributeName) {
        if (session == null || request == null || sessionKey == null) {
            return;
        }
        Object value = session.getAttribute(sessionKey);
        if (value != null) {
            request.setAttribute(attributeName, value);
            session.removeAttribute(sessionKey);
        }
    }

    public static String resolveSafeRedirect(HttpServletRequest request, String fallbackPath) {
        if (request == null) {
            return fallbackPath;
        }
        String referer = request.getHeader("Referer");
        if (referer != null && referer.contains("/views/staff/examstaff/")) {
            return referer;
        }
        String ctx = request.getContextPath();
        if (fallbackPath.startsWith("/")) {
            return ctx + fallbackPath;
        }
        return ctx + "/" + fallbackPath;
    }

    public static String stripQueryString(String url) {
        if (url == null) {
            return null;
        }
        int q = url.indexOf('?');
        return q >= 0 ? url.substring(0, q) : url;
    }

    public static String upsertQueryParam(String url, String key, String value) {
        if (url == null || key == null || value == null) {
            return url;
        }
        String base = stripQueryString(url);
        String query = url.contains("?") ? url.substring(url.indexOf('?') + 1) : "";
        StringBuilder rebuilt = new StringBuilder();
        boolean replaced = false;
        if (!query.isBlank()) {
            for (String part : query.split("&")) {
                if (part.isBlank()) {
                    continue;
                }
                if (part.startsWith(key + "=")) {
                    if (!replaced) {
                        if (rebuilt.length() > 0) {
                            rebuilt.append('&');
                        }
                        rebuilt.append(key).append('=').append(value);
                        replaced = true;
                    }
                } else {
                    if (rebuilt.length() > 0) {
                        rebuilt.append('&');
                    }
                    rebuilt.append(part);
                }
            }
        }
        if (!replaced) {
            if (rebuilt.length() > 0) {
                rebuilt.append('&');
            }
            rebuilt.append(key).append('=').append(value);
        }
        return base + "?" + rebuilt;
    }

    // --- From BaseExamStaffServlet.java ---


    

    public static void bindPickerView(HttpServletRequest request, ExamStaffPickerViewDTO picker) {
        if (request == null || picker == null) {
            return;
        }
        request.setAttribute("examOptions", picker.getExamOptions());
        request.setAttribute("allSessions", picker.getAllSessions());
        request.setAttribute("currentSession", picker.getCurrentSession());
        request.setAttribute("selectedExamId", picker.getExamId());
        request.setAttribute("selectedSessionId", picker.getSelectedSessionId());
        if (picker.getPickerCommittedSessionId() != null) {
            request.setAttribute("pickerCommittedSessionId", picker.getPickerCommittedSessionId());
        }
        if (picker.getPickerCommittedExamId() != null) {
            request.setAttribute("pickerCommittedExamId", picker.getPickerCommittedExamId());
        }
    }

    public static void publishQueue(HttpServletRequest request, HttpSession session,
            CandidateQueueSnapshotDTO snapshot) {
        if (snapshot == null) {
            return;
        }
        publishQueue(request, session, snapshot.getFullQueue(), snapshot.getActiveQueue(),
                snapshot.getProcedureDone(), snapshot.getResolvedExamId(), snapshot.getResolvedSessionId(),
                null);
    }

    public static void publishQueue(HttpServletRequest request, HttpSession session,
            List<ExamRegistrationDTO> qList, List<ExamRegistrationDTO> active,
            List<ExamRegistrationDTO> done, int examId, int sessionId) {
        publishQueue(request, session, qList, active, done, examId, sessionId, null);
    }

    public static void publishQueue(HttpServletRequest request, HttpSession session,
            List<ExamRegistrationDTO> qList, List<ExamRegistrationDTO> active,
            List<ExamRegistrationDTO> done, int examId, int sessionId, SessionDTO currentSession) {
        if (qList == null) {
            qList = List.of();
        }
        if (active == null) {
            active = List.of();
        }
        if (done == null) {
            done = List.of();
        }

        if (session != null) {
            session.setAttribute("candidateQueue", qList);
            session.setAttribute("activeCallQueue", active);
            session.setAttribute("procedureDoneCandidates", done);
            session.setAttribute("examStaffLoadedExamId", examId);
            session.setAttribute("examStaffLoadedSessionId", sessionId);
            if (examId > 0) {
                session.setAttribute("selectedExamId", examId);
                session.setAttribute("lastLoadedExamId", examId);
            }
            if (sessionId > 0) {
                session.setAttribute("selectedSessionId", sessionId);
                session.setAttribute("lastLoadedSessionId", sessionId);
            }
        }
        if (request != null) {
            request.setAttribute("candidateQueue", qList);
            request.setAttribute("activeCallQueue", active);
            request.setAttribute("procedureDoneCandidates", done);
            request.setAttribute("examStaffLoadedExamId", examId);
            request.setAttribute("examStaffLoadedSessionId", sessionId);
            request.setAttribute("selectedExamId", examId);
            request.setAttribute("selectedSessionId", sessionId > 0 ? sessionId : null);
            if (currentSession != null) {
                request.setAttribute("currentSession", currentSession);
            }
        }
    }

    public static void bindCandidateCallPage(HttpServletRequest request, int examId,
            ExamRegistrationDTO callingCandidate, int sessionId, int suspendedCount,
            SessionDTO currentSession) {
        if (request == null) {
            return;
        }
        request.setAttribute("callingCandidate", callingCandidate);
        request.setAttribute("suspendedCount", suspendedCount);
        if (currentSession != null) {
            request.setAttribute("currentSession", currentSession);
        }
        request.setAttribute("selectedExamId", examId);
        request.setAttribute("selectedSessionId", sessionId > 0 ? sessionId : null);
    }

    public static void bindProcedureFees(HttpServletRequest request, ProcedureFeeResultDTO fees) {
        if (request == null || fees == null) {
            return;
        }
        request.setAttribute("feeLines", fees.getFeeLines());
        request.setAttribute("feeTotal", fees.getFeeTotal());
        request.setAttribute("feesFromPayment", fees.isFeesFromPayment());
    }

    public static void bindImportExam(HttpServletRequest request, SessionDTO currentSession, int examId) {
        if (request == null) {
            return;
        }
        if (currentSession != null) {
            request.setAttribute("currentSession", currentSession);
            if (currentSession.getLicenseCode() != null && !currentSession.getLicenseCode().isBlank()) {
                request.setAttribute("importExamLicense", currentSession.getLicenseCode());
            }
        }
        request.setAttribute("selectedExamId", examId);
    }

    public static void persistExamSelection(HttpSession session, int sessionId, int examId) {
        if (session == null) {
            return;
        }
        if (examId > 0) {
            session.setAttribute("selectedExamId", examId);
        }
        if (sessionId > 0) {
            session.setAttribute("selectedSessionId", sessionId);
        }
    }

    public static void clearCandidateCache(HttpSession session) {
        if (session == null) {
            return;
        }
        session.removeAttribute("candidateQueue");
        session.removeAttribute("activeCallQueue");
        session.removeAttribute("procedureDoneCandidates");
        session.removeAttribute("examStaffLoadedExamId");
        session.removeAttribute("examStaffLoadedSessionId");
        session.removeAttribute("lastLoadedExamId");
        session.removeAttribute("lastLoadedSessionId");
        session.removeAttribute("callQueueOrder");
    }

    public static void clearProcedureStateOnExamChange(HttpSession session, int newExamId, int newSessionId) {
        if (session == null) {
            return;
        }
        session.removeAttribute("callingSbd");
        session.removeAttribute("lastSelectedSbd");
        session.removeAttribute("procedureStep");
        session.removeAttribute("procedureJustPaid");
        session.removeAttribute("procedureJustPaidSbd");
        session.removeAttribute("shiftEnded");
        session.removeAttribute("permanentAbsents");
        clearCandidateCache(session);
        if (newExamId > 0 && newSessionId > 0) {
            persistExamSelection(session, newSessionId, newExamId);
        }
    }

    public static void syncCallQueueOrder(HttpSession session, int sessionId, List<ExamRegistrationDTO> queue) {
        if (session == null || queue == null) {
            return;
        }
        List<String> order = new ArrayList<>();
        for (ExamRegistrationDTO c : queue) {
            if (c != null && c.getSbd() != null) {
                order.add(c.getSbd());
            }
        }
        session.setAttribute("callQueueOrder", order);
        session.setAttribute("callQueueOrderSessionId", sessionId);
    }

    // --- From BaseExamStaffServlet.java ---


    

    public static void bind(HttpServletRequest request, ExamReportProcedureStatusDTO status) {
        if (request == null || status == null) {
            return;
        }
        request.setAttribute("missingPhotoCount", status.getMissingPhotoCount());
        request.setAttribute("missingPhotoSbds", status.getMissingPhotoSbds());
        request.setAttribute("missingPhotoCandidates", status.getMissingPhotoCandidates());
        request.setAttribute("procedurePendingCandidates", status.getProcedurePendingCandidates());
        request.setAttribute("procedureCompleteCount", status.getProcedureCompleteCount());
        request.setAttribute("procedurePendingCount", status.getProcedurePendingCount());
    }

    // --- From BaseExamStaffServlet.java ---


    

    public static void bind(HttpServletRequest request, ExamReportStatsDTO stats) {
        if (request == null || stats == null) {
            return;
        }
        request.setAttribute("totalCandidates", stats.getTotalCandidates());
        request.setAttribute("examCompletedCount", stats.getExamCompletedCount());
        request.setAttribute("passedCount", stats.getPassedCount());
        request.setAttribute("failedCount", stats.getFailedCount());
        request.setAttribute("absentCount", stats.getAbsentCount());
        request.setAttribute("passRate", stats.getPassRate());
        request.setAttribute("licenseStats", stats.getLicenseStats());
        request.setAttribute("a1Count", stats.getA1Count());
        request.setAttribute("a1Completed", stats.getA1Completed());
        request.setAttribute("a1Passed", stats.getA1Passed());
        request.setAttribute("a1Failed", stats.getA1Failed());
        request.setAttribute("aCount", stats.getACount());
        request.setAttribute("aCompleted", stats.getACompleted());
        request.setAttribute("aPassed", stats.getAPassed());
        request.setAttribute("aFailed", stats.getAFailed());
        request.setAttribute("b1Count", stats.getB1Count());
        request.setAttribute("b1Completed", stats.getB1Completed());
        request.setAttribute("b1Passed", stats.getB1Passed());
        request.setAttribute("b1Failed", stats.getB1Failed());
        request.setAttribute("theoryCount", stats.getTheoryCount());
        request.setAttribute("theoryPassed", stats.getTheoryPassed());
        request.setAttribute("theoryFailed", stats.getTheoryFailed());
        request.setAttribute("practicalCount", stats.getPracticalCount());
        request.setAttribute("practicalPassed", stats.getPracticalPassed());
        request.setAttribute("practicalFailed", stats.getPracticalFailed());
        request.setAttribute("roadCount", stats.getRoadCount());
        request.setAttribute("roadPassed", stats.getRoadPassed());
        request.setAttribute("roadFailed", stats.getRoadFailed());
        request.setAttribute("infractions", stats.getInfractions());
    }

    // --- From BaseExamStaffServlet.java ---


    private static final StaffAuditLogService AUDIT_LOG = new StaffAuditLogServiceImpl();

    

    public static void persist(HttpSession session, String action, String details) {
        persist(session, action, details, 0);
    }

    public static void persist(HttpSession session, String action, String details, int recordId) {
        AUDIT_LOG.logAction(SessionUserUtil.resolveUserId(session), action, details, recordId);
    }

    public static void persistWithSessionFeed(HttpSession session, String action, String details) {
        persistWithSessionFeed(session, action, details, 0);
    }

    public static void persistWithSessionFeed(HttpSession session, String action, String details, int recordId) {
        appendSessionFeed(session, action, details);
        persist(session, action, details, recordId);
    }

    @SuppressWarnings("unchecked")
    private static void appendSessionFeed(HttpSession session, String action, String details) {
        if (session == null) {
            return;
        }
        List<Map<String, String>> sessionAuditLogs
                = (List<Map<String, String>>) session.getAttribute("sessionAuditLogs");
        if (sessionAuditLogs == null) {
            sessionAuditLogs = new ArrayList<>();
            session.setAttribute("sessionAuditLogs", sessionAuditLogs);
        }
        Map<String, String> audit = new HashMap<>();
        audit.put("time", new SimpleDateFormat("HH:mm").format(new Date()));
        audit.put("action", action);
        audit.put("details", details);
        sessionAuditLogs.add(0, audit);
    }

    // --- From BaseExamStaffServlet.java ---


    

    public static void bind(HttpServletRequest request, StaffAuditPageViewDTO view) {
        if (request == null || view == null) {
            return;
        }
        request.setAttribute("personalLogs", view.getPersonalLogs());
        request.setAttribute("examStaffPageSlice", view.getPageSlice());
        request.setAttribute("examStaffListPath", "/views/staff/examstaff/audit");
        int completed = view.getProcedureKpi() != null ? view.getProcedureKpi().getCompletedCount() : 0;
        double totalFees = view.getProcedureKpi() != null ? view.getProcedureKpi().getTotalFees() : 0;
        request.setAttribute("myCompletedProcedures", completed);
        request.setAttribute("myTotalFees", totalFees);
    }

}
