package admin.controller;

import admin.dao.ExamAreaManageDAO;
import admin.dao.ExamZoneDAO;
import admin.dao.UsageGuardDAO;
import admin.dao.impl.ExamAreaManageDAOImpl;
import admin.dao.impl.ExamZoneDAOImpl;
import admin.dao.impl.UsageGuardDAOImpl;
import admin.model.AreaView;
import admin.model.ZoneView;
import admin.service.FacilityExcelService;
import admin.service.impl.FacilityExcelServiceImpl;
import admin.util.AdminAuditLog;
import admin.util.BulkResult;
import admin.util.ExcelDownload;
import admin.util.Sanitize;
import admin.util.SessionUtil;
import admin.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Phòng thi = ExamArea (phòng/sân thuộc 1 ExamZone). Modal chọn Khu vực trước rồi hiện field. */
@WebServlet(name = "ExamRoomServlet", urlPatterns = {"/admin/exam-room"})
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 10 * 1024 * 1024, maxRequestSize = 12 * 1024 * 1024)
public class ExamRoomServlet extends HttpServlet {

    private final ExamAreaManageDAO dao = new ExamAreaManageDAOImpl();
    private final ExamZoneDAO zoneDAO = new ExamZoneDAOImpl();
    private final UsageGuardDAO guard = new UsageGuardDAOImpl();
    private final FacilityExcelService excel = new FacilityExcelServiceImpl();
    private static final String LIST_VIEW = "/views/admin/exam-room.jsp";
    private static final List<String> TYPES = Arrays.asList("Phòng thủ tục", "Phòng thi", "Sân thi");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!SessionUtil.requireAdmin(req, resp)) return;

        String action = Sanitize.text(req.getParameter("action"));
        if ("template".equals(action)) {
            ExcelDownload.send(resp, "Bieu-mau-import-phong-thi.xlsx",
                    out -> excel.writeRoomTemplate(zoneDAO.listActive(), out));
            return;
        }

        String keyword = Sanitize.text(req.getParameter("searchKeyword"));
        String type = Sanitize.text(req.getParameter("filterType"));
        Integer zoneId = Sanitize.toIntegerOrNull(req.getParameter("filterZone"));
        List<AreaView> areas = dao.search(keyword, type, zoneId);

        if ("export".equals(action)) {
            ExcelDownload.send(resp, "Danh-sach-phong-thi.xlsx", out -> excel.writeRooms(areas, out));
            return;
        }

        req.setAttribute("areas", areas);
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

        if ("import".equals(action)) {
            handleImport(req, resp);
            return;
        }

        if ("bulkDelete".equals(action)) {
            handleBulkDelete(req, resp);
            return;
        }

        if ("delete".equals(action)) {
            int id = Sanitize.toInt(req.getParameter("id"), 0);
            AreaView a = dao.findById(id);
            String blocker = guard.areaBlocker(id);
            if (blocker != null) {
                SessionUtil.flash(req, "danger", blocker);
                resp.sendRedirect(ctx + "/admin/exam-room");
                return;
            }
            boolean ok = id > 0 && dao.delete(id);
            if (ok) { AdminAuditLog.persist(req.getSession(), "DELETE", "Xóa phòng/sân thi: " + (a != null ? a.getAreaName() : id), id);
                SessionUtil.flash(req, "success", "Đã xóa phòng/sân thi."); }
            else SessionUtil.flash(req, "danger", "Xóa phòng/sân thi thất bại.");
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

    /** Import nhiều phòng/sân thi cùng lúc từ file Excel theo biểu mẫu. */
    private void handleImport(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String ctx = req.getContextPath();
        Part part = null;
        try { part = req.getPart("file"); } catch (Exception ignore) {}

        if (part == null || part.getSize() == 0) {
            SessionUtil.flash(req, "danger", "Vui lòng chọn file Excel (.xlsx) cần import.");
            resp.sendRedirect(ctx + "/admin/exam-room"); return;
        }
        String fileName = part.getSubmittedFileName() == null ? "" : part.getSubmittedFileName().toLowerCase();
        if (!fileName.endsWith(".xlsx")) {
            SessionUtil.flash(req, "danger", "Chỉ hỗ trợ file .xlsx. Hãy tải biểu mẫu bằng nút \"Tải biểu mẫu Excel\".");
            resp.sendRedirect(ctx + "/admin/exam-room"); return;
        }

        List<FacilityExcelService.RoomRow> rows;
        try (InputStream in = part.getInputStream()) {
            rows = excel.readRoomImport(in);
        } catch (Exception e) {
            SessionUtil.flash(req, "danger", "Không đọc được file Excel. Hãy dùng đúng biểu mẫu tải từ hệ thống.");
            resp.sendRedirect(ctx + "/admin/exam-room"); return;
        }
        if (rows.isEmpty()) {
            SessionUtil.flash(req, "danger", "File không có dòng dữ liệu nào.");
            resp.sendRedirect(ctx + "/admin/exam-room"); return;
        }

        List<ZoneView> zones = zoneDAO.listActive();
        int created = 0;
        List<String> errors = new ArrayList<>();

        for (FacilityExcelService.RoomRow r : rows) {
            String name = Validator.normalize(r.areaName);
            String type = Validator.normalize(r.areaType);
            String location = Validator.normalize(r.location);
            String capStr = Validator.normalize(r.capacity);
            Integer capacity = capStr.isEmpty() ? null : Sanitize.toIntegerOrNull(capStr);
            int zoneId = resolveZoneId(zones, r.zoneName);

            String err = null;
            if (zoneId <= 0) err = "Khu vực thi \"" + Validator.normalize(r.zoneName) + "\" không tồn tại hoặc đang bị khóa.";
            if (err == null) err = Validator.name("Tên phòng/sân", name, 2, 100);
            if (err == null && !TYPES.contains(type)) err = "Loại phải là: Phòng thủ tục, Phòng thi hoặc Sân thi.";
            if (err == null) err = Validator.name("Địa điểm", location, 3, 255);
            if (err == null && !capStr.isEmpty() && capacity == null) err = "Sức chứa phải là số.";
            if (err == null && capacity != null) err = Validator.intRange("Sức chứa", capacity, 1, 100000);

            if (err != null) { errors.add("Dòng " + r.rowNumber + ": " + err); continue; }

            AreaView a = new AreaView();
            a.setZoneId(zoneId); a.setAreaName(name); a.setAreaType(type);
            a.setCapacity(capacity); a.setLocation(location);
            int newId = dao.insert(a);
            if (newId > 0) {
                created++;
                AdminAuditLog.persist(req.getSession(), "INSERT", "Import phòng/sân thi: " + name, newId);
            } else {
                errors.add("Dòng " + r.rowNumber + ": thêm thất bại.");
            }
        }

        BulkResult result = new BulkResult();
        for (int i = 0; i < created; i++) result.success();
        for (String e : errors) result.skip(e);
        SessionUtil.flash(req, result.flashType(), result.message("đã thêm", "phòng/sân thi", rows.size()));
        resp.sendRedirect(ctx + "/admin/exam-room");
    }

    /**
     * Xóa hàng loạt phòng/sân thi: theo các dòng được tick chọn (ids),
     * hoặc theo toàn bộ kết quả của bộ lọc hiện tại (scope=filtered).
     * Phòng đang được nơi khác sử dụng sẽ bị bỏ qua kèm lý do.
     */
    private void handleBulkDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String ctx = req.getContextPath();
        List<AreaView> targets = new ArrayList<>();

        if ("filtered".equals(Sanitize.text(req.getParameter("scope")))) {
            targets = dao.search(
                    Sanitize.text(req.getParameter("searchKeyword")),
                    Sanitize.text(req.getParameter("filterType")),
                    Sanitize.toIntegerOrNull(req.getParameter("filterZone")));
        } else {
            String[] ids = req.getParameterValues("ids");
            if (ids != null) {
                for (String raw : ids) {
                    int id = Sanitize.toInt(raw, 0);
                    AreaView a = (id > 0) ? dao.findById(id) : null;
                    if (a != null) targets.add(a);
                }
            }
        }

        if (targets.isEmpty()) {
            SessionUtil.flash(req, "danger", "Chưa chọn phòng/sân thi nào để xóa.");
            resp.sendRedirect(ctx + "/admin/exam-room");
            return;
        }

        BulkResult result = new BulkResult();
        for (AreaView a : targets) {
            String label = a.getAreaName() == null ? ("#" + a.getAreaId()) : a.getAreaName();
            String blocker = guard.areaBlocker(a.getAreaId());
            if (blocker != null) {
                result.skip(label + " (đang được sử dụng)");
                continue;
            }
            if (dao.delete(a.getAreaId())) {
                result.success();
                AdminAuditLog.persist(req.getSession(), "DELETE",
                        "Xóa hàng loạt phòng/sân thi: " + label, a.getAreaId());
            } else {
                result.skip(label + " (xóa thất bại)");
            }
        }

        SessionUtil.flash(req, result.flashType(), result.message("đã xóa", "phòng/sân thi", targets.size()));
        resp.sendRedirect(ctx + "/admin/exam-room");
    }

    private int resolveZoneId(List<ZoneView> zones, String zoneName) {
        String want = Validator.normalize(zoneName);
        if (want.isEmpty() || zones == null) return 0;
        for (ZoneView z : zones) {
            if (z.getZoneName() != null && z.getZoneName().trim().equalsIgnoreCase(want)) return z.getZoneId();
        }
        return 0;
    }

}
