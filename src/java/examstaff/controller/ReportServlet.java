package examstaff.controller;

import auth.dto.UserDTO;
import examstaff.dto.ExamRegistrationDTO;
import examstaff.dto.ExamReportProcedureStatusDTO;
import examstaff.dto.ExamReportStatsDTO;
import examstaff.dto.ExamStaffPageContext;
import examstaff.dto.ExamSummaryDTO;
import examstaff.service.DocumentService;
import examstaff.service.ExamStaffViewService;
import examstaff.service.impl.DocumentServiceImpl;
import examstaff.service.impl.ExamStaffViewServiceImpl;
import examstaff.util.ExamStaffLabels;
import shared.Attributes;
import shared.model.Profile;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Báo cáo kỳ thi: thống kê + trạng thái thủ tục; xuất Excel/PDF hoặc forward {@code report.jsp}.
 *
 * Vai trò:
 * Trang báo cáo tổng hợp kỳ: KPI đậu/rớt/vắng, thống kê theo hạng GPLX, vi phạm,
 * trạng thái thủ tục (thiếu ảnh, chưa hoàn tất). Hỗ trợ export Excel/PDF qua {@link DocumentService}
 * (chặn export khi còn thiếu ảnh thủ tục).
 *
 * Luồng GET:
 * - {@code prepareExamStaffPage} → consume flash exam-control
 * - {@code analyzeProcedureStatus} + {@code computeReportStats} → bind KPI
 * - Nếu {@code exportExcel/Pdf} và không bị chặn → stream document
 * - Ngược lại → forward {@code report.jsp} (kèm cờ exportBlocked nếu thiếu ảnh)
 *
 * Ai gọi:
 * Menu exam staff; sidebar sau chọn kỳ; link export từ {@code report.jsp}.
 */
@WebServlet("/examstaff/report")
public class ReportServlet extends HttpServlet {

    private final ExamStaffViewService viewService = new ExamStaffViewServiceImpl();
    private final DocumentService documentService = new DocumentServiceImpl();

    /**
     * GET: prepare page → analyze procedure + stats → (exportExcel/Pdf nếu không bị chặn) hoặc JSP.
     * <p>
     * Export bị chặn khi còn thiếu ảnh thủ tục ({@code missingPhotoCount &gt; 0}).
     * @throws ServletException lỗi forward
     * @throws IOException      lỗi stream/export
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        String webRoot = request.getServletContext().getRealPath("/");

        // 1) Chuẩn bị kỳ + queue
        ExamStaffPageContext pageCtx = ExamStaffPageSupport.prepareExamStaffPage(
                request, session, webRoot, true, viewService);
        int examId = pageCtx.getExamId();
        List<ExamRegistrationDTO> qList = pageCtx.getCandidates();
        ExamSummaryDTO currentExam = (ExamSummaryDTO) request.getAttribute("currentExam");
        ExamStaffHttpSupport.consumeFlash(session, "examControlMsg", request, "examControlMsg");
        ExamStaffHttpSupport.consumeFlash(session, "examControlError", request, "examControlError");

        // 2) Phân tích thiếu ảnh / chưa xong thủ tục
        ExamReportProcedureStatusDTO procedureStatus = viewService.analyzeProcedureStatus(qList);
        if (procedureStatus != null) {
            request.setAttribute("missingPhotoCount", procedureStatus.getMissingPhotoCount());
            request.setAttribute("missingPhotoSbds", procedureStatus.getMissingPhotoSbds());
            request.setAttribute("missingPhotoCandidates", procedureStatus.getMissingPhotoCandidates());
            request.setAttribute("procedurePendingCandidates", procedureStatus.getProcedurePendingCandidates());
            request.setAttribute("procedureCompleteCount", procedureStatus.getProcedureCompleteCount());
            request.setAttribute("procedurePendingCount", procedureStatus.getProcedurePendingCount());
        }
        int missingPhotoCount = procedureStatus != null ? procedureStatus.getMissingPhotoCount() : 0;

        // 3) KPI báo cáo
        request.setAttribute("candidateList", qList);
        ExamReportStatsDTO stats = viewService.computeReportStats(qList, examId);
        if (stats != null) {
            request.setAttribute("totalCandidates", stats.getTotalCandidates());
            request.setAttribute("examCompletedCount", stats.getExamCompletedCount());
            request.setAttribute("passedCount", stats.getPassedCount());
            request.setAttribute("failedCount", stats.getFailedCount());
            request.setAttribute("absentCount", stats.getAbsentCount());
            request.setAttribute("suspendedCount", stats.getSuspendedCount());
            request.setAttribute("passRate", stats.getPassRate());
            request.setAttribute("licenseStats", stats.getLicenseStats());
            request.setAttribute("theoryCount", stats.getTheoryCount());
            request.setAttribute("theoryPassed", stats.getTheoryPassed());
            request.setAttribute("theoryFailed", stats.getTheoryFailed());
            request.setAttribute("practicalCount", stats.getPracticalCount());
            request.setAttribute("practicalPassed", stats.getPracticalPassed());
            request.setAttribute("practicalFailed", stats.getPracticalFailed());
            request.setAttribute("infractions", stats.getInfractions());
        }

        // 4) Nhánh export hoặc hiển thị
        boolean exportExcel = "true".equals(request.getParameter("exportExcel"));
        boolean exportPdf = "true".equals(request.getParameter("exportPdf"));
        boolean exportBlocked = (exportExcel || exportPdf) && missingPhotoCount > 0;
        if (exportBlocked) {
            request.setAttribute("exportBlocked", true);
        }

        if (exportExcel && !exportBlocked) {
            streamExcel(response, request, currentExam, qList, examId);
            return;
        }
        if (exportPdf && !exportBlocked) {
            request.setAttribute("autoPrint", Boolean.TRUE);
            request.getRequestDispatcher("/views/staff/examstaff/report-print.jsp").forward(request, response);
            return;
        }

        request.getRequestDispatcher("/views/staff/examstaff/report.jsp").forward(request, response);
    }

    /**
     * Stream file Excel báo cáo kỳ thi (Content-Disposition attachment).
     * @throws IOException lỗi ghi output stream
     */
    private void streamExcel(HttpServletResponse response, HttpServletRequest request,
            ExamSummaryDTO currentExam, List<ExamRegistrationDTO> qList, int examId) throws IOException {
        String token = ExamStaffLabels.safeFileToken(
                currentExam != null ? currentExam.getExamName() : "ky_thi");
        String datePart = new SimpleDateFormat("ddMMyyyy", Locale.forLanguageTag("vi-VN")).format(new Date());
        String filename = "bao_cao_ky_thi_" + token + "_" + datePart + ".xlsx";

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        ExamReportStatsDTO stats = viewService.computeReportStats(qList, examId);
        String exporterName = resolveExporterName(request.getSession());

        documentService.exportExamReport(
                response.getOutputStream(),
                currentExam,
                qList,
                stats,
                exporterName);
        response.getOutputStream().flush();
    }

    /**
     * Tên người xuất báo cáo: profile.fullName → user.profile → username.
     * @return tên hiển thị
     */
    private String resolveExporterName(HttpSession session) {
        Object profileObj = session.getAttribute(Attributes.Session.USER_PROFILE);
        if (profileObj instanceof Profile profile && profile.getFullName() != null) {
            return profile.getFullName();
        }
        Object userObj = session.getAttribute(Attributes.Session.USER);
        if (userObj instanceof UserDTO user && user.getProfile() != null
                && user.getProfile().getFullName() != null) {
            return user.getProfile().getFullName();
        }
        return SessionUserHelper.resolveUsername(session);
    }
}
