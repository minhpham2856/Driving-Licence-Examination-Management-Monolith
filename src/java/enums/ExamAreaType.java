package enums;

public enum ExamAreaType {
    PROCEDURE_ROOM("Phòng thủ tục"),
    EXAM_ROOM("Phòng thi"),
    PRACTICE_YARD("Sân thi");

    private final String value;

    ExamAreaType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ExamAreaType fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (ExamAreaType type : values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        return null;
    }
}
