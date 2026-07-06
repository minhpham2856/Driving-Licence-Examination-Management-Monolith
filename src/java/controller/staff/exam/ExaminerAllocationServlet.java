package controller.staff.exam;

import dto.ExaminerSlotDTO;

import service.ExaminerAllocationService;

import service.impl.ExaminerAllocationServiceImpl;

import model.ExamArea;

import model.ExamDevice;

import model.User;

import dto.SessionDTO;

import dto.UserDTO;

import dao.ExamSessionDAO;

import dao.impl.ExamSessionDAOImpl;

import util.ExamAreaTypeResolver;

import jakarta.servlet.ServletException;

import jakarta.servlet.annotation.WebServlet;

import jakarta.servlet.http.HttpServlet;

import jakarta.servlet.http.HttpServletRequest;

import jakarta.servlet.http.HttpServletResponse;

import jakarta.servlet.http.HttpSession;

import java.io.IOException;

import java.util.ArrayList;

import java.util.HashMap;

import java.util.HashSet;

import java.util.List;

import java.util.Map;

import java.util.Set;

@WebServlet("/views/staff/examstaff/examiner-allocation")

public class ExaminerAllocationServlet extends HttpServlet {

    private final ExamSessionDAO sessionDAO = new ExamSessionDAOImpl();

    @Override

    // Xu ly yeu cau GET
    protected void doGet(HttpServletRequest request, HttpServletResponse response)

            throws ServletException, IOException {

        ExamStaffViewHelper.applyNoCacheHeaders(response);

        HttpSession session = request.getSession();

        ExaminerAllocationService allocationService = new ExaminerAllocationServiceImpl();

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

        ExamStaffViewHelper.consumeFlash(session, "sessionSelectMsg", request, "sessionSelectMsg");

        ExamStaffViewHelper.ExamStaffPageContext pageCtx = ExamStaffViewHelper.prepareExamStaffPage(

                request, session, sessionDAO, getServletContext().getRealPath("/"), false);

        List<SessionDTO> allSessions = pageCtx.getAllSessions();

        int examId = pageCtx.getExamId();

        int sessionId = pageCtx.getSessionId();

        SessionDTO pickedFromUrl = ExamStaffViewHelper.resolveSessionFromRequest(

                request, session, sessionDAO, allSessions);

        if (pickedFromUrl != null) {

            examId = pickedFromUrl.getExamId();

            sessionId = pickedFromUrl.getId();

        }

        SessionDTO currentSession = sessionId > 0 ? allocationService.getSessionById(sessionId) : null;

        if (currentSession == null && pickedFromUrl != null) {

            currentSession = pickedFromUrl;

        }

        if (currentSession == null && examId > 0) {

            currentSession = ExamStaffViewHelper.representativeSessionForExam(allSessions, examId, sessionDAO);

            if (currentSession != null) {

                sessionId = currentSession.getId();

            }

        }

        request.setAttribute("allSessions", allSessions);

        request.setAttribute("currentSession", currentSession);

        request.setAttribute("selectedExamId", examId);

        request.setAttribute("selectedSessionId", sessionId > 0 ? sessionId : null);

        Map<Integer, UserDTO> examinerMap = buildExaminerMap(allocationService);

        String action = request.getParameter("action");

        if (action != null && examId > 0) {
            // handle action

            handleAction(request, session, action, examinerMap, allocationService);

        }

            // populate allocation view
        if (examId > 0) {

            populateAllocationView(request, allocationService, allSessions, examId, sessionId);

        }

        request.getRequestDispatcher("/views/staff/examstaff/examiner-allocation.jsp").forward(request, response);
    // populate allocation view

    }

