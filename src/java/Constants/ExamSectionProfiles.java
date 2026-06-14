package Constants;

/**
 * Maps assigned exam section names to examiner portal behaviour.
 */
public final class ExamSectionProfiles {

    private ExamSectionProfiles() {
    }

    public static ExamSectionType resolveType(String sectionName) {
        if (sectionName == null || sectionName.isBlank()) {
            return ExamSectionType.THEORY;
        }
        String normalized = sectionName.trim().toLowerCase();
        if (normalized.contains("lý thuyết")
                || normalized.contains("ly thuyet")
                || normalized.contains("theory")) {
            return ExamSectionType.THEORY;
        }
        return ExamSectionType.SCORE_BASED;
    }

    public static boolean isSidebarMenuDisabled(ExamSectionType type, String menuKey) {
        if (type != ExamSectionType.THEORY || menuKey == null) {
            return false;
        }
        return "nhap-diem".equals(menuKey) || "sua-ket-qua".equals(menuKey);
    }
}
