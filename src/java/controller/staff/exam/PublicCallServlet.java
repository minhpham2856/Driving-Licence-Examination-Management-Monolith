package controller.staff.exam;

import controller.staff.exam.CandidateCallBoard;
import controller.staff.exam.CandidatePhotoHelper;
import dao.ExamRegistrationDAO;
import dao.ExamSessionDAO;
import dao.impl.ExamRegistrationDAOImpl;
import dao.impl.ExamSessionDAOImpl;
import model.exam.ExamRegistration;
import model.exam.ExamSession;

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

    private final ExamRegistrationDAO regdao = new ExamRegistrationDAOImpl();
    private final ExamSessionDAO sessiondao = new ExamSessionDAOImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int sessionId = CandidateCallBoard.resolveActiveSessionId(
                getServletContext(), request.getSession(false), request.getParameter("sessionId"));

        List<ExamRegistration> qList;
        try {
            qList = regdao.getCandidatesBySession(sessionId);
        } catch (Exception e) {
            e.printStackTrace();
            qList = new ArrayList<>();
        }
        CandidatePhotoHelper.normalizeQueue(getServletContext().getRealPath("/"), qList, regdao);

        CandidateCallBoard.State board = CandidateCallBoard.getState(getServletContext(), sessionId);
        String callingSbd = board != null ? board.getCallingSbd() : null;
        String nextSbd = board != null ? board.getNextSbd() : null;
        boolean shiftEnded = board != null && board.isShiftEnded();

        if (nextSbd == null && !shiftEnded) {
            nextSbd = CandidateCallBoard.resolveNextSbd(qList, callingSbd);
        }

        ExamRegistration callingCandidate = CandidateCallBoard.findBySbd(qList, callingSbd);
        ExamRegistration nextCandidate = CandidateCallBoard.findBySbd(qList, nextSbd);

        ExamSession currentSession = null;
        try {
            currentSession = sessiondao.getById(sessionId);
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
