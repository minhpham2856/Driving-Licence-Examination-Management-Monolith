package controller.staff.examstaff;

import dto.EnrollmentDTO;
import dto.ServiceResult;
import enums.ExamStatus;
import model.Exam;
import model.User;
import service.CallService;
import service.ExamService;
import service.RegistrationService;
import service.impl.CallServiceImpl;
import service.impl.ExamServiceImpl;
import service.impl.RegistrationServiceImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/staff/examstaff/candidate-call")
public class CandidateCallServlet extends HttpServlet {

    // Branch called this "Bàn làm thủ tục số 2"; keep a neutral procedure desk label.
    private static final String PROCEDURE_DESK = "Bàn làm thủ tục";

    private final CallService callService = new CallServiceImpl();
    private final RegistrationService registrationService = new RegistrationServiceImpl();
    private final ExamService examService = new ExamServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();

        // "desk" view: jump straight to the procedure desk for the active candidate.
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

        // Resolve the active exam session (mirrors branch default of session 2).
        int examId = 2;
        Integer selectedExamId = (Integer) session.getAttribute("selectedExamId");
        if (selectedExamId != null) {
            examId = selectedExamId;
        }

        // Detect shift ended: session flag OR DB session status.
        boolean isShiftEnded = "true".equals(session.getAttribute("shiftEnded"));
        Exam sessionDto = examService.getById(examId);
        if (sessionDto != null && ExamStatus.isEnded(sessionDto.getStatus())) {
            isShiftEnded = true;
            session.setAttribute("shiftEnded", "true");
        }
        request.setAttribute("currentExam", sessionDto);

        // Load the candidate queue from DB while the shift is active.
        List<EnrollmentDTO> candidateQueue = null;
        if (!isShiftEnded) {
            candidateQueue = registrationService.getCandidatesByExam(examId);
            session.setAttribute("candidateQueue", candidateQueue);
            session.setAttribute("lastLoadedExamId", examId);
        }

        // Permanent-absent list lives in the session for undo support.
        List<EnrollmentDTO> permanentAbsents = (List<EnrollmentDTO>) session.getAttribute("permanentAbsents");
        if (permanentAbsents == null) {
            permanentAbsents = new ArrayList<>();
            session.setAttribute("permanentAbsents", permanentAbsents);
        }

        User user = (User) session.getAttribute("user");
        Integer actionUserId = (user != null) ? user.getUserId() : null;

        String qAction = request.getParameter("action");
        String qSbd = request.getParameter("sbd");

