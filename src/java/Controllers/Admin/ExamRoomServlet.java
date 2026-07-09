package Controllers.Admin;

import DAO.ExamAreaDAO;
import DAO.ExamRoomDAO;
import DAO.Impl.ExamAreaDAOImpl;
import DAO.Impl.ExamRoomDAOImpl;
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

@WebServlet(name = "ExamRoomServlet", urlPatterns = {"/admin/exam-room"})
public class ExamRoomServlet extends HttpServlet {

    private final ExamRoomDAO dao = new ExamRoomDAOImpl();
    private final ExamAreaDAO areaDAO = new ExamAreaDAOImpl();
    private static final String LIST_VIEW = "/views/admin/exam-room.jsp";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!SessionUtil.requireAdmin(req, resp)) return;
        req.setCharacterEncoding("UTF-8");

        String action = Sanitize.text(req.getParameter("action"));

        // ---- AJAX: danh sách phòng theo khu vực (cascade) ----
        if ("roomsByArea".equals(action)) {
            writeRoomsByArea(req, resp);
            return;
        }

        String keyword = Sanitize.text(req.getParameter("searchKeyword"));
        Integer areaId = Sanitize.toIntegerOrNull(req.getParameter("filterArea"));
        String type = Sanitize.text(req.getParameter("filterType"));
        String status = Sanitize.text(req.getParameter("filterStatus"));

        req.setAttribute("examRooms", dao.search(keyword, areaId, type, status));
        req.setAttribute("examAreas", areaDAO.search(null, null)); // dropdown
        req.setAttribute("totalRooms", dao.countAll());
        req.setAttribute("activeRooms", dao.countByStatus("active"));
        req.setAttribute("theoryRooms", dao.countByType("theory"));
        req.setAttribute("practicalRooms", dao.countByType("practical"));
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
        String ctx = req.getContextPath();

        if ("delete".equals(action)) {
            int id = Sanitize.toInt(req.getParameter("id"), 0);
            ExamRoom room = dao.findById(id);
            boolean ok = id > 0 && dao.delete(id);
            if (ok) {
                AuditLogHelper.persist(req.getSession(), "DELETE",
                        "Xóa phòng thi: " + (room != null ? room.getRoomName() : id), id);
                SessionUtil.flash(req, "success", "Đã xóa phòng thi.");
            } else {
                SessionUtil.flash(req, "danger",
                        "Không thể xóa phòng này (có thể đang có máy thi trực thuộc).");
            }
            resp.sendRedirect(ctx + "/admin/exam-room");
            return;
        }

        // ---- save (insert/update) ----
        int id = Sanitize.toInt(req.getParameter("examRoomId"), 0);
        String name = Sanitize.text(req.getParameter("roomName"));
        String type = Sanitize.text(req.getParameter("roomType"));
        String status = Sanitize.text(req.getParameter("status"));
        String floor = Sanitize.text(req.getParameter("floor"));
        Integer capacity = Sanitize.toIntegerOrNull(req.getParameter("capacity"));
        int areaId = Sanitize.toInt(req.getParameter("examAreaId"), 0);
        boolean isEdit = id > 0;

        // Validate — khu vực trước để đúng thứ tự cascade
        String error = null;
        if (areaId <= 0) {
            error = "Vui lòng chọn khu vực thi.";
        } else if (name.isEmpty()) {
            error = "Vui lòng nhập tên phòng thi.";
        } else {
            error = Validator.name("Tên phòng thi", name, 3, 100);
        }
        if (error == null && type.isEmpty()) error = "Vui lòng chọn loại phòng.";
        if (error == null && status.isEmpty()) error = "Vui lòng chọn trạng thái.";
        if (error == null && capacity != null) error = Validator.intRange("Sức chứa", capacity, 0, 1000);
        if (error == null && isDuplicateName(name, areaId, id)) {
            error = "Khu vực này đã có phòng thi tên \"" + name + "\".";
        }

        if (error != null) {
            respond(req, resp, ajax, false, error);
            return;
        }

        ExamRoom room = new ExamRoom();
        room.setExamRoomId(id);
        room.setRoomName(name);
        room.setRoomType(type);
        room.setStatus(status);
        room.setFloor(floor.isEmpty() ? null : floor);
        room.setCapacity(capacity);
        room.setExamAreaId(areaId);

        boolean ok;
        String msg;
        if (isEdit) {
            room.setUpdatedByUserId(admin.getId());
            ok = dao.update(room);
            AuditLogHelper.persist(req.getSession(), "UPDATE", "Cập nhật phòng thi: " + name, id);
            msg = ok ? "Đã cập nhật phòng \"" + name + "\"." : "Cập nhật phòng thi thất bại.";
        } else {
            room.setCreatedByUserId(admin.getId());
            room.setUpdatedByUserId(admin.getId());
            int newId = dao.insert(room);
            ok = newId > 0;
            AuditLogHelper.persist(req.getSession(), "INSERT", "Tạo phòng thi: " + name, newId);
            msg = ok ? "Đã thêm phòng \"" + name + "\"." : "Thêm phòng thi thất bại.";
        }
        respond(req, resp, ajax, ok, msg);
    }

    // ---------- helpers ----------

    private void respond(HttpServletRequest req, HttpServletResponse resp,
                         boolean ajax, boolean ok, String message) throws IOException {
        if (ajax) {
            if (ok) SessionUtil.flash(req, "success", message); // hiện sau khi JS reload
            writeJson(resp, "{\"ok\":" + ok + ",\"message\":\"" + esc(message) + "\"}");
        } else {
            SessionUtil.flash(req, ok ? "success" : "danger", message);
            resp.sendRedirect(req.getContextPath() + "/admin/exam-room");
        }
    }

    private boolean isDuplicateName(String name, int areaId, int selfId) {
        List<ExamRoom> rooms = dao.search(null, areaId, null, null);
        for (ExamRoom r : rooms) {
            if (r.getExamRoomId() != selfId
                    && r.getRoomName() != null
                    && r.getRoomName().trim().equalsIgnoreCase(name.trim())) {
                return true;
            }
        }
        return false;
    }

    private void writeRoomsByArea(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Integer areaId = Sanitize.toIntegerOrNull(req.getParameter("areaId"));
        List<ExamRoom> rooms = (areaId == null || areaId <= 0)
                ? java.util.Collections.emptyList()
                : dao.search(null, areaId, null, null);
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