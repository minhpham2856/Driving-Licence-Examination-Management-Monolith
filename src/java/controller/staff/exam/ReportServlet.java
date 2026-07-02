package controller.staff.exam;

import dao.ExamRegistrationDAO;
import dao.ExamSessionDAO;
import dao.impl.ExamRegistrationDAOImpl;
import dao.impl.ExamSessionDAOImpl;
import dto.exam.ExamRegistrationDTO;
import dto.exam.SessionDTO;
import model.user.Profile;
import model.user.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@WebServlet("/views/staff/examstaff/report")
public class ReportServlet extends HttpServlet {

    private final ExamSessionDAO sessionDAO = new ExamSessionDAOImpl();
    private final ExamRegistrationDAO regDAO = new ExamRegistrationDAOImpl();

    // Xu ly yeu cau GET
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        String webRoot = request.getServletContext().getRealPath("/");
        ExamStaffViewHelper.ExamStaffPageContext pageCtx = ExamStaffViewHelper.prepareExamStaffPage(
                request, session, sessionDAO, webRoot);
        int examId = pageCtx.getExamId();
        List<ExamRegistrationDTO> qList = pageCtx.getCandidates();
        SessionDTO currentSession = (SessionDTO) request.getAttribute("currentSession");
        List<String> missingPhotoSbds = new ArrayList<>();
        List<ExamRegistrationDTO> missingPhotoCandidates = new ArrayList<>();
        List<ExamRegistrationDTO> procedurePendingCandidates = new ArrayList<>();
        int missingPhotoCount = 0;
        int procedureCompleteCount = 0;
        int procedurePendingCount = 0;
        for (ExamRegistrationDTO reg : qList) {
            boolean valid = CandidatePhotoHelper.resolveCapturedPhoto(webRoot, reg);
            if (reg.isAbsent()) {
                continue;
            }
            if (reg.isProcedureComplete()) {
                procedureCompleteCount++;
                continue;
            }
            procedurePendingCount++;
            procedurePendingCandidates.add(reg);
            if (!valid) {
                missingPhotoCount++;
                missingPhotoSbds.add(reg.getSbd() + " — " + reg.getName());
                missingPhotoCandidates.add(reg);
            }
        }
        request.setAttribute("missingPhotoCount", missingPhotoCount);
        request.setAttribute("missingPhotoSbds", missingPhotoSbds);
        request.setAttribute("missingPhotoCandidates", missingPhotoCandidates);
        request.setAttribute("procedurePendingCandidates", procedurePendingCandidates);
        request.setAttribute("procedureCompleteCount", procedureCompleteCount);
        request.setAttribute("procedurePendingCount", procedurePendingCount);

        boolean exportExcel = "true".equals(request.getParameter("exportExcel"));
        boolean exportPdf = "true".equals(request.getParameter("exportPdf"));
        boolean exportBlocked = (exportExcel || exportPdf) && missingPhotoCount > 0;
        if (exportBlocked) {
            request.setAttribute("exportBlocked", true);
        }

        request.setAttribute("candidateList", qList);
        ReportStatsHelper.populateReportAttributes(request, qList);

            // stream excel
        if (exportExcel && !exportBlocked) {
            streamExcel(response, request, currentSession, qList);
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

    @SuppressWarnings("unchecked")
    private void streamExcel(HttpServletResponse response, HttpServletRequest request,
            SessionDTO currentSession, List<ExamRegistrationDTO> qList) throws IOException {
        String token = ReportExportLabels.safeFileToken(
                currentSession != null ? currentSession.getSessionName() : "ca_thi");
        String datePart = new SimpleDateFormat("ddMMyyyy", Locale.forLanguageTag("vi-VN")).format(new Date());
        String filename = "bao_cao_ca_thi_" + token + "_" + datePart + ".xlsx";

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        ReportExportStats stats = ReportExportStats.fromRequest(request);
        List<Map<String, Object>> infractions = (List<Map<String, Object>>) request.getAttribute("infractions");
        String exporterName = resolveExporterName(request.getSession());

        ReportExcelExporter.exportExamReport(
                response.getOutputStream(),
                currentSession,
                qList,
                stats,
                infractions,
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
