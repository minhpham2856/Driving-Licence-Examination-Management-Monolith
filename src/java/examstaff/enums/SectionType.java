package examstaff.enums;

public enum SectionType {
    THEORY("Lý thuyết"),
    LAYOUT("Thực hành trong hình"),
    SCORE_BASED("Thực hành tính điểm");

    private final String value;

    private SectionType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static SectionType fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (SectionType type : values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        return null;
    }

    public static SectionType resolveSectionType(String sectionName) {
        if (sectionName == null || sectionName.isBlank()) {
            return THEORY;
        }
        String normalized = sectionName.trim().toLowerCase();
        if (normalized.contains("ly thuyet") || normalized.contains("theory")) {
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