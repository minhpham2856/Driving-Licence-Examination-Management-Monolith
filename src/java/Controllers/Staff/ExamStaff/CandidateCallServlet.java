package Controllers.Staff.ExamStaff;

import DAO.ExamRegistrationDAO;
import DAO.Impl.ExamRegistrationDAOImpl;
import DAO.CandidateCallDAO;
import DAO.Impl.CandidateCallDAOImpl;
import Models.ExamRegistration;
import Models.CandidateCall;

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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        
        // 1. Initialize queue from CSDL if not exists
        String shiftEndedVal = (String) session.getAttribute("shiftEnded");
        boolean isShiftEnded = "true".equals(shiftEndedVal);
        
        List<ExamRegistration> candidateQueue = (List<ExamRegistration>) session.getAttribute("candidateQueue");
        if (candidateQueue == null && !isShiftEnded) {
            // Load pre-registered/walk-in candidates for B2 Session (id: 2) as default active session
            candidateQueue = regDAO.getCandidatesBySession(2);
            session.setAttribute("candidateQueue", candidateQueue);
            session.setAttribute("callingSbd", null);
        }
        
        // 2. Handle operations
        String qAction = request.getParameter("action");
        String qSbd = request.getParameter("sbd");
        
        List<ExamRegistration> permanentAbsents = (List<ExamRegistration>) session.getAttribute("permanentAbsents");
        if (permanentAbsents == null) {
            permanentAbsents = new java.util.ArrayList<>();
            session.setAttribute("permanentAbsents", permanentAbsents);
        }
        
        if ("startCall".equals(qAction)) {
            if (candidateQueue != null) {
                for (ExamRegistration c : candidateQueue) {
                    boolean isDone = c.isPaymentCompleted() && c.getPhotoUrl() != null && !c.getPhotoUrl().isEmpty();
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
                    ExamRegistration removed = candidateQueue.remove(foundIdx);
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
                for (ExamRegistration c : candidateQueue) {
                    boolean isDone = c.isPaymentCompleted() && c.getPhotoUrl() != null && !c.getPhotoUrl().isEmpty();
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
                    ExamRegistration removed = candidateQueue.remove(foundIdx);
                    permanentAbsents.add(removed);
                    
                    // Update database: theoryPassed = failed, practicalPassed = failed, notes = Absent
                    try (java.sql.Connection conn = java.sql.DriverManager.getConnection("jdbc:sqlserver://localhost:1433;databaseName=DLEM_DB;trustServerCertificate=true", "sa", "123")) {
                        // Mark scores failed in DB
                        regDAO.updateScores(removed.getId(), 0, "failed", 0, "failed");
                        
                        // Lock record by setting notes = 'Absent'
                        String sqlLock = "update ExamRegistration set notes = 'Absent' where id = ?";
                        try (PreparedStatement ps = conn.prepareStatement(sqlLock)) {
                            ps.setInt(1, removed.getId());
                            ps.executeUpdate();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    
                    removed.setNotes("Absent");
                    removed.setTheoryPassed("failed");
                    removed.setPracticalPassed("failed");
                    removed.setTheoryScore(0);
                    removed.setPracticalScore(0);
                }
                
                // Find next candidate who is not done
                String nextSbd = null;
                for (ExamRegistration c : candidateQueue) {
                    boolean isDone = c.isPaymentCompleted() && c.getPhotoUrl() != null && !c.getPhotoUrl().isEmpty();
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
                    ExamRegistration restored = permanentAbsents.remove(foundIdx);
                    
                    // Reset fields
                    restored.setNotes("");
                    restored.setTheoryPassed("none");
                    restored.setPracticalPassed("none");
                    restored.setTheoryScore(null);
                    restored.setPracticalScore(null);
                    
                    // Update DB to clear failures and notes
                    try (java.sql.Connection conn = java.sql.DriverManager.getConnection("jdbc:sqlserver://localhost:1433;databaseName=DLEM_DB;trustServerCertificate=true", "sa", "123")) {
                        // Delete scores
                        String delTheory = "delete from TheoryScore where examPaperId in (select id from ExamPaper where examRegistrationId = ?)";
                        String delPrac = "delete from PracticalScore where examRegistrationId = ?";
                        String delPaper = "delete from ExamPaper where examRegistrationId = ?";
                        String resetReg = "update ExamRegistration set notes = null where id = ?";
                        
                        try (PreparedStatement ps = conn.prepareStatement(delTheory)) {
                            ps.setInt(1, restored.getId());
                            ps.executeUpdate();
                        }
                        try (PreparedStatement ps = conn.prepareStatement(delPrac)) {
                            ps.setInt(1, restored.getId());
                            ps.executeUpdate();
                        }
                        try (PreparedStatement ps = conn.prepareStatement(delPaper)) {
                            ps.setInt(1, restored.getId());
                            ps.executeUpdate();
                        }
                        try (PreparedStatement ps = conn.prepareStatement(resetReg)) {
                            ps.setInt(1, restored.getId());
                            ps.executeUpdate();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    
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
                try (java.sql.Connection conn = java.sql.DriverManager.getConnection("jdbc:sqlserver://localhost:1433;databaseName=DLEM_DB;trustServerCertificate=true", "sa", "123")) {
                    // All remaining incomplete candidates in the queue will be marked as absent
                    String sqlLockNotes = "update ExamRegistration set notes = 'Absent' where id = ?";
                    try (PreparedStatement ps = conn.prepareStatement(sqlLockNotes)) {
                        java.util.List<ExamRegistration> toRemove = new java.util.ArrayList<>();
                        for (ExamRegistration c : candidateQueue) {
                            boolean isDone = c.isPaymentCompleted() && c.getPhotoUrl() != null && !c.getPhotoUrl().isEmpty();
                            if (!isDone) {
                                c.setNotes("Absent");
                                c.setTheoryPassed("failed");
                                c.setPracticalPassed("failed");
                                c.setTheoryScore(0);
                                c.setPracticalScore(0);
                                
                                ps.setInt(1, c.getId());
                                ps.addBatch();
                                
                                // Update database scores to failed (0)
                                regDAO.updateScores(c.getId(), 0, "failed", 0, "failed");
                                
                                if (permanentAbsents != null) {
                                    permanentAbsents.add(c);
                                }
                                toRemove.add(c);
                            }
                        }
                        if (!toRemove.isEmpty()) {
                            ps.executeBatch();
                            candidateQueue.removeAll(toRemove);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
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
        
        request.getRequestDispatcher("/views/staff/examstaff/candidatecall.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}

