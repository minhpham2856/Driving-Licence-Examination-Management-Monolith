package Controllers.Staff.ExamStaff;

import DAO.ExamRegistrationDAO;
import DAO.ExamSessionDAO;
import DAO.Impl.ExamRegistrationDAOImpl;
import DAO.Impl.ExamSessionDAOImpl;
import Models.ExamRegistration;
import Models.ExamSession;
import Models.User;
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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        int sessId = ExamStaffViewHelper.resolveSessionId(request, session, 2);

        ExamSession currentSession;
        try {
            currentSession = sessionDAO.getById(sessId);
        } catch (Exception e) {
            e.printStackTrace();
            currentSession = null;
        }
        if (currentSession != null) {
            request.setAttribute("currentSession", currentSession);
        }

        List<ExamRegistration> qList;
        try {
            qList = regDAO.getCandidatesBySession(sessId);
        } catch (Exception e) {
            e.printStackTrace();
            qList = new ArrayList<>();
        }
        if (qList == null) {
            qList = new ArrayList<>();
        }

        String webRoot = request.getServletContext().getRealPath("/");
        List<String> missingPhotoSbds = new ArrayList<>();
        List<ExamRegistration> missingPhotoCandidates = new ArrayList<>();
        int missingPhotoCount = 0;
        int procedureCompleteCount = 0;
        int procedurePendingCount = 0;
        for (ExamRegistration reg : qList) {
            boolean valid = CandidatePhotoHelper.resolveCapturedPhoto(webRoot, reg);
            reg.setValidCapturedPhoto(valid);
            if (reg.isAbsent()) {
                continue;
            }
            if (reg.isProcedureComplete()) {
                procedureCompleteCount++;
                continue;
            }
            procedurePendingCount++;
            if (!valid) {
                missingPhotoCount++;
                missingPhotoSbds.add(reg.getSbd() + " — " + reg.getName());
                missingPhotoCandidates.add(reg);
            }
        }
        request.setAttribute("missingPhotoCount", missingPhotoCount);
        request.setAttribute("missingPhotoSbds", missingPhotoSbds);
        request.setAttribute("missingPhotoCandidates", missingPhotoCandidates);
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
    }

    @SuppressWarnings("unchecked")
    private void streamExcel(HttpServletResponse response, HttpServletRequest request,
            ExamSession currentSession, List<ExamRegistration> qList) throws IOException {
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
        response.getOutputStream().flush();
    }

    private String resolveExporterName(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null && user.getPerson() != null && user.getPerson().getFullName() != null) {
            return user.getPerson().getFullName();
        }
        return "";
    }
}