        if ("startCall".equals(qAction)) {
            if (candidateQueue != null) {
                for (EnrollmentDTO c : candidateQueue) {
                    if (isProcedureDone(c)) {
                        continue;
                    }
                    session.setAttribute("callingSbd", String.valueOf(c.getCandidateNumber()));
                    callService.recordProcedureCall(examId, c.getCandidateNumber(), "Calling",
                            PROCEDURE_DESK, actionUserId);
                    break;
                }
            }
        } else if ("moveToBottom".equals(qAction) || "absent".equals(qAction) || "autoAbsent".equals(qAction)) {
            if (candidateQueue != null && qSbd != null) {
                int foundIdx = indexOfSbd(candidateQueue, qSbd);
                if (foundIdx != -1) {
                    EnrollmentDTO removed = candidateQueue.remove(foundIdx);
                    candidateQueue.add(removed);
                    callService.recordProcedureCall(examId, removed.getCandidateNumber(), "Absent",
                            PROCEDURE_DESK, actionUserId);
                }
                String nextSbd = null;
                for (EnrollmentDTO c : candidateQueue) {
                    if (isProcedureDone(c) || qSbd.equals(String.valueOf(c.getCandidateNumber()))) {
                        continue;
                    }
                    nextSbd = String.valueOf(c.getCandidateNumber());
                    callService.recordProcedureCall(examId, c.getCandidateNumber(), "Calling",
                            PROCEDURE_DESK, actionUserId);
                    break;
                }
                session.setAttribute("callingSbd", nextSbd);
                if ("autoAbsent".equals(qAction)) {
                    request.setAttribute("autoAbsentAlert", qSbd);
                } else {
                    request.setAttribute("absentAlert", qSbd);
                }
            }
        } else if ("permanentAbsent".equals(qAction)) {
            if (candidateQueue != null && qSbd != null) {
                int foundIdx = indexOfSbd(candidateQueue, qSbd);
                if (foundIdx != -1) {
                    EnrollmentDTO removed = candidateQueue.remove(foundIdx);
                    permanentAbsents.add(removed);
                    registrationService.updateScores(removed.getId(), 0, "failed", 0, "failed");
                    registrationService.markAbsent(removed.getId());
                    removed.setNotes("Absent");
                    removed.setTheoryPassed("failed");
                    removed.setPracticalPassed("failed");
                    removed.setTheoryScore(0);
                    removed.setPracticalScore(0);
                }
                String nextSbd = null;
                for (EnrollmentDTO c : candidateQueue) {
                    if (isProcedureDone(c) || qSbd.equals(String.valueOf(c.getCandidateNumber()))) {
                        continue;
                    }
                    nextSbd = String.valueOf(c.getCandidateNumber());
                    break;
                }
                session.setAttribute("callingSbd", nextSbd);
                request.setAttribute("permanentAbsentAlert", qSbd);
            }
        } else if ("undoAbsent".equals(qAction)) {
            if (qSbd != null && permanentAbsents != null) {
                int foundIdx = indexOfSbd(permanentAbsents, qSbd);
                if (foundIdx != -1) {
                    EnrollmentDTO restored = permanentAbsents.remove(foundIdx);
                    restored.setNotes("");
                    restored.setTheoryPassed("none");
                    restored.setPracticalPassed("none");
                    restored.setTheoryScore(null);
                    restored.setPracticalScore(null);
                    registrationService.clearAbsentMarking(restored.getId());
                    if (candidateQueue != null) {
                        candidateQueue.add(0, restored);
                    }
                    session.setAttribute("callingSbd", String.valueOf(restored.getCandidateNumber()));
                    request.setAttribute("undoAlert", qSbd);
                }
            }
        } else if ("endShift".equals(qAction)) {
            if (candidateQueue != null) {
                List<EnrollmentDTO> toRemove = new ArrayList<>();
                for (EnrollmentDTO c : candidateQueue) {
                    if (isProcedureDone(c)) {
                        continue;
                    }
                    c.setNotes("Absent");
                    c.setTheoryPassed("failed");
                    c.setPracticalPassed("failed");
                    c.setTheoryScore(0);
                    c.setPracticalScore(0);
                    registrationService.updateScores(c.getId(), 0, "failed", 0, "failed");
                    registrationService.markAbsent(c.getId());
                    if (permanentAbsents != null) {
                        permanentAbsents.add(c);
                    }
                    toRemove.add(c);
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
            response.sendRedirect("candidate-call");
            return;
        }

        advanceCallingIfDone(session, candidateQueue);

        String callingSbd = (String) session.getAttribute("callingSbd");
        request.setAttribute("callingCandidate", findBySbd(candidateQueue, callingSbd));
        request.setAttribute("candidateQueue", candidateQueue);
        request.setAttribute("permanentAbsents", permanentAbsents);
        request.setAttribute("nextCallingCandidate",
                findBySbd(candidateQueue, resolveNextSbd(candidateQueue, callingSbd)));

        request.getRequestDispatcher("/views/staff/examstaff/candidatecall.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    private boolean isProcedureDone(EnrollmentDTO c) {
        return c.isPaymentCompleted() && c.isValidCapturedPhoto();
    }

    private int indexOfSbd(List<EnrollmentDTO> list, String sbd) {
        if (list == null || sbd == null) {
            return -1;
        }
        for (int i = 0; i < list.size(); i++) {
            if (sbd.equals(String.valueOf(list.get(i).getCandidateNumber()))) {
                return i;
            }
        }
        return -1;
    }

    private EnrollmentDTO findBySbd(List<EnrollmentDTO> queue, String sbd) {
        if (queue == null || sbd == null || sbd.trim().isEmpty()) {
            return null;
        }
        for (EnrollmentDTO c : queue) {
            if (sbd.equals(String.valueOf(c.getCandidateNumber()))) {
                return c;
            }
        }
        return null;
    }

    private String resolveNextSbd(List<EnrollmentDTO> queue, String callingSbd) {
        if (queue == null || queue.isEmpty()) {
            return null;
        }
        boolean afterCalling = (callingSbd == null || callingSbd.trim().isEmpty());
        for (EnrollmentDTO c : queue) {
            if (isProcedureDone(c)) {
                continue;
            }
            if (!afterCalling) {
                if (callingSbd.equals(String.valueOf(c.getCandidateNumber()))) {
                    afterCalling = true;
                }
                continue;
            }
            return String.valueOf(c.getCandidateNumber());
        }
        return null;
    }

    private void advanceCallingIfDone(HttpSession session, List<EnrollmentDTO> candidateQueue) {
        if (candidateQueue == null) {
            return;
        }
        String callingSbd = (String) session.getAttribute("callingSbd");
        if (callingSbd == null || callingSbd.trim().isEmpty()) {
            return;
        }
        EnrollmentDTO current = findBySbd(candidateQueue, callingSbd);
        if (current == null || !isProcedureDone(current)) {
            return;
        }
        String nextSbd = null;
        for (EnrollmentDTO c : candidateQueue) {
            if (isProcedureDone(c)) {
                continue;
            }
            nextSbd = String.valueOf(c.getCandidateNumber());
            break;
        }
        session.setAttribute("callingSbd", nextSbd);
    }
}
