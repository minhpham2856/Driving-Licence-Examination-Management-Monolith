package examstaff.service.impl;

import examstaff.dto.AutoAllocateResultDTO;
import examstaff.dto.exam.ExamRegistrationDTO;
import examstaff.dto.AllocationActionResultDTO;
import examstaff.dto.AllocationCandidateActionRequest;
import examstaff.enums.SectionType;
import shared.model.ExamArea;
import examstaff.service.AllocationActionService;
import examstaff.service.ExamAreaQueryService;
import examstaff.service.ExamRegistrationService;
import examstaff.service.impl.ExamRegistrationServiceImpl;
import examstaff.service.ExaminerAllocationService;
import examstaff.util.AllocationStageHelper;
import examstaff.util.ExaminerAssignmentRules;

import java.util.List;
import java.util.Set;

public class AllocationActionServiceImpl implements AllocationActionService {

    private final ExamRegistrationService regService = new ExamRegistrationServiceImpl();
    private final ExamAreaQueryService areaQueryService = new ExamAreaQueryServiceImpl();
    private final ExaminerAllocationService examinerAllocationService = new ExaminerAllocationServiceImpl();

    @Override
    public AllocationActionResultDTO autoAllocateOnOverview(int sessionId, String stage) {
        AllocationActionResultDTO result = new AllocationActionResultDTO();
        if (sessionId <= 0) {
            return result;
        }
        int allocated = 0;
        if (AllocationStageHelper.STAGE_OVERVIEW.equals(stage)
                || AllocationStageHelper.STAGE_THEORY.equals(stage)) {
            AutoAllocateResultDTO theoryAlloc = examinerAllocationService.autoAllocateSession(sessionId);
            allocated += theoryAlloc.allocatedCount;
        }
        if (AllocationStageHelper.STAGE_OVERVIEW.equals(stage)
                || AllocationStageHelper.STAGE_PRACTICAL.equals(stage)) {
            AutoAllocateResultDTO practicalAlloc =
                    examinerAllocationService.autoAllocatePracticalSession(sessionId);
            allocated += practicalAlloc.allocatedCount;
        }
        result.setAllocatedCount(allocated);
        return result;
    }

    @Override
    public AllocationActionResultDTO executeCandidateAction(AllocationCandidateActionRequest request) {
        AllocationActionResultDTO result = new AllocationActionResultDTO();
        if (request == null || request.getProfile() == null || request.getAction() == null) {
            result.setErrorMsg("KhÃ´ng xÃ¡c Ä‘á»‹nh Ä‘Æ°á»£c thao tÃ¡c phÃ¢n bá»•.");
            return result;
        }

        String action = request.getAction();
        ExamRegistrationDTO profile = request.getProfile();
        int regId = request.getRegId();
        int sessionId = request.getExamId();

        switch (action) {
            case "allocateRoom" -> handleAllocateRoom(result, request, profile, regId, sessionId);
            case "allocatePracticalRoom" -> handleAllocatePracticalRoom(result, request, profile, regId, sessionId);
            default -> result.setErrorMsg("Thao tÃ¡c khÃ´ng há»— trá»£: " + action);
        }

        result.setRedirectServletPath(AllocationStageHelper.inferServletPathFromAction(action));
        return result;
    }

    @Override
    public ExamRegistrationDTO findCandidate(int regId, int sessionId, List<ExamRegistrationDTO> queue) {
        if (queue != null) {
            for (ExamRegistrationDTO candidate : queue) {
                if (candidate.getId() == regId) {
                    return candidate;
                }
            }
        }
        if (sessionId > 0) {
            for (ExamRegistrationDTO candidate : regService.getCandidatesBySession(sessionId)) {
                if (candidate.getId() == regId) {
                    return candidate;
                }
            }
        }
        return null;
    }

