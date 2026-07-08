package Controllers.Admin;

import DAO.ExamDeviceManageDAO;
import DAO.ExamRoomDAO;
import DAO.Impl.ExamDeviceManageDAOImpl;
import DAO.Impl.ExamRoomDAOImpl;
import Models.ExamDeviceView;
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

@WebServlet(name = "ExamDeviceServlet", urlPatterns = {"/admin/exam-computer"})
public class ExamDeviceServlet extends HttpServlet {

    private final ExamDeviceManageDAO dao = new ExamDeviceManageDAOImpl();
    private final ExamRoomDAO roomDAO = new ExamRoomDAOImpl();
    private static final String LIST_VIEW = "/views/admin/exam-computer.jsp";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!SessionUtil.requireAdmin(req, resp)) {
            return;
        }

        String keyword = Sanitize.text(req.getParameter("searchKeyword"));
        Integer roomId = Sanitize.toIntegerOrNull(req.getParameter("filterRoom"));
        String status = Sanitize.text(req.getParameter("filterStatus"));

        req.setAttribute("examDevices", dao.search(keyword, roomId, status));
        req.setAttribute("rooms", roomDAO.search(null, null, null, null));
        req.setAttribute("totalDevices", dao.countAll());
        // "Đang hoạt động" = Operational + Available (theo vocab hiện có trong DB của nhóm)
        req.setAttribute("activeDevices", dao.countByStatus("Operational") + dao.countByStatus("Available"));
        req.setAttribute("maintenanceDevices", dao.countByStatus("Maintenance"));
        req.setAttribute("brokenDevices", dao.countByStatus("Broken"));
        req.getRequestDispatcher(LIST_VIEW).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!SessionUtil.requireAdmin(req, resp)) {
            return;
        }
        String action = Sanitize.text(req.getParameter("action"));
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
        boolean isEdit = id > 0;

        String error = Validator.name("Tên máy/thiết bị", name, 2, 100);
        if (name.isEmpty()) {
            error = "Vui lòng nhập tên máy thi.";
        } else if (type.isEmpty()) {
            error = "Vui lòng nhập loại thiết bị.";
        } else if (status.isEmpty()) {
            error = "Vui lòng chọn tình trạng máy.";
        } else if (roomId <= 0) {
            error = "Vui lòng chọn phòng thi.";
        }

        if (error != null) {
            SessionUtil.flash(req, "danger", error);
            resp.sendRedirect(req.getContextPath() + "/admin/exam-computer");
            return;
        }

        ExamDeviceView dev = new ExamDeviceView();
        dev.setExamDeviceId(id);
        dev.setDeviceName(name);
        dev.setDeviceType(type);
        dev.setStatus(status);
        dev.setExamRoomId(roomId);

        if (isEdit) {
            boolean ok = dao.update(dev, adminId);
            AuditLogHelper.persist(req.getSession(), "UPDATE", "Cập nhật máy thi: " + name, id);
            SessionUtil.flash(req, ok ? "success" : "danger",
                    ok ? "Đã cập nhật máy \"" + name + "\"." : "Cập nhật máy thi thất bại.");
        } else {
            int newId = dao.insert(dev, adminId);
            boolean ok = newId > 0;
            AuditLogHelper.persist(req.getSession(), "INSERT", "Tạo máy thi: " + name, newId);
            SessionUtil.flash(req, ok ? "success" : "danger",
                    ok ? "Đã thêm máy \"" + name + "\"." : "Thêm máy thi thất bại.");
        }
        resp.sendRedirect(req.getContextPath() + "/admin/exam-computer");
    }
}
