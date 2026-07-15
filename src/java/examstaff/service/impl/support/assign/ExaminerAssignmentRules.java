package examstaff.service.impl.support.assign;

import examstaff.dto.ExaminerSlotDTO;
import examstaff.enums.ExamSection;
import examstaff.util.ExamAreaTypeResolver;
import shared.model.ExamArea;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Quy tắc phân công sát hạch viên / phòng thi — helper thuần, không HTTP. */
public final class ExaminerAssignmentRules {

    private ExaminerAssignmentRules() {
    }

    /**
     * Slot đã gán sát hạch viên hợp lệ (có userId và areaId dương).
     *
     * @param slot slot phân công
     * @return {@code true} nếu đã staffed
     */
    public static boolean isStaffedSlot(ExaminerSlotDTO slot) {
        return slot != null && slot.getExaminerUserId() > 0 && slot.getAreaId() > 0;
    }

    /**
     * Nhận diện loại khu vực lý thuyết (schema Clean / SWP alias / tiếng Anh).
     *
     * @param areaType chuỗi loại khu vực
     * @return {@code true} nếu là phòng LT
     */
    public static boolean isTheoryAreaType(String areaType) {
        // validate
        if (areaType == null || areaType.isBlank()) {
            return false;
        }
        String normalized = areaType.trim();
        String lower = normalized.toLowerCase();
        // result: Schema Clean: "Lý thuyết" — schema SWP/DLEM: "Phòng thi"
        return ExamSection.LY_THUYET.getDisplayName().equalsIgnoreCase(normalized)
                || ExamAreaTypeResolver.theoryAreaTypeAlias().equalsIgnoreCase(normalized)
                || lower.contains("theory")
                || lower.contains("lý thuyết")
                || lower.contains("ly thuyet")
                || lower.contains("phòng thi")
                || lower.contains("phong thi");
    }

    /**
     * Nhận diện loại khu vực thực hành / sân thi (schema Clean / SWP alias).
     *
     * @param areaType chuỗi loại khu vực
     * @return {@code true} nếu là sân/phòng TH
     */
    public static boolean isPracticalAreaType(String areaType) {
        // validate
        if (areaType == null || areaType.isBlank()) {
            return false;
        }
        String normalized = areaType.trim();
        if (ExamAreaTypeResolver.PRACTICAL_AREA_TYPE.equalsIgnoreCase(normalized)
                || ExamAreaTypeResolver.practicalAreaTypeAlias().equalsIgnoreCase(normalized)) {
            return true;
        }
        String lower = normalized.toLowerCase();
        // result: Schema Clean: "Thực hành" — schema SWP/DLEM: "Sân thi"
        return lower.contains("thực hành") || lower.contains("thuc hanh")
                || lower.contains("practical") || lower.contains("sa hình")
                || lower.contains("sa hinh") || lower.contains("layout")
                || lower.contains("sân thi") || lower.contains("san thi");
    }

    /**
     * Slot lý thuyết đã có sát hạch viên (theo areaType hoặc examTypeId).
     *
     * @param slot slot phân công
     * @return {@code true} nếu là slot LT đã staffed
     */
    public static boolean isTheorySlot(ExaminerSlotDTO slot) {
        // validate
        if (!isStaffedSlot(slot)) {
            return false;
        }
        // result: ưu tiên areaType; fallback examTypeId LT
        if (isTheoryAreaType(slot.getAreaType())) {
            return true;
        }
        return slot.getExamTypeId() == ExamSection.LY_THUYET.getExamTypeId();
    }

    /**
     * Slot thực hành đã có sát hạch viên (theo areaType hoặc examTypeId trong hình/đường).
     *
     * @param slot slot phân công
     * @return {@code true} nếu là slot TH đã staffed
     */
    public static boolean isPracticalSlot(ExaminerSlotDTO slot) {
        // validate
        if (!isStaffedSlot(slot)) {
            return false;
        }
        // result: ưu tiên areaType; fallback examTypeId TH trong hình / trên đường
        if (isPracticalAreaType(slot.getAreaType())) {
            return true;
        }
        int typeId = slot.getExamTypeId();
        return typeId == ExamSection.THUC_HANH_TRONG_HINH.getExamTypeId()
                || typeId == ExamSection.THUC_HANH_TREN_DUONG.getExamTypeId();
    }

