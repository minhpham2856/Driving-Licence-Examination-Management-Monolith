package shared.enums;

public enum FileName {
    CANDIDATES("danh-sach-thi-sinh"),
    RESULTS("ket-qua-thi"),
    RESULT("bien-ban-thi"),
    VIOLATIONS("vi-pham"),
    AUDIT("nhat-ky"),
    BB1("bb1-ly-thuyet"),
    BB2("bb2-thuc-hanh-trong-hinh"),
    DEFAULT("tai-lieu");

    private final String value;

    FileName(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static FileName fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (FileName name : values()) {
            if (name.getValue().equals(value)) {
                return name;
            }
        }
        return null;
    }
}
