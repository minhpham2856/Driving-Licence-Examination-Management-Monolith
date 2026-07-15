package examstaff.controller;

import examstaff.dto.AuditDTO;
import examstaff.service.AuditService;
import examstaff.service.impl.AuditServiceImpl;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Xuất Excel nhật ký audit cá nhân (stream attachment); lỗi → redirect audit kèm exportError.
 */
@WebServlet("/examstaff/audit-export")
public class AuditExportServlet extends HttpServlet {

    private final AuditService auditService = new AuditServiceImpl();

    /**
     * GET: kiểm tra đăng nhập → load logs + KPI → streamExcel (hoặc redirect lỗi).
     *
     * @throws ServletException không dùng
     * @throws IOException      lỗi stream/redirect
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(Attributes.Session.USER) == null) {
            response.sendRedirect(request.getContextPath() + "/staff/login");
            return;
        }

        int userId = SessionUserHelper.resolveUserId(session);
        String filterDate = resolveFilterDate(request);
        List<AuditDTO> personalLogs = loadLogs(userId, filterDate);
        var procedureKpi = auditService.getStaffProcedureKpi(userId, filterDate);

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

    /**
     * Ghi file Excel nhật ký (header no-store + Content-Disposition).
     *
     * @param logs                danh sách log
     * @param completedProcedures KPI số thủ tục hoàn tất
     * @param totalFees           KPI tổng phí
     * @param filterDate          phạm vi ngày (null = tất cả)
     * @throws IOException lỗi ghi stream
     */
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

        auditService.exportAuditLog(response.getOutputStream(), logs, completedProcedures,
                totalFees, staffName, scopeLabel);
        response.getOutputStream().flush();
    }

    /**
     * Tên staff trên file: profile.fullName → username.
     *
     * @return tên hiển thị
     */
    private static String resolveStaffName(HttpSession session) {
        Object profileObj = session.getAttribute(Attributes.Session.USER_PROFILE);
        if (profileObj instanceof Profile profile && profile.getFullName() != null) {
            return profile.getFullName();
        }
        return SessionUserHelper.resolveUsername(session);
    }

    /**
     * Load log theo user + ngày; lỗi → list rỗng.
     *
     * @return danh sách AuditDTO (không null)
     */
    private List<AuditDTO> loadLogs(int userId, String filterDate) {
        try {
            return auditService.listLogsByUserAndDate(userId, filterDate);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Đọc {@code filterDate} (ưu tiên) hoặc {@code date} từ request.
     *
     * @return chuỗi ngày hoặc null
     */
    private static String resolveFilterDate(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String filterDate = request.getParameter("filterDate");
        if (filterDate == null || filterDate.isBlank()) {
            filterDate = request.getParameter("date");
        }
        return filterDate;
    }
}
