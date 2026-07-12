package shared.enums;
public enum ExamSection {
    LY_THUYET("LÃ½ thuyáº¿t", 1),
    THUC_HANH_TRONG_HINH("Thá»±c hÃ nh trong hÃ¬nh", 2),
    THUC_HANH_TREN_DUONG("Thá»±c hÃ nh trÃªn Ä‘Æ°á»ng", 4);
    private final String displayName;
    private final int examTypeId;
    ExamSection(String displayName, int examTypeId) {
        this.displayName = displayName;
        this.examTypeId = examTypeId;
    }
    public String getDisplayName() {
        return displayName;
    }
    public int getExamTypeId() {
        return examTypeId;
    }
    public boolean matchesSectionName(String sectionName) {
        if (sectionName == null || sectionName.isBlank()) {
            return false;
        }
        return sectionName.trim().contains(displayName);
    }
    public static ExamSection fromSectionName(String sectionName) {
        if (sectionName == null || sectionName.isBlank()) {
            return LY_THUYET;
        }
        String name = sectionName.trim();
        for (ExamSection section : values()) {
            if (name.contains(section.displayName)) {
                return section;
            }
        }
        if (name.contains("Sa hÃ¬nh")) {
            return THUC_HANH_TRONG_HINH;
        }
        return LY_THUYET;
    }
    public static int resolveExamTypeId(String sectionName) {
        return fromSectionName(sectionName).getExamTypeId();
    }
    public static boolean isTheory(String sectionName) {
        return fromSectionName(sectionName) == LY_THUYET;
    }
    public static boolean isSidebarMenuDisabled(boolean isTheory, String menuKey) {
        if (!isTheory || menuKey == null) {
            return false;
        }
        return "score-entry".equals(menuKey) || "result-details".equals(menuKey);
    }
}

