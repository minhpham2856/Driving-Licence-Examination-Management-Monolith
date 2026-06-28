package enums;

public enum Sex {
    MALE(true, "Nam"),
    FEMALE(false, "Nữ");

    private final boolean isMale;
    private final String labelVi;

    Sex(boolean isMale, String labelVi) {
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
