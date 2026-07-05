package controller.admin;
import dto.*;
import model.*;
import model.*;
import service.*;
import service.impl.*;
import service.LicenceService;
import service.impl.LicenceServiceImpl;
import model.Licence;
import model.User;
import enums.AuditAction;
import enums.AuditEntity;
import service.AuditLogService;
import util.Sanitize;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
@WebServlet(name = "LicenceServlet", urlPatterns = {"/admin/licence-class"})
public class LicenceServlet extends HttpServlet {
    private final AuditLogService auditLogService = new AuditLogServiceImpl();
    private LicenceService licenceService;
    private static final String LIST_VIEW = "/views/admin/licence-class.jsp";
    private static final String FORM_VIEW = "/views/admin/licence-class-form.jsp";
    @Override
    public void init() {
        licenceService = new LicenceServiceImpl();
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = Sanitize.text(req.getParameter("action"));
        if ("new".equals(action)) {
            req.setAttribute("mode", "create");
            req.setAttribute("licences", licenceService.findAll());
            req.getRequestDispatcher(FORM_VIEW).forward(req, resp);
        } else if ("edit".equals(action)) {
            int id = Sanitize.toInt(req.getParameter("id"), 0);
            Licence licence = licenceService.getById(id);
            if (licence == null) {
                HttpSession flashSession = req.getSession(true);
                flashSession.setAttribute("flashType", "danger");
                flashSession.setAttribute("flashMessage", "Không tìm thấy hạng GPLX cần sửa.");
                resp.sendRedirect(req.getContextPath() + "/admin/licence-class");
                return;
            }
            req.setAttribute("mode", "edit");
            req.setAttribute("licence", licence);
            req.setAttribute("licences", licenceService.findAll());
            req.getRequestDispatcher(FORM_VIEW).forward(req, resp);
        } else {
            String keyword = Sanitize.text(req.getParameter("searchKeyword"));
            java.util.List<Licence> licenceClasses = licenceService.search(keyword);
            req.setAttribute("licenceClasses", licenceClasses);
            req.setAttribute("licenceClassById", buildLicenceClassById(licenceClasses));
            req.setAttribute("totalClasses", licenceService.countAll());
            req.getRequestDispatcher(LIST_VIEW).forward(req, resp);
        }
    }
    private java.util.Map<Integer, String> buildLicenceClassById(java.util.List<Licence> licences) {
        java.util.Map<Integer, String> byId = new java.util.HashMap<>();
        if (licences != null) {
            for (Licence licence : licences) {
                byId.put(licence.getLicenceId(), licence.getLicenceClass());
            }
        }
        return byId;
    }
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User admin = (User) req.getSession().getAttribute("user");
        int id = Sanitize.toInt(req.getParameter("licenceId"), 0);
        String licenceClass = Sanitize.text(req.getParameter("licenceClass"));
        String description = Sanitize.text(req.getParameter("description"));
        int minimumAge = Sanitize.toInt(req.getParameter("minimumAge"), 0);
        int validForYears = Sanitize.toInt(req.getParameter("validForYears"), 0);
        Integer upgradeFrom = Sanitize.toIntegerOrNull(req.getParameter("upgradeFromLicenceId"));
        boolean isEdit = id > 0;
        Licence l = build(id, licenceClass, description, minimumAge, validForYears, upgradeFrom);
        ServiceResult<SaveEntityData> result = licenceService.save(l, admin.getUserId());
        if (!result.isSuccess()) {
            req.setAttribute("mode", isEdit ? "edit" : "create");
            req.setAttribute("licence", l);
            req.setAttribute("licences", licenceService.findAll());
            req.setAttribute("error", result.getMessage());
            req.getRequestDispatcher(FORM_VIEW).forward(req, resp);
            return;
        }
        int savedId = result.getData() != null ? result.getData().getEntityId() : l.getLicenceId();
        auditLogService.logAction(((User) req.getSession().getAttribute("user")).getUserId(),
                isEdit ? AuditAction.UPDATE : AuditAction.CREATE, AuditEntity.DOSSIER,
                (isEdit ? "Cập nhật hạng GPLX: " : "Tạo hạng GPLX: ") + licenceClass, savedId);
        HttpSession flashSession = req.getSession(true);
        flashSession.setAttribute("flashType", "success");
        flashSession.setAttribute("flashMessage", result.getMessage());
        resp.sendRedirect(req.getContextPath() + "/admin/licence-class");
    }
    private Licence build(int id, String cls, String desc, int age, int years, Integer up) {
        Licence l = new Licence();
        l.setLicenceId(id);
        l.setLicenceClass(cls);
        l.setDescription(desc);
        l.setMinimumAge(age);
        l.setValidForYears(years);
        l.setUpgradeFromLicenceId(up);
        return l;
    }
}
