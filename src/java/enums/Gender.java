package enums;

public enum Gender {
    MALE(true, "Nam"),
    FEMALE(false, "Nữ");

    private final boolean isMale;
    private final String labelVi;

    Gender(boolean isMale, String labelVi) {
        this.isMale = isMale;
        this.labelVi = labelVi;
    }

    public boolean isMale() {
        return isMale;
    }

    public String getLabelVi() {
        return labelVi;
    }
}
