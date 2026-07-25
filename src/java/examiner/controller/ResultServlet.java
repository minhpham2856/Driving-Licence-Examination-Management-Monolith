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
import java.util.HashMap;
import java.util.Map;
import shared.enums.CandidateStatus;

@WebServlet(urlPatterns = {
    "/examiner/result-details",
    "/examiner/result-details-edit"
})
// Result edit screen: update practical score via deduction list with password confirmation.
public class ResultServlet extends HttpServlet {

    protected final ExamViewService viewService = new ExamViewServiceImpl();
    protected final ActionService actionService = new ActionServiceImpl();

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

        if ("/examiner/result-details".equals(path)) {
            String redirect = request.getContextPath() + "/examiner/action";
            if (sbd != null) {
                redirect = request.getContextPath() + "/examiner/result-details-edit?sbd="
                        + RequestUtil.urlEncode(sbd);
            }
            response.sendRedirect(redirect);
            return;
        }

        if (activeExamId != null && activeExamId > 0) {
            SectionType sectionType = ExaminerFilter.resolveSectionType(session);
            if (sectionType == THEORY) {
                response.sendRedirect(request.getContextPath() + "/examiner/action?error=theoryNoResultEdit");
                return;
            }

            if (sbd != null && "/examiner/result-details-edit".equals(path)) {
                CandidateRowDTO candidate = viewService.getCandidateViewRow(activeExamId, sbd, sectionType);
                if (candidate == null || candidate.getSectionStatus() != CandidateStatus.COMPLETED) {
                    response.sendRedirect(request.getContextPath() + "/examiner/action?sbd="
                            + RequestUtil.urlEncode(sbd) + "&error=scoreEditNotAllowed");
                    return;
                }
                Map<String, Object> data = viewService.getResultDetailsViewByExam(
                        activeExamId, sbd, sectionType);
                RequestUtil.applyModel(request, data);
            } else if (sbd == null) {
                response.sendRedirect(request.getContextPath() + "/examiner/action?error=noSbd");
                return;
            }
        }

        request.getRequestDispatcher("/views/examiner/result-details-edit.jsp").forward(request, response);
    }

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
                response.sendRedirect(request.getContextPath() + "/examiner/action?error=noSbd");
                return;
            }

            SectionType sectionType = ExaminerFilter.resolveSectionType(session);
            CandidateRowDTO candidate = viewService.getCandidateViewRow(activeExamId, sbd, sectionType);
            if (candidate == null || candidate.getSectionStatus() != CandidateStatus.COMPLETED) {
                response.sendRedirect(request.getContextPath() + "/examiner/action?sbd="
                        + RequestUtil.urlEncode(sbd) + "&error=scoreEditNotAllowed");
                return;
            }

            String reason = request.getParameter("reasonCode");
            String reasonDetail = request.getParameter("reasonDetail");
            String password = request.getParameter("confirmPassword");
            UserDTO userDto = (UserDTO) session.getAttribute(Attributes.Session.USER);
            if (userDto == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            Map<Integer, Integer> occurrences = parseOccurrences(request);
            if (occurrences.isEmpty()) {
                request.setAttribute(Attributes.Examiner.EDIT_ERROR, "Vui lòng điều chỉnh danh sách lỗi trước khi lưu.");
                request.setAttribute(Attributes.Examiner.FORM_REASON, reason);
                request.setAttribute(Attributes.Examiner.FORM_REASON_DETAIL, reasonDetail);
                doGet(request, response);
                return;
            }
            User user = userDto.toUser();
            ServiceResult<Void> updateResult = actionService.updatePracticalScoreWithDeductions(
                    activeExamId, sbd, occurrences, user, password, reason, reasonDetail,
                    user.getUserId(), sectionType);
            if (updateResult == null || !updateResult.isSuccess()) {
                request.setAttribute(Attributes.Examiner.EDIT_ERROR, buildEditErrorMessage(updateResult));
                request.setAttribute(Attributes.Examiner.FORM_REASON, reason);
                request.setAttribute(Attributes.Examiner.FORM_REASON_DETAIL, reasonDetail);
                doGet(request, response);
                return;
            }

            response.sendRedirect(request.getContextPath() + "/examiner/action?saved=1&sbd="
                    + RequestUtil.urlEncode(sbd));
            return;
        }

        doGet(request, response);
    }

    private Map<Integer, Integer> parseOccurrences(HttpServletRequest request) {
        Map<Integer, Integer> occurrences = new HashMap<>();
        for (Map.Entry<String, String[]> item : request.getParameterMap().entrySet()) {
            if (!item.getKey().startsWith("deduction_")) {
                continue;
            }
            try {
                int deductionId = Integer.parseInt(item.getKey().substring("deduction_".length()));
                int count = Integer.parseInt(item.getValue()[0]);
                occurrences.put(deductionId, count);
            } catch (NumberFormatException ignored) {
            }
        }
        return occurrences;
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
            case "scorePayloadInvalid" ->
                "Danh sách lỗi trừ điểm không hợp lệ.";
            case "deductionInvalid" ->
                "Có lỗi trừ điểm không hợp lệ.";
            case "Mật khẩu xác nhận không đúng." ->
                "Mật khẩu xác nhận không đúng.";
            case "Vui lòng chọn lý do sửa điểm." ->
                "Vui lòng chọn lý do sửa điểm.";
            default ->
                "Không lưu được thay đổi điểm. Vui lòng thử lại.";
        };
    }
}
