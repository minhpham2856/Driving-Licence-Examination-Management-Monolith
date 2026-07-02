package controller.staff.exam;

import dao.AuditLogDAO;
import dao.impl.AuditLogDAOImpl;
import dto.user.AuditDTO;
import model.Profile;
import model.User;
import util.SessionUserHelper;
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

@WebServlet("/views/staff/examstaff/audit-export")
public class AuditExportServlet extends HttpServlet {

    private final AuditLogDAO logDAO = new AuditLogDAOImpl();

    // Xu ly yeu cau GET
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int userId = SessionUserHelper.resolveUserId(session);
        String filterDate = resolveFilterDate(request);
        List<AuditDTO> personalLogs = loadLogs(userId, filterDate);
        var procedureKpi = logDAO.getStaffProcedureKpi(userId, filterDate);

        try {
            streamExcel(response, session, personalLogs, procedureKpi.getCompletedCount(),
                    procedureKpi.getTotalFees(), filterDate);
        } catch (Exception e) {
            e.printStackTrace();
            if (!response.isCommitted()) {
                response.reset();
                String redirect = request.getContextPath() + "/views/staff/examstaff/audit?exportError=1";
                if (filterDate != null && !filterDate.isBlank()) {
                    redirect += "&filterDate=" + java.net.URLEncoder.encode(filterDate, "UTF-8");
                }
                response.sendRedirect(redirect);
            }
        }
    }

    static void streamExcel(HttpServletResponse response, HttpSession session,
            List<AuditDTO> logs, int completedProcedures, double totalFees, String filterDate)
            throws IOException {
        response.reset();
        response.setBufferSize(128 * 1024);
        response.setCharacterEncoding("UTF-8");

        String stamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.forLanguageTag("vi-VN")).format(new Date());
        String filename = "nhat_ky_chi_tiet_" + stamp + ".xlsx";
        if (filterDate != null && !filterDate.isBlank()) {
            filename = "nhat_ky_" + filterDate.replace("-", "") + "_" + stamp + ".xlsx";
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        String staffName = "";
        Object profileObj = session.getAttribute("userProfile");
        if (profileObj instanceof Profile profile && profile.getFullName() != null) {
            staffName = profile.getFullName();
        } else {
            User user = (User) session.getAttribute("user");
            if (user != null) {
                staffName = user.getUsername();
            }
        }
        String scopeLabel = (filterDate != null && !filterDate.isBlank())
                ? "Ngày " + filterDate
                : "Tất cả lịch sử";

        AuditExcelExporter.export(response.getOutputStream(), logs, completedProcedures,
                totalFees, staffName, scopeLabel);
        response.getOutputStream().flush();
    }
    // Xac dinh filter date

    private static String resolveFilterDate(HttpServletRequest request) {
        String filterDate = request.getParameter("filterDate");
        if (filterDate == null || filterDate.isBlank()) {
            filterDate = request.getParameter("date");
        }
        return filterDate;
    // Tai logs
    }

    private List<AuditDTO> loadLogs(int userId, String filterDate) {
        try {
            if (filterDate != null && !filterDate.trim().isEmpty()) {
                return logDAO.getLogsByUserAndDate(userId, filterDate);
            }
            return logDAO.getLogsByUserAndDate(userId, null);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
