package examiner.controller;

import auth.dto.UserDTO;
import shared.Attributes;
import examiner.filter.ExaminerFilter;
import shared.enums.SectionType;
import shared.model.ExaminerSchedule;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServlet;
import examiner.service.ExamViewService;
import examiner.service.impl.ExamViewServiceImpl;
import examiner.service.ActionService;
import examiner.service.impl.ActionServiceImpl;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@WebServlet("/examiner/devices")

// Device management screen: list exam vehicles/devices and toggle maintenance or operational status.
public class DevicesServlet extends HttpServlet {

    protected final ExamViewService viewService = new ExamViewServiceImpl();
    protected final ActionService actionService = new ActionServiceImpl();

    // Load device rows for the active exam, optionally filtered by search query and preferred area.
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        Integer activeExamId = (Integer) session.getAttribute(ExaminerFilter.ATTR_ACTIVE_EXAM_ID);
        String search = request.getParameter("q");

        if (activeExamId != null && activeExamId > 0) {
            // Echo search back to JSP for the filter input value.
            if (search != null && !search.isBlank()) {
                request.setAttribute("searchQuery", search.trim());
            }
            // Prefer devices in the examiner's assigned area when schedule provides examAreaId.
            Integer preferredAreaId = null;
            ExaminerSchedule schedule = (ExaminerSchedule) session.getAttribute(ExaminerFilter.ATTR_EXAMINER_SCHEDULE);
            if (schedule != null && schedule.getExamAreaId() != null && schedule.getExamAreaId() > 0) {
                preferredAreaId = schedule.getExamAreaId();
            }
            SectionType sectionType = ExaminerFilter.resolveSectionType(session);
            // Service returns a map of list rows and summary counts for devices.jsp.
            Map<String, Object> data = viewService.getDeviceViewByExam(activeExamId, search, preferredAreaId, sectionType);
            if (data != null) {
                for (Map.Entry<String, Object> mapEntry : data.entrySet()) {
                    request.setAttribute(mapEntry.getKey(), mapEntry.getValue());
                }
            }
        }
        request.getRequestDispatcher("/views/examiner/devices.jsp").forward(request, response);
    }

    // Set a device to maintenance or operational status and redirect back preserving the search query.
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        Integer activeExamId = (Integer) session.getAttribute(ExaminerFilter.ATTR_ACTIVE_EXAM_ID);
        if (activeExamId == null || activeExamId <= 0) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String action = request.getParameter("action");
        if (action == null || (!"maintenance".equals(action) && !"operational".equals(action))) {
            response.sendRedirect(buildDevicesUrl(request, "invalidAction"));
            return;
        }

        // deviceId identifies ExamDevice row to toggle availability.
        int deviceId;
        try {
            deviceId = Integer.parseInt(request.getParameter("deviceId"));
        } catch (NumberFormatException e) {
            response.sendRedirect(buildDevicesUrl(request, "invalidDevice"));
            return;
        }

        UserDTO user = (UserDTO) session.getAttribute(Attributes.Session.USER);
        Integer userId = user != null ? user.getUserId() : null;
        boolean updated;
        if ("operational".equals(action)) {
            updated = actionService.setDeviceAvailable(deviceId, userId).isSuccess();
        } else {
            updated = actionService.setDeviceMaintenance(deviceId, userId).isSuccess();
        }
        if (!updated) {
            response.sendRedirect(buildDevicesUrl(request, "updateFailed"));
            return;
        }
        response.sendRedirect(buildDevicesUrl(request, null));
    }

    // Build a devices-page redirect URL preserving the search query and optional error code.
    private String buildDevicesUrl(HttpServletRequest request, String error) {
        StringBuilder url = new StringBuilder(request.getContextPath()).append("/examiner/devices");
        String q = request.getParameter("q");
        boolean hasParam = false;
        if (q != null && !q.isBlank()) {
            url.append("?q=").append(URLEncoder.encode(q.trim(), StandardCharsets.UTF_8));
            hasParam = true;
        }
        if (error != null && !error.isBlank()) {
            url.append(hasParam ? "&" : "?").append("error=").append(URLEncoder.encode(error, StandardCharsets.UTF_8));
        }
        return url.toString();
    }
}
