package examiner.controller;

import auth.dto.UserDTO;
import shared.Attributes;
import examiner.filter.ExaminerFilter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServlet;
import examiner.service.ExamViewService;
import examiner.service.impl.ExamViewServiceImpl;
import examiner.service.CallService;
import examiner.service.impl.CallServiceImpl;
import shared.model.ExaminerSchedule;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@WebServlet("/examiner/devices")
public class ExaminerDevicesServlet extends HttpServlet {
    protected final ExamViewService viewDataService = new ExamViewServiceImpl();
    protected final CallService callService = new CallServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        renderPage(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        Integer activeExamId = (Integer) session.getAttribute(ExaminerFilter.ATTR_ACTIVE_EXAM_ID);
        String action = request.getParameter("action");
        String ctx = request.getContextPath();

        if (activeExamId != null && activeExamId > 0
                && action != null
                && ("maintenance".equals(action) || "operational".equals(action))) {
            int deviceId;
            try {
                deviceId = Integer.parseInt(request.getParameter("deviceId"));
            } catch (Exception e) {
                response.sendRedirect(ctx + "/examiner/devices?error=invalidDevice");
                return;
            }

            UserDTO user = (UserDTO) session.getAttribute(Attributes.Session.USER);
            Integer userId = user != null ? user.getUserId() : null;
            boolean updated;
            if ("operational".equals(action)) {
                updated = callService.setDeviceAvailable(deviceId, userId).isSuccess();
            } else {
                updated = callService.setDeviceMaintenance(deviceId, userId).isSuccess();
            }
            if (!updated) {
                response.sendRedirect(ctx + "/examiner/devices?error=updateFailed");
                return;
            }
            response.sendRedirect(ctx + "/examiner/devices");
            return;
        }

        renderPage(request, response);
    }

    private void renderPage(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        Integer activeExamId = (Integer) session.getAttribute(ExaminerFilter.ATTR_ACTIVE_EXAM_ID);
        String search = request.getParameter("q");
        request.setAttribute(Attributes.Request.PAGE_URL, request.getContextPath() + "/examiner/devices");
        request.setAttribute(Attributes.Request.SEARCH_QUERY, search != null ? search : "");

        if (activeExamId != null && activeExamId > 0) {
            Integer preferredAreaId = null;
            Object scheduleObj = session.getAttribute(ExaminerFilter.ATTR_EXAMINER_SCHEDULE);
            if (scheduleObj instanceof ExaminerSchedule) {
                preferredAreaId = ((ExaminerSchedule) scheduleObj).getExamAreaId();
            }
            Map<String, Object> data = viewDataService.getDevicesData(activeExamId, search, preferredAreaId);
            if (data != null) {
                for (Map.Entry<String, Object> mapEntry : data.entrySet()) {
                    request.setAttribute(mapEntry.getKey(), mapEntry.getValue());
                }
            }
        } else {
            request.setAttribute("devices", Collections.emptyList());
        }
        request.getRequestDispatcher("/views/examiner/devices.jsp").forward(request, response);
    }
}
