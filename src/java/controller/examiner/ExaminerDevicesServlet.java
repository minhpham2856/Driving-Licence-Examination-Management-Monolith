package controller.examiner;

import model.ExaminerSchedule;
import filter.ExaminerFilter;
import model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.ExamAreaService;
import service.ExamViewService;
import service.impl.ExamAreaServiceImpl;
import service.impl.ExamViewServiceImpl;
import service.CallService;
import service.impl.CallServiceImpl;
import java.io.IOException;
import java.util.Map;

@WebServlet("/views/examiner/devices")
public class ExaminerDevicesServlet extends BaseExaminerServlet {

    protected final ExamViewService viewDataService = new ExamViewServiceImpl();
    protected final CallService ScheduleService = new CallServiceImpl();
    private final ExamAreaService examAreaService = new ExamAreaServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = requireSession(request, response);
        if (session == null) {
            return;
        }
        Integer sessionId = getActiveSessionId(session);
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
                User deviceUser = (User) session.getAttribute("user");
                int actionUserId = deviceUser.getUserId();
                boolean updated;
                String redirectParam;
                if ("operational".equals(action)) {
                    updated = ScheduleService.setDeviceAvailable(deviceId, actionUserId).isSuccess();
                    redirectParam = updated ? "/views/examiner/devices?operationalDone=" + deviceId : "/views/examiner/devices?error=operationalFailed&deviceId=" + deviceId;
                } else {
                    updated = ScheduleService.setDeviceMaintenance(deviceId, actionUserId).isSuccess();
                    redirectParam = updated ? "/views/examiner/devices?maintenanceDone=" + deviceId : "/views/examiner/devices?error=maintenanceFailed&deviceId=" + deviceId;
                }
                response.sendRedirect(request.getContextPath() + redirectParam);
                return;
            }
            Integer preferredAreaId = null;
            ExaminerSchedule schedule = (ExaminerSchedule) session.getAttribute(ExaminerFilter.ATTR_EXAMINER_SCHEDULE);
            if (schedule != null && schedule.getExamAreaId() != null && schedule.getExamAreaId() > 0) {
                preferredAreaId = schedule.getExamAreaId();
            }
            Map<String, Object> data = viewDataService.getDevicesData(sessionId, search, preferredAreaId);
            for (Map.Entry<String, Object> mapEntry : data.entrySet()) {
                request.setAttribute(mapEntry.getKey(), mapEntry.getValue());
            }
            if (preferredAreaId != null && preferredAreaId > 0) {
                request.setAttribute("devicesTitle", "Máy tính - " + loadAreaName(preferredAreaId));
                request.setAttribute("devicesUnit", "máy");
            }
        }
        request.getRequestDispatcher("/views/examiner/devices.jsp").forward(request, response);
    }

    private String loadAreaName(int areaId) {
        model.ExamArea area = examAreaService.getById(areaId);
        return area != null && area.getAreaName() != null ? area.getAreaName() : "Phòng thi";
    }
}
