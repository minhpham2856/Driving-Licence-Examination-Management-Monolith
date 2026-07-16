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
import java.nio.file.Path;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import managingstaff.dao.DossierDAO;
import managingstaff.dao.LicenceDAO;
import managingstaff.dao.TentativeExamDateDAO;
import managingstaff.dao.impl.DossierDAOImpl;
import managingstaff.dao.impl.LicenceDAOImpl;
import managingstaff.dao.impl.TentativeExamDateDAOImpl;
import managingstaff.dto.DossierDTO;
import managingstaff.dto.TentativeExamDateDTO;
import managingstaff.service.ApprovedCandidateExcelService;
import managingstaff.service.DossierPdfService;
import managingstaff.service.impl.ApprovedCandidateExcelServiceImpl;
import managingstaff.service.impl.AwtDossierPdfService;
import managingstaff.util.SessionUtil;

@WebServlet("/manager/tentative-exam-dates")
public class TentativeExamDateServlet extends HttpServlet {

    private static final int DATE_PAGE_SIZE = 10, CANDIDATE_PAGE_SIZE = 15;
    private final TentativeExamDateDAO dateDAO = new TentativeExamDateDAOImpl();
    private final DossierDAO dossierDAO = new DossierDAOImpl();
    private final LicenceDAO licenceDAO = new LicenceDAOImpl();
    private final DossierPdfService pdfService = new AwtDossierPdfService();
    private final ApprovedCandidateExcelService excelService = new ApprovedCandidateExcelServiceImpl();

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
        String tab = "expired".equalsIgnoreCase(req.getParameter("tab")) ? "expired" : "active";
        int page = Math.max(1, integer(req.getParameter("page")));
        int total = dateDAO.countAll(tab);
        int pages = Math.max(1, (total + DATE_PAGE_SIZE - 1) / DATE_PAGE_SIZE);
        page = Math.min(page, pages);
        req.setAttribute("dates", dateDAO.findPage(tab, page, DATE_PAGE_SIZE));
        req.setAttribute("licences", licenceDAO.findAll());
        req.setAttribute("today", java.time.LocalDate.now().toString());
        req.setAttribute("currentPage", page);
        req.setAttribute("totalPages", pages);
        req.setAttribute("totalItems", total);
        req.setAttribute("activeTab", tab);
        req.setAttribute("activeCount", dateDAO.countAll("active"));
        req.setAttribute("expiredCount", dateDAO.countAll("expired"));
        if (dateId > 0) {
            TentativeExamDateDTO selected = dateDAO.findById(dateId);
            if (selected == null) {
                resp.sendError(404);
                return;
            }
            int cp = Math.max(1, integer(req.getParameter("candidatePage")));
            int ct = dateDAO.countRegistrations(dateId);
            int cps = Math.max(1, (ct + CANDIDATE_PAGE_SIZE - 1) / CANDIDATE_PAGE_SIZE);
            cp = Math.min(cp, cps);
            req.setAttribute("selectedDate", selected);
            req.setAttribute("candidates", dossiers(dateDAO.findRegistrationIds(dateId, cp, CANDIDATE_PAGE_SIZE)));
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
        if ("delete".equals(req.getParameter("action"))) {
            int id = integer(req.getParameter("dateId"));
            boolean deleted = id > 0 && dateDAO.deleteIfUnused(id);
            req.getSession().setAttribute(deleted ? "tentativeSuccess" : "tentativeError",
                    deleted ? "Đã xóa ngày thi dự kiến chưa có đăng ký."
                            : "Không thể xóa vì ngày này đã có thí sinh đăng ký.");
            String tab = "expired".equals(req.getParameter("returnTab")) ? "expired" : "active";
            resp.sendRedirect(req.getContextPath() + "/manager/tentative-exam-dates?tab=" + tab);
            return;
        }
        try {
            Date date = Date.valueOf(req.getParameter("examDate"));
            int licenceId = integer(req.getParameter("licenceId"));
            if (licenceDAO.findById(licenceId) == null) {
                throw new IllegalArgumentException("Hạng GPLX không hợp lệ.");
            }
            int id = dateDAO.create(date, licenceId);
            req.getSession().setAttribute("tentativeSuccess", "Đã tạo ngày thi dự kiến. Giới hạn đăng ký: 50 người.");
            resp.sendRedirect(req.getContextPath() + "/manager/tentative-exam-dates?dateId=" + id);
        } catch (Exception e) {
            req.getSession().setAttribute("tentativeError", e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/manager/tentative-exam-dates");
        }
    }

    private void export(HttpServletRequest req, HttpServletResponse resp, int dateId, String type) throws IOException {
        TentativeExamDateDTO date = dateDAO.findById(dateId);
        if (date == null) {
            resp.sendError(404);
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
        if ("pdf".equals(type)) {
            int registrationId = integer(req.getParameter("registrationId"));
            DossierDTO dossier = dossierDAO.findByRegistrationId(registrationId);
            if (dossier == null || !dateDAO.findAllRegistrationIds(dateId).contains(registrationId)) {
                resp.sendError(404);
                return;
            }
            byte[] pdf = pdfService.generate(dossier, webRoot());
            resp.setContentType("application/pdf");
            download(resp, "ho-so-" + registrationId + ".pdf");
            resp.getOutputStream().write(pdf);
            return;
        }
        if ("zip".equals(type)) {
            resp.setContentType("application/zip");
            download(resp, base + ".zip");
            try (ZipOutputStream zip = new ZipOutputStream(resp.getOutputStream())) {
                for (DossierDTO d : rows) {
                    zip.putNextEntry(new ZipEntry(String.format("%03d_%s.pdf", d.getRegistrationId(), safe(d.getProfile().getGovIdNo()))));
                    zip.write(pdfService.generate(d, webRoot()));
                    zip.closeEntry();
                }
            }
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

    private Path webRoot() throws IOException {
        String value = getServletContext().getRealPath("/");
        if (value == null) {
            throw new IOException("Không xác định được thư mục web.");
        }
        return Path.of(value).toAbsolutePath().normalize();
    }

    private static void download(HttpServletResponse r, String name) {
        r.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + URLEncoder.encode(name, StandardCharsets.UTF_8).replace("+", "%20"));
    }

    private static String safe(String s) {
        return s == null ? "unknown" : s.replaceAll("[^0-9A-Za-z_-]", "_");
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
}
