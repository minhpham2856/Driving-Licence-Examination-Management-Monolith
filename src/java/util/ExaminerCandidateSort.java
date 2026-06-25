package util;


import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

 // Utility for sorting candidate view-rows in the examiner portal.
public final class ExaminerCandidateSort {

    // Default column used when the requested sort column is invalid or missing
    public static final String DEFAULT_COLUMN = "sbd";
    // Default direction used when the requested direction is invalid or missing
    public static final String DEFAULT_DIRECTION = "asc";

    // Whitelist of column names that are allowed for sorting — prevents SQL-injection-like issues
    private static final Set<String> ALLOWED_COLUMNS = Set.of(
            "fullName", "sbd", "dob", "address", "status", "governmentId",
            "correct", "wrong", "unanswered", "result", "examDate", "examScore");

    // Private constructor prevents instantiation — all methods are static
    private ExaminerCandidateSort() {
    }

         // Immutable sort specification containing the column name and sort direction.
    public static final class Spec {
        // The validated column name to sort by
        private final String column;
        // True for ascending order (A-Z, 0-9), false for descending
        private final boolean ascending;

        // Constructor accepting the column name and ascending flag
        public Spec(String column, boolean ascending) {
            this.column = column;
            this.ascending = ascending;
        }

        // Returns the sort column name
        public String getColumn() { return column; }
        // Returns whether the sort is ascending
        public boolean isAscending() { return ascending; }
    }

         // Parses sort parameters from request strings into a {@link Spec}.
    public static Spec parse(String sort, String dir) {
        // Use the provided column or fall back to the default if null
        String column = sort != null ? sort.trim() : DEFAULT_COLUMN;
        // Reject any column not in the whitelist to prevent invalid sorting
        if (!ALLOWED_COLUMNS.contains(column)) {
            column = DEFAULT_COLUMN;
        }
        // "desc" means descending; anything else (including null) defaults to ascending
        boolean ascending = !"desc".equalsIgnoreCase(dir);
        return new Spec(column, ascending);
    }

         // Sorts a list of candidate view-row maps in place according to the given spec.
    public static void sort(List<Map<String, Object>> rows, Spec spec) {
        // Guard: skip sorting if the list is too small or spec is missing
        if (rows == null || rows.size() < 2 || spec == null) {
            return;
        }
        // Build the appropriate comparator for the requested column type
        Comparator<Map<String, Object>> comparator = comparatorFor(spec.getColumn());
        // Reverse the comparator if descending order was requested
        if (!spec.isAscending()) {
            comparator = comparator.reversed();
        }
        // Perform the in-place sort on the mutable list
        rows.sort(comparator);
    }

    // Returns the comparator for a given column name.
    private static Comparator<Map<String, Object>> comparatorFor(String column) {
        return switch (column) {
            // Text columns: compared as case-insensitive normalised strings
            case "fullName", "address", "governmentId", "sbd" -> stringComparator(column);
            // Date columns: compared using the raw ISO date string (dobRaw/examDateRaw)
            case "dob" -> dateComparator("dobRaw");
            case "examDate" -> dateComparator("examDateRaw");
            // Status column: compared using a custom ordinal mapping (pending < testing < done < absent)
            case "status" -> Comparator.comparingInt(ExaminerCandidateSort::statusOrder);
            // Result column: compared using a custom ordinal mapping (unscored < failed < passed)
            case "result" -> Comparator.comparingInt(ExaminerCandidateSort::resultOrder);
            // Numeric columns: parsed to int for numerical comparison
            case "correct", "wrong", "unanswered", "examScore" -> numericComparator(column);
            // Fallback: sort by SBD as a string if column is unrecognised
            default -> stringComparator("sbd");
        };
    }

    // Creates a string comparator that normalises the value at the given map key
    private static Comparator<Map<String, Object>> stringComparator(String key) {
        return Comparator.comparing(row -> normalizeString(row.get(key)));
    }

    // Creates a date comparator using the raw date string at the given map key
    private static Comparator<Map<String, Object>> dateComparator(String key) {
        return Comparator.comparing(row -> normalizeString(row.get(key)));
    }

    // Creates a numeric comparator that parses the int value at the given map key
    private static Comparator<Map<String, Object>> numericComparator(String key) {
        return Comparator.comparingInt(row -> parseNumeric(row.get(key)));
    }

    // Normalises a value for string comparison: null -> "", "-" -> "".
    private static String normalizeString(Object value) {
        // Treat null as empty string so nulls sort first
        if (value == null) {
            return "";
        }
        String text = String.valueOf(value).trim();
        // Treat the dash placeholder as empty so it sorts with nulls
        return "-".equals(text) ? "" : text.toLowerCase();
    }

    // Parses a numeric value, returning -1 for null, empty, or "-".
    private static int parseNumeric(Object value) {
        // Null values sort before all valid numbers
        if (value == null) {
            return -1;
        }
        String text = String.valueOf(value).trim();
        // Empty or dash placeholder treated as missing
        if (text.isEmpty() || "-".equals(text)) {
            return -1;
        }
        try {
            // Attempt integer parsing for numeric comparison
            return Integer.parseInt(text);
        } catch (NumberFormatException ignored) {
            // Non-numeric text is treated as missing
            return -1;
        }
    }

    // Returns the sort order index for a candidate status string.
    private static int statusOrder(Map<String, Object> row) {
        // Extract the status string, defaulting to empty if null
        String status = row.get("status") != null ? String.valueOf(row.get("status")) : "";
        return switch (status) {
            case "pending" -> 0;   // Not yet started — sorts first
            case "testing" -> 1;   // Currently taking the exam
            case "done" -> 2;      // Completed the exam
            case "absent" -> 3;    // Marked absent — sorts last
            default -> 4;         // Unknown status — sorts after absent
        };
    }

    // Returns the sort order index for a candidate result (passed/failed).
    private static int resultOrder(Map<String, Object> row) {
        // Passed candidates get the highest ordinal
        if (Boolean.TRUE.equals(row.get("passed"))) {
            return 2;
        }
        // Check the Vietnamese result label for "TRUOT" (failed)
        String label = row.get("resultLabel") != null ? String.valueOf(row.get("resultLabel")) : "-";
        if ("TRƯỢT".equalsIgnoreCase(label)) {
            return 1; // Failed candidates sort in the middle
        }
        return 0; // Unscored/pending candidates sort first
    }
}
