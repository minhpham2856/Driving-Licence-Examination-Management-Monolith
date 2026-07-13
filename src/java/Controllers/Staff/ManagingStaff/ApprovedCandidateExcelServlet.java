package Controllers.Staff.ManagingStaff;

import DAOs.DossierDAO;
import DAOs.Impl.DossierDAOImpl;
import DTOs.DossierDTO;
import Models.User;
import Services.ApprovedCandidateExcelService;
import Services.Impl.ApprovedCandidateExcelServiceImpl;
import Utils.AuditLogHelper;
import Utils.SessionUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@WebServlet("/manager/registrants/export-approved")
public class ApprovedCandidateExcelServlet extends HttpServlet {

    private static final Set<String> LICENCE_CLASSES = Set.of("A1", "A", "B1");
    private final DossierDAO dossierDAO = new DossierDAOImpl();
    private final ApprovedCandidateExcelService excelService = new ApprovedCandidateExcelServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!hasAccess(request, response)) return;

        String licence = request.getParameter("licence") == null ? ""
                : request.getParameter("licence").trim().toUpperCase(Locale.ROOT);
        if (!LICENCE_CLASSES.contains(licence)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Hạng GPLX chỉ hỗ trợ A1, A hoặc B1.");
            return;
        }

        List<DossierDTO> approved = dossierDAO.findRegistrantsByFilters("approved", licence);
        String filename = "danh-sach-ho-so-da-duyet-" + licence + "-"
                + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".xlsx";
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + filename + "\"; filename*=UTF-8''" + encoded);
        response.setHeader("Cache-Control", "no-store");

        excelService.writeApprovedCandidates(licence, approved, response.getOutputStream());
        AuditLogHelper.persist(request.getSession(), "EXPORT APPROVED DOSSIER",
                "Xuất " + approved.size() + " hồ sơ đã duyệt hạng " + licence);
    }

    private boolean hasAccess(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = SessionUtil.getCurrentUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        String role = user.getRole() == null ? "" : user.getRole().getRoleName();
        if (!Set.of("ManagingStaff", "Admin").contains(role)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        return true;
    }
}
