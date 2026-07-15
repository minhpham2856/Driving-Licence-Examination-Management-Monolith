package examstaff.util;

import examstaff.dto.ExamRegistrationDTO;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;

/** Sắp xếp danh sách đăng ký / thí sinh theo cột và hướng (server-side). */
public final class ExamRegistrationSort {

    public static final String DEFAULT_COLUMN = "sbd";
    public static final String DEFAULT_DIRECTION = "asc";

    private static final Set<String> ALLOWED_COLUMNS = Set.of(
            "sbd", "name", "clazz", "govIdNo", "cccd", "dob",
            "theoryScore", "practicalScore");

    private ExamRegistrationSort() {
    }

    /** Đặc tả cột + chiều sắp xếp đã chuẩn hóa. */
    public static final class Spec {
        private final String column;
        private final boolean ascending;

        /**
         * @param column    tên cột đã whitelist
         * @param ascending {@code true} = tăng dần
         */
        public Spec(String column, boolean ascending) {
            this.column = column;
            this.ascending = ascending;
        }

        /** @return tên cột sắp xếp */
        public String getColumn() {
            return column;
        }

        /** @return {@code true} nếu tăng dần */
        public boolean isAscending() {
            return ascending;
        }
    }

    /**
     * Parse tham số sort/dir thành {@link Spec} (alias {@code cccd} → {@code govIdNo}).
     *
     * @param sort tên cột (invalid → default)
     * @param dir  {@code desc} = giảm; còn lại = tăng
     * @return spec hợp lệ
     */
    public static Spec parse(String sort, String dir) {
        String column = sort != null ? sort.trim() : DEFAULT_COLUMN;
        if ("cccd".equals(column)) {
            column = "govIdNo";
        }
        if (!ALLOWED_COLUMNS.contains(column)) {
            column = DEFAULT_COLUMN;
        }
        boolean ascending = !"desc".equalsIgnoreCase(dir);
        return new Spec(column, ascending);
    }

    /**
     * Sắp xếp tại chỗ danh sách theo {@link Spec}.
     *
     * @param rows danh sách (mutate nếu ≥ 2 phần tử)
     * @param spec đặc tả sắp xếp
     */
    public static void sort(List<ExamRegistrationDTO> rows, Spec spec) {
        if (rows == null || rows.size() < 2 || spec == null) {
            return;
        }
        Comparator<ExamRegistrationDTO> comparator = comparatorFor(spec.getColumn());
        if (!spec.isAscending()) {
            comparator = comparator.reversed();
        }
        rows.sort(comparator);
    }

    /** Comparator theo tên cột whitelist. */
    private static Comparator<ExamRegistrationDTO> comparatorFor(String column) {
        return switch (column) {
            case "name" -> Comparator.comparing(c -> normalize(c.getName()));
            case "clazz" -> Comparator.comparing(c -> normalize(c.getClazz()));
            case "govIdNo" -> Comparator.comparing(c -> normalize(c.getGovIdNo()));
            case "dob" -> Comparator.comparingLong(ExamRegistrationSort::dobMillis);
            case "theoryScore" -> Comparator.comparingInt(c -> scoreOrMinus(c.getTheoryScore()));
            case "practicalScore" -> Comparator.comparingInt(c -> scoreOrMinus(c.getPracticalScore()));
            default -> Comparator.comparingInt(ExamRegistrationSort::sbdNumber);
        };
    }

    /** Số nguyên từ chữ số trong SBD; lỗi → -1. */
    private static int sbdNumber(ExamRegistrationDTO c) {
        if (c == null || c.getSbd() == null) {
            return -1;
        }
        String digits = c.getSbd().replaceAll("\\D", "");
        if (digits.isEmpty()) {
            return -1;
        }
        try {
            return Integer.parseInt(digits);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /** epoch millis của ngày sinh; null → 0. */
    private static long dobMillis(ExamRegistrationDTO c) {
        Date dob = c != null ? c.getDateOfBirth() : null;
        return dob != null ? dob.getTime() : 0L;
    }

    /** Điểm null coi như -1 khi so sánh. */
    private static int scoreOrMinus(Integer score) {
        return score != null ? score : -1;
    }

    /** Chuẩn hóa chuỗi so sánh (trim, {@code "-"} → rỗng, lower-case). */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        String text = value.trim();
        return "-".equals(text) ? "" : text.toLowerCase();
    }
}
