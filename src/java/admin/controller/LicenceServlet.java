package admin.controller;

import admin.dao.LicenceManageDAO;
import admin.dao.UsageGuardDAO;
import admin.dao.impl.LicenceManageDAOImpl;
import admin.dao.impl.UsageGuardDAOImpl;
import admin.model.LicenceView;
import admin.util.AdminAuditLog;
import admin.util.Sanitize;
import admin.util.SessionUtil;
import admin.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Hạng GPLX = Licence. */
@WebServlet(name = "LicenceServlet", urlPatterns = {"/admin/licence-class"})
public class LicenceServlet extends HttpServlet {

    private final LicenceManageDAO dao = new LicenceManageDAOImpl();
    private final UsageGuardDAO guard = new UsageGuardDAOImpl();
    private static final String LIST_VIEW = "/views/admin/licence-class.jsp";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!SessionUtil.requireAdmin(req, resp)) return;
        String keyword = Sanitize.text(req.getParameter("searchKeyword"));
        req.setAttribute("licences", dao.search(keyword));
        req.setAttribute("totalLicences", dao.countAll());
        req.getRequestDispatcher(LIST_VIEW).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!SessionUtil.requireAdmin(req, resp)) return;
        String action = Sanitize.text(req.getParameter("action"));
        String ctx = req.getContextPath();

        if ("delete".equals(action)) {
            int id = Sanitize.toInt(req.getParameter("id"), 0);
            LicenceView l = dao.findById(id);
            String blocker = guard.licenceBlocker(id);
            if (blocker != null) {
                SessionUtil.flash(req, "danger", blocker);
                resp.sendRedirect(ctx + "/admin/licence-class");
                return;
            }
            boolean ok = id > 0 && dao.delete(id);
            if (ok) { AdminAuditLog.persist(req.getSession(), "DELETE", "Xóa hạng GPLX: " + (l != null ? l.getLicenceClass() : id), id);
                SessionUtil.flash(req, "success", "Đã xóa hạng GPLX."); }
            else SessionUtil.flash(req, "danger", "Xóa hạng GPLX thất bại.");
            resp.sendRedirect(ctx + "/admin/licence-class");
            return;
        }

        int id = Sanitize.toInt(req.getParameter("licenceId"), 0);
        String cls = Sanitize.text(req.getParameter("licenceClass")).toUpperCase();
        String desc = Sanitize.text(req.getParameter("description"));
        Integer minAge = Sanitize.toIntegerOrNull(req.getParameter("minimumAge"));
        Integer valid = Sanitize.toIntegerOrNull(req.getParameter("validForYears"));
        boolean isEdit = id > 0;

        String error = Validator.licenceClass(cls);
        if (error == null) error = Validator.intRange("Độ tuổi tối thiểu", minAge, 16, 100);
        if (error == null) error = Validator.intRange("Số năm hiệu lực", valid, 1, 50);
        if (error == null && dao.classExists(cls, id)) error = "Hạng GPLX \"" + cls + "\" đã tồn tại.";

        if (error != null) {
            SessionUtil.flash(req, "danger", error);
            var s = req.getSession();
            s.setAttribute("reopenModal", "licence");
            s.setAttribute("f_mode", isEdit ? "edit" : "create");
            s.setAttribute("f_licenceId", id);
            s.setAttribute("f_licenceClass", cls);
            s.setAttribute("f_description", desc);
            s.setAttribute("f_minimumAge", req.getParameter("minimumAge"));
            s.setAttribute("f_validForYears", req.getParameter("validForYears"));
            resp.sendRedirect(ctx + "/admin/licence-class");
            return;
        }

        LicenceView l = new LicenceView();
        l.setLicenceId(id); l.setLicenceClass(cls); l.setDescription(desc);
        l.setMinimumAge(minAge); l.setValidForYears(valid);
        if (isEdit) {
            boolean ok = dao.update(l);
            AdminAuditLog.persist(req.getSession(), "UPDATE", "Cập nhật hạng GPLX: " + cls, id);
            SessionUtil.flash(req, ok ? "success" : "danger", ok ? "Đã cập nhật hạng \"" + cls + "\"." : "Cập nhật thất bại.");
        } else {
            int newId = dao.insert(l);
            AdminAuditLog.persist(req.getSession(), "INSERT", "Tạo hạng GPLX: " + cls, newId);
            SessionUtil.flash(req, newId > 0 ? "success" : "danger", newId > 0 ? "Đã thêm hạng \"" + cls + "\"." : "Thêm thất bại.");
        }
        resp.sendRedirect(ctx + "/admin/licence-class");
    }
}
