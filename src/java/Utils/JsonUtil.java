package Utils;

public final class JsonUtil {

    private JsonUtil() {
    }

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

    public static void appendJsonField(StringBuilder json, String name, String value, boolean trailingComma) {
        json.append('"').append(name).append("\":").append(escapeJson(value));
        if (trailingComma) {
            json.append(',');
        }
    }

    public static void appendJsonField(StringBuilder json, String name, long value, boolean trailingComma) {
        json.append('"').append(name).append("\":").append(value);
        if (trailingComma) {
            json.append(',');
        }
    }

    public static void appendJsonField(StringBuilder json, String name, boolean value, boolean trailingComma) {
        json.append('"').append(name).append("\":").append(value);
        if (trailingComma) {
            json.append(',');
        }
    }

    public static void appendCandidateJson(StringBuilder json, Models.ExamRegistration candidate) {
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
}
