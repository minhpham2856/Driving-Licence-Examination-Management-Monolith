package shared.enums;

public enum ExamSessionStatus {
    CHUA_DIEN_RA("ChÆ°a diá»…n ra"),
    MO("Má»Ÿ"),
    DANG_DIEN_RA("Äang diá»…n ra"),
    HOAN_TAT("HoÃ n táº¥t"),
    DA_HUY("ÄÃ£ há»§y");

    private final String value;

    private ExamSessionStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ExamSessionStatus fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (ExamSessionStatus status : values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }
}
