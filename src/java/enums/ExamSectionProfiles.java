package enums;

/**
 * Maps assigned exam section names to examiner portal behaviour.
 */
public final class ExamSectionProfiles {

    private ExamSectionProfiles() {
    }

    public static SectionType resolveType(String sectionName) {
        if (sectionName == null || sectionName.isBlank()) {
            return SectionType.THEORY;
        }
        String normalized = sectionName.trim().toLowerCase();
        if (normalized.contains("lý thuyết")
                || normalized.contains("ly thuyet")
                || normalized.contains("theory")) {
            return SectionType.THEORY;
        }
        return SectionType.SCORE_BASED;
    }

    public static boolean isSidebarMenuDisabled(SectionType type, String menuKey) {
        if (type != SectionType.THEORY || menuKey == null) {
            return false;
        }
        return "nhap-diem".equals(menuKey) || "sua-ket-qua".equals(menuKey);
    }
}
