package examiner.filter;

import auth.dto.UserDTO;
import shared.Attributes;
import shared.enums.SectionType;
import static shared.enums.SectionType.THEORY;
import shared.enums.ExamStatus;
import shared.enums.RoleType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import shared.model.Exam;
import shared.model.ExamArea;
import shared.model.ExamSection;
import shared.model.ExaminerSchedule;
import shared.model.Role;
import examiner.service.ExamAreaService;
import examiner.service.ExamSectionService;
import examiner.service.ExamService;
import examiner.service.RoleService;
import examiner.service.ScheduleService;
import examiner.service.impl.ExamAreaServiceImpl;
import examiner.service.impl.ExamServiceImpl;
import examiner.service.impl.ExamSectionServiceImpl;
import examiner.service.impl.RoleServiceImpl;
import examiner.service.impl.ScheduleServiceImpl;

@WebFilter(urlPatterns = {"/views/examiner/*", "/examiner/*"})
public class ExaminerFilter extends HttpFilter {

    // Session attributes shared between examiner pages (delegate to shared.Attributes)
    public static final String ATTR_EXAMINER_SCHEDULE = Attributes.Examiner.SCHEDULE;
    public static final String ATTR_ACTIVE_EXAM_ID = Attributes.Examiner.ACTIVE_EXAM_ID;
    public static final String ATTR_EXAM_SECTION = Attributes.Examiner.EXAM_SECTION;
    public static final String ATTR_HAS_ACTIVE = Attributes.Examiner.HAS_ACTIVE_EXAM;
    public static final String ATTR_MESSAGE = Attributes.Examiner.EXAM_MESSAGE;

    // Session selection page
    private static final String SESSION_SELECT_PATH = "/views/examiner/exam";

    // Business services
    private final RoleService roleService = new RoleServiceImpl();
    private final ScheduleService scheduleService = new ScheduleServiceImpl();
    private final ExamService examService = new ExamServiceImpl();
    private final ExamAreaService examAreaService = new ExamAreaServiceImpl();
    private final ExamSectionService examSectionService = new ExamSectionServiceImpl();

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // Get the logged-in user
        HttpSession session = request.getSession(false);
        UserDTO user = session != null ? (UserDTO) session.getAttribute(Attributes.Session.USER) : null;

        // Redirect unauthenticated users to login page
        if (user == null) {
            HttpSession loginSession = request.getSession(true);
            loginSession.setAttribute(Attributes.Session.ERROR_MESSAGE, "Bạn cần đăng nhập để truy cập.");
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
        Exam exam = examService.getById(schedule.getExamId());
        if (exam == null || ExamStatus.fromValue(exam.getStatus()) != ExamStatus.IN_PROGRESS) {
            clearContext(session);
            ExamStatus status = exam != null ? ExamStatus.fromValue(exam.getStatus()) : null;
            if (status == ExamStatus.PAUSED) {
                session.setAttribute(ATTR_MESSAGE, "Kỳ thi đang tạm dừng");
            } else {
                session.setAttribute(ATTR_MESSAGE, "Kỳ thi đang không diễn ra");
            }
            return false;
        }

        // Load the assigned examination area
        if (schedule.getExamAreaId() != null && schedule.getExamAreaId() > 0) {
            ExamArea area = examAreaService.getById(schedule.getExamAreaId());
            schedule.setExamArea(area);
        }

        // Populate information
        schedule.setExam(exam);
        ExamSection section = (schedule.getExamSectionId() != null && schedule.getExamSectionId() > 0)
                ? examSectionService.getById(schedule.getExamSectionId()) : null;
        schedule.setExamSection(section);

        // Determine whether the examiner is supervising a theory exam
        SectionType examSection = (section != null) ? SectionType.fromValue(section.getSectionType()) : null;

        // Update the session with the latest examination context
        session.setAttribute(ATTR_EXAMINER_SCHEDULE, schedule);
        session.setAttribute(ATTR_ACTIVE_EXAM_ID, exam.getExamId());
        session.setAttribute(ATTR_EXAM_SECTION, examSection);
        session.setAttribute(ATTR_HAS_ACTIVE, Boolean.TRUE);
        session.setAttribute(ATTR_MESSAGE, null);

        return true;
    }

    // Remove the current examination context
    private void clearContext(HttpSession session) {
        session.removeAttribute(ATTR_EXAMINER_SCHEDULE);
        session.removeAttribute(ATTR_ACTIVE_EXAM_ID);
        session.removeAttribute(ATTR_EXAM_SECTION);
        session.setAttribute(ATTR_HAS_ACTIVE, Boolean.FALSE);
    }

    // Copy session attributes into the current request
    private void updateRequest(HttpSession session, HttpServletRequest request) {
        copySessionToRequest(session, request, ATTR_HAS_ACTIVE);
        copySessionToRequest(session, request, ATTR_EXAMINER_SCHEDULE);
        copySessionToRequest(session, request, ATTR_ACTIVE_EXAM_ID);
        copySessionToRequest(session, request, ATTR_EXAM_SECTION);
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

    // Check whether the request targets an export endpoint
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

    public static boolean isTheory(HttpSession session) {
        return session.getAttribute(ATTR_EXAM_SECTION) == THEORY;
    }
}

