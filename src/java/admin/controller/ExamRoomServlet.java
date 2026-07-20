package admin.controller;

import admin.dao.ExamAreaManageDAO;
import admin.dao.ExamZoneDAO;
import admin.dao.impl.ExamAreaManageDAOImpl;
import admin.dao.impl.ExamZoneDAOImpl;
import admin.model.AreaView;
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

/** Phòng thi = ExamArea (phòng/sân thuộc 1 ExamZone). Modal chọn Khu vực trước rồi hiện field. */
@WebServlet(name = "ExamRoomServlet", urlPatterns = {"/admin/exam-room"})
public class ExamRoomServlet extends HttpServlet {

    private final ExamAreaManageDAO dao = new ExamAreaManageDAOImpl();
    private final ExamZoneDAO zoneDAO = new ExamZoneDAOImpl();
    private static final String LIST_VIEW = "/views/admin/exam-room.jsp";
    private static final List<String> TYPES = Arrays.asList("Phòng thủ tục", "Phòng thi", "Sân thi");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!SessionUtil.requireAdmin(req, resp)) return;
        String keyword = Sanitize.text(req.getParameter("searchKeyword"));
        String type = Sanitize.text(req.getParameter("filterType"));
        Integer zoneId = Sanitize.toIntegerOrNull(req.getParameter("filterZone"));
        req.setAttribute("areas", dao.search(keyword, type, zoneId));
        req.setAttribute("zones", zoneDAO.listActive());
        req.setAttribute("allAreas", dao.search(null, null, null));
        req.setAttribute("totalAreas", dao.countAll());
        req.getRequestDispatcher(LIST_VIEW).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!SessionUtil.requireAdmin(req, resp)) return;
        String action = Sanitize.text(req.getParameter("action"));
        String ctx = req.getContextPath();

        if ("delete".equals(action)) {
            int id = Sanitize.toInt(req.getParameter("id"), 0);
            AreaView a = dao.findById(id);
            boolean ok = id > 0 && dao.delete(id);
            if (ok) { AdminAuditLog.persist(req.getSession(), "DELETE", "Xóa phòng/sân thi: " + (a != null ? a.getAreaName() : id), id);
                SessionUtil.flash(req, "success", "Đã xóa phòng/sân thi."); }
            else SessionUtil.flash(req, "danger", "Không thể xóa (đang chứa máy thi hoặc gắn với kỳ thi).");
            resp.sendRedirect(ctx + "/admin/exam-room");
            return;
        }

        int id = Sanitize.toInt(req.getParameter("areaId"), 0);
        int zoneId = Sanitize.toInt(req.getParameter("zoneId"), 0);
        String name = Sanitize.text(req.getParameter("areaName"));
        String type = Sanitize.text(req.getParameter("areaType"));
        String location = Sanitize.text(req.getParameter("location"));
        String capStr = Sanitize.text(req.getParameter("capacity"));
        Integer capacity = capStr.isEmpty() ? null : Sanitize.toIntegerOrNull(capStr);
        boolean isEdit = id > 0;

        String error = null;
        if (zoneId <= 0) error = "Vui lòng chọn khu vực thi.";
        if (error == null) error = Validator.name("Tên phòng/sân", name, 2, 100);
        if (error == null && !TYPES.contains(type)) error = "Vui lòng chọn loại (Phòng thủ tục, Phòng thi hoặc Sân thi).";
        if (error == null) error = Validator.name("Địa điểm", location, 3, 255);
        if (error == null && capacity != null) error = Validator.intRange("Sức chứa", capacity, 1, 100000);

        if (error != null) {
            SessionUtil.flash(req, "danger", error);
            var s = req.getSession();
            s.setAttribute("reopenModal", "room");
            s.setAttribute("f_mode", isEdit ? "edit" : "create");
            s.setAttribute("f_areaId", id);
            s.setAttribute("f_zoneId", zoneId);
            s.setAttribute("f_areaName", name);
            s.setAttribute("f_areaType", type);
            s.setAttribute("f_capacity", capStr);
            s.setAttribute("f_location", location);
            resp.sendRedirect(ctx + "/admin/exam-room");
            return;
        }

        AreaView a = new AreaView();
        a.setAreaId(id); a.setZoneId(zoneId); a.setAreaName(name);
        a.setAreaType(type); a.setCapacity(capacity); a.setLocation(location);
        if (isEdit) {
            boolean ok = dao.update(a);
            AdminAuditLog.persist(req.getSession(), "UPDATE", "Cập nhật phòng/sân thi: " + name, id);
            SessionUtil.flash(req, ok ? "success" : "danger", ok ? "Đã cập nhật \"" + name + "\"." : "Cập nhật thất bại.");
        } else {
            int newId = dao.insert(a);
            AdminAuditLog.persist(req.getSession(), "INSERT", "Tạo phòng/sân thi: " + name, newId);
            SessionUtil.flash(req, newId > 0 ? "success" : "danger", newId > 0 ? "Đã thêm \"" + name + "\"." : "Thêm thất bại.");
        }
        resp.sendRedirect(ctx + "/admin/exam-room");
    }
}
