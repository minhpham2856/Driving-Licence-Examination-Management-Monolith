package controller.staff.exam;

import java.util.*;
import service.RegistrationService;
import service.impl.RegistrationServiceImpl;
import dao.AuditDAO;
import dao.impl.AuditDAOImpl;
import service.SessionService;
import service.impl.SessionServiceImpl;
import dto.SessionViewDTO;
import dto.EnrollmentDTO;
import enums.ExamSessionStatus;
import model.Audit;
import service.PhotoService;
import service.impl.PhotoServiceImpl;
import dto.CallBoardDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;
import controller.staff.exam.BaseStaffExamServlet;
import java.io.IOException;
import java.util.List;

@WebServlet("/views/staff/exam/candidatecall")
public class CandidateCallServlet extends BaseStaffExamServlet {

    private static final String CALL_BOARD_CONTEXT_KEY = "candidateCallBoards";
    private final RegistrationService regService = new RegistrationServiceImpl();
    private final AuditDAO auditDAO = new AuditDAOImpl();
    private final SessionService sessionService = new SessionServiceImpl();
    private final PhotoService photoService = new PhotoServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        if ("desk".equals(request.getParameter("view"))) {
            String deskSbd = request.getParameter("sbd");
            if (deskSbd == null || deskSbd.isBlank()) {
                deskSbd = (String) session.getAttribute("callingSbd");
            }
            if (deskSbd != null && !deskSbd.isBlank()) {
                response.sendRedirect("procedure?sbd=" + deskSbd);
            } else {
                response.sendRedirect("procedure");
            }
            return;
        }
        int examSessionId = readSessionId(request, session, sessionService);
        if (examSessionId > 0) {
            session.setAttribute("selectedSessionId", examSessionId);
        }
        SessionViewDTO SessionViewDTO = examSessionId > 0 ? sessionService.getSessionById(examSessionId) : null;
        String shiftEndedVal = (String) session.getAttribute("shiftEnded");
        boolean isShiftEnded = "true".equals(shiftEndedVal);
        if (SessionViewDTO != null && ExamSessionStatus.isEnded(SessionViewDTO.getStatus())) {
            isShiftEnded = true;
            session.setAttribute("shiftEnded", "true");
        }
        List<EnrollmentDTO> candidateQueue = null;
        String webRoot = request.getServletContext().getRealPath("/");
        if (!isShiftEnded) {
            candidateQueue = regService.getCandidatesBySession(examSessionId);
            photoService.normalizeQueue(webRoot, candidateQueue);
            session.setAttribute("candidateQueue", candidateQueue);
            session.setAttribute("lastLoadedSessionId", examSessionId);
        }
        // 2. Handle operations
        String qAction = request.getParameter("action");
        String qSbd = request.getParameter("sbd");
        List<EnrollmentDTO> permanentAbsents = (List<EnrollmentDTO>) session.getAttribute("permanentAbsents");
        if (permanentAbsents == null) {
            permanentAbsents = new ArrayList<>();
            session.setAttribute("permanentAbsents", permanentAbsents);
        }
        if ("startCall".equals(qAction)) {
            if (qSbd != null && !qSbd.isBlank()) {
                session.setAttribute("callingSbd", qSbd.trim());
            } else if (candidateQueue != null) {
                for (EnrollmentDTO c : candidateQueue) {
                    boolean isDone = c.isPaymentCompleted() && c.isValidCapturedPhoto();
                    if (!isDone) {
                        session.setAttribute("callingSbd", String.valueOf(c.getSbd()));
                        // Insert call record in database
                        Audit audit = new Audit();
                        audit.setUserId(resolveStaffUserId(session));
                        audit.setAction("CALL");
                        String entityId = c.getExamSessionId() + "-" + c.getCandidateNo();
                        String detail = "calledTo=Bàn làm thủ tục số 2;result=Calling";
                        audit.setReason(detail);
                        audit.setEntityName("Candidate");
                        audit.setEntityId(entityId);
                        audit.setNewValue(detail);
                        auditDAO.insert(audit);
                        break;
                    }
                }
            }
        } else if ("moveToBottom".equals(qAction) || "absent".equals(qAction) || "autoAbsent".equals(qAction)) {
            // UC-03 Normal Flow 3.0: Move candidate to bottom of the wait queue
            if (candidateQueue != null && qSbd != null) {
                int foundIdx = -1;
                for (int i = 0; i < candidateQueue.size(); i++) {
                    if (qSbd.equals(String.valueOf(candidateQueue.get(i).getSbd()))) {
                        foundIdx = i;
                        break;
                    }
                }
                if (foundIdx != -1) {
                    EnrollmentDTO removed = candidateQueue.remove(foundIdx);
                    candidateQueue.add(removed); // Move to the end of the queue
                    // Insert Call Record as Absent in DB
                    Audit audit = new Audit();
                    audit.setUserId(resolveStaffUserId(session));
                    audit.setAction("CALL");
                    String entityId = removed.getExamSessionId() + "-" + removed.getCandidateNo();
                    String detail = "calledTo=Bàn làm thủ tục số 2;result=Absent";
                    audit.setReason(detail);
                    audit.setEntityName("Candidate");
                    audit.setEntityId(entityId);
                    audit.setNewValue(detail);
                    auditDAO.insert(audit);
                }
                // Find next candidate who is not done
                String nextSbd = null;
                for (EnrollmentDTO c : candidateQueue) {
                    boolean isDone = c.isPaymentCompleted() && c.isValidCapturedPhoto();
                    if (!isDone && !String.valueOf(c.getSbd()).equals(qSbd)) {
                        nextSbd = String.valueOf(c.getSbd());
                        // Register a calling log for next person
                        Audit audit = new Audit();
                        audit.setUserId(3);
                        audit.setAction("CALL");
                        String entityId = c.getExamSessionId() + "-" + c.getCandidateNo();
                        String detail = "calledTo=Bàn làm thủ tục số 2;result=Calling";
                        audit.setReason(detail);
                        audit.setEntityName("Candidate");
                        audit.setEntityId(entityId);
                        audit.setNewValue(detail);
                        auditDAO.insert(audit);
                        break;
                    }
                }
                session.setAttribute("callingSbd", nextSbd);
                if ("autoAbsent".equals(qAction)) {
                    request.setAttribute("autoAbsentAlert", qSbd);
                } else {
                    request.setAttribute("absentAlert", qSbd);
                }
            }
        } else if ("permanentAbsent".equals(qAction)) {
            // UC-03 Alternative Flow 3.1: Confirm permanent absence (No-Show)
            if (candidateQueue != null && qSbd != null) {
                int foundIdx = -1;
                for (int i = 0; i < candidateQueue.size(); i++) {
                    if (qSbd.equals(String.valueOf(candidateQueue.get(i).getSbd()))) {
                        foundIdx = i;
                        break;
                    }
                }
                if (foundIdx != -1) {
                    EnrollmentDTO removed = candidateQueue.remove(foundIdx);
                    permanentAbsents.add(removed);
                    regService.updateScores(removed.getId(), 0, "failed", 0, "failed");
                    regService.markAbsent(removed.getId());
                    removed.setAbsent(true);
                    removed.setTheoryPassed("failed");
                    removed.setPracticalPassed("failed");
                    removed.setTheoryScore(0);
                    removed.setPracticalScore(0);
                }
                // Find next candidate who is not done
                String nextSbd = null;
                for (EnrollmentDTO c : candidateQueue) {
                    boolean isDone = c.isPaymentCompleted() && c.isValidCapturedPhoto();
                    if (!isDone && !String.valueOf(c.getSbd()).equals(qSbd)) {
                        nextSbd = String.valueOf(c.getSbd());
                        break;
                    }
                }
                session.setAttribute("callingSbd", nextSbd);
                request.setAttribute("permanentAbsentAlert", qSbd);
            }
        } else if ("undoAbsent".equals(qAction)) {
            // UC-03 Undo / Exception Safety action
            if (qSbd != null && permanentAbsents != null) {
                int foundIdx = -1;
                for (int i = 0; i < permanentAbsents.size(); i++) {
                    if (qSbd.equals(String.valueOf(permanentAbsents.get(i).getSbd()))) {
                        foundIdx = i;
                        break;
                    }
                }
                if (foundIdx != -1) {
                    EnrollmentDTO restored = permanentAbsents.remove(foundIdx);
                    // Reset fields
                    restored.setAbsent(false);
                    restored.setTheoryPassed("none");
                    restored.setPracticalPassed("none");
                    restored.setTheoryScore(null);
                    restored.setPracticalScore(null);
                    regService.clearAbsentMarking(restored.getId());
                    // Put back to queue
                    if (candidateQueue != null) {
                        candidateQueue.add(0, restored); // Put at the beginning so they can be called next!
                    }
                    session.setAttribute("callingSbd", String.valueOf(restored.getSbd())); // Set as active call immediately
                    request.setAttribute("undoAlert", qSbd);
                }
            }
        } else if ("endShift".equals(qAction)) {
            if (candidateQueue != null) {
                List<EnrollmentDTO> toRemove = new ArrayList<>();
                for (EnrollmentDTO c : candidateQueue) {
                    boolean isDone = c.isPaymentCompleted() && c.isValidCapturedPhoto();
                    if (!isDone) {
                        c.setAbsent(true);
                        c.setTheoryPassed("failed");
                        c.setPracticalPassed("failed");
                        c.setTheoryScore(0);
                        c.setPracticalScore(0);
                        regService.updateScores(c.getId(), 0, "failed", 0, "failed");
                        regService.markAbsent(c.getId());
                        if (permanentAbsents != null) {
                            permanentAbsents.add(c);
                        }
                        toRemove.add(c);
                    }
                }
                if (!toRemove.isEmpty()) {
                    candidateQueue.removeAll(toRemove);
                }
            }
            session.setAttribute("callingSbd", null);
            session.setAttribute("shiftEnded", "true");
        } else if ("startShift".equals(qAction)) {
            session.removeAttribute("shiftEnded");
            session.removeAttribute("candidateQueue");
            session.removeAttribute("permanentAbsents");
            response.sendRedirect("candidatecall");
            return;
        }
        advanceCallingIfDone(session, candidateQueue);
        CallBoardDTO state = getCallBoardState(examSessionId);
        if (state != null) {
            String callingSbd = (String) session.getAttribute("callingSbd");
            if (callingSbd != null) {
                state.setCallingSbd(callingSbd);
            }
            state.setShiftEnded("true".equals(session.getAttribute("shiftEnded")));
        }
        String callingSbd = (String) session.getAttribute("callingSbd");
        String nextSbd = null;
        if (candidateQueue != null) {
            if (callingSbd == null || callingSbd.isBlank()) {
                for (EnrollmentDTO c : candidateQueue) {
                    if (!(c.isPaymentCompleted() && c.isValidCapturedPhoto())) {
                        nextSbd = String.valueOf(c.getSbd());
                        break;
                    }
                }
            } else {
                boolean foundCurrent = false;
                for (EnrollmentDTO c : candidateQueue) {
                    if (foundCurrent) {
                        if (!(c.isPaymentCompleted() && c.isValidCapturedPhoto())) {
                            nextSbd = String.valueOf(c.getSbd());
                            break;
                        }
                    }
                    if (callingSbd.equals(String.valueOf(c.getSbd()))) {
                        foundCurrent = true;
                    }
                }
            }
        }
        EnrollmentDTO callingCandidate = resolveCallingCandidate(session, candidateQueue);
        if (callingCandidate != null) {
            request.setAttribute("callingCandidate", toCallView(callingCandidate, SessionViewDTO));
        }
        request.getRequestDispatcher("/views/staff/exam/candidatecall.jsp").forward(request, response);
    }

    private EnrollmentDTO resolveCallingCandidate(HttpSession session,
            List<EnrollmentDTO> candidateQueue) {
        String callingSbd = (String) session.getAttribute("callingSbd");
        if (callingSbd == null || callingSbd.isBlank() || candidateQueue == null) {
            return null;
        }
        for (EnrollmentDTO c : candidateQueue) {
            if (callingSbd.equals(String.valueOf(c.getSbd()))) {
                return c;
            }
        }
        return null;
    }

    private Map<String, Object> toCallView(EnrollmentDTO c, SessionViewDTO SessionViewDTO) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("sbd", String.valueOf(c.getSbd()));
        view.put("fullName", c.getFullName());
        view.put("cccd", c.getGovIdNo());
        view.put("licenseClass", SessionViewDTO != null && SessionViewDTO.getLicenseCode() != null
                ? "Hạng " + SessionViewDTO.getLicenseCode() : "Chưa rõ");
        view.put("shiftLabel", SessionViewDTO != null ? SessionViewDTO.getCaLabel() : "Chưa xếp khóa");
        view.put("faceMatchRate", "99.8%");
        return view;
    }

    private int resolveStaffUserId(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return user != null && user.getUserId() > 0 ? user.getUserId() : 0;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    private void advanceCallingIfDone(HttpSession session, List<EnrollmentDTO> candidateQueue) {
        if (candidateQueue == null) {
            return;
        }
        String callingSbd = (String) session.getAttribute("callingSbd");
        if (callingSbd == null || callingSbd.isBlank()) {
            return;
        }
        EnrollmentDTO current = null;
        for (EnrollmentDTO c : candidateQueue) {
            if (callingSbd.equals(String.valueOf(c.getSbd()))) {
                current = c;
                break;
            }
        }
        if (current == null || !(current.isPaymentCompleted() && current.isValidCapturedPhoto())) {
            return;
        }
        String nextSbd = null;
        for (EnrollmentDTO c : candidateQueue) {
            if (c.isPaymentCompleted() && c.isValidCapturedPhoto()) {
                continue;
            }
            nextSbd = String.valueOf(c.getSbd());
            break;
        }
        session.setAttribute("callingSbd", nextSbd);
    }

    @SuppressWarnings("unchecked")
    private CallBoardDTO getCallBoardState(int examSessionId) {
        if (examSessionId <= 0) {
            return null;
        }
        jakarta.servlet.ServletContext ctx = getServletContext();
        Map<Integer, CallBoardDTO> boards
                = (Map<Integer, CallBoardDTO>) ctx.getAttribute(CALL_BOARD_CONTEXT_KEY);
        if (boards == null) {
            synchronized (ctx) {
                boards = (Map<Integer, CallBoardDTO>) ctx.getAttribute(CALL_BOARD_CONTEXT_KEY);
                if (boards == null) {
                    boards = new HashMap<>();
                    ctx.setAttribute(CALL_BOARD_CONTEXT_KEY, boards);
                }
            }
        }
        return boards.computeIfAbsent(examSessionId, id -> new CallBoardDTO());
    }
}
