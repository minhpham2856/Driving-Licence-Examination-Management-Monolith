package Controllers.Admin;

import DAOs.ExamAreaDAO;
import DAOs.Impl.ExamAreaDAOImpl;
import Models.ExamArea;
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

// Handles list / create / edit / delete for Exam Areas.
//   GET  /admin/exam-area                 -> list
//   GET  /admin/exam-area?action=new      -> empty form
//   GET  /admin/exam-area?action=edit&id= -> edit form
//   POST /admin/exam-area?action=save     -> insert or update
//   POST /admin/exam-area?action=delete   -> delete
@WebServlet(name = "ExamAreaServlet", urlPatterns = {"/admin/exam-area"})
public class ExamAreaServlet extends HttpServlet {

    private final ExamAreaDAO dao = new ExamAreaDAOImpl();
    private static final String LIST_VIEW = "/views/admin/exam-area.jsp";
    private static final String FORM_VIEW = "/views/admin/exam-area-form.jsp";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!SessionUtil.requireAdmin(req, resp)) return;
        String action = Sanitize.text(req.getParameter("action"));

        if ("new".equals(action)) {
            req.setAttribute("mode", "create");
            req.getRequestDispatcher(FORM_VIEW).forward(req, resp);
        } else if ("edit".equals(action)) {
            int id = Sanitize.toInt(req.getParameter("id"), 0);
            ExamArea area = dao.getById(id);
            if (area == null) {
                SessionUtil.flash(req, "danger", "Không tìm thấy khu vực thi cần sửa.");
                resp.sendRedirect(req.getContextPath() + "/admin/exam-area");
                return;
            }
            req.setAttribute("mode", "edit");
            req.setAttribute("area", area);
            req.getRequestDispatcher(FORM_VIEW).forward(req, resp);
        } else {
            String keyword = Sanitize.text(req.getParameter("searchKeyword"));
            String type = Sanitize.text(req.getParameter("filterType"));
            req.setAttribute("examAreas", dao.search(keyword, type));
            req.setAttribute("totalAreas", dao.countAll());
            req.getRequestDispatcher(LIST_VIEW).forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!SessionUtil.requireAdmin(req, resp)) return;
        String action = Sanitize.text(req.getParameter("action"));
        User admin = SessionUtil.getCurrentUser(req);

        if ("delete".equals(action)) {
            handleDelete(req, resp, admin);
            return;
        }
        handleSave(req, resp, admin);
    }

    private void handleSave(HttpServletRequest req, HttpServletResponse resp, User admin)
            throws ServletException, IOException {
        int id = Sanitize.toInt(req.getParameter("examAreaId"), 0);
        String name = Sanitize.text(req.getParameter("areaName"));
        String type = Sanitize.text(req.getParameter("areaType"));
        String location = Sanitize.text(req.getParameter("location"));
        int capacity = Sanitize.toInt(req.getParameter("capacity"), 0);
        boolean isEdit = id > 0;

        String error = null;
        if (name.isEmpty()) error = "Vui lòng nhập tên khu vực thi.";
        else if (type.isEmpty()) error = "Vui lòng chọn loại khu vực.";
        else if (location.isEmpty()) error = "Vui lòng nhập địa chỉ khu vực.";
        else if (capacity <= 0) error = "Sức chứa phải lớn hơn 0.";

        if (error != null) {
            ExamArea area = build(id, name, type, location, capacity);
            req.setAttribute("mode", isEdit ? "edit" : "create");
            req.setAttribute("area", area);
            req.setAttribute("error", error);
            req.getRequestDispatcher(FORM_VIEW).forward(req, resp);
            return;
        }

        ExamArea area = build(id, name, type, location, capacity);
        if (isEdit) {
            boolean ok = dao.update(area);
            AuditLogHelper.persist(req.getSession(), "UPDATE", "cap nhat khu vuc thi: " + name, id);
            SessionUtil.flash(req, ok ? "success" : "danger",
                    ok ? "da cap nhat khu vuc \"" + name + "\"." : "cap nhat khu vuc that bai");
        } else {
            int newId = dao.insert(area);
            boolean ok = newId > 0;
            AuditLogHelper.persist(req.getSession(), "INSERT", "tao khu vuc thi: " + name, newId);
            SessionUtil.flash(req, ok ? "success" : "danger",
                    ok ? "Đã thêm khu vực \"" + name + "\"." : "them khu vuc that bai");
        }
        resp.sendRedirect(req.getContextPath() + "/admin/exam-area");
    }

    private void handleDelete(HttpServletRequest req, HttpServletResponse resp, User admin)
            throws IOException {
        int id = Sanitize.toInt(req.getParameter("id"), 0);
        ExamArea area = dao.getById(id);
        boolean ok = id > 0 && dao.delete(id);
        if (ok) {
            AuditLogHelper.persist(req.getSession(), "DELETE",
                    "Xóa khu vực thi: " + (area != null ? area.getAreaName() : id), id);
            SessionUtil.flash(req, "success", "Đã xóa khu vực thi.");
        } else {
            SessionUtil.flash(req, "danger",
                    "Không thể xóa khu vực này (có thể đang được sử dụng bởi phòng/thiết bị/kỳ thi).");
        }
        resp.sendRedirect(req.getContextPath() + "/admin/exam-area");
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
