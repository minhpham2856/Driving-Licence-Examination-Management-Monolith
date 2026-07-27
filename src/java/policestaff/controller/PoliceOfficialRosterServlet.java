package policestaff.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import policestaff.dto.OfficialExamCandidateDTO;
import policestaff.dto.OfficialRosterPublishResult;
import policestaff.dto.PoliceSubmissionDTO;
import policestaff.service.PoliceDashboardService;
import policestaff.service.impl.PoliceDashboardServiceImpl;

/** Lập, chốt và xuất danh sách sát hạch chính thức. */
@WebServlet("/police/official-rosters")
public class PoliceOfficialRosterServlet extends HttpServlet {
    private final PoliceDashboardService service = new PoliceDashboardServiceImpl();
    private static final int ROSTER_PAGE_SIZE = 8;
    private static final int CANDIDATE_PAGE_SIZE = 15;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int dateId = integer(req.getParameter("dateId"));
        PoliceSubmissionDTO requested = dateId == 0 ? null : service.findSubmission(dateId);
        String activeStatus = status(req.getParameter("status"), requested);
        Integer year = "completed".equals(activeStatus) ? nullableInteger(req.getParameter("year")) : null;
        String dbStatus = "completed".equals(activeStatus) ? "COMPLETED" : "PENDING";
        int totalRosters = service.countSubmissions(dbStatus, year);
        int rosterPages = Math.max(1, (totalRosters + ROSTER_PAGE_SIZE - 1) / ROSTER_PAGE_SIZE);
        int rosterPage = Math.min(Math.max(1, integer(req.getParameter("page"))), rosterPages);
        List<PoliceSubmissionDTO> submissions =
                service.loadSubmissions(dbStatus, year, rosterPage, ROSTER_PAGE_SIZE);
        if (dateId == 0 && !submissions.isEmpty()) dateId = submissions.get(0).getExamDateId();
        PoliceSubmissionDTO selected = requested != null ? requested
                : (dateId == 0 ? null : service.findSubmission(dateId));
        if (dateId > 0 && selected == null) { resp.sendError(404); return; }
        if ("csv".equalsIgnoreCase(req.getParameter("export"))) {
            if (selected == null || !selected.isCompleted()) {
                resp.sendError(409, "Danh sách chưa được ban hành."); return;
            }
            writeCsv(resp, selected, service.loadOfficialCandidates(dateId)); return;
        }
        int totalCandidates = selected == null ? 0 : service.countOfficialCandidates(dateId);
        int candidatePages = Math.max(1, (totalCandidates + CANDIDATE_PAGE_SIZE - 1) / CANDIDATE_PAGE_SIZE);
        int candidatePage = Math.min(Math.max(1, integer(req.getParameter("candidatePage"))), candidatePages);
        List<OfficialExamCandidateDTO> candidates = selected == null ? List.of()
                : service.loadOfficialCandidates(dateId, candidatePage, CANDIDATE_PAGE_SIZE);
        req.setAttribute("submissions", submissions);
        req.setAttribute("selected", selected);
        req.setAttribute("activeStatus", activeStatus);
        req.setAttribute("pendingRosterCount", service.countSubmissions("PENDING", null));
        req.setAttribute("completedRosterCount", service.countSubmissions("COMPLETED", null));
        req.setAttribute("completedYears", service.loadCompletedYears());
        req.setAttribute("selectedYear", year);
        req.setAttribute("page", rosterPage);
        req.setAttribute("totalPages", rosterPages);
        req.setAttribute("totalRosters", totalRosters);
        req.setAttribute("candidatePage", candidatePage);
        req.setAttribute("candidatePages", candidatePages);
        req.setAttribute("totalOfficialCandidates", totalCandidates);
        req.setAttribute("officialCandidates", candidates);
        flash(req, "rosterSuccess");
        flash(req, "rosterError");
        req.getRequestDispatcher("/views/staff/policestaff/official-rosters.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int dateId = integer(req.getParameter("dateId"));
        try {
            String action = text(req, "action");
            if ("complete".equals(action)) {
                OfficialRosterPublishResult result = service.complete(dateId);
                String message = "Đã ban hành danh sách gồm " + result.getTotalCandidates() + " thí sinh.";
                if (result.isEmailConfigured()) {
                    message += " Đã gửi email tới " + result.getCentreEmailsSent()
                            + " tài khoản quản lý trung tâm và " + result.getCandidateEmailsSent() + " thí sinh.";
                } else {
                    message += " SMTP chưa cấu hình nên chưa gửi được email; trạng thái trên hệ thống vẫn đã cập nhật.";
                }
                success(req, message);
            } else {
                throw new IllegalArgumentException("Thao tác không hợp lệ.");
            }
        } catch (Exception ex) {
            req.getSession().setAttribute("rosterError", ex.getMessage());
        }
        resp.sendRedirect(req.getContextPath() + "/police/official-rosters?dateId=" + Math.max(dateId, 0));
    }

    private static void writeCsv(HttpServletResponse resp, PoliceSubmissionDTO submission,
            List<OfficialExamCandidateDTO> rows) throws IOException {
        resp.setContentType("text/csv;charset=UTF-8");
        resp.setHeader("Content-Disposition", "attachment; filename=official-list-"
                + submission.getExamDateId() + ".csv");
        StringBuilder csv = new StringBuilder(
                "Số báo danh,Họ và tên,Ngày sinh,CCCD,Hạng GPLX,Nội dung thi,Số điện thoại,Email\r\n");
        java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("dd/MM/yyyy");
        for (OfficialExamCandidateDTO row : rows) {
            String dob = row.getDateOfBirth() == null ? "" : format.format(row.getDateOfBirth());
            csv.append(cell(row.getCandidateNumber())).append(',').append(cell(row.getFullName())).append(',')
                    .append(cell(dob)).append(',').append(cell(row.getGovernmentIdNumber())).append(',')
                    .append(cell(submission.getLicenceClass())).append(',')
                    .append(cell(row.getExamParticipationLabel())).append(',')
                    .append(cell(row.getPhoneNumber())).append(',')
                    .append(cell(row.getEmail())).append("\r\n");
        }
        resp.getOutputStream().write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
        resp.getOutputStream().write(csv.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static String cell(String value) {
        String safe = value == null ? "" : value;
        return '"' + safe.replace("\"", "\"\"") + '"';
    }
    private static int integer(String value) {
        try { return Integer.parseInt(value); } catch (Exception ex) { return 0; }
    }
    private static Integer nullableInteger(String value) {
        int parsed = integer(value); return parsed > 0 ? parsed : null;
    }
    private static String text(HttpServletRequest req, String key) {
        String value = req.getParameter(key); return value == null ? "" : value.trim();
    }
    private static String status(String value, PoliceSubmissionDTO requested) {
        if ("completed".equalsIgnoreCase(value)) return "completed";
        if ("pending".equalsIgnoreCase(value)) return "pending";
        return requested != null && requested.isCompleted() ? "completed" : "pending";
    }
    private static void success(HttpServletRequest req, String value) {
        req.getSession().setAttribute("rosterSuccess", value);
    }
    private static void flash(HttpServletRequest req, String key) {
        Object value = req.getSession().getAttribute(key);
        if (value != null) {
            req.setAttribute(key, value); req.getSession().removeAttribute(key);
        }
    }
}
