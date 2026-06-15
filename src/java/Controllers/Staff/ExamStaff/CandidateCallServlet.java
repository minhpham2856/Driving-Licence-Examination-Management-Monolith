package Controllers.Staff.ExamStaff;

import Constants.ExamSessionStatus;
import DAO.ExamRegistrationDAO;
import DAO.Impl.ExamRegistrationDAOImpl;
import DAO.CandidateCallDAO;
import DAO.ExamSessionDAO;
import DAO.Impl.CandidateCallDAOImpl;
import DAO.Impl.ExamSessionDAOImpl;
import Models.ExamSession;
import Models.ExamRegistration;
import Models.CandidateCall;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        ExamSession examSession = sessionDAO.getById(examSessionId);
        if (examSession != null && ExamSessionStatus.isEnded(examSession.getStatus())) {
            isShiftEnded = true;
            session.setAttribute("shiftEnded", "true");
        }
        
        String qAction = request.getParameter("action");
        if (qAction == null) {
            qAction = "";
        }
        String qSbd = request.getParameter("sbd");

        String webRoot = request.getServletContext().getRealPath("/");
        List<ExamRegistration> candidateQueue = null;
        if (!isShiftEnded) {
            candidateQueue = loadCallQueue(session, examSessionId, webRoot, qAction);
        }
        
        List<ExamRegistration> permanentAbsents = (List<ExamRegistration>) session.getAttribute("permanentAbsents");
        if (permanentAbsents == null) {
            permanentAbsents = new java.util.ArrayList<>();
            session.setAttribute("permanentAbsents", permanentAbsents);
        }
        
        if ("startCall".equals(qAction)) {
            if (candidateQueue != null) {
                for (ExamRegistration c : candidateQueue) {
                    boolean isDone = c.isProcedureComplete();
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
                
                String nextSbd = resolveFirstPendingSbd(candidateQueue);
                session.setAttribute("callingSbd", nextSbd);
                if (nextSbd != null) {
                    ExamRegistration next = findBySbd(candidateQueue, nextSbd);
                    if (next != null) {
                        CandidateCall call = new CandidateCall();
                        call.setExamSessionId(next.getExamSessionId());
                        call.setCandidateNo(next.getCandidateNo());
                        call.setCalledTo("Bàn làm thủ tục số 2");
                        call.setCalledBy(3);
                        call.setResult("Calling");
                        callDAO.insert(call);
                    }
                }

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
                    
                    regDAO.updateScores(removed.getId(), removed.getExamSessionId(), 0, "failed", 0, "failed");
                    regDAO.markAbsent(removed.getId());
                    
                    removed.setNotes("Absent");
                    removed.setTheoryPassed("failed");
                    removed.setPracticalPassed("failed");
                    removed.setTheoryScore(0);
                    removed.setPracticalScore(0);
                }
                
                session.setAttribute("callingSbd", resolveFirstPendingSbd(candidateQueue));
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
                java.util.List<ExamRegistration> toRemove = new java.util.ArrayList<>();
                for (ExamRegistration c : candidateQueue) {
                    boolean isDone = c.isProcedureComplete();
                    if (!isDone) {
                        c.setNotes("Absent");
                        c.setTheoryPassed("failed");
                        c.setPracticalPassed("failed");
                        c.setTheoryScore(0);
                        c.setPracticalScore(0);
                        regDAO.updateScores(c.getId(), c.getExamSessionId(), 0, "failed", 0, "failed");
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
        if (candidateQueue != null) {
            session.setAttribute("candidateQueue", candidateQueue);
            session.setAttribute("lastLoadedSessionId", examSessionId);
        }
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

    private List<ExamRegistration> loadCallQueue(HttpSession session, int examSessionId,
            String webRoot, String qAction) {
        List<ExamRegistration> fresh = regDAO.getCandidatesBySession(examSessionId);
        CandidatePhotoHelper.normalizeQueue(webRoot, fresh, regDAO);

        if ("reloadQueue".equals(qAction) || "startShift".equals(qAction)) {
            session.setAttribute("candidateQueue", fresh);
            session.setAttribute("lastLoadedSessionId", examSessionId);
            return fresh;
        }

        List<ExamRegistration> existing = (List<ExamRegistration>) session.getAttribute("candidateQueue");
        Integer lastLoaded = (Integer) session.getAttribute("lastLoadedSessionId");
        List<ExamRegistration> merged;
        if (existing == null || lastLoaded == null || lastLoaded != examSessionId) {
            merged = fresh;
        } else {
            merged = mergeQueueOrder(existing, fresh);
        }
        session.setAttribute("candidateQueue", merged);
        session.setAttribute("lastLoadedSessionId", examSessionId);
        return merged;
    }

    private List<ExamRegistration> mergeQueueOrder(List<ExamRegistration> ordered,
            List<ExamRegistration> fresh) {
        Map<String, ExamRegistration> freshBySbd = new HashMap<>();
        for (ExamRegistration c : fresh) {
            freshBySbd.put(c.getSbd(), c);
        }
        List<ExamRegistration> merged = new ArrayList<>();
        for (ExamRegistration c : ordered) {
            ExamRegistration updated = freshBySbd.remove(c.getSbd());
            if (updated != null) {
                merged.add(updated);
            }
        }
        merged.addAll(freshBySbd.values());
        return merged;
    }

    private String resolveFirstPendingSbd(List<ExamRegistration> queue) {
        if (queue == null) {
            return null;
        }
        for (ExamRegistration c : queue) {
            if (!c.isProcedureComplete()) {
                return c.getSbd();
            }
        }
        return null;
    }

    private ExamRegistration findBySbd(List<ExamRegistration> queue, String sbd) {
        if (queue == null || sbd == null) {
            return null;
        }
        for (ExamRegistration c : queue) {
            if (sbd.equals(c.getSbd())) {
                return c;
            }
        }
        return null;
    }

    private void advanceCallingIfDone(HttpSession session, List<ExamRegistration> candidateQueue) {
        if (candidateQueue == null) {
            return;
        }
        String callingSbd = (String) session.getAttribute("callingSbd");
        if (callingSbd == null || callingSbd.trim().isEmpty()) {
            return;
        }
        ExamRegistration current = null;
        for (ExamRegistration c : candidateQueue) {
            if (callingSbd.equals(c.getSbd())) {
                current = c;
                break;
            }
        }
        if (current == null || !current.isProcedureComplete()) {
            return;
        }
        session.setAttribute("callingSbd", resolveFirstPendingSbd(candidateQueue));
    }
}

