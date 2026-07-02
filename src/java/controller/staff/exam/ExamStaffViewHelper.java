package controller.staff.exam;

import dao.ExamRegistrationDAO;
import dao.ExamSessionDAO;
import dao.FeeDAO;
import dao.PaymentDAO;
import dao.impl.ExamRegistrationDAOImpl;
import dao.impl.ExamSessionDAOImpl;
import dao.impl.FeeDAOImpl;
import dao.impl.PaymentDAOImpl;
import dto.exam.ExamRegistrationDTO;
import dto.exam.SessionDTO;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.payment.Fee;
import model.payment.Payment;
import util.ProcedureFeeTotals;
import util.SessionUserHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ExamStaffViewHelper {

    private static final String UTF_8 = "UTF-8";

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

    private static final class WorkContext {
        private int examId;
        private int sessionId;
    }

    // Chuan bi context trang exam staff (co tai hang doi)
    public static ExamStaffPageContext prepareExamStaffPage(HttpServletRequest request, HttpSession session,
            ExamSessionDAO sessionDAO, String webRoot) {
        return prepareExamStaffPage(request, session, sessionDAO, webRoot, true);
    }

    // Chuan bi context trang exam staff (tuy chon tai hang doi)
    public static ExamStaffPageContext prepareExamStaffPage(HttpServletRequest request, HttpSession session,
            ExamSessionDAO sessionDAO, String webRoot, boolean loadCandidates) {
        applyUtf8Request(request);
        List<SessionDTO> allSessions = loadAllSessions(sessionDAO);

        int urlSessionId = parseSessionIdParam(request);
        Integer previousExamId = session != null ? (Integer) session.getAttribute("selectedExamId") : null;
        Integer previousSessionId = session != null ? (Integer) session.getAttribute("selectedSessionId") : null;
        if (urlSessionId > 0 && session != null) {
            Integer loadedSession = (Integer) session.getAttribute("examStaffLoadedSessionId");
            if (loadedSession == null || loadedSession != urlSessionId) {
                clearCandidateCache(session);
            }
            SessionDTO urlSession = resolveSessionById(urlSessionId, allSessions, sessionDAO);
            if (urlSession != null && urlSession.getExamId() > 0
                    && previousExamId != null && previousExamId > 0
                    && !previousExamId.equals(urlSession.getExamId())) {
                clearProcedureStateOnExamChange(request, session, previousExamId, previousSessionId,
                        urlSession.getExamId(), urlSessionId);
            }
            applySessionIdFromRequest(request, session, allSessions, sessionDAO);
        }

        int[] ids = resolveExamAndSessionForRequest(request, session, sessionDAO);
        int examId = ids[0];
        int sessionId = ids[1];
        if (examId <= 0) {
            WorkContext work = resolveWorkContext(request, session, sessionDAO, allSessions);
            examId = work.examId;
            sessionId = work.sessionId;
        }

        if (hasSessionIdParam(request) && examId <= 0 && request != null) {
            request.setAttribute("sessionSelectError",
                    "Không tìm thấy kỳ thi (sessionId=" + urlSessionId + ").");
        }

        examId = bindExamPickerAttributes(request, sessionDAO, allSessions, examId);
        if (urlSessionId > 0) {
            sessionId = urlSessionId;
        } else {
            int[] afterBind = resolveExamAndSessionForRequest(request, session, sessionDAO);
            if (afterBind[1] > 0) {
                sessionId = afterBind[1];
            }
            if (afterBind[0] > 0) {
                examId = afterBind[0];
            }
        }
        if (sessionId <= 0 && examId > 0) {
            sessionId = resolvePrimarySessionId(allSessions, examId);
        }

        if (examId <= 0 && request != null) {
            @SuppressWarnings("unchecked")
            List<SessionDTO> pickerOptions = (List<SessionDTO>) request.getAttribute("examOptions");
            if (pickerOptions != null && !pickerOptions.isEmpty()) {
                SessionDTO first = pickerOptions.get(0);
                examId = first.getExamId();
                sessionId = first.getId();
                persistExamSelection(session, sessionId, examId);
                bindExamPickerAttributes(request, sessionDAO, allSessions, examId);
            }
        }

        if (examId > 0 && sessionId > 0 && session != null) {
            persistExamSelection(session, sessionId, examId);
        }

        List<ExamRegistrationDTO> candidates = resolveCandidatesForPage(
                request, session, examId, sessionId, webRoot, allSessions, loadCandidates);
        publishCandidateQueue(request, session, candidates, examId, sessionId);
        return new ExamStaffPageContext(examId, sessionId, allSessions, candidates);
    }

    // Gan header no-cache cho response
    public static void applyNoCacheHeaders(HttpServletResponse response) {
        if (response == null) {
            return;
        }
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
    }

    // Doc sessionId tu request (uu tien gia tri cuoi neu trung lap)
    public static int parseSessionIdParam(HttpServletRequest request) {
        if (request == null) {
            return 0;
        }
        String[] values = request.getParameterValues("sessionId");
        if (values == null || values.length == 0) {
            String single = request.getParameter("sessionId");
            if (single == null || single.isBlank()) {
                return 0;
            }
            values = new String[]{single};
        }
        for (int i = values.length - 1; i >= 0; i--) {
            if (values[i] == null || values[i].isBlank()) {
                continue;
            }
            try {
                int id = Integer.parseInt(values[i].trim());
                if (id > 0) {
                    return id;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return 0;
    }

    // Kiem tra URL co sessionId hop le
    public static boolean hasSessionIdParam(HttpServletRequest request) {
        return parseSessionIdParam(request) > 0;
    }

    // Tai tat ca ca thi tu DAO
    public static List<SessionDTO> loadAllSessions(ExamSessionDAO sessionDAO) {
        if (sessionDAO == null) {
            sessionDAO = new ExamSessionDAOImpl();
        }
        try {
            List<SessionDTO> sessions = sessionDAO.getAllSessions();
            return sessions != null ? sessions : new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Xoa cache hang doi thi sinh trong session
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

    // Ap dung sessionId tu request vao session, tra ve examId
    public static int applySessionIdFromRequest(HttpServletRequest request, HttpSession session,
            List<SessionDTO> allSessions, ExamSessionDAO sessionDAO) {
        int sessionId = parseSessionIdParam(request);
        if (sessionId <= 0) {
            return resolveExamId(request, session, allSessions, 0);
        }
        SessionDTO picked = resolveSessionById(sessionId, allSessions, sessionDAO);
        if (picked == null || picked.getExamId() <= 0) {
            return 0;
        }
        persistExamSelection(session, sessionId, picked.getExamId());
        return picked.getExamId();
    }

    // Giai quyet examId tu request/session
    public static int resolveExamId(HttpServletRequest request, HttpSession session,
            List<SessionDTO> allSessions, int defaultId) {
        if (session != null) {
            Integer selectedExam = (Integer) session.getAttribute("selectedExamId");
            if (selectedExam != null && selectedExam > 0) {
                return selectedExam;
            }
        }
        int sessionId = parseSessionIdParam(request);
        if (sessionId > 0) {
            SessionDTO current = resolveSessionById(sessionId, allSessions, null);
            if (current != null && current.getExamId() > 0) {
                return current.getExamId();
            }
        }
        if (request != null) {
            String examIdParam = request.getParameter("examId");
            if (examIdParam != null && !examIdParam.isBlank()) {
                try {
                    int parsed = Integer.parseInt(examIdParam.trim());
                    if (parsed > 0) {
                        return parsed;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        if (defaultId > 0) {
            return defaultId;
        }
        if (allSessions != null && !allSessions.isEmpty()) {
            return resolveDefaultExamId(allSessions);
        }
        return 0;
    }

    // Dam bao co examId hop le trong session
    public static int ensureExamId(HttpServletRequest request, HttpSession session, List<SessionDTO> allSessions) {
        return ensureExamId(request, session, allSessions, new ExamSessionDAOImpl());
    }

    // Dam bao co examId hop le (co DAO de tra cuu ca)
    public static int ensureExamId(HttpServletRequest request, HttpSession session,
            List<SessionDTO> allSessions, ExamSessionDAO sessionDAO) {
        int examId = resolveExamId(request, session, allSessions, 0);
        if (examId > 0) {
            return examId;
        }
        if (allSessions == null || allSessions.isEmpty()) {
            allSessions = loadAllSessions(sessionDAO);
        }
        examId = resolveDefaultExamId(allSessions);
        if (examId > 0 && session != null) {
            int sessionId = resolvePrimarySessionId(allSessions, examId);
            persistExamSelection(session, sessionId, examId);
        }
        return examId;
    }

    // Giai quyet sessionId tu request/session
    public static int resolveSessionId(HttpServletRequest request, HttpSession session,
            List<SessionDTO> allSessions, int defaultId) {
        int parsed = parseSessionIdParam(request);
        if (parsed > 0) {
            return parsed;
        }
        if (session != null) {
            Integer selected = (Integer) session.getAttribute("selectedSessionId");
            if (selected != null && selected > 0) {
                return selected;
            }
        }
        if (defaultId > 0) {
            return defaultId;
        }
        if (session != null) {
            Integer examId = (Integer) session.getAttribute("selectedExamId");
            if (examId != null && examId > 0 && allSessions != null) {
                return resolvePrimarySessionId(allSessions, examId);
            }
        }
        return 0;
    }

    // Dong bo selectedExamId/selectedSessionId sau thao tac
    public static void syncExamSelection(HttpSession session, List<SessionDTO> allSessions, int examId) {
        if (session == null || examId <= 0) {
            return;
        }
        session.setAttribute("selectedExamId", examId);
        Integer currentSession = (Integer) session.getAttribute("selectedSessionId");
        if (currentSession == null || currentSession <= 0) {
            int primary = resolvePrimarySessionId(allSessions, examId);
            if (primary > 0) {
                session.setAttribute("selectedSessionId", primary);
            }
        } else if (allSessions != null) {
            SessionDTO picked = findSessionById(allSessions, currentSession);
            if (picked == null || picked.getExamId() != examId) {
                session.setAttribute("selectedSessionId", resolvePrimarySessionId(allSessions, examId));
            }
        }
    }

    // Tai hang doi thi sinh (overload ngan)
    public static List<ExamRegistrationDTO> refreshCandidateQueue(HttpSession session, int examId, String webRoot) {
        return refreshCandidateQueue(session, examId, webRoot, null);
    }

    // Tai hang doi thi sinh (lay sessionId tu session)
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

    // Tai hang doi thi sinh theo ca (uu tien sessionId)
    public static List<ExamRegistrationDTO> refreshCandidateQueue(HttpSession session, int examId, int sessionId,
            String webRoot, List<SessionDTO> allSessions) {
        if (session == null) {
            return new ArrayList<>();
        }
        if (examId <= 0 && allSessions != null && !allSessions.isEmpty()) {
            examId = resolveDefaultExamId(allSessions);
        }
        if (examId <= 0 && sessionId > 0 && allSessions != null) {
            SessionDTO picked = findSessionById(allSessions, sessionId);
            if (picked == null) {
                picked = new ExamSessionDAOImpl().getById(sessionId);
            }
            if (picked != null && picked.getExamId() > 0) {
                examId = picked.getExamId();
            }
        }
        if (examId <= 0 && sessionId <= 0) {
            return new ArrayList<>();
        }

        if (allSessions == null || allSessions.isEmpty()) {
            allSessions = loadAllSessions(new ExamSessionDAOImpl());
        }
        if (sessionId <= 0 && examId > 0) {
            sessionId = resolvePrimarySessionId(allSessions, examId);
            Integer picked = (Integer) session.getAttribute("selectedSessionId");
            if (picked != null && picked > 0) {
                SessionDTO pickedSession = findSessionById(allSessions, picked);
                if (pickedSession == null) {
                    pickedSession = new ExamSessionDAOImpl().getById(picked);
                }
                if (pickedSession != null && pickedSession.getExamId() == examId) {
                    sessionId = picked;
                }
            }
        }

        ExamRegistrationDAO regDAO = new ExamRegistrationDAOImpl();
        List<ExamRegistrationDTO> qList;
        try {
            if (sessionId > 0) {
                SessionDTO picked = findSessionById(allSessions, sessionId);
                if (picked == null) {
                    picked = new ExamSessionDAOImpl().getById(sessionId);
                }
                if (picked != null && picked.getExamId() > 0) {
                    examId = picked.getExamId();
                }
                qList = regDAO.getCandidatesBySession(sessionId);
            } else if (examId > 0) {
                qList = regDAO.getCandidatesByExam(examId);
                if (qList.isEmpty()) {
                    int primarySessionId = resolvePrimarySessionId(allSessions, examId);
                    if (primarySessionId > 0) {
                        qList = regDAO.getCandidatesBySession(primarySessionId);
                    }
                }
            } else {
                qList = new ArrayList<>();
            }
        } catch (Exception e) {
            e.printStackTrace();
            qList = new ArrayList<>();
        }
        if (webRoot != null) {
            CandidatePhotoHelper.normalizeQueue(webRoot, qList);
        }
        for (ExamRegistrationDTO c : qList) {
            AllocationPassRules.applyToCandidate(c);
        }
        qList = applyCallQueueOrder(session, sessionId, qList);
        publishCandidateQueue(null, session, qList, examId, sessionId);
        return qList;
    }

    // Publish hang doi len session/request cho JSP
    public static void publishCandidateQueue(HttpServletRequest request, HttpSession session,
            List<ExamRegistrationDTO> qList, int examId, int sessionId) {
        if (qList == null) {
            qList = new ArrayList<>();
        }
        List<ExamRegistrationDTO> active = filterActiveCallQueue(qList);
        List<ExamRegistrationDTO> done = listProcedureDoneNewestFirst(qList);

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
            request.setAttribute("currentSession", resolveCurrentSession(sessionId, examId, null));
        }
    }

    // Loc hang doi dang cho goi thu tuc
    public static List<ExamRegistrationDTO> filterActiveCallQueue(List<ExamRegistrationDTO> queue) {
        if (queue == null || queue.isEmpty()) {
            return new ArrayList<>();
        }
        List<ExamRegistrationDTO> active = new ArrayList<>();
        for (ExamRegistrationDTO c : queue) {
            if (isCallablePending(c)) {
                active.add(c);
            }
        }
        return active;
    }

    // Thi sinh con cho goi / lam thu tuc
    public static boolean isCallablePending(ExamRegistrationDTO c) {
        if (c == null) {
            return false;
        }
        if (c.isSuspended() || c.isAbsent()) {
            return false;
        }
        return !c.isProcedureComplete();
    }

    // Tim thi sinh theo SBD trong hang doi
    public static ExamRegistrationDTO findBySbd(List<ExamRegistrationDTO> queue, String sbd) {
        if (queue == null || sbd == null || sbd.isBlank()) {
            return null;
        }
        for (ExamRegistrationDTO c : queue) {
            if (sbd.equals(c.getSbd())) {
                return c;
            }
        }
        return null;
    }

    // Tim SBD ke tiep chua xong thu tuc
    public static String findNextPendingSbd(List<ExamRegistrationDTO> queue, String afterSbd) {
        if (queue == null || queue.isEmpty()) {
            return null;
        }
        boolean after = afterSbd == null || afterSbd.isBlank();
        for (ExamRegistrationDTO c : queue) {
            if (!isCallablePending(c)) {
                continue;
            }
            if (!after) {
                if (afterSbd.equals(c.getSbd())) {
                    after = true;
                }
                continue;
            }
            if (afterSbd != null && afterSbd.equals(c.getSbd())) {
                continue;
            }
            return c.getSbd();
        }
        if (!after && afterSbd != null) {
            return findNextPendingSbd(queue, null);
        }
        return null;
    }

    // Dua thi sinh callable len dau hang doi
    public static boolean moveCallableCandidateToFront(List<ExamRegistrationDTO> queue, String sbd) {
        if (queue == null || sbd == null || sbd.isBlank()) {
            return false;
        }
        for (int i = 0; i < queue.size(); i++) {
            ExamRegistrationDTO c = queue.get(i);
            if (!sbd.equals(c.getSbd()) || !isCallablePending(c)) {
                continue;
            }
            if (i == 0) {
                return true;
            }
            queue.remove(i);
            queue.add(0, c);
            return true;
        }
        return false;
    }

    // Dua thi sinh callable xuong cuoi hang doi
    public static boolean moveCallableCandidateToBottom(List<ExamRegistrationDTO> queue, String sbd) {
        if (queue == null || sbd == null || sbd.isBlank()) {
            return false;
        }
        for (int i = 0; i < queue.size(); i++) {
            ExamRegistrationDTO c = queue.get(i);
            if (!sbd.equals(c.getSbd()) || !isCallablePending(c)) {
                continue;
            }
            queue.remove(i);
            queue.add(c);
            return true;
        }
        return false;
    }

    // Luu thu tu goi thi sinh vao session
    public static void syncCallQueueOrderFromQueue(HttpSession session, int sessionId,
            List<ExamRegistrationDTO> queue) {
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

    // Danh sach thi sinh bi dinh chi trong ca
    public static List<ExamRegistrationDTO> listSuspendedInSession(List<ExamRegistrationDTO> queue) {
        if (queue == null || queue.isEmpty()) {
            return List.of();
        }
        List<ExamRegistrationDTO> suspended = new ArrayList<>();
        for (ExamRegistrationDTO c : queue) {
            if (c.isSuspended()) {
                suspended.add(c);
            }
        }
        return suspended;
    }

    // Thi sinh da xong thu tuc, moi nhat truoc
    public static List<ExamRegistrationDTO> listProcedureDoneNewestFirst(List<ExamRegistrationDTO> queue) {
        if (queue == null || queue.isEmpty()) {
            return List.of();
        }
        List<ExamRegistrationDTO> done = new ArrayList<>();
        for (ExamRegistrationDTO c : queue) {
            if (c.isProcedureComplete()) {
                done.add(c);
            }
        }
        done.sort(Comparator
                .comparing(ExamRegistrationDTO::getPresentMarkedAt,
                        Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(ExamRegistrationDTO::getSbd));
        return done;
    }

    // Tra cuu thi sinh theo SBD (queue truoc, DB sau)
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
        ExamRegistrationDAO regDAO = new ExamRegistrationDAOImpl();
        try {
            if (examId > 0) {
                ExamRegistrationDTO byExam = regDAO.getByExamAndSbd(examId, trimmed);
                if (byExam != null) {
                    return byExam;
                }
            }
            if (sessionId > 0) {
                return regDAO.getBySessionAndSbd(sessionId, trimmed);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Thi sinh dang duoc goi len ban thu tuc
    public static ExamRegistrationDTO resolveCallingCandidate(HttpSession session, List<ExamRegistrationDTO> qList) {
        if (qList == null || session == null) {
            return null;
        }
        String sbdParam = (String) session.getAttribute("callingSbd");
        if (sbdParam == null || sbdParam.isBlank()) {
            return null;
        }
        for (ExamRegistrationDTO c : qList) {
            if (!sbdParam.equals(c.getSbd())) {
                continue;
            }
            if (!c.isProcedureComplete()) {
                return c;
            }
            String nextSbd = findNextPendingSbd(qList, null);
            if (nextSbd != null) {
                session.setAttribute("callingSbd", nextSbd);
                return findBySbd(qList, nextSbd);
            }
            session.removeAttribute("callingSbd");
            return null;
        }
        return null;
    }

    // Dong bo callingSbd giua session va bang goi loa
    public static String syncCallingSbd(HttpSession session, ServletContext application,
            int sessionId, List<ExamRegistrationDTO> qList, boolean shiftEnded) {
        String callingSbd = session != null ? (String) session.getAttribute("callingSbd") : null;
        CandidateCallBoard.State callBoard = application != null
                ? CandidateCallBoard.getState(application, sessionId) : null;
        if (callBoard != null && callBoard.getCallingSbd() != null && !callBoard.getCallingSbd().isBlank()) {
            callingSbd = callBoard.getCallingSbd();
        }
        if (callingSbd != null && !callingSbd.isBlank() && qList != null) {
            ExamRegistrationDTO atDesk = findBySbd(qList, callingSbd);
            if (atDesk == null || atDesk.isProcedureComplete() || atDesk.isSuspended() || atDesk.isAbsent()) {
                callingSbd = null;
            }
        }
        if (session != null) {
            if (callingSbd != null && !callingSbd.isBlank()) {
                session.setAttribute("callingSbd", callingSbd);
            } else {
                session.removeAttribute("callingSbd");
            }
        }
        if (application != null) {
            CandidateCallBoard.sync(application, sessionId, callingSbd, qList, shiftEnded);
        }
        return callingSbd;
    }

    // Kiem tra ca goi thi da ket thuc
    public static boolean isCallShiftEnded(HttpSession session) {
        return session != null && "true".equals(session.getAttribute("shiftEnded"));
    }

    // Mo lai ca goi thi
    public static void resumeCallShift(ServletContext application, HttpSession session, int sessionId) {
        if (session != null) {
            session.removeAttribute("shiftEnded");
        }
        if (application != null && sessionId > 0) {
            CandidateCallBoard.State state = CandidateCallBoard.getState(application, sessionId);
            if (state != null) {
                state.setShiftEnded(false);
            }
        }
    }

    // Gan thuoc tinh trang goi thi / ban thu tuc
    public static void bindCandidateCallPageAttributes(HttpServletRequest request, ExamSessionDAO sessionDAO,
            HttpSession session, int examId, List<ExamRegistrationDTO> qList) {
        if (request == null) {
            return;
        }
        ExamRegistrationDTO calling = resolveCallingCandidate(session, qList);
        request.setAttribute("callingCandidate", calling);
        List<ExamRegistrationDTO> suspended = listSuspendedInSession(qList);
        request.setAttribute("suspendedCount", suspended.size());
        int sessionId = resolveSessionId(request, session, null, 0);
        request.setAttribute("currentSession", resolveCurrentSession(sessionId, examId, sessionDAO));
        request.setAttribute("selectedExamId", examId);
        request.setAttribute("selectedSessionId", sessionId > 0 ? sessionId : null);
    }

    // Gan thuoc tinh le phi thu tuc
    public static void bindProcedureFeeAttributes(HttpServletRequest request, ExamRegistrationDTO profile) {
        if (request == null || profile == null) {
            return;
        }
        String licenseCode = profile.getLicenseCode();
        if (licenseCode == null || licenseCode.isBlank()) {
            licenseCode = profile.getClazz();
        }
        boolean requiresRoadTest = profile.isRequiresRoadTest();
        FeeDAO feeDAO = new FeeDAOImpl();
        PaymentDAO payDAO = new PaymentDAOImpl();
        Payment payment = payDAO.getByCandidateId(profile.getId());
        List<Fee> feeLines = new ArrayList<>();
        boolean feesFromPayment = false;
        if (payment != null && payment.getId() > 0) {
            feeLines = feeDAO.getFeesByPaymentId(payment.getId());
            feesFromPayment = feeLines != null && !feeLines.isEmpty();
        }
        if (feeLines == null || feeLines.isEmpty()) {
            feeLines = feeDAO.getProcedureFees(licenseCode, requiresRoadTest);
            feesFromPayment = false;
        }
        double feeTotal = ProcedureFeeTotals.resolvePaidAmount(payment, feeLines);
        if (feeTotal <= 0) {
            feeTotal = feeDAO.sumProcedureFees(licenseCode, requiresRoadTest);
        }
        request.setAttribute("feeLines", feeLines);
        request.setAttribute("feeTotal", feeTotal);
        request.setAttribute("feesFromPayment", feesFromPayment);
    }

    // Gan thuoc tinh trang import DSTS
    public static void bindImportExamAttributes(HttpServletRequest request, SessionDTO currentSession, int examId) {
        if (request == null) {
            return;
        }
        if (currentSession == null && examId > 0) {
            int primarySessionId = resolvePrimarySessionId(loadAllSessions(new ExamSessionDAOImpl()), examId);
            if (primarySessionId > 0) {
                currentSession = new ExamSessionDAOImpl().getById(primarySessionId);
            }
        }
        if (currentSession != null) {
            request.setAttribute("currentSession", currentSession);
            if (currentSession.getLicenseCode() != null && !currentSession.getLicenseCode().isBlank()) {
                request.setAttribute("importExamLicense", currentSession.getLicenseCode());
            }
        }
        request.setAttribute("selectedExamId", examId);
    }

    // Gan sidebar exam staff neu chua co examOptions
    public static void bindSidebarIfNeeded(HttpServletRequest request, HttpSession session) {
        if (request == null) {
            return;
        }
        if (request.getAttribute("examOptions") != null) {
            return;
        }
        ExamSessionDAO sessionDAO = new ExamSessionDAOImpl();
        List<SessionDTO> allSessions = loadAllSessions(sessionDAO);
        int examId = resolveExamId(request, session, allSessions, 0);
        bindExamPickerAttributes(request, sessionDAO, allSessions, examId);
        if (session != null) {
            @SuppressWarnings("unchecked")
            List<SessionDTO> options = (List<SessionDTO>) request.getAttribute("examOptions");
            if (options != null) {
                session.setAttribute("examStaffExamOptions", options);
            }
        }
    }

    // Xoa trang thai thu tuc khi doi ngay thi
    public static void clearProcedureStateOnExamChange(HttpServletRequest request, HttpSession session,
            int previousExamId, Integer previousSessionId, int newExamId, int newSessionId) {
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

    // Chuyen flash message tu session sang request
    public static void consumeFlash(HttpSession session, String sessionKey, HttpServletRequest request,
            String requestKey) {
        if (session == null || request == null) {
            return;
        }
        Object value = session.getAttribute(sessionKey);
        if (value != null) {
            request.setAttribute(requestKey, value);
            session.removeAttribute(sessionKey);
        }
    }

    // Lay staff user id tu session
    public static int resolveStaffId(HttpSession session) {
        return SessionUserHelper.resolveUserId(session);
    }

    // Tao danh sach ky thi cho dropdown (1 ca dai dien / examId)
    public static List<SessionDTO> buildExamOptions(List<SessionDTO> allSessions) {
        LinkedHashMap<Integer, SessionDTO> examOptionMap = new LinkedHashMap<>();
        if (allSessions != null) {
            LinkedHashMap<Integer, SessionDTO> byId = new LinkedHashMap<>();
            for (SessionDTO s : allSessions) {
                if (s.getId() > 0) {
                    byId.put(s.getId(), s);
                }
            }
            for (SessionDTO s : allSessions) {
                if (s.getExamId() <= 0 || examOptionMap.containsKey(s.getExamId())) {
                    continue;
                }
                int primaryId = resolvePrimarySessionId(allSessions, s.getExamId());
                SessionDTO primary = primaryId > 0 ? byId.get(primaryId) : null;
                if (primary != null) {
                    examOptionMap.put(s.getExamId(), primary);
                }
            }
        }
        return new ArrayList<>(examOptionMap.values());
    }

    // Gia tri option picker (sessionId dai dien) cho ky thi
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

    // Sap xep ky thi cho sidebar
    public static List<SessionDTO> sortExamDaysForSidebar(List<SessionDTO> options) {
        if (options == null || options.isEmpty()) {
            return new ArrayList<>();
        }
        List<SessionDTO> sorted = new ArrayList<>(options);
        sorted.sort(Comparator
                .comparing(SessionDTO::getExamDate, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(s -> s.getLicenseCode() != null ? s.getLicenseCode() : "",
                        String.CASE_INSENSITIVE_ORDER)
                .thenComparing(SessionDTO::getId));
        return sorted;
    }

    // Muc dau tien tren dropdown ky thi sidebar
    public static SessionDTO firstPickerOption(List<SessionDTO> allSessions) {
        List<SessionDTO> options = sortExamDaysForSidebar(buildExamOptions(allSessions));
        return options.isEmpty() ? null : options.get(0);
    }

    // Ky thi mac dinh tu picker
    public static int resolveDefaultExamId(List<SessionDTO> allSessions) {
        SessionDTO first = firstPickerOption(allSessions);
        return first != null && first.getExamId() > 0 ? first.getExamId() : 0;
    }

    // Ca mac dinh tu picker
    public static int resolveDefaultSessionId(List<SessionDTO> allSessions) {
        SessionDTO first = firstPickerOption(allSessions);
        return first != null ? first.getId() : 0;
    }

    // Tim ca theo id trong danh sach
    public static SessionDTO findSessionById(List<SessionDTO> allSessions, int sessionId) {
        if (allSessions == null || sessionId <= 0) {
            return null;
        }
        for (SessionDTO s : allSessions) {
            if (s.getId() == sessionId) {
                return s;
            }
        }
        return null;
    }

    // Ca dai dien cho mot ky thi
    public static SessionDTO representativeSessionForExam(List<SessionDTO> allSessions, int examId,
            ExamSessionDAO sessionDAO) {
        if (examId <= 0) {
            return null;
        }
        List<SessionDTO> daySessions = sessionsForExam(allSessions, examId);
        if (!daySessions.isEmpty()) {
            return daySessions.get(0);
        }
        if (sessionDAO != null) {
            int primaryId = resolvePrimarySessionId(allSessions, examId);
            if (primaryId > 0) {
                return sessionDAO.getById(primaryId);
            }
        }
        return null;
    }

    // Tat ca ca trong cung ky thi (examId)
    public static List<SessionDTO> sessionsForExam(List<SessionDTO> allSessions, int examId) {
        if (allSessions == null || examId <= 0) {
            return List.of();
        }
        List<SessionDTO> result = new ArrayList<>();
        for (SessionDTO s : allSessions) {
            if (s.getExamId() == examId) {
                result.add(s);
            }
        }
        result.sort(Comparator
                .comparing(SessionDTO::getShiftStartTime, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(SessionDTO::getId));
        return result;
    }

    // Ca chinh (sessionId) cho ky thi
    public static int resolvePrimarySessionId(List<SessionDTO> allSessions, int examId) {
        if (examId <= 0) {
            return 0;
        }
        if (allSessions == null || allSessions.isEmpty()) {
            allSessions = loadAllSessions(new ExamSessionDAOImpl());
        }
        List<SessionDTO> daySessions = sessionsForExam(allSessions, examId);
        if (daySessions.isEmpty()) {
            return 0;
        }
        SessionDTO best = daySessions.get(0);
        for (SessionDTO s : daySessions) {
            if (s.getRegisteredCount() > best.getRegisteredCount()) {
                best = s;
            }
        }
        return best.getId();
    }

    // Doc ca tu request URL va cap nhat session
    public static SessionDTO resolveSessionFromRequest(HttpServletRequest request, HttpSession httpSession,
            ExamSessionDAO sessionDAO, List<SessionDTO> allSessions) {
        int sessionId = parseSessionIdParam(request);
        if (sessionId <= 0) {
            return null;
        }
        SessionDTO picked = resolveSessionById(sessionId, allSessions, sessionDAO);
        if (picked != null && picked.getExamId() > 0) {
            persistExamSelection(httpSession, sessionId, picked.getExamId());
            return picked;
        }
        return null;
    }

    // Redirect an toan ve path trong ung dung
    public static String resolveSafeRedirect(HttpServletRequest request, String fallbackPath) {
        String ctx = request != null ? request.getContextPath() : "";
        String candidate = fallbackPath != null ? fallbackPath : "/views/staff/examstaff/dashboard";
        if (request != null) {
            String redirect = request.getParameter("redirect");
            if (redirect != null && !redirect.isBlank()) {
                candidate = redirect.trim();
            } else {
                String referer = request.getHeader("Referer");
                if (referer != null && !referer.isBlank()) {
                    candidate = referer.trim();
                }
            }
        }
        return normalizeAppRedirectPath(candidate, ctx);
    }

    private static String normalizeAppRedirectPath(String candidate, String ctx) {
        if (candidate == null || candidate.isBlank()) {
            return joinContextPath(ctx, "/views/staff/examstaff/dashboard");
        }
        String path = candidate;
        int q = path.indexOf('?');
        String query = "";
        if (q >= 0) {
            query = path.substring(q);
            path = path.substring(0, q);
        }
        if (path.startsWith("http://") || path.startsWith("https://")) {
            try {
                java.net.URI uri = java.net.URI.create(path);
                if (uri.getPath() != null && !uri.getPath().isBlank()) {
                    path = uri.getPath();
                }
            } catch (Exception ignored) {
            }
        }
        String normalized = path;
        if (ctx != null && !ctx.isEmpty()) {
            if (path.startsWith(ctx + "/") || path.equals(ctx)) {
                normalized = path;
            } else if (path.startsWith("/")) {
                normalized = ctx + path;
            } else {
                normalized = ctx + "/" + path;
            }
        } else if (!path.startsWith("/")) {
            normalized = "/" + path;
        }
        return normalized + query;
    }

    private static String joinContextPath(String ctx, String path) {
        if (path == null || path.isBlank()) {
            return ctx != null ? ctx : "";
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (ctx == null || ctx.isEmpty()) {
            return path;
        }
        if (path.startsWith(ctx + "/") || path.equals(ctx)) {
            return path;
        }
        return ctx + path;
    }

    // Bo query string khoi URL
    public static String stripQueryString(String url) {
        if (url == null || url.isBlank()) {
            return url;
        }
        int q = url.indexOf('?');
        return q >= 0 ? url.substring(0, q) : url;
    }

    // Them hoac cap nhat query param
    public static String upsertQueryParam(String url, String key, String value) {
        if (url == null || url.isBlank() || key == null || key.isBlank()) {
            return url;
        }
        String base = stripQueryString(url);
        String fragment = "";
        int hash = base.indexOf('#');
        if (hash >= 0) {
            fragment = base.substring(hash);
            base = base.substring(0, hash);
        }
        Map<String, String> params = new LinkedHashMap<>();
        int q = url.indexOf('?');
        if (q >= 0) {
            int end = url.indexOf('#');
            String query = end >= 0 ? url.substring(q + 1, end) : url.substring(q + 1);
            for (String pair : query.split("&")) {
                if (pair.isBlank()) {
                    continue;
                }
                int eq = pair.indexOf('=');
                String k = eq >= 0 ? pair.substring(0, eq) : pair;
                String v = eq >= 0 ? pair.substring(eq + 1) : "";
                if (!key.equals(k)) {
                    params.put(k, v);
                }
            }
        }
        if (value != null) {
            params.put(key, value);
        }
        if (params.isEmpty()) {
            return base + fragment;
        }
        StringBuilder sb = new StringBuilder(base).append('?');
        boolean first = true;
        for (Map.Entry<String, String> e : params.entrySet()) {
            if (!first) {
                sb.append('&');
            }
            sb.append(e.getKey()).append('=').append(e.getValue());
            first = false;
        }
        sb.append(fragment);
        return sb.toString();
    }

    // Gan UTF-8 cho request
    private static void applyUtf8Request(HttpServletRequest request) {
        if (request == null) {
            return;
        }
        try {
            request.setCharacterEncoding(UTF_8);
        } catch (Exception ignored) {
        }
    }

    // Giai quyet examId + sessionId hien tai
    private static int[] resolveExamAndSessionForRequest(HttpServletRequest request, HttpSession session,
            ExamSessionDAO sessionDAO) {
        List<SessionDTO> allSessions = sessionDAO != null
                ? loadAllSessions(sessionDAO) : loadAllSessions(new ExamSessionDAOImpl());
        int sessionId = parseSessionIdParam(request);
        int examId = 0;
        if (sessionId > 0) {
            SessionDTO picked = resolveSessionById(sessionId, allSessions, sessionDAO);
            if (picked != null) {
                examId = picked.getExamId();
            }
        }
        if (examId <= 0 && session != null) {
            Integer storedExam = (Integer) session.getAttribute("selectedExamId");
            if (storedExam != null && storedExam > 0) {
                examId = storedExam;
            }
        }
        if (sessionId <= 0 && session != null) {
            Integer storedSession = (Integer) session.getAttribute("selectedSessionId");
            if (storedSession != null && storedSession > 0) {
                sessionId = storedSession;
            }
        }
        if (examId <= 0) {
            examId = resolveExamId(request, session, allSessions, 0);
        }
        if (sessionId <= 0 && examId > 0) {
            sessionId = resolvePrimarySessionId(allSessions, examId);
        }
        return new int[]{examId, sessionId};
    }

    // Work context khi chua co exam/session
    private static WorkContext resolveWorkContext(HttpServletRequest request, HttpSession session,
            ExamSessionDAO sessionDAO, List<SessionDTO> allSessions) {
        WorkContext work = new WorkContext();
        if (allSessions == null || allSessions.isEmpty()) {
            allSessions = loadAllSessions(sessionDAO);
        }
        SessionDTO first = firstPickerOption(allSessions);
        if (first != null) {
            work.examId = first.getExamId();
            work.sessionId = first.getId();
            persistExamSelection(session, work.sessionId, work.examId);
        }
        return work;
    }

    // Tai hang doi cho trang (hoac doc cache)
    private static List<ExamRegistrationDTO> resolveCandidatesForPage(HttpServletRequest request,
            HttpSession session, int examId, int sessionId, String webRoot, List<SessionDTO> allSessions,
            boolean loadCandidates) {
        if (!loadCandidates && session != null) {
            Integer loadedExam = (Integer) session.getAttribute("examStaffLoadedExamId");
            Integer loadedSession = (Integer) session.getAttribute("examStaffLoadedSessionId");
            if (loadedExam != null && loadedExam == examId
                    && (sessionId <= 0 || loadedSession == null || loadedSession == sessionId)) {
                @SuppressWarnings("unchecked")
                List<ExamRegistrationDTO> cached = (List<ExamRegistrationDTO>) session.getAttribute("candidateQueue");
                if (cached != null) {
                    return cached;
                }
            }
            return new ArrayList<>();
        }
        return refreshCandidateQueue(session, examId, sessionId, webRoot, allSessions);
    }

    // Gan examOptions + currentSession cho picker
    private static int bindExamPickerAttributes(HttpServletRequest request, ExamSessionDAO sessionDAO,
            List<SessionDTO> allSessions, int examId) {
        if (request == null) {
            return examId;
        }
        if (allSessions == null || allSessions.isEmpty()) {
            allSessions = loadAllSessions(sessionDAO);
        }
        List<SessionDTO> options = sortExamDaysForSidebar(buildExamOptions(allSessions));
        request.setAttribute("examOptions", options);

        int urlSessionId = parseSessionIdParam(request);
        SessionDTO current = null;
        if (urlSessionId > 0) {
            current = resolveSessionById(urlSessionId, allSessions, sessionDAO);
            if (current != null && current.getExamId() > 0) {
                examId = current.getExamId();
                request.setAttribute("selectedSessionId", urlSessionId);
            }
        } else if (examId > 0) {
            Integer sessionAttr = request.getAttribute("selectedSessionId") instanceof Integer
                    ? (Integer) request.getAttribute("selectedSessionId") : null;
            int sessionId = sessionAttr != null && sessionAttr > 0
                    ? sessionAttr
                    : resolvePrimarySessionId(allSessions, examId);
            current = resolveSessionById(sessionId, allSessions, sessionDAO);
            request.setAttribute("selectedSessionId", sessionId > 0 ? sessionId : null);
        }
        if (current == null && examId > 0) {
            current = representativeSessionForExam(allSessions, examId, sessionDAO);
        }
        if (current == null && !options.isEmpty()) {
            current = options.get(0);
            examId = current.getExamId();
        }
        request.setAttribute("currentSession", current);
        request.setAttribute("selectedExamId", examId);
        request.setAttribute("allSessions", allSessions);
        int committedSessionId = 0;
        if (urlSessionId > 0 && current != null && current.getExamId() == examId) {
            committedSessionId = urlSessionId;
        } else {
            committedSessionId = resolvePickerOptionSessionId(options, examId);
        }
        if (committedSessionId > 0) {
            request.setAttribute("pickerCommittedSessionId", committedSessionId);
        }
        if (examId > 0) {
            request.setAttribute("pickerCommittedExamId", examId);
        }
        return examId;
    }

    // Luu lua chon ky thi / ca vao session
    private static void persistExamSelection(HttpSession session, int sessionId, int examId) {
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

    // Tra cuu ca theo id (list hoac DAO)
    public static SessionDTO resolveSessionById(int sessionId, List<SessionDTO> allSessions,
            ExamSessionDAO sessionDAO) {
        if (sessionId <= 0) {
            return null;
        }
        SessionDTO found = findSessionById(allSessions, sessionId);
        if (found != null) {
            return found;
        }
        if (sessionDAO == null) {
            sessionDAO = new ExamSessionDAOImpl();
        }
        try {
            return sessionDAO.getById(sessionId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Lay SessionDTO hien tai cho header/JSP
    private static SessionDTO resolveCurrentSession(int sessionId, int examId, ExamSessionDAO sessionDAO) {
        if (sessionDAO == null) {
            sessionDAO = new ExamSessionDAOImpl();
        }
        if (sessionId > 0) {
            SessionDTO s = sessionDAO.getById(sessionId);
            if (s != null) {
                return s;
            }
        }
        if (examId > 0) {
            return representativeSessionForExam(loadAllSessions(sessionDAO), examId, sessionDAO);
        }
        return null;
    }

    // Ap dung thu tu goi thi da luu
    private static List<ExamRegistrationDTO> applyCallQueueOrder(HttpSession session, int sessionId,
            List<ExamRegistrationDTO> qList) {
        if (session == null || qList == null || qList.isEmpty()) {
            return qList;
        }
        Object orderAttr = session.getAttribute("callQueueOrder");
        Integer orderSession = (Integer) session.getAttribute("callQueueOrderSessionId");
        if (!(orderAttr instanceof List) || orderSession == null || orderSession != sessionId) {
            return qList;
        }
        @SuppressWarnings("unchecked")
        List<String> order = (List<String>) orderAttr;
        if (order.isEmpty()) {
            return qList;
        }
        Map<String, ExamRegistrationDTO> bySbd = new LinkedHashMap<>();
        for (ExamRegistrationDTO c : qList) {
            if (c != null && c.getSbd() != null) {
                bySbd.put(c.getSbd(), c);
            }
        }
        List<ExamRegistrationDTO> reordered = new ArrayList<>();
        for (String sbd : order) {
            ExamRegistrationDTO c = bySbd.remove(sbd);
            if (c != null) {
                reordered.add(c);
            }
        }
        reordered.addAll(bySbd.values());
        return reordered;
    }
}
