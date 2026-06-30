package controller.examiner;

import model.User;
import service.ExaminerActionsService;
import service.ExaminerDataService;
import service.impl.ExaminerActionsServiceImpl;
import service.impl.ExaminerDataServiceImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Map;

@WebServlet(urlPatterns = {
    "/views/examiner/candidate-details",
    "/views/examiner/candidate-details-edit",
    "/views/examiner/candidate-paper"
})
public class ExaminerCandidateDetailsServlet extends HttpServlet {

    protected final ExaminerDataService viewDataService = new ExaminerDataServiceImpl();
    protected final ExaminerActionsService examinerService = new ExaminerActionsServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = ExaminerServletSupport.requireSession(request, response);
        if (session == null) {
            return;
        }

        Integer sessionId = ExaminerServletSupport.activeSessionId(session);
        String path = ExaminerServletSupport.stripContextPath(request);
        Integer sbd = ExaminerServletSupport.parseSbdParam(request.getParameter("sbd"));
        String search = request.getParameter("q");

        if (sessionId != null && sessionId > 0) {
            if ("/views/examiner/candidate-paper".equals(path)) {
                Map<String, Object> data = viewDataService.getCandidateCallData(sessionId, sbd, search);
                for (Map.Entry<String, Object> mapEntry : data.entrySet()) {
                    request.setAttribute(mapEntry.getKey(), mapEntry.getValue());
                }
                int paperSbd = sbd != null ? sbd : 0;
                Map<String, Object> ansData = viewDataService.getPaperAnswersData(sessionId, paperSbd, request.getContextPath());
                for (Map.Entry<String, Object> mapEntry : ansData.entrySet()) {
                    request.setAttribute(mapEntry.getKey(), mapEntry.getValue());
                }
            } else {
                Map<String, Object> data = viewDataService.getCandidateCallData(sessionId, sbd, search);
                for (Map.Entry<String, Object> mapEntry : data.entrySet()) {
                    request.setAttribute(mapEntry.getKey(), mapEntry.getValue());
                }
            }
        }

        String jsp = switch (path) {
            case "/views/examiner/candidate-details" -> "/views/examiner/candidate-details.jsp";
            case "/views/examiner/candidate-details-edit" -> "/views/examiner/candidate-details-edit.jsp";
            case "/views/examiner/candidate-paper" -> "/views/examiner/candidate-paper.jsp";
            default -> "/views/examiner/candidate-details.jsp";
        };
        request.getRequestDispatcher(jsp).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = ExaminerServletSupport.requireSession(request, response);
        if (session == null) {
            return;
        }

        Integer sessionId = ExaminerServletSupport.activeSessionId(session);
        if (sessionId == null || sessionId <= 0) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String path = ExaminerServletSupport.stripContextPath(request);
        if ("/views/examiner/candidate-details-edit".equals(path)) {
            Integer sbd = ExaminerServletSupport.parseSbdParam(request.getParameter("sbd"));
            if (sbd == null) {
                response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-details?error=noSbd");
                return;
            }
            boolean updated = examinerService.updateCandidateProfile(
                    sessionId,
                    sbd,
                    request.getParameter("fullName"),
                    request.getParameter("dateOfBirth"),
                    request.getParameter("govIdNo"),
                    request.getParameter("email"),
                    request.getParameter("phoneNo"),
                    request.getParameter("address"),
                    request.getParameter("sex"),
                    request.getParameter("reasonForTaking"),
                    ((User) session.getAttribute("user")).getUserId());

            if (updated) {
                response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-details-edit?sbd="
                        + ExaminerServletSupport.urlEncode(sbd) + "&saved=1");
                return;
            }

            Map<String, Object> data = viewDataService.getCandidateCallData(sessionId, sbd, null);
            for (Map.Entry<String, Object> mapEntry : data.entrySet()) {
                request.setAttribute(mapEntry.getKey(), mapEntry.getValue());
            }
            request.setAttribute("profileError", "Không lưu được thông tin. Kiểm tra lại dữ liệu nhập.");
            request.getRequestDispatcher("/views/examiner/candidate-details-edit.jsp").forward(request, response);
            return;
        }

        doGet(request, response);
    }
}
