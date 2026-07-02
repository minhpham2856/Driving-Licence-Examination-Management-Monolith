package examstaff.util;

import examstaff.dto.exam.ExamRegistrationDTO;

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
