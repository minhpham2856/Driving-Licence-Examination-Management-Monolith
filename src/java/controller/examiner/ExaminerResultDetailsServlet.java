package controller.examiner;
import filter.ExaminerPortalFilter;
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
@WebServlet(urlPatterns = {
    "/views/examiner/result-details",
    "/views/examiner/result-details-edit"
})
public class ExaminerResultDetailsServlet extends HttpServlet {
    protected final ExaminerDataService viewDataService = new ExaminerDataServiceImpl();
    protected final ExaminerActionsService examinerService = new ExaminerActionsServiceImpl();
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = requireSession(request, response);
        if (session == null) {
            return;
        }
        Integer sessionId = activeSessionId(session);
        String path = stripContextPath(request);
        Integer sbd = parseSbdParam(request.getParameter("sbd"));
        String search = request.getParameter("q");
        String action = request.getParameter("action");
        if (sessionId != null && sessionId > 0) {
            if (isTheorySection(request)) {
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
                            + urlEncode(sbd) + "&error=invalidDeduction");
                    return;
                }
                if (!examinerService.adjustScoreDeduction(sessionId, sbd, deductionId, delta,
                        ((User) session.getAttribute("user")).getUserId())) {
                    response.sendRedirect(request.getContextPath() + path + "?sbd="
                            + urlEncode(sbd) + "&error=deductionFailed");
                    return;
                }
                response.sendRedirect(request.getContextPath() + path + "?sbd="
                        + urlEncode(sbd));
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
                Map<String, Object> data = viewDataService.getCandidateCallData(sessionId, sbd, search);
                for (Map.Entry<String, Object> mapEntry : data.entrySet()) {
                    request.setAttribute(mapEntry.getKey(), mapEntry.getValue());
                }
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
        Integer sessionId = activeSessionId(session);
        if (sessionId == null || sessionId <= 0) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        String path = stripContextPath(request);
        if ("/views/examiner/result-details-edit".equals(path)) {
            if (isTheorySection(request)) {
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
            if (!examinerService.verifyPassword(user, password)) {
                forwardScoreFormError(request, response, sessionId, sbd, reason, reasonDetail, "Mật khẩu không đúng.");
                return;
            }
            boolean updated = examinerService.logPracticalScoreEditReason(
                    sessionId, sbd, reason, reasonDetail, user, password, user.getUserId());
            if (updated) {
                response.sendRedirect(request.getContextPath() + "/views/examiner/result-details-edit?sbd="
                        + urlEncode(sbd) + "&saved=1");
                return;
            }
            forwardScoreFormError(request, response, sessionId, sbd, reason, reasonDetail, "Lỗi");
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
    private HttpSession requireSession(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
        return session;
    }
    private Integer activeSessionId(HttpSession session) {
        if (session == null) {
            return null;
        }
        return (Integer) session.getAttribute(ExaminerPortalFilter.ATTR_ACTIVE_SESSION_ID);
    }
    private String stripContextPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String ctx = request.getContextPath();
        if (ctx != null && !ctx.isEmpty() && uri.startsWith(ctx)) {
            return uri.substring(ctx.length());
        }
        return uri;
    }
    private Integer parseSbdParam(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            int sbd = Integer.parseInt(raw.trim());
            return sbd > 0 ? sbd : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
    private boolean isTheorySection(HttpServletRequest request) {
        return ExaminerPortalFilter.isTheorySession(request.getSession(false));
    }
    private String urlEncode(int value) {
        return URLEncoder.encode(String.valueOf(value), StandardCharsets.UTF_8);
    }
}
