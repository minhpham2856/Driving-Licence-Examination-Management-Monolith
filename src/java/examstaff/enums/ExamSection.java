package examstaff.enums;

/** Phần thi trong kỳ (ánh xạ ExamTypeId). */
public enum ExamSection {
    /** Phần lý thuyết (ExamTypeId = 1). */
    LY_THUYET("Lý thuyết", 1),
    /** Thực hành trong hình / sa hình (ExamTypeId = 2). */
    THUC_HANH_TRONG_HINH("Thực hành trong hình", 2),
    /** Thực hành trên đường (ExamTypeId = 4). */
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
