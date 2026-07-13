package examstaff.service.impl;

import examstaff.dto.ExaminerSlotDTO;
import examstaff.util.ExamStaffExamRules;
import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.UserDTO;
import examstaff.dto.ExaminerAllocationActionResultDTO;
import examstaff.dto.ExaminerAllocationViewDTO;
import shared.model.ExamArea;
import examstaff.service.ExaminerAllocationDeskService;
import examstaff.service.ExaminerAllocationService;
import examstaff.util.ExamAreaTypeResolver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ExaminerAllocationDeskServiceImpl implements ExaminerAllocationDeskService {

    private final ExaminerAllocationService allocationService = new ExaminerAllocationServiceImpl();

    @Override
    public ExaminerAllocationViewDTO buildAllocationView(int examId, int fallbackExamId,
            List<ExamSummaryDTO> allExams) {
        ExaminerAllocationViewDTO view = new ExaminerAllocationViewDTO();
        List<ExamSummaryDTO> dayExams = ExamStaffExamRules.examsForExam(allExams, examId);

        List<ExaminerSlotDTO> dayAssignments = new ArrayList<>();
        Set<Integer> busyIds = new HashSet<>();
        for (ExamSummaryDTO ds : dayExams) {
            List<ExaminerSlotDTO> slots = allocationService.getAssignmentsByExamId(ds.getId());
            dayAssignments.addAll(slots);
            for (ExaminerSlotDTO slot : slots) {
                if (slot.getExaminerUserId() > 0) {
                    busyIds.add(slot.getExaminerUserId());
                }
            }
        }
        view.setDayAssignments(dayAssignments);

        List<UserDTO> allExaminers = allocationService.getActiveExaminers();
        List<UserDTO> availableExaminers = new ArrayList<>();
        List<UserDTO> busyExaminers = new ArrayList<>();
        for (UserDTO ex : allExaminers) {
            if (busyIds.contains(ex.getId())) {
                busyExaminers.add(ex);
            } else {
                availableExaminers.add(ex);
            }
        }
        view.setAllExaminers(allExaminers);
        view.setAvailableExaminers(availableExaminers);
        view.setBusyExaminers(busyExaminers);

        List<Map<String, Object>> areaAssignOptions = new ArrayList<>();
        for (ExamSummaryDTO ds : dayExams) {
            List<ExamArea> areas = allocationService.getAvailableAreasForExam(ds.getId()).stream()
                    .filter(ExamAreaTypeResolver::isAssignableExamArea)
                    .toList();
            for (ExamArea area : areas) {
                Map<String, Object> opt = new HashMap<>();
                opt.put("examId", ds.getId());
                opt.put("examName", ds.getExamName());
                opt.put("areaId", area.getId());
                opt.put("areaName", area.getAreaName());
                opt.put("areaType", area.getAreaType());
                areaAssignOptions.add(opt);
            }
        }
        view.setAreaAssignOptions(areaAssignOptions);
        return view;
    }

    @Override
    public Map<Integer, UserDTO> buildExaminerMap() {
        Map<Integer, UserDTO> map = new HashMap<>();
        for (UserDTO u : allocationService.getActiveExaminers()) {
            map.put(u.getId(), u);
        }
        return map;
    }

    @Override
    public ExaminerAllocationActionResultDTO assignExaminer(int targetExamId, int areaId,
            int examinerUserId, int staffId) {
        ExaminerAllocationActionResultDTO result = new ExaminerAllocationActionResultDTO();
        Map<Integer, UserDTO> examinerMap = buildExaminerMap();

        ExamSummaryDTO targetExam = allocationService.getExamById(targetExamId);
        ExamArea area = allocationService.getAreaById(areaId);
        UserDTO examiner = examinerMap.get(examinerUserId);

        if (targetExam == null || area == null || examiner == null) {
            result.setErrorMsg("Dữ liệu phân công không hợp lệ.");
            return result;
        }

        if (!ExamAreaTypeResolver.isAssignableExamArea(area)) {
            result.setErrorMsg("Chỉ phân công giám khảo vào phòng lý thuyết hoặc sân thực hành.");
            return result;
        }

        ExaminerSlotDTO slot = new ExaminerSlotDTO();
        slot.setExamId(targetExamId);
        slot.setAreaId(areaId);
        slot.setExamTypeId(targetExam.getExamTypeId());
        slot.setExaminerUserId(examinerUserId);
        slot.setAssignedBy(staffId);
        slot.setAreaName(area.getAreaName());
        slot.setAreaType(area.getAreaType());
        slot.setExamTypeName(targetExam.getExamTypeName());
        slot.setExamName(targetExam.getExamName());
        slot.setExaminerName(resolveExaminerName(examiner));
        slot.setExaminerUsername(examiner.getUsername());

        boolean ok = allocationService.assignExaminer(slot);
        if (ok) {
            result.setSuccess(true);
            result.setAlertMsg("Đã phân công giám khảo " + slot.getExaminerName()
                    + " vào " + area.getAreaName() + ".");
            result.setAuditAction("ASSIGN Examiner");
            result.setAuditDetails(formatAssignAuditDetails(slot.getExaminerName(), area.getAreaName(),
                    targetExam.getExamName()));
        } else {
            result.setErrorMsg(
                    "Giám khảo đã được phân công ở phòng khác trong cùng kỳ thi. Gỡ phân công cũ trước khi gán mới.");
        }
        return result;
    }

    @Override
    public ExaminerAllocationActionResultDTO removeExaminer(String slotKey) {
        ExaminerAllocationActionResultDTO result = new ExaminerAllocationActionResultDTO();
        if (slotKey == null || slotKey.isEmpty()) {
            result.setErrorMsg("Không xác định được phân công cần gỡ.");
            return result;
        }

        ExaminerSlotDTO existing = findSlotByKey(slotKey);
        boolean ok = allocationService.removeAssignment(slotKey);
        if (ok) {
            result.setSuccess(true);
            result.setAlertMsg(existing != null
                    ? "Đã gỡ phân công giám khảo " + resolveSlotExaminerLabel(existing) + "."
                    : "Đã gỡ phân công giám khảo.");
            result.setAuditAction("REMOVE Examiner");
            result.setAuditDetails(formatRemoveAuditDetails(existing));
        } else {
            result.setErrorMsg("Gỡ phân công thất bại. Vui lòng thử lại.");
        }
        return result;
    }

    private ExaminerSlotDTO findSlotByKey(String slotKey) {
        String[] parts = slotKey.split(":");
        if (parts.length < 3) {
            return null;
        }
        try {
            int examId = Integer.parseInt(parts[0].trim());
            int areaId = Integer.parseInt(parts[1].trim());
            int examinerUserId = Integer.parseInt(parts[2].trim());
            if (examId <= 0) {
                return null;
            }
            for (ExaminerSlotDTO slot : allocationService.getAssignmentsByExamId(examId)) {
                if (slot.getAreaId() == areaId && slot.getExaminerUserId() == examinerUserId) {
                    return slot;
                }
            }
        } catch (NumberFormatException ignored) {
            return null;
        }
        return null;
    }

    private static String formatAssignAuditDetails(String examinerName, String areaName, String examName) {
        StringBuilder details = new StringBuilder("Phân công giám khảo ");
        details.append(blankToDash(examinerName));
        details.append(" vào ").append(blankToDash(areaName));
        appendExamSuffix(details, examName);
        return details.toString();
    }

    private static String formatRemoveAuditDetails(ExaminerSlotDTO slot) {
        if (slot == null) {
            return "Gỡ phân công giám khảo.";
        }
        StringBuilder details = new StringBuilder("Gỡ phân công giám khảo ");
        details.append(resolveSlotExaminerLabel(slot));
        details.append(" khỏi ").append(blankToDash(slot.getAreaName()));
        appendExamSuffix(details, slot.getExamName());
        return details.toString();
    }

    private static void appendExamSuffix(StringBuilder details, String examName) {
        if (examName != null && !examName.isBlank()) {
            details.append(" — kỳ thi ").append(examName.trim());
        }
    }

    private static String resolveSlotExaminerLabel(ExaminerSlotDTO slot) {
        if (slot.getExaminerName() != null && !slot.getExaminerName().isBlank()) {
            return slot.getExaminerName().trim();
        }
        if (slot.getExaminerUsername() != null && !slot.getExaminerUsername().isBlank()) {
            return slot.getExaminerUsername().trim();
        }
        return "userId=" + slot.getExaminerUserId();
    }

    private static String blankToDash(String value) {
        return value == null || value.isBlank() ? "—" : value.trim();
    }

    private static String resolveExaminerName(UserDTO examiner) {
        if (examiner.getProfile() != null && examiner.getProfile().getFullName() != null
                && !examiner.getProfile().getFullName().isBlank()) {
            return examiner.getProfile().getFullName().trim();
        }
        if (examiner.getUsername() != null && !examiner.getUsername().isBlank()) {
            return examiner.getUsername().trim();
        }
        return "userId=" + examiner.getId();
    }
}
