package examstaff.util;

import examstaff.enums.ExamSection;
import examstaff.model.ExamArea;

/** Phân loại khu vực thi (LT / TH) — helper thuần. */
public final class ExamAreaTypeResolver {

    public static final String PRACTICAL_AREA_TYPE = "Thực hành";

    private ExamAreaTypeResolver() {
    }

    /** Phòng dùng để phân giám khảo / phân thí sinh (bỏ khu hỗn hợp / thủ tục). */
    public static boolean isAssignableExamArea(ExamArea area) {
        if (area == null || area.getAreaType() == null) {
            return false;
        }
        String type = area.getAreaType().trim();
        return ExamSection.LY_THUYET.getDisplayName().equalsIgnoreCase(type)
                || PRACTICAL_AREA_TYPE.equalsIgnoreCase(type);
    }
}
