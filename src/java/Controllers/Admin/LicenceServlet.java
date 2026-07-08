package Controllers.Admin;

import DAO.Impl.LicenceDAOImpl;
import DAO.LicenceDAO;
import Models.Licence;
import Models.User;
import Utils.AuditLogHelper;
import Utils.Sanitize;
import Utils.SessionUtil;
import Utils.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "LicenceServlet", urlPatterns = {"/admin/licence-class"})
public class LicenceServlet extends HttpServlet {

    private final LicenceDAO dao = new LicenceDAOImpl();
    private static final String LIST_VIEW = "/views/admin/licence-class.jsp";
    private static final String FORM_VIEW = "/views/admin/licence-class-form.jsp";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!SessionUtil.requireAdmin(req, resp)) {
            return;
        }
        String action = Sanitize.text(req.getParameter("action"));

        if ("new".equals(action)) {
            req.setAttribute("mode", "create");
            req.setAttribute("licences", dao.findAll());
            req.getRequestDispatcher(FORM_VIEW).forward(req, resp);
        } else if ("edit".equals(action)) {
            int id = Sanitize.toInt(req.getParameter("id"), 0);
            Licence licence = dao.findById(id);
            if (licence == null) {
                SessionUtil.flash(req, "danger", "Không tìm thấy hạng GPLX cần sửa.");
                resp.sendRedirect(req.getContextPath() + "/admin/licence-class");
                return;
            }
            req.setAttribute("mode", "edit");
            req.setAttribute("licence", licence);
            req.setAttribute("licences", dao.findAll());
            req.getRequestDispatcher(FORM_VIEW).forward(req, resp);
        } else {
            String keyword = Sanitize.text(req.getParameter("searchKeyword"));
            req.setAttribute("licenceClasses", dao.search(keyword));
            req.setAttribute("totalClasses", dao.countAll());
            req.getRequestDispatcher(LIST_VIEW).forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!SessionUtil.requireAdmin(req, resp)) {
            return;
        }
        User admin = SessionUtil.getCurrentUser(req);

        int id = Sanitize.toInt(req.getParameter("licenceId"), 0);
        String licenceClass = Sanitize.text(req.getParameter("licenceClass"));
        String description = Sanitize.text(req.getParameter("description"));
        int minimumAge = Sanitize.toInt(req.getParameter("minimumAge"), 0);
        int validForYears = Sanitize.toInt(req.getParameter("validForYears"), 0);
        Integer upgradeFrom = Sanitize.toIntegerOrNull(req.getParameter("upgradeFromLicenceId"));
        boolean isEdit = id > 0;

        String error = Validator.licenceClass(licenceClass);
        if (licenceClass.isEmpty()) {
            error = "Vui lòng nhập mã hạng (VD: A1, B2, C...).";
        } else if (minimumAge <= 0) {
            error = "Độ tuổi tối thiểu phải lớn hơn 0.";
        } else if (validForYears <= 0) {
            error = "Thời hạn (năm) phải lớn hơn 0.";
        } else if (dao.existsByClass(licenceClass, id)) {
            error = "Mã Hạng \"" + licenceClass + "\" đã tồn tại.";
        } else if (upgradeFrom != null && upgradeFrom == id && isEdit) {
            error = "Hạng không thể nâng cấp từ chính nó.";
        }

        if (error != null) {
            Licence l = build(id, licenceClass, description, minimumAge, validForYears, upgradeFrom);
            req.setAttribute("mode", isEdit ? "edit" : "create");
            req.setAttribute("licence", l);
            req.setAttribute("licences", dao.findAll());
            req.setAttribute("error", error);
            req.getRequestDispatcher(FORM_VIEW).forward(req, resp);
            return;
        }

        Licence l = build(id, licenceClass, description, minimumAge, validForYears, upgradeFrom);
        if (isEdit) {
            l.setUpdatedByUserId(admin.getId());
            boolean ok = dao.update(l);
            AuditLogHelper.persist(req.getSession(), "UPDATE", "Cập Nhật Hạng GPLX: " + licenceClass, id);
            SessionUtil.flash(req, ok ? "success" : "danger",
                    ok ? "Đã cập nhật hạng \"" + licenceClass + "\"." : "Cập nhật hạng GPLX thất bại.");
        } else {
            l.setCreatedByUserId(admin.getId());
            l.setUpdatedByUserId(admin.getId());
            int newId = dao.insert(l);
            boolean ok = newId > 0;
            AuditLogHelper.persist(req.getSession(), "INSERT", "Tạo hạng GPLX: " + licenceClass, newId);
            SessionUtil.flash(req, ok ? "success" : "danger",
                    ok ? "Đã thêm hạng \"" + licenceClass + "\"." : "Thêm hạng thất bại.");
        }
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
