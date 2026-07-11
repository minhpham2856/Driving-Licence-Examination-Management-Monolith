package service.impl;

import dto.ExaminerSlotDTO;
import util.examstaff.ExamStaffSessionRules;
import dto.SessionDTO;
import dto.UserDTO;
import dto.examstaff.ExaminerAllocationActionResultDTO;
import dto.examstaff.ExaminerAllocationViewDTO;
import model.ExamArea;
import model.ExamDevice;
import service.ExaminerAllocationDeskService;
import service.ExaminerAllocationService;
import util.ExamAreaTypeResolver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ExaminerAllocationDeskServiceImpl implements ExaminerAllocationDeskService {

    private final ExaminerAllocationService allocationService = new ExaminerAllocationServiceImpl();

    @Override
    public ExaminerAllocationViewDTO buildAllocationView(int examId, int sessionId,
            List<SessionDTO> allSessions) {
        ExaminerAllocationViewDTO view = new ExaminerAllocationViewDTO();
        List<SessionDTO> daySessions = ExamStaffSessionRules.sessionsForExam(allSessions, examId);
        view.setDaySessions(daySessions);

        List<ExaminerSlotDTO> dayAssignments = new ArrayList<>();
        Set<Integer> busyIds = new HashSet<>();
        for (SessionDTO ds : daySessions) {
            List<ExaminerSlotDTO> slots = allocationService.getAssignmentsBySessionId(ds.getId());
            dayAssignments.addAll(slots);
            for (ExaminerSlotDTO slot : slots) {
                if (slot.getExaminerUserId() > 0) {
                    busyIds.add(slot.getExaminerUserId());
                }
            }
        }
        view.setDayAssignments(dayAssignments);

        if (sessionId > 0) {
            view.setSessionAssignments(allocationService.getAssignmentsBySessionId(sessionId));
        } else {
            view.setSessionAssignments(List.of());
        }

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

        List<ExamArea> sessionAreas = sessionId > 0
                ? allocationService.getAvailableAreasForSession(sessionId)
                : List.of();
        view.setSessionAreas(sessionAreas);

        Map<Integer, List<ExamDevice>> devicesByArea = new HashMap<>();
        for (ExamArea area : sessionAreas) {
            devicesByArea.put(area.getId(), allocationService.getDevicesByAreaId(area.getId()));
        }
        view.setDevicesByArea(devicesByArea);

        Map<String, List<ExamArea>> areasBySession = new HashMap<>();
        List<Map<String, Object>> areaAssignOptions = new ArrayList<>();
        for (SessionDTO ds : daySessions) {
            List<ExamArea> areas = allocationService.getAvailableAreasForSession(ds.getId()).stream()
                    .filter(ExamAreaTypeResolver::isAssignableExamArea)
                    .toList();
            areasBySession.put(String.valueOf(ds.getId()), areas);
            for (ExamArea area : areas) {
                Map<String, Object> opt = new HashMap<>();
                opt.put("sessionId", ds.getId());
                opt.put("sessionName", ds.getSessionName());
                opt.put("areaId", area.getId());
                opt.put("areaName", area.getAreaName());
                opt.put("areaType", area.getAreaType());
                areaAssignOptions.add(opt);
            }
        }
        view.setAreasBySession(areasBySession);
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
    public ExaminerAllocationActionResultDTO assignExaminer(int targetSessionId, int areaId,
            int examinerUserId, int staffId) {
        ExaminerAllocationActionResultDTO result = new ExaminerAllocationActionResultDTO();
        Map<Integer, UserDTO> examinerMap = buildExaminerMap();

        SessionDTO targetSession = allocationService.getSessionById(targetSessionId);
        ExamArea area = allocationService.getAreaById(areaId);
        UserDTO examiner = examinerMap.get(examinerUserId);

        if (targetSession == null || area == null || examiner == null) {
            result.setErrorMsg("Dữ liệu phân công không hợp lệ.");
            return result;
        }

        if (!ExamAreaTypeResolver.isAssignableExamArea(area)) {
            result.setErrorMsg("Chỉ phân công giám khảo vào phòng lý thuyết hoặc sân thực hành.");
            return result;
        }

        ExaminerSlotDTO slot = new ExaminerSlotDTO();
        slot.setExamSessionId(targetSessionId);
        slot.setAreaId(areaId);
        slot.setExamTypeId(targetSession.getExamTypeId());
        slot.setExaminerUserId(examinerUserId);
        slot.setAssignedBy(staffId);
        slot.setAreaName(area.getAreaName());
        slot.setAreaType(area.getAreaType());
        slot.setExamTypeName(targetSession.getExamTypeName());
        slot.setSessionName(targetSession.getSessionName());
        slot.setExaminerName(resolveExaminerName(examiner));
        slot.setExaminerUsername(examiner.getUsername());

        boolean ok = allocationService.assignExaminer(slot);
        if (ok) {
            result.setSuccess(true);
            result.setAlertMsg("Đã phân công giám khảo vào phòng " + area.getAreaName() + ".");
            result.setAuditAction("ASSIGN Examiner");
            result.setAuditDetails("Phân công giám khảo userId=" + examinerUserId
                    + " kỳ " + targetSessionId + ", phòng " + area.getAreaName());
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

        boolean ok = allocationService.removeAssignment(slotKey);
        if (ok) {
            result.setSuccess(true);
            result.setAlertMsg("Đã gỡ phân công giám khảo.");
            result.setAuditAction("REMOVE Examiner");
            result.setAuditDetails("Gỡ phân công slot=" + slotKey);
        } else {
            result.setErrorMsg("Gỡ phân công thất bại. Vui lòng thử lại.");
        }
        return result;
    }

    private static String resolveExaminerName(UserDTO examiner) {
        if (examiner.getProfile() != null && examiner.getProfile().getFullName() != null
                && !examiner.getProfile().getFullName().isBlank()) {
            return examiner.getProfile().getFullName();
        }
        return examiner.getUsername();
    }
}
