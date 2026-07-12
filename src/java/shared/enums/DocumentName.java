package shared.enums;

public enum DocumentName {
    CANDIDATES("danh-sach-thi-sinh"),
    RESULTS("ket-qua-thi"),
    MINUTES("bien-ban-thi"),
    VIOLATIONS("vi-pham"),
    AUDIT("nhat-ky"),
    BB1("bb1-ly-thuyet"),
    BB2("bb2-thuc-hanh-trong-hinh"),
    DEFAULT("tai-lieu");

    private final String value;

    private DocumentName(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static DocumentName fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (DocumentName status : values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }
}
