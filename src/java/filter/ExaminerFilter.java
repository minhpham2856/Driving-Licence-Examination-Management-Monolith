package filter;

import enums.ExamSection;
import static enums.ExamSection.THEORY;
import enums.ExamSessionStatus;
import enums.UserRole;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.ExamArea;
import model.ExaminerSchedule;
import model.Session;
import model.User;
import service.ExamAreaService;
import service.ExamSessionService;
import service.ExaminerService;
import service.RoleService;
import service.impl.ExamAreaServiceImpl;
import service.impl.ExamSessionServiceImpl;
import service.impl.ExaminerServiceImpl;
import service.impl.RoleServiceImpl;

import java.io.IOException;

@WebFilter(urlPatterns = {"/views/examiner/*", "/examiner/*"})
public class ExaminerFilter extends HttpFilter {

    public static final String ATTR_EXAMINER_SCHEDULE = "examinerSchedule";
    public static final String ATTR_ACTIVE_SESSION_ID = "activeSessionId";
    public static final String ATTR_EXAM_SECTION = "examSection";
    public static final String ATTR_EXAM_SECTION_NAME = "examSectionName";
    public static final String ATTR_SECTION_THEORY = "examinerSectionTheory";
    public static final String ATTR_HAS_ACTIVE = "examinerHasActiveSession";
    public static final String ATTR_MESSAGE = "examinerSessionMessage";

    private static final String SESSION_SELECT_PATH = "/views/examiner/session";

    private final RoleService roleService = new RoleServiceImpl();
    private final ExaminerService examinerService = new ExaminerServiceImpl();
    private final ExamSessionService examSessionService = new ExamSessionServiceImpl();
    private final ExamAreaService examAreaService = new ExamAreaServiceImpl();

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpSession session = request.getSession(false);
        User user = session != null ? (User) session.getAttribute("user") : null;

        if (user == null) {
            HttpSession loginSession = request.getSession(true);
            loginSession.setAttribute("errorMessage", "Bạn cần đăng nhập để truy cập.");
            response.sendRedirect(request.getContextPath() + "/staff/login");
            return;
        }

        String roleName = roleService.getRoleNameById(user.getRoleId());
        if (UserRole.fromValue(roleName) != UserRole.EXAMINER) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String path = requestPath(request);
        if (SESSION_SELECT_PATH.equals(path)) {
            chain.doFilter(request, response);
            return;
        }

        if (!refreshSelectedSession(session, user.getUserId())) {
            response.sendRedirect(request.getContextPath() + SESSION_SELECT_PATH);
            return;
        }

        updateRequest(session, request);

        if (!isActive(session) && isExportPath(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean refreshSelectedSession(HttpSession session, int examinerUserId) {
        if (session == null) {
            return false;
        }

        Object scheduleObj = session.getAttribute(ATTR_EXAMINER_SCHEDULE);
        if (!(scheduleObj instanceof ExaminerSchedule)) {
            clearSessionContext(session);
            return false;
        }

        ExaminerSchedule stored = (ExaminerSchedule) scheduleObj;
        ExaminerSchedule schedule = examinerService.getScheduleById(stored.getExaminerScheduleId());
        if (schedule == null || schedule.getExaminerId() != examinerUserId) {
            clearSessionContext(session);
            return false;
        }

        Session examSession = examSessionService.getById(schedule.getSessionId());
        if (examSession == null
                || ExamSessionStatus.fromValue(examSession.getStatus()) != ExamSessionStatus.IN_PROGRESS) {
            clearSessionContext(session);
            session.setAttribute(ATTR_MESSAGE, "Ca thi không còn đang diễn ra");
            return false;
        }

        if (schedule.getExamAreaId() != null && schedule.getExamAreaId() > 0) {
            ExamArea area = examAreaService.getById(schedule.getExamAreaId());
            schedule.setExamArea(area);
        }
        schedule.setSession(examSession);
        schedule.setExamSection(examSessionService.getExamSectionModel(schedule, examSession));

        ExamSection examSection = examSessionService.resolveExamSection(schedule, examSession);
        boolean isTheory = examSection == THEORY;

        session.setAttribute(ATTR_EXAMINER_SCHEDULE, schedule);
        session.setAttribute(ATTR_ACTIVE_SESSION_ID, examSession.getSessionId());
        session.setAttribute(ATTR_EXAM_SECTION, examSection);
        session.setAttribute(ATTR_EXAM_SECTION_NAME, examSection.getValue());
        session.setAttribute(ATTR_SECTION_THEORY, isTheory);
        session.setAttribute(ATTR_HAS_ACTIVE, Boolean.TRUE);
        session.setAttribute(ATTR_MESSAGE, null);
        return true;
    }

    private void clearSessionContext(HttpSession session) {
        session.removeAttribute(ATTR_EXAMINER_SCHEDULE);
        session.removeAttribute(ATTR_ACTIVE_SESSION_ID);
        session.removeAttribute(ATTR_EXAM_SECTION);
        session.removeAttribute(ATTR_EXAM_SECTION_NAME);
        session.removeAttribute(ATTR_SECTION_THEORY);
        session.setAttribute(ATTR_HAS_ACTIVE, Boolean.FALSE);
    }

    private void updateRequest(HttpSession session, HttpServletRequest request) {
        copySessionToRequest(session, request, ATTR_HAS_ACTIVE);
        copySessionToRequest(session, request, ATTR_EXAMINER_SCHEDULE);
        copySessionToRequest(session, request, ATTR_ACTIVE_SESSION_ID);
        copySessionToRequest(session, request, ATTR_EXAM_SECTION);
        copySessionToRequest(session, request, ATTR_EXAM_SECTION_NAME);
        copySessionToRequest(session, request, ATTR_SECTION_THEORY);
        copySessionToRequest(session, request, ATTR_MESSAGE);
    }

    private void copySessionToRequest(HttpSession session, HttpServletRequest request, String attribute) {
        request.setAttribute(attribute, session.getAttribute(attribute));
    }

    private boolean isActive(HttpSession session) {
        return session != null && Boolean.TRUE.equals(session.getAttribute(ATTR_HAS_ACTIVE));
    }

    private boolean isExportPath(HttpServletRequest request) {
        return requestPath(request).startsWith("/examiner/");
    }

    private String requestPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String ctx = request.getContextPath();
        if (ctx != null && !ctx.isEmpty() && uri.startsWith(ctx)) {
            return uri.substring(ctx.length());
        }
        return uri;
    }

    public static boolean isTheorySession(HttpSession session) {
        return session != null && Boolean.TRUE.equals(session.getAttribute(ATTR_SECTION_THEORY));
    }
}
