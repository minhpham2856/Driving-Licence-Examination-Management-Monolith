package controller.staff.exam;
import dto.*;
import model.*;
import service.CandidatePhotoService;
import service.impl.CandidatePhotoServiceImpl;
import dto.CandidateCallBoardStateDTO;
import service.ExamRegistrationService;
import service.ExamSessionControlService;
import service.impl.ExamRegistrationServiceImpl;
import service.impl.ExamSessionControlServiceImpl;
import dto.CandidateEnrollmentDTO;
import dto.SessionDTO;
import service.CandidatePhotoService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@WebServlet("/views/public/public-call")
public class PublicCallServlet extends HttpServlet {
    private static final String CALL_BOARD_CONTEXT_KEY = "candidateCallBoards";
    private final ExamRegistrationService regService = new ExamRegistrationServiceImpl();
    private final ExamSessionControlService sessionService = new ExamSessionControlServiceImpl();
    private final CandidatePhotoService photoService = new CandidatePhotoServiceImpl();
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int sessionId = 2; // Default
        String sessionIdStr = request.getParameter("sessionId");
        if (sessionIdStr != null && !sessionIdStr.isBlank()) {
            try {
                sessionId = Integer.parseInt(sessionIdStr.trim());
            } catch (Exception e) {}
        } else {
            // Also check session
            jakarta.servlet.http.HttpSession httpSession = request.getSession(false);
            if (httpSession != null && httpSession.getAttribute("selectedSessionId") != null) {
                sessionId = (Integer) httpSession.getAttribute("selectedSessionId");
            }
        }
        List<CandidateEnrollmentDTO> qList;
        try {
            qList = regService.getCandidatesBySession(sessionId);
        } catch (Exception e) {
            e.printStackTrace();
            qList = new ArrayList<>();
        }
        photoService.normalizeQueue(getServletContext().getRealPath("/"), qList);
        CandidateCallBoardStateDTO board = getCallBoardState(sessionId);
        String callingSbd = board != null ? board.getCallingSbd() : null;
        boolean shiftEnded = board != null && board.isShiftEnded();
        String nextSbd = null;
        if (!shiftEnded) {
            if (callingSbd == null || callingSbd.isBlank()) {
                for (CandidateEnrollmentDTO c : qList) {
                    if (!(c.isPaymentCompleted() && c.isValidCapturedPhoto())) {
                        nextSbd = String.valueOf(c.getSbd());
                        break;
                    }
                }
            } else {
                boolean foundCurrent = false;
                for (CandidateEnrollmentDTO c : qList) {
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
        CandidateEnrollmentDTO callingCandidate = null;
        if (callingSbd != null) {
            for (CandidateEnrollmentDTO c : qList) {
                if (callingSbd.equals(String.valueOf(c.getSbd()))) {
                    callingCandidate = c;
                    break;
                }
            }
        }
        CandidateEnrollmentDTO nextCandidate = null;
        if (nextSbd != null) {
            for (CandidateEnrollmentDTO c : qList) {
                if (nextSbd.equals(String.valueOf(c.getSbd()))) {
                    nextCandidate = c;
                    break;
                }
            }
        }
        SessionDTO currentSession = null;
        try {
            currentSession = sessionService.getSessionById(sessionId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        request.setAttribute("currentSession", currentSession);
        request.setAttribute("callingCandidate", callingCandidate);
        request.setAttribute("nextCandidate", nextCandidate);
        request.setAttribute("isCallingActive", callingCandidate != null && !shiftEnded);
        request.setAttribute("shiftEnded", shiftEnded);
        request.setAttribute("sessionId", sessionId);
        request.getRequestDispatcher("/views/public/public-call.jsp").forward(request, response);
    }
    @SuppressWarnings("unchecked")
    private CandidateCallBoardStateDTO getCallBoardState(int examSessionId) {
        if (examSessionId <= 0) {
            return null;
        }
        jakarta.servlet.ServletContext ctx = getServletContext();
        Map<Integer, CandidateCallBoardStateDTO> boards =
                (Map<Integer, CandidateCallBoardStateDTO>) ctx.getAttribute(CALL_BOARD_CONTEXT_KEY);
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
