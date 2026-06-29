package controller.examiner;

import java.util.*;

import model.*;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServlet;
import util.ExaminerUtil;
import service.ExaminerDataService;
import service.impl.ExaminerDataServiceImpl;
import service.ExaminerActionsService;
import service.impl.ExaminerActionsServiceImpl;

import java.io.IOException;

@WebServlet("/views/examiner/devices")
public class ExaminerDevicesServlet extends HttpServlet {
    protected final ExaminerDataService viewDataService = new ExaminerDataServiceImpl();
    protected final ExaminerActionsService examinerService = new ExaminerActionsServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = ExaminerUtil.requireSession(request, response);
        if (session == null) return;

        Integer sessionId = ExaminerUtil.activeSessionId(session);
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

            Map<String, Object> data = viewDataService.getDevicesData(sessionId, search); for(Map.Entry<String, Object> mapEntry : data.entrySet()) request.setAttribute(mapEntry.getKey(), mapEntry.getValue());
        }

        request.getRequestDispatcher("/views/examiner/devices.jsp").forward(request, response);
    }
}

