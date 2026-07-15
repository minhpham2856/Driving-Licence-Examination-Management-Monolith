package examstaff.util;

import examstaff.dto.ExamRegistrationDTO;

/** Escape / nối field JSON thủ công cho payload bảng gọi / thí sinh. */
public final class JsonUtil {

    private JsonUtil() {
    }

    /**
     * Escape chuỗi thành literal JSON (null → token {@code null}).
     *
     * @param value chuỗi gốc
     * @return literal JSON (đã bọc dấu ngoặc kép nếu không null)
     */
    public static String escapeJson(String value) {
        if (value == null) {
            return "null";
        }
        String escaped = value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
        return "\"" + escaped + "\"";
    }

    /**
     * Nối field chuỗi vào builder.
     *
     * @param json           buffer JSON
     * @param name           tên field
     * @param value          giá trị chuỗi
     * @param trailingComma  có thêm dấu phẩy sau field
     */
    public static void appendJsonField(StringBuilder json, String name, String value, boolean trailingComma) {
        json.append('"').append(name).append("\":").append(escapeJson(value));
        if (trailingComma) {
            json.append(',');
        }
    }

    /**
     * Nối field số nguyên dài vào builder.
     *
     * @param json           buffer JSON
     * @param name           tên field
     * @param value          giá trị long
     * @param trailingComma  có thêm dấu phẩy sau field
     */
    public static void appendJsonField(StringBuilder json, String name, long value, boolean trailingComma) {
        json.append('"').append(name).append("\":").append(value);
        if (trailingComma) {
            json.append(',');
        }
    }

    /**
     * Nối field boolean vào builder.
     *
     * @param json           buffer JSON
     * @param name           tên field
     * @param value          giá trị boolean
     * @param trailingComma  có thêm dấu phẩy sau field
     */
    public static void appendJsonField(StringBuilder json, String name, boolean value, boolean trailingComma) {
        json.append('"').append(name).append("\":").append(value);
        if (trailingComma) {
            json.append(',');
        }
    }

    /**
     * Nối object thí sinh rút gọn ({@code sbd}, {@code name}, {@code clazz}).
     *
     * @param json      buffer JSON
     * @param candidate hồ sơ (null → {@code null})
     */
    public static void appendCandidateJson(StringBuilder json, ExamRegistrationDTO candidate) {
        if (candidate == null) {
            json.append("null");
            return;
        }
        json.append('{');
        appendJsonField(json, "sbd", candidate.getSbd(), true);
        appendJsonField(json, "name", candidate.getName(), true);
        appendJsonField(json, "clazz", candidate.getClazz(), false);
        json.append('}');
    }

    /**
     * Nối mảng thí sinh rút gọn.
     *
     * @param json       buffer JSON
     * @param candidates danh sách (null/rỗng → {@code []})
     */
    public static void appendCandidateArrayJson(StringBuilder json, java.util.List<ExamRegistrationDTO> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            json.append("[]");
            return;
        }
        json.append('[');
        for (int i = 0; i < candidates.size(); i++) {
            appendCandidateJson(json, candidates.get(i));
            if (i < candidates.size() - 1) {
                json.append(',');
            }
        }
        json.append(']');
    }
}
