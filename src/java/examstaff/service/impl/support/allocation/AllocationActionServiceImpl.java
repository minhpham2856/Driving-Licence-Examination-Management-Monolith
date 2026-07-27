package examstaff.service.impl.support.allocation;
import examstaff.service.impl.support.assign.ExaminerAssignmentRules;
import examstaff.service.impl.support.assign.ExaminerAllocationServiceImpl;

import examstaff.service.RegistrationService;
import examstaff.service.impl.RegistrationServiceImpl;

import examstaff.dto.ExamRegistrationDTO;
import examstaff.dto.AllocationActionResultDTO;
import examstaff.dto.AllocationCandidateActionRequest;
import shared.model.ExamArea;

import java.util.List;
import java.util.Set;

/**
 * Điều phối auto-allocate và thao tác đổi phòng/sân cho một thí sinh.
 *
 * Hai lối vào:
 * - autoAllocateOnOverview — batch: overview/theory → auto LT;
 *       overview/practical → auto TH (qua ExaminerAllocationServiceImpl)
 * - executeCandidateAction — một thí sinh: allocateRoom (LT)
 *       hoặc allocatePracticalRoom (TH) theo areaId form
 * <p>Trước khi gán, kiểm tra phòng/sân đã có sát hạch viên (ExaminerAssignmentRules).
 * Persist qua RegistrationService → ExamEnrollmentSectionSupport.
 */
public class AllocationActionServiceImpl {

    private final RegistrationService regService = new RegistrationServiceImpl();
    private final ExamAreaQueryServiceImpl areaQueryService = new ExamAreaQueryServiceImpl();
    private final ExaminerAllocationServiceImpl examinerAllocationService = new ExaminerAllocationServiceImpl();

    /**
     * Tự động phân phòng trên màn overview theo giai đoạn.
     * @param examId mã kỳ thi
     * @param stage  giai đoạn phân phòng (overview / theory / practical, …)
     * @return kết quả thao tác auto-allocate (tổng số đã phân)
     */
    public AllocationActionResultDTO autoAllocateOnOverview(int examId, String stage) {
        AllocationActionResultDTO result = new AllocationActionResultDTO();
        // validate
        if (examId <= 0) {
            return result;
        }
        int allocated = 0;
        String error = null;
        // mutate: auto LT khi overview hoặc theory
        if (AllocationStageHelper.STAGE_OVERVIEW.equals(stage)
                || AllocationStageHelper.STAGE_THEORY.equals(stage)) {
            AllocationActionResultDTO theoryAlloc = examinerAllocationService.autoAllocateExam(examId);
            allocated += theoryAlloc.getAllocatedCount();
            if (theoryAlloc.getErrorMsg() != null && !theoryAlloc.getErrorMsg().isBlank()) {
                error = theoryAlloc.getErrorMsg();
            }
        }
        // mutate: auto TH khi overview hoặc practical
        if (AllocationStageHelper.STAGE_OVERVIEW.equals(stage)
                || AllocationStageHelper.STAGE_PRACTICAL.equals(stage)) {
            AllocationActionResultDTO practicalAlloc =
                    examinerAllocationService.autoAllocatePracticalExam(examId);
            allocated += practicalAlloc.getAllocatedCount();
            if (error == null && practicalAlloc.getErrorMsg() != null
                    && !practicalAlloc.getErrorMsg().isBlank()) {
                error = practicalAlloc.getErrorMsg();
            }
        }
        // result
        result.setAllocatedCount(allocated);
        if (allocated > 0) {
            result.setAlertMsg("Đã tự động phân phòng/sân cho " + allocated + " thí sinh.");
        } else if (error != null) {
            result.setErrorMsg(error);
        } else {
            result.setAlertMsg("Không có thí sinh mới cần tự động phân phòng/sân.");
        }
        return result;
    }

    /**
     * Thực hiện một thao tác phân phòng trên một thí sinh (gán/đổi phòng LT hoặc sân TH).
     * @param request yêu cầu thao tác kèm ngữ cảnh (profile, action, areaId, …)
     * @return kết quả thao tác (alert / error / audit / redirect)
     */
    public AllocationActionResultDTO executeCandidateAction(AllocationCandidateActionRequest request) {
        AllocationActionResultDTO result = new AllocationActionResultDTO();
        // validate
        if (request == null || request.getProfile() == null || request.getAction() == null) {
            result.setErrorMsg("Không xác định được thao tác phân bổ.");
            return result;
        }

        // load ngữ cảnh từ request
        String action = request.getAction();
        ExamRegistrationDTO profile = request.getProfile();
        int regId = request.getRegId();
        int examId = request.getExamId();

        // mutate: ủy quyền handler theo action
        switch (action) {
            case "allocateRoom" -> handleAllocateRoom(result, request, profile, regId, examId);
            case "allocatePracticalRoom" -> handleAllocatePracticalRoom(result, request, profile, regId, examId);
            default -> result.setErrorMsg("Thao tác không hỗ trợ: " + action);
        }

        // result: đường redirect theo action
        result.setRedirectServletPath(AllocationStageHelper.inferServletPathFromAction(action));
        return result;
    }

