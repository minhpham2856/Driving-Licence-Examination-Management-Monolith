package examstaff.util;

import examstaff.enums.ExamSection;
import shared.enums.ExamAreaType;
import shared.model.ExamArea;

/** Phân loại khu vực thi (LT / TH) — helper thuần. */
public final class ExamAreaTypeResolver {

    public static final String PRACTICAL_AREA_TYPE = "Thực hành";

    private ExamAreaTypeResolver() {
    }

    /**
     * Phòng dùng để phân giám khảo / phân thí sinh (bỏ khu hỗn hợp / thủ tục).
     *
     * @param area khu vực thi
     * @return {@code true} nếu là phòng LT hoặc TH có thể phân công
     */
    public static boolean isAssignableExamArea(ExamArea area) {
        return ExaminerAssignmentRules.isTheoryRoom(area)
                || ExaminerAssignmentRules.isPracticalRoom(area);
    }

    /**
     * Nhãn AreaType “chuẩn” cho lý thuyết (schema Clean).
     *
     * @return chuỗi hiển thị lý thuyết
     */
    public static String theoryAreaTypeLabel() {
        return ExamSection.LY_THUYET.getDisplayName();
    }

    /**
     * Nhãn AreaType “chuẩn” cho thực hành (schema Clean).
     *
     * @return chuỗi hiển thị thực hành
     */
    public static String practicalAreaTypeLabel() {
        return PRACTICAL_AREA_TYPE;
    }

    /**
     * Alias schema SWP/DLEM: Phòng thi ≈ lý thuyết.
     *
     * @return giá trị {@link ExamAreaType#EXAM_ROOM}
     */
    public static String theoryAreaTypeAlias() {
        return ExamAreaType.EXAM_ROOM.getValue();
    }

    /**
     * Alias schema SWP/DLEM: Sân thi ≈ thực hành.
     *
     * @return giá trị {@link ExamAreaType#EXAM_GROUND}
     */
    public static String practicalAreaTypeAlias() {
        return ExamAreaType.EXAM_GROUND.getValue();
    }
}
