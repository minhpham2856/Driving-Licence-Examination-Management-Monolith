package controller.examiner;

import java.util.*;


import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

// Handles dashboard and confirmation page rendering.
@WebServlet(urlPatterns = {
    "/views/examiner/dashboard",
    "/views/examiner/confirmation"
})
public class ExaminerDashboardServlet extends BaseExaminerServlet {

    // Handles GET requests to render the examiner dashboard or confirmation.
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = requireSession(request, response);
        if (session == null) {
            return;
        }

        Integer sessionId = activeSessionId(session);
        String path = stripContextPath(request);
        String sbd = request.getParameter("sbd");
        String search = request.getParameter("q");

        if (sessionId != null && sessionId > 0) {
            // Attach generic list data
            Map<String, Object> data = viewDataService.getCandidateCallData(sessionId, sbd, search); for(Map.Entry<String, Object> mapEntry : data.entrySet()) request.setAttribute(mapEntry.getKey(), mapEntry.getValue());
        }

        String jsp = "/views/examiner/dashboard".equals(path)
                ? "/views/examiner/dashboard.jsp"
                : "/views/examiner/confirmation.jsp";
        forward(request, response, jsp);
    }
}



