package Controllers.Admin;

import DAO.ExamAreaDAO;
import DAO.Impl.ExamAreaDAOImpl;
import Models.ExamArea;
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

/**
 * Exam Area management (dùng modal trong exam-area.jsp).
 * GET  /admin/exam-area                 -> list + lọc
 * POST /admin/exam-area?action=save     -> thêm/sửa (lỗi -> flash + về danh sách)
 * POST /admin/exam-area?action=delete   -> xóa
 * Bỏ hẳn trang exam-area-form.jsp: mọi lỗi báo bằng flash message ngay trên danh sách.
 */
@WebServlet(name = "ExamAreaServlet", urlPatterns = {"/admin/exam-area"})
public class ExamAreaServlet extends HttpServlet {

    private final ExamAreaDAO dao = new ExamAreaDAOImpl();
    private static final String LIST_VIEW = "/views/admin/exam-area.jsp";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!SessionUtil.requireAdmin(req, resp)) return;

        // Đảm bảo nhận dữ liệu từ khóa tìm kiếm tiếng Việt không bị lỗi font
        req.setCharacterEncoding("UTF-8");

        String keyword = Sanitize.text(req.getParameter("searchKeyword"));
        String type = Sanitize.text(req.getParameter("filterType"));
        req.setAttribute("examAreas", dao.search(keyword, type));
        req.setAttribute("totalAreas", dao.countAll());
        req.getRequestDispatcher(LIST_VIEW).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!SessionUtil.requireAdmin(req, resp)) return;

        // Đảm bảo dữ liệu form tiếng Việt gửi lên không bị lỗi font
        req.setCharacterEncoding("UTF-8");

        String action = Sanitize.text(req.getParameter("action"));
        User admin = SessionUtil.getCurrentUser(req);
        String ctx = req.getContextPath();

        if ("delete".equals(action)) {
            int id = Sanitize.toInt(req.getParameter("id"), 0);
            ExamArea area = dao.findById(id);
            boolean ok = id > 0 && dao.delete(id);
            if (ok) {
                AuditLogHelper.persist(req.getSession(), "DELETE",
                        "Xóa khu vực thi: " + (area != null ? area.getAreaName() : id), id);
                SessionUtil.flash(req, "success", "Đã xóa khu vực thi.");
            } else {
                SessionUtil.flash(req, "danger",
                        "Không thể xóa khu vực này (có thể đang được sử dụng bởi phòng/thiết bị/kỳ thi).");
            }
            resp.sendRedirect(ctx + "/admin/exam-area");
            return;
        }

        // ---- save (thêm/sửa) ----
        int id = Sanitize.toInt(req.getParameter("examAreaId"), 0);
        String name = Sanitize.text(req.getParameter("areaName"));
        String type = Sanitize.text(req.getParameter("areaType"));
        String location = Sanitize.text(req.getParameter("location"));
        int capacity = Sanitize.toInt(req.getParameter("capacity"), 0);
        boolean isEdit = id > 0;

        // Validate: mọi lỗi -> flash + về danh sách
        String error = Validator.name("Tên khu vực", name, 3, 100);
        if (error == null && type.isEmpty()) error = "Vui lòng chọn loại khu vực.";
        if (error == null && location.isEmpty()) error = "Vui lòng nhập địa điểm khu vực.";
        if (error == null) error = Validator.intRange("Sức chứa", capacity, 1, 100000);

        if (error != null) {
            SessionUtil.flash(req, "danger", error);
            resp.sendRedirect(ctx + "/admin/exam-area");
            return;
        }

        ExamArea area = build(id, name, type, location, capacity);
        if (isEdit) {
            area.setUpdatedByUserId(admin.getId());
            boolean ok = dao.update(area);
            AuditLogHelper.persist(req.getSession(), "UPDATE", "Cập nhật khu vực thi: " + name, id);
            SessionUtil.flash(req, ok ? "success" : "danger",
                    ok ? "Đã cập nhật khu vực \"" + name + "\"." : "Cập nhật khu vực thất bại.");
        } else {
            area.setCreatedByUserId(admin.getId());
            area.setUpdatedByUserId(admin.getId());
            int newId = dao.insert(area);
            boolean ok = newId > 0;
            AuditLogHelper.persist(req.getSession(), "INSERT", "Tạo khu vực thi: " + name, newId);
            SessionUtil.flash(req, ok ? "success" : "danger",
                    ok ? "Đã thêm khu vực \"" + name + "\"." : "Thêm khu vực thất bại.");
        }
        resp.sendRedirect(ctx + "/admin/exam-area");
    }

    private ExamArea build(int id, String name, String type, String location, int capacity) {
        ExamArea area = new ExamArea();
        area.setExamAreaId(id);
        area.setAreaName(name);
        area.setAreaType(type);
        area.setLocation(location);
        area.setCapacity(capacity);
        return area;
    }
}