package examstaff.controller.staff.exam;

import examstaff.service.ExamReportStatsService;
import examstaff.service.StaffReportExportService;
import examstaff.controller.staff.exam.binder.ReportProcedureStatusBinder;
import examstaff.controller.staff.exam.binder.ReportStatsBinder;
import examstaff.controller.staff.exam.http.ExamStaffHttpSupport;
import examstaff.controller.staff.exam.module.ExamStaffWebModule;
import examstaff.controller.staff.exam.page.ExamStaffPageFacade;
import examstaff.service.ExamReportProcedureStatusService;
import examstaff.service.ExamStaffServices;
import examstaff.dto.ExamReportProcedureStatusDTO;
import examstaff.dto.exam.ExamRegistrationDTO;
import examstaff.dto.ExamReportStatsDTO;
import examstaff.dto.ExamSummaryDTO;
import examstaff.util.ReportExportLabels;
import examstaff.util.SessionUserHelper;
import auth.dto.UserDTO;
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
 * Trang staff Báo cáo kỳ thi: điều phối HTTP ↔ thống kê/xuất Excel/PDF.
 * Action ghi trạng thái không có; chỉ đọc queue và stream/export.
 */
@WebServlet("/views/staff/examstaff/report")
public class ReportServlet extends HttpServlet {

    private static final ExamStaffServices SERVICES = ExamStaffWebModule.getInstance().services();

    private final ExamReportStatsService reportStatsService = SERVICES.reportStats();
    private final StaffReportExportService reportExportService = SERVICES.reportExport();
    private final ExamReportProcedureStatusService procedureStatusService = SERVICES.reportProcedureStatus();

    /**
     * GET trang báo cáo: prepare page → bind stats/procedure status → forward JSP
     * hoặc stream Excel / in PDF khi có query export.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        String webRoot = request.getServletContext().getRealPath("/");
        ExamStaffPageFacade.ExamStaffPageContext pageCtx = ExamStaffPageFacade.prepareExamStaffPage(
                request, session, webRoot);
        int examId = pageCtx.getExamId();
        List<ExamRegistrationDTO> qList = pageCtx.getCandidates();
        ExamSummaryDTO currentExam = (ExamSummaryDTO) request.getAttribute("currentExam");
        ExamStaffHttpSupport.consumeFlash(session, "examControlMsg", request, "examControlMsg");
        ExamStaffHttpSupport.consumeFlash(session, "examControlError", request, "examControlError");
        ExamReportProcedureStatusDTO procedureStatus = procedureStatusService.analyze(qList, webRoot);
        ReportProcedureStatusBinder.bind(request, procedureStatus);
        int missingPhotoCount = procedureStatus.getMissingPhotoCount();

        request.setAttribute("candidateList", qList);
        ReportStatsBinder.bind(request, reportStatsService.computeStats(qList, examId));

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
     * Stream file Excel báo cáo kỳ thi ra response (Content-Disposition attachment).
     *
     * @param currentExam kỳ thi đang chọn (đặt tên file)
     * @param qList       danh sách thí sinh
     * @param examId      mã kỳ để tính thống kê
     */
    private void streamExcel(HttpServletResponse response, HttpServletRequest request,
            ExamSummaryDTO currentExam, List<ExamRegistrationDTO> qList, int examId) throws IOException {
        String token = ReportExportLabels.safeFileToken(
                currentExam != null ? currentExam.getExamName() : "ky_thi");
        String datePart = new SimpleDateFormat("ddMMyyyy", Locale.forLanguageTag("vi-VN")).format(new Date());
        String filename = "bao_cao_ky_thi_" + token + "_" + datePart + ".xlsx";

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        ExamReportStatsDTO stats = reportStatsService.computeStats(qList, examId);
        String exporterName = resolveExporterName(request.getSession());

        reportExportService.exportExamReport(
                response.getOutputStream(),
                currentExam,
                qList,
                stats,
                exporterName);
        response.getOutputStream().flush();
    }

    /** Lấy tên người xuất báo cáo từ Profile/User session; fallback username. */
    private String resolveExporterName(HttpSession session) {
        Object profileObj = session.getAttribute(Attributes.Session.USER_PROFILE);
        if (profileObj instanceof Profile) {
            Profile profile = (Profile) profileObj;
            if (profile.getFullName() != null && !profile.getFullName().isBlank()) {
                return profile.getFullName();
            }
        }
        Object userObj = session.getAttribute(Attributes.Session.USER);
        if (userObj instanceof UserDTO) {
            UserDTO user = (UserDTO) userObj;
            if (user.getProfile() != null && user.getProfile().getFullName() != null
                    && !user.getProfile().getFullName().isBlank()) {
                return user.getProfile().getFullName();
            }
        }
        return SessionUserHelper.resolveUsername(session);
    }
}
