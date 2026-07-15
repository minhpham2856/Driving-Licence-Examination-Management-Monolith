package examstaff.controller;

import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.ExamStaffPageContext;
import examstaff.dto.ExaminerAllocationActionResultDTO;
import examstaff.dto.ExaminerAllocationViewDTO;
import examstaff.dto.ServiceResult;
import examstaff.service.AuditService;
import examstaff.service.ExamStaffViewService;
import examstaff.service.ExaminerAssignService;
import examstaff.service.impl.AuditServiceImpl;
import examstaff.service.impl.ExamStaffViewServiceImpl;
import examstaff.service.impl.ExaminerAssignServiceImpl;
import shared.Attributes;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

/**
 * Phân công sát hạch viên theo khu vực/kỳ: prepare → (assign/remove) → build view → JSP.
 */
@WebServlet("/examstaff/examiner-allocation")
public class ExaminerAllocationServlet extends HttpServlet {

    private final ExaminerAssignService assignService = new ExaminerAssignServiceImpl();
    private final ExamStaffViewService viewService = new ExamStaffViewServiceImpl();
    private final AuditService auditService = new AuditServiceImpl();

    /**
     * GET: no-cache → flash exam-control → prepare (không load candidates) → resolve kỳ →
     * xử lý action nếu có → bind allocation view → forward JSP.
     *
     * @throws ServletException lỗi forward
     * @throws IOException      lỗi I/O
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        ExamStaffHttpSupport.applyNoCacheHeaders(response);
        HttpSession session = request.getSession();

        request.removeAttribute("errorMsg");
        request.removeAttribute("alertMsg");

        // Flash từ exam-control sang alert/error trang phân công
        String examControlMsg = (String) session.getAttribute("examControlMsg");
        String examControlError = (String) session.getAttribute("examControlError");
        if (examControlMsg != null) {
            request.setAttribute("alertMsg", examControlMsg);
            session.removeAttribute("examControlMsg");
        }
        if (examControlError != null) {
            request.setAttribute("errorMsg", examControlError);
            session.removeAttribute("examControlError");
        }

        ExamStaffHttpSupport.consumeFlash(session, "examSelectMsg", request, "examSelectMsg");

        // Prepare sidebar/kỳ (không cần queue thí sinh)
        ExamStaffPageContext pageCtx = ExamStaffPageSupport.prepareExamStaffPage(
                request, session, getServletContext().getRealPath("/"), false, viewService);
        List<ExamSummaryDTO> allExams = pageCtx.getAllExams();
        int examId = pageCtx.getExamId();

        ExamSummaryDTO pickedFromUrl = ExamStaffPageSupport.resolveExamFromRequest(
                request, session, allExams, viewService);
        if (pickedFromUrl != null) {
            examId = pickedFromUrl.getExamId() > 0 ? pickedFromUrl.getExamId() : pickedFromUrl.getId();
        }

        ExamSummaryDTO currentExam = examId > 0 ? assignService.getExamById(examId) : null;
        if (currentExam == null && pickedFromUrl != null) {
            currentExam = pickedFromUrl;
        }
        if (currentExam == null && examId > 0) {
            currentExam = viewService.representativeExam(allExams, examId);
            if (currentExam != null) {
                examId = currentExam.getExamId() > 0 ? currentExam.getExamId() : currentExam.getId();
            }
        }

        request.setAttribute("allExams", allExams);
        request.setAttribute("currentExam", currentExam);
        ExamStaffPageBinder.bindExamShiftContext(request, currentExam);
        request.setAttribute("selectedExamId", examId > 0 ? examId : null);

        String action = request.getParameter("action");
        if (action != null && examId > 0) {
            handleAction(request, session, action);
        }

        if (examId > 0) {
            ExaminerAllocationViewDTO view = assignService.buildAllocationView(examId, examId, allExams);
            if (view != null) {
                request.setAttribute(Attributes.ExamStaff.EXAM_ASSIGNMENTS, view.getDayAssignments());
                request.setAttribute(Attributes.ExamStaff.ALL_EXAMINERS, view.getAllExaminers());
                request.setAttribute(Attributes.ExamStaff.AVAILABLE_EXAMINERS, view.getAvailableExaminers());
                request.setAttribute(Attributes.ExamStaff.BUSY_EXAMINERS, view.getBusyExaminers());
                request.setAttribute(Attributes.ExamStaff.AREA_ASSIGN_OPTIONS, view.getAreaAssignOptions());
                request.setAttribute(Attributes.ExamStaff.LOADED_EXAM_ID, examId);
            }
        }

        request.getRequestDispatcher("/views/staff/examstaff/examiner-allocation.jsp").forward(request, response);
    }

    /**
     * Xử lý assign / remove sát hạch viên rồi {@link #applyActionResult}.
     *
     * @param action {@code assign} hoặc {@code remove}
     */
    private void handleAction(HttpServletRequest request, HttpSession session, String action) {
        try {
            ServiceResult<ExaminerAllocationActionResultDTO> result;
            if ("assign".equals(action)) {
                int targetExamId = Integer.parseInt(request.getParameter("targetExamId"));
                int areaId = Integer.parseInt(request.getParameter("areaId"));
                int examinerUserId = Integer.parseInt(request.getParameter("examinerUserId"));
                result = assignService.assignExaminer(targetExamId, areaId, examinerUserId,
                        SessionUserHelper.resolveUserId(session));
            } else if ("remove".equals(action)) {
                result = assignService.removeExaminer(request.getParameter("slotKey"));
            } else {
                return;
            }
            applyActionResult(request, session, result);
        } catch (NumberFormatException e) {
            request.setAttribute("errorMsg", "Dữ liệu không hợp lệ.");
        }
    }

    /**
     * Bind alert/error từ ServiceResult và ghi audit nếu thành công có auditAction.
     */
    private void applyActionResult(HttpServletRequest request, HttpSession session,
            ServiceResult<ExaminerAllocationActionResultDTO> result) {
        ExaminerAllocationActionResultDTO data = result.getData();
        if (result.isSuccess()) {
            if (result.getMessage() != null) {
                request.setAttribute("alertMsg", result.getMessage());
            } else if (data != null && data.getAlertMsg() != null) {
                request.setAttribute("alertMsg", data.getAlertMsg());
            }
            if (data != null && data.getAuditAction() != null) {
                auditService.logAction(SessionUserHelper.resolveUserId(session),
                        data.getAuditAction(), data.getAuditDetails());
            }
            return;
        }
        if (result.getMessage() != null) {
            request.setAttribute("errorMsg", result.getMessage());
        } else if (data != null && data.getErrorMsg() != null) {
            request.setAttribute("errorMsg", data.getErrorMsg());
        }
    }

    /** POST dùng chung luồng GET (form assign/remove). */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
