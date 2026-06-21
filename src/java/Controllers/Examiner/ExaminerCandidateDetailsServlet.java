package Controllers.Examiner;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

// Handles viewing and editing candidate profile details, and viewing candidate paper.
@WebServlet(urlPatterns = {
    "/views/examiner/candidate-details",
    "/views/examiner/candidate-details-edit",
    "/views/examiner/candidate-paper"
})
public class ExaminerCandidateDetailsServlet extends BaseExaminerServlet {

    // Renders the candidate details, edit form, or theory paper view.
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = requireSession(request, response);
        if (session == null) return;

        Integer sessionId = activeSessionId(session);
        String path = stripContextPath(request);
        String sbd = request.getParameter("sbd");
        String search = request.getParameter("q");

        if (sessionId != null && sessionId > 0) {
            if ("/views/examiner/candidate-paper".equals(path)) {
                viewDataService.attachToRequest(request, sessionId, sbd, search);
                viewDataService.attachPaperAnswers(request, sessionId, sbd, request.getContextPath());
            } else {
                viewDataService.attachToRequest(request, sessionId, sbd, search);
            }
        }

        String jsp = switch (path) {
            case "/views/examiner/candidate-details" -> "/views/examiner/candidate-details.jsp";
            case "/views/examiner/candidate-details-edit" -> "/views/examiner/candidate-details-edit.jsp";
            case "/views/examiner/candidate-paper" -> "/views/examiner/candidate-paper.jsp";
            default -> "/views/examiner/candidate-details.jsp";
        };
        forward(request, response, jsp);
    }

    // Handles POST requests to save candidate profile updates.
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = requireSession(request, response);
        if (session == null) return;

        Integer sessionId = activeSessionId(session);
        if (sessionId == null || sessionId <= 0) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Chưa có ca thi đang diễn ra.");
            return;
        }

        String path = stripContextPath(request);
        if ("/views/examiner/candidate-details-edit".equals(path)) {
            String sbd = request.getParameter("sbd");
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
                    session);

            if (updated) {
                redirect(response, request, "/views/examiner/candidate-details-edit?sbd=" + urlEncode(sbd) + "&saved=1");
                return;
            }

            viewDataService.attachToRequest(request, sessionId, sbd, null);
            request.setAttribute("profileError", "Không lưu được thông tin. Kiểm tra lại dữ liệu nhập.");
            forward(request, response, "/views/examiner/candidate-details-edit.jsp");
            return;
        }

        doGet(request, response);
    }
}
