package controller.examiner;
import filter.ExaminerFilter;
import model.User;
import service.CallService;
import service.ExamViewService;
import service.impl.CallServiceImpl;
import service.impl.ExamViewServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
@WebServlet(urlPatterns = {
    "/views/examiner/result-details",
    "/views/examiner/result-details-edit"
})
public class ExaminerResultDetailsServlet extends BaseExaminerServlet {
    protected final ExamViewService viewDataService = new ExamViewServiceImpl();
    protected final CallService ScheduleService = new CallServiceImpl();
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
        String action = request.getParameter("action");
        if (sessionId != null && sessionId > 0) {
            if (isTheorySection(session)) {
                response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=theoryNoResultEdit");
                return;
            }
            if ("/views/examiner/result-details-edit".equals(path) && "adjustDeduction".equals(action)) {
                if (sbd == null) {
                    response.sendRedirect(request.getContextPath() + path + "?error=noSbd");
                    return;
                }
                int deductionId;
                int delta;
                try {
                    deductionId = Integer.parseInt(request.getParameter("deductionId"));
                    delta = Integer.parseInt(request.getParameter("delta"));
                } catch (Exception e) {
                    response.sendRedirect(request.getContextPath() + path + "?sbd="
                            + encodeSbd(sbd) + "&error=invalidDeduction");
                    return;
                }
                if (!ScheduleService.adjustScoreDeduction(sessionId, sbd, deductionId, delta,
                        ((User) session.getAttribute("user")).getUserId()).isSuccess()) {
                    response.sendRedirect(request.getContextPath() + path + "?sbd="
                            + encodeSbd(sbd) + "&error=deductionFailed");
                    return;
                }
                response.sendRedirect(request.getContextPath() + path + "?sbd="
                        + encodeSbd(sbd));
                return;
            }
            if ("/views/examiner/result-details-edit".equals(path)) {
                Map<String, Object> data = viewDataService.getResultDetailsEditData(sessionId, sbd);
                for (Map.Entry<String, Object> mapEntry : data.entrySet()) {
                    request.setAttribute(mapEntry.getKey(), mapEntry.getValue());
                }
                if (sbd == null || request.getAttribute("candidate") == null) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/result-details");
                    return;
                }
                request.setAttribute("theoryMaxScore", viewDataService.theoryMaxQuestions());
                request.setAttribute("theoryPassScore", viewDataService.theoryPassThreshold());
                request.setAttribute("pageUrl", request.getContextPath() + "/views/examiner/result-details-edit");
                Object candidateObj = request.getAttribute("candidate");
                if (candidateObj != null) {
                    request.setAttribute("singleCandidateList", Collections.singletonList(candidateObj));
                }
            } else {
                applyCandidateListAttributes(request, session, viewDataService, sessionId, sbd, search);
            }
        }
        String jsp = "/views/examiner/result-details-edit".equals(path)
                ? "/views/examiner/result-details-edit.jsp"
                : "/views/examiner/result-details.jsp";
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
        if ("/views/examiner/result-details-edit".equals(path)) {
            if (isTheorySection(session)) {
                response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=theoryNoResultEdit");
                return;
            }
            Integer sbd = parseSbdParam(request.getParameter("sbd"));
            User user = (User) session.getAttribute("user");
            String password = request.getParameter("password");
            String reason = request.getParameter("reason");
            String reasonDetail = request.getParameter("reasonDetail");
            if (sbd == null) {
                forwardScoreFormError(request, response, sessionId, null, reason, reasonDetail, "Thiếu SBD.");
                return;
            }
            if (reason == null || reason.isBlank()) {
                forwardScoreFormError(request, response, sessionId, sbd, reason, reasonDetail, "Vui lòng nhập lý do.");
                return;
            }
            if (password == null || password.isBlank()) {
                forwardScoreFormError(request, response, sessionId, sbd, reason, reasonDetail, "Vui lòng nhập mật khẩu.");
                return;
            }
            if (!ScheduleService.verifyPassword(user, password)) {
                forwardScoreFormError(request, response, sessionId, sbd, reason, reasonDetail, "Mật khẩu không đúng.");
                return;
            }
            if (!ScheduleService.logPracticalScoreEditReason(sessionId, sbd, user, password, reason,
                    reasonDetail, user.getUserId()).isSuccess()) {
                forwardScoreFormError(request, response, sessionId, sbd, reason, reasonDetail, "Lỗi");
                return;
            }
            response.sendRedirect(request.getContextPath() + "/views/examiner/result-details-edit?sbd="
                    + encodeSbd(sbd) + "&saved=1");
            return;
        }
        doGet(request, response);
    }
    private void forwardScoreFormError(HttpServletRequest request, HttpServletResponse response,
            int sessionId, Integer sbd, String reason, String reasonDetail,
            String errorMessage) throws ServletException, IOException {
        Map<String, Object> data = viewDataService.getResultDetailsEditData(sessionId, sbd);
        for (Map.Entry<String, Object> mapEntry : data.entrySet()) {
            request.setAttribute(mapEntry.getKey(), mapEntry.getValue());
        }
        request.setAttribute("scoreError", errorMessage);
        request.setAttribute("formReason", reason);
        request.setAttribute("formReasonDetail", reasonDetail);
        request.setAttribute("pageUrl", request.getContextPath() + "/views/examiner/result-details-edit");
        Object candidateObj = request.getAttribute("candidate");
        if (candidateObj != null) {
            request.setAttribute("singleCandidateList", Collections.singletonList(candidateObj));
        }
        request.getRequestDispatcher("/views/examiner/result-details-edit.jsp").forward(request, response);
    }

    private boolean isTheorySection(HttpSession session) {
        return ExaminerFilter.isTheorySession(session);
    }
}
