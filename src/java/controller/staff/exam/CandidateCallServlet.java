package controller.staff.exam;

import service.ExamRegistrationService;

import service.impl.ExamRegistrationServiceImpl;

import dao.CandidateCallDAO;

import dao.ExamSessionDAO;

import dao.impl.CandidateCallDAOImpl;

import dao.impl.ExamSessionDAOImpl;

import dto.SessionDTO;

import dto.exam.ExamRegistrationDTO;

import dto.candidate.CandidateCallDTO;

import controller.staff.exam.CandidateCallBoard;
import service.CandidatePhotoService;
import service.impl.CandidatePhotoServiceImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/views/staff/examstaff/candidatecall")
public class CandidateCallServlet extends HttpServlet {

    private final ExamRegistrationService regDAO = new ExamRegistrationServiceImpl();
    private final CandidateCallDAO callDAO = new CandidateCallDAOImpl();
    private final ExamSessionDAO sessionDAO = new ExamSessionDAOImpl();

    // Xu ly yeu cau GET
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

        String webRoot = request.getServletContext().getRealPath("/");
        ExamStaffViewHelper.applyNoCacheHeaders(response);
        ExamStaffViewHelper.ExamStaffPageContext pageCtx = ExamStaffViewHelper.prepareExamStaffPage(
                request, session, sessionDAO, webRoot);
        int examId = pageCtx.getExamId();
        int boardSessionId = pageCtx.getSessionId();
        List<SessionDTO> allSessions = pageCtx.getAllSessions();

        boolean isShiftEnded = ExamStaffViewHelper.isCallShiftEnded(session);

        List<ExamRegistrationDTO> fullQueue = loadFullQueue(session, webRoot, examId, isShiftEnded, request);
        List<ExamRegistrationDTO> activeQueue = ExamStaffViewHelper.filterActiveCallQueue(fullQueue);

        String qAction = request.getParameter("action");
        String qSbd = request.getParameter("sbd");
        String returnView = request.getParameter("returnView");

        List<ExamRegistrationDTO> permanentAbsents = (List<ExamRegistrationDTO>) session.getAttribute("permanentAbsents");
        if (permanentAbsents == null) {
            permanentAbsents = new ArrayList<>();
            session.setAttribute("permanentAbsents", permanentAbsents);
        }

