package Controllers.Staff.ExamStaff;

import DAO.ExamRegistrationDAO;
import DAO.Impl.ExamRegistrationDAOImpl;
import Models.ExamRegistration;
import Models.ExamSession;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

public final class ExamStaffViewHelper {

    private ExamStaffViewHelper() {
    }

    public static int resolveSessionId(HttpServletRequest request, HttpSession session, int defaultId) {
        String sessIdParam = request.getParameter("sessionId");
        if (sessIdParam != null && !sessIdParam.isEmpty()) {
            try {
                return Integer.parseInt(sessIdParam);
            } catch (NumberFormatException ignored) {
            }
        }
        Integer selected = (Integer) session.getAttribute("selectedSessionId");
        return selected != null ? selected : defaultId;
    }

    public static List<ExamSession> buildExamOptions(List<ExamSession> allSessions) {
        LinkedHashMap<Integer, ExamSession> examOptionMap = new LinkedHashMap<>();
        if (allSessions != null) {
            for (ExamSession s : allSessions) {
                if (s.getExamId() > 0 && !examOptionMap.containsKey(s.getExamId())) {
                    examOptionMap.put(s.getExamId(), s);
                }
            }
        }
        return new ArrayList<>(examOptionMap.values());
    }

    public static ExamSession findSessionById(List<ExamSession> allSessions, int sessionId) {
        if (allSessions == null) {
            return null;
        }
        for (ExamSession s : allSessions) {
            if (s.getId() == sessionId) {
                return s;
            }
        }
        return null;
    }

    public static void consumeFlash(HttpSession session, String sessionKey, HttpServletRequest request, String requestKey) {
        Object value = session.getAttribute(sessionKey);
        if (value != null) {
            request.setAttribute(requestKey, value);
            session.removeAttribute(sessionKey);
        }
    }

    public static int resolveStaffId(HttpSession session) {
        return Utils.SessionUserHelper.resolveUserId(session);
    }

    public static List<ExamRegistration> ensureCandidateQueue(HttpSession session, int sessionId, String webRoot) {
        List<ExamRegistration> qList = (List<ExamRegistration>) session.getAttribute("candidateQueue");
        if (qList == null) {
            ExamRegistrationDAO regDAO = new ExamRegistrationDAOImpl();
            try {
                qList = regDAO.getCandidatesBySession(sessionId);
            } catch (Exception e) {
                e.printStackTrace();
                qList = new ArrayList<>();
            }
            session.setAttribute("candidateQueue", qList);
        }
        if (qList != null && webRoot != null) {
            CandidatePhotoHelper.normalizeQueue(webRoot, qList, new ExamRegistrationDAOImpl());
        }
        return qList;
    }

    public static ExamRegistration resolveCallingCandidate(HttpSession session, List<ExamRegistration> qList) {
        if (qList == null) {
            return null;
        }
        String sbdParam = (String) session.getAttribute("callingSbd");
        if (sbdParam == null || sbdParam.trim().isEmpty()) {
            return null;
        }
        for (ExamRegistration c : qList) {
            if (!sbdParam.equals(c.getSbd())) {
                continue;
            }
            if (!c.isProcedureComplete()) {
                return c;
            }
            String nextSbd = null;
            for (ExamRegistration pending : qList) {
                if (!pending.isProcedureComplete()) {
                    nextSbd = pending.getSbd();
                    break;
                }
            }
            session.setAttribute("callingSbd", nextSbd);
            if (nextSbd == null) {
                return null;
            }
            for (ExamRegistration pending : qList) {
                if (nextSbd.equals(pending.getSbd())) {
                    return pending;
                }
            }
            return null;
        }
        return null;
    }

    public static String syncCallingSbd(HttpSession session, ServletContext application,
            int sessionId, List<ExamRegistration> qList, boolean shiftEnded) {
        String callingSbd = (String) session.getAttribute("callingSbd");
        CandidateCallBoard.State callBoard = CandidateCallBoard.getState(application, sessionId);
        if (callBoard != null && callBoard.getCallingSbd() != null && !callBoard.getCallingSbd().isBlank()) {
            callingSbd = callBoard.getCallingSbd();
        }
        if (callingSbd != null && !callingSbd.isBlank() && qList != null) {
            ExamRegistration atDesk = null;
            for (ExamRegistration c : qList) {
                if (callingSbd.equals(c.getSbd())) {
                    atDesk = c;
                    break;
                }
            }
            if (atDesk == null || atDesk.isProcedureComplete()) {
                callingSbd = null;
            }
        }
        if (callingSbd != null && !callingSbd.isBlank()) {
            session.setAttribute("callingSbd", callingSbd);
        } else {
            session.removeAttribute("callingSbd");
        }
        CandidateCallBoard.sync(application, sessionId, callingSbd, qList, shiftEnded);
        return callingSbd;
    }

    /** Thí sinh đã xong thủ tục, mới nhất trước (theo PaidAt; không có mốc thì xếp cuối). */
    public static List<ExamRegistration> listProcedureDoneNewestFirst(List<ExamRegistration> queue) {
        if (queue == null || queue.isEmpty()) {
            return List.of();
        }
        List<ExamRegistration> done = new ArrayList<>();
        for (ExamRegistration c : queue) {
            if (c.isProcedureComplete()) {
                done.add(c);
            }
        }
        done.sort(Comparator
                .comparing(ExamRegistration::getProcedureCompletedAt,
                        Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(ExamRegistration::getSbd));
        return done;
    }
}
