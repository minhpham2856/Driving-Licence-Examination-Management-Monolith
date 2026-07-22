package examstaff.controller;

import examstaff.dto.CandidateQueueSnapshotDTO;
import examstaff.dto.ExamStaffPageCommand;
import examstaff.dto.ExamTransitionResultDTO;
import examstaff.dto.ServiceResult;
import shared.enums.ExamStaffMessage;
import examstaff.service.ExamStaffViewService;
import examstaff.service.impl.ExamStaffViewServiceImpl;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Chọn / đổi kỳ thi trên sidebar: processSelection → clear state → refresh queue → redirect PRG.
 *
 * Vai trò:
 * Xử lý picker kỳ thi trên sidebar: persist {@code selectedExamId}, xóa state bàn thủ tục khi đổi kỳ,
 * refresh queue session và flash thông báo. Pattern PRG — không render JSP trực tiếp.
 *
 * Luồng GET/POST:
 * - UTF-8 + no-cache → {@code viewService.processSelection}
 * - Lỗi → flash {@code examSelectError} → redirect dashboard
 * - Thành công: {@code applyExamIdFromRequest} → clear procedure (nếu đổi kỳ) → refresh queue
 * - Flash {@code examSelectMsg} → redirect Referer/dashboard kèm {@code examId} + cache-buster
 *
 * Ai gọi:
 * Form GET/POST trên sidebar mọi trang exam staff (picker {@code select-exam}).
 */
@WebServlet("/examstaff/select-exam")
public class ExamSelectServlet extends HttpServlet {

    private final ExamStaffViewService viewService = new ExamStaffViewServiceImpl();

    /** GET: ủy quyền {@link #handleSelect}. */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        handleSelect(request, response);
    }

    /** POST: ủy quyền {@link #handleSelect} (form chọn kỳ). */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        handleSelect(request, response);
    }

    /**
     * Luồng chọn kỳ: UTF-8 → processSelection → apply examId → clear procedure (nếu đổi) →
     * refresh queue → flash success → redirect Referer/dashboard kèm examId.
     * @throws IOException lỗi redirect
     */
    private void handleSelect(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Utf8EncodingHelper.apply(request, response);
        ExamStaffHttpSupport.applyNoCacheHeaders(response);
        HttpSession httpSession = request.getSession();
        try {
            // 1) Service quyết định transition
            ExamStaffPageCommand selectRequest = new ExamStaffPageCommand();
            selectRequest.setUrlExamId(ExamStaffHttpSupport.parseExamIdParam(request));
            selectRequest.setPreviousExamId(ExamStaffPageBinder.readSelectedExamId(httpSession));
            selectRequest.setWebRoot(request.getServletContext().getRealPath("/"));

            ServiceResult<ExamTransitionResultDTO> selectResult = viewService.processSelection(selectRequest);
            if (!selectResult.isSuccess()) {
                String error = selectResult.getMessage();
                if (error == null && selectResult.getData() != null) {
                    error = selectResult.getData().getErrorMessage();
                }
                httpSession.setAttribute("examSelectError", error);
                response.sendRedirect(ExamStaffHttpSupport.resolveSafeRedirect(request, "/examstaff/dashboard"));
                return;
            }

            ExamTransitionResultDTO result = selectResult.getData();
            ExamStaffPageSupport.applyExamIdFromRequest(request, httpSession,
                    viewService.listAllExams(), viewService);

            // 2) Đổi kỳ → xóa state bàn thủ tục cũ
            if (result != null && result.isClearProcedureState()) {
                ExamStaffPageBinder.clearProcedureStateOnExamChange(httpSession,
                        result.getNewExamId(), result.getNewExamId());
            }

            // 3) Nạp lại queue + đánh dấu vừa đổi kỳ
            int examId = result != null ? result.getExamId() : 0;
            refreshCandidateQueue(httpSession, examId,
                    selectRequest.getWebRoot(), viewService.listAllExams());

            httpSession.setAttribute("examStaffQueueRevision", System.currentTimeMillis());
            httpSession.setAttribute("examStaffExamJustChanged", Boolean.TRUE);
            httpSession.setAttribute("examSelectMsg", ExamStaffMessage.EXAM_SELECTED.getText());

            // 4) Redirect PRG giữ examId trên URL
            String redirect = ExamStaffHttpSupport.resolveSafeRedirect(request, "/examstaff/dashboard");
            redirect = ExamStaffHttpSupport.stripQueryString(redirect);

            int pickerExamId = ExamStaffHttpSupport.parseExamIdParam(request);
            if (pickerExamId > 0) {
                redirect = ExamStaffHttpSupport.upsertQueryParam(redirect, "examId", String.valueOf(pickerExamId));
            } else if (examId > 0) {
                redirect = ExamStaffHttpSupport.upsertQueryParam(redirect, "examId", String.valueOf(examId));
            }

            redirect = ExamStaffHttpSupport.upsertQueryParam(redirect, "_", String.valueOf(System.currentTimeMillis()));
            response.sendRedirect(redirect);
        } catch (Exception e) {
            e.printStackTrace();
            httpSession.setAttribute("examSelectError",
                    ExamStaffMessage.EXAM_CHANGE_ERROR_PREFIX.getText()
                            + (e.getMessage() != null ? e.getMessage() : ExamStaffMessage.UNKNOWN_ERROR.getText()));
            response.sendRedirect(ExamStaffHttpSupport.resolveSafeRedirect(request, "/examstaff/dashboard"));
        }
    }

    /**
     * Refresh queue từ DB rồi publish snapshot vào session (không bind request).
     * @param examId   kỳ cần nạp
     * @param webRoot  real path web
     * @param allExams danh sách kỳ
     */
    private void refreshCandidateQueue(HttpSession session, int examId,
            String webRoot, java.util.List<examstaff.dto.ExamSummaryDTO> allExams) {
        if (session == null) {
            return;
        }
        ExamStaffPageCommand input = new ExamStaffPageCommand();
        input.setExamId(examId);
        input.setWebRoot(webRoot);
        input.setAllExams(allExams);
        input.setSelectedExamId(ExamStaffPageBinder.readSelectedExamId(session));
        @SuppressWarnings("unchecked")
        java.util.List<String> order = (java.util.List<String>) session.getAttribute("callQueueOrder");
        input.setCallQueueOrder(order);
        input.setCallQueueOrderExamId(ExamStaffPageBinder.readCallQueueOrderExamId(session));

        CandidateQueueSnapshotDTO snapshot = viewService.refreshQueue(input);
        ExamStaffPageBinder.publishQueue(null, session, snapshot);
    }
}
