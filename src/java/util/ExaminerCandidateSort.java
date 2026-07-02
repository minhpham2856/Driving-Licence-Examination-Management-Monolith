package util;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ExaminerCandidateSort {

    public static final String DEFAULT_COLUMN = "sbd";
    public static final String DEFAULT_DIRECTION = "asc";

    private static final Set<String> ALLOWED_COLUMNS = Set.of(
            "fullName", "sbd", "dob", "address", "status", "governmentId",
            "correct", "wrong", "unanswered", "result", "examDate", "examScore");

    private ExaminerCandidateSort() {
    }

    public static final class Spec {
        private final String column;
        private final boolean ascending;

        public Spec(String column, boolean ascending) {
            this.column = column;
            this.ascending = ascending;
        }

        public String getColumn() {
            return column;
        }

        public boolean isAscending() {
            return ascending;
        }
    }

    public static Spec parse(String sort, String dir) {
        String column = sort != null ? sort.trim() : DEFAULT_COLUMN;
        if (!ALLOWED_COLUMNS.contains(column)) {
            column = DEFAULT_COLUMN;
        }
        boolean ascending = !"desc".equalsIgnoreCase(dir);
        return new Spec(column, ascending);
    }

    public static void sort(List<Map<String, Object>> rows, Spec spec) {
        if (rows == null || rows.size() < 2 || spec == null) {
            return;
        }
        Comparator<Map<String, Object>> comparator = comparatorFor(spec.getColumn());
        if (!spec.isAscending()) {
            comparator = comparator.reversed();
        }
        rows.sort(comparator);
    }

    private static Comparator<Map<String, Object>> comparatorFor(String column) {
        return switch (column) {
            case "fullName", "address", "governmentId", "sbd" -> stringComparator(column);
            case "dob" -> dateComparator("dobRaw");
            case "examDate" -> dateComparator("examDateRaw");
            case "status" -> Comparator.comparingInt(ExaminerCandidateSort::statusOrder);
            case "result" -> Comparator.comparingInt(ExaminerCandidateSort::resultOrder);
            case "correct", "wrong", "unanswered", "examScore" -> numericComparator(column);
            default -> stringComparator("sbd");
        };
    }

    private static Comparator<Map<String, Object>> stringComparator(String key) {
        return Comparator.comparing(row -> normalizeString(row.get(key)));
    }

    private static Comparator<Map<String, Object>> dateComparator(String key) {
        return Comparator.comparing(row -> normalizeString(row.get(key)));
    }

    private static Comparator<Map<String, Object>> numericComparator(String key) {
        return Comparator.comparingInt(row -> parseNumeric(row.get(key)));
    }

    private static String normalizeString(Object value) {
        if (value == null) {
            return "";
        }
        String text = String.valueOf(value).trim();
        return "—".equals(text) ? "" : text.toLowerCase();
    }

    private static int parseNumeric(Object value) {
        if (value == null) {
            return -1;
        }
        String text = String.valueOf(value).trim();
        if (text.isEmpty() || "—".equals(text)) {
            return -1;
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

    private static int statusOrder(Map<String, Object> row) {
        String status = row.get("status") != null ? String.valueOf(row.get("status")) : "";
        return switch (status) {
            case "pending" -> 0;
            case "testing" -> 1;
            case "done" -> 2;
            case "absent" -> 3;
            default -> 4;
        };
    }

    private static int resultOrder(Map<String, Object> row) {
        if (Boolean.TRUE.equals(row.get("passed"))) {
            return 2;
        }
        String label = row.get("resultLabel") != null ? String.valueOf(row.get("resultLabel")) : "—";
        if ("TRƯỢT".equalsIgnoreCase(label)) {
            return 1;
        }
        return 0;
    }
}