    /**
     * Kiểm tra coverage tối thiểu trước khi bắt đầu kỳ (cần cả LT và TH có SHV).
     *
     * @param assignments danh sách slot hiện có của kỳ
     * @return thông báo lỗi tiếng Việt, hoặc {@code null} nếu đủ phòng lý thuyết + thực hành có sát hạch viên
     */
    public static String validateStartCoverage(List<ExaminerSlotDTO> assignments) {
        // validate: chưa có phân công nào
        if (assignments == null || assignments.isEmpty()) {
            return "Chưa phân công sát hạch viên. Vào mục \"Phân bổ sát hạch viên\" trước khi bắt đầu kỳ thi.";
        }
        // load cờ coverage LT / TH
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
        // result: thông báo thiếu coverage
        if (!hasTheory && !hasPractical) {
            return "Chưa phân công sát hạch viên vào phòng lý thuyết và phòng thực hành. "
                    + "Vào mục \"Phân bổ sát hạch viên\" trước khi bắt đầu kỳ thi.";
        }
        if (!hasTheory) {
            return "Chưa phân công sát hạch viên cho phòng thi lý thuyết. "
                    + "Vào mục \"Phân bổ sát hạch viên\" trước khi bắt đầu kỳ thi.";
        }
        if (!hasPractical) {
            return "Chưa phân công sát hạch viên cho phòng/khu thi thực hành. "
                    + "Vào mục \"Phân bổ sát hạch viên\" trước khi bắt đầu kỳ thi.";
        }
        return null;
    }

    /**
     * Các phòng lý thuyết đã có ít nhất một sát hạch viên trong kỳ.
     *
     * @param assignments danh sách slot
     * @return tập ExamAreaId LT đã staffed
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
     * Kiểm tra {@link ExamArea} có phải phòng lý thuyết không.
     *
     * @param room khu vực thi
     * @return {@code true} nếu là phòng LT
     */
    public static boolean isTheoryRoom(ExamArea room) {
        return room != null && isTheoryAreaType(room.getAreaType());
    }

    /**
     * Các sân/phòng thực hành đã có ít nhất một sát hạch viên trong kỳ.
     *
     * @param assignments danh sách slot
     * @return tập ExamAreaId TH đã staffed
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
     * Kiểm tra {@link ExamArea} có phải sân/phòng thực hành không.
     *
     * @param room khu vực thi
     * @return {@code true} nếu là sân TH
     */
    public static boolean isPracticalRoom(ExamArea room) {
        return room != null && isPracticalAreaType(room.getAreaType());
    }

    /**
     * Phòng dùng để phân sát hạch viên / phân thí sinh (bỏ khu hỗn hợp / thủ tục).
     *
     * @param area khu vực thi
     * @return {@code true} nếu là LT hoặc TH
     */
    public static boolean isAssignableExamArea(ExamArea area) {
        return isTheoryRoom(area) || isPracticalRoom(area);
    }

    /**
     * Lọc phòng lý thuyết đã có sát hạch viên.
     *
     * @param rooms          danh sách phòng nguồn
     * @param staffedAreaIds tập areaId đã staffed LT
     * @return phòng LT nằm trong tập staffed (rỗng nếu thiếu đầu vào)
     */
    public static List<ExamArea> filterTheoryRoomsWithStaff(List<ExamArea> rooms,
            Set<Integer> staffedAreaIds) {
        // validate
        if (rooms == null || rooms.isEmpty() || staffedAreaIds == null || staffedAreaIds.isEmpty()) {
            return List.of();
        }
        // result
        return rooms.stream()
                .filter(ExaminerAssignmentRules::isTheoryRoom)
                .filter(room -> staffedAreaIds.contains(room.getId()))
                .toList();
    }

    /**
     * Lọc sân/phòng thực hành đã có sát hạch viên.
     *
     * @param rooms          danh sách phòng nguồn
     * @param staffedAreaIds tập areaId đã staffed TH
     * @return sân TH nằm trong tập staffed (rỗng nếu thiếu đầu vào)
     */
    public static List<ExamArea> filterPracticalRoomsWithStaff(List<ExamArea> rooms,
            Set<Integer> staffedAreaIds) {
        // validate
        if (rooms == null || rooms.isEmpty() || staffedAreaIds == null || staffedAreaIds.isEmpty()) {
            return List.of();
        }
        // result
        return rooms.stream()
                .filter(ExaminerAssignmentRules::isPracticalRoom)
                .filter(room -> staffedAreaIds.contains(room.getId()))
                .toList();
    }
}
