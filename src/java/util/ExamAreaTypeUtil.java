package util;

import dto.SessionDTO;
import enums.ExamSection;
import model.ExamArea;

public final class ExamAreaTypeUtil {

    public static final String PRACTICAL_AREA_TYPE = "Thực hành";

    private ExamAreaTypeUtil() {
    }

    public static String resolveAreaType(SessionDTO session) {
        if (session == null) {
            return ExamSection.LY_THUYET.getDisplayName();
        }
        String sectionName = session.getExamTypeName();
        if (sectionName != null && !sectionName.isBlank()) {
            if (ExamSection.LY_THUYET.matchesSectionName(sectionName)
                    || sectionName.toLowerCase().contains("theory")) {
                return ExamSection.LY_THUYET.getDisplayName();
            }
            if (ExamSection.THUC_HANH_TRONG_HINH.matchesSectionName(sectionName)
                    || ExamSection.THUC_HANH_TREN_DUONG.matchesSectionName(sectionName)
                    || sectionName.toLowerCase().contains("practical")
                    || sectionName.toLowerCase().contains("road")) {
                return PRACTICAL_AREA_TYPE;
            }
        }
        int examTypeId = session.getExamTypeId();
        if (examTypeId == ExamSection.THUC_HANH_TRONG_HINH.getExamTypeId()
                || examTypeId == ExamSection.THUC_HANH_TREN_DUONG.getExamTypeId()) {
            return PRACTICAL_AREA_TYPE;
        }
        return ExamSection.LY_THUYET.getDisplayName();
    }

    public static boolean areaMatchesSession(ExamArea area, SessionDTO session) {
        if (area == null || area.getAreaType() == null || session == null) {
            return false;
        }
        return area.getAreaType().trim().equalsIgnoreCase(resolveAreaType(session).trim());
    }
}
