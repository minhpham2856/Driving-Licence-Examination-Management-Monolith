package controller.admin;

import service.LicenceService;
import service.impl.LicenceServiceImpl;
import model.licence.Licence;
import model.user.User;
import service.AuditLogService;

import util.Sanitize;
import util.SessionUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "LicenceServlet", urlPatterns = {"/admin/licence-class"})
public class LicenceServlet extends HttpServlet {
    private final service.AuditLogService auditLogService = new service.impl.AuditLogServiceImpl();

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
            Licence licence = licenceService.findById(id);
            if (licence == null) {
                SessionUtil.flash(req, "danger", "KhÃ´ng tÃ¬m tháº¥y háº¡ng GPLX cáº§n sá»­a.");
                resp.sendRedirect(req.getContextPath() + "/admin/licence-class");
                return;
            }
            req.setAttribute("mode", "edit");
            req.setAttribute("licence", licence);
            req.setAttribute("licences", licenceService.findAll());
            req.getRequestDispatcher(FORM_VIEW).forward(req, resp);
        } else {
            String keyword = Sanitize.text(req.getParameter("searchKeyword"));
            req.setAttribute("licenceClasses", licenceService.search(keyword));
            req.setAttribute("totalClasses", licenceService.countAll());
            req.getRequestDispatcher(LIST_VIEW).forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User admin = SessionUtil.getCurrentUser(req);

        int id = Sanitize.toInt(req.getParameter("licenceId"), 0);
        String licenceClass = Sanitize.text(req.getParameter("licenceClass"));
        String description = Sanitize.text(req.getParameter("description"));
        int minimumAge = Sanitize.toInt(req.getParameter("minimumAge"), 0);
        int validForYears = Sanitize.toInt(req.getParameter("validForYears"), 0);
        Integer upgradeFrom = Sanitize.toIntegerOrNull(req.getParameter("upgradeFromLicenceId"));
        boolean isEdit = id > 0;

        Licence l = build(id, licenceClass, description, minimumAge, validForYears, upgradeFrom);
        
        LicenceService.SaveResult result = licenceService.save(l, admin.getUserId());

        if (!result.success) {
            req.setAttribute("mode", isEdit ? "edit" : "create");
            req.setAttribute("licence", l);
            req.setAttribute("licences", licenceService.findAll());
            req.setAttribute("error", result.message);
            req.getRequestDispatcher(FORM_VIEW).forward(req, resp);
            return;
        }

        auditLogService.persist(((model.user.User) req.getSession().getAttribute("user")).getUserId(), isEdit ? "UPDATE" : "INSERT", 
                (isEdit ? "Cáº­p Nháº­t Háº¡ng GPLX: " : "Táº¡o háº¡ng GPLX: ") + licenceClass, result.id);
        SessionUtil.flash(req, "success", result.message);
        
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


