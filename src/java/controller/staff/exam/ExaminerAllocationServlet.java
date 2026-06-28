package controller.staff.exam;

import dto.examiner.ExaminerSlotDTO;
import service.ExaminerAllocationService;
import service.impl.ExaminerAllocationServiceImpl;
import model.exam.ExamArea;
import model.exam.ExamDevice;
import dto.exam.SessionDTO;
import dto.user.UserDTO;
import model.user.User;
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

@WebServlet("/views/staff/exam/examiner-allocation")
public class ExaminerAllocationServlet extends HttpServlet {
    private final service.AuditLogService auditLogService = new service.impl.AuditLogServiceImpl();
    private final ExaminerAllocationService allocationService = new ExaminerAllocationServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        request.removeAttribute("errorMsg");
        request.removeAttribute("alertMsg");
        
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

        List<SessionDTO> allSessions = allocationService.getAllSessions();
        request.setAttribute("allSessions", allSessions);

        String sessIdParam = request.getParameter("sessionId");
        int sessionId = 2;
        if (sessIdParam != null && !sessIdParam.isEmpty()) {
            try {
                sessionId = Integer.parseInt(sessIdParam);
            } catch (Exception ignored) {}
        } else if (session.getAttribute("selectedSessionId") != null) {
            sessionId = (Integer) session.getAttribute("selectedSessionId");
        }
        session.setAttribute("selectedSessionId", sessionId);

        SessionDTO currentSession = allocationService.getSessionById(sessionId);
        request.setAttribute("currentSession", currentSession);

        Map<Integer, Date> sessionDates = buildSessionDateMap(allSessions);
        Map<Integer, UserDTO> examinerMap = buildExaminerMap();

        String action = request.getParameter("action");
        if (action != null && currentSession != null) {
            handleAction(request, session, action, examinerMap);
        }

        if (currentSession != null) {
            List<SessionDTO> daySessions = allocationService.getSessionsByExamDate(currentSession.getExamDate());
            request.setAttribute("daySessions", daySessions);

            List<ExaminerSlotDTO> dayAssignments = allocationService.getAssignmentsByExamDate(
                    currentSession.getExamDate(), sessionDates);
            request.setAttribute("dayAssignments", dayAssignments);

            List<ExaminerSlotDTO> sessionAssignments = allocationService.getAssignmentsBySessionId(sessionId);
            request.setAttribute("sessionAssignments", sessionAssignments);

            Set<Integer> busyIds = allocationService.getBusyExaminerIds(
                    currentSession.getExamDate(), sessionDates);
            List<UserDTO> allExaminers = allocationService.getActiveExaminers();
            List<UserDTO> availableExaminers = new ArrayList<>();
            List<UserDTO> busyExaminers = new ArrayList<>();
            for (UserDTO ex : allExaminers) {
                if (busyIds.contains(ex.getId())) {
                    busyExaminers.add(ex);
                } else {
                    availableExaminers.add(ex);
                }
            }

            request.setAttribute("allExaminers", allExaminers);
            request.setAttribute("availableExaminers", availableExaminers);
            request.setAttribute("busyExaminers", busyExaminers);

            List<ExamArea> sessionAreas = allocationService.getAreasBySessionId(sessionId);
            request.setAttribute("sessionAreas", sessionAreas);

            Map<Integer, List<ExamDevice>> devicesByArea = new HashMap<>();
            for (ExamArea area : sessionAreas) {
                devicesByArea.put(area.getId(), allocationService.getDevicesByAreaId(area.getId()));
            }
            request.setAttribute("devicesByArea", devicesByArea);

            Map<Integer, List<ExamArea>> areasBySession = new HashMap<>();
            for (SessionDTO ds : daySessions) {
                areasBySession.put(ds.getId(), allocationService.getAreasBySessionId(ds.getId()));
            }
            request.setAttribute("areasBySession", areasBySession);
        }

