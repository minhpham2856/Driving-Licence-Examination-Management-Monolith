package examiner.enums;

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

    DocumentName(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static DocumentName fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (DocumentName name : values()) {
            if (name.getValue().equals(value)) {
                return name;
            }
        }
        return null;
    }

    public static DocumentName parseAlias(String alias) {
        if (alias == null || alias.isBlank()) {
            return DEFAULT;
        }
        String normalized = alias.trim().toLowerCase();
        switch (normalized) {
            case "candidates":
                return CANDIDATES;
            case "results":
                return RESULTS;
            case "minutes":
                return MINUTES;
            case "violations":
                return VIOLATIONS;
            case "audit":
                return AUDIT;
            case "bb1":
            case "bb1-ly-thuyet":
                return BB1;
            case "bb2":
            case "bb2-thuc-hanh-trong-hinh":
                return BB2;
            default:
                DocumentName byValue = fromValue(normalized);
                return byValue != null ? byValue : DEFAULT;
        }
    }

    public String filename(String extension) {
        return value + "." + extension;
    }

    public static String withExtension(String documentType, String extension) {
        return parseAlias(documentType).filename(extension);
    }

    public static String printCandidate(String documentType, int sbd) {
        return parseAlias(documentType).getValue() + "-sbd-" + sbd + ".docx";
    }
}
