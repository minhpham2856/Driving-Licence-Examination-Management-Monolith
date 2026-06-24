package Controllers.Staff.ExamStaff;

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
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/views/staff/examstaff/dashboard")
public class DashboardServlet extends HttpServlet {

    private final ExamSessionDAO sessionDAO = new ExamSessionDAOImpl();
    private final ExamRegistrationDAO regDAO = new ExamRegistrationDAOImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        String webRoot = request.getServletContext().getRealPath("/");

        List<ExamSession> allSessions;
        try {
            allSessions = sessionDAO.getAllSessions();
        } catch (Exception e) {
            e.printStackTrace();
            allSessions = new ArrayList<>();
        }
        request.setAttribute("allSessions", allSessions);
        request.setAttribute("examOptions", ExamStaffViewHelper.buildExamOptions(allSessions));

        int sessionId = ExamStaffViewHelper.resolveSessionId(request, session, 2);
        session.setAttribute("selectedSessionId", sessionId);

        ExamSession currentSession = ExamStaffViewHelper.findSessionById(allSessions, sessionId);
        request.setAttribute("currentSession", currentSession);

        int examId = (currentSession != null && currentSession.getExamId() > 0)
                ? currentSession.getExamId()
                : sessionId;
        request.setAttribute("selectedExamId", examId);

        List<ExamSession> examSessions = new ArrayList<>();
        if (currentSession != null) {
            try {
                examSessions = sessionDAO.getSessionsByExamId(examId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        request.setAttribute("examSessions", examSessions);

        List<ExamRegistration> qList;
        try {
            qList = regDAO.getCandidatesByExamId(examId);
        } catch (Exception e) {
            e.printStackTrace();
            qList = new ArrayList<>();
        }
        CandidatePhotoHelper.normalizeQueue(webRoot, qList, regDAO);
        session.setAttribute("candidateQueue", qList);
        session.setAttribute("lastLoadedExamId", examId);

        boolean shiftEnded = "true".equals(session.getAttribute("shiftEnded"));
        ExamStaffViewHelper.syncCallingSbd(session, getServletContext(), sessionId, qList, shiftEnded);

        List<ExaminerSlot> assignedExaminers = ExaminerAssignmentStore.getByExamId(session, examId);
        int assignedWithArea = 0;
        if (assignedExaminers != null) {
            for (ExaminerSlot slot : assignedExaminers) {
                if (slot.getAreaId() > 0) {
                    assignedWithArea++;
                }
            }
        }
        request.setAttribute("assignedExaminerCount", assignedWithArea);

        ExamStaffViewHelper.consumeFlash(session, "sessionControlMsg", request, "sessionControlMsg");
        ExamStaffViewHelper.consumeFlash(session, "sessionControlError", request, "sessionControlError");

        request.getRequestDispatcher("/views/staff/examstaff/dashboard.jsp").forward(request, response);
    }
}