        request.getRequestDispatcher("/views/staff/exam/examiner-allocation.jsp").forward(request, response);
    }

    private void handleAction(HttpServletRequest request, HttpSession session, String action,
            Map<Integer, UserDTO> examinerMap) {
        try {
            if ("assign".equals(action)) {
                int targetSessionId = Integer.parseInt(request.getParameter("targetSessionId"));
                int areaId = Integer.parseInt(request.getParameter("areaId"));
                int examinerUserId = Integer.parseInt(request.getParameter("examinerUserId"));

                SessionDTO targetSession = allocationService.getSessionById(targetSessionId);
                ExamArea area = allocationService.getAreaById(areaId);
                UserDTO examiner = examinerMap.get(examinerUserId);
                if (targetSession == null || area == null || examiner == null) {
                    request.setAttribute("errorMsg", "Du lieu phan cong khong hop le.");
                    return;
                }
                if (!allocationService.isAreaInSession(targetSessionId, areaId)) {
                    request.setAttribute("errorMsg", "Phong thi khong thuoc ca thi da chon (Session_ExamArea).");
                    return;
                }

                ExaminerSlotDTO slot = new ExaminerSlotDTO();
                slot.setExamSessionId(targetSessionId);
                slot.setAreaId(areaId);
                slot.setExamTypeId(targetSession.getExamTypeId());
                slot.setExaminerUserId(examinerUserId);
                slot.setAssignedBy(resolveStaffId(session));
                slot.setAreaName(area.getAreaName());
                slot.setAreaType(area.getAreaType());
                slot.setExamTypeName(targetSession.getExamTypeName());
                slot.setSessionName(targetSession.getSessionName());
                slot.setExaminerName(resolveExaminerName(examiner));
                slot.setExaminerUsername(examiner.getUsername());

                boolean ok = allocationService.assignExaminer(slot);
                if (ok) {
                    request.setAttribute("alertMsg", "Da phan cong giam khao vao phong " + area.getAreaName() + ".");
                    addAuditLog(session, "ASSIGN Examiner", "Phan cong giam khao userId=" + examinerUserId
                            + " ca " + targetSessionId + ", phong " + area.getAreaName());
                } else {
                    request.setAttribute("errorMsg", "Giam khao da duoc phan cong ca nay. Go phan cong cu truoc khi doi phong.");
                }
            } else if ("remove".equals(action)) {
                String slotKey = request.getParameter("slotKey");
                if (slotKey == null || slotKey.isEmpty()) {
                    request.setAttribute("errorMsg", "Khong xac dinh duoc phan cong can go.");
                    return;
                }
                boolean ok = allocationService.removeAssignment(slotKey);
                if (ok) {
                    request.setAttribute("alertMsg", "Da go phan cong giam khao.");
                    addAuditLog(session, "REMOVE Examiner", "Go phan cong slot=" + slotKey);
                } else {
                    request.setAttribute("errorMsg", "Go phan cong that bai.");
                }
            }
        } catch (NumberFormatException e) {
            request.setAttribute("errorMsg", "Du lieu khong hop le.");
        }
    }

    private Map<Integer, Date> buildSessionDateMap(List<SessionDTO> sessions) {
        Map<Integer, Date> map = new HashMap<>();
        for (SessionDTO s : sessions) {
            map.put(s.getId(), s.getExamDate());
        }
        return map;
    }

    private Map<Integer, UserDTO> buildExaminerMap() {
        Map<Integer, UserDTO> map = new HashMap<>();
        for (UserDTO u : allocationService.getActiveExaminers()) {
            map.put(u.getId(), u);
        }
        return map;
    }

    private String resolveExaminerName(UserDTO examiner) {
        if (examiner.getProfile() != null && examiner.getProfile().getFullName() != null
                && !examiner.getProfile().getFullName().isBlank()) {
            return examiner.getProfile().getFullName();
        }
        return examiner.getUsername();
    }

    private int resolveStaffId(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return (user != null && user.getUserId() > 0) ? user.getUserId() : 3;
    }

    private void addAuditLog(HttpSession session, String action, String details) {
        auditLogService.persist(session, action, details);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
