package examstaff.enums;

public enum ExamSection {
    LY_THUYET("Lý thuyết", 1),
    THUC_HANH_TRONG_HINH("Thực hành trong hình", 2),
    THUC_HANH_TREN_DUONG("Thực hành trên đường", 4);

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
}
