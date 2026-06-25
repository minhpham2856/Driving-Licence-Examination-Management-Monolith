package enums;

public enum SectionType {
    THEORY,
    SCORE_BASED;

    public static SectionType resolveSectionType(String sectionName) {
        if (sectionName == null || sectionName.isBlank()) {
            return THEORY;
        }
        String normalized = sectionName.trim().toLowerCase();
        if (normalized.contains("lý thuyết") || normalized.contains("ly thuyet") || normalized.contains("theory")) {
            return THEORY;
        }
        return SCORE_BASED;
    }

    public static boolean isSidebarMenuDisabled(SectionType type, String menuKey) {
        if (type != THEORY || menuKey == null) {
            return false;
        }
        return "score-entry".equals(menuKey) || "result-details".equals(menuKey);
    }
}
