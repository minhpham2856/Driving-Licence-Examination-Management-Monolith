package controller.staff.exam;

import service.CandidateCallBoardService;
import service.impl.CandidateCallBoardServiceImpl;

import service.CandidatePhotoService;
import service.impl.CandidatePhotoServiceImpl;

import dto.candidate.CandidateCallBoardStateDTO;


import service.ExamRegistrationService;

import dao.SessionDAO;
import service.impl.ExamRegistrationServiceImpl;

import dao.impl.SessionDAOImpl;

import dto.candidate.CandidateEnrollmentDTO;

import dto.exam.SessionDTO;

import service.CandidateCallBoardService;

import service.CandidatePhotoService;


import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/views/public/public-call")
public class PublicCallServlet extends HttpServlet {

    private final ExamRegistrationService regService = new ExamRegistrationServiceImpl();
    private final SessionDAO sessionDAO = new SessionDAOImpl();
    private final CandidatePhotoService photoService = new CandidatePhotoServiceImpl();
    private final CandidateCallBoardService callBoardService = new CandidateCallBoardServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int sessionId = 2; // Default
        String sessionIdStr = request.getParameter("sessionId");
        if (sessionIdStr != null && !sessionIdStr.trim().isEmpty()) {
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

        CandidateCallBoardStateDTO board = callBoardService.getState(getServletContext(), sessionId);
        
        String callingSbd = board != null ? board.getCallingSbd() : null;
        boolean shiftEnded = board != null && board.isShiftEnded();
        String nextSbd = null;

        if (!shiftEnded) {
            if (callingSbd == null || callingSbd.trim().isEmpty()) {
                for (CandidateEnrollmentDTO c : qList) {
                    if (!(c.isPaymentCompleted() && c.isValidCapturedPhoto())) {
                        nextSbd = c.getSbd();
                        break;
                    }
                }
            } else {
                boolean foundCurrent = false;
                for (CandidateEnrollmentDTO c : qList) {
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

        CandidateEnrollmentDTO callingCandidate = null;
        if (callingSbd != null) {
            for (CandidateEnrollmentDTO c : qList) {
                if (callingSbd.equals(c.getSbd())) {
                    callingCandidate = c;
                    break;
                }
            }
        }
        
        CandidateEnrollmentDTO nextCandidate = null;
        if (nextSbd != null) {
            for (CandidateEnrollmentDTO c : qList) {
                if (nextSbd.equals(c.getSbd())) {
                    nextCandidate = c;
                    break;
                }
            }
        }

        SessionDTO currentSession = null;
        try {
            currentSession = sessionDAO.getById(sessionId);
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
}










