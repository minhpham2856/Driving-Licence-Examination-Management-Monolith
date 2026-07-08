package util;

import dto.CandidateRowDTO;
import java.util.Comparator;
import java.util.List;
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

    private static Comparator<CandidateRowDTO> comparatorFor(String column) {
        return switch (column) {
            case "fullName", "address", "governmentId" -> stringComparator(column);
            case "sbd" -> Comparator.comparingInt(CandidateRowDTO::getSbd);
            case "dob" -> Comparator.comparing(row -> normalizeString(row.getDobRaw()));
            case "examDate" -> Comparator.comparing(row -> normalizeString(row.getExamDate()));
            case "status" -> Comparator.comparingInt(ExaminerCandidateSort::statusOrder);
            case "result" -> Comparator.comparingInt(ExaminerCandidateSort::resultOrder);
            case "correct" -> Comparator.comparingInt(CandidateRowDTO::getCorrect);
            case "wrong" -> Comparator.comparingInt(CandidateRowDTO::getWrong);
            case "unanswered" -> Comparator.comparingInt(CandidateRowDTO::getUnanswered);
            case "examScore" -> Comparator.comparingInt(row -> parseNumeric(row.getExamScore()));
            default -> Comparator.comparingInt(CandidateRowDTO::getSbd);
        };
    }

    private static Comparator<CandidateRowDTO> stringComparator(String field) {
        return Comparator.comparing(row -> {
            return switch (field) {
                case "fullName" -> normalizeString(row.getFullName());
                case "address" -> normalizeString(row.getAddress());
                case "governmentId" -> normalizeString(row.getGovernmentId());
                default -> "";
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

    private static int parseNumeric(Object value) {
        if (value == null) {
            return -1;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        String text = String.valueOf(value).trim();
        if (text.isEmpty() || "-".equals(text)) {
            return -1;
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

    private static int statusOrder(CandidateRowDTO row) {
        String status = row.getStatus() != null ? row.getStatus() : "";
        return switch (status) {
            case "pending" -> 0;
            case "testing" -> 1;
            case "awaiting" -> 2;
            case "done" -> 3;
            default -> 4;
        };
    }

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
