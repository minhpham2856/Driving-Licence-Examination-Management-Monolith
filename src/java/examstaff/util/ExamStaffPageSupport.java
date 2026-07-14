package examstaff.util;

import examstaff.dto.CandidateQueueSnapshotDTO;
import examstaff.dto.ExamStaffDashboardViewDTO;
import examstaff.dto.ExamStaffPageContextDTO;
import examstaff.dto.ExamStaffPagePrepareInput;
import examstaff.dto.ExamStaffPageTransitionInput;
import examstaff.dto.ExamStaffPageTransitionStateDTO;
import examstaff.dto.ExamStaffPickerViewDTO;
import examstaff.dto.ExamStaffSelectionResolveInput;
import examstaff.dto.ExamStaffSelectionStateDTO;
import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.ProcedureFeeResultDTO;
import examstaff.dto.exam.ExamRegistrationDTO;
import examstaff.service.impl.CandidateQueueServiceImpl;
import examstaff.service.impl.ExamStaffDashboardServiceImpl;
import examstaff.service.impl.ExamStaffPageServiceImpl;
import examstaff.service.impl.ExamStaffSelectionServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import shared.Attributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ExamStaffPageSupport {

    private static final ExamStaffPageServiceImpl PAGE_SERVICE = new ExamStaffPageServiceImpl();
    private static final ExamStaffSelectionServiceImpl SELECTION_SERVICE = new ExamStaffSelectionServiceImpl();
    private static final CandidateQueueServiceImpl QUEUE_SERVICE = new CandidateQueueServiceImpl();
    private static final ExamStaffDashboardServiceImpl DASHBOARD_SERVICE = new ExamStaffDashboardServiceImpl();

    private ExamStaffPageSupport() {
    }

    public static final class PageContext {
        private final int examId;
        private final List<ExamSummaryDTO> allSessions;
        private final List<ExamRegistrationDTO> candidates;

        public PageContext(int examId, List<ExamSummaryDTO> allSessions, List<ExamRegistrationDTO> candidates) {
            this.examId = examId;
            this.allSessions = allSessions != null ? allSessions : List.of();
            this.candidates = candidates != null ? candidates : List.of();
        }

        public int getExamId() {
            return examId;
        }

        public List<ExamSummaryDTO> getAllSessions() {
            return allSessions;
        }

        public List<ExamRegistrationDTO> getCandidates() {
            return candidates;
        }
    }

    public static int preparePage(HttpServletRequest request, boolean loadCandidates) {
        return preparePageContext(request, loadCandidates).getExamId();
    }

    public static PageContext preparePageContext(HttpServletRequest request, boolean loadCandidates) {
        HttpSession session = request.getSession();
        Utf8EncodingHelper.applyRequest(request);
        String webRoot = request.getServletContext().getRealPath("/");
        int urlExamId = parseExamIdParam(request);

        if (urlExamId > 0 && session != null) {
            ExamStaffPageTransitionInput transitionInput = new ExamStaffPageTransitionInput();
            transitionInput.setUrlExamId(urlExamId);
            transitionInput.setAllSessions(PAGE_SERVICE.listAllSessions());
            transitionInput.setPreviousExamId(readSelectedExamId(session));
            transitionInput.setLoadedExamId(readLoadedExamId(session));
            ExamStaffPageTransitionStateDTO transition = SELECTION_SERVICE.preparePageTransition(transitionInput);
            if (transition.isClearCandidateCache()) {
                clearCandidateCache(session);
            }
            if (transition.isClearProcedureState()) {
                clearProcedureStateOnExamChange(session, transition.getExamId(), transition.getExamId());
            }
            if (transition.isPersistSelection()) {
                persistExamSelection(session, transition.getExamId(), transition.getExamId());
            }
        }

        ExamStaffPagePrepareInput input = buildPrepareInput(request, session, webRoot, loadCandidates, urlExamId);
        ExamStaffPageContextDTO ctx = PAGE_SERVICE.preparePageContext(input);

        if (urlExamId > 0 && ctx.getExamId() <= 0 && request != null) {
            request.setAttribute("sessionSelectError", "Không tìm thấy kỳ thi (mã " + urlExamId + ").");
        }

        if (ctx.getExamId() > 0 && session != null) {
            persistExamSelection(session, ctx.getExamId(), ctx.getExamId());
        }

        if (ctx.getPickerView() != null) {
            bindPickerView(request, ctx.getPickerView());
        }

        CandidateQueueSnapshotDTO snapshot = QUEUE_SERVICE.buildSnapshot(
                ctx.getCandidates(), ctx.getExamId(), ctx.getExamId());
        publishQueue(request, session, snapshot);

        return new PageContext(ctx.getExamId(), ctx.getAllSessions(), ctx.getCandidates());
    }

    public static void bindDashboard(HttpServletRequest request, int examId, List<ExamSummaryDTO> allSessions) {
        if (request == null) {
            return;
        }
        ExamStaffDashboardViewDTO view = DASHBOARD_SERVICE.buildView(allSessions, examId);
        request.setAttribute("assignedExaminerUniqueCount", view.getAssignedExaminerUniqueCount());
        request.setAttribute("totalActiveExaminerCount", view.getTotalActiveExaminerCount());
    }

    public static List<ExamSummaryDTO> loadAllExams() {
        return PAGE_SERVICE.listAllSessions();
    }

    public static int parseExamIdParam(HttpServletRequest request) {
        if (request == null) {
            return 0;
        }
        String[] values = request.getParameterValues("examId");
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

    public static int resolveExamId(HttpServletRequest request, HttpSession session,
            List<ExamSummaryDTO> allSessions) {
        return SELECTION_SERVICE.resolveExamId(buildSelectionInput(request, session, allSessions, 0));
    }

    public static int ensureExamId(HttpServletRequest request, HttpSession session,
            List<ExamSummaryDTO> allSessions) {
        ExamStaffSelectionResolveInput input = buildSelectionInput(request, session, allSessions, 0);
        int examId = SELECTION_SERVICE.ensureExamId(input);
        if (examId > 0 && session != null) {
            int primaryExamId = PAGE_SERVICE.resolvePrimaryExamId(input.getAllSessions(), examId);
            persistExamSelection(session, primaryExamId, examId);
        }
        return examId;
    }

    public static void syncExamSelection(HttpSession session, List<ExamSummaryDTO> allSessions, int examId) {
        if (session == null || examId <= 0) {
            return;
        }
        Integer currentExamId = readSelectedExamId(session);
        ExamStaffSelectionStateDTO state = SELECTION_SERVICE.syncExamSelection(examId, currentExamId, allSessions);
        session.setAttribute(Attributes.ExamStaff.SELECTED_EXAM_ID,
                state.getExamId() > 0 ? state.getExamId() : examId);
        session.removeAttribute("selectedSessionId");
    }

    public static void bindSidebarIfNeeded(HttpServletRequest request, HttpSession session) {
        if (request == null || request.getAttribute("examOptions") != null) {
            return;
        }
        List<ExamSummaryDTO> allSessions = loadAllExams();
        int examId = resolveExamId(request, session, allSessions);
        bindPickerView(request, PAGE_SERVICE.buildPickerView(allSessions, examId, 0));
    }

    public static ExamSummaryDTO findExamById(List<ExamSummaryDTO> allSessions, int examId) {
        return PAGE_SERVICE.findExamById(examId, allSessions);
    }

    public static ExamSummaryDTO representativeSessionForExam(List<ExamSummaryDTO> allSessions, int examId) {
        return PAGE_SERVICE.representativeSessionForExam(allSessions, examId);
    }

    public static void bindPickerView(HttpServletRequest request, ExamStaffPickerViewDTO picker) {
        if (request == null || picker == null) {
            return;
        }
        if (picker.getExamOptions() != null) {
            for (ExamSummaryDTO s : picker.getExamOptions()) {
                normalizeSession(s);
            }
        }
        if (picker.getAllSessions() != null) {
            for (ExamSummaryDTO s : picker.getAllSessions()) {
                normalizeSession(s);
            }
        }
        normalizeSession(picker.getCurrentSession());
        bindSessionShiftContext(request, picker.getCurrentSession());
        request.setAttribute("examOptions", picker.getExamOptions());
        request.setAttribute("allSessions", picker.getAllSessions());
        request.setAttribute(Attributes.ExamStaff.CURRENT_EXAM, picker.getCurrentSession());
        int selectedExamId = picker.getExamId() > 0
                ? picker.getExamId()
                : (picker.getSelectedExamId() != null ? picker.getSelectedExamId() : 0);
        request.setAttribute(Attributes.ExamStaff.SELECTED_EXAM_ID, selectedExamId > 0 ? selectedExamId : null);
        Integer committedExamId = picker.getPickerCommittedExamId();
        if (committedExamId != null) {
            request.setAttribute("pickerCommittedExamId", committedExamId);
        }
    }

    public static void publishQueue(HttpServletRequest request, HttpSession session,
            CandidateQueueSnapshotDTO snapshot) {
        if (snapshot == null) {
            return;
        }
        publishQueue(request, session, snapshot.getFullQueue(), snapshot.getActiveQueue(),
                snapshot.getProcedureDone(), snapshot.getResolvedExamId(), snapshot.getResolvedExamId(), null);
    }

    public static void publishQueue(HttpServletRequest request, HttpSession session,
            List<ExamRegistrationDTO> qList, List<ExamRegistrationDTO> active,
            List<ExamRegistrationDTO> done, int examId, int fallbackExamId) {
        publishQueue(request, session, qList, active, done, examId, fallbackExamId, null);
    }

    public static void publishQueue(HttpServletRequest request, HttpSession session,
            List<ExamRegistrationDTO> qList, List<ExamRegistrationDTO> active,
            List<ExamRegistrationDTO> done, int examId, int fallbackExamId, ExamSummaryDTO currentExam) {
        if (qList == null) {
            qList = List.of();
        }
        if (active == null) {
            active = List.of();
        }
        if (done == null) {
            done = List.of();
        }

        normalizeSession(currentExam);
        for (ExamRegistrationDTO c : qList) {
            normalizeCandidate(c);
        }
        for (ExamRegistrationDTO c : active) {
            normalizeCandidate(c);
        }
        for (ExamRegistrationDTO c : done) {
            normalizeCandidate(c);
        }

        if (session != null) {
            session.setAttribute(Attributes.ExamStaff.CANDIDATE_QUEUE, qList);
            session.setAttribute("activeCallQueue", active);
            session.setAttribute("procedureDoneCandidates", done);
            session.setAttribute("examStaffLoadedExamId", examId);
            int resolvedExamId = examId > 0 ? examId : fallbackExamId;
            if (resolvedExamId > 0) {
                session.setAttribute(Attributes.ExamStaff.SELECTED_EXAM_ID, resolvedExamId);
                session.setAttribute("lastLoadedExamId", resolvedExamId);
            }
        }
        if (request != null) {
            request.setAttribute(Attributes.ExamStaff.CANDIDATE_QUEUE, qList);
            request.setAttribute("activeCallQueue", active);
            request.setAttribute("procedureDoneCandidates", done);
            request.setAttribute("examStaffLoadedExamId", examId);
            int resolvedExamId = examId > 0 ? examId : fallbackExamId;
            request.setAttribute(Attributes.ExamStaff.SELECTED_EXAM_ID, resolvedExamId > 0 ? resolvedExamId : null);
            if (currentExam != null) {
                request.setAttribute(Attributes.ExamStaff.CURRENT_EXAM, currentExam);
                bindSessionShiftContext(request, currentExam);
            }
        }
    }

    public static void bindSessionShiftContext(HttpServletRequest request, ExamSummaryDTO session) {
        if (request == null || session == null) {
            return;
        }
        java.sql.Timestamp scheduledStart = session.getScheduledStartAt() != null
                ? session.getScheduledStartAt()
                : session.getCreatedAt();
        request.setAttribute("sessionCanStartNow", ExamScheduleRules.canStartNow(scheduledStart));
        if (scheduledStart != null) {
            request.setAttribute("sessionScheduledStartLabel",
                    ExamScheduleRules.formatScheduledStart(scheduledStart));
        }
    }

    public static void bindCandidateCallPage(HttpServletRequest request, int examId,
            ExamRegistrationDTO callingCandidate, int selectedExamId, int suspendedCount,
            ExamSummaryDTO currentExam) {
        if (request == null) {
            return;
        }
        normalizeCandidate(callingCandidate);
        normalizeSession(currentExam);
        request.setAttribute("callingCandidate", callingCandidate);
        request.setAttribute("suspendedCount", suspendedCount);
        if (currentExam != null) {
            request.setAttribute(Attributes.ExamStaff.CURRENT_EXAM, currentExam);
            bindSessionShiftContext(request, currentExam);
        }
        request.setAttribute(Attributes.ExamStaff.SELECTED_EXAM_ID, examId > 0 ? examId : selectedExamId);
    }

    public static void bindProcedureFees(HttpServletRequest request, ProcedureFeeResultDTO fees) {
        if (request == null || fees == null) {
            return;
        }
        request.setAttribute("feeLines", fees.getFeeLines());
        request.setAttribute("feeTotal", fees.getFeeTotal());
        request.setAttribute("feesFromPayment", fees.isFeesFromPayment());
    }

    public static Integer readSelectedExamId(HttpSession session) {
        if (session == null) {
            return null;
        }
        Integer selected = (Integer) session.getAttribute(Attributes.ExamStaff.SELECTED_EXAM_ID);
        if (selected == null) {
            selected = (Integer) session.getAttribute("selectedExamId");
        }
        if (selected != null && selected > 0) {
            return selected;
        }
        selected = (Integer) session.getAttribute("selectedSessionId");
        if (selected != null && selected > 0) {
            session.setAttribute(Attributes.ExamStaff.SELECTED_EXAM_ID, selected);
            session.removeAttribute("selectedSessionId");
            return selected;
        }
        return selected;
    }

    public static Integer readCallQueueOrderExamId(HttpSession session) {
        if (session == null) {
            return null;
        }
        Integer examId = (Integer) session.getAttribute("callQueueOrderExamId");
        if (examId != null && examId > 0) {
            return examId;
        }
        Integer legacy = (Integer) session.getAttribute("callQueueOrderSessionId");
        if (legacy != null && legacy > 0) {
            session.setAttribute("callQueueOrderExamId", legacy);
            session.removeAttribute("callQueueOrderSessionId");
            return legacy;
        }
        return legacy;
    }

    public static Integer readLoadedExamId(HttpSession session) {
        if (session == null) {
            return null;
        }
        Integer loaded = (Integer) session.getAttribute("examStaffLoadedExamId");
        if (loaded != null && loaded > 0) {
            return loaded;
        }
        loaded = (Integer) session.getAttribute("examStaffLoadedSessionId");
        if (loaded != null && loaded > 0) {
            session.setAttribute("examStaffLoadedExamId", loaded);
            session.removeAttribute("examStaffLoadedSessionId");
            return loaded;
        }
        return loaded;
    }

    public static void persistExamSelection(HttpSession session, int fallbackExamId, int examId) {
        if (session == null) {
            return;
        }
        int resolvedExamId = examId > 0 ? examId : fallbackExamId;
        if (resolvedExamId > 0) {
            session.setAttribute(Attributes.ExamStaff.SELECTED_EXAM_ID, resolvedExamId);
            session.removeAttribute("selectedSessionId");
        }
    }

    public static void clearCandidateCache(HttpSession session) {
        if (session == null) {
            return;
        }
        session.removeAttribute(Attributes.ExamStaff.CANDIDATE_QUEUE);
        session.removeAttribute("activeCallQueue");
        session.removeAttribute("procedureDoneCandidates");
        session.removeAttribute("examStaffLoadedExamId");
        session.removeAttribute("examStaffLoadedSessionId");
        session.removeAttribute("lastLoadedExamId");
        session.removeAttribute("lastLoadedSessionId");
        session.removeAttribute("callQueueOrder");
    }

    public static void clearProcedureStateOnExamChange(HttpSession session, int newExamId, int newFallbackExamId) {
        if (session == null) {
            return;
        }
        session.removeAttribute("callingSbd");
        session.removeAttribute("lastSelectedSbd");
        session.removeAttribute("procedureStep");
        session.removeAttribute("procedureJustPaid");
        session.removeAttribute("procedureJustPaidSbd");
        session.removeAttribute("shiftEnded");
        session.removeAttribute("shiftPaused");
        session.removeAttribute("permanentAbsents");
        clearCandidateCache(session);
        if (newExamId > 0 || newFallbackExamId > 0) {
            persistExamSelection(session, newFallbackExamId, newExamId);
        }
    }

    public static void syncCallQueueOrder(HttpSession session, int examId, List<ExamRegistrationDTO> queue) {
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
        session.setAttribute("callQueueOrderExamId", examId);
        session.removeAttribute("callQueueOrderSessionId");
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

    private static ExamStaffPagePrepareInput buildPrepareInput(HttpServletRequest request, HttpSession session,
            String webRoot, boolean loadCandidates, int urlExamId) {
        ExamStaffPagePrepareInput input = new ExamStaffPagePrepareInput();
        input.setUrlExamId(urlExamId);
        input.setWebRoot(webRoot);
        input.setLoadCandidates(loadCandidates);
        input.setHasExamIdParam(parseExamIdParam(request) > 0);
        input.setAllSessions(loadAllExams());
        if (request != null) {
            input.setExamIdParam(request.getParameter("examId"));
        }
        if (session != null) {
            input.setPreviousExamId(readSelectedExamId(session));
            input.setSelectedExamId(readSelectedExamId(session));
            input.setLoadedExamId(readLoadedExamId(session));
            @SuppressWarnings("unchecked")
            List<ExamRegistrationDTO> cached = (List<ExamRegistrationDTO>) session.getAttribute(
                    Attributes.ExamStaff.CANDIDATE_QUEUE);
            input.setCachedQueue(cached);
            @SuppressWarnings("unchecked")
            List<String> order = (List<String>) session.getAttribute("callQueueOrder");
            input.setCallQueueOrder(order);
            input.setCallQueueOrderExamId(readCallQueueOrderExamId(session));
        }
        return input;
    }

    private static ExamStaffSelectionResolveInput buildSelectionInput(HttpServletRequest request,
            HttpSession session, List<ExamSummaryDTO> allSessions, int defaultExamId) {
        ExamStaffSelectionResolveInput input = new ExamStaffSelectionResolveInput();
        input.setUrlExamId(parseExamIdParam(request));
        input.setAllSessions(allSessions);
        input.setDefaultExamId(defaultExamId);
        if (request != null) {
            input.setExamIdParam(request.getParameter("examId"));
        }
        if (session != null) {
            input.setSelectedExamId(readSelectedExamId(session));
        }
        return input;
    }

    private static String normalizeLicenseForExamstaff(String raw) {
        if (raw == null) {
            return null;
        }
        String normalized = LicenseClassRules.normalizeManaged(raw);
        if (normalized != null && !normalized.isBlank()) {
            return normalized;
        }
        return raw.trim().toUpperCase(Locale.ROOT);
    }

    private static void normalizeSession(ExamSummaryDTO s) {
        if (s == null) {
            return;
        }
        s.setLicenseCode(normalizeLicenseForExamstaff(s.getLicenseCode()));
    }

    private static void normalizeCandidate(ExamRegistrationDTO c) {
        if (c == null) {
            return;
        }
        c.setLicenseCode(normalizeLicenseForExamstaff(c.getLicenseCode()));
    }
}
