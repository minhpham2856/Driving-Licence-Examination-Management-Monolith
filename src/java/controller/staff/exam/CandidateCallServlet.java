package controller.staff.exam;
import dto.*;
import model.*;

import java.util.*;

import service.ExamRegistrationService;

import service.impl.ExamRegistrationServiceImpl;

import service.AuditLogService;
import service.ExamSessionControlService;
import service.impl.AuditLogServiceImpl;
import service.impl.ExamSessionControlServiceImpl;

import dto.SessionDTO;

import dto.CandidateEnrollmentDTO;

import model.Audit;

import service.CandidatePhotoService;
import service.impl.CandidatePhotoServiceImpl;

import service.CandidateCallBoardService;
import service.impl.CandidateCallBoardServiceImpl;

import dto.CandidateCallBoardStateDTO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import service.EnumMappingService;
import service.impl.EnumMappingServiceImpl;

@WebServlet("/views/staff/exam/candidatecall")
public class CandidateCallServlet extends HttpServlet {

    private final EnumMappingService enumMappingService = new EnumMappingServiceImpl();

    private final ExamRegistrationService regService = new ExamRegistrationServiceImpl();
    private final AuditLogService auditService = new AuditLogServiceImpl();
    private final ExamSessionControlService sessionService = new ExamSessionControlServiceImpl();
    private final CandidatePhotoService photoService = new CandidatePhotoServiceImpl();
    private final CandidateCallBoardService callBoardService = new CandidateCallBoardServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();

        if ("desk".equals(request.getParameter("view"))) {
            String deskSbd = request.getParameter("sbd");
            if (deskSbd == null || deskSbd.trim().isEmpty()) {
                deskSbd = (String) session.getAttribute("callingSbd");
            }
            if (deskSbd != null && !deskSbd.trim().isEmpty()) {
                response.sendRedirect("procedure?sbd=" + deskSbd);
            } else {
                response.sendRedirect("procedure");
            }
            return;
        }

        // 1. LuÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â´n tÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚ÂºÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£i hÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â ng ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¾ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¹Ã…â€œÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â»ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â£i tÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â»ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â« DB (ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¾ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¹Ã…â€œÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â»ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã¢â‚¬Å“ng bÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â»ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¾Ãƒâ€šÃ‚Â¢ vÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â»ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚Âºi bÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â n thÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â»ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ tÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â»ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¥c)
        int examSessionId = 2;
        Integer selectedSessionId = (Integer) session.getAttribute("selectedSessionId");
        if (selectedSessionId != null) {
            examSessionId = selectedSessionId;
        }

        String shiftEndedVal = (String) session.getAttribute("shiftEnded");
        boolean isShiftEnded = "true".equals(shiftEndedVal);
        SessionDTO SessionDTO = sessionService.getSessionById(examSessionId);
        if (SessionDTO != null && enumMappingService.isSessionEnded(SessionDTO.getStatus())) {
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
                        session.setAttribute("callingSbd", c.getSbd());

                        // Insert call record in database
                        Audit audit = new Audit();
                        audit.setUserId(3); // Default staff
                        audit.setAction("CALL");
                        String entityId = c.getExamSessionId() + "-" + c.getCandidateNo();
                        String detail = "calledTo=BÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â n lÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â m thÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â»ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ tÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â»ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¥c sÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â»ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¹Ã…â€œ 2;result=Calling";
                        audit.setReason(detail);
                        audit.setEntityName("Candidate");
                        audit.setEntityId(entityId);
                        audit.setNewValue(detail);
                        auditService.insertAudit(audit);
                        break;
                    }
                }
            }
        } else if ("moveToBottom".equals(qAction) || "absent".equals(qAction) || "autoAbsent".equals(qAction)) {
            // UC-03 Normal Flow 3.0: Move candidate to bottom of the wait queue
            if (candidateQueue != null && qSbd != null) {
                int foundIdx = -1;
                for (int i = 0; i < candidateQueue.size(); i++) {
                    if (qSbd.equals(candidateQueue.get(i).getSbd())) {
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
                    String detail = "calledTo=BÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â n lÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â m thÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â»ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ tÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â»ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¥c sÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â»ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¹Ã…â€œ 2;result=Absent";
                    audit.setReason(detail);
                    audit.setEntityName("Candidate");
                    audit.setEntityId(entityId);
                    audit.setNewValue(detail);
                    auditService.insertAudit(audit);
                }

                // Find next candidate who is not done
                String nextSbd = null;
                for (CandidateEnrollmentDTO c : candidateQueue) {
                    boolean isDone = c.isPaymentCompleted() && c.isValidCapturedPhoto();
                    if (!isDone && !c.getSbd().equals(qSbd)) {
                        nextSbd = c.getSbd();

                        // Register a calling log for next person
                        Audit audit = new Audit();
                        audit.setUserId(3);
                        audit.setAction("CALL");
                        String entityId = c.getExamSessionId() + "-" + c.getCandidateNo();
                        String detail = "calledTo=BÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â n lÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â m thÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â»ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§ tÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â»ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¥c sÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â»ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¹Ã…â€œ 2;result=Calling";
                        audit.setReason(detail);
                        audit.setEntityName("Candidate");
                        audit.setEntityId(entityId);
                        audit.setNewValue(detail);
                        auditService.insertAudit(audit);
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
                    if (qSbd.equals(candidateQueue.get(i).getSbd())) {
                        foundIdx = i;
                        break;
                    }
                }
                if (foundIdx != -1) {
                    CandidateEnrollmentDTO removed = candidateQueue.remove(foundIdx);
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
                for (CandidateEnrollmentDTO c : candidateQueue) {
                    boolean isDone = c.isPaymentCompleted() && c.isValidCapturedPhoto();
                    if (!isDone && !c.getSbd().equals(qSbd)) {
                        nextSbd = c.getSbd();
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
                    if (qSbd.equals(permanentAbsents.get(i).getSbd())) {
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

                    session.setAttribute("callingSbd", restored.getSbd()); // Set as active call immediately
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
        CandidateCallBoardStateDTO state = callBoardService.getState(getServletContext(), examSessionId);
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
            if (callingSbd == null || callingSbd.trim().isEmpty()) {
                for (CandidateEnrollmentDTO c : candidateQueue) {
                    if (!(c.isPaymentCompleted() && c.isValidCapturedPhoto())) {
                        nextSbd = c.getSbd();
                        break;
                    }
                }
            } else {
                boolean foundCurrent = false;
                for (CandidateEnrollmentDTO c : candidateQueue) {
                    if (foundCurrent) {
                        if (!(c.isPaymentCompleted() && c.isValidCapturedPhoto())) {
                            nextSbd = c.getSbd();
                            break;
                        }
                    }
                    if (callingSbd.equals(c.getSbd())) {
                        foundCurrent = true;
                    }
                }
            }
        }

        CandidateEnrollmentDTO nextCallingCandidate = null;
        if (nextSbd != null && candidateQueue != null) {
            for (CandidateEnrollmentDTO c : candidateQueue) {
                if (nextSbd.equals(c.getSbd())) {
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
        if (callingSbd == null || callingSbd.trim().isEmpty()) {
            return;
        }
        CandidateEnrollmentDTO current = null;
        for (CandidateEnrollmentDTO c : candidateQueue) {
            if (callingSbd.equals(c.getSbd())) {
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
            nextSbd = c.getSbd();
            break;
        }
        session.setAttribute("callingSbd", nextSbd);
    }
}




