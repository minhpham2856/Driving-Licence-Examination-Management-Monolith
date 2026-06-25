package controller.examiner;


import model.user.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

// Handles viewing and editing result details (score deductions).
@WebServlet(urlPatterns = {
    "/views/examiner/result-details",
    "/views/examiner/result-details-edit"
})
public class ExaminerResultDetailsServlet extends BaseExaminerServlet {

    // Renders the score details view and the score edit form.
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = requireSession(request, response);
        if (session == null) return;

        Integer sessionId = activeSessionId(session);
        String path = stripContextPath(request);
        String sbd = request.getParameter("sbd");
        String search = request.getParameter("q");
        String action = request.getParameter("action");

        if (sessionId != null && sessionId > 0) {
            // Block access to result edit features for theory sections
            if (isTheorySection(request)) {
                redirect(response, request, "/views/examiner/candidate-call?error=theoryNoResultEdit");
                return;
            }

            if ("/views/examiner/result-details-edit".equals(path) && "adjustDeduction".equals(action)) {
                if (sbd == null || sbd.isBlank()) {
                    redirect(response, request, path + "?error=noSbd");
                    return;
                }
                int deductionId;
                int delta;
                try {
                    deductionId = Integer.parseInt(request.getParameter("deductionId"));
                    delta = Integer.parseInt(request.getParameter("delta"));
                } catch (Exception e) {
                    redirect(response, request, path + "?sbd=" + urlEncode(sbd) + "&error=invalidDeduction");
                    return;
                }
                if (!examinerService.adjustScoreDeduction(sessionId, sbd, deductionId, delta, session)) {
                    redirect(response, request, path + "?sbd=" + urlEncode(sbd) + "&error=deductionFailed");
                    return;
                }
                redirect(response, request, path + "?sbd=" + urlEncode(sbd));
                return;
            }

            if ("/views/examiner/result-details-edit".equals(path)) {
                viewDataService.attachResultDetailsEdit(request, sessionId, sbd);
                if (sbd == null || sbd.isBlank() || request.getAttribute("candidate") == null) {
                    redirect(response, request, "/views/examiner/result-details");
                    return;
                }
                request.setAttribute("theoryMaxScore", viewDataService.theoryMaxQuestions());
                request.setAttribute("theoryPassScore", viewDataService.theoryPassThreshold());
                Object candidateObj = request.getAttribute("candidate");
                if (candidateObj != null) {
                    request.setAttribute("singleCandidateList", java.util.Collections.singletonList(candidateObj));
                }
            } else {
                viewDataService.attachToRequest(request, sessionId, sbd, search);
            }
        }

        String jsp = "/views/examiner/result-details-edit".equals(path) 
                ? "/views/examiner/result-details-edit.jsp" 
                : "/views/examiner/result-details.jsp";
        forward(request, response, jsp);
    }

    // Handles POST requests to save score edit reasons and password validations.
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
        if ("/views/examiner/result-details-edit".equals(path)) {
            if (isTheorySection(request)) {
                redirect(response, request, "/views/examiner/candidate-call?error=theoryNoResultEdit");
                return;
            }

            String sbd = request.getParameter("sbd");
            User user = (User) session.getAttribute("user");
            String password = request.getParameter("password");
            String reason = request.getParameter("reason");
            String reasonDetail = request.getParameter("reasonDetail");

            if (reason == null || reason.isBlank()) {
                forwardScoreFormError(request, response, sessionId, sbd, reason, reasonDetail, "Vui lòng chọn lý do điều chỉnh.");
                return;
            }
            if (password == null || password.isBlank()) {
                forwardScoreFormError(request, response, sessionId, sbd, reason, reasonDetail, "Vui lòng nhập mật khẩu để xác nhận.");
                return;
            }
            if (!examinerService.verifyPassword(user, password)) {
                forwardScoreFormError(request, response, sessionId, sbd, reason, reasonDetail, "Mật khẩu không chính xác.");
                return;
            }

            boolean updated = examinerService.logPracticalScoreEditReason(
                    sessionId, sbd, reason, reasonDetail, user, password, session);

            if (updated) {
                redirect(response, request, "/views/examiner/result-details-edit?sbd=" + urlEncode(sbd) + "&saved=1");
                return;
            }
            forwardScoreFormError(request, response, sessionId, sbd, reason, reasonDetail, "Không lưu được lý do. Kiểm tra thí sinh và ca thi.");
            return;
        }

        doGet(request, response);
    }

    // Forwards back to the score edit form with an error message, preserving form state.
    private void forwardScoreFormError(HttpServletRequest request, HttpServletResponse response,
            int sessionId, String sbd, String reason, String reasonDetail,
            String errorMessage) throws ServletException, IOException {
        viewDataService.attachResultDetailsEdit(request, sessionId, sbd);
        request.setAttribute("scoreError", errorMessage);
        request.setAttribute("formReason", reason);
        request.setAttribute("formReasonDetail", reasonDetail);

        Object candidateObj = request.getAttribute("candidate");
        if (candidateObj != null) {
            request.setAttribute("singleCandidateList", java.util.Collections.singletonList(candidateObj));
        }

        forward(request, response, "/views/examiner/result-details-edit.jsp");
    }
}

