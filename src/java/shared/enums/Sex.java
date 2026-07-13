package shared.enums;

public enum Sex {
    MALE("Nam"),
    FEMALE("Nữ");

    private final String value;

    private Sex(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
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
}
