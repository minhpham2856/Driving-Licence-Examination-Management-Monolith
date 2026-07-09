package filter;

import enums.SectionType;
import static enums.SectionType.THEORY;
import enums.ExamSessionStatus;
import enums.RoleType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import model.ExamArea;
import model.ExaminerSchedule;
import model.Role;
import model.Session;
import model.User;
import service.ExamAreaService;
import service.RoleService;
import service.ScheduleService;
import service.SessionService;
import service.impl.ExamAreaServiceImpl;
import service.impl.RoleServiceImpl;
import service.impl.ScheduleServiceImpl;
import service.impl.SessionServiceImpl;

@WebFilter(urlPatterns = {"/views/examiner/*", "/examiner/*"})
public class ExaminerFilter extends HttpFilter {

    // Session attributes shared between examiner pages
    public static final String ATTR_EXAMINER_SCHEDULE = "examinerSchedule";
    public static final String ATTR_ACTIVE_SESSION_ID = "activeSessionId";
    public static final String ATTR_EXAM_SECTION = "examSection";
    public static final String ATTR_EXAM_SECTION_NAME = "examSectionName";
    public static final String ATTR_SECTION_THEORY = "examinerSectionTheory";
    public static final String ATTR_HAS_ACTIVE = "examinerHasActiveSession";
    public static final String ATTR_MESSAGE = "examinerSessionMessage";

    // Session selection page
    private static final String SESSION_SELECT_PATH = "/views/examiner/session";

    // Business services
    private final RoleService roleService = new RoleServiceImpl();
    private final ScheduleService scheduleService = new ScheduleServiceImpl();
    private final SessionService sessionService = new SessionServiceImpl();
    private final ExamAreaService examAreaService = new ExamAreaServiceImpl();

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // Get the logged-in user
        HttpSession session = request.getSession(false);
        User user = session != null ? (User) session.getAttribute("user") : null;

        // Redirect unauthenticated users to login page
        if (user == null) {
            HttpSession loginSession = request.getSession(true);
            loginSession.setAttribute("errorMessage", "Bạn cần đăng nhập để truy cập.");
            response.sendRedirect(request.getContextPath() + "/staff/login");
            return;
        }

        // Allow only examiners to access examiner pages
        Role role = roleService.getById(user.getRoleId());
        if (RoleType.fromValue(role.getRoleName()) != RoleType.EXAMINER) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // Skip exam session validation on the session selection page
        String path = requestPath(request);
        if (SESSION_SELECT_PATH.equals(path)) {
            chain.doFilter(request, response);
            return;
        }

        // Refresh and validate the selected examination session
        if (!refreshSession(session, user.getUserId())) {
            response.sendRedirect(request.getContextPath() + SESSION_SELECT_PATH);
            return;
        }

        // Make session data available as request attributes
        updateRequest(session, request);

        // Prevent export requests when no active session exists
        if (!isActive(session) && isExportPath(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // Continue processing
        chain.doFilter(request, response);
    }

    // Reload and validate the examiner's selected session
    private boolean refreshSession(HttpSession session, int examinerUserId) {
        if (session == null) {
            return false;
        }

        // case 1: Verify that a schedule has been selected
        Object scheduleObj = session.getAttribute(ATTR_EXAMINER_SCHEDULE);
        if (!(scheduleObj instanceof ExaminerSchedule)) {
            clearContext(session);
            return false;
        }

        // Reload the latest schedule information
        ExaminerSchedule stored = (ExaminerSchedule) scheduleObj;
        ExaminerSchedule schedule = scheduleService.getScheduleById(stored.getExaminerScheduleId());

        // case 2: Ensure the schedule still belongs to the current examiner
        if (schedule == null || schedule.getExaminerId() != examinerUserId) {
            clearContext(session);
            return false;
        }

        // case 3: Verify that the examination session is still active
        Session examSession = sessionService.getById(schedule.getSessionId());
        if (examSession == null || ExamSessionStatus.fromValue(examSession.getStatus()) != ExamSessionStatus.IN_PROGRESS) {
            clearContext(session);
            session.setAttribute(ATTR_MESSAGE, "Ca thi đang không diễn ra");
            return false;
        }

        // Load the assigned examination area
        if (schedule.getExamAreaId() != null && schedule.getExamAreaId() > 0) {
            ExamArea area = examAreaService.getById(schedule.getExamAreaId());
            schedule.setExamArea(area);
        }

        // Populate information
        schedule.setSession(examSession);
        schedule.setExamSection(sessionService.getExamSectionModel(schedule, examSession));

        // Determine whether the examiner is supervising a theory exam
        SectionType examSection = sessionService.getExamSection(schedule, examSession);
        boolean isTheory = examSection == THEORY;

        // Update the session with the latest examination context
        session.setAttribute(ATTR_EXAMINER_SCHEDULE, schedule);
        session.setAttribute(ATTR_ACTIVE_SESSION_ID, examSession.getSessionId());
        session.setAttribute(ATTR_EXAM_SECTION, examSection);
        session.setAttribute(ATTR_EXAM_SECTION_NAME, examSection.getValue());
        session.setAttribute(ATTR_SECTION_THEORY, isTheory);
        session.setAttribute(ATTR_HAS_ACTIVE, Boolean.TRUE);
        session.setAttribute(ATTR_MESSAGE, null);

        return true;
    }

    // Remove the current examination context
    private void clearContext(HttpSession session) {
        session.removeAttribute(ATTR_EXAMINER_SCHEDULE);
        session.removeAttribute(ATTR_ACTIVE_SESSION_ID);
        session.removeAttribute(ATTR_EXAM_SECTION);
        session.removeAttribute(ATTR_EXAM_SECTION_NAME);
        session.removeAttribute(ATTR_SECTION_THEORY);
        session.setAttribute(ATTR_HAS_ACTIVE, Boolean.FALSE);
    }

    // Copy session attributes into the current request
    private void updateRequest(HttpSession session, HttpServletRequest request) {
        copySessionToRequest(session, request, ATTR_HAS_ACTIVE);
        copySessionToRequest(session, request, ATTR_EXAMINER_SCHEDULE);
        copySessionToRequest(session, request, ATTR_ACTIVE_SESSION_ID);
        copySessionToRequest(session, request, ATTR_EXAM_SECTION);
        copySessionToRequest(session, request, ATTR_EXAM_SECTION_NAME);
        copySessionToRequest(session, request, ATTR_SECTION_THEORY);
        copySessionToRequest(session, request, ATTR_MESSAGE);
    }

    // Copy a single session attribute to the request
    private void copySessionToRequest(HttpSession session, HttpServletRequest request, String attribute) {
        request.setAttribute(attribute, session.getAttribute(attribute));
    }

    // Check whether an active examination session exists
    private boolean isActive(HttpSession session) {
        return session != null && Boolean.TRUE.equals(session.getAttribute(ATTR_HAS_ACTIVE));
    }

    // Check whether the request targetS an export endpoint
    private boolean isExportPath(HttpServletRequest request) {
        return requestPath(request).startsWith("/examiner/");
    }

    // Get the request path without the context path
    private String requestPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String ctx = request.getContextPath();

        if (ctx != null && !ctx.isEmpty() && uri.startsWith(ctx)) {
            return uri.substring(ctx.length());
        }

        return uri;
    }

    // Check whether the current session is a theory examination
    public static boolean isTheory(HttpSession session) {
        return session != null && Boolean.TRUE.equals(session.getAttribute(ATTR_SECTION_THEORY));
    }
}