    /**
     * Tìm thí sinh trong hàng đợi theo mã đăng ký và kỳ thi.
     * @param regId  mã đăng ký
     * @param examId mã kỳ thi (dùng khi queue không có)
     * @param queue  hàng đợi nguồn (ưu tiên tra trước)
     * @return hồ sơ khớp, hoặc null
     */
    public ExamRegistrationDTO findCandidate(int regId, int examId, List<ExamRegistrationDTO> queue) {
        // load từ queue trước
        if (queue != null) {
            for (ExamRegistrationDTO candidate : queue) {
                if (candidate.getId() == regId) {
                    return candidate;
                }
            }
        }
        // load fallback từ registration service theo kỳ
        if (examId > 0) {
            for (ExamRegistrationDTO candidate : regService.getCandidatesByExam(examId)) {
                if (candidate.getId() == regId) {
                    return candidate;
                }
            }
        }
        return null;
    }

    /**
     * Đổi / gán phòng lý thuyết cho thí sinh sau khi kiểm tra sát hạch viên đã phân công.
     * @param result  DTO kết quả (mutate)
     * @param request request gốc (areaId, …)
     * @param profile hồ sơ thí sinh (mutate khi thành công)
     * @param regId   mã đăng ký
     * @param examId  mã kỳ thi (0 → lấy từ profile)
     */
    private void handleAllocateRoom(AllocationActionResultDTO result, AllocationCandidateActionRequest request,
            ExamRegistrationDTO profile, int regId, int examId) {
        // validate kỳ thi
        int areaId = request.getAreaId();
        int enrollExamId = examId > 0 ? examId : profile.getExamId();
        if (enrollExamId <= 0) {
            result.setErrorMsg("Không xác định được kỳ thi để đổi phòng.");
            return;
        }

        // load + validate loại phòng LT
        ExamArea targetArea = areaQueryService.findById(areaId);
        if (targetArea == null || !ExaminerAssignmentRules.isTheoryAreaType(targetArea.getAreaType())) {
            result.setErrorMsg("Phòng thi không hợp lệ - chỉ dùng phòng loại Lý thuyết / Phòng thi.");
            return;
        }

        // validate: phòng phải đã có sát hạch viên
        Set<Integer> staffedTheoryAreas = ExaminerAssignmentRules.staffedTheoryAreaIds(
                examinerAllocationService.getAssignmentsByExamId(enrollExamId));
        if (!staffedTheoryAreas.contains(targetArea.getId())) {
            result.setErrorMsg("Phòng \"" + targetArea.getAreaName()
                    + "\" chưa được phân công sát hạch viên trong kỳ thi này.");
            return;
        }

        // validate: đã đúng phòng → no-op
        Integer currentAreaId = profile.getAllocatedAreaId();
        if (currentAreaId != null && currentAreaId == areaId) {
            return;
        }

        // mutate DB + DTO; result alert/audit hoặc error
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

    /**
     * Đổi / gán sân thực hành cho thí sinh sau khi kiểm tra sát hạch viên đã phân công.
     * @param result  DTO kết quả (mutate)
     * @param request request gốc (areaId, …)
     * @param profile hồ sơ thí sinh (mutate khi thành công)
     * @param regId   mã đăng ký
     * @param examId  mã kỳ thi (0 → lấy từ profile)
     */
    private void handleAllocatePracticalRoom(AllocationActionResultDTO result,
            AllocationCandidateActionRequest request, ExamRegistrationDTO profile, int regId, int examId) {
        // validate kỳ thi
        int areaId = request.getAreaId();
        int enrollExamId = examId > 0 ? examId : profile.getExamId();
        if (enrollExamId <= 0) {
            result.setErrorMsg("Không xác định được kỳ thi để đổi sân thi.");
            return;
        }

        // load + validate loại sân TH
        ExamArea targetArea = areaQueryService.findById(areaId);
        if (targetArea == null || !ExaminerAssignmentRules.isPracticalAreaType(targetArea.getAreaType())) {
            result.setErrorMsg("Sân thi không hợp lệ - chỉ dùng khu vực loại Thực hành.");
            return;
        }

        // validate: sân phải đã có sát hạch viên
        Set<Integer> staffedPracticalAreas = ExaminerAssignmentRules.staffedPracticalAreaIds(
                examinerAllocationService.getAssignmentsByExamId(enrollExamId));
        if (!staffedPracticalAreas.contains(targetArea.getId())) {
            result.setErrorMsg("Sân \"" + targetArea.getAreaName()
                    + "\" chưa được phân công sát hạch viên trong kỳ thi này.");
            return;
        }

        // validate: đã đúng sân → no-op
        Integer currentAreaId = profile.getPracticalAllocatedAreaId();
        if (currentAreaId != null && currentAreaId == areaId) {
            return;
        }

        // mutate DB + DTO; result alert/audit hoặc error
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
