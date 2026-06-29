package controller.examiner;

import java.util.*;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServlet;
import util.ExaminerUtil;
import service.ExaminerDataService;
import service.impl.ExaminerDataServiceImpl;
import service.ExaminerActionsService;
import service.impl.ExaminerActionsServiceImpl;

import java.io.IOException;

@WebServlet(urlPatterns = {
    "/views/examiner/dashboard",
    "/views/examiner/confirmation"
})
public class ExaminerDashboardServlet extends HttpServlet {
    protected final ExaminerDataService viewDataService = new ExaminerDataServiceImpl();
    protected final ExaminerActionsService examinerService = new ExaminerActionsServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = ExaminerUtil.requireSession(request, response);
        if (session == null) {
            return;
        }

        Integer sessionId = ExaminerUtil.activeSessionId(session);
        String path = ExaminerUtil.stripContextPath(request);
        String sbd = request.getParameter("sbd");
        String search = request.getParameter("q");

        if (sessionId != null && sessionId > 0) {
            
            Map<String, Object> data = viewDataService.getCandidateCallData(sessionId, sbd, search); for(Map.Entry<String, Object> mapEntry : data.entrySet()) request.setAttribute(mapEntry.getKey(), mapEntry.getValue());
        }

        String jsp = "/views/examiner/dashboard".equals(path)
                ? "/views/examiner/dashboard.jsp"
                : "/views/examiner/confirmation.jsp";
        request.getRequestDispatcher(jsp).forward(request, response);
    }
}

