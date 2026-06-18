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
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Exam Room management (Phòng thi).
 * GET  /admin/exam-room                 -> list (filters: searchKeyword, filterArea, filterType, filterStatus)
 * POST /admin/exam-room?action=save     -> insert or update (modal form)
 * POST /admin/exam-room?action=delete   -> delete
 * Create/Edit/Detail use in-page modals, so no separate form page is needed.
 */
@WebServlet(name = "ExamRoomServlet", urlPatterns = {"/admin/exam-room"})
public class ExamRoomServlet extends HttpServlet {

    private final ExamRoomDAO dao = new ExamRoomDAOImpl();
    private final ExamAreaDAO areaDAO = new ExamAreaDAOImpl();
    private static final String LIST_VIEW = "/views/admin/exam-room.jsp";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!SessionUtil.requireAdmin(req, resp)) return;

        String keyword = Sanitize.text(req.getParameter("searchKeyword"));
        Integer areaId = Sanitize.toIntegerOrNull(req.getParameter("filterArea"));
        String type = Sanitize.text(req.getParameter("filterType"));
        String status = Sanitize.text(req.getParameter("filterStatus"));

        req.setAttribute("examRooms", dao.search(keyword, areaId, type, status));
        req.setAttribute("examAreas", areaDAO.search(null, null)); // dropdowns
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
        String action = Sanitize.text(req.getParameter("action"));
        User admin = SessionUtil.getCurrentUser(req);

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
            resp.sendRedirect(req.getContextPath() + "/admin/exam-room");
            return;
        }

        // save (insert/update)
        int id = Sanitize.toInt(req.getParameter("examRoomId"), 0);
        String name = Sanitize.text(req.getParameter("roomName"));
        String type = Sanitize.text(req.getParameter("roomType"));
        String status = Sanitize.text(req.getParameter("status"));
        String floor = Sanitize.text(req.getParameter("floor"));
        Integer capacity = Sanitize.toIntegerOrNull(req.getParameter("capacity"));
        int areaId = Sanitize.toInt(req.getParameter("examAreaId"), 0);
        boolean isEdit = id > 0;

        String error = null;
        if (name.isEmpty()) error = "Vui lòng nhập tên phòng thi.";
        else if (type.isEmpty()) error = "Vui lòng chọn loại phòng.";
        else if (status.isEmpty()) error = "Vui lòng chọn trạng thái.";
        else if (areaId <= 0) error = "Vui lòng chọn khu vực thi.";

        if (error != null) {
            SessionUtil.flash(req, "danger", error);
            resp.sendRedirect(req.getContextPath() + "/admin/exam-room");
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

        if (isEdit) {
            room.setUpdatedByUserId(admin.getId());
            boolean ok = dao.update(room);
            AuditLogHelper.persist(req.getSession(), "UPDATE", "Cập nhật phòng thi: " + name, id);
            SessionUtil.flash(req, ok ? "success" : "danger",
                    ok ? "Đã cập nhật phòng \"" + name + "\"." : "Cập nhật phòng thi thất bại.");
        } else {
            room.setCreatedByUserId(admin.getId());
            room.setUpdatedByUserId(admin.getId());
            int newId = dao.insert(room);
            boolean ok = newId > 0;
            AuditLogHelper.persist(req.getSession(), "INSERT", "Tạo phòng thi: " + name, newId);
            SessionUtil.flash(req, ok ? "success" : "danger",
                    ok ? "Đã thêm phòng \"" + name + "\"." : "Thêm phòng thi thất bại.");
        }
        resp.sendRedirect(req.getContextPath() + "/admin/exam-room");
    }
}