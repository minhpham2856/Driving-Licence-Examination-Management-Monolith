package Controllers.Staff.ExamStaff;

import DAOs.ExamAreaDAO;
import DAOs.ExamDeviceDAO;
import DAOs.ExamSessionDAO;
import DAOs.ExaminerAssignmentDAO;
import DAOs.Impl.ExamAreaDAOImpl;
import DAOs.Impl.ExamDeviceDAOImpl;
import DAOs.Impl.ExamSessionDAOImpl;
import DAOs.Impl.ExaminerAssignmentDAOImpl;
import Models.ExamArea;
import Models.ExamDevice;
import DTOs.SessionDTO;
import DTOs.UserDTO;
import Models.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

 // Servlet for the staff-level examiner allocation page.
@WebServlet("/views/staff/examstaff/examiner-allocation")
public class ExaminerAllocationServlet extends HttpServlet {

    // DAO for querying exam sessions (list, get by ID, get by date)
    private final ExamSessionDAO sessionDAO = new ExamSessionDAOImpl();
    // DAO for querying exam areas and checking area-session membership
    private final ExamAreaDAO areaDAO = new ExamAreaDAOImpl();
    // DAO for querying exam devices by area (for the allocation UI device panel)
    private final ExamDeviceDAO deviceDAO = new ExamDeviceDAOImpl();
    // DAO for managing examiner assignments and querying active examiners
    private final ExaminerAssignmentDAO assignmentDAO = new ExaminerAssignmentDAOImpl();

    // Handles GET requests: loads all allocation data and forwards to the JSP
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Get the current HTTP session for reading/writing session-scoped attributes
        HttpSession session = request.getSession();
        // Clear any previous error/alert messages from the request scope
        request.removeAttribute("errorMsg");
        request.removeAttribute("alertMsg");
        // Check for flash messages set by the session control servlet (via redirect)
        String sessionControlMsg = (String) session.getAttribute("sessionControlMsg");
        String sessionControlError = (String) session.getAttribute("sessionControlError");
        // Transfer flash success message to request scope and clear from session
        if (sessionControlMsg != null) {
            request.setAttribute("alertMsg", sessionControlMsg);
            session.removeAttribute("sessionControlMsg");
        }
        // Transfer flash error message to request scope and clear from session
        if (sessionControlError != null) {
            request.setAttribute("errorMsg", sessionControlError);
            session.removeAttribute("sessionControlError");
        }

        // Load all exam sessions for the session dropdown selector
        List<SessionDTO> allSessions = sessionDAO.getAllSessions();
        request.setAttribute("allSessions", allSessions);

        // Determine which session is currently selected (from URL param, session cache, or default)
        String sessIdParam = request.getParameter("sessionId");
        // Default session ID used when no parameter or cache is available
        int sessionId = 2;
        if (sessIdParam != null && !sessIdParam.isEmpty()) {
            // Parse the session ID from the URL query parameter
            try {
                sessionId = Integer.parseInt(sessIdParam);
            } catch (Exception ignored) {}
        } else if (session.getAttribute("selectedSessionId") != null) {
            // Fall back to the previously selected session stored in the HTTP session
            sessionId = (Integer) session.getAttribute("selectedSessionId");
        }
        // Cache the selected session ID in the HTTP session for subsequent requests
        session.setAttribute("selectedSessionId", sessionId);

        // Load the full details of the currently selected session
        SessionDTO currentSession = sessionDAO.getById(sessionId);
        request.setAttribute("currentSession", currentSession);

        // Build helper maps: session-to-date mapping and examiner ID-to-DTO lookup
        Map<Integer, Date> sessionDates = buildSessionDateMap(allSessions);
        Map<Integer, UserDTO> examinerMap = buildExaminerMap();

        // Handle assign/remove actions if the "action" parameter is present
        String action = request.getParameter("action");
        if (action != null && currentSession != null) {
            handleAction(request, session, action, examinerMap);
        }

