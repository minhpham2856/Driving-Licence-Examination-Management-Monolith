package enums;

public enum Sex {
    MALE("Nam", false),
    FEMALE("Nữ", true);

    private final String value;
    private final boolean dbBit;

    private Sex(String value, boolean dbBit) {
        this.value = value;
        this.dbBit = dbBit;
    }

    public String getValue() {
        return value;
    }

    public boolean toDbBit() {
        return dbBit;
    }

    public static Sex fromValue(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        for (Sex gender : values()) {
            if (gender.getValue().equalsIgnoreCase(trimmed)) {
                return gender;
            }
        }
        if ("1".equals(trimmed) || "nu".equalsIgnoreCase(trimmed)) {
            return FEMALE;
        }
        if ("0".equals(trimmed) || "nam".equalsIgnoreCase(trimmed)) {
            return MALE;
        }
        return null;
    }

    public static Sex fromDbBit(boolean dbBit) {
        return dbBit ? FEMALE : MALE;
    }
}
