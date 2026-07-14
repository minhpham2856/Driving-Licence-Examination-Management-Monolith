package registrant.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AuditChangeDetails {

    private static final Pattern CHANGE_BLOCK = Pattern.compile(
            "\\{\"f\":(\"(?:\\\\.|[^\"\\\\])*\"|null),\"o\":(\"(?:\\\\.|[^\"\\\\])*\"|null),\"n\":(\"(?:\\\\.|[^\"\\\\])*\"|null)\\}");

    private AuditChangeDetails() {
    }

    public static final class FieldChange {
        private final String field;
        private final String oldValue;
        private final String newValue;

        public FieldChange(String field, String oldValue, String newValue) {
            this.field = field;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }

        public String field() {
            return field;
        }

        public String oldValue() {
            return oldValue;
        }

        public String newValue() {
            return newValue;
        }
    }

    public static final class DisplayColumns {
        private final String info;
        private final String oldValue;
        private final String newValue;
        private final boolean multiline;

        public DisplayColumns(String info, String oldValue, String newValue, boolean multiline) {
            this.info = info;
            this.oldValue = oldValue;
            this.newValue = newValue;
            this.multiline = multiline;
        }

        public String info() {
            return info;
        }

        public String oldValue() {
            return oldValue;
        }

        public String newValue() {
            return newValue;
        }

        public boolean multiline() {
            return multiline;
        }
    }

    public static String toJson(List<FieldChange> changes) {
        if (changes == null || changes.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder("{\"changes\":[");
        for (int i = 0; i < changes.size(); i++) {
            FieldChange change = changes.get(i);
            if (i > 0) {
                sb.append(',');
            }
            sb.append("{\"f\":").append(jsonString(change.field()))
                    .append(",\"o\":").append(jsonString(change.oldValue()))
                    .append(",\"n\":").append(jsonString(change.newValue()))
                    .append('}');
        }
        sb.append("]}");
        return sb.toString();
    }

    public static List<FieldChange> parseChanges(String detailsJson) {
        List<FieldChange> changes = new ArrayList<>();
        if (detailsJson == null || detailsJson.isBlank()) {
            return changes;
        }
        Matcher matcher = CHANGE_BLOCK.matcher(detailsJson);
        while (matcher.find()) {
            changes.add(new FieldChange(
                    decodeJsonString(matcher.group(1)),
                    decodeJsonString(matcher.group(2)),
                    decodeJsonString(matcher.group(3))));
        }
        return changes;
    }

    public static DisplayColumns toDisplayColumns(String detailsJson, String legacyOld, String legacyNew) {
        List<FieldChange> changes = parseChanges(detailsJson);
        if (!changes.isEmpty()) {
            StringBuilder info = new StringBuilder();
            StringBuilder old = new StringBuilder();
            StringBuilder neu = new StringBuilder();
            for (int i = 0; i < changes.size(); i++) {
                FieldChange change = changes.get(i);
                if (i > 0) {
                    info.append(", ");
                    old.append('\n');
                    neu.append('\n');
                }
                info.append("Thay đổi ").append(change.field().toLowerCase());
                old.append(nullToDash(change.oldValue()));
                neu.append(nullToDash(change.newValue()));
            }
            return new DisplayColumns(
                    info.toString(),
                    old.toString(),
                    neu.toString(),
                    changes.size() > 1);
        }
        if (legacyOld != null && !legacyOld.isBlank()) {
            return new DisplayColumns(null, legacyOld, nullToDash(legacyNew), legacyOld.contains("\n"));
        }
        return new DisplayColumns(null, null, nullToDash(legacyNew), false);
    }

    public static void addIfChanged(List<FieldChange> changes, String field, String oldValue, String newValue) {
        String oldNorm = normalize(oldValue);
        String newNorm = normalize(newValue);
        if (!oldNorm.equals(newNorm)) {
            changes.add(new FieldChange(field, displayValue(oldValue), displayValue(newValue)));
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private static String displayValue(String value) {
        return value == null || value.isBlank() ? "-" : value.trim();
    }

    private static String nullToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private static String jsonString(String value) {
        if (value == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '\\' -> sb.append("\\\\");
                case '"' -> sb.append("\\\"");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> sb.append(ch);
            }
        }
        sb.append('"');
        return sb.toString();
    }

    private static String decodeJsonString(String token) {
        if (token == null || "null".equals(token)) {
            return null;
        }
        if (token.length() < 2 || token.charAt(0) != '"') {
            return token;
        }
        String raw = token.substring(1, token.length() - 1);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < raw.length(); i++) {
            char ch = raw.charAt(i);
            if (ch == '\\' && i + 1 < raw.length()) {
                char next = raw.charAt(++i);
                switch (next) {
                    case 'n' -> sb.append('\n');
                    case 'r' -> sb.append('\r');
                    case 't' -> sb.append('\t');
                    case '"' -> sb.append('"');
                    case '\\' -> sb.append('\\');
                    default -> sb.append(next);
                }
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }
}
