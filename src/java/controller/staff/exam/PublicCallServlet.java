package controller.staff.exam;

import service.impl.PhotoServiceImpl;
import dto.CallBoardDTO;
import service.RegistrationService;
import service.SessionService;
import service.impl.RegistrationServiceImpl;
import service.impl.SessionServiceImpl;
import dto.EnrollmentDTO;
import dto.SessionViewDTO;
import service.PhotoService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import controller.staff.exam.BaseStaffExamServlet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/views/public/public-call")
public class PublicCallServlet extends BaseStaffExamServlet {

    private static final String CALL_BOARD_CONTEXT_KEY = "candidateCallBoards";
    private final RegistrationService regService = new RegistrationServiceImpl();
    private final SessionService sessionService = new SessionServiceImpl();
    private final PhotoService photoService = new PhotoServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int sessionId = readSessionId(request, request.getSession(true), sessionService);
        List<EnrollmentDTO> qList;
        try {
            qList = regService.getCandidatesBySession(sessionId);
        } catch (Exception e) {
            e.printStackTrace();
            qList = new ArrayList<>();
        }
        photoService.normalizeQueue(getServletContext().getRealPath("/"), qList);
        CallBoardDTO board = getCallBoardState(sessionId);
        String callingSbd = board != null ? board.getCallingSbd() : null;
        boolean shiftEnded = board != null && board.isShiftEnded();
        String nextSbd = null;
        if (!shiftEnded) {
            if (callingSbd == null || callingSbd.isBlank()) {
                for (EnrollmentDTO c : qList) {
                    if (!(c.isPaymentCompleted() && c.isValidCapturedPhoto())) {
                        nextSbd = String.valueOf(c.getSbd());
                        break;
                    }
                }
            } else {
                boolean foundCurrent = false;
                for (EnrollmentDTO c : qList) {
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
        EnrollmentDTO callingCandidate = null;
        if (callingSbd != null) {
            for (EnrollmentDTO c : qList) {
                if (callingSbd.equals(String.valueOf(c.getSbd()))) {
                    callingCandidate = c;
                    break;
                }
            }
        }
        EnrollmentDTO nextCandidate = null;
        if (nextSbd != null) {
            for (EnrollmentDTO c : qList) {
                if (nextSbd.equals(String.valueOf(c.getSbd()))) {
                    nextCandidate = c;
                    break;
                }
            }
        }
        SessionViewDTO currentSession = null;
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
