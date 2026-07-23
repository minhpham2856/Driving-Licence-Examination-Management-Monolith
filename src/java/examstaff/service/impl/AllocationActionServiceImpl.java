package examstaff.service.impl;

import examstaff.dto.AutoAllocateResultDTO;
import examstaff.dto.exam.ExamRegistrationDTO;
import examstaff.dto.AllocationActionResultDTO;
import examstaff.dto.AllocationCandidateActionRequest;
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
    public AllocationActionResultDTO autoAllocateOnOverview(int examId, String stage) {
        AllocationActionResultDTO result = new AllocationActionResultDTO();
        if (examId <= 0) {
            return result;
        }
        int allocated = 0;
        if (AllocationStageHelper.STAGE_OVERVIEW.equals(stage)
                || AllocationStageHelper.STAGE_THEORY.equals(stage)) {
            AutoAllocateResultDTO theoryAlloc = examinerAllocationService.autoAllocateExam(examId);
            allocated += theoryAlloc.allocatedCount;
        }
        if (AllocationStageHelper.STAGE_OVERVIEW.equals(stage)
                || AllocationStageHelper.STAGE_PRACTICAL.equals(stage)) {
            AutoAllocateResultDTO practicalAlloc =
                    examinerAllocationService.autoAllocatePracticalExam(examId);
            allocated += practicalAlloc.allocatedCount;
        }
        result.setAllocatedCount(allocated);
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
        int examId = request.getExamId();

        switch (action) {
            case "allocateRoom" -> handleAllocateRoom(result, request, profile, regId, examId);
            case "allocatePracticalRoom" -> handleAllocatePracticalRoom(result, request, profile, regId, examId);
            default -> result.setErrorMsg("Thao tác không hỗ trợ: " + action);
        }

        result.setRedirectServletPath(AllocationStageHelper.inferServletPathFromAction(action));
        return result;
    }

    @Override
    public ExamRegistrationDTO findCandidate(int regId, int examId, List<ExamRegistrationDTO> queue) {
        if (queue != null) {
            for (ExamRegistrationDTO candidate : queue) {
                if (candidate.getId() == regId) {
                    return candidate;
                }
            }
        }
        if (examId > 0) {
            for (ExamRegistrationDTO candidate : regService.getCandidatesByExam(examId)) {
                if (candidate.getId() == regId) {
                    return candidate;
                }
            }
        }
        return null;
    }

    private void handleAllocateRoom(AllocationActionResultDTO result, AllocationCandidateActionRequest request,
            ExamRegistrationDTO profile, int regId, int examId) {
        int areaId = request.getAreaId();
        int enrollExamId = examId > 0 ? examId : profile.getExamId();
        if (enrollExamId <= 0) {
            result.setErrorMsg("Không xác định được kỳ thi để đổi phòng.");
            return;
        }

        ExamArea targetArea = areaQueryService.findById(areaId);
        if (targetArea == null || !ExaminerAssignmentRules.isTheoryAreaType(targetArea.getAreaType())) {
            result.setErrorMsg("Phòng thi không hợp lệ - chỉ dùng phòng loại Lý thuyết / Phòng thi.");
            return;
        }

        Set<Integer> staffedTheoryAreas = ExaminerAssignmentRules.staffedTheoryAreaIds(
                examinerAllocationService.getAssignmentsByExamId(enrollExamId));
        if (!staffedTheoryAreas.contains(targetArea.getId())) {
            result.setErrorMsg("Phòng \"" + targetArea.getAreaName()
                    + "\" chưa được phân công sát hạch viên trong kỳ thi này.");
            return;
        }

        Integer currentAreaId = profile.getAllocatedAreaId();
        if (currentAreaId != null && currentAreaId == areaId) {
            return;
        }

        if (regService.updateAllocatedRoom(regId, enrollExamId, targetArea.getId(), targetArea.getAreaName())) {
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
            AllocationCandidateActionRequest request, ExamRegistrationDTO profile, int regId, int examId) {
        int areaId = request.getAreaId();
        int enrollExamId = examId > 0 ? examId : profile.getExamId();
        if (enrollExamId <= 0) {
            result.setErrorMsg("Không xác định được kỳ thi để đổi sân thi.");
            return;
        }

        ExamArea targetArea = areaQueryService.findById(areaId);
        if (targetArea == null || !ExaminerAssignmentRules.isPracticalAreaType(targetArea.getAreaType())) {
            result.setErrorMsg("Sân thi không hợp lệ - chỉ dùng khu vực loại Thực hành.");
            return;
        }

        Set<Integer> staffedPracticalAreas = ExaminerAssignmentRules.staffedPracticalAreaIds(
                examinerAllocationService.getAssignmentsByExamId(enrollExamId));
        if (!staffedPracticalAreas.contains(targetArea.getId())) {
            result.setErrorMsg("Sân \"" + targetArea.getAreaName()
                    + "\" chưa được phân công sát hạch viên trong kỳ thi này.");
            return;
        }

        Integer currentAreaId = profile.getPracticalAllocatedAreaId();
        if (currentAreaId != null && currentAreaId == areaId) {
            return;
        }

        if (regService.updatePracticalAllocatedRoom(regId, enrollExamId, targetArea.getId(),
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
}
