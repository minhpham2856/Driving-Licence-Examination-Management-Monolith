package Controllers.Admin;

import DAOs.ExamDeviceManageDAO;
import DAOs.Impl.ExamDeviceManageDAOImpl;
import DTOs.ExamDeviceViewDTO;
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
 * Admin "MÃ¡y thi" management. Uses ExamDeviceManageDAO (NOT the team's
 * DAO.ExamDeviceDAO, which serves a different feature). A device belongs to an
 * ExamArea.
 *
 * GET  /admin/exam-computer                -> list (filters: searchKeyword, filterStatus)
 * POST /admin/exam-computer?action=save    -> insert or update
 * POST /admin/exam-computer?action=delete  -> delete
 */
@WebServlet(name = "ExamDeviceServlet", urlPatterns = {"/admin/exam-computer"})
public class ExamDeviceServlet extends HttpServlet {

    private final ExamDeviceManageDAO dao = new ExamDeviceManageDAOImpl();
    private static final String LIST_VIEW = "/views/admin/exam-computer.jsp";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!SessionUtil.requireAdmin(req, resp)) return;

        String keyword = Sanitize.text(req.getParameter("searchKeyword"));
        String status = Sanitize.text(req.getParameter("filterStatus"));

        req.setAttribute("examDevices", dao.search(keyword, status));
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
        String action = Sanitize.text(req.getParameter("action"));
        User admin = SessionUtil.getCurrentUser(req);
        Integer adminId = (admin != null) ? admin.getId() : null;

        if ("delete".equals(action)) {
            int id = Sanitize.toInt(req.getParameter("id"), 0);
            ExamDeviceViewDTO dev = dao.findById(id);
            boolean ok = id > 0 && dao.delete(id);
            if (ok) {
                AuditLogHelper.persist(req.getSession(), "DELETE",
                        "XÃ³a mÃ¡y thi: " + (dev != null ? dev.getDeviceName() : id), id);
                SessionUtil.flash(req, "success", "ÄÃ£ xÃ³a mÃ¡y thi.");
            } else {
                SessionUtil.flash(req, "danger", "XÃ³a mÃ¡y thi tháº¥t báº¡i.");
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

        String error = null;
        if (name.isEmpty()) error = "Vui lÃ²ng nháº­p tÃªn mÃ¡y thi.";
        else if (type.isEmpty()) error = "Vui lÃ²ng nháº­p loáº¡i thiáº¿t bá»‹.";
        else if (status.isEmpty()) error = "Vui lÃ²ng chá»�n tÃ¬nh tráº¡ng mÃ¡y.";
        else if (areaId <= 0) error = "Vui lÃ²ng chá»�n khu vá»±c thi.";

        if (error != null) {
            SessionUtil.flash(req, "danger", error);
            resp.sendRedirect(req.getContextPath() + "/admin/exam-computer");
            return;
        }

        ExamDeviceViewDTO dev = new ExamDeviceViewDTO();
        dev.setExamDeviceId(id);
        dev.setDeviceName(name);
        dev.setDeviceType(type);
        dev.setStatus(status);
        dev.setExamAreaId(areaId);

        if (isEdit) {
            boolean ok = dao.update(dev, adminId);
            AuditLogHelper.persist(req.getSession(), "UPDATE", "Cáº­p nháº­t mÃ¡y thi: " + name, id);
            SessionUtil.flash(req, ok ? "success" : "danger",
                    ok ? "ÄÃ£ cáº­p nháº­t mÃ¡y \"" + name + "\"." : "Cáº­p nháº­t mÃ¡y thi tháº¥t báº¡i.");
        } else {
            int newId = dao.insert(dev, adminId);
            boolean ok = newId > 0;
            AuditLogHelper.persist(req.getSession(), "INSERT", "Táº¡o mÃ¡y thi: " + name, newId);
            SessionUtil.flash(req, ok ? "success" : "danger",
                    ok ? "ÄÃ£ thÃªm mÃ¡y \"" + name + "\"." : "ThÃªm mÃ¡y thi tháº¥t báº¡i.");
        }
        resp.sendRedirect(req.getContextPath() + "/admin/exam-computer");
    }
}