        if ("startCall".equals(qAction)) {
            if (activeQueue != null) {
                // promote caller
                String startSbd = ExamStaffViewHelper.resolveNextCallingSbd(fullQueue, null);
                promoteCaller(session, activeQueue, startSbd);
            }
        } else if ("moveToBottom".equals(qAction) || "absent".equals(qAction) || "autoAbsent".equals(qAction)) {
            if (fullQueue != null && qSbd != null) {
                ExamRegistrationDTO moved = ExamStaffViewHelper.findBySbd(fullQueue, qSbd);
                if (moved != null
                        && ExamStaffViewHelper.moveCallableCandidateToBottom(fullQueue, qSbd)) {
                    ExamStaffViewHelper.syncCallQueueOrderFromQueue(session, boardSessionId, fullQueue);
                    activeQueue = ExamStaffViewHelper.filterActiveCallQueue(fullQueue);

                    CandidateCallDTO call = new CandidateCallDTO();
                    call.setExamSessionId(moved.getExamSessionId());
                    call.setCandidateNo(moved.getCandidateNo());
                    call.setCalledTo("Bàn làm thủ tục số 2");
                    call.setCalledBy(3);
                    call.setResult("Absent");
                    callDAO.insert(call);
                }
                // promote caller

                String nextSbd = ExamStaffViewHelper.resolveNextCallingSbd(fullQueue, qSbd);
                promoteCaller(session, activeQueue, nextSbd);
                CandidateCallBoard.sync(getServletContext(), boardSessionId, nextSbd, fullQueue,
                        "true".equals(session.getAttribute("shiftEnded")));

                if ("autoAbsent".equals(qAction)) {
                    request.setAttribute("autoAbsentAlert", qSbd);
                } else {
                    request.setAttribute("absentAlert", qSbd);
                }
            }
        } else if ("permanentAbsent".equals(qAction)) {
            ExamRegistrationDTO removed = ExamStaffViewHelper.findBySbd(fullQueue, qSbd);
            if (removed != null) {
                regDAO.updateScores(removed.getId(), 0, "failed", 0, "failed");
                regDAO.markSuspended(removed.getId());
                regDAO.markAbsent(removed.getId());

                fullQueue = loadFullQueue(session, webRoot, examId, isShiftEnded, request);
                // promote caller
                activeQueue = ExamStaffViewHelper.filterActiveCallQueue(fullQueue);

                String nextSbd = ExamStaffViewHelper.resolveNextCallingSbd(fullQueue, qSbd);
                promoteCaller(session, activeQueue, nextSbd);
                request.setAttribute("permanentAbsentAlert", qSbd);
            } else {
                fullQueue = loadFullQueue(session, webRoot, examId, isShiftEnded, request);
                activeQueue = ExamStaffViewHelper.filterActiveCallQueue(fullQueue);
            }
        } else if ("undoAbsent".equals(qAction)) {
            ExamRegistrationDTO restored = ExamStaffViewHelper.findBySbd(fullQueue, qSbd);
            if (restored == null && permanentAbsents != null) {
                for (int i = 0; i < permanentAbsents.size(); i++) {
                    if (qSbd.equals(permanentAbsents.get(i).getSbd())) {
                        restored = permanentAbsents.remove(i);
                        break;
                    }
                }
            }
            if (restored != null && (restored.isSuspended() || restored.isAbsent())) {
                regDAO.undoSuspension(restored.getId());
                regDAO.clearAbsentMarking(restored.getId());

                restored.setSuspended(false);
                restored.setAbsent(false);
                restored.setTheoryPassed("none");
                restored.setPracticalPassed("none");
                restored.setTheoryScore(null);
                restored.setPracticalScore(null);

                fullQueue = loadFullQueue(session, webRoot, examId, isShiftEnded, request);
                activeQueue = ExamStaffViewHelper.filterActiveCallQueue(fullQueue);
                ExamRegistrationDTO fresh = ExamStaffViewHelper.findBySbd(fullQueue, qSbd);
                if (fresh != null) {
                    ExamStaffViewHelper.moveCallableCandidateToFront(fullQueue, qSbd);
                    ExamStaffViewHelper.syncCallQueueOrderFromQueue(session, boardSessionId, fullQueue);
                    activeQueue = ExamStaffViewHelper.filterActiveCallQueue(fullQueue);
                }

                session.setAttribute("callingSbd", qSbd);
                request.setAttribute("undoAlert", qSbd);
            }
        } else if ("endShift".equals(qAction)) {
            if (activeQueue != null) {
                List<ExamRegistrationDTO> toRemove = new ArrayList<>();
                for (ExamRegistrationDTO c : activeQueue) {
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
                    activeQueue.removeAll(toRemove);
                }
            }
            session.setAttribute("callingSbd", null);
            session.setAttribute("shiftEnded", "true");
            fullQueue = loadFullQueue(session, webRoot, examId, true, request);
            activeQueue = ExamStaffViewHelper.filterActiveCallQueue(fullQueue);
        } else if ("startShift".equals(qAction)) {
            ExamStaffViewHelper.resumeCallShift(getServletContext(), session, boardSessionId);
        // advance calling if done
            response.sendRedirect(request.getContextPath() + "/views/staff/examstaff/candidatecall");
            return;
        }

        advanceCallingIfDone(session, activeQueue);
        boolean shiftEndedNow = "true".equals(session.getAttribute("shiftEnded"));
        String callingSbdForBoard = ExamStaffViewHelper.syncCallingSbd(
                session, getServletContext(), boardSessionId, activeQueue, shiftEndedNow);
        CandidateCallBoard.sync(getServletContext(), boardSessionId, callingSbdForBoard, activeQueue, shiftEndedNow);