    private void handleAllocateRoom(AllocationActionResultDTO result, AllocationCandidateActionRequest request,
            ExamRegistrationDTO profile, int regId, int sessionId) {
        int areaId = request.getAreaId();
        int enrollSessionId = sessionId > 0 ? sessionId : profile.getExamId();
        if (enrollSessionId <= 0) {
            result.setErrorMsg("KhÃ´ng xÃ¡c Ä‘á»‹nh Ä‘Æ°á»£c ká»³ thi Ä‘á»ƒ Ä‘á»•i phÃ²ng.");
            return;
        }

        ExamArea targetArea = areaQueryService.findById(areaId);
        if (targetArea == null
                || !examstaff.enums.SectionType.THEORY.getValue().equalsIgnoreCase(targetArea.getAreaType())) {
            result.setErrorMsg("PhÃ²ng thi khÃ´ng há»£p lá»‡ â€” chá»‰ dÃ¹ng phÃ²ng loáº¡i LÃ½ thuyáº¿t.");
            return;
        }

        Set<Integer> staffedTheoryAreas = ExaminerAssignmentRules.staffedTheoryAreaIds(
                examinerAllocationService.getAssignmentsBySessionId(enrollSessionId));
        if (!staffedTheoryAreas.contains(targetArea.getId())) {
            result.setErrorMsg("PhÃ²ng \"" + targetArea.getAreaName()
                    + "\" chÆ°a Ä‘Æ°á»£c phÃ¢n cÃ´ng giÃ¡m kháº£o trong ká»³ thi nÃ y.");
            return;
        }

        Integer currentAreaId = profile.getAllocatedAreaId();
        if (currentAreaId != null && currentAreaId == areaId) {
            return;
        }

        if (regService.updateAllocatedRoom(regId, enrollSessionId, targetArea.getId(), targetArea.getAreaName())) {
            profile.setAllocatedAreaId(targetArea.getId());
            profile.setAllocatedAreaName(targetArea.getAreaName());
            result.setAlertMsg("ÄÃ£ Ä‘á»•i phÃ²ng â†’ " + targetArea.getAreaName());
            result.setAuditAction("UPDATE ExamRegistrationDTO");
            result.setAuditDetails("Chuyá»ƒn phÃ²ng thi â†’ " + targetArea.getAreaName() + " cho SBD " + profile.getSbd());
            result.setAuditRecordId(regId);
        } else {
            result.setErrorMsg("KhÃ´ng lÆ°u Ä‘Æ°á»£c phÃ²ng thi cho SBD " + profile.getSbd()
                    + ". Kiá»ƒm tra Ä‘Äƒng kÃ½ ká»³ thi.");
        }
    }

    private void handleAllocatePracticalRoom(AllocationActionResultDTO result,
            AllocationCandidateActionRequest request, ExamRegistrationDTO profile, int regId, int sessionId) {
        int areaId = request.getAreaId();
        int enrollSessionId = sessionId > 0 ? sessionId : profile.getExamId();
        if (enrollSessionId <= 0) {
            result.setErrorMsg("KhÃ´ng xÃ¡c Ä‘á»‹nh Ä‘Æ°á»£c ká»³ thi Ä‘á»ƒ Ä‘á»•i sÃ¢n thi.");
            return;
        }

        ExamArea targetArea = areaQueryService.findById(areaId);
        if (targetArea == null || !ExaminerAssignmentRules.isPracticalAreaType(targetArea.getAreaType())) {
            result.setErrorMsg("SÃ¢n thi khÃ´ng há»£p lá»‡ â€” chá»‰ dÃ¹ng khu vá»±c loáº¡i Thá»±c hÃ nh.");
            return;
        }

        Set<Integer> staffedPracticalAreas = ExaminerAssignmentRules.staffedPracticalAreaIds(
                examinerAllocationService.getAssignmentsBySessionId(enrollSessionId));
        if (!staffedPracticalAreas.contains(targetArea.getId())) {
            result.setErrorMsg("SÃ¢n \"" + targetArea.getAreaName()
                    + "\" chÆ°a Ä‘Æ°á»£c phÃ¢n cÃ´ng giÃ¡m kháº£o trong ká»³ thi nÃ y.");
            return;
        }

        Integer currentAreaId = profile.getPracticalAllocatedAreaId();
        if (currentAreaId != null && currentAreaId == areaId) {
            return;
        }

        if (regService.updatePracticalAllocatedRoom(regId, enrollSessionId, targetArea.getId(),
                targetArea.getAreaName())) {
            profile.setPracticalAllocatedAreaId(targetArea.getId());
            profile.setPracticalAllocatedAreaName(targetArea.getAreaName());
            result.setAlertMsg("ÄÃ£ Ä‘á»•i sÃ¢n thi â†’ " + targetArea.getAreaName());
            result.setAuditAction("UPDATE ExamEnrollmentSection");
            result.setAuditDetails("Chuyá»ƒn sÃ¢n thá»±c hÃ nh â†’ " + targetArea.getAreaName()
                    + " cho SBD " + profile.getSbd());
            result.setAuditRecordId(regId);
        } else {
            result.setErrorMsg("KhÃ´ng lÆ°u Ä‘Æ°á»£c sÃ¢n thi cho SBD " + profile.getSbd() + ".");
        }
    }
}

