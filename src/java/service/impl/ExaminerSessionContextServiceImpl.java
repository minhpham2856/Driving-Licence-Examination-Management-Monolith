package service.impl;




import enums.SectionType;

import dto.ExaminerSlotDTO;

import service.ExamSessionControlService;

import service.ExaminerSessionContextService;

import static service.ExaminerSessionContextService.ATTR_ACTIVE_SESSION_ID;

import static service.ExaminerSessionContextService.ATTR_EXAM_SECTION_NAME;

import static service.ExaminerSessionContextService.ATTR_HAS_ACTIVE;

import static service.ExaminerSessionContextService.ATTR_MESSAGE;

import static service.ExaminerSessionContextService.ATTR_SECTION_THEORY;

import static service.ExaminerSessionContextService.ATTR_SECTION_TYPE;

import static service.ExaminerSessionContextService.ATTR_SLOT;

import util.ExaminerBreadcrumbs;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.List;

// Implementation of {@link ExaminerSessionContextService}.
public class ExaminerSessionContextServiceImpl implements ExaminerSessionContextService {

    // Service used to query the examiner's eligible in-progress assignments from the database
    private final ExamSessionControlService controlService = new ExamSessionControlServiceImpl();

    // Loads (or reloads) the examiner's active session assignment.
    @Override
    public void refresh(HttpSession session, int examinerUserId) {
        // Guard: do nothing if session is null (e.g. unit test scenario)
        if (session == null) {
            return;
        }
        // Clear all previously cached session context attributes before reloading
        clear(session);

        // Query the database for all in-progress assignments for this examiner
        List<ExaminerSlotDTO> slots = controlService.getLoginEligibleAssignments(examinerUserId);
        // If no active assignments exist, set the "no active session" state
        if (slots == null || slots.isEmpty()) {
            session.setAttribute(ATTR_HAS_ACTIVE, Boolean.FALSE);
            // Store a descriptive Vietnamese message explaining why no session is available
            session.setAttribute(ATTR_MESSAGE, "Chưa có ca thi");
            return;
        }

        // Use the first assignment slot (examiner typically has one active slot)
        ExaminerSlotDTO slot = slots.get(0);
        // Resolve the section type enum from the exam type name string
        SectionType sectionType = enums.SectionType.resolveSectionType(slot.getExamTypeName());
        // Cache the slot object for use by controllers and export servlets
        session.setAttribute(ATTR_SLOT, slot);
        // Cache the database session ID for quick access
        session.setAttribute(ATTR_ACTIVE_SESSION_ID, slot.getExamSessionId());
        // Cache the human-readable section name for display in the UI
        session.setAttribute(ATTR_EXAM_SECTION_NAME, resolveSectionName(slot));
        // Cache the section type enum for branching logic (theory vs score-based)
        session.setAttribute(ATTR_SECTION_TYPE, sectionType);
        // Cache a boolean flag indicating whether this is a theory section
        session.setAttribute(ATTR_SECTION_THEORY, sectionType == SectionType.THEORY);
        // Set the active flag to true so JSPs know an active session exists
        session.setAttribute(ATTR_HAS_ACTIVE, Boolean.TRUE);
        // Clear any previous message (no error — session is active)
        session.setAttribute(ATTR_MESSAGE, null);
    }

    // Removes all examiner context attributes from the HTTP session.
    @Override
    public void clear(HttpSession session) {
        // Guard: do nothing if session is null
        if (session == null) {
            return;
        }
        // Remove each cached attribute by its constant key
        session.removeAttribute(ATTR_SLOT);
        session.removeAttribute(ATTR_ACTIVE_SESSION_ID);
        session.removeAttribute(ATTR_EXAM_SECTION_NAME);
        session.removeAttribute(ATTR_SECTION_TYPE);
        session.removeAttribute(ATTR_SECTION_THEORY);
        session.removeAttribute(ATTR_HAS_ACTIVE);
        session.removeAttribute(ATTR_MESSAGE);
    }

    // Copies all cached session-context attributes into request scope.
    @Override
    public void copyToRequest(HttpSession session, HttpServletRequest request) {
        // Guard: do nothing if either session or request is null
        if (session == null || request == null) {
            return;
        }
        // Read the cached "has active session" flag from the HTTP session
        Boolean hasActive = (Boolean) session.getAttribute(ATTR_HAS_ACTIVE);
        // Normalise to primitive boolean (null/FALSE => false)
        boolean active = Boolean.TRUE.equals(hasActive);

        // Copy all context attributes from session scope to request scope for JSP access
        request.setAttribute(ATTR_HAS_ACTIVE, active);
        request.setAttribute(ATTR_SLOT, session.getAttribute(ATTR_SLOT));
        request.setAttribute(ATTR_ACTIVE_SESSION_ID, session.getAttribute(ATTR_ACTIVE_SESSION_ID));
        request.setAttribute(ATTR_EXAM_SECTION_NAME, session.getAttribute(ATTR_EXAM_SECTION_NAME));
        request.setAttribute(ATTR_SECTION_TYPE, session.getAttribute(ATTR_SECTION_TYPE));
        request.setAttribute(ATTR_SECTION_THEORY, session.getAttribute(ATTR_SECTION_THEORY));
        request.setAttribute(ATTR_MESSAGE, session.getAttribute(ATTR_MESSAGE));

        // Build and attach the breadcrumb navigation items for the current page
        request.setAttribute("headerBreadcrumbItems", ExaminerBreadcrumbs.buildItems(request));
        // Build and attach the plain-text breadcrumb string for the page header
        request.setAttribute("headerBreadcrumb", ExaminerBreadcrumbs.resolve(request));
        // Duplicate the section name for convenience access by JSP components
        request.setAttribute("examSectionName", session.getAttribute(ATTR_EXAM_SECTION_NAME));
    }

    // Convenience check for whether the current HTTP session has a valid,
    @Override
    public boolean hasActiveSession(HttpSession session) {
        return session != null && Boolean.TRUE.equals(session.getAttribute(ATTR_HAS_ACTIVE));
    }

    // Resolves the human-readable section name from a slot object.
    private static String resolveSectionName(ExaminerSlotDTO slot) {
        // Return dash placeholder if slot is null
        if (slot == null) {
            return "-";
        }
        // Read the exam type name from the slot
        String name = slot.getExamTypeName();
        // Return the trimmed name if it exists and is non-blank
        if (name != null && !name.isBlank()) {
            return name.trim();
        }
        // Return dash placeholder for blank or null exam type names
        return "-";
    }
}







