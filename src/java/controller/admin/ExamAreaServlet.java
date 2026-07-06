package controller.admin;
import dto.payload.SaveEntityData;
import dto.ServiceResult;
import model.*;
import model.*;
import service.*;
import service.impl.*;
import service.ExamAreaService;
import service.impl.ExamAreaServiceImpl;
import model.ExamArea;
import model.User;
import enums.AuditAction;
import enums.AuditEntity;
import service.AuditLogService;
import util.Sanitize;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
@WebServlet(name = "ExamAreaServlet", urlPatterns = {"/admin/exam-area"})
public class ExamAreaServlet extends HttpServlet {
    private final AuditLogService auditLogService = new AuditLogServiceImpl();
    private ExamAreaService examAreaService;
    private static final String LIST_VIEW = "/views/admin/exam-area.jsp";
    private static final String FORM_VIEW = "/views/admin/exam-area-form.jsp";
    @Override
    public void init() {
        examAreaService = new ExamAreaServiceImpl();
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = Sanitize.text(req.getParameter("action"));
        if ("new".equals(action)) {
            req.setAttribute("mode", "create");
            req.getRequestDispatcher(FORM_VIEW).forward(req, resp);
        } else if ("edit".equals(action)) {
            int id = Sanitize.toInt(req.getParameter("id"), 0);
            ExamArea area = examAreaService.getById(id);
            if (area == null) {
                HttpSession flashSession = req.getSession(true);
                flashSession.setAttribute("flashType", "danger");
                flashSession.setAttribute("flashMessage", "Không tìm thấy khu vực thi cần sửa.");
                resp.sendRedirect(req.getContextPath() + "/admin/exam-area");
                return;
            }
            req.setAttribute("mode", "edit");
            req.setAttribute("area", area);
            req.getRequestDispatcher(FORM_VIEW).forward(req, resp);
        } else {
            String keyword = Sanitize.text(req.getParameter("searchKeyword"));
            String type = Sanitize.text(req.getParameter("filterType"));
            req.setAttribute("examAreas", examAreaService.search(keyword, type));
            req.setAttribute("totalAreas", examAreaService.countAll());
            req.getRequestDispatcher(LIST_VIEW).forward(req, resp);
        }
    }
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = Sanitize.text(req.getParameter("action"));
        User admin = (User) req.getSession().getAttribute("user");
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
        ExamArea area = build(id, name, type, location, capacity);
        ServiceResult<SaveEntityData> result = examAreaService.save(area, admin.getUserId());
        if (!result.isSuccess()) {
            req.setAttribute("mode", isEdit ? "edit" : "create");
            req.setAttribute("area", area);
            req.setAttribute("error", result.getMessage());
            req.getRequestDispatcher(FORM_VIEW).forward(req, resp);
            return;
        }
        int savedId = result.getData() != null ? result.getData().getEntityId() : area.getExamAreaId();
        auditLogService.logAction(((User) req.getSession().getAttribute("user")).getUserId(),
                isEdit ? AuditAction.UPDATE : AuditAction.CREATE, AuditEntity.EXAM_ROOM,
                (isEdit ? "cap nhat khu vuc thi: " : "tao khu vuc thi: ") + name, savedId);
        HttpSession flashSession = req.getSession(true);
        flashSession.setAttribute("flashType", "success");
        flashSession.setAttribute("flashMessage", result.getMessage());
        resp.sendRedirect(req.getContextPath() + "/admin/exam-area");
    }
    private void handleDelete(HttpServletRequest req, HttpServletResponse resp, User admin)
            throws IOException {
        int id = Sanitize.toInt(req.getParameter("id"), 0);
        ExamArea area = examAreaService.getById(id);
        String name = area != null ? area.getAreaName() : String.valueOf(id);
        ServiceResult<Void> result = examAreaService.delete(id, admin.getUserId());
        if (result.isSuccess()) {
            auditLogService.logAction(((User) req.getSession().getAttribute("user")).getUserId(),
                    AuditAction.DELETE, AuditEntity.EXAM_ROOM, "Xóa khu vực thi: " + name, id);
            HttpSession flashSession = req.getSession(true);
        flashSession.setAttribute("flashType", "success");
        flashSession.setAttribute("flashMessage", result.getMessage());
        } else {
            HttpSession flashSession = req.getSession(true);
            flashSession.setAttribute("flashType", "danger");
            flashSession.setAttribute("flashMessage", result.getMessage());
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
