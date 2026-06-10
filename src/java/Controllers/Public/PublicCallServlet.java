package Controllers.Public;

import Controllers.Staff.ExamStaff.CandidateCallBoard;
import Controllers.Staff.ExamStaff.CandidatePhotoHelper;
import DAO.ExamRegistrationDAO;
import DAO.ExamSessionDAO;
import DAO.Impl.ExamRegistrationDAOImpl;
import DAO.Impl.ExamSessionDAOImpl;
import Models.ExamRegistration;
import Models.ExamSession;

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

    private final ExamRegistrationDAO regDAO = new ExamRegistrationDAOImpl();
    private final ExamSessionDAO sessionDAO = new ExamSessionDAOImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int sessionId = CandidateCallBoard.resolveActiveSessionId(
                getServletContext(), request.getSession(false), request.getParameter("sessionId"));

        List<ExamRegistration> qList;
        try {
            qList = regDAO.getCandidatesBySession(sessionId);
        } catch (Exception e) {
            e.printStackTrace();
            qList = new ArrayList<>();
        }
        CandidatePhotoHelper.normalizeQueue(getServletContext().getRealPath("/"), qList, regDAO);

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
