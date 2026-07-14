package examstaff.util;

import shared.enums.ExamSection;
import shared.model.ExamArea;

/** PhÃ¢n loáº¡i khu vá»±c thi (LT / TH) â€” helper thuáº§n. */
public final class ExamAreaTypeResolver {

    public static final String PRACTICAL_AREA_TYPE = "Thá»±c hÃ nh";

    private ExamAreaTypeResolver() {
    }

    /** PhÃ²ng dÃ¹ng Ä‘á»ƒ phÃ¢n giÃ¡m kháº£o / phÃ¢n thÃ­ sinh (bá» khu há»—n há»£p / thá»§ tá»¥c). */
    public static boolean isAssignableExamArea(ExamArea area) {
        if (area == null || area.getAreaType() == null) {
            return false;
        }
        String type = area.getAreaType().trim();
        return ExamSection.LY_THUYET.getValue().equalsIgnoreCase(type)
                || PRACTICAL_AREA_TYPE.equalsIgnoreCase(type);
    }
}


