package examiner.filter;

import auth.dto.UserDTO;
import shared.Attributes;
import shared.enums.SectionType;
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
import examiner.service.ExamService;
import examiner.service.RoleService;
import examiner.service.impl.ExamServiceImpl;
import examiner.service.impl.RoleServiceImpl;

@WebFilter(urlPatterns = {"/examiner/*"})

// Authentication and session-context filter for all examiner URLs; enforces EXAMINER role and active exam binding.
public class ExaminerFilter extends HttpFilter {

    // Session attributes shared between examiner pages (delegate to shared.Attributes)
    public static final String ATTR_EXAMINER_SCHEDULE = Attributes.Examiner.SCHEDULE;
    public static final String ATTR_ACTIVE_EXAM_ID = Attributes.Examiner.ACTIVE_EXAM_ID;
    public static final String ATTR_EXAM_SECTION = Attributes.Examiner.EXAM_SECTION;
    public static final String ATTR_HAS_ACTIVE = Attributes.Examiner.HAS_ACTIVE_EXAM;
    public static final String ATTR_MESSAGE = Attributes.Examiner.EXAM_MESSAGE;

    // Session selection page
    private static final String SESSION_SELECT_PATH = "/examiner/exam";
    private static final String PROFILE_PATH = "/examiner/profile";
    private static final String CHANGE_PASSWORD_PATH = "/examiner/change-password";

    // Business services
    private final RoleService roleService = new RoleServiceImpl();
    private final ExamService examService = new ExamServiceImpl();

    // Authenticate examiner and refresh active exam context for all examiner pages.
    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // Get user
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
        Role role = roleService.get(user.getRoleId());
        if (role == null || RoleType.fromValue(role.getRoleName()) != RoleType.EXAMINER) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // The shift-selection page manages its own state and must not be gated by it.
        String path = requestPath(request);
        if (SESSION_SELECT_PATH.equals(path)) {
            chain.doFilter(request, response);
            return;
        }

        // Account pages do not require an active exam session. In particular, the
        // forced first-login password page must remain reachable before a shift is selected.
        // They still render the shared examiner shell, so mirror whatever context exists:
        // skipping this left the sidebar permanently locked and the header stuck on
        // "Chưa có ca" even when a shift was already selected.
        if (PROFILE_PATH.equals(path) || CHANGE_PASSWORD_PATH.equals(path)) {
            refreshSession(session, user.getUserId());
            updateRequest(session, request);
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

        // Continue processing
        chain.doFilter(request, response);
    }

    // Reload schedule from DB, verify ownership and IN_PROGRESS status, and refresh session attributes.
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
        ExaminerSchedule schedule = examService.getByScheduleId(stored.getExaminerScheduleId());

        // case 2: Ensure the schedule still belongs to the current examiner
        if (schedule == null || schedule.getExaminerId() != examinerUserId) {
            clearContext(session);
            return false;
        }

        // case 3: Verify that the examination session is still active
        Exam exam = examService.get(schedule.getExamId());
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
            ExamArea area = examService.getByAreaId(schedule.getExamAreaId());
            schedule.setExamArea(area);
        }

        // Populate information
        schedule.setExam(exam);
        ExamSection section = (schedule.getExamSectionId() != null && schedule.getExamSectionId() > 0)
                ? examService.getBySectionId(schedule.getExamSectionId()) : null;
        schedule.setExamSection(section);

        // Determine whether the examiner is supervising a theory exam
        SectionType sectionType = (section != null) ? SectionType.fromValue(section.getSectionType()) : null;

        // Update the session with the latest examination context
        session.setAttribute(ATTR_EXAMINER_SCHEDULE, schedule);
        session.setAttribute(ATTR_ACTIVE_EXAM_ID, exam.getExamId());
        session.setAttribute(ATTR_EXAM_SECTION, sectionType);
        session.setAttribute(ATTR_HAS_ACTIVE, Boolean.TRUE);
        session.setAttribute(ATTR_MESSAGE, null);
        return true;
    }

    // Clear all active-exam session attributes when the context becomes invalid.
    private void clearContext(HttpSession session) {
        session.removeAttribute(ATTR_EXAMINER_SCHEDULE);
        session.removeAttribute(ATTR_ACTIVE_EXAM_ID);
        session.removeAttribute(ATTR_EXAM_SECTION);
        session.setAttribute(ATTR_HAS_ACTIVE, Boolean.FALSE);
    }

    // Mirror session exam context onto request attributes for JSP access and sidebar display.
    private void updateRequest(HttpSession session, HttpServletRequest request) {
        copySessionToRequest(session, request, ATTR_HAS_ACTIVE);
        copySessionToRequest(session, request, ATTR_EXAMINER_SCHEDULE);
        copySessionToRequest(session, request, ATTR_ACTIVE_EXAM_ID);
        copySessionToRequest(session, request, ATTR_EXAM_SECTION);
        copySessionToRequest(session, request, ATTR_MESSAGE);
        request.setAttribute(Attributes.Examiner.HAS_ACTIVE_EXAM, session.getAttribute(ATTR_HAS_ACTIVE));
        SectionType sectionType = resolveSectionType(session);
        request.setAttribute(Attributes.Examiner.SECTION_IS_THEORY, sectionType == SectionType.THEORY);
        String sectionDisplay = sectionType.getValue();
        request.setAttribute(Attributes.Examiner.EXAM_SECTION_NAME, sectionDisplay);
    }

    // Copy a single named session attribute onto the request scope.
    private void copySessionToRequest(HttpSession session, HttpServletRequest request, String attribute) {
        request.setAttribute(attribute, session.getAttribute(attribute));
    }

    // Return the request  with the servlet context path prefix removed.
    private String requestPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String ctx = request.getContextPath();

        if (ctx != null && !ctx.isEmpty() && uri.startsWith(ctx)) {
            return uri.substring(ctx.length());
        }

        return uri;
    }

    // Resolve the active section enum from the canonical session attribute.
    public static SectionType resolveSectionType(HttpSession session) {
        if (session == null) {
            return SectionType.LAYOUT;
        }
        Object raw = session.getAttribute(ATTR_EXAM_SECTION);
        if (raw instanceof SectionType) {
            return (SectionType) raw;
        }
        return SectionType.LAYOUT;
    }
}
