import os

files_content = {
    'CandidateStatus.java': '''package enums;

public enum CandidateStatus {
    NOT_STARTED("Chưa thi"),
    IN_PROGRESS("Đang thi"),
    AWAITING_SIGNATURE("Chờ ký"),
    COMPLETED("Đã thi");

    private final String value;

    private CandidateStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static CandidateStatus fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (CandidateStatus status : values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }
}
''',
    'Sex.java': '''package enums;

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
''',
    'PaymentStatus.java': '''package enums;

public enum PaymentStatus {
    COMPLETED("Hoàn tất"),
    FAILED("Thất bại"),
    PENDING("Chờ thanh toán");

    private final String value;

    private PaymentStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static PaymentStatus fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (PaymentStatus status : values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }
}
''',
    'RegistrationStatus.java': '''package enums;

public enum RegistrationStatus {
    PENDING("Chờ duyệt"),
    APPROVED("Duyệt"),
    REJECTED("Loại");

    private final String value;

    private RegistrationStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static RegistrationStatus fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (RegistrationStatus status : values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }
}
''',
    'ViolationReason.java': '''package enums;

public enum ViolationReason {
    SAFETY_VIOLATION("Gây mất an toàn nghiêm trọng trong quá trình thi"),
    CHEATING("Gian lận"),
    OTHER("Lý do khác");

    private final String value;

    private ViolationReason(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ViolationReason fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (ViolationReason reason : values()) {
            if (reason.getValue().equals(value)) {
                return reason;
            }
        }
        return null;
    }
}
'''
}

base_dir = r'src/java/enums/'
for filename, content in files_content.items():
    with open(os.path.join(base_dir, filename), 'w', encoding='utf-8') as f:
        f.write(content)