        // Load and attach all view data for the allocation page
        if (currentSession != null) {
            // Load all sessions on the same exam date (for the day-view panel)
            List<SessionDTO> daySessions = sessionDAO.getSessionsByExamDate(currentSession.getExamDate());
            request.setAttribute("daySessions", daySessions);

            // Load all examiner assignments for the current exam date
            List<ExaminerSlot> dayAssignments = ExaminerAssignmentStore.getByExamDate(
                    session, currentSession.getExamDate(), sessionDates);
            request.setAttribute("dayAssignments", dayAssignments);

            // Load all examiner assignments for the selected session specifically
            List<ExaminerSlot> sessionAssignments = ExaminerAssignmentStore.getBySessionId(session, sessionId);
            request.setAttribute("sessionAssignments", sessionAssignments);

            // Determine which examiners are busy (already assigned) on this exam date
            Set<Integer> busyIds = ExaminerAssignmentStore.getBusyExaminerIds(
                    session, currentSession.getExamDate(), sessionDates);
            // Load the full list of active examiners from the database
            List<UserDTO> allExaminers = assignmentDAO.getActiveExaminers();
            // Partition examiners into available and busy lists for the UI
            List<UserDTO> availableExaminers = new ArrayList<>();
            List<UserDTO> busyExaminers = new ArrayList<>();
            for (UserDTO ex : allExaminers) {
                // Check if this examiner is already assigned on the exam date
                if (busyIds.contains(ex.getId())) {
                    busyExaminers.add(ex);
                } else {
                    availableExaminers.add(ex);
                }
            }

            // Set all examiner lists as request attributes for the JSP
            request.setAttribute("allExaminers", allExaminers);
            request.setAttribute("availableExaminers", availableExaminers);
            request.setAttribute("busyExaminers", busyExaminers);

            // Load all exam areas linked to the selected session
            List<ExamArea> sessionAreas = areaDAO.getBySessionId(sessionId);
            request.setAttribute("sessionAreas", sessionAreas);

            // Build a map of area ID to its devices for the device panel
            Map<Integer, List<ExamDevice>> devicesByArea = new HashMap<>();
            for (ExamArea area : sessionAreas) {
                devicesByArea.put(area.getId(), deviceDAO.getByAreaId(area.getId()));
            }
            request.setAttribute("devicesByArea", devicesByArea);

            // Build a map of session ID to its areas for the day-view assignment grid
            Map<Integer, List<ExamArea>> areasBySession = new HashMap<>();
            for (SessionDTO ds : daySessions) {
                areasBySession.put(ds.getId(), areaDAO.getBySessionId(ds.getId()));
            }
            request.setAttribute("areasBySession", areasBySession);
        }

