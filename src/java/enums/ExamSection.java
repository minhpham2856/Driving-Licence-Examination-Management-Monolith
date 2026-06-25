package enums;

public enum ExamSection {
    THEORY("Lý thuyết"),
    LAYOUT("Thực hành trong hình"),
    ROAD("Thực hành trên đường");

    private final String displayName;

    ExamSection(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
