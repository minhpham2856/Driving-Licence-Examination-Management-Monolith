package examiner.util;

import examiner.dto.CandidateRowDTO;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

// Parses and applies column sort specs for examiner candidate table views.
public final class SortUtil {

    public static final String DEFAULT_COLUMN = "sbd";
    public static final String DEFAULT_DIRECTION = "asc";
    private static final Set<String> ALLOWED_COLUMNS = Set.of(
            "fullName", "sbd", "dob", "address", "status", "governmentId",
            "correct", "wrong", "unanswered", "result", "examDate", "examScore");

    private SortUtil() {
    }

    public static final class Spec {

        private final String column;
        private final boolean ascending;

        // Parsed sort column and ascending flag from request parameters.
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

    // Parses sort column and direction from request parameters.
    public static Spec parse(String sort, String dir) {
        String column = sort != null ? sort.trim() : DEFAULT_COLUMN;
        if (!ALLOWED_COLUMNS.contains(column)) {
            column = DEFAULT_COLUMN;
        }
        boolean ascending = !"desc".equalsIgnoreCase(dir);
        return new Spec(column, ascending);
    }

    // Sorts candidate rows in place using the parsed spec.
    public static void sort(List<CandidateRowDTO> rows, Spec spec) {
        if (rows == null || rows.size() < 2 || spec == null) {
            return;
        }
        Comparator<CandidateRowDTO> comparator = comparatorFor(spec.getColumn());
        if (!spec.isAscending()) {
            comparator = comparator.reversed();
        }
        rows.sort(comparator);
    }

    // Private helper: comparator for.
    private static Comparator<CandidateRowDTO> comparatorFor(String column) {
        return switch (column) {
            case "fullName", "address", "governmentId" ->
                stringComparator(column);
            case "sbd" ->
                Comparator.comparingInt(CandidateRowDTO::getCandidateNumber);
            case "dob" ->
                Comparator.comparing(row -> normalizeString(row.getDob()));
            case "examDate" ->
                Comparator.comparing(row -> normalizeString(row.getExamDate()));
            case "status" ->
                Comparator.comparingInt(SortUtil::statusOrder);
            case "result" ->
                Comparator.comparingInt(SortUtil::resultOrder);
            case "correct" ->
                Comparator.comparingInt(CandidateRowDTO::getCorrect);
            case "wrong" ->
                Comparator.comparingInt(CandidateRowDTO::getWrong);
            case "unanswered" ->
                Comparator.comparingInt(CandidateRowDTO::getUnanswered);
            case "examScore" ->
                Comparator.comparingInt(
                row -> row.getExamScore() != null ? row.getExamScore() : -1
                );
            default ->
                Comparator.comparingInt(CandidateRowDTO::getCandidateNumber);
        };
    }

    // Private helper: string comparator.
    private static Comparator<CandidateRowDTO> stringComparator(String field) {
        return Comparator.comparing(row -> {
            return switch (field) {
                case "fullName" ->
                    normalizeString(row.getFullName());
                case "address" ->
                    normalizeString(row.getAddress());
                case "governmentId" ->
                    normalizeString(row.getGovernmentId());
                default ->
                    "";
            };
        });
    }

    private static String normalizeString(Object value) {
        if (value == null) {
            return "";
        }
        String text = String.valueOf(value).trim();
        return "-".equals(text) ? "" : text.toLowerCase();
    }

    // Private helper: status order.
    private static int statusOrder(CandidateRowDTO row) {
        if (row.getSectionStatus() == null) {
            return 4;
        }
        return switch (row.getSectionStatus()) {
            case NOT_STARTED ->
                0;
            case IN_PROGRESS ->
                1;
            case AWAITING_SIGNATURE ->
                2;
            case COMPLETED ->
                3;
            default ->
                4;
        };
    }

    // Private helper: result order.
    private static int resultOrder(CandidateRowDTO row) {
        if (row.isPassed()) {
            return 2;
        }
        String label = row.getResultLabel() != null ? row.getResultLabel() : "-";
        if ("TRƯỢT".equalsIgnoreCase(label)) {
            return 1;
        }
        return 0;
    }
}
