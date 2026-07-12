package shared.enums;

public enum SectionStatus {
    CHUA_THI("ChÆ°a thi"),
    DANG_THI("Äang thi"),
    DAT("Äáº¡t"),
    TRUOT("TrÆ°á»£t"),
    BO_THI("Bá» thi");

    private final String value;

    private SectionStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
    
    public String getDisplayName() {
        return value;
    }

    public static SectionStatus fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (SectionStatus status : values()) {
            if (status.getValue().equals(value) || status.name().equalsIgnoreCase(value)) {
                return status;
            }
        }
        return null;
    }
}

