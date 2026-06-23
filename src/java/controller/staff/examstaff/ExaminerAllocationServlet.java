package controller.staff.examstaff;

import dto.AssignmentDTO;
import dto.SessionViewDTO;
import dto.UserRowDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.ExamArea;
import model.ExamDevice;
import model.User;
import service.AllocationService;
import service.SessionService;
import service.impl.AllocationServiceImpl;
import service.impl.SessionServiceImpl;

import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Examiner allocation (Phân bổ giám khảo). Ports the branch's ExaminerAllocationServlet.
// All data access goes through the Service layer; assignment state is persisted by
// AllocationService (the branch's in-memory ExaminerAssignmentStore/ExaminerSlot helpers
// are replaced by AssignmentDTO + AllocationService calls).
@WebServlet("/staff/examstaff/examiner-allocation")
public class ExaminerAllocationServlet extends HttpServlet {

    // Controller talks to the service layer only. No DAO or DB access here.
    private final AllocationService allocationService = new AllocationServiceImpl();
    private final SessionService sessionService = new SessionServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        request.removeAttribute("errorMsg");
        request.removeAttribute("alertMsg");

        // Surface any flash messages left by the session-control flow.
        String sessionControlMsg = (String) session.getAttribute("sessionControlMsg");
        String sessionControlError = (String) session.getAttribute("sessionControlError");
        if (sessionControlMsg != null) {
            request.setAttribute("alertMsg", sessionControlMsg);
            session.removeAttribute("sessionControlMsg");
        }
        if (sessionControlError != null) {
            request.setAttribute("errorMsg", sessionControlError);
            session.removeAttribute("sessionControlError");
        }

        // 0. Load all sessions for the session dropdown.
        List<SessionViewDTO> allSessions = sessionService.getAllSessions();
        request.setAttribute("allSessions", allSessions);

        // 1. Resolve the selected session id (request param wins, then session).
        String sessIdParam = request.getParameter("sessionId");
        int sessionId = 2; // Default session, matching the branch default.
        if (sessIdParam != null && !sessIdParam.isEmpty()) {
            try {
                sessionId = Integer.parseInt(sessIdParam);
            } catch (NumberFormatException e) {
                // Keep the default when the param is not a valid number.
            }
        } else if (session.getAttribute("selectedSessionId") != null) {
            sessionId = (Integer) session.getAttribute("selectedSessionId");
        }
        session.setAttribute("selectedSessionId", sessionId);

        // Current session details for the header.
        SessionViewDTO currentSession = sessionService.getSessionById(sessionId);
        request.setAttribute("currentSession", currentSession);

        // Map of session id -> exam date (used to resolve busy examiners by day).
        Map<Integer, Date> sessionDates = buildSessionDateMap(allSessions);

        // Map of examiner id -> examiner record (used to validate an assignment).
        List<UserRowDTO> allExaminers = allocationService.getActiveExaminers();
        Map<Integer, UserRowDTO> examinerMap = buildExaminerMap(allExaminers);
        request.setAttribute("allExaminers", allExaminers);

        // 2. Handle assignment actions (assign / remove).
        String action = request.getParameter("action");
        if (action != null && currentSession != null) {
            handleAction(request, session, action, examinerMap);
        }

        if (currentSession != null) {
            Date examDate = currentSession.getExamDate();

            // Sessions on the same exam date (for the day-level view).
            List<SessionViewDTO> daySessions = allocationService.getSessionsByExamDate(examDate);
            request.setAttribute("daySessions", daySessions);

            // Assignments for the whole day and for just the selected session.
            List<AssignmentDTO> dayAssignments =
                    allocationService.getAssignmentsByExamDate(examDate, sessionDates);
            request.setAttribute("dayAssignments", dayAssignments);

            List<AssignmentDTO> sessionAssignments =
                    allocationService.getAssignmentsBySessionId(sessionId);
            request.setAttribute("sessionAssignments", sessionAssignments);

            // Split examiners into available / busy for the selected day.
            Set<Integer> busyIds = allocationService.getBusyExaminerIds(examDate, sessionDates);
            List<UserRowDTO> availableExaminers = new ArrayList<>();
            List<UserRowDTO> busyExaminers = new ArrayList<>();
            for (UserRowDTO ex : allExaminers) {
                if (busyIds.contains(ex.getUserId())) {
                    busyExaminers.add(ex);
                } else {
                    availableExaminers.add(ex);
                }
            }
            request.setAttribute("availableExaminers", availableExaminers);
            request.setAttribute("busyExaminers", busyExaminers);

            // Areas (rooms) belonging to the selected session.
            List<ExamArea> sessionAreas = allocationService.getAreasBySessionId(sessionId);
            request.setAttribute("sessionAreas", sessionAreas);

            // Devices grouped by area (branch's devicesByArea map).
            Map<Integer, List<ExamDevice>> devicesByArea = new HashMap<>();
            for (ExamArea area : sessionAreas) {
                devicesByArea.put(area.getExamAreaId(),
                        allocationService.getDevicesByAreaId(area.getExamAreaId()));
            }
            request.setAttribute("devicesByArea", devicesByArea);

            // Areas grouped by each day session (branch's areasBySession map).
            Map<Integer, List<ExamArea>> areasBySession = new HashMap<>();
            if (daySessions != null) {
                for (SessionViewDTO ds : daySessions) {
                    areasBySession.put(ds.getId(),
                            allocationService.getAreasBySessionId(ds.getId()));
                }
            }
            request.setAttribute("areasBySession", areasBySession);
        }

