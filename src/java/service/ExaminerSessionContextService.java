package service;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

 
public interface ExaminerSessionContextService {

    
    String ATTR_SLOT = "ExaminerSlotDTO";

    
    String ATTR_ACTIVE_SESSION_ID = "activeSessionId";

    
    String ATTR_EXAM_SECTION_NAME = "examSectionName";

    
    String ATTR_SECTION_TYPE = "examinerSectionType";

    
    String ATTR_SECTION_THEORY = "examinerSectionTheory";

    
    String ATTR_HAS_ACTIVE = "examinerHasActiveSession";

    
    String ATTR_MESSAGE = "examinerSessionMessage";

    
    void refresh(HttpSession session, int examinerUserId);

    
    void clear(HttpSession session);

    
    void copyToRequest(HttpSession session, HttpServletRequest request);

    
    boolean hasActiveSession(HttpSession session);
}

