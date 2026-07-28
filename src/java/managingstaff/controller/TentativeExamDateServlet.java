package managingstaff.controller;

import auth.dto.UserDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import managingstaff.dao.DossierDAO;
import managingstaff.dao.LicenceDAO;
import managingstaff.dao.TentativeExamDateDAO;
import managingstaff.dao.impl.DossierDAOImpl;
import managingstaff.dao.impl.LicenceDAOImpl;
import managingstaff.dao.impl.TentativeExamDateDAOImpl;
import managingstaff.dto.DossierDTO;
import managingstaff.dto.TentativeExamDateDTO;
import managingstaff.service.ApprovedCandidateExcelService;
import managingstaff.service.EmailService;
import managingstaff.service.impl.ApprovedCandidateExcelServiceImpl;
import managingstaff.service.impl.EmailServiceImpl;
import managingstaff.util.AuditLogHelper;
import managingstaff.util.SessionUtil;
import shared.util.TentativeExamDatePolicy;

@WebServlet("/manager/tentative-exam-dates")
public class TentativeExamDateServlet extends HttpServlet {

    private static final int DATE_PAGE_SIZE = 10, CANDIDATE_PAGE_SIZE = 15;
    private final TentativeExamDateDAO dateDAO = new TentativeExamDateDAOImpl();
    private final DossierDAO dossierDAO = new DossierDAOImpl();
    private final LicenceDAO licenceDAO = new LicenceDAOImpl();
    private final ApprovedCandidateExcelService excelService = new ApprovedCandidateExcelServiceImpl();
    private final EmailService emailService = new EmailServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!authorized(req, resp)) {
            return;
        }
        int dateId = integer(req.getParameter("dateId"));
        String export = req.getParameter("export");
        if (dateId > 0 && export != null) {
            export(req, resp, dateId, export);
            return;
        }
        String tab = tab(req.getParameter("tab"));
        int page = Math.max(1, integer(req.getParameter("page")));
        int total = dateDAO.countAll(tab);
        int pages = Math.max(1, (total + DATE_PAGE_SIZE - 1) / DATE_PAGE_SIZE);
        page = Math.min(page, pages);
        req.setAttribute("dates", dateDAO.findPage(tab, page, DATE_PAGE_SIZE));
        req.setAttribute("licences", licenceDAO.findAll());
        req.setAttribute("today", java.time.LocalDate.now().toString());
        req.setAttribute("minimumExamDate",
                TentativeExamDatePolicy.earliestCreatableDate(java.time.LocalDate.now()).toString());
        req.setAttribute("currentPage", page);
        req.setAttribute("totalPages", pages);
        req.setAttribute("totalItems", total);
        req.setAttribute("activeTab", tab);
        req.setAttribute("activeCount", dateDAO.countAll("active"));
        req.setAttribute("expiredCount", dateDAO.countAll("expired"));
        req.setAttribute("cancelledCount", dateDAO.countAll("cancelled"));
        List<TentativeExamDateDTO> policeReturned = dateDAO.findPoliceCompletedUnlinked();
        req.setAttribute("policeReturnedDates", policeReturned);
        req.setAttribute("policeReturnedCount", policeReturned.size());
        if (dateId > 0) {
            TentativeExamDateDTO selected = dateDAO.findById(dateId);
            if (selected == null) {
                resp.sendError(404);
                return;
            }
            int cp = Math.max(1, integer(req.getParameter("candidatePage")));
            int ct = selected.isCancelled()
                    ? selected.getCancelledRegistrationCount()
                    : dateDAO.countRegistrations(dateId);
            int cps = Math.max(1, (ct + CANDIDATE_PAGE_SIZE - 1) / CANDIDATE_PAGE_SIZE);
            cp = Math.min(cp, cps);
            req.setAttribute("selectedDate", selected);
            req.setAttribute("candidates", selected.isCancelled()
                    ? List.of()
                    : dossiers(dateDAO.findRegistrationIds(dateId, cp, CANDIDATE_PAGE_SIZE)));
            req.setAttribute("candidatePage", cp);
            req.setAttribute("candidatePages", cps);
            req.setAttribute("candidateTotal", ct);
        }
        req.getRequestDispatcher("/views/staff/managingstaff/tentative-exam-dates.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!authorized(req, resp)) {
            return;
        }
        if ("cancel".equals(req.getParameter("action"))) {
            int id = integer(req.getParameter("dateId"));
            String reason = req.getParameter("cancelReason");
            try {
                TentativeExamDateDTO cancelledDate = dateDAO.findById(id);
                List<DossierDTO> recipients = cancelledDate == null
                        ? List.of()
                        : dossiers(dateDAO.findAllRegistrationIds(id));
                int affected = dateDAO.cancel(id, reason,
                        SessionUtil.currentUserId(req.getSession(false)));
                int sent = sendCancellationEmails(cancelledDate, recipients, reason);
                AuditLogHelper.persistChange(req.getSession(), "CANCEL TENTATIVE EXAM DATE",
                        "Hủy ngày thi dự kiến #" + id + ". Lý do: " + reason.trim()
                                + ". Đã hủy " + affected + " lựa chọn ngày và gửi "
                                + sent + " email.",
                        "Open", "Cancelled", "ExamDates", id);
                String emailResult = emailService.isConfigured()
                        ? " Đã gửi email cho " + sent + "/" + affected + " thí sinh."
                        : " Chưa gửi email vì SMTP chưa được cấu hình.";
                req.getSession().setAttribute("tentativeSuccess",
                        "Đã hủy ngày thi dự kiến và hủy " + affected
                                + " lựa chọn ngày của thí sinh." + emailResult);
                resp.sendRedirect(req.getContextPath()
                        + "/manager/tentative-exam-dates?tab=cancelled&dateId=" + id);
            } catch (Exception e) {
                req.getSession().setAttribute("tentativeError", e.getMessage());
                resp.sendRedirect(req.getContextPath()
                        + "/manager/tentative-exam-dates?tab=active&dateId=" + Math.max(id, 0));
            }
            return;
        }
        if ("sendPolice".equals(req.getParameter("action"))) {
            int id = integer(req.getParameter("dateId"));
            try {
                int count = dateDAO.submitToPolice(id);
                AuditLogHelper.persistChange(req.getSession(), "SEND POLICE DOSSIERS",
                        "Gửi ngày thi dự kiến #" + id + " cùng " + count + " hồ sơ tới CSGT.",
                        "NOT_SENT", "PENDING", "ExamDates", id);
                req.getSession().setAttribute("tentativeSuccess",
                        "Đã gửi " + count + " hồ sơ tới CSGT. Danh sách đã đóng và không thể sửa.");
            } catch (Exception ex) {
                req.getSession().setAttribute("tentativeError", ex.getMessage());
            }
            resp.sendRedirect(req.getContextPath()
                    + "/manager/tentative-exam-dates?tab=active&dateId=" + Math.max(id, 0));
            return;
        }
        try {
            Date date = Date.valueOf(req.getParameter("examDate"));
            int licenceId = integer(req.getParameter("licenceId"));
            if (licenceDAO.findById(licenceId) == null) {
                throw new IllegalArgumentException("Hạng GPLX không hợp lệ.");
            }
            int id = dateDAO.create(date, licenceId);
            req.getSession().setAttribute("tentativeSuccess",
                    "Đã tạo ngày thi dự kiến. Số lượng hợp lệ: từ 10 đến 50 thí sinh.");
            resp.sendRedirect(req.getContextPath() + "/manager/tentative-exam-dates?dateId=" + id);
        } catch (Exception e) {
            req.getSession().setAttribute("tentativeError", e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/manager/tentative-exam-dates");
        }
    }

    private int sendCancellationEmails(TentativeExamDateDTO date,
            List<DossierDTO> recipients, String reason) {
        if (!emailService.isConfigured() || date == null || date.getExamDate() == null) {
            return 0;
        }
        String formattedDate = date.getExamDate().toLocalDate()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        int sent = 0;
        for (DossierDTO dossier : recipients) {
            if (dossier == null || dossier.getUser() == null) {
                continue;
            }
            String fullName = dossier.getProfile() == null
                    || dossier.getProfile().getFullName() == null
                    || dossier.getProfile().getFullName().isBlank()
                    ? "thí sinh" : dossier.getProfile().getFullName();
            String body = "Xin chào " + fullName + ",\n\n"
                    + "Trung tâm đã hủy ngày thi dự kiến " + formattedDate
                    + ", hạng " + date.getLicenceClass() + ".\n"
                    + "Lý do: " + reason.trim() + ".\n\n"
                    + "Lựa chọn ngày cũ của bạn đã được hủy. Hồ sơ đã duyệt và "
                    + "tài liệu vẫn được giữ nguyên; vui lòng đăng nhập và thực hiện "
                    + "lại bước đăng ký ngày thi dự kiến từ đầu.";
            if (emailService.sendTextEmail(dossier.getUser().getEmail(),
                    "Thông báo hủy ngày thi dự kiến", body)) {
                sent++;
            }
        }
        return sent;
    }

    private void export(HttpServletRequest req, HttpServletResponse resp, int dateId, String type) throws IOException {
        TentativeExamDateDTO date = dateDAO.findById(dateId);
        if (date == null) {
            resp.sendError(404);
            return;
        }
        if (date.isCancelled()) {
            resp.sendError(409, "Ngày thi dự kiến đã bị hủy.");
            return;
        }
        List<DossierDTO> rows = dossiers(dateDAO.findAllRegistrationIds(dateId));
        String base = "ho-so-du-kien-" + date.getExamDate() + "-" + date.getLicenceClass();
        if ("excel".equals(type)) {
            resp.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            download(resp, base + ".xlsx");
            excelService.writeApprovedCandidates(date.getLicenceClass(), rows, resp.getOutputStream());
            return;
        }
        resp.sendError(400, "Loại file không hợp lệ.");
    }

    private List<DossierDTO> dossiers(List<Integer> ids) {
        List<DossierDTO> out = new ArrayList<>();
        for (Integer id : ids) {
            DossierDTO d = dossierDAO.findByRegistrationId(id);
            if (d != null) {
                out.add(d);
            }
        }
        return out;
    }

    private static void download(HttpServletResponse r, String name) {
        r.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + URLEncoder.encode(name, StandardCharsets.UTF_8).replace("+", "%20"));
    }

    private boolean authorized(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        UserDTO u = SessionUtil.getCurrentUser(req);
        if (u == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return false;
        }
        if (!SessionUtil.isManager(u)) {
            resp.sendError(403);
            return false;
        }
        return true;
    }

    private static int integer(String s) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return 0;
        }
    }

    private static String tab(String value) {
        if ("expired".equalsIgnoreCase(value)) return "expired";
        if ("cancelled".equalsIgnoreCase(value)) return "cancelled";
        return "active";
    }
}
