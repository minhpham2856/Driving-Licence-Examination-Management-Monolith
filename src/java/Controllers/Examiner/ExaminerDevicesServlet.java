package Controllers.Examiner;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

// Handles viewing and toggling device (exam machine) statuses.
@WebServlet("/views/examiner/devices")
public class ExaminerDevicesServlet extends BaseExaminerServlet {

    // Renders the devices list and handles status toggle actions.
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = requireSession(request, response);
        if (session == null) return;

        Integer sessionId = activeSessionId(session);
        String search = request.getParameter("q");
        String action = request.getParameter("action");

        if (sessionId != null && sessionId > 0) {
            if (action != null && ("maintenance".equals(action) || "operational".equals(action))) {
                int deviceId;
                try {
                    deviceId = Integer.parseInt(request.getParameter("deviceId"));
                } catch (Exception e) {
                    redirect(response, request, "/views/examiner/devices?error=invalidDevice");
                    return;
                }

                boolean updated;
                String redirectParam;
                if ("operational".equals(action)) {
                    updated = examinerService.setDeviceAvailable(deviceId, session);
                    redirectParam = updated ? "/views/examiner/devices?operationalDone=" + deviceId : "/views/examiner/devices?error=operationalFailed&deviceId=" + deviceId;
                } else {
                    updated = examinerService.setDeviceMaintenance(deviceId, session);
                    redirectParam = updated ? "/views/examiner/devices?maintenanceDone=" + deviceId : "/views/examiner/devices?error=maintenanceFailed&deviceId=" + deviceId;
                }
                redirect(response, request, redirectParam);
                return;
            }

            viewDataService.attachDevices(request, sessionId, search);
        }

        forward(request, response, "/views/examiner/devices.jsp");
    }
}
