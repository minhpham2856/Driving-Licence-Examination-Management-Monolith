package service.impl;
import dto.*;
import model.*;

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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.List;
import service.EnumMappingService;


public class ExaminerSessionContextServiceImpl implements ExaminerSessionContextService {

    private final EnumMappingService enumMappingService = new EnumMappingServiceImpl();

    
    private final ExamSessionControlService controlService = new ExamSessionControlServiceImpl();

    
    @Override
    public void refresh(HttpSession session, int examinerUserId) {
        
        if (session == null) {
            return;
        }
        
        clear(session);

        
        List<ExaminerSlotDTO> slots = controlService.getLoginEligibleAssignments(examinerUserId);
        
        if (slots == null || slots.isEmpty()) {
            session.setAttribute(ATTR_HAS_ACTIVE, Boolean.FALSE);
            
            session.setAttribute(ATTR_MESSAGE, "Chưa có ca thi");
            return;
        }

        
        ExaminerSlotDTO slot = slots.get(0);
        
        SectionType sectionType = enumMappingService.resolveSectionType(slot.getExamTypeName());
        
        session.setAttribute(ATTR_SLOT, slot);
        
        session.setAttribute(ATTR_ACTIVE_SESSION_ID, slot.getExamSessionId());
        
        session.setAttribute(ATTR_EXAM_SECTION_NAME, resolveSectionName(slot));
        
        session.setAttribute(ATTR_SECTION_TYPE, sectionType);
        
        session.setAttribute(ATTR_SECTION_THEORY, sectionType == SectionType.THEORY);
        
        session.setAttribute(ATTR_HAS_ACTIVE, Boolean.TRUE);
        
        session.setAttribute(ATTR_MESSAGE, null);
    }

    
    @Override
    public void clear(HttpSession session) {
        
        if (session == null) {
            return;
        }
        
        session.removeAttribute(ATTR_SLOT);
        session.removeAttribute(ATTR_ACTIVE_SESSION_ID);
        session.removeAttribute(ATTR_EXAM_SECTION_NAME);
        session.removeAttribute(ATTR_SECTION_TYPE);
        session.removeAttribute(ATTR_SECTION_THEORY);
        session.removeAttribute(ATTR_HAS_ACTIVE);
        session.removeAttribute(ATTR_MESSAGE);
    }

    
    @Override
    public void copyToRequest(HttpSession session, HttpServletRequest request) {
        
        if (session == null || request == null) {
            return;
        }
        
        Boolean hasActive = (Boolean) session.getAttribute(ATTR_HAS_ACTIVE);
        
        boolean active = Boolean.TRUE.equals(hasActive);

        
        request.setAttribute(ATTR_HAS_ACTIVE, active);
        request.setAttribute(ATTR_SLOT, session.getAttribute(ATTR_SLOT));
        request.setAttribute(ATTR_ACTIVE_SESSION_ID, session.getAttribute(ATTR_ACTIVE_SESSION_ID));
        request.setAttribute(ATTR_EXAM_SECTION_NAME, session.getAttribute(ATTR_EXAM_SECTION_NAME));
        request.setAttribute(ATTR_SECTION_TYPE, session.getAttribute(ATTR_SECTION_TYPE));
        request.setAttribute(ATTR_SECTION_THEORY, session.getAttribute(ATTR_SECTION_THEORY));
        request.setAttribute(ATTR_MESSAGE, session.getAttribute(ATTR_MESSAGE));

        
        request.setAttribute("examSectionName", session.getAttribute(ATTR_EXAM_SECTION_NAME));
    }

    
    @Override
    public boolean hasActiveSession(HttpSession session) {
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
}
