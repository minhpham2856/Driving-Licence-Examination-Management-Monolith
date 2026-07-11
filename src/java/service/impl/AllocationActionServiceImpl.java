package service.impl;

import dto.AutoAllocateResultDTO;
import dto.exam.ExamRegistrationDTO;
import dto.examstaff.AllocationActionResultDTO;
import dto.examstaff.AllocationCandidateActionRequest;
import dto.examstaff.AllocationScoreResultDTO;
import enums.ExamSection;
import model.ExamArea;
import service.AllocationActionService;
import service.AllocationRegistrationService;
import service.AllocationScoreService;
import service.ExamAreaQueryService;
import service.ExamRegistrationService;
import service.ExaminerAllocationService;
import util.examstaff.AllocationStageHelper;
import util.examstaff.ExaminerAssignmentRules;

import java.util.List;
import java.util.Set;

public class AllocationActionServiceImpl implements AllocationActionService {

    private final ExamRegistrationService regService = new ExamRegistrationServiceImpl();
    private final ExamAreaQueryService areaQueryService = new ExamAreaQueryServiceImpl();
    private final AllocationScoreService allocationScoreService = new AllocationScoreServiceImpl();
    private final AllocationRegistrationService allocationRegistrationService = new AllocationRegistrationServiceImpl();
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
    public AllocationActionResultDTO executeAutoAllocate(int sessionId) {
        AllocationActionResultDTO result = new AllocationActionResultDTO();
        AutoAllocateResultDTO allocResult = examinerAllocationService.autoAllocateSession(sessionId);

        if (allocResult.errorMsg != null) {
            result.setErrorMsg(allocResult.errorMsg);
        } else if (allocResult.warningMsg != null) {
            result.setWarningMsg(allocResult.warningMsg);
        }

        result.setAllocatedCount(allocResult.allocatedCount);
        if (allocResult.allocatedCount > 0) {
            result.setAlertMsg("Tự động phân bổ thành công " + allocResult.allocatedCount
                    + " thí sinh vào phòng thi lý thuyết!");
            result.setAuditAction("ALLOCATE Candidates");
            result.setAuditDetails("Tự động phân bổ " + allocResult.allocatedCount
                    + " thí sinh vào phòng thi lý thuyết.");
        } else if (allocResult.errorMsg == null) {
            result.setWarningMsg("Không có thí sinh nào đã hoàn thành thủ tục hồ sơ cần phân phòng!");
        }

        result.setRedirectServletPath("/views/staff/examstaff/allocation-theory");
        return result;
    }

