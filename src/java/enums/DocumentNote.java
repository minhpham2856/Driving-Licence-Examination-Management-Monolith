package enums;
public enum DocumentNote {
    DA_TAI_LEN("Đã tải lên");
    private final String displayName;
    DocumentNote(String displayName) {
        this.displayName = displayName;
    }
    public String getDisplayName() {
        return displayName;
    }
}
