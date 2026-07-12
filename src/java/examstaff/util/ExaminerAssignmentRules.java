package examstaff.util;

import examstaff.dto.ExaminerSlotDTO;
import shared.enums.ExamSection;
import shared.model.ExamArea;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Quy táº¯c phÃ¢n cÃ´ng giÃ¡m kháº£o / phÃ²ng thi â€” helper thuáº§n, khÃ´ng HTTP. */
public final class ExaminerAssignmentRules {

    private ExaminerAssignmentRules() {
    }

    public static boolean isStaffedSlot(ExaminerSlotDTO slot) {
        return slot != null && slot.getExaminerUserId() > 0 && slot.getAreaId() > 0;
    }

    public static boolean isTheoryAreaType(String areaType) {
        if (areaType == null || areaType.isBlank()) {
            return false;
        }
        String normalized = areaType.trim();
        return ExamSection.LY_THUYET.getDisplayName().equalsIgnoreCase(normalized)
                || normalized.toLowerCase().contains("theory")
                || normalized.toLowerCase().contains("lÃ½ thuyáº¿t")
                || normalized.toLowerCase().contains("ly thuyet");
    }

    public static boolean isPracticalAreaType(String areaType) {
        if (areaType == null || areaType.isBlank()) {
            return false;
        }
        String normalized = areaType.trim();
        if (examstaff.util.ExamAreaTypeResolver.PRACTICAL_AREA_TYPE.equalsIgnoreCase(normalized)) {
            return true;
        }
        String lower = normalized.toLowerCase();
        return lower.contains("thá»±c hÃ nh") || lower.contains("thuc hanh")
                || lower.contains("practical") || lower.contains("sa hÃ¬nh")
                || lower.contains("sa hinh") || lower.contains("layout");
    }

    public static boolean isTheorySlot(ExaminerSlotDTO slot) {
        if (!isStaffedSlot(slot)) {
            return false;
        }
        if (isTheoryAreaType(slot.getAreaType())) {
            return true;
        }
        return slot.getExamTypeId() == ExamSection.LY_THUYET.getExamTypeId();
    }

    public static boolean isPracticalSlot(ExaminerSlotDTO slot) {
        if (!isStaffedSlot(slot)) {
            return false;
        }
        if (isPracticalAreaType(slot.getAreaType())) {
            return true;
        }
        int typeId = slot.getExamTypeId();
        return typeId == ExamSection.THUC_HANH_TRONG_HINH.getExamTypeId()
                || typeId == ExamSection.THUC_HANH_TREN_DUONG.getExamTypeId();
    }

    /**
     * @return thÃ´ng bÃ¡o lá»—i tiáº¿ng Viá»‡t, hoáº·c {@code null} náº¿u Ä‘á»§ phÃ²ng lÃ½ thuyáº¿t + thá»±c hÃ nh cÃ³ giÃ¡m kháº£o.
     */
    public static String validateStartCoverage(List<ExaminerSlotDTO> assignments) {
        if (assignments == null || assignments.isEmpty()) {
            return "ChÆ°a phÃ¢n cÃ´ng giÃ¡m kháº£o. VÃ o má»¥c \"PhÃ¢n bá»• giÃ¡m kháº£o\" trÆ°á»›c khi báº¯t Ä‘áº§u ká»³ thi.";
        }
        boolean hasTheory = false;
        boolean hasPractical = false;
        for (ExaminerSlotDTO slot : assignments) {
            if (isTheorySlot(slot)) {
                hasTheory = true;
            }
            if (isPracticalSlot(slot)) {
                hasPractical = true;
            }
        }
        if (!hasTheory && !hasPractical) {
            return "ChÆ°a phÃ¢n cÃ´ng giÃ¡m kháº£o vÃ o phÃ²ng lÃ½ thuyáº¿t vÃ  phÃ²ng thá»±c hÃ nh. "
                    + "VÃ o má»¥c \"PhÃ¢n bá»• giÃ¡m kháº£o\" trÆ°á»›c khi báº¯t Ä‘áº§u ká»³ thi.";
        }
        if (!hasTheory) {
            return "ChÆ°a phÃ¢n cÃ´ng giÃ¡m kháº£o cho phÃ²ng thi lÃ½ thuyáº¿t. "
                    + "VÃ o má»¥c \"PhÃ¢n bá»• giÃ¡m kháº£o\" trÆ°á»›c khi báº¯t Ä‘áº§u ká»³ thi.";
        }
        if (!hasPractical) {
            return "ChÆ°a phÃ¢n cÃ´ng giÃ¡m kháº£o cho phÃ²ng/khu thi thá»±c hÃ nh. "
                    + "VÃ o má»¥c \"PhÃ¢n bá»• giÃ¡m kháº£o\" trÆ°á»›c khi báº¯t Ä‘áº§u ká»³ thi.";
        }
        return null;
    }

    /** CÃ¡c phÃ²ng lÃ½ thuyáº¿t Ä‘Ã£ cÃ³ Ã­t nháº¥t má»™t giÃ¡m kháº£o trong ká»³. */
    public static Set<Integer> staffedTheoryAreaIds(List<ExaminerSlotDTO> assignments) {
        Set<Integer> ids = new HashSet<>();
        if (assignments == null) {
            return ids;
        }
        for (ExaminerSlotDTO slot : assignments) {
            if (isTheorySlot(slot)) {
                ids.add(slot.getAreaId());
            }
        }
        return ids;
    }

    public static boolean isTheoryRoom(ExamArea room) {
        return room != null && isTheoryAreaType(room.getAreaType());
    }

    public static Set<Integer> staffedPracticalAreaIds(List<ExaminerSlotDTO> assignments) {
        Set<Integer> ids = new HashSet<>();
        if (assignments == null) {
            return ids;
        }
        for (ExaminerSlotDTO slot : assignments) {
            if (isPracticalSlot(slot)) {
                ids.add(slot.getAreaId());
            }
        }
        return ids;
    }

    public static boolean isPracticalRoom(ExamArea room) {
        return room != null && isPracticalAreaType(room.getAreaType());
    }

    public static List<ExamArea> filterTheoryRoomsWithStaff(List<ExamArea> rooms,
            Set<Integer> staffedAreaIds) {
        if (rooms == null || rooms.isEmpty() || staffedAreaIds == null || staffedAreaIds.isEmpty()) {
            return List.of();
        }
        return rooms.stream()
                .filter(ExaminerAssignmentRules::isTheoryRoom)
                .filter(room -> staffedAreaIds.contains(room.getId()))
                .toList();
    }

    public static List<ExamArea> filterPracticalRoomsWithStaff(List<ExamArea> rooms,
            Set<Integer> staffedAreaIds) {
        if (rooms == null || rooms.isEmpty() || staffedAreaIds == null || staffedAreaIds.isEmpty()) {
            return List.of();
        }
        return rooms.stream()
                .filter(ExaminerAssignmentRules::isPracticalRoom)
                .filter(room -> staffedAreaIds.contains(room.getId()))
                .toList();
    }
}

