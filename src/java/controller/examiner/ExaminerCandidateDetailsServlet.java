package controller.examiner;

import dto.payload.CandidateCallDataDTO;
import service.ExaminerDataService;
import service.impl.ExaminerDataServiceImpl;
import util.ExaminerCandidateSort;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
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
public class ExaminerCandidateDetailsServlet extends BaseExaminerServlet {

    protected final ExaminerDataService viewDataService = new ExaminerDataServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = requireSession(request, response);
        if (session == null) {
            return;
        }
        Integer sessionId = getActiveSessionId(session);
        String path = stripContextPath(request);
        Integer sbd = parseSbdParam(request.getParameter("sbd"));
        String search = request.getParameter("q");
        if (sessionId != null && sessionId > 0) {
            CandidateCallDataDTO data = viewDataService.getCandidateCallData(sessionId, sbd, search);
            applyCandidateListSort(request, data);
            data.applyTo(request);
            if ("/views/examiner/candidate-paper".equals(path)) {
                int paperSbd = sbd != null ? sbd : 0;
                Map<String, Object> ansData = viewDataService.getPaperAnswersData(sessionId, paperSbd, request.getContextPath());
                for (Map.Entry<String, Object> mapEntry : ansData.entrySet()) {
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
        HttpSession session = requireSession(request, response);
        if (session == null) {
            return;
        }
        Integer sessionId = getActiveSessionId(session);
        if (sessionId == null || sessionId <= 0) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        String path = stripContextPath(request);
        if ("/views/examiner/candidate-details-edit".equals(path)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Giám khảo không được sửa thông tin thí sinh.");
            return;
        }
        doGet(request, response);
    }

    private void applyCandidateListSort(HttpServletRequest request, CandidateCallDataDTO data) {
        if (data.getCandidates() != null) {
            ExaminerCandidateSort.applyCandidateSort(request, data.getCandidates());
        }
    }
}
