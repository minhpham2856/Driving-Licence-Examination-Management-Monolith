package managingstaff.controller;

import managingstaff.dao.ManagementReportDAO;
import managingstaff.dao.impl.ManagementReportDAOImpl;
import managingstaff.dto.ManagementReportRowDTO;
import auth.dto.UserDTO;
import managingstaff.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Year;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@WebServlet("/manager/reports")
public class ManagementReportServlet extends HttpServlet {

    private static final String VIEW = "/views/staff/managingstaff/report.jsp";
    private static final Set<String> GROUPS = Set.of("exam", "month", "year");
    private static final Set<String> LICENCE_CLASSES = Set.of("A1", "A", "B1");

    private final ManagementReportDAO reportDAO = new ManagementReportDAOImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!hasAccess(request, response)) return;

        String periodGroup = normalizeGroup(request.getParameter("periodGroup"));
        String licenceClass = normalizeLicence(request.getParameter("licenceClass"));
        int examId = parsePositiveInt(request.getParameter("examId"));

        List<Integer> availableYears = reportDAO.findAvailableYears();
        int defaultYear = availableYears.isEmpty() ? Year.now().getValue() : availableYears.get(0);
        int selectedYear = parsePositiveInt(request.getParameter("year"));
        if (selectedYear <= 0) selectedYear = defaultYear;

        List<ManagementReportRowDTO> rows = reportDAO.findReportRows(
                periodGroup, examId, selectedYear, licenceClass);

        int total = rows.stream().mapToInt(ManagementReportRowDTO::getTotalCount).sum();
        int passed = rows.stream().mapToInt(ManagementReportRowDTO::getPassCount).sum();
        int failed = rows.stream().mapToInt(ManagementReportRowDTO::getFailCount).sum();
        int evaluated = passed + failed;
        double passRate = evaluated == 0 ? 0.0
                : Math.round(passed * 1000.0 / evaluated) / 10.0;

        request.setAttribute("reportReady", true);
        request.setAttribute("reportData", rows);
        request.setAttribute("examOptions", reportDAO.findExamOptions());
        request.setAttribute("availableYears", availableYears);
        request.setAttribute("periodGroup", periodGroup);
        request.setAttribute("selectedExamId", examId);
        request.setAttribute("selectedYear", selectedYear);
        request.setAttribute("selectedLicence", licenceClass);
        request.setAttribute("periodGroupLabel", groupLabel(periodGroup));
        request.setAttribute("totalCandidates", total);
        request.setAttribute("totalPassed", passed);
        request.setAttribute("totalFailed", failed);
        request.setAttribute("passRateAll", passRate);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    private boolean hasAccess(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        UserDTO user = SessionUtil.getCurrentUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        if (!SessionUtil.isManager(user)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        return true;
    }

    private static String normalizeGroup(String value) {
        String group = value == null ? "exam" : value.trim().toLowerCase(Locale.ROOT);
        return GROUPS.contains(group) ? group : "exam";
    }

    private static String normalizeLicence(String value) {
        String licence = value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
        return LICENCE_CLASSES.contains(licence) ? licence : "";
    }

    private static int parsePositiveInt(String value) {
        try {
            int parsed = Integer.parseInt(value);
            return Math.max(parsed, 0);
        } catch (Exception ignored) {
            return 0;
        }
    }

    private static String groupLabel(String group) {
        return switch (group) {
            case "month" -> "Theo tháng";
            case "year" -> "Theo năm";
            default -> "Theo kỳ thi";
        };
    }
}
