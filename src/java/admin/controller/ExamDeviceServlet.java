package admin.controller;

import admin.dao.ExamAreaManageDAO;
import admin.dao.ExamDeviceManageDAO;
import admin.dao.ExamZoneDAO;
import admin.dao.impl.ExamAreaManageDAOImpl;
import admin.dao.impl.ExamDeviceManageDAOImpl;
import admin.dao.impl.ExamZoneDAOImpl;
import admin.model.DeviceView;
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
import java.util.Arrays;
import java.util.List;

/** Máy thi = ExamDevice (thuộc 1 ExamArea). Modal chọn Khu vực -> Phòng rồi hiện field. */
@WebServlet(name = "ExamDeviceServlet", urlPatterns = {"/admin/exam-computer"})
public class ExamDeviceServlet extends HttpServlet {

    private final ExamDeviceManageDAO dao = new ExamDeviceManageDAOImpl();
    private final ExamAreaManageDAO areaDAO = new ExamAreaManageDAOImpl();
    private final ExamZoneDAO zoneDAO = new ExamZoneDAOImpl();
    private static final String LIST_VIEW = "/views/admin/exam-computer.jsp";
    private static final List<String> TYPES = Arrays.asList("Máy tính", "Mô tô", "Mô tô ba bánh");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!SessionUtil.requireAdmin(req, resp)) return;
        String keyword = Sanitize.text(req.getParameter("searchKeyword"));
        String type = Sanitize.text(req.getParameter("filterType"));
        Integer zoneId = Sanitize.toIntegerOrNull(req.getParameter("filterZone"));
        Integer areaId = Sanitize.toIntegerOrNull(req.getParameter("filterArea"));
        req.setAttribute("devices", dao.search(keyword, type, zoneId, areaId));
        req.setAttribute("zones", zoneDAO.listActive());
        req.setAttribute("areas", areaDAO.search(null, null, null)); // toàn bộ phòng/sân, JS lọc theo zone
        req.setAttribute("totalDevices", dao.countAll());
        req.getRequestDispatcher(LIST_VIEW).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!SessionUtil.requireAdmin(req, resp)) return;
        String action = Sanitize.text(req.getParameter("action"));
        String ctx = req.getContextPath();

        if ("delete".equals(action)) {
            int id = Sanitize.toInt(req.getParameter("id"), 0);
            DeviceView d = dao.findById(id);
            boolean ok = id > 0 && dao.delete(id);
            if (ok) { AdminAuditLog.persist(req.getSession(), "DELETE", "Xóa máy thi: " + (d != null ? d.getDeviceName() : id), id);
                SessionUtil.flash(req, "success", "Đã xóa máy thi."); }
            else SessionUtil.flash(req, "danger", "Không thể xóa (máy đang gắn với kỳ thi).");
            resp.sendRedirect(ctx + "/admin/exam-computer");
            return;
        }

        if ("toggle".equals(action)) {
            int id = Sanitize.toInt(req.getParameter("id"), 0);
            boolean toActive = "true".equals(req.getParameter("active"));
            boolean ok = id > 0 && dao.setActive(id, toActive);
            if (ok) AdminAuditLog.persist(req.getSession(), "UPDATE", (toActive ? "Kích hoạt" : "Bảo trì") + " máy thi #" + id, id);
            SessionUtil.flash(req, ok ? "success" : "danger", ok ? "Đã đổi trạng thái máy thi." : "Thao tác thất bại.");
            resp.sendRedirect(ctx + "/admin/exam-computer");
            return;
        }

        int id = Sanitize.toInt(req.getParameter("deviceId"), 0);
        int areaId = Sanitize.toInt(req.getParameter("areaId"), 0);
        int zoneId = Sanitize.toInt(req.getParameter("zoneId"), 0);
        String name = Sanitize.text(req.getParameter("deviceName"));
        String type = Sanitize.text(req.getParameter("deviceType"));
        boolean active = !"inactive".equals(Sanitize.text(req.getParameter("status")));
        boolean isEdit = id > 0;

        String error = null;
        if (zoneId <= 0) error = "Vui lòng chọn khu vực thi.";
        if (error == null && areaId <= 0) error = "Vui lòng chọn phòng/sân thi.";
        if (error == null) error = Validator.name("Tên máy/thiết bị", name, 2, 100);
        if (error == null && !TYPES.contains(type)) error = "Vui lòng chọn loại thiết bị (Máy tính, Mô tô hoặc Mô tô ba bánh).";

        if (error != null) {
            SessionUtil.flash(req, "danger", error);
            var s = req.getSession();
            s.setAttribute("reopenModal", "device");
            s.setAttribute("f_mode", isEdit ? "edit" : "create");
            s.setAttribute("f_deviceId", id);
            s.setAttribute("f_zoneId", zoneId);
            s.setAttribute("f_areaId", areaId);
            s.setAttribute("f_deviceName", name);
            s.setAttribute("f_deviceType", type);
            s.setAttribute("f_active", active);
            resp.sendRedirect(ctx + "/admin/exam-computer");
            return;
        }

        DeviceView d = new DeviceView();
        d.setDeviceId(id); d.setAreaId(areaId); d.setDeviceName(name);
        d.setDeviceType(type); d.setActive(active);
        if (isEdit) {
            boolean ok = dao.update(d);
            AdminAuditLog.persist(req.getSession(), "UPDATE", "Cập nhật máy thi: " + name, id);
            SessionUtil.flash(req, ok ? "success" : "danger", ok ? "Đã cập nhật \"" + name + "\"." : "Cập nhật thất bại.");
        } else {
            int newId = dao.insert(d);
            AdminAuditLog.persist(req.getSession(), "INSERT", "Tạo máy thi: " + name, newId);
            SessionUtil.flash(req, newId > 0 ? "success" : "danger", newId > 0 ? "Đã thêm \"" + name + "\"." : "Thêm thất bại.");
        }
        resp.sendRedirect(ctx + "/admin/exam-computer");
    }
}
