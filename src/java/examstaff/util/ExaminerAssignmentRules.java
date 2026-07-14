package examstaff.util;

import examstaff.dto.ExaminerSlotDTO;
import examstaff.enums.ExamSection;
import shared.model.ExamArea;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Quy tắc phân công giám khảo / phòng thi — helper thuần. */
public final class ExaminerAssignmentRules {

    private ExaminerAssignmentRules() {
    }

    /**
     * Slot đã có giám khảo và phòng hợp lệ.
     *
     * @param slot slot phân công
     * @return {@code true} nếu examinerUserId &gt; 0 và areaId &gt; 0
     */
    public static boolean isStaffedSlot(ExaminerSlotDTO slot) {
        return slot != null && slot.getExaminerUserId() > 0 && slot.getAreaId() > 0;
    }

    /**
     * Chuỗi AreaType có nghĩa là phòng lý thuyết (các schema/alias).
     *
     * @param areaType giá trị AreaType thô
     * @return {@code true} nếu nhận diện là lý thuyết
     */
    public static boolean isTheoryAreaType(String areaType) {
        if (areaType == null || areaType.isBlank()) {
            return false;
        }
        String normalized = areaType.trim();
        String lower = normalized.toLowerCase();
        // Schema Clean: "Lý thuyết" — schema SWP/DLEM: "Phòng thi"
        return ExamSection.LY_THUYET.getDisplayName().equalsIgnoreCase(normalized)
                || ExamAreaTypeResolver.theoryAreaTypeAlias().equalsIgnoreCase(normalized)
                || lower.contains("theory")
                || lower.contains("lý thuyết")
                || lower.contains("ly thuyet")
                || lower.contains("phòng thi")
                || lower.contains("phong thi");
    }

    /**
     * Chuỗi AreaType có nghĩa là phòng/sân thực hành (các schema/alias).
     *
     * @param areaType giá trị AreaType thô
     * @return {@code true} nếu nhận diện là thực hành
     */
    public static boolean isPracticalAreaType(String areaType) {
        if (areaType == null || areaType.isBlank()) {
            return false;
        }
        String normalized = areaType.trim();
        if (ExamAreaTypeResolver.PRACTICAL_AREA_TYPE.equalsIgnoreCase(normalized)
                || ExamAreaTypeResolver.practicalAreaTypeAlias().equalsIgnoreCase(normalized)) {
            return true;
        }
        String lower = normalized.toLowerCase();
        // Schema Clean: "Thực hành" — schema SWP/DLEM: "Sân thi"
        return lower.contains("thực hành") || lower.contains("thuc hanh")
                || lower.contains("practical") || lower.contains("sa hình")
                || lower.contains("sa hinh") || lower.contains("layout")
                || lower.contains("sân thi") || lower.contains("san thi");
    }

    /**
     * Slot đã staff và thuộc phần lý thuyết (theo AreaType hoặc examTypeId).
     *
     * @param slot slot phân công
     * @return {@code true} nếu là slot lý thuyết
     */
    public static boolean isTheorySlot(ExaminerSlotDTO slot) {
        if (!isStaffedSlot(slot)) {
            return false;
        }
        if (isTheoryAreaType(slot.getAreaType())) {
            return true;
        }
        return slot.getExamTypeId() == ExamSection.LY_THUYET.getExamTypeId();
    }

    /**
     * Slot đã staff và thuộc phần thực hành (trong hình / trên đường).
     *
     * @param slot slot phân công
     * @return {@code true} nếu là slot thực hành
     */
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
     * Kiểm tra đủ phòng lý thuyết + thực hành có giám khảo trước khi bắt đầu kỳ.
     *
     * @param assignments danh sách slot phân công
     * @return thông báo lỗi tiếng Việt, hoặc {@code null} nếu đủ coverage
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

    /**
     * Các phòng lý thuyết đã có ít nhất một giám khảo trong kỳ.
     *
     * @param assignments danh sách slot
     * @return tập areaId phòng LT đã staff
     */
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

    /**
     * {@link ExamArea} có AreaType lý thuyết.
     *
     * @param room khu vực
     * @return {@code true} nếu là phòng LT
     */
    public static boolean isTheoryRoom(ExamArea room) {
        return room != null && isTheoryAreaType(room.getAreaType());
    }

    /**
     * Các phòng/sân thực hành đã có ít nhất một giám khảo.
     *
     * @param assignments danh sách slot
     * @return tập areaId phòng TH đã staff
     */
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

    /**
     * {@link ExamArea} có AreaType thực hành.
     *
     * @param room khu vực
     * @return {@code true} nếu là phòng/sân TH
     */
    public static boolean isPracticalRoom(ExamArea room) {
        return room != null && isPracticalAreaType(room.getAreaType());
    }

    /**
     * Lọc phòng lý thuyết trong {@code rooms} đã có giám khảo theo {@code staffedAreaIds}.
     *
     * @param rooms          danh sách phòng
     * @param staffedAreaIds tập areaId đã staff
     * @return danh sách phòng LT có staff (có thể rỗng)
     */
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

    /**
     * Lọc phòng/sân thực hành trong {@code rooms} đã có giám khảo theo {@code staffedAreaIds}.
     *
     * @param rooms          danh sách phòng
     * @param staffedAreaIds tập areaId đã staff
     * @return danh sách phòng TH có staff (có thể rỗng)
     */
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