    private void populateAllocationView(HttpServletRequest request, ExaminerAllocationService allocationService,

            List<SessionDTO> allSessions, int examId, int sessionId) {

        List<SessionDTO> daySessions = ExamStaffViewHelper.sessionsForExam(allSessions, examId);

        request.setAttribute("daySessions", daySessions);

        request.setAttribute("examSessions", daySessions);

        List<ExaminerSlotDTO> dayAssignments = new ArrayList<>();

        Set<Integer> busyIds = new HashSet<>();

        for (SessionDTO ds : daySessions) {

            List<ExaminerSlotDTO> slots = allocationService.getAssignmentsBySessionId(ds.getId());

            dayAssignments.addAll(slots);

            for (ExaminerSlotDTO slot : slots) {

                if (slot.getExaminerUserId() > 0) {

                    busyIds.add(slot.getExaminerUserId());

                }

            }

        }

        request.setAttribute("dayAssignments", dayAssignments);

        request.setAttribute("examAssignments", dayAssignments);

        if (sessionId > 0) {

            request.setAttribute("sessionAssignments", allocationService.getAssignmentsBySessionId(sessionId));

        } else {

            request.setAttribute("sessionAssignments", List.of());

        }

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

        List<ExamArea> sessionAreas = sessionId > 0

                ? allocationService.getAvailableAreasForSession(sessionId)

                : List.of();

        request.setAttribute("sessionAreas", sessionAreas);

        Map<Integer, List<ExamDevice>> devicesByArea = new HashMap<>();

        for (ExamArea area : sessionAreas) {

            devicesByArea.put(area.getId(), allocationService.getDevicesByAreaId(area.getId()));

        }

        request.setAttribute("devicesByArea", devicesByArea);

        Map<String, List<ExamArea>> areasBySession = new HashMap<>();

        List<Map<String, Object>> areaAssignOptions = new ArrayList<>();

        for (SessionDTO ds : daySessions) {

            List<ExamArea> areas = allocationService.getAvailableAreasForSession(ds.getId());

            areasBySession.put(String.valueOf(ds.getId()), areas);

            for (ExamArea area : areas) {

                Map<String, Object> opt = new HashMap<>();

                opt.put("sessionId", ds.getId());

                opt.put("sessionName", ds.getSessionName());

                opt.put("areaId", area.getId());

                opt.put("areaName", area.getAreaName());

                opt.put("areaType", area.getAreaType());

                areaAssignOptions.add(opt);

            }

        }

        request.setAttribute("areasBySession", areasBySession);

        request.setAttribute("areaAssignOptions", areaAssignOptions);

    // handle action
        request.setAttribute("examStaffLoadedExamId", examId);

    }

    private void handleAction(HttpServletRequest request, HttpSession session, String action,

            Map<Integer, UserDTO> examinerMap, ExaminerAllocationService allocationService) {

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

                if (!ExamAreaTypeResolver.areaMatchesSession(area, targetSession)) {

                    request.setAttribute("errorMsg", "Phòng thi không đúng loại với ca/môn đã chọn.");

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

                    request.setAttribute("errorMsg", "Giám khảo đã được phân công ở ca/phòng khác. Gỡ phân công cũ trước khi gán ca mới.");

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
    // Tao examiner map

        }

    }

    private Map<Integer, UserDTO> buildExaminerMap(ExaminerAllocationService allocationService) {

        Map<Integer, UserDTO> map = new HashMap<>();

        for (UserDTO u : allocationService.getActiveExaminers()) {

            map.put(u.getId(), u);

    // Xac dinh examiner name
        }

        return map;

    }

    private String resolveExaminerName(UserDTO examiner) {

        if (examiner.getProfile() != null && examiner.getProfile().getFullName() != null

                && !examiner.getProfile().getFullName().isBlank()) {

            return examiner.getProfile().getFullName();
    // Xac dinh staff id

        }

        return examiner.getUsername();

    }

    // add audit log
    private int resolveStaffId(HttpSession session) {

        User user = (User) session.getAttribute("user");

        return (user != null && user.getUserId() > 0) ? user.getUserId() : 3;

    }
    // Xu ly yeu cau POST

    private void addAuditLog(HttpSession session, String action, String details) {

        // Xu ly yeu cau GET
        util.AuditLogHelper.persist(session, action, details);

    }

    @Override

    protected void doPost(HttpServletRequest request, HttpServletResponse response)

            throws ServletException, IOException {

        doGet(request, response);

    }

}
