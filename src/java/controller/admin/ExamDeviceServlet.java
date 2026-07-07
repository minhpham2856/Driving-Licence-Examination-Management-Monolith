package controller.admin;
import dto.*;
import model.*;
import model.*;
import service.*;
import service.impl.*;
import service.ExamDeviceService;
import service.impl.ExamDeviceServiceImpl;
import dto.ExamDeviceViewDTO;
import dto.ServiceResult;
import dto.payload.DeleteExamDeviceCommand;
import dto.payload.SaveExamDeviceCommand;
import dto.payload.SaveExamDeviceData;
import model.User;
import service.AuditLogService;
import util.FormatUtil;
import enums.AuditAction;
import enums.AuditEntity;
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
        String keyword = FormatUtil.text(req.getParameter("searchKeyword"));
        String status = FormatUtil.text(req.getParameter("filterStatus"));
        req.setAttribute("examDevices", examDeviceService.search(keyword, status));
        req.setAttribute("totalDevices", examDeviceService.countAll());
        req.setAttribute("activeDevices", examDeviceService.countByStatus(DeviceStatus.ACTIVE.getValue()));
        req.setAttribute("maintenanceDevices", examDeviceService.countByStatus(DeviceStatus.MAINTENANCE.getValue()));
        req.setAttribute("brokenDevices", 0);
        req.getRequestDispatcher(LIST_VIEW).forward(req, resp);
    }
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = FormatUtil.text(req.getParameter("action"));
        User admin = (User) req.getSession().getAttribute("user");
        Integer adminId = (admin != null) ? admin.getUserId() : null;
        if ("delete".equals(action)) {
            int id = FormatUtil.toInt(req.getParameter("id"), 0);
            DeleteExamDeviceCommand deleteCommand = new DeleteExamDeviceCommand();
            deleteCommand.setDeviceId(id);
            deleteCommand.setAdminUserId(adminId);
            ServiceResult<Void> result = examDeviceService.delete(deleteCommand);
            if (result.isSuccess()) {
                auditLogService.logAction(((User) req.getSession().getAttribute("user")).getUserId(),
                        AuditAction.DELETE, AuditEntity.EXAM_DEVICE, "Xóa máy thi id: " + id, id);
                HttpSession flashSession = req.getSession(true);
                flashSession.setAttribute("flashType", "success");
                flashSession.setAttribute("flashMessage", result.getMessage());
            } else {
                HttpSession flashSession = req.getSession(true);
                flashSession.setAttribute("flashType", "danger");
                flashSession.setAttribute("flashMessage", result.getMessage());
            }
            resp.sendRedirect(req.getContextPath() + "/admin/exam-computer");
            return;
        }
        int id = FormatUtil.toInt(req.getParameter("examDeviceId"), 0);
        String name = FormatUtil.text(req.getParameter("deviceName"));
        String type = FormatUtil.text(req.getParameter("deviceType"));
        String status = FormatUtil.text(req.getParameter("status"));
        int areaId = FormatUtil.toInt(req.getParameter("examAreaId"), 0);
        boolean isEdit = id > 0;
        ExamDeviceViewDTO dev = new ExamDeviceViewDTO();
        dev.setExamDeviceId(id);
        dev.setDeviceName(name);
        dev.setDeviceType(type);
        dev.setStatus(status);
        dev.setExamAreaId(areaId);
        SaveExamDeviceCommand saveCommand = new SaveExamDeviceCommand();
        saveCommand.setDevice(dev);
        saveCommand.setAdminUserId(adminId);
        ServiceResult<SaveExamDeviceData> result = examDeviceService.save(saveCommand);
        if (!result.isSuccess()) {
            HttpSession flashSession = req.getSession(true);
            flashSession.setAttribute("flashType", "danger");
            flashSession.setAttribute("flashMessage", result.getMessage());
            resp.sendRedirect(req.getContextPath() + "/admin/exam-computer");
            return;
        }
        int savedId = result.getData() != null ? result.getData().getDeviceId() : dev.getExamDeviceId();
        auditLogService.logAction(((User) req.getSession().getAttribute("user")).getUserId(),
                isEdit ? AuditAction.UPDATE : AuditAction.CREATE, AuditEntity.EXAM_DEVICE,
                (isEdit ? "Cập nhật máy thi: " : "Tạo máy thi: ") + name, savedId);
        HttpSession flashSession = req.getSession(true);
        flashSession.setAttribute("flashType", "success");
        flashSession.setAttribute("flashMessage", result.getMessage());
        resp.sendRedirect(req.getContextPath() + "/admin/exam-computer");
    }
}
