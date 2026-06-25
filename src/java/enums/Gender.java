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

    public static String sexFromGender(boolean isMale) {
        return isMale ? MALE.labelVi : FEMALE.labelVi;
    }

    public static boolean genderFromSex(String sex) {
        if (sex == null) return false;
        String s = sex.trim();
        return s.equalsIgnoreCase("Nam") || s.equalsIgnoreCase("Male") || s.equals("M") || s.equals("1");
    }
}
