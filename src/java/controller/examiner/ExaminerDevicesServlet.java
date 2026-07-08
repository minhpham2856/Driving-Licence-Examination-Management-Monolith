package controller.examiner;

import model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServlet;
import service.ExamViewService;
import service.impl.ExamViewServiceImpl;
import service.CallService;
import service.impl.CallServiceImpl;
import java.io.IOException;
import java.util.Map;

@WebServlet("/views/examiner/devices")
public class ExaminerDevicesServlet extends HttpServlet {
    protected final ExamViewService viewDataService = new ExamViewServiceImpl();
    protected final CallService callService = new CallServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        Integer sessionId = (Integer) session.getAttribute("activeSessionId");
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
                boolean updated = false;
                if ("operational".equals(action)) {
                    updated = callService.setDeviceAvailable(deviceId, ((User) session.getAttribute("user")).getUserId()).isSuccess();
                } else {
                    updated = callService.setDeviceMaintenance(deviceId, ((User) session.getAttribute("user")).getUserId()).isSuccess();
                }
                if (!updated) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/devices?error=updateFailed");
                    return;
                }
                response.sendRedirect(request.getContextPath() + "/views/examiner/devices");
                return;
            }

            Integer preferredAreaId = null;
            Map<String, Object> data = viewDataService.getDevicesData(sessionId, search, preferredAreaId);
            if (data != null) {
                for (Map.Entry<String, Object> mapEntry : data.entrySet()) {
                    request.setAttribute(mapEntry.getKey(), mapEntry.getValue());
                }
            }
        }
        request.getRequestDispatcher("/views/examiner/devices.jsp").forward(request, response);
    }
}
