package controller.examiner;
import dto.ExaminerSlotDTO;
import filter.ExaminerPortalFilter;
import model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServlet;
import service.ExaminerDataService;
import service.impl.ExaminerDataServiceImpl;
import service.ExaminerActionsService;
import service.impl.ExaminerActionsServiceImpl;
import java.io.IOException;
import java.util.Map;
@WebServlet("/views/examiner/devices")
public class ExaminerDevicesServlet extends HttpServlet {
    protected final ExaminerDataService viewDataService = new ExaminerDataServiceImpl();
    protected final ExaminerActionsService examinerService = new ExaminerActionsServiceImpl();
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = requireSession(request, response);
        if (session == null) {
            return;
        }
        Integer sessionId = activeSessionId(session);
        String search = request.getParameter("q");
        String action = request.getParameter("action");
        if (sessionId != null && sessionId > 0) {
            if (action != null && ("maintenance".equals(action) || "operational".equals(action))) {
                int deviceId;
                try {
                    deviceId = Integer.parseInt(request.getParameter("deviceId"));
                } catch (Exception e) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/devices?error=invalidDevice");
                    return;
                }
                boolean updated;
                String redirectParam;
                if ("operational".equals(action)) {
                    updated = examinerService.setDeviceAvailable(deviceId, ((User) session.getAttribute("user")).getUserId());
                    redirectParam = updated ? "/views/examiner/devices?operationalDone=" + deviceId : "/views/examiner/devices?error=operationalFailed&deviceId=" + deviceId;
                } else {
                    updated = examinerService.setDeviceMaintenance(deviceId, ((User) session.getAttribute("user")).getUserId());
                    redirectParam = updated ? "/views/examiner/devices?maintenanceDone=" + deviceId : "/views/examiner/devices?error=maintenanceFailed&deviceId=" + deviceId;
                }
                response.sendRedirect(request.getContextPath() + redirectParam);
                return;
            }
            Integer preferredAreaId = null;
            Object slotObj = session.getAttribute(ExaminerPortalFilter.ATTR_SLOT);
            if (slotObj instanceof ExaminerSlotDTO) {
                int areaId = ((ExaminerSlotDTO) slotObj).getAreaId();
                if (areaId > 0) {
                    preferredAreaId = areaId;
                }
            }
            Map<String, Object> data = viewDataService.getDevicesData(sessionId, search, preferredAreaId);
            for (Map.Entry<String, Object> mapEntry : data.entrySet()) {
                request.setAttribute(mapEntry.getKey(), mapEntry.getValue());
            }
        }
        request.getRequestDispatcher("/views/examiner/devices.jsp").forward(request, response);
    }
    private HttpSession requireSession(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
        return session;
    }
    private Integer activeSessionId(HttpSession session) {
        if (session == null) {
            return null;
        }
        return (Integer) session.getAttribute(ExaminerPortalFilter.ATTR_ACTIVE_SESSION_ID);
    }
}
