package examstaff.controller.staff.exam;

import examstaff.controller.staff.exam.http.AuditFilterSupport;
import examstaff.dto.user.AuditDTO;
import examstaff.controller.staff.exam.module.ExamStaffWebModule;
import shared.Attributes;
import shared.model.Profile;
import examstaff.service.ExamStaffServices;
import examstaff.service.StaffAuditExportService;
import examstaff.service.StaffAuditQueryService;
import examstaff.util.SessionUserHelper;
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

@WebServlet("/examstaff/audit-export")
public class AuditExportServlet extends HttpServlet {

    private static final ExamStaffServices SERVICES = new ExamStaffWebModule().services();

    private final StaffAuditQueryService auditQueryService = SERVICES.auditQuery();
    private final StaffAuditExportService auditExportService = SERVICES.auditExport();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(Attributes.Session.USER) == null) {
            response.sendRedirect(request.getContextPath() + "/staff/login");
            return;
        }

        int userId = SessionUserHelper.resolveUserId(session);
        String filterDate = AuditFilterSupport.resolveFilterDate(request);
        List<AuditDTO> personalLogs = loadLogs(userId, filterDate);
        var procedureKpi = auditQueryService.getStaffProcedureKpi(userId, filterDate);

        try {
            streamExcel(response, session, personalLogs, procedureKpi.getCompletedCount(),
                    procedureKpi.getTotalFees(), filterDate);
        } catch (Exception e) {
            e.printStackTrace();
            if (!response.isCommitted()) {
                response.reset();
                String redirect = request.getContextPath() + "/examstaff/audit?exportError=1";
                if (filterDate != null && !filterDate.isBlank()) {
                    redirect += "&filterDate=" + java.net.URLEncoder.encode(filterDate, "UTF-8");
                }
                response.sendRedirect(redirect);
            }
        }
    }

    private void streamExcel(HttpServletResponse response, HttpSession session,
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

        String staffName = resolveStaffName(session);
        String scopeLabel = (filterDate != null && !filterDate.isBlank())
                ? "Ngày " + filterDate
                : "Tất cả lịch sử";

        auditExportService.exportAuditLog(response.getOutputStream(), logs, completedProcedures,
                totalFees, staffName, scopeLabel);
        response.getOutputStream().flush();
    }

    private static String resolveStaffName(HttpSession session) {
        Object profileObj = session.getAttribute(Attributes.Session.USER_PROFILE);
        if (profileObj instanceof Profile profile && profile.getFullName() != null) {
            return profile.getFullName();
        }
        return SessionUserHelper.resolveUsername(session);
    }

    private List<AuditDTO> loadLogs(int userId, String filterDate) {
        try {
            return auditQueryService.listLogsByUserAndDate(userId, filterDate);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
