package filter;
import dto.ExaminerSlotDTO;
import enums.ExamSection;
import enums.UserRole;
import model.User;
import service.ExamSessionControlService;
import service.RoleService;
import service.impl.ExamSessionControlServiceImpl;
import service.impl.RoleServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
@WebFilter(urlPatterns = {"/views/examiner/*", "/examiner/*"})
public class ExaminerPortalFilter extends HttpFilter {
    public static final String ATTR_SLOT = "ExaminerSlotDTO";
    public static final String ATTR_ACTIVE_SESSION_ID = "activeSessionId";
    public static final String ATTR_EXAM_SECTION_NAME = "examSectionName";
    public static final String ATTR_SECTION_THEORY = "examinerSectionTheory";
    public static final String ATTR_HAS_ACTIVE = "examinerHasActiveSession";
    public static final String ATTR_MESSAGE = "examinerSessionMessage";
    private final RoleService roleService = new RoleServiceImpl();
    private final ExamSessionControlService controlService = new ExamSessionControlServiceImpl();
    public static boolean isTheorySession(HttpSession session) {
        if (session == null) {
            return true;
        }
        Object value = session.getAttribute(ATTR_SECTION_THEORY);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return true;
    }
    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpSession session = request.getSession(false);
        User user = session != null ? (User) session.getAttribute("user") : null;
        if (user == null) {
            HttpSession loginSession = request.getSession(true);
            loginSession.setAttribute("errorMessage", "Bạn cần đăng nhập để truy cập.");
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        String roleName = roleService.getRoleNameById(user.getRoleId());
        if (!UserRole.isExaminer(roleName)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        refreshSessionContext(session, user.getUserId());
        copySessionToRequest(session, request);
        if (!hasActiveSession(session) && isExaminerActionPath(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        chain.doFilter(request, response);
    }
    private void refreshSessionContext(HttpSession session, int examinerUserId) {
        if (session == null) {
            return;
        }
        clearSessionContext(session);
        List<ExaminerSlotDTO> slots = controlService.getLoginEligibleAssignments(examinerUserId);
        if (slots == null || slots.isEmpty()) {
            session.setAttribute(ATTR_HAS_ACTIVE, Boolean.FALSE);
            session.setAttribute(ATTR_MESSAGE, "Chưa có ca thi");
            return;
        }
        ExaminerSlotDTO slot = slots.get(0);
        boolean isTheory = ExamSection.isTheory(slot.getExamTypeName());
        session.setAttribute(ATTR_SLOT, slot);
        session.setAttribute(ATTR_ACTIVE_SESSION_ID, slot.getExamSessionId());
        session.setAttribute(ATTR_EXAM_SECTION_NAME, resolveSectionName(slot));
        session.setAttribute(ATTR_SECTION_THEORY, isTheory);
        session.setAttribute(ATTR_HAS_ACTIVE, Boolean.TRUE);
        session.setAttribute(ATTR_MESSAGE, null);
    }
    private void clearSessionContext(HttpSession session) {
        if (session == null) {
            return;
        }
        session.removeAttribute(ATTR_SLOT);
        session.removeAttribute(ATTR_ACTIVE_SESSION_ID);
        session.removeAttribute(ATTR_EXAM_SECTION_NAME);
        session.removeAttribute(ATTR_SECTION_THEORY);
        session.removeAttribute(ATTR_HAS_ACTIVE);
        session.removeAttribute(ATTR_MESSAGE);
    }
    private void copySessionToRequest(HttpSession session, HttpServletRequest request) {
        if (session == null || request == null) {
            return;
        }
        boolean active = Boolean.TRUE.equals(session.getAttribute(ATTR_HAS_ACTIVE));
        request.setAttribute(ATTR_HAS_ACTIVE, active);
        request.setAttribute(ATTR_SLOT, session.getAttribute(ATTR_SLOT));
        request.setAttribute(ATTR_ACTIVE_SESSION_ID, session.getAttribute(ATTR_ACTIVE_SESSION_ID));
        request.setAttribute(ATTR_EXAM_SECTION_NAME, session.getAttribute(ATTR_EXAM_SECTION_NAME));
        request.setAttribute(ATTR_SECTION_THEORY, session.getAttribute(ATTR_SECTION_THEORY));
        request.setAttribute(ATTR_MESSAGE, session.getAttribute(ATTR_MESSAGE));
        request.setAttribute("examSectionName", session.getAttribute(ATTR_EXAM_SECTION_NAME));
    }
    private boolean hasActiveSession(HttpSession session) {
        return session != null && Boolean.TRUE.equals(session.getAttribute(ATTR_HAS_ACTIVE));
    }
    private static String resolveSectionName(ExaminerSlotDTO slot) {
        if (slot == null) {
            return "-";
        }
        String name = slot.getExamTypeName();
        if (name != null && !name.isBlank()) {
            return name.trim();
        }
        return "-";
    }
    private boolean isExaminerActionPath(HttpServletRequest request) {
        return stripContextPath(request).startsWith("/examiner/");
    }
    private String stripContextPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String ctx = request.getContextPath();
        if (ctx != null && !ctx.isEmpty() && uri.startsWith(ctx)) {
            return uri.substring(ctx.length());
        }
        return uri;
    }
}
