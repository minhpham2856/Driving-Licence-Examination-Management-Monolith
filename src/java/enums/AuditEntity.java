package enums;

public enum AuditEntity {
    CANDIDATE("Thí sinh"),
    THI_SINH("Thí sinh"),
    EXAMREGISTRATION("Thí sinh"),
    HO_SO_DANG_KY("Thí sinh"),
    PROFILE("Hồ sơ"),
    PAYMENT("Thanh toán"),
    EXAMSCORE("Điểm thi"),
    EXAMDEVICE("Thiết bị thi"),
    SESSION("Ca thi"),
    SESSION_EXAMINER("Phân công sát hạch viên"),
    SESSION_EXAMINERAREA("Phân công phòng sát hạch viên"),
    CANDIDATECALL("Gọi thí sinh"),
    KET_QUA_THI("Kết quả thi"),
    PHONG_THI("Phòng thi"),
    SCOREENTRYQUEUE("Hàng đợi nhập điểm");

    private final String labelVi;

    AuditEntity(String labelVi) {
        this.labelVi = labelVi;
    }

    public String getLabelVi() {
        return labelVi;
    }
}
