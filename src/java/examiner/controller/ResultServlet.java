package examiner.controller;

import auth.dto.UserDTO;
import shared.Attributes;
import shared.enums.SectionType;
import static shared.enums.SectionType.THEORY;
import shared.model.User;
import examiner.filter.ExaminerFilter;
import examiner.service.ActionService;
import examiner.service.ExamViewService;
import examiner.dto.CandidateRowDTO;
import examiner.service.impl.ActionServiceImpl;
import examiner.service.impl.ExamViewServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import static shared.util.FormatUtil.formatPositiveInteger;
import examiner.util.ListUtil;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = {
    "/examiner/result-details",
    "/examiner/result-details-edit"
})
// Result details and edit screens: view practical scores, adjust deductions, and log edit reasons with password confirmation.
public class ResultServlet extends HttpServlet {

    protected final ExamViewService viewService = new ExamViewServiceImpl();
    protected final ActionService actionService = new ActionServiceImpl();

    // Route to result-details list or edit view; handle GET deduction adjustments for the edit page.
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        Integer activeExamId = (Integer) session.getAttribute(ExaminerFilter.ATTR_ACTIVE_EXAM_ID);
        String path = stripContextPath(request);
        Integer sbd = formatPositiveInteger(request.getParameter("sbd"));

        String search = request.getParameter("q");
        String action = request.getParameter("action");

        if (activeExamId != null && activeExamId > 0) {
            SectionType sectionType = ExaminerFilter.resolveSectionType(session);
            // Result edit flows apply to practical (layout) section only.
            if (sectionType == THEORY) {
                response.sendRedirect(request.getContextPath() + "/examiner/action?error=theoryNoResultEdit");
                return;
            }

            if ("/examiner/result-details-edit".equals(path) && "adjustDeduction".equals(action)) {
                // Edit page only supports staged changes; database updates require password-confirm submit.
                response.sendRedirect(request.getContextPath() + path + "?sbd=" + urlEncode(sbd) + "&error=needConfirmSave");
                return;
            }

            if ("/examiner/result-details".equals(path)) {
                // List view with optional selected candidate detail panel.
                List<CandidateRowDTO> candidates = viewService.getAllFilteredByExam(
                        activeExamId, sectionType, search);
                ListUtil.applySortAndSearch(request, candidates);
                request.setAttribute("candidates", candidates);
                if (sbd != null) {
                    CandidateRowDTO candidate = viewService.getCandidateViewRow(
                            activeExamId, sbd, sectionType);
                    if (candidate != null) {
                        request.setAttribute("candidate", candidate);
                    }
                }
            } else if (sbd != null && "/examiner/result-details-edit".equals(path)) {
                // Edit view loads fault list, deductions, and password-gated save form.
                Map<String, Object> data = viewService.getResultDetailsViewByExam(
                        activeExamId, sbd, sectionType);
                if (data != null) {
                    for (Map.Entry<String, Object> mapEntry : data.entrySet()) {
                        request.setAttribute(mapEntry.getKey(), mapEntry.getValue());
                    }
                }
            }
        }

        String jsp = switch (path) {
            case "/examiner/result-details" ->
                "/views/examiner/result-details.jsp";
            case "/examiner/result-details-edit" ->
                "/views/examiner/result-details-edit.jsp";
            default ->
                "/views/examiner/result-details.jsp";
        };
        request.getRequestDispatcher(jsp).forward(request, response);
    }

    // Save deduction changes or log a practical score edit reason with password confirmation on the edit page.
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        Integer activeExamId = (Integer) session.getAttribute(ExaminerFilter.ATTR_ACTIVE_EXAM_ID);
        if (activeExamId == null || activeExamId <= 0) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String path = stripContextPath(request);
        if ("/examiner/result-details-edit".equals(path)) {
            Integer sbd = formatPositiveInteger(request.getParameter("sbd"));

            if (sbd == null) {
                response.sendRedirect(request.getContextPath() + "/examiner/result-details?error=noSbd");
                return;
            }

            SectionType sectionType = ExaminerFilter.resolveSectionType(session);

            String reason = request.getParameter("reasonCode");
            String reasonDetail = request.getParameter("reasonDetail");
            String password = request.getParameter("confirmPassword");
            String pendingAdjustments = request.getParameter("pendingAdjustments");
            UserDTO userDto = (UserDTO) session.getAttribute(Attributes.Session.USER);
            if (userDto == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            User user = userDto.toUser();
            // Password confirms the examiner before persisting an audit reason for score edits.
            if (!actionService.logPracticalScoreEditReason(activeExamId, sbd, user, password, reason, reasonDetail,
                    user.getUserId(), sectionType).isSuccess()) {
                request.setAttribute("editError", "Lưu lý do thất bại. Vui lòng kiểm tra lại mật khẩu xác nhận.");
                request.setAttribute("formReason", reason);
                request.setAttribute("formReasonDetail", reasonDetail);
                doGet(request, response);
                return;
            }

            if (!applyPendingAdjustments(activeExamId, sbd, pendingAdjustments, user.getUserId(), sectionType)) {
                request.setAttribute("editError", "Không lưu được thay đổi điểm. Vui lòng thử lại.");
                request.setAttribute("formReason", reason);
                request.setAttribute("formReasonDetail", reasonDetail);
                doGet(request, response);
                return;
            }

            response.sendRedirect(request.getContextPath() + "/examiner/result-details?sbd="
                    + urlEncode(sbd) + "&reasonSaved=1");
            return;
        }

        doGet(request, response);
    }

    // Strip the servlet context path prefix from the request URI for multi-path routing.
    private String stripContextPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String ctx = request.getContextPath();
        if (ctx != null && !ctx.isEmpty() && uri.startsWith(ctx)) {
            return uri.substring(ctx.length());
        }
        return uri;
    }

    private String urlEncode(int value) {
        return URLEncoder.encode(String.valueOf(value), StandardCharsets.UTF_8);
    }

    // Parses pending delta list "deductionId:delta,..." and persists each change.
    private boolean applyPendingAdjustments(int examId, int sbd, String pendingAdjustments,
            Integer userId, SectionType sectionType) {
        if (pendingAdjustments == null || pendingAdjustments.isBlank()) {
            return true;
        }
        String[] tokens = pendingAdjustments.split(",");
        for (String token : tokens) {
            if (token == null || token.isBlank()) {
                continue;
            }
            String[] pair = token.trim().split(":");
            if (pair.length != 2) {
                return false;
            }
            int deductionId;
            int delta;
            try {
                deductionId = Integer.parseInt(pair[0].trim());
                delta = Integer.parseInt(pair[1].trim());
            } catch (NumberFormatException ex) {
                return false;
            }
            if (delta == 0) {
                continue;
            }
            if (!actionService.adjustScoreDeduction(examId, sbd, deductionId, delta, userId, sectionType).isSuccess()) {
                return false;
            }
        }
        return true;
    }
}
