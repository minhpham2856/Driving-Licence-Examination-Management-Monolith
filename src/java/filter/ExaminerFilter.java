package filter;

import dto.ExaminerSlotDTO;
import enums.ExamSection;
import static enums.ExamSection.THEORY;
import enums.UserRole;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import model.User;
import service.ExamSessionControlService;
import service.RoleService;
import service.impl.ExamSessionControlServiceImpl;
import service.impl.RoleServiceImpl;

@WebFilter(urlPatterns = {"/views/examiner/*", "/examiner/*"})
public class ExaminerFilter extends HttpFilter {

    public static final String ATTR_SLOT = "ExaminerSlotDTO";
    public static final String ATTR_ACTIVE_SESSION_ID = "activeSessionId";
    public static final String ATTR_EXAM_SECTION = "examSection";
    public static final String ATTR_EXAM_SECTION_NAME = "examSectionName";
    public static final String ATTR_SECTION_THEORY = "examinerSectionTheory";
    public static final String ATTR_HAS_ACTIVE = "examinerHasActiveSession";
    public static final String ATTR_MESSAGE = "examinerSessionMessage";

    private final RoleService roleService = new RoleServiceImpl();
    private final ExamSessionControlService controlService = new ExamSessionControlServiceImpl();

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // Get current logged in user
        HttpSession session = request.getSession(false);
        User user = session != null
                ? (User) session.getAttribute("user")
                : null;

        // Check if user has logged in
        if (user == null) {
            HttpSession loginSession = request.getSession(true); // Create new session
            loginSession.setAttribute("errorMessage", "Bạn cần đăng nhập để truy cập.");
            response.sendRedirect(request.getContextPath() + "/staff/login");
            return;
        }

        // Check if user is an examiner
        String roleName = roleService.getRoleNameById(user.getRoleId());
        if (UserRole.fromValue(roleName) != UserRole.EXAMINER) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // Refresh examiner session information
        refreshSessionContext(session, user.getUserId());

        // Copy session values to request for views to access them
        updateRequest(session, request);

        // Prevent examiner actions when there is no active assignment
        if (!isActive(session) && isExaminer(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        chain.doFilter(request, response);
    }

    private void refreshSessionContext(HttpSession session, int examinerUserId) {
        if (session == null) {
            return;
        }

        // Get examiner's currently available assignments
        List<ExaminerSlotDTO> slots = controlService.getLoginEligibleAssignments(examinerUserId);

        // No active assignment
        if (slots == null || slots.isEmpty()) {
            clearSessionContext(session);
            session.setAttribute(ATTR_HAS_ACTIVE, Boolean.FALSE);
            session.setAttribute(ATTR_MESSAGE, "Chưa có ca thi");
            return;
        }

        // Use the first active assignment
        ExaminerSlotDTO slot = slots.get(0);

        ExamSection examSection = slot.getExamSection() != null ? slot.getExamSection() : ExamSection.THEORY;
        boolean isTheory = examSection == THEORY;

        session.setAttribute(ATTR_SLOT, slot);
        session.setAttribute(ATTR_ACTIVE_SESSION_ID, slot.getExamSessionId());
        session.setAttribute(ATTR_EXAM_SECTION, examSection);
        session.setAttribute(ATTR_EXAM_SECTION_NAME, examSection.getValue());
        session.setAttribute(ATTR_SECTION_THEORY, isTheory);
        session.setAttribute(ATTR_HAS_ACTIVE, Boolean.TRUE);
        session.setAttribute(ATTR_MESSAGE, null);
    }

    private void clearSessionContext(HttpSession session) {
        session.removeAttribute(ATTR_SLOT);
        session.removeAttribute(ATTR_ACTIVE_SESSION_ID);
        session.removeAttribute(ATTR_EXAM_SECTION);
        session.removeAttribute(ATTR_EXAM_SECTION_NAME);
        session.removeAttribute(ATTR_SECTION_THEORY);
        session.removeAttribute(ATTR_HAS_ACTIVE);
        session.removeAttribute(ATTR_MESSAGE);
    }

    private void updateRequest(HttpSession session, HttpServletRequest request) {
        copySessionToRequest(session, request, ATTR_HAS_ACTIVE);
        copySessionToRequest(session, request, ATTR_SLOT);
        copySessionToRequest(session, request, ATTR_ACTIVE_SESSION_ID);
        copySessionToRequest(session, request, ATTR_EXAM_SECTION);
        copySessionToRequest(session, request, ATTR_EXAM_SECTION_NAME);
        copySessionToRequest(session, request, ATTR_SECTION_THEORY);
        copySessionToRequest(session, request, ATTR_MESSAGE);
    }

    private void copySessionToRequest(HttpSession session, HttpServletRequest request, String attribute) {
        Object sessionAttribute = session.getAttribute(attribute);
        request.setAttribute(attribute, sessionAttribute);
    }

    private boolean isActive(HttpSession session) {
        return session != null
                && Boolean.TRUE.equals(session.getAttribute(ATTR_HAS_ACTIVE));
    }

    private boolean isExaminer(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return path.startsWith("/examiner/");
    }

    public static boolean isTheorySession(HttpSession session) {
        return session != null && Boolean.TRUE.equals(session.getAttribute(ATTR_SECTION_THEORY));
    }

}
