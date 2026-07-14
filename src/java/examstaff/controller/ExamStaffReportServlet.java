package examstaff.controller;

import examstaff.dto.ExamReportProcedureStatusDTO;
import examstaff.dto.ExamReportStatsDTO;
import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.exam.ExamRegistrationDTO;
import examstaff.service.impl.ExamReportProcedureStatusServiceImpl;
import examstaff.service.impl.ExamReportStatsServiceImpl;
import examstaff.service.impl.StaffReportExportServiceImpl;
import examstaff.util.ExamStaffPageSupport;
import examstaff.util.ExamStaffPageSupport.PageContext;
import examstaff.util.ReportExportLabels;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import shared.Attributes;
import shared.model.Profile;
import shared.model.User;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@WebServlet("/views/staff/examstaff/report")
public class ExamStaffReportServlet extends HttpServlet {

    private final ExamReportStatsServiceImpl reportStatsService = new ExamReportStatsServiceImpl();
    private final StaffReportExportServiceImpl reportExportService = new StaffReportExportServiceImpl();
    private final ExamReportProcedureStatusServiceImpl procedureStatusService =
            new ExamReportProcedureStatusServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String webRoot = request.getServletContext().getRealPath("/");
        PageContext pageCtx = ExamStaffPageSupport.preparePageContext(request, true);
        List<ExamRegistrationDTO> qList = pageCtx.getCandidates();
        ExamSummaryDTO currentExam = (ExamSummaryDTO) request.getAttribute(Attributes.ExamStaff.CURRENT_EXAM);

        ExamReportProcedureStatusDTO procedureStatus = procedureStatusService.analyze(qList, webRoot);
        bindProcedureStatus(request, procedureStatus);
        int missingPhotoCount = procedureStatus.getMissingPhotoCount();

        request.setAttribute("candidateList", qList);
        bindReportStats(request, reportStatsService.computeStats(qList, pageCtx.getExamId()));

        boolean exportExcel = "true".equals(request.getParameter("exportExcel"));
        boolean exportPdf = "true".equals(request.getParameter("exportPdf"));
        if ((exportExcel || exportPdf) && missingPhotoCount > 0) {
            request.setAttribute("exportBlocked", true);
        }

        if (exportExcel && missingPhotoCount == 0) {
            streamExcel(response, request, currentExam, qList);
            return;
        }
        if (exportPdf && missingPhotoCount == 0) {
            request.setAttribute("autoPrint", Boolean.TRUE);
            request.getRequestDispatcher("/views/staff/examstaff/report-print.jsp").forward(request, response);
            return;
        }

        request.getRequestDispatcher("/views/staff/examstaff/report.jsp").forward(request, response);
    }

    private void bindProcedureStatus(HttpServletRequest request, ExamReportProcedureStatusDTO status) {
        if (request == null || status == null) {
            return;
        }
        request.setAttribute("missingPhotoCount", status.getMissingPhotoCount());
        request.setAttribute("missingPhotoSbds", status.getMissingPhotoSbds());
        request.setAttribute("missingPhotoCandidates", status.getMissingPhotoCandidates());
        request.setAttribute("procedurePendingCandidates", status.getProcedurePendingCandidates());
        request.setAttribute("procedureCompleteCount", status.getProcedureCompleteCount());
        request.setAttribute("procedurePendingCount", status.getProcedurePendingCount());
    }

    private void bindReportStats(HttpServletRequest request, ExamReportStatsDTO stats) {
        if (request == null || stats == null) {
            return;
        }
        request.setAttribute("totalCandidates", stats.getTotalCandidates());
        request.setAttribute("examCompletedCount", stats.getExamCompletedCount());
        request.setAttribute("passedCount", stats.getPassedCount());
        request.setAttribute("failedCount", stats.getFailedCount());
        request.setAttribute("absentCount", stats.getAbsentCount());
        request.setAttribute("suspendedCount", stats.getSuspendedCount());
        request.setAttribute("passRate", stats.getPassRate());
        request.setAttribute("licenseStats", stats.getLicenseStats());
        request.setAttribute("a1Count", stats.getA1Count());
        request.setAttribute("a1Completed", stats.getA1Completed());
        request.setAttribute("a1Passed", stats.getA1Passed());
        request.setAttribute("a1Failed", stats.getA1Failed());
        request.setAttribute("aCount", stats.getACount());
        request.setAttribute("aCompleted", stats.getACompleted());
        request.setAttribute("aPassed", stats.getAPassed());
        request.setAttribute("aFailed", stats.getAFailed());
        request.setAttribute("b1Count", stats.getB1Count());
        request.setAttribute("b1Completed", stats.getB1Completed());
        request.setAttribute("b1Passed", stats.getB1Passed());
        request.setAttribute("b1Failed", stats.getB1Failed());
        request.setAttribute("theoryCount", stats.getTheoryCount());
        request.setAttribute("theoryPassed", stats.getTheoryPassed());
        request.setAttribute("theoryFailed", stats.getTheoryFailed());
        request.setAttribute("practicalCount", stats.getPracticalCount());
        request.setAttribute("practicalPassed", stats.getPracticalPassed());
        request.setAttribute("practicalFailed", stats.getPracticalFailed());
        request.setAttribute("infractions", stats.getInfractions());
    }

    private void streamExcel(HttpServletResponse response, HttpServletRequest request,
            ExamSummaryDTO currentExam, List<ExamRegistrationDTO> qList) throws IOException {
        String token = ReportExportLabels.safeFileToken(
                currentExam != null ? currentExam.getSessionName() : "ca_thi");
        String datePart = new SimpleDateFormat("ddMMyyyy", Locale.forLanguageTag("vi-VN")).format(new Date());
        String filename = "bao_cao_ca_thi_" + token + "_" + datePart + ".xlsx";

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        ExamReportStatsDTO stats = reportStatsService.computeStats(qList);
        reportExportService.exportExamReport(
                response.getOutputStream(), currentExam, qList, stats, resolveExporterName(request));
        response.getOutputStream().flush();
    }

    private String resolveExporterName(HttpServletRequest request) {
        var session = request.getSession(false);
        if (session == null) {
            return "";
        }
        Object profileObj = session.getAttribute(Attributes.Session.USER_PROFILE);
        if (profileObj instanceof Profile profile && profile.getFullName() != null) {
            return profile.getFullName();
        }
        User user = (User) session.getAttribute(Attributes.Session.USER);
        return user != null ? user.getUsername() : "";
    }
}
