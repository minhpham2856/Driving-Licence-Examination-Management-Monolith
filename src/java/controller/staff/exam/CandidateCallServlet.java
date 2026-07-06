package controller.staff.exam;

import java.util.*;
import dto.payload.UpdateEnrollmentScoresCommand;
import service.ExamRegistrationService;
import service.impl.ExamRegistrationServiceImpl;
import dao.AuditDAO;
import dao.impl.AuditDAOImpl;
import service.ExamSessionControlService;
import service.impl.ExamSessionControlServiceImpl;
import dto.SessionDTO;
import dto.CandidateEnrollmentDTO;
import model.Audit;
import service.CandidatePhotoService;
import service.impl.CandidatePhotoServiceImpl;
import dto.CandidateCallBoardStateDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/views/staff/exam/candidatecall")
public class CandidateCallServlet extends HttpServlet {

    private static final String CALL_BOARD_CONTEXT_KEY = "candidateCallBoards";
    private final ExamSessionControlService sessionControlService = new ExamSessionControlServiceImpl();
    private final ExamRegistrationService regService = new ExamRegistrationServiceImpl();
    private final AuditDAO auditDAO = new AuditDAOImpl();
    private final ExamSessionControlService sessionService = new ExamSessionControlServiceImpl();
    private final CandidatePhotoService photoService = new CandidatePhotoServiceImpl();

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
        int examSessionId = 2;
        Integer selectedSessionId = (Integer) session.getAttribute("selectedSessionId");
        if (selectedSessionId != null) {
            examSessionId = selectedSessionId;
        }
        String shiftEndedVal = (String) session.getAttribute("shiftEnded");
        boolean isShiftEnded = "true".equals(shiftEndedVal);
        SessionDTO SessionDTO = sessionService.getSessionById(examSessionId);
        if (SessionDTO != null && isSessionEnded(SessionDTO.getStatus())) {
            isShiftEnded = true;
            session.setAttribute("shiftEnded", "true");
        }
        List<CandidateEnrollmentDTO> candidateQueue = null;
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
        List<CandidateEnrollmentDTO> permanentAbsents = (List<CandidateEnrollmentDTO>) session.getAttribute("permanentAbsents");
        if (permanentAbsents == null) {
            permanentAbsents = new ArrayList<>();
            session.setAttribute("permanentAbsents", permanentAbsents);
        }
        if ("startCall".equals(qAction)) {
            if (candidateQueue != null) {
                for (CandidateEnrollmentDTO c : candidateQueue) {
                    boolean isDone = c.isPaymentCompleted() && c.isValidCapturedPhoto();
                    if (!isDone) {
                        session.setAttribute("callingSbd", String.valueOf(c.getSbd()));
                        // Insert call record in database
                        Audit audit = new Audit();
                        audit.setUserId(3); // Default staff
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
                    CandidateEnrollmentDTO removed = candidateQueue.remove(foundIdx);
                    candidateQueue.add(removed); // Move to the end of the queue
                    // Insert Call Record as Absent in DB
                    Audit audit = new Audit();
                    audit.setUserId(3);
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
                for (CandidateEnrollmentDTO c : candidateQueue) {
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
                    CandidateEnrollmentDTO removed = candidateQueue.remove(foundIdx);
                    permanentAbsents.add(removed);
                    UpdateEnrollmentScoresCommand failScores = new UpdateEnrollmentScoresCommand();
                    failScores.setCandidateId(removed.getId());
                    failScores.setTheoryScore(0);
                    failScores.setTheoryResult("failed");
                    failScores.setPracticalScore(0);
                    failScores.setPracticalResult("failed");
                    regService.updateScores(failScores);
                    regService.markAbsent(removed.getId());
                    removed.setAbsent(true);
                    removed.setTheoryPassed("failed");
                    removed.setPracticalPassed("failed");
                    removed.setTheoryScore(0);
                    removed.setPracticalScore(0);
                }
                // Find next candidate who is not done
                String nextSbd = null;
                for (CandidateEnrollmentDTO c : candidateQueue) {
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
                    CandidateEnrollmentDTO restored = permanentAbsents.remove(foundIdx);
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
                List<CandidateEnrollmentDTO> toRemove = new ArrayList<>();
                for (CandidateEnrollmentDTO c : candidateQueue) {
                    boolean isDone = c.isPaymentCompleted() && c.isValidCapturedPhoto();
                    if (!isDone) {
                        c.setAbsent(true);
                        c.setTheoryPassed("failed");
                        c.setPracticalPassed("failed");
                        c.setTheoryScore(0);
                        c.setPracticalScore(0);
                        UpdateEnrollmentScoresCommand failScores = new UpdateEnrollmentScoresCommand();
                        failScores.setCandidateId(c.getId());
                        failScores.setTheoryScore(0);
                        failScores.setTheoryResult("failed");
                        failScores.setPracticalScore(0);
                        failScores.setPracticalResult("failed");
                        regService.updateScores(failScores);
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
        CandidateCallBoardStateDTO state = getCallBoardState(examSessionId);
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
                for (CandidateEnrollmentDTO c : candidateQueue) {
                    if (!(c.isPaymentCompleted() && c.isValidCapturedPhoto())) {
                        nextSbd = String.valueOf(c.getSbd());
                        break;
                    }
                }
            } else {
                boolean foundCurrent = false;
                for (CandidateEnrollmentDTO c : candidateQueue) {
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
        CandidateEnrollmentDTO nextCallingCandidate = null;
        if (nextSbd != null && candidateQueue != null) {
            for (CandidateEnrollmentDTO c : candidateQueue) {
                if (nextSbd.equals(String.valueOf(c.getSbd()))) {
                    nextCallingCandidate = c;
                    break;
                }
            }
        }
        request.setAttribute("nextCallingCandidate", nextCallingCandidate);
        request.getRequestDispatcher("/views/staff/exam/candidatecall.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    private void advanceCallingIfDone(HttpSession session, List<CandidateEnrollmentDTO> candidateQueue) {
        if (candidateQueue == null) {
            return;
        }
        String callingSbd = (String) session.getAttribute("callingSbd");
        if (callingSbd == null || callingSbd.isBlank()) {
            return;
        }
        CandidateEnrollmentDTO current = null;
        for (CandidateEnrollmentDTO c : candidateQueue) {
            if (callingSbd.equals(String.valueOf(c.getSbd()))) {
                current = c;
                break;
            }
        }
        if (current == null || !(current.isPaymentCompleted() && current.isValidCapturedPhoto())) {
            return;
        }
        String nextSbd = null;
        for (CandidateEnrollmentDTO c : candidateQueue) {
            if (c.isPaymentCompleted() && c.isValidCapturedPhoto()) {
                continue;
            }
            nextSbd = String.valueOf(c.getSbd());
            break;
        }
        session.setAttribute("callingSbd", nextSbd);
    }

    @SuppressWarnings("unchecked")
    private CandidateCallBoardStateDTO getCallBoardState(int examSessionId) {
        if (examSessionId <= 0) {
            return null;
        }
        jakarta.servlet.ServletContext ctx = getServletContext();
        Map<Integer, CandidateCallBoardStateDTO> boards
                = (Map<Integer, CandidateCallBoardStateDTO>) ctx.getAttribute(CALL_BOARD_CONTEXT_KEY);
        if (boards == null) {
            synchronized (ctx) {
                boards = (Map<Integer, CandidateCallBoardStateDTO>) ctx.getAttribute(CALL_BOARD_CONTEXT_KEY);
                if (boards == null) {
                    boards = new HashMap<>();
                    ctx.setAttribute(CALL_BOARD_CONTEXT_KEY, boards);
                }
            }
        }
        return boards.computeIfAbsent(examSessionId, id -> new CandidateCallBoardStateDTO());
    }
}