        // Forward to the JSP page for rendering the allocation UI
        request.getRequestDispatcher("/views/staff/examstaff/examiner-allocation.jsp").forward(request, response);
    }

    // Handles the "assign" and "remove" actions for examiner allocation.
    private void handleAction(HttpServletRequest request, HttpSession session, String action,
            Map<Integer, UserDTO> examinerMap) {
        try {
            // --- Handle the "assign" action: assign an examiner to a session-area slot ---
            if ("assign".equals(action)) {
                // Parse the target session, area, and examiner IDs from the form parameters
                int targetSessionId = Integer.parseInt(request.getParameter("targetSessionId"));
                int areaId = Integer.parseInt(request.getParameter("areaId"));
                int examinerUserId = Integer.parseInt(request.getParameter("examinerUserId"));

                // Look up the full objects for validation
                SessionDTO targetSession = sessionDAO.getById(targetSessionId);
                ExamArea area = areaDAO.getById(areaId);
                UserDTO examiner = examinerMap.get(examinerUserId);
                // Validate that all referenced entities exist in the database
                if (targetSession == null || area == null || examiner == null) {
                    request.setAttribute("errorMsg", "Du lieu phan cong khong hop le.");
                    return;
                }
                // Validate that the area actually belongs to the target session
                if (!areaDAO.isAreaInSession(targetSessionId, areaId)) {
                    request.setAttribute("errorMsg", "Phong thi khong thuoc ca thi da chon (Session_ExamArea).");
                    return;
                }

                // Build the slot object with all required fields for the assignment
                ExaminerSlot slot = new ExaminerSlot();
                slot.setExamSessionId(targetSessionId);
                slot.setAreaId(areaId);
                slot.setExamTypeId(targetSession.getExamTypeId());
                slot.setExaminerUserId(examinerUserId);
                // Resolve the staff user ID who is performing this assignment
                slot.setAssignedBy(resolveStaffId(session));
                slot.setAreaName(area.getAreaName());
                slot.setAreaType(area.getAreaType());
                slot.setExamTypeName(targetSession.getExamTypeName());
                slot.setSessionName(targetSession.getSessionName());
                // Resolve the examiner's display name from their profile
                slot.setExaminerName(resolveExaminerName(examiner));
                slot.setExaminerUsername(examiner.getUsername());

                // Attempt the assignment via the store (delegates to the DAO)
                boolean ok = ExaminerAssignmentStore.assign(session, slot);
                if (ok) {
                    // Show success message with the assigned area name
                    request.setAttribute("alertMsg", "Da phan cong giam khao vao phong " + area.getAreaName() + ".");
                    // Write an audit log entry for the assignment action
                    addAuditLog(session, "ASSIGN Examiner", "Phan cong giam khao userId=" + examinerUserId
                            + " ca " + targetSessionId + ", phong " + area.getAreaName());
                } else {
                    // Duplicate assignment — examiner is already assigned to this session
                    request.setAttribute("errorMsg",
                            "Giam khao da duoc phan cong ca nay. Go phan cong cu truoc khi doi phong.");
                }
            // --- Handle the "remove" action: unassign an examiner from a slot ---
            } else if ("remove".equals(action)) {
                // Read the slot key from the form parameter
                String slotKey = request.getParameter("slotKey");
                // Validate that a slot key was provided
                if (slotKey == null || slotKey.isEmpty()) {
                    request.setAttribute("errorMsg", "Khong xac dinh duoc phan cong can go.");
                    return;
                }
                // Attempt the removal via the store (delegates to the DAO)
                boolean ok = ExaminerAssignmentStore.remove(session, slotKey);
                if (ok) {
                    request.setAttribute("alertMsg", "Da go phan cong giam khao.");
                    // Write an audit log entry for the removal action
                    addAuditLog(session, "REMOVE Examiner", "Go phan cong slot=" + slotKey);
                } else {
                    request.setAttribute("errorMsg", "Go phan cong that bai.");
                }
            }
        } catch (NumberFormatException e) {
            // Catch invalid numeric parameter values (e.g. non-integer session ID)
            request.setAttribute("errorMsg", "Du lieu khong hop le.");
        }
    }

    // Builds a map of session ID to exam date from the list of all sessions.
    private Map<Integer, Date> buildSessionDateMap(List<SessionDTO> sessions) {
        Map<Integer, Date> map = new HashMap<>();
        for (SessionDTO s : sessions) {
            map.put(s.getSessionId(), s.getExamDate());
        }
        return map;
    }

    // Builds a lookup map of examiner user ID to UserDTO.
    private Map<Integer, UserDTO> buildExaminerMap() {
        Map<Integer, UserDTO> map = new HashMap<>();
        for (UserDTO u : assignmentDAO.getActiveExaminers()) {
            map.put(u.getId(), u);
        }
        return map;
    }

    // Resolves the display name for an examiner from their profile.
    private String resolveExaminerName(UserDTO examiner) {
        // Prefer the profile full name if it exists and is non-blank
        if (examiner.getProfile() != null && examiner.getProfile().getFullName() != null
                && !examiner.getProfile().getFullName().isBlank()) {
            return examiner.getProfile().getFullName();
        }
        // Fall back to the login username if no profile name is available
        return examiner.getUsername();
    }

    // Resolves the staff user ID from the current HTTP session.
    private int resolveStaffId(HttpSession session) {
        // Read the user object stored at login time
        User user = (User) session.getAttribute("user");
        // Return the user ID if valid, otherwise default to system user ID 3
        return (user != null && user.getUserId() > 0) ? user.getUserId() : 3;
    }

    // Writes an audit log entry for the allocation action.
    private void addAuditLog(HttpSession session, String action, String details) {
        Utils.AuditLogHelper.persist(session, action, details);
    }

    // POST requests are handled identically to GET (form submissions use GET params)
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
