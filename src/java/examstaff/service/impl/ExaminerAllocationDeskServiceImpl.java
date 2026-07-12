package examstaff.service.impl;

import examstaff.dto.ExaminerSlotDTO;
import examstaff.util.ExamStaffSessionRules;
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
    public ExaminerAllocationViewDTO buildAllocationView(int examId, int sessionId,
            List<ExamSummaryDTO> allSessions) {
        ExaminerAllocationViewDTO view = new ExaminerAllocationViewDTO();
        List<ExamSummaryDTO> daySessions = ExamStaffSessionRules.sessionsForExam(allSessions, examId);

        List<ExaminerSlotDTO> dayAssignments = new ArrayList<>();
        Set<Integer> busyIds = new HashSet<>();
        for (ExamSummaryDTO ds : daySessions) {
            List<ExaminerSlotDTO> slots = allocationService.getAssignmentsBySessionId(ds.getId());
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
        for (ExamSummaryDTO ds : daySessions) {
            List<ExamArea> areas = allocationService.getAvailableAreasForSession(ds.getId()).stream()
                    .filter(ExamAreaTypeResolver::isAssignableExamArea)
                    .toList();
            for (ExamArea area : areas) {
                Map<String, Object> opt = new HashMap<>();
                opt.put("examId", ds.getId());
                opt.put("examName", ds.getSessionName());
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
    public ExaminerAllocationActionResultDTO assignExaminer(int targetSessionId, int areaId,
            int examinerUserId, int staffId) {
        ExaminerAllocationActionResultDTO result = new ExaminerAllocationActionResultDTO();
        Map<Integer, UserDTO> examinerMap = buildExaminerMap();

        ExamSummaryDTO targetSession = allocationService.getSessionById(targetSessionId);
        ExamArea area = allocationService.getAreaById(areaId);
        UserDTO examiner = examinerMap.get(examinerUserId);

        if (targetSession == null || area == null || examiner == null) {
            result.setErrorMsg("Dá»¯ liá»‡u phÃ¢n cÃ´ng khÃ´ng há»£p lá»‡.");
            return result;
        }

        if (!ExamAreaTypeResolver.isAssignableExamArea(area)) {
            result.setErrorMsg("Chá»‰ phÃ¢n cÃ´ng giÃ¡m kháº£o vÃ o phÃ²ng lÃ½ thuyáº¿t hoáº·c sÃ¢n thá»±c hÃ nh.");
            return result;
        }

        ExaminerSlotDTO slot = new ExaminerSlotDTO();
        slot.setExamId(targetSessionId);
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
            result.setAlertMsg("ÄÃ£ phÃ¢n cÃ´ng giÃ¡m kháº£o " + slot.getExaminerName()
                    + " vÃ o " + area.getAreaName() + ".");
            result.setAuditAction("ASSIGN Examiner");
            result.setAuditDetails(formatAssignAuditDetails(slot.getExaminerName(), area.getAreaName(),
                    targetSession.getSessionName()));
        } else {
            result.setErrorMsg(
                    "GiÃ¡m kháº£o Ä‘Ã£ Ä‘Æ°á»£c phÃ¢n cÃ´ng á»Ÿ phÃ²ng khÃ¡c trong cÃ¹ng ká»³ thi. Gá»¡ phÃ¢n cÃ´ng cÅ© trÆ°á»›c khi gÃ¡n má»›i.");
        }
        return result;
    }

    @Override
    public ExaminerAllocationActionResultDTO removeExaminer(String slotKey) {
        ExaminerAllocationActionResultDTO result = new ExaminerAllocationActionResultDTO();
        if (slotKey == null || slotKey.isEmpty()) {
            result.setErrorMsg("KhÃ´ng xÃ¡c Ä‘á»‹nh Ä‘Æ°á»£c phÃ¢n cÃ´ng cáº§n gá»¡.");
            return result;
        }

        ExaminerSlotDTO existing = findSlotByKey(slotKey);
        boolean ok = allocationService.removeAssignment(slotKey);
        if (ok) {
            result.setSuccess(true);
            result.setAlertMsg(existing != null
                    ? "ÄÃ£ gá»¡ phÃ¢n cÃ´ng giÃ¡m kháº£o " + resolveSlotExaminerLabel(existing) + "."
                    : "ÄÃ£ gá»¡ phÃ¢n cÃ´ng giÃ¡m kháº£o.");
            result.setAuditAction("REMOVE Examiner");
            result.setAuditDetails(formatRemoveAuditDetails(existing));
        } else {
            result.setErrorMsg("Gá»¡ phÃ¢n cÃ´ng tháº¥t báº¡i. Vui lÃ²ng thá»­ láº¡i.");
        }
        return result;
    }

    private ExaminerSlotDTO findSlotByKey(String slotKey) {
        String[] parts = slotKey.split(":");
        if (parts.length < 3) {
            return null;
        }
        try {
            int sessionId = Integer.parseInt(parts[0].trim());
            int areaId = Integer.parseInt(parts[1].trim());
            int examinerUserId = Integer.parseInt(parts[2].trim());
            if (sessionId <= 0) {
                return null;
            }
            for (ExaminerSlotDTO slot : allocationService.getAssignmentsBySessionId(sessionId)) {
                if (slot.getAreaId() == areaId && slot.getExaminerUserId() == examinerUserId) {
                    return slot;
                }
            }
        } catch (NumberFormatException ignored) {
            return null;
        }
        return null;
    }

    private static String formatAssignAuditDetails(String examinerName, String areaName, String sessionName) {
        StringBuilder details = new StringBuilder("PhÃ¢n cÃ´ng giÃ¡m kháº£o ");
        details.append(blankToDash(examinerName));
        details.append(" vÃ o ").append(blankToDash(areaName));
        appendSessionSuffix(details, sessionName);
        return details.toString();
    }

    private static String formatRemoveAuditDetails(ExaminerSlotDTO slot) {
        if (slot == null) {
            return "Gá»¡ phÃ¢n cÃ´ng giÃ¡m kháº£o.";
        }
        StringBuilder details = new StringBuilder("Gá»¡ phÃ¢n cÃ´ng giÃ¡m kháº£o ");
        details.append(resolveSlotExaminerLabel(slot));
        details.append(" khá»i ").append(blankToDash(slot.getAreaName()));
        appendSessionSuffix(details, slot.getSessionName());
        return details.toString();
    }

    private static void appendSessionSuffix(StringBuilder details, String sessionName) {
        if (sessionName != null && !sessionName.isBlank()) {
            details.append(" â€” ká»³ thi ").append(sessionName.trim());
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
        return value == null || value.isBlank() ? "â€”" : value.trim();
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

