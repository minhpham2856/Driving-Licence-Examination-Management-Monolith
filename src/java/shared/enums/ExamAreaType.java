package shared.enums;

public enum ExamAreaType {
    PROCEDURE_ROOM("Phòng thủ tục"),
    EXAM_ROOM("Phòng thi"),
    PRACTICE_YARD("Sân thi");

    private final String value;

    private ExamAreaType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ExamAreaType fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (ExamAreaType status : values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }
}
