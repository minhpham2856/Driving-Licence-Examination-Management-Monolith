package enums;
public enum Gender {
    NAM("Nam"),
    NU("Nữ");
    private final String displayName;
    Gender(String displayName) {
        this.displayName = displayName;
    }
    public String getDisplayName() {
        return displayName;
    }
    public static String display(boolean male) {
        return male ? NAM.getDisplayName() : NU.getDisplayName();
    }
    public static boolean isFemale(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        String trimmed = value.trim();
        return NU.matches(trimmed) || "0".equals(trimmed);
    }
    public static boolean isMale(String value) {
        if (value == null || value.isBlank()) {
            return true;
        }
        String trimmed = value.trim();
        return NAM.matches(trimmed) || "1".equals(trimmed);
    }
    public boolean matches(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        return displayName.equalsIgnoreCase(value.trim());
    }
}
