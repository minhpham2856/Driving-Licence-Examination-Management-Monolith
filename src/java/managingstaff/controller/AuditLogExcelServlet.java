package managingstaff.controller;

import managingstaff.dao.AuditLogDAO;
import managingstaff.dao.impl.AuditLogDAOImpl;
import managingstaff.dto.AuditDTO;
import auth.dto.UserDTO;
import managingstaff.service.AuditLogExcelService;
import managingstaff.service.impl.AuditLogExcelServiceImpl;
import managingstaff.util.AuditLogHelper;
import managingstaff.util.Sanitize;
import managingstaff.util.SessionUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@WebServlet("/manager/audit/export")
public class AuditLogExcelServlet extends HttpServlet {

    private static final Set<String> ACTIONS = Set.of(
            "APPROVE", "INSERT", "UPDATE", "DELETE", "EXPORT",
            "ASSIGN", "IMPORT", "WARNING", "SYSTEM");
    private final AuditLogDAO auditDAO = new AuditLogDAOImpl();
    private final AuditLogExcelService excelService = new AuditLogExcelServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UserDTO currentUser = requireManager(request, response);
        if (currentUser == null) {
            return;
        }

        String keyword = Sanitize.text(request.getParameter("keyword"));
        String action = normalizeAction(request.getParameter("action"));
        String startDate = validDate(request.getParameter("startDate"));
        String endDate = validDate(request.getParameter("endDate"));
        if (startDate == null || endDate == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Ngày lọc không hợp lệ.");
            return;
        }
        if (!startDate.isEmpty() && !endDate.isEmpty()
                && LocalDate.parse(startDate).isAfter(LocalDate.parse(endDate))) {
            String swap = startDate;
            startDate = endDate;
            endDate = swap;
        }

        int total = auditDAO.countUserLogs(currentUser.getUserId(), keyword, action, startDate, endDate);
        List<AuditDTO> logs = auditDAO.searchUserLogsPaginated(
                currentUser.getUserId(), keyword, action, startDate, endDate, 1, Math.max(total, 1));
        String filename = "nhat-ky-thao-tac-"
                + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".xlsx";
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + filename + "\"; filename*=UTF-8''" + encoded);
        response.setHeader("Cache-Control", "no-store");
        excelService.writeAuditLogs(logs, response.getOutputStream());
        AuditLogHelper.persist(request.getSession(), "EXPORT AUDIT",
                "Xuất " + logs.size() + " bản ghi nhật ký thao tác");
    }

    private static String normalizeAction(String value) {
        String action = Sanitize.text(value).toUpperCase(Locale.ROOT);
        return ACTIONS.contains(action) ? action : "";
    }

    private static String validDate(String value) {
        String date = Sanitize.text(value);
        if (date.isEmpty()) {
            return "";
        }
        try {
            return LocalDate.parse(date).toString();
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private UserDTO requireManager(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        UserDTO user = SessionUtil.getCurrentUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return null;
        }
        if (!SessionUtil.isManager(user)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        return user;
    }
}
