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
import examiner.dto.ServiceResult;
import examiner.service.impl.ActionServiceImpl;
import examiner.service.impl.ExamViewServiceImpl;
import examiner.util.RequestUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import static shared.util.FormatUtil.formatPositiveInteger;
import examiner.util.ListUtil;
import java.util.List;
import java.util.Map;
import shared.enums.CandidateStatus;

@WebServlet(urlPatterns = {
    "/examiner/result-details",
    "/examiner/result-details-edit"
})
// Result details and edit screens: view practical results and update practical score with secure confirmation.
public class ResultServlet extends HttpServlet {

    protected final ExamViewService viewService = new ExamViewServiceImpl();
    protected final ActionService actionService = new ActionServiceImpl();

    // Route to result-details list or direct score-edit view.
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        Integer activeExamId = (Integer) session.getAttribute(Attributes.Examiner.ACTIVE_EXAM_ID);
        String path = RequestUtil.stripContextPath(request);
        Integer sbd = formatPositiveInteger(request.getParameter("sbd"));

        String search = request.getParameter("q");
        if (activeExamId != null && activeExamId > 0) {
            SectionType sectionType = ExaminerFilter.resolveSectionType(session);
            // Result edit flows apply to practical (layout) section only.
            if (sectionType == THEORY) {
                response.sendRedirect(request.getContextPath() + "/examiner/action?error=theoryNoResultEdit");
                return;
            }

            if ("/examiner/result-details".equals(path)) {
                // List view with optional selected candidate detail panel.
                List<CandidateRowDTO> candidates = viewService.getAllFilteredByExam(
                        activeExamId, sectionType, search);
                ListUtil.applySortAndSearch(request, candidates);
                request.setAttribute(Attributes.Request.CANDIDATES, candidates);
                if (sbd != null) {
                    CandidateRowDTO candidate = viewService.getCandidateViewRow(
                            activeExamId, sbd, sectionType);
                    if (candidate != null) {
                        request.setAttribute(Attributes.Request.CANDIDATE, candidate);
                    }
                }
            } else if (sbd != null && "/examiner/result-details-edit".equals(path)) {
                // Edit view loads candidate + current score for direct score update form.
                CandidateRowDTO candidate = viewService.getCandidateViewRow(activeExamId, sbd, sectionType);
                if (candidate == null || candidate.getSectionStatus() != CandidateStatus.COMPLETED) {
                    response.sendRedirect(request.getContextPath() + "/examiner/result-details?sbd="
                            + RequestUtil.urlEncode(sbd) + "&error=scoreEditNotAllowed");
                    return;
                }
                Map<String, Object> data = viewService.getResultDetailsViewByExam(
                        activeExamId, sbd, sectionType);
                RequestUtil.applyModel(request, data);
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

    // Save direct practical score update with password confirmation on the edit page.
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        Integer activeExamId = (Integer) session.getAttribute(Attributes.Examiner.ACTIVE_EXAM_ID);
        if (activeExamId == null || activeExamId <= 0) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String path = RequestUtil.stripContextPath(request);
        if ("/examiner/result-details-edit".equals(path)) {
            Integer sbd = formatPositiveInteger(request.getParameter("sbd"));

            if (sbd == null) {
                response.sendRedirect(request.getContextPath() + "/examiner/result-details?error=noSbd");
                return;
            }

            SectionType sectionType = ExaminerFilter.resolveSectionType(session);
            CandidateRowDTO candidate = viewService.getCandidateViewRow(activeExamId, sbd, sectionType);
            if (candidate == null || candidate.getSectionStatus() != CandidateStatus.COMPLETED) {
                response.sendRedirect(request.getContextPath() + "/examiner/result-details?sbd="
                        + RequestUtil.urlEncode(sbd) + "&error=scoreEditNotAllowed");
                return;
            }

            String reason = request.getParameter("reasonCode");
            String reasonDetail = request.getParameter("reasonDetail");
            String password = request.getParameter("confirmPassword");
            Integer newScore = formatPositiveInteger(request.getParameter("newScore"));
            UserDTO userDto = (UserDTO) session.getAttribute(Attributes.Session.USER);
            if (userDto == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            if (newScore == null || newScore < 0 || newScore > 100 || newScore % 5 != 0) {
                request.setAttribute(Attributes.Examiner.EDIT_ERROR, "Điểm mới phải từ 0 đến 100 và chia hết cho 5.");
                request.setAttribute(Attributes.Examiner.FORM_REASON, reason);
                request.setAttribute(Attributes.Examiner.FORM_REASON_DETAIL, reasonDetail);
                request.setAttribute(Attributes.Examiner.FORM_NEW_SCORE, request.getParameter("newScore"));
                doGet(request, response);
                return;
            }
            User user = userDto.toUser();
            ServiceResult<Void> updateResult = actionService.updatePracticalScoreWithReason(
                    activeExamId, sbd, newScore, user, password, reason, reasonDetail, user.getUserId(), sectionType);
            if (updateResult == null || !updateResult.isSuccess()) {
                request.setAttribute(Attributes.Examiner.EDIT_ERROR, buildEditErrorMessage(updateResult));
                request.setAttribute(Attributes.Examiner.FORM_REASON, reason);
                request.setAttribute(Attributes.Examiner.FORM_REASON_DETAIL, reasonDetail);
                request.setAttribute(Attributes.Examiner.FORM_NEW_SCORE, request.getParameter("newScore"));
                doGet(request, response);
                return;
            }

            response.sendRedirect(request.getContextPath() + "/examiner/result-details?sbd="
                    + RequestUtil.urlEncode(sbd) + "&saved=1");
            return;
        }

        doGet(request, response);
    }

    private String buildEditErrorMessage(ServiceResult<Void> result) {
        if (result == null || result.getMessage() == null) {
            return "Không lưu được thay đổi điểm. Vui lòng thử lại.";
        }
        return switch (result.getMessage()) {
            case "invalidScore" ->
                "Điểm mới phải từ 0 đến 100 và chia hết cho 5.";
            case "scoreEditNotAllowed" ->
                "Chỉ được sửa điểm khi thí sinh đã hoàn tất phần thi thực hành.";
            case "Mật khẩu xác nhận không đúng." ->
                "Mật khẩu xác nhận không đúng.";
            case "Vui lòng chọn lý do sửa điểm." ->
                "Vui lòng chọn lý do sửa điểm.";
            default ->
                "Không lưu được thay đổi điểm. Vui lòng thử lại.";
        };
    }
}
