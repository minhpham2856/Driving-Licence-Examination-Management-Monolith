package examstaff.enums;

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
        for (Sex sex : values()) {
            if (sex.getValue().equalsIgnoreCase(value)) {
                return sex;
            }
        }
        return null;
    }
    
    public static Sex fromDbBit(boolean bit) {
        return bit ? FEMALE : MALE;
    }
}
