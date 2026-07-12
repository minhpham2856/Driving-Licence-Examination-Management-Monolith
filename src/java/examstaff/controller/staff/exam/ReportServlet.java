package examstaff.controller.staff.exam;

import examstaff.service.ExamReportStatsService;
import examstaff.service.StaffReportExportService;
import examstaff.controller.staff.exam.binder.ReportProcedureStatusBinder;
import examstaff.controller.staff.exam.binder.ReportStatsBinder;
import examstaff.controller.staff.exam.module.ExamStaffWebModule;
import examstaff.controller.staff.exam.page.ExamStaffPageFacade;
import examstaff.service.ExamReportProcedureStatusService;
import examstaff.service.ExamStaffServices;
import examstaff.dto.ExamReportProcedureStatusDTO;
import examstaff.dto.exam.ExamRegistrationDTO;
import examstaff.dto.ExamReportStatsDTO;
import examstaff.dto.ExamSummaryDTO;
import shared.model.Profile;
import shared.model.User;
import examstaff.util.ReportExportLabels;
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

@WebServlet("/views/staff/examstaff/report")
public class ReportServlet extends HttpServlet {

    private static final ExamStaffServices SERVICES = new ExamStaffWebModule().services();

    private final ExamReportStatsService reportStatsService = SERVICES.reportStats();
    private final StaffReportExportService reportExportService = SERVICES.reportExport();
    private final ExamReportProcedureStatusService procedureStatusService = SERVICES.reportProcedureStatus();

    // Xu ly yeu cau GET
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
    // stream excel
    }

    private void streamExcel(HttpServletResponse response, HttpServletRequest request,
            ExamSummaryDTO currentExam, List<ExamRegistrationDTO> qList, int examId) throws IOException {
        String token = ReportExportLabels.safeFileToken(
                currentExam != null ? currentExam.getSessionName() : "ky_thi");
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
    // Xac dinh exporter name
        response.getOutputStream().flush();
    }

    private String resolveExporterName(HttpSession session) {
        Object profileObj = session.getAttribute("userProfile");
        if (profileObj instanceof Profile profile && profile.getFullName() != null) {
            return profile.getFullName();
        }
        User user = (User) session.getAttribute("user");
        return user != null ? user.getUsername() : "";
    }
}

