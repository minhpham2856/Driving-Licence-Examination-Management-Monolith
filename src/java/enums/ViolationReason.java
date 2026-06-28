package enums;

public enum ViolationReason {
    QUY_CHE("quy-che", "Vi phạm quy chế phòng thi"),
    GIAN_LAN("gian-lan", "Gian lận / sao chép"),
    DEVICES("devices", "Sử dụng thiết bị cấm"),
    RA_VAO("ra-vao", "Ra vào phòng thi trái quy định"),
    KHAC("khac", "Lý do khác");

    private final String code;
    private final String label;

    ViolationReason(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }
}
