package Controllers.Staff.ExamStaff;

import Utils.ExamConstants;
import DAOs.ExamRegistrationDAO;
import DAOs.Impl.ExamRegistrationDAOImpl;
import DAOs.CandidateCallDAO;
import DAOs.ExamSessionDAO;
import DAOs.Impl.CandidateCallDAOImpl;
import DAOs.Impl.ExamSessionDAOImpl;
import DTOs.SessionDTO;
import DTOs.ExamRegistrationDTO;
import DTOs.CandidateCall;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.DriverManager;

@WebServlet("/views/staff/examstaff/candidatecall")
public class CandidateCallServlet extends HttpServlet {

    private final ExamRegistrationDAO regDAO = new ExamRegistrationDAOImpl();
    private final CandidateCallDAO callDAO = new CandidateCallDAOImpl();
    private final ExamSessionDAO sessionDAO = new ExamSessionDAOImpl();

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
        
        // 1. Luôn tải hàng đợi từ DB (đồng bộ với bàn thủ tục)
        int examSessionId = 2;
        Integer selectedSessionId = (Integer) session.getAttribute("selectedSessionId");
        if (selectedSessionId != null) {
            examSessionId = selectedSessionId;
        }

        String shiftEndedVal = (String) session.getAttribute("shiftEnded");
        boolean isShiftEnded = "true".equals(shiftEndedVal);
        SessionDTO SessionDTO = sessionDAO.getById(examSessionId);
        if (SessionDTO != null && ExamConstants.isSessionEnded(SessionDTO.getStatus())) {
            isShiftEnded = true;
            session.setAttribute("shiftEnded", "true");
        }
        
        List<ExamRegistrationDTO> candidateQueue = null;
        String webRoot = request.getServletContext().getRealPath("/");
        if (!isShiftEnded) {
            candidateQueue = regDAO.getCandidatesBySession(examSessionId);
            CandidatePhotoHelper.normalizeQueue(webRoot, candidateQueue, regDAO);
            session.setAttribute("candidateQueue", candidateQueue);
            session.setAttribute("lastLoadedSessionId", examSessionId);
        }
        
        // 2. Handle operations
        String qAction = request.getParameter("action");
        String qSbd = request.getParameter("sbd");
        
        List<ExamRegistrationDTO> permanentAbsents = (List<ExamRegistrationDTO>) session.getAttribute("permanentAbsents");
        if (permanentAbsents == null) {
            permanentAbsents = new java.util.ArrayList<>();
            session.setAttribute("permanentAbsents", permanentAbsents);
        }
        
        if ("startCall".equals(qAction)) {
            if (candidateQueue != null) {
                for (ExamRegistrationDTO c : candidateQueue) {
                    boolean isDone = c.isPaymentCompleted() && c.isValidCapturedPhoto();
                    if (!isDone) {
                        session.setAttribute("callingSbd", c.getSbd());
                        
                        // Insert call record in database
                        CandidateCall call = new CandidateCall();
                        call.setExamSessionId(c.getExamSessionId());
                        call.setCandidateNo(c.getCandidateNo());
                        call.setCalledTo("Bàn làm thủ tục số 2");
                        call.setCalledBy(3); // Default staff
                        call.setResult("Calling");
                        callDAO.insert(call);
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
                    ExamRegistrationDTO removed = candidateQueue.remove(foundIdx);
                    candidateQueue.add(removed); // Move to the end of the queue
                    
                    // Insert Call Record as Absent in DB
                    CandidateCall call = new CandidateCall();
                    call.setExamSessionId(removed.getExamSessionId());
                    call.setCandidateNo(removed.getCandidateNo());
                    call.setCalledTo("Bàn làm thủ tục số 2");
                    call.setCalledBy(3);
                    call.setResult("Absent");
                    callDAO.insert(call);
                }
                
                // Find next candidate who is not done
                String nextSbd = null;
                for (ExamRegistrationDTO c : candidateQueue) {
                    boolean isDone = c.isPaymentCompleted() && c.isValidCapturedPhoto();
                    if (!isDone && !c.getSbd().equals(qSbd)) {
                        nextSbd = c.getSbd();
                        
                        // Register a calling log for next person
                        CandidateCall call = new CandidateCall();
                        call.setExamSessionId(c.getExamSessionId());
                        call.setCandidateNo(c.getCandidateNo());
                        call.setCalledTo("Bàn làm thủ tục số 2");
                        call.setCalledBy(3);
                        call.setResult("Calling");
                        callDAO.insert(call);
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
                    ExamRegistrationDTO removed = candidateQueue.remove(foundIdx);
                    permanentAbsents.add(removed);
                    
                    regDAO.updateScores(removed.getId(), 0, "failed", 0, "failed");
                    regDAO.markAbsent(removed.getId());
                    
                    removed.setAbsent(true);
                    removed.setTheoryPassed("failed");
                    removed.setPracticalPassed("failed");
                    removed.setTheoryScore(0);
                    removed.setPracticalScore(0);
                }
                
                // Find next candidate who is not done
                String nextSbd = null;
                for (ExamRegistrationDTO c : candidateQueue) {
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
                    ExamRegistrationDTO restored = permanentAbsents.remove(foundIdx);
                    
                    // Reset fields
                    restored.setAbsent(false);
                    restored.setTheoryPassed("none");
                    restored.setPracticalPassed("none");
                    restored.setTheoryScore(null);
                    restored.setPracticalScore(null);
                    
                    regDAO.clearAbsentMarking(restored.getId());
                    
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
                java.util.List<ExamRegistrationDTO> toRemove = new java.util.ArrayList<>();
                for (ExamRegistrationDTO c : candidateQueue) {
                    boolean isDone = c.isPaymentCompleted() && c.isValidCapturedPhoto();
                    if (!isDone) {
                        c.setAbsent(true);
                        c.setTheoryPassed("failed");
                        c.setPracticalPassed("failed");
                        c.setTheoryScore(0);
                        c.setPracticalScore(0);
                        regDAO.updateScores(c.getId(), 0, "failed", 0, "failed");
                        regDAO.markAbsent(c.getId());
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
        CandidateCallBoard.syncFromSession(getServletContext(), session, candidateQueue);
        request.setAttribute("nextCallingCandidate",
                CandidateCallBoard.findBySbd(candidateQueue,
                        CandidateCallBoard.resolveNextSbd(candidateQueue,
                                (String) session.getAttribute("callingSbd"))));

        request.getRequestDispatcher("/views/staff/examstaff/candidatecall.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    private void advanceCallingIfDone(HttpSession session, List<ExamRegistrationDTO> candidateQueue) {
        if (candidateQueue == null) {
            return;
        }
        String callingSbd = (String) session.getAttribute("callingSbd");
        if (callingSbd == null || callingSbd.trim().isEmpty()) {
            return;
        }
        ExamRegistrationDTO current = null;
        for (ExamRegistrationDTO c : candidateQueue) {
            if (callingSbd.equals(c.getSbd())) {
                current = c;
                break;
            }
        }
        if (current == null || !(current.isPaymentCompleted() && current.isValidCapturedPhoto())) {
            return;
        }
        String nextSbd = null;
        for (ExamRegistrationDTO c : candidateQueue) {
            if (c.isPaymentCompleted() && c.isValidCapturedPhoto()) {
                continue;
            }
            nextSbd = c.getSbd();
            break;
        }
        session.setAttribute("callingSbd", nextSbd);
    }
}