    @Override
    public AllocationActionResultDTO executeCandidateAction(AllocationCandidateActionRequest request) {
        AllocationActionResultDTO result = new AllocationActionResultDTO();
        if (request == null || request.getProfile() == null || request.getAction() == null) {
            result.setErrorMsg("Không xác định được thao tác phân bổ.");
            return result;
        }

        String action = request.getAction();
        ExamRegistrationDTO profile = request.getProfile();
        int regId = request.getRegId();
        int sessionId = request.getSessionId();

        switch (action) {
            case "checkin" -> handleCheckin(profile, regId);
            case "callCandidate" -> handleCallCandidate(result, profile);
            case "allocateRoom" -> handleAllocateRoom(result, request, profile, regId, sessionId);
            case "allocatePracticalRoom" -> handleAllocatePracticalRoom(result, request, profile, regId, sessionId);
            case "submitTheoryScore" -> handleTheoryScore(result, profile, regId, sessionId, request.getScore());
            case "submitPracticalScore" -> handlePracticalScore(result, profile, regId, sessionId, request.getScore());
            case "quickComplete" -> handleQuickComplete(result, profile, regId, sessionId);
            default -> result.setErrorMsg("Thao tác không hỗ trợ: " + action);
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

    private void handleCheckin(ExamRegistrationDTO profile, int regId) {
        if (regService.updatePresent(regId, true)) {
            profile.setIsPresent(true);
        }
    }

    private void handleCallCandidate(AllocationActionResultDTO result, ExamRegistrationDTO profile) {
        result.setSyncCallBoard(true);
        result.setCallingSbd(profile.getSbd());
    }

    private void handleAllocateRoom(AllocationActionResultDTO result, AllocationCandidateActionRequest request,
            ExamRegistrationDTO profile, int regId, int sessionId) {
        int areaId = request.getAreaId();
        int enrollSessionId = sessionId > 0 ? sessionId : profile.getExamSessionId();
        if (enrollSessionId <= 0) {
            result.setErrorMsg("Không xác định được kỳ thi để đổi phòng.");
            return;
        }

        ExamArea targetArea = areaQueryService.findById(areaId);
        if (targetArea == null
                || !ExamSection.LY_THUYET.getDisplayName().equalsIgnoreCase(targetArea.getAreaType())) {
            result.setErrorMsg("Phòng thi không hợp lệ — chỉ dùng phòng loại Lý thuyết.");
            return;
        }

        Set<Integer> staffedTheoryAreas = ExaminerAssignmentRules.staffedTheoryAreaIds(
                examinerAllocationService.getAssignmentsBySessionId(enrollSessionId));
        if (!staffedTheoryAreas.contains(targetArea.getId())) {
            result.setErrorMsg("Phòng \"" + targetArea.getAreaName()
                    + "\" chưa được phân công giám khảo trong kỳ thi này.");
            return;
        }

        Integer currentAreaId = profile.getAllocatedAreaId();
        if (currentAreaId != null && currentAreaId == areaId) {
            return;
        }

        if (regService.updateAllocatedRoom(regId, enrollSessionId, targetArea.getId(), targetArea.getAreaName())) {
            profile.setAllocatedAreaId(targetArea.getId());
            profile.setAllocatedAreaName(targetArea.getAreaName());
            result.setAlertMsg("Đã đổi phòng → " + targetArea.getAreaName());
            result.setAuditAction("UPDATE ExamRegistrationDTO");
            result.setAuditDetails("Chuyển phòng thi → " + targetArea.getAreaName() + " cho SBD " + profile.getSbd());
            result.setAuditRecordId(regId);
        } else {
            result.setErrorMsg("Không lưu được phòng thi cho SBD " + profile.getSbd()
                    + ". Kiểm tra đăng ký kỳ thi.");
        }
    }

    private void handleAllocatePracticalRoom(AllocationActionResultDTO result,
            AllocationCandidateActionRequest request, ExamRegistrationDTO profile, int regId, int sessionId) {
        int areaId = request.getAreaId();
        int enrollSessionId = sessionId > 0 ? sessionId : profile.getExamSessionId();
        if (enrollSessionId <= 0) {
            result.setErrorMsg("Không xác định được kỳ thi để đổi sân thi.");
            return;
        }

        ExamArea targetArea = areaQueryService.findById(areaId);
        if (targetArea == null || !ExaminerAssignmentRules.isPracticalAreaType(targetArea.getAreaType())) {
            result.setErrorMsg("Sân thi không hợp lệ — chỉ dùng khu vực loại Thực hành.");
            return;
        }

        Set<Integer> staffedPracticalAreas = ExaminerAssignmentRules.staffedPracticalAreaIds(
                examinerAllocationService.getAssignmentsBySessionId(enrollSessionId));
        if (!staffedPracticalAreas.contains(targetArea.getId())) {
            result.setErrorMsg("Sân \"" + targetArea.getAreaName()
                    + "\" chưa được phân công giám khảo trong kỳ thi này.");
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
            result.setAlertMsg("Đã đổi sân thi → " + targetArea.getAreaName());
            result.setAuditAction("UPDATE ExamEnrollmentSection");
            result.setAuditDetails("Chuyển sân thực hành → " + targetArea.getAreaName()
                    + " cho SBD " + profile.getSbd());
            result.setAuditRecordId(regId);
        } else {
            result.setErrorMsg("Không lưu được sân thi cho SBD " + profile.getSbd() + ".");
        }
    }

    private void handleTheoryScore(AllocationActionResultDTO result, ExamRegistrationDTO profile,
            int regId, int sessionId, int score) {
        AllocationScoreResultDTO scoreResult =
                allocationScoreService.submitTheoryScore(profile, sessionId, score);
        applyScoreResult(result, scoreResult, regId);
        if (!scoreResult.isSaved()
                || !"passed".equalsIgnoreCase(scoreResult.getPassedFlag())
                || profile == null
                || profile.skipsPractical()) {
            return;
        }
        int enrollSessionId = profile.getExamSessionId() > 0 ? profile.getExamSessionId() : sessionId;
        AutoAllocateResultDTO allocResult = examinerAllocationService.autoAllocatePracticalCandidate(
                enrollSessionId, regId);
        if (allocResult != null && allocResult.allocatedCount > 0) {
            result.setAllocatedCount(allocResult.allocatedCount);
            result.setAlertMsg("Đã đỗ lý thuyết và tự động phân sân thực hành cho SBD "
                    + profile.getSbd() + ".");
        } else if (allocResult != null && allocResult.errorMsg != null && result.getErrorMsg() == null) {
            result.setWarningMsg(allocResult.errorMsg);
        }
    }

    private void handlePracticalScore(AllocationActionResultDTO result, ExamRegistrationDTO profile,
            int regId, int sessionId, int score) {
        applyScoreResult(result, allocationScoreService.submitPracticalScore(profile, sessionId, score), regId);
    }

    private void handleQuickComplete(AllocationActionResultDTO result, ExamRegistrationDTO profile,
            int regId, int sessionId) {
        allocationRegistrationService.quickCompleteProcedure(profile, regId);
        int enrollSessionId = profile.getExamSessionId() > 0 ? profile.getExamSessionId() : sessionId;
        AutoAllocateResultDTO allocResult = examinerAllocationService.autoAllocateCandidate(
                enrollSessionId, regId);
        if (allocResult != null && allocResult.allocatedCount > 0) {
            result.setAlertMsg("Hoàn thành thủ tục và tự động phân phòng cho SBD " + profile.getSbd());
        }
        result.setAuditAction("UPDATE ExamRegistrationDTO");
        result.setAuditDetails("Hoàn thành nhanh thủ tục (FaceID + lệ phí) cho SBD " + profile.getSbd());
    }

    private static void applyScoreResult(AllocationActionResultDTO result,
            AllocationScoreResultDTO scoreResult, int regId) {
        if (scoreResult.getErrorMessage() != null) {
            result.setErrorMsg(scoreResult.getErrorMessage());
        } else if (scoreResult.isSaved()) {
            result.setAuditAction("UPDATE ExamScore");
            result.setAuditDetails(scoreResult.getAuditDetail());
            result.setAuditRecordId(regId);
        }
    }
}
