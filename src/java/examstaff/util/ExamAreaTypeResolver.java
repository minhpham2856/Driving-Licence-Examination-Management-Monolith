package examstaff.util;

import examstaff.enums.ExamSection;
import shared.enums.ExamAreaType;
import shared.model.ExamArea;

/** Phân loại khu vực thi (LT / TH) — helper thuần. */
public final class ExamAreaTypeResolver {

    public static final String PRACTICAL_AREA_TYPE = "Thực hành";

    private ExamAreaTypeResolver() {
    }

    /** Phòng dùng để phân giám khảo / phân thí sinh (bỏ khu hỗn hợp / thủ tục). */
    public static boolean isAssignableExamArea(ExamArea area) {
        return ExaminerAssignmentRules.isTheoryRoom(area)
                || ExaminerAssignmentRules.isPracticalRoom(area);
    }

    /** Giá trị AreaType “chuẩn” dùng khi query exact (schema Clean). */
    public static String theoryAreaTypeLabel() {
        return ExamSection.LY_THUYET.getDisplayName();
    }

    public static String practicalAreaTypeLabel() {
        return PRACTICAL_AREA_TYPE;
    }

    /** Alias schema SWP/DLEM: Phòng thi ≈ lý thuyết, Sân thi ≈ thực hành. */
    public static String theoryAreaTypeAlias() {
        return ExamAreaType.EXAM_ROOM.getValue();
    }

    public static String practicalAreaTypeAlias() {
        return ExamAreaType.EXAM_GROUND.getValue();
    }
}
