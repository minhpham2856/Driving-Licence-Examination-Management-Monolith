package Controllers.Staff.ExamStaff;



import DAO.ExamAreaDAO;

import DAO.ExamDeviceDAO;

import DAO.ExamSessionDAO;

import DAO.ExaminerAssignmentDAO;

import DAO.Impl.ExamAreaDAOImpl;

import DAO.Impl.ExamDeviceDAOImpl;

import DAO.Impl.ExamSessionDAOImpl;

import DAO.Impl.ExaminerAssignmentDAOImpl;

import Models.ExamArea;

import Models.ExamDevice;

import Models.ExamSession;

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



@WebServlet("/views/staff/examstaff/examiner-allocation")

public class ExaminerAllocationServlet extends HttpServlet {



    private final ExamSessionDAO sessionDAO = new ExamSessionDAOImpl();

    private final ExamAreaDAO areaDAO = new ExamAreaDAOImpl();

    private final ExamDeviceDAO deviceDAO = new ExamDeviceDAOImpl();

    private final ExaminerAssignmentDAO assignmentDAO = new ExaminerAssignmentDAOImpl();



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



        List<ExamSession> allSessions = sessionDAO.getAllSessions();

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



        ExamSession currentSession = sessionDAO.getById(sessionId);

        request.setAttribute("currentSession", currentSession);



        Map<Integer, Date> sessionDates = buildSessionDateMap(allSessions);

        Map<Integer, User> examinerMap = buildExaminerMap();



        String action = request.getParameter("action");

        if (action != null && currentSession != null) {

            handleAction(request, session, action, examinerMap);

        }



        if (currentSession != null) {

            List<ExamSession> daySessions = sessionDAO.getSessionsByExamDate(currentSession.getExamDate());

            request.setAttribute("daySessions", daySessions);



            List<ExaminerSlot> dayAssignments = ExaminerAssignmentStore.getByExamDate(

                    session, currentSession.getExamDate(), sessionDates);

            request.setAttribute("dayAssignments", dayAssignments);



            List<ExaminerSlot> sessionAssignments = ExaminerAssignmentStore.getBySessionId(session, sessionId);

            request.setAttribute("sessionAssignments", sessionAssignments);



            Set<Integer> busyIds = ExaminerAssignmentStore.getBusyExaminerIds(

                    session, currentSession.getExamDate(), sessionDates);

            List<User> allExaminers = assignmentDAO.getActiveExaminers();

            List<User> availableExaminers = new ArrayList<>();

            List<User> busyExaminers = new ArrayList<>();

            for (User ex : allExaminers) {

                if (busyIds.contains(ex.getId())) {

                    busyExaminers.add(ex);

                } else {

                    availableExaminers.add(ex);

                }

            }

            request.setAttribute("allExaminers", allExaminers);

            request.setAttribute("availableExaminers", availableExaminers);

            request.setAttribute("busyExaminers", busyExaminers);



            List<ExamArea> sessionAreas = areaDAO.getAreasBySessionId(sessionId);

            request.setAttribute("sessionAreas", sessionAreas);



            Map<Integer, List<ExamDevice>> devicesByArea = new HashMap<>();

            for (ExamArea area : sessionAreas) {

                devicesByArea.put(area.getId(), deviceDAO.getDevicesByAreaId(area.getId()));

            }

            request.setAttribute("devicesByArea", devicesByArea);



            Map<Integer, List<ExamArea>> areasBySession = new HashMap<>();

            for (ExamSession ds : daySessions) {

                areasBySession.put(ds.getId(), areaDAO.getAreasBySessionId(ds.getId()));

            }

            request.setAttribute("areasBySession", areasBySession);

        }



        request.getRequestDispatcher("/views/staff/examstaff/examiner-allocation.jsp").forward(request, response);

    }



    private void handleAction(HttpServletRequest request, HttpSession session, String action,

            Map<Integer, User> examinerMap) {

        try {

            if ("assign".equals(action)) {

                int targetSessionId = Integer.parseInt(request.getParameter("targetSessionId"));

                int areaId = Integer.parseInt(request.getParameter("areaId"));

                int examinerUserId = Integer.parseInt(request.getParameter("examinerUserId"));



                ExamSession targetSession = sessionDAO.getById(targetSessionId);

                ExamArea area = areaDAO.getById(areaId);

                User examiner = examinerMap.get(examinerUserId);

                if (targetSession == null || area == null || examiner == null) {

                    request.setAttribute("errorMsg", "Dữ liệu phân công không hợp lệ.");

                    return;

                }

                if (!areaDAO.isAreaInSession(targetSessionId, areaId)) {

                    request.setAttribute("errorMsg", "Phòng thi không thuộc ca thi đã chọn (Session_ExamArea).");

                    return;

                }



                ExaminerSlot slot = new ExaminerSlot();

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



                boolean ok = ExaminerAssignmentStore.assign(session, slot);

                if (ok) {

                    request.setAttribute("alertMsg", "Đã phân công giám khảo vào phòng " + area.getAreaName() + ".");

                    addAuditLog(session, "ASSIGN Examiner", "Phân công giám khảo userId=" + examinerUserId

                            + " ca " + targetSessionId + ", phòng " + area.getAreaName());

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

                boolean ok = ExaminerAssignmentStore.remove(session, slotKey);

                if (ok) {

                    request.setAttribute("alertMsg", "Đã gỡ phân công giám khảo.");

                    addAuditLog(session, "REMOVE Examiner", "Gỡ phân công slot=" + slotKey);

                } else {

                    request.setAttribute("errorMsg", "Gỡ phân công thất bại.");

                }

            }

        } catch (NumberFormatException e) {

            request.setAttribute("errorMsg", "Dữ liệu không hợp lệ.");

        }

    }



    private Map<Integer, Date> buildSessionDateMap(List<ExamSession> sessions) {

        Map<Integer, Date> map = new HashMap<>();

        for (ExamSession s : sessions) {

            map.put(s.getId(), s.getExamDate());

        }

        return map;

    }



    private Map<Integer, User> buildExaminerMap() {

        Map<Integer, User> map = new HashMap<>();

        for (User u : assignmentDAO.getActiveExaminers()) {

            map.put(u.getId(), u);

        }

        return map;

    }



    private String resolveExaminerName(User examiner) {
        if (examiner.getPerson() != null && examiner.getPerson().getFullName() != null
                && !examiner.getPerson().getFullName().isBlank()) {
            return examiner.getPerson().getFullName();
        }
        return examiner.getUsername();
    }

    private int resolveStaffId(HttpSession session) {

        User user = (User) session.getAttribute("user");

        return (user != null && user.getId() > 0) ? user.getId() : 3;

    }



    private void addAuditLog(HttpSession session, String action, String details) {

        Utils.AuditLogHelper.persist(session, action, details);

    }



    @Override

    protected void doPost(HttpServletRequest request, HttpServletResponse response)

            throws ServletException, IOException {

        doGet(request, response);

    }

}