        ExamStaffViewHelper.bindCandidateCallPageAttributes(request, sessionDAO, session, examId, fullQueue);
        ExamStaffViewHelper.publishCandidateQueue(request, session, fullQueue, examId, boardSessionId);

        if ("suspended".equals(request.getParameter("view")) || "suspended".equals(returnView)) {
            request.setAttribute("suspendedList", ExamStaffViewHelper.listSuspendedInSession(fullQueue));
            request.getRequestDispatcher("/views/staff/examstaff/candidate-suspended.jsp").forward(request, response);
            return;
        }

        String callingSbd = callingSbdForBoard;
        String nextSbd = ExamStaffViewHelper.resolveNextCallingSbd(fullQueue, callingSbd);

        ExamRegistrationDTO nextCallingCandidate = ExamStaffViewHelper.findBySbd(activeQueue, nextSbd);
    // promote caller
        request.setAttribute("nextCallingCandidate", nextCallingCandidate);

        request.getRequestDispatcher("/views/staff/examstaff/candidatecall.jsp").forward(request, response);
    }

    private void promoteCaller(HttpSession session, List<ExamRegistrationDTO> activeQueue, String nextSbd) {
        if (nextSbd != null && !nextSbd.isBlank()) {
            session.setAttribute("callingSbd", nextSbd);
            ExamRegistrationDTO next = ExamStaffViewHelper.findBySbd(activeQueue, nextSbd);
            if (next != null) {
                CandidateCallDTO call = new CandidateCallDTO();
                call.setExamSessionId(next.getExamSessionId());
                call.setCandidateNo(next.getCandidateNo());
                call.setCalledTo("Bàn làm thủ tục số 2");
                call.setCalledBy(3);
                call.setResult("Calling");
                callDAO.insert(call);
    // Tai full queue
            }
        } else {
            session.removeAttribute("callingSbd");
        }
    }

    private List<ExamRegistrationDTO> loadFullQueue(HttpSession session, String webRoot,
            int examId, boolean shiftEnded, HttpServletRequest request) {
        if (shiftEnded) {
            Integer lastLoadedExam = (Integer) session.getAttribute("lastLoadedExamId");
            if (lastLoadedExam != null && lastLoadedExam == examId) {
                @SuppressWarnings("unchecked")
                List<ExamRegistrationDTO> cached = (List<ExamRegistrationDTO>) session.getAttribute("candidateQueue");
                if (cached != null && !cached.isEmpty()) {
                    return cached;
                }
            }
        }
        List<SessionDTO> allSessions = sessionDAO.getAllSessions();
        List<ExamRegistrationDTO> queue = ExamStaffViewHelper.refreshCandidateQueue(session, examId, webRoot, allSessions);
    // Xu ly yeu cau POST
        ExamStaffViewHelper.publishCandidateQueue(request, session, queue, examId,
                ExamStaffViewHelper.resolvePrimarySessionId(allSessions, examId));
        // Xu ly yeu cau GET
        session.setAttribute("lastLoadedSessionId",
                ExamStaffViewHelper.resolvePrimarySessionId(allSessions, examId));
    // advance calling if done
        return queue;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    private void advanceCallingIfDone(HttpSession session, List<ExamRegistrationDTO> candidateQueue) {
        if (candidateQueue == null) {
            return;
        // promote caller
        }
        String callingSbd = (String) session.getAttribute("callingSbd");
        if (callingSbd == null || callingSbd.trim().isEmpty()) {
            return;
        }
        ExamRegistrationDTO current = ExamStaffViewHelper.findBySbd(candidateQueue, callingSbd);
        if (current == null || current.isSuspended() || !current.isProcedureComplete()) {
            return;
        }
        String nextSbd = ExamStaffViewHelper.resolveNextCallingSbd(candidateQueue, callingSbd);
        List<ExamRegistrationDTO> activeQueue = ExamStaffViewHelper.filterActiveCallQueue(candidateQueue);
        promoteCaller(session, activeQueue, nextSbd);
    }
}
