package examstaff.util;

import examstaff.dto.ExaminerSlotDTO;
import examstaff.enums.ExamSection;
import examstaff.model.ExamArea;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Quy tắc phân công giám khảo / phòng thi — helper thuần, không HTTP. */
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
                || normalized.toLowerCase().contains("lý thuyết")
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
        return lower.contains("thực hành") || lower.contains("thuc hanh")
                || lower.contains("practical") || lower.contains("sa hình")
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
     * @return thông báo lỗi tiếng Việt, hoặc {@code null} nếu đủ phòng lý thuyết + thực hành có giám khảo.
     */
    public static String validateStartCoverage(List<ExaminerSlotDTO> assignments) {
        if (assignments == null || assignments.isEmpty()) {
            return "Chưa phân công giám khảo. Vào mục \"Phân bổ giám khảo\" trước khi bắt đầu kỳ thi.";
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
            return "Chưa phân công giám khảo vào phòng lý thuyết và phòng thực hành. "
                    + "Vào mục \"Phân bổ giám khảo\" trước khi bắt đầu kỳ thi.";
        }
        if (!hasTheory) {
            return "Chưa phân công giám khảo cho phòng thi lý thuyết. "
                    + "Vào mục \"Phân bổ giám khảo\" trước khi bắt đầu kỳ thi.";
        }
        if (!hasPractical) {
            return "Chưa phân công giám khảo cho phòng/khu thi thực hành. "
                    + "Vào mục \"Phân bổ giám khảo\" trước khi bắt đầu kỳ thi.";
        }
        return null;
    }

    /** Các phòng lý thuyết đã có ít nhất một giám khảo trong kỳ. */
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
