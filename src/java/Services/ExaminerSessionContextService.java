package Services;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

 // Service interface for managing the examiner's active session context.
public interface ExaminerSessionContextService {

    // Session attribute key for the parsed {@code ExaminerSlot} object.
    String ATTR_SLOT = "examinerSlot";

    // Session attribute key for the active database session ID.
    String ATTR_ACTIVE_SESSION_ID = "activeSessionId";

    // Session attribute key for the exam section display name.
    String ATTR_EXAM_SECTION_NAME = "examSectionName";

    // Session attribute key for the examiner's section type constant.
    String ATTR_SECTION_TYPE = "examinerSectionType";

    // Session attribute key for the "is theory section" boolean flag.
    String ATTR_SECTION_THEORY = "examinerSectionTheory";

    // Session attribute key for the "has active session" boolean flag.
    String ATTR_HAS_ACTIVE = "examinerHasActiveSession";

    // Session attribute key for an optional status or error message.
    String ATTR_MESSAGE = "examinerSessionMessage";

    // Loads the examiner's active assignment from the DB and caches it in the HTTP session
    void refresh(HttpSession session, int examinerUserId);

    // Removes all cached examiner context attributes from the HTTP session
    void clear(HttpSession session);

    // Copies cached session context to request scope and builds breadcrumb navigation
    void copyToRequest(HttpSession session, HttpServletRequest request);

    // Quick check: returns true if the examiner has an active session assignment
    boolean hasActiveSession(HttpSession session);
}
