package managingstaff.controller;

import auth.dto.UserDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import managingstaff.dao.DossierDAO;
import managingstaff.dao.impl.DossierDAOImpl;
import managingstaff.dao.ExamSessionDAO;
import managingstaff.dao.impl.ExamSessionDAOImpl;
import managingstaff.dao.TentativeExamDateDAO;
import managingstaff.dao.impl.TentativeExamDateDAOImpl;
import managingstaff.dto.SessionDTO;
import java.util.List;
import managingstaff.util.SessionUtil;

@WebServlet("/manager/dashboard")
public class ManagingDashboardServlet extends HttpServlet {

    private static final String VIEW = "/views/staff/managingstaff/dashboard.jsp";
    private final DossierDAO dossierDAO = new DossierDAOImpl();
    private final ExamSessionDAO sessionDAO = new ExamSessionDAOImpl();
    private final TentativeExamDateDAO tentativeExamDateDAO = new TentativeExamDateDAOImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        UserDTO user = SessionUtil.getCurrentUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        if (!SessionUtil.isManager(user)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        int pending = dossierDAO.countSubmitted();
        request.setAttribute("reviewableCount", pending);
        request.setAttribute("recentDossiers", dossierDAO.findSubmittedPage(1, 10));
        List<SessionDTO> upcoming = sessionDAO.findPage("upcoming", List.of(), 1, 5);
        request.setAttribute("upcomingSessions", upcoming);
        request.setAttribute("upcomingCount", sessionDAO.count("upcoming", List.of()));
        request.setAttribute("tentativeExamDateCount", tentativeExamDateDAO.countAll("active"));
        request.getRequestDispatcher(VIEW).forward(request, response);
    }
}
