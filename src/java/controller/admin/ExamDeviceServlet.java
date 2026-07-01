package controller.admin;
import dto.*;
import model.*;
import model.*;
import service.*;
import service.impl.*;
import service.ExamDeviceService;
import service.impl.ExamDeviceServiceImpl;
import dto.ExamDeviceViewDTO;
import model.User;
import service.AuditLogService;
import util.Sanitize;
import enums.DeviceStatus;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
@WebServlet(name = "ExamDeviceServlet", urlPatterns = {"/admin/exam-computer"})
public class ExamDeviceServlet extends HttpServlet {
    private final AuditLogService auditLogService = new AuditLogServiceImpl();
    private ExamDeviceService examDeviceService;
    private static final String LIST_VIEW = "/views/admin/exam-computer.jsp";
    @Override
    public void init() {
        examDeviceService = new ExamDeviceServiceImpl();
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String keyword = Sanitize.text(req.getParameter("searchKeyword"));
        String status = Sanitize.text(req.getParameter("filterStatus"));
        req.setAttribute("examDevices", examDeviceService.search(keyword, status));
        req.setAttribute("totalDevices", examDeviceService.countAll());
        req.setAttribute("activeDevices", examDeviceService.countByStatus(DeviceStatus.HOAT_DONG.getDisplayName()));
        req.setAttribute("maintenanceDevices", examDeviceService.countByStatus(DeviceStatus.BAO_TRI.getDisplayName()));
        req.setAttribute("brokenDevices", 0);
        req.getRequestDispatcher(LIST_VIEW).forward(req, resp);
    }
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = Sanitize.text(req.getParameter("action"));
        User admin = (User) req.getSession().getAttribute("user");
        Integer adminId = (admin != null) ? admin.getUserId() : null;
        if ("delete".equals(action)) {
            int id = Sanitize.toInt(req.getParameter("id"), 0);
            ExamDeviceService.DeleteResult result = examDeviceService.delete(id, adminId);
            if (result.success) {
                auditLogService.logAction(((User) req.getSession().getAttribute("user")).getUserId(), "DELETE", "Xóa máy thi id: " + id, id);
                HttpSession flashSession = req.getSession(true);
                flashSession.setAttribute("flashType", "success");
                flashSession.setAttribute("flashMessage", result.message);
            } else {
                HttpSession flashSession = req.getSession(true);
                flashSession.setAttribute("flashType", "danger");
                flashSession.setAttribute("flashMessage", result.message);
            }
            resp.sendRedirect(req.getContextPath() + "/admin/exam-computer");
            return;
        }
        int id = Sanitize.toInt(req.getParameter("examDeviceId"), 0);
        String name = Sanitize.text(req.getParameter("deviceName"));
        String type = Sanitize.text(req.getParameter("deviceType"));
        String status = Sanitize.text(req.getParameter("status"));
        int areaId = Sanitize.toInt(req.getParameter("examAreaId"), 0);
        boolean isEdit = id > 0;
        ExamDeviceViewDTO dev = new ExamDeviceViewDTO();
        dev.setExamDeviceId(id);
        dev.setDeviceName(name);
        dev.setDeviceType(type);
        dev.setStatus(status);
        dev.setExamAreaId(areaId);
        ExamDeviceService.SaveResult result = examDeviceService.save(dev, adminId);
        if (!result.success) {
            HttpSession flashSession = req.getSession(true);
            flashSession.setAttribute("flashType", "danger");
            flashSession.setAttribute("flashMessage", result.message);
            resp.sendRedirect(req.getContextPath() + "/admin/exam-computer");
            return;
        }
        auditLogService.logAction(((User) req.getSession().getAttribute("user")).getUserId(), isEdit ? "UPDATE" : "INSERT", 
                (isEdit ? "Cập nhật máy thi: " : "Tạo máy thi: ") + name, result.id);
        HttpSession flashSession = req.getSession(true);
        flashSession.setAttribute("flashType", "success");
        flashSession.setAttribute("flashMessage", result.message);
        resp.sendRedirect(req.getContextPath() + "/admin/exam-computer");
    }
}
