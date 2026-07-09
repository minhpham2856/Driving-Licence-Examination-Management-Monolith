package Controllers.Admin;

import DAO.ExamAreaDAO;
import DAO.ExamDeviceManageDAO;
import DAO.ExamRoomDAO;
import DAO.Impl.ExamAreaDAOImpl;
import DAO.Impl.ExamDeviceManageDAOImpl;
import DAO.Impl.ExamRoomDAOImpl;
import Models.ExamDeviceView;
import Models.ExamRoom;
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
import java.io.PrintWriter;
import java.util.List;

@WebServlet(name = "ExamDeviceServlet", urlPatterns = {"/admin/exam-computer"})
public class ExamDeviceServlet extends HttpServlet {

    private final ExamDeviceManageDAO dao = new ExamDeviceManageDAOImpl();
    private final ExamRoomDAO roomDAO = new ExamRoomDAOImpl();
    private final ExamAreaDAO areaDAO = new ExamAreaDAOImpl();
    private static final String LIST_VIEW = "/views/admin/exam-computer.jsp";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!SessionUtil.requireAdmin(req, resp)) return;
        req.setCharacterEncoding("UTF-8");

        String action = Sanitize.text(req.getParameter("action"));
        if ("roomsByArea".equals(action)) {
            writeRoomsByArea(req, resp);
            return;
        }

        String keyword = Sanitize.text(req.getParameter("searchKeyword"));
        Integer roomId = Sanitize.toIntegerOrNull(req.getParameter("filterRoom"));
        String status = Sanitize.text(req.getParameter("filterStatus"));

        req.setAttribute("examDevices", dao.search(keyword, roomId, status));
        req.setAttribute("rooms", roomDAO.search(null, null, null, null)); // filter dropdown
        req.setAttribute("examAreas", areaDAO.search(null, null));         // modal cascade
        req.setAttribute("totalDevices", dao.countAll());
        req.setAttribute("activeDevices", dao.countByStatus("Operational") + dao.countByStatus("Available"));
        req.setAttribute("maintenanceDevices", dao.countByStatus("Maintenance"));
        req.setAttribute("brokenDevices", dao.countByStatus("Broken"));
        req.getRequestDispatcher(LIST_VIEW).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!SessionUtil.requireAdmin(req, resp)) return;
        req.setCharacterEncoding("UTF-8");

        String action = Sanitize.text(req.getParameter("action"));
        boolean ajax = "1".equals(req.getParameter("ajax"));
        User admin = SessionUtil.getCurrentUser(req);
        Integer adminId = (admin != null) ? admin.getId() : null;

        if ("delete".equals(action)) {
            int id = Sanitize.toInt(req.getParameter("id"), 0);
            ExamDeviceView dev = dao.findById(id);
            boolean ok = id > 0 && dao.delete(id);
            if (ok) {
                AuditLogHelper.persist(req.getSession(), "DELETE",
                        "Xóa máy thi: " + (dev != null ? dev.getDeviceName() : id), id);
                SessionUtil.flash(req, "success", "Đã xóa máy thi.");
            } else {
                SessionUtil.flash(req, "danger", "Xóa máy thi thất bại.");
            }
            resp.sendRedirect(req.getContextPath() + "/admin/exam-computer");
            return;
        }

        int id = Sanitize.toInt(req.getParameter("examDeviceId"), 0);
        String name = Sanitize.text(req.getParameter("deviceName"));
        String type = Sanitize.text(req.getParameter("deviceType"));
        String status = Sanitize.text(req.getParameter("status"));
        int roomId = Sanitize.toInt(req.getParameter("examRoomId"), 0);
        int areaId = Sanitize.toInt(req.getParameter("examAreaId"), 0);
        boolean isEdit = id > 0;

        // Validate — khu vực -> phòng -> các field còn lại
        String error = null;
        if (areaId <= 0) {
            error = "Vui lòng chọn khu vực thi.";
        } else if (roomId <= 0) {
            error = "Vui lòng chọn phòng thi.";
        } else if (name.isEmpty()) {
            error = "Vui lòng nhập tên máy thi.";
        } else {
            error = Validator.name("Tên máy/thiết bị", name, 2, 100);
        }
        if (error == null && type.isEmpty()) error = "Vui lòng nhập loại thiết bị.";
        if (error == null && status.isEmpty()) error = "Vui lòng chọn tình trạng máy.";

        if (error != null) {
            respond(req, resp, ajax, false, error);
            return;
        }

        ExamDeviceView dev = new ExamDeviceView();
        dev.setExamDeviceId(id);
        dev.setDeviceName(name);
        dev.setDeviceType(type);
        dev.setStatus(status);
        dev.setExamRoomId(roomId);

        boolean ok;
        String msg;
        if (isEdit) {
            ok = dao.update(dev, adminId);
            AuditLogHelper.persist(req.getSession(), "UPDATE", "Cập nhật máy thi: " + name, id);
            msg = ok ? "Đã cập nhật máy \"" + name + "\"." : "Cập nhật máy thi thất bại.";
        } else {
            int newId = dao.insert(dev, adminId);
            ok = newId > 0;
            AuditLogHelper.persist(req.getSession(), "INSERT", "Tạo máy thi: " + name, newId);
            msg = ok ? "Đã thêm máy \"" + name + "\"." : "Thêm máy thi thất bại.";
        }
        respond(req, resp, ajax, ok, msg);
    }

    // ---------- helpers ----------

    private void respond(HttpServletRequest req, HttpServletResponse resp,
                         boolean ajax, boolean ok, String message) throws IOException {
        if (ajax) {
            if (ok) SessionUtil.flash(req, "success", message);
            writeJson(resp, "{\"ok\":" + ok + ",\"message\":\"" + esc(message) + "\"}");
        } else {
            SessionUtil.flash(req, ok ? "success" : "danger", message);
            resp.sendRedirect(req.getContextPath() + "/admin/exam-computer");
        }
    }

    private void writeRoomsByArea(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Integer areaId = Sanitize.toIntegerOrNull(req.getParameter("areaId"));
        List<ExamRoom> rooms = (areaId == null || areaId <= 0)
                ? java.util.Collections.emptyList()
                : roomDAO.search(null, areaId, null, null);
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < rooms.size(); i++) {
            ExamRoom r = rooms.get(i);
            if (i > 0) sb.append(',');
            sb.append("{")
              .append("\"id\":").append(r.getId()).append(',')
              .append("\"code\":\"").append(esc(r.getCode())).append("\",")
              .append("\"name\":\"").append(esc(r.getRoomName())).append("\",")
              .append("\"typeLabel\":\"").append("theory".equals(r.getRoomType()) ? "Lý thuyết" : "Thực hành").append("\"")
              .append("}");
        }
        sb.append("]");
        writeJson(resp, sb.toString());
    }

    private void writeJson(HttpServletResponse resp, String json) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();
        out.write(json);
        out.flush();
    }

    private static String esc(String s) {
        if (s == null) return "";
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': b.append("\\\""); break;
                case '\\': b.append("\\\\"); break;
                case '\n': b.append("\\n"); break;
                case '\r': b.append("\\r"); break;
                case '\t': b.append("\\t"); break;
                default: b.append(c);
            }
        }
        return b.toString();
    }
}