        request.getRequestDispatcher("/views/staff/examstaff/examiner-allocation.jsp").forward(request, response);
    }

    private void handleAction(HttpServletRequest request, HttpSession session, String action,
            Map<Integer, UserRowDTO> examinerMap) {

        try {
            if ("assign".equals(action)) {
                int targetSessionId = Integer.parseInt(request.getParameter("targetSessionId"));
                int areaId = Integer.parseInt(request.getParameter("areaId"));
                int examinerUserId = Integer.parseInt(request.getParameter("examinerUserId"));

                SessionViewDTO targetSession = sessionService.getSessionById(targetSessionId);
                ExamArea area = allocationService.getAreaById(areaId);
                UserRowDTO examiner = examinerMap.get(examinerUserId);
                if (targetSession == null || area == null || examiner == null) {
                    request.setAttribute("errorMsg", "Dữ liệu phân công không hợp lệ.");
                    return;
                }
                if (!allocationService.isAreaInSession(targetSessionId, areaId)) {
                    request.setAttribute("errorMsg",
                            "Phòng thi không thuộc ca thi đã chọn (Session_ExamArea).");
                    return;
                }

                // Only the foreign keys are persisted; display fields are derived
                // when assignments are read back via AllocationService.
                AssignmentDTO slot = new AssignmentDTO();
                slot.setExamSessionId(targetSessionId);
                slot.setAreaId(areaId);
                slot.setExaminerUserId(examinerUserId);
                slot.setAssignedBy(resolveStaffId(session));

                boolean ok = allocationService.assignExaminer(slot);
                if (ok) {
                    request.setAttribute("alertMsg",
                            "Đã phân công giám khảo vào phòng " + area.getAreaName() + ".");
                } else {
                    request.setAttribute("errorMsg",
                            "Giám khảo đã được phân công ca này. Gỡ phân công cũ trước khi đổi phòng.");
                }

            } else if ("remove".equals(action)) {
                String slotKey = request.getParameter("slotKey");
                if (slotKey == null || slotKey.isEmpty()) {
                    request.setAttribute("errorMsg", "Không xác định được phân công cần gỡ.");
                    return;
                }
                boolean ok = allocationService.removeAssignment(slotKey);
                if (ok) {
                    request.setAttribute("alertMsg", "Đã gỡ phân công giám khảo.");
                } else {
                    request.setAttribute("errorMsg", "Gỡ phân công thất bại.");
                }
            }
        } catch (NumberFormatException e) {
            request.setAttribute("errorMsg", "Dữ liệu không hợp lệ.");
        }
    }

    private Map<Integer, Date> buildSessionDateMap(List<SessionViewDTO> sessions) {
        Map<Integer, Date> map = new HashMap<>();
        if (sessions == null) {
            return map;
        }
        for (SessionViewDTO s : sessions) {
            map.put(s.getId(), s.getExamDate());
        }
        return map;
    }

    private Map<Integer, UserRowDTO> buildExaminerMap(List<UserRowDTO> examiners) {
        Map<Integer, UserRowDTO> map = new HashMap<>();
        if (examiners == null) {
            return map;
        }
        for (UserRowDTO u : examiners) {
            map.put(u.getUserId(), u);
        }
        return map;
    }

    // The logged-in staff id, used as the "assigned by" of an assignment.
    private int resolveStaffId(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return (user != null && user.getUserId() > 0) ? user.getUserId() : 3;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
