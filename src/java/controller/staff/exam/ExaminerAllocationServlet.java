package controller.staff.exam;
import dto.*;
import model.*;
import model.*;
import service.*;
import service.impl.*;
import dto.AssignmentDTO;
import service.AllocationService;
import service.impl.AllocationServiceImpl;
import model.ExamArea;
import model.ExamDevice;
import dto.SessionViewDTO;
import dto.UserRowDTO;
import enums.AuditAction;
import enums.AuditEntity;
import model.User;
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
import controller.staff.exam.BaseStaffExamServlet;

@WebServlet("/views/staff/exam/examiner-allocation")
public class ExaminerAllocationServlet extends BaseStaffExamServlet {
    private final AuditService AuditService = new AuditServiceImpl();
    private final AllocationService allocationService = new AllocationServiceImpl();
    private final SessionService sessionControlService = new SessionServiceImpl();
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
        List<SessionViewDTO> allSessions = allocationService.getAllSessions();
        request.setAttribute("allSessions", allSessions);
        int sessionId = readSessionId(request, session, sessionControlService);
        if (sessionId > 0) {
            session.setAttribute("selectedSessionId", sessionId);
        }
        SessionViewDTO currentSession = allocationService.getSessionById(sessionId);
        request.setAttribute("currentSession", currentSession);
        Map<Integer, Date> sessionDates = buildSessionDateMap(allSessions);
        Map<Integer, UserRowDTO> examinerMap = buildExaminerMap();
        String action = request.getParameter("action");
        if (action != null && currentSession != null) {
            handleAction(request, session, action, examinerMap);
        }
        if (currentSession != null) {
            List<SessionViewDTO> daySessions = allocationService.getSessionsByExamDate(currentSession.getExamDate());
            request.setAttribute("daySessions", daySessions);
            List<AssignmentDTO> dayAssignments = allocationService.getAssignmentsByExamDate(
                    currentSession.getExamDate(), sessionDates);
            request.setAttribute("dayAssignments", dayAssignments);
            List<AssignmentDTO> sessionAssignments = allocationService.getAssignmentsBySessionId(sessionId);
            request.setAttribute("sessionAssignments", sessionAssignments);
            Set<Integer> busyIds = allocationService.getBusyExaminerIds(
                    currentSession.getExamDate(), sessionDates);
            List<UserRowDTO> allExaminers = allocationService.getActiveExaminers();
            List<UserRowDTO> availableExaminers = new ArrayList<>();
            List<UserRowDTO> busyExaminers = new ArrayList<>();
            for (UserRowDTO ex : allExaminers) {
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
                devicesByArea.put(area.getExamAreaId(), allocationService.getDevicesByAreaId(area.getExamAreaId()));
            }
            request.setAttribute("devicesByArea", devicesByArea);
            Map<Integer, List<ExamArea>> areasBySession = new HashMap<>();
            for (SessionViewDTO ds : daySessions) {
                areasBySession.put(ds.getId(), allocationService.getAreasBySessionId(ds.getId()));
            }
            request.setAttribute("areasBySession", areasBySession);
        }
        request.getRequestDispatcher("/views/staff/exam/examiner-allocation.jsp").forward(request, response);
    }
    private void handleAction(HttpServletRequest request, HttpSession session, String action,
            Map<Integer, UserRowDTO> examinerMap) {
        try {
            if ("assign".equals(action)) {
                int targetSessionId = Integer.parseInt(request.getParameter("targetSessionId"));
                int areaId = Integer.parseInt(request.getParameter("areaId"));
                int examinerUserId = Integer.parseInt(request.getParameter("examinerUserId"));
                SessionViewDTO targetSession = allocationService.getSessionById(targetSessionId);
                ExamArea area = allocationService.getAreaById(areaId);
                UserRowDTO examiner = examinerMap.get(examinerUserId);
                if (targetSession == null || area == null || examiner == null) {
                    request.setAttribute("errorMsg", "Du lieu phan cong khong hop le.");
                    return;
                }
                if (!allocationService.isAreaInSession(targetSessionId, areaId)) {
                    request.setAttribute("errorMsg", "Phong thi khong thuoc ca thi da chon (Session_ExamArea).");
                    return;
                }
                AssignmentDTO slot = new AssignmentDTO();
                slot.setExamSessionId(targetSessionId);
                slot.setAreaId(areaId);
                slot.setExamSection(targetSession.getExamSection());
                slot.setExaminerUserId(examinerUserId);
                slot.setAssignedBy(getCurrentStaffUserId(session));
                slot.setAreaName(area.getAreaName());
                slot.setAreaType(area.getAreaType());
                slot.setExamTypeName(targetSession.getExamTypeName());
                slot.setMorningSession(targetSession.isMorningSession());
                slot.setExaminerName(getExaminerDisplayName(examiner));
                slot.setExaminerUsername(examiner.getUsername());
                boolean ok = allocationService.assignExaminer(slot);
                if (ok) {
                    request.setAttribute("alertMsg", "Da phan cong giam khao vao phong " + area.getAreaName() + ".");
                    addAuditLog(session, AuditAction.CREATE, AuditEntity.EXAMINER_ASSIGNMENT,
                            "Phan cong giam khao userId=" + examinerUserId
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
                    addAuditLog(session, AuditAction.DELETE, AuditEntity.EXAMINER_ASSIGNMENT,
                            "Go phan cong slot=" + slotKey);
                } else {
                    request.setAttribute("errorMsg", "Go phan cong that bai.");
                }
            }
        } catch (NumberFormatException e) {
            request.setAttribute("errorMsg", "Du lieu khong hop le.");
        }
    }
    private Map<Integer, Date> buildSessionDateMap(List<SessionViewDTO> sessions) {
        Map<Integer, Date> map = new HashMap<>();
        for (SessionViewDTO s : sessions) {
            map.put(s.getId(), s.getExamDate());
        }
        return map;
    }
    private Map<Integer, UserRowDTO> buildExaminerMap() {
        Map<Integer, UserRowDTO> map = new HashMap<>();
        for (UserRowDTO u : allocationService.getActiveExaminers()) {
            map.put(u.getId(), u);
        }
        return map;
    }
    private String getExaminerDisplayName(UserRowDTO examiner) {
        if (examiner.getProfile() != null && examiner.getProfile().getFullName() != null
                && !examiner.getProfile().getFullName().isBlank()) {
            return examiner.getProfile().getFullName();
        }
        return examiner.getUsername();
    }
    private int getCurrentStaffUserId(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return (user != null && user.getUserId() > 0) ? user.getUserId() : 3;
    }
    private void addAuditLog(HttpSession session, AuditAction action, AuditEntity entity, String details) {
        AuditService.logAction(((User) session.getAttribute("user")).getUserId(), action, entity, details);
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
