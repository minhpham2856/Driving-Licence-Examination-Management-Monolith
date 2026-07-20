package admin.controller;

import admin.dao.ExamZoneDAO;
import admin.dao.impl.ExamZoneDAOImpl;
import admin.model.ZoneView;
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

/** Khu vực thi = ExamZone (khuôn viên). */
@WebServlet(name = "ExamZoneServlet", urlPatterns = {"/admin/exam-area"})
public class ExamZoneServlet extends HttpServlet {

    private final ExamZoneDAO dao = new ExamZoneDAOImpl();
    private static final String LIST_VIEW = "/views/admin/exam-area.jsp";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!SessionUtil.requireAdmin(req, resp)) return;
        String keyword = Sanitize.text(req.getParameter("searchKeyword"));
        String statusF = Sanitize.text(req.getParameter("filterStatus"));
        Boolean active = "active".equals(statusF) ? Boolean.TRUE : ("inactive".equals(statusF) ? Boolean.FALSE : null);
        req.setAttribute("zones", dao.search(keyword, active));
        req.setAttribute("totalZones", dao.countAll());
        req.getRequestDispatcher(LIST_VIEW).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!SessionUtil.requireAdmin(req, resp)) return;
        String action = Sanitize.text(req.getParameter("action"));
        String ctx = req.getContextPath();

        if ("delete".equals(action)) {
            int id = Sanitize.toInt(req.getParameter("id"), 0);
            ZoneView z = dao.findById(id);
            boolean ok = id > 0 && dao.delete(id);
            if (ok) { AdminAuditLog.persist(req.getSession(), "DELETE", "Xóa khu vực thi: " + (z != null ? z.getZoneName() : id), id);
                SessionUtil.flash(req, "success", "Đã xóa khu vực thi."); }
            else SessionUtil.flash(req, "danger", "Không thể xóa (khu vực đang chứa phòng/sân thi). Hãy khóa thay vì xóa.");
            resp.sendRedirect(ctx + "/admin/exam-area");
            return;
        }

        int id = Sanitize.toInt(req.getParameter("zoneId"), 0);
        String name = Sanitize.text(req.getParameter("zoneName"));
        String location = Sanitize.text(req.getParameter("location"));
        boolean active = !"inactive".equals(Sanitize.text(req.getParameter("status")));
        boolean isEdit = id > 0;

        String error = Validator.name("Tên khu vực", name, 3, 100);
        if (error == null) error = Validator.name("Địa điểm", location, 3, 255);

        if (error != null) {
            SessionUtil.flash(req, "danger", error);
            reopen(req, isEdit ? "edit" : "create", id, name, location, active);
            resp.sendRedirect(ctx + "/admin/exam-area");
            return;
        }

        ZoneView z = new ZoneView();
        z.setZoneId(id); z.setZoneName(name); z.setLocation(location); z.setActive(active);
        if (isEdit) {
            boolean ok = dao.update(z);
            AdminAuditLog.persist(req.getSession(), "UPDATE", "Cập nhật khu vực thi: " + name, id);
            SessionUtil.flash(req, ok ? "success" : "danger", ok ? "Đã cập nhật khu vực \"" + name + "\"." : "Cập nhật thất bại.");
        } else {
            int newId = dao.insert(z);
            AdminAuditLog.persist(req.getSession(), "INSERT", "Tạo khu vực thi: " + name, newId);
            SessionUtil.flash(req, newId > 0 ? "success" : "danger", newId > 0 ? "Đã thêm khu vực \"" + name + "\"." : "Thêm khu vực thất bại.");
        }
        resp.sendRedirect(ctx + "/admin/exam-area");
    }

    private void reopen(HttpServletRequest req, String mode, int id, String name, String location, boolean active) {
        var s = req.getSession();
        s.setAttribute("reopenModal", "zone");
        s.setAttribute("f_mode", mode);
        s.setAttribute("f_zoneId", id);
        s.setAttribute("f_zoneName", name);
        s.setAttribute("f_location", location);
        s.setAttribute("f_active", active);
    }
}
