package admin.controller;

import admin.dao.ExamAreaManageDAO;
import admin.dao.ExamDeviceManageDAO;
import admin.dao.ExamZoneDAO;
import admin.dao.UsageGuardDAO;
import admin.dao.impl.ExamAreaManageDAOImpl;
import admin.dao.impl.ExamDeviceManageDAOImpl;
import admin.dao.impl.ExamZoneDAOImpl;
import admin.dao.impl.UsageGuardDAOImpl;
import admin.model.AreaView;
import admin.model.DeviceView;
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

/** Máy thi = ExamDevice (thuộc 1 ExamArea). Modal chọn Khu vực -> Phòng rồi hiện field. */
@WebServlet(name = "ExamDeviceServlet", urlPatterns = {"/admin/exam-computer"})
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 10 * 1024 * 1024, maxRequestSize = 12 * 1024 * 1024)
public class ExamDeviceServlet extends HttpServlet {

    private final ExamDeviceManageDAO dao = new ExamDeviceManageDAOImpl();
    private final ExamAreaManageDAO areaDAO = new ExamAreaManageDAOImpl();
    private final ExamZoneDAO zoneDAO = new ExamZoneDAOImpl();
    private final UsageGuardDAO guard = new UsageGuardDAOImpl();
    private final FacilityExcelService excel = new FacilityExcelServiceImpl();
    private static final String LIST_VIEW = "/views/admin/exam-computer.jsp";
    private static final List<String> TYPES = Arrays.asList("Máy tính", "Mô tô", "Mô tô ba bánh");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!SessionUtil.requireAdmin(req, resp)) return;

        String action = Sanitize.text(req.getParameter("action"));
        if ("template".equals(action)) {
            ExcelDownload.send(resp, "Bieu-mau-import-may-thi.xlsx",
                    out -> excel.writeDeviceTemplate(areaDAO.search(null, null, null), out));
            return;
        }

        String keyword = Sanitize.text(req.getParameter("searchKeyword"));
        String type = Sanitize.text(req.getParameter("filterType"));
        Integer zoneId = Sanitize.toIntegerOrNull(req.getParameter("filterZone"));
        Integer areaId = Sanitize.toIntegerOrNull(req.getParameter("filterArea"));
        List<DeviceView> devices = dao.search(keyword, type, zoneId, areaId);

        if ("export".equals(action)) {
            ExcelDownload.send(resp, "Danh-sach-may-thi.xlsx", out -> excel.writeDevices(devices, out));
            return;
        }

        req.setAttribute("devices", devices);
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
            DeviceView d = dao.findById(id);
            String blocker = guard.deviceBlocker(id);
            if (blocker != null) {
                SessionUtil.flash(req, "danger", blocker);
                resp.sendRedirect(ctx + "/admin/exam-computer");
                return;
            }
            boolean ok = id > 0 && dao.delete(id);
            if (ok) { AdminAuditLog.persist(req.getSession(), "DELETE", "Xóa máy thi: " + (d != null ? d.getDeviceName() : id), id);
                SessionUtil.flash(req, "success", "Đã xóa máy thi."); }
            else SessionUtil.flash(req, "danger", "Xóa máy thi thất bại.");
            resp.sendRedirect(ctx + "/admin/exam-computer");
            return;
        }

        if ("toggle".equals(action)) {
            int id = Sanitize.toInt(req.getParameter("id"), 0);
            boolean toActive = "true".equals(req.getParameter("active"));
            // Không cho chuyển sang Bảo trì/Khóa khi máy đang được sử dụng
            if (!toActive) {
                String blocker = guard.deviceBlocker(id);
                if (blocker != null) {
                    SessionUtil.flash(req, "danger", blocker);
                    resp.sendRedirect(ctx + "/admin/exam-computer");
                    return;
                }
            }
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
        // Không cho khóa (vô hiệu hóa) máy thi đang được sử dụng
        if (error == null && isEdit && !active) {
            DeviceView current = dao.findById(id);
            if (current != null && current.isActive()) error = guard.deviceBlocker(id);
        }

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

    /** Import nhiều máy/thiết bị thi cùng lúc từ file Excel theo biểu mẫu. */
    private void handleImport(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String ctx = req.getContextPath();
        Part part = null;
        try { part = req.getPart("file"); } catch (Exception ignore) {}

        if (part == null || part.getSize() == 0) {
            SessionUtil.flash(req, "danger", "Vui lòng chọn file Excel (.xlsx) cần import.");
            resp.sendRedirect(ctx + "/admin/exam-computer"); return;
        }
        String fileName = part.getSubmittedFileName() == null ? "" : part.getSubmittedFileName().toLowerCase();
        if (!fileName.endsWith(".xlsx")) {
            SessionUtil.flash(req, "danger", "Chỉ hỗ trợ file .xlsx. Hãy tải biểu mẫu bằng nút \"Tải biểu mẫu Excel\".");
            resp.sendRedirect(ctx + "/admin/exam-computer"); return;
        }

        List<FacilityExcelService.DeviceRow> rows;
        try (InputStream in = part.getInputStream()) {
            rows = excel.readDeviceImport(in);
        } catch (Exception e) {
            SessionUtil.flash(req, "danger", "Không đọc được file Excel. Hãy dùng đúng biểu mẫu tải từ hệ thống.");
            resp.sendRedirect(ctx + "/admin/exam-computer"); return;
        }
        if (rows.isEmpty()) {
            SessionUtil.flash(req, "danger", "File không có dòng dữ liệu nào.");
            resp.sendRedirect(ctx + "/admin/exam-computer"); return;
        }

        List<AreaView> allAreas = areaDAO.search(null, null, null);
        int created = 0;
        List<String> errors = new ArrayList<>();

        for (FacilityExcelService.DeviceRow r : rows) {
            String name = Validator.normalize(r.deviceName);
            String type = Validator.normalize(r.deviceType);
            String zoneName = Validator.normalize(r.zoneName);
            String areaName = Validator.normalize(r.areaName);
            AreaView area = resolveArea(allAreas, zoneName, areaName);

            String err = null;
            if (area == null) {
                err = "Không tìm thấy phòng/sân \"" + areaName + "\" thuộc khu vực \"" + zoneName + "\".";
            }
            if (err == null) err = Validator.name("Tên máy/thiết bị", name, 2, 100);
            if (err == null && !TYPES.contains(type)) err = "Loại thiết bị phải là: Máy tính, Mô tô hoặc Mô tô ba bánh.";

            if (err != null) { errors.add("Dòng " + r.rowNumber + ": " + err); continue; }

            DeviceView d = new DeviceView();
            d.setAreaId(area.getAreaId());
            d.setDeviceName(name);
            d.setDeviceType(type);
            d.setActive(!isMaintenance(r.status));
            int newId = dao.insert(d);
            if (newId > 0) {
                created++;
                AdminAuditLog.persist(req.getSession(), "INSERT", "Import máy thi: " + name, newId);
            } else {
                errors.add("Dòng " + r.rowNumber + ": thêm thất bại.");
            }
        }

        BulkResult result = new BulkResult();
        for (int i = 0; i < created; i++) result.success();
        for (String e : errors) result.skip(e);
        SessionUtil.flash(req, result.flashType(), result.message("đã thêm", "máy/thiết bị thi", rows.size()));
        resp.sendRedirect(ctx + "/admin/exam-computer");
    }

    /**
     * Xóa hàng loạt máy/thiết bị thi: theo các dòng được tick chọn (ids),
     * hoặc theo toàn bộ kết quả của bộ lọc hiện tại (scope=filtered).
     * Máy đang gắn với kỳ thi/thí sinh sẽ bị bỏ qua kèm lý do.
     */
    private void handleBulkDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String ctx = req.getContextPath();
        List<DeviceView> targets = new ArrayList<>();

        if ("filtered".equals(Sanitize.text(req.getParameter("scope")))) {
            targets = dao.search(
                    Sanitize.text(req.getParameter("searchKeyword")),
                    Sanitize.text(req.getParameter("filterType")),
                    Sanitize.toIntegerOrNull(req.getParameter("filterZone")),
                    Sanitize.toIntegerOrNull(req.getParameter("filterArea")));
        } else {
            String[] ids = req.getParameterValues("ids");
            if (ids != null) {
                for (String raw : ids) {
                    int id = Sanitize.toInt(raw, 0);
                    DeviceView d = (id > 0) ? dao.findById(id) : null;
                    if (d != null) targets.add(d);
                }
            }
        }

        if (targets.isEmpty()) {
            SessionUtil.flash(req, "danger", "Chưa chọn máy/thiết bị thi nào để xóa.");
            resp.sendRedirect(ctx + "/admin/exam-computer");
            return;
        }

        BulkResult result = new BulkResult();
        for (DeviceView d : targets) {
            String label = d.getDeviceName() == null ? ("#" + d.getDeviceId()) : d.getDeviceName();
            String blocker = guard.deviceBlocker(d.getDeviceId());
            if (blocker != null) {
                result.skip(label + " (đang được sử dụng)");
                continue;
            }
            if (dao.delete(d.getDeviceId())) {
                result.success();
                AdminAuditLog.persist(req.getSession(), "DELETE",
                        "Xóa hàng loạt máy thi: " + label, d.getDeviceId());
            } else {
                result.skip(label + " (xóa thất bại)");
            }
        }

        SessionUtil.flash(req, result.flashType(), result.message("đã xóa", "máy/thiết bị thi", targets.size()));
        resp.sendRedirect(ctx + "/admin/exam-computer");
    }

    /** Khớp phòng/sân theo cặp (khu vực, tên phòng); nếu tên phòng là duy nhất thì chấp nhận cả khi thiếu khu vực. */
    private AreaView resolveArea(List<AreaView> areas, String zoneName, String areaName) {
        if (areaName == null || areaName.isEmpty() || areas == null) return null;
        AreaView onlyByName = null;
        int nameMatches = 0;
        for (AreaView a : areas) {
            if (a.getAreaName() == null || !a.getAreaName().trim().equalsIgnoreCase(areaName)) continue;
            nameMatches++;
            onlyByName = a;
            if (!zoneName.isEmpty() && a.getZoneName() != null
                    && a.getZoneName().trim().equalsIgnoreCase(zoneName)) {
                return a;
            }
        }
        // Không có khu vực nào khớp: chỉ chấp nhận khi tên phòng không trùng lặp và người dùng bỏ trống khu vực
        return (zoneName.isEmpty() && nameMatches == 1) ? onlyByName : null;
    }

    private boolean isMaintenance(String status) {
        String s = Validator.normalize(status).toLowerCase();
        return s.startsWith("bảo trì") || s.startsWith("bao tri") || s.startsWith("hỏng")
                || s.startsWith("hong") || s.equals("inactive");
    }
}
