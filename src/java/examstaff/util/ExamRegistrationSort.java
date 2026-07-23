package examstaff.util;

import examstaff.dto.ExamRegistrationDTO;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Utility sắp xếp server-side danh sách {@link ExamRegistrationDTO} theo cột và hướng
 * — whitelist cột an toàn, alias {@code cccd} → {@code govIdNo}.
 *
 * Vai trò trong luồng examstaff:
 * Các màn allocation, dashboard và báo cáo nhận tham số {@code sort}/{@code dir} từ URL;
 * helper parse thành {@link Spec} rồi {@link #sort} mutate list tại chỗ trước khi bind JSP
 * hoặc export — tránh sort không kiểm soát trên cột JDBC.
 *
 * Cách hoạt động:
 * - {@link #parse} — trim, alias cccd, whitelist ({@code sbd}, {@code name}, {@code clazz},
 *       {@code govIdNo}, {@code dob}, {@code theoryScore}, {@code practicalScore}); invalid → default SBD asc.
 * - {@link #sort} — no-op nếu &lt; 2 phần tử; SBD parse số từ chữ số; điểm null = -1.
 *
 * Ai gọi:
 * {@code AllocationStageViewServiceImpl}, {@code AllocationServlet}, {@code ReportServlet},
 * {@code CandidateQueueQueryServiceImpl} — paging/sort bảng thí sinh trên UI staff.
 */
public final class ExamRegistrationSort {

    /** Cột sắp xếp mặc định khi tham số không hợp lệ. */
    public static final String DEFAULT_COLUMN = "sbd";
    /** Chiều mặc định (tăng dần). */
    public static final String DEFAULT_DIRECTION = "asc";

    /** Tập cột được phép sort (sau khi alias cccd → govIdNo). */
    private static final Set<String> ALLOWED_COLUMNS = Set.of(
            "sbd", "name", "clazz", "govIdNo", "cccd", "dob",
            "theoryScore", "practicalScore");

    /** Không cho khởi tạo — chỉ dùng static. */
    private ExamRegistrationSort() {
    }

    /**
     * Đặc tả cột + chiều sắp xếp đã chuẩn hóa (whitelist + ascending flag).
     */
    public static final class Spec {
        private final String column;
        private final boolean ascending;

        /**
         * Tạo đặc tả sort đã được caller đảm bảo hợp lệ.
         * @param column    tên cột đã whitelist
         * @param ascending {@code true} = tăng dần
         */
        public Spec(String column, boolean ascending) {
            this.column = column;
            this.ascending = ascending;
        }

        /**
         * @return tên cột sắp xếp
         */
        public String getColumn() {
            return column;
        }

        /**
         * @return {@code true} nếu tăng dần
         */
        public boolean isAscending() {
            return ascending;
        }
    }

    /**
     * Parse tham số sort/dir thành {@link Spec} (alias {@code cccd} → {@code govIdNo}).
     * <p>
 *
     * Luồng:
     * - Trim sort; null → {@link #DEFAULT_COLUMN}
     * - {@code cccd} → {@code govIdNo}
     * - Không nằm whitelist → default column
     * - dir khác {@code desc} (không phân biệt hoa thường) → tăng dần
     * @param sort tên cột (invalid → default)
     * @param dir  {@code desc} = giảm; còn lại = tăng
     * @return spec hợp lệ
     */
    public static Spec parse(String sort, String dir) {
        // Bước 1: cột thô hoặc mặc định
        String column = sort != null ? sort.trim() : DEFAULT_COLUMN;
        // Bước 2: alias UI → field nội bộ
        if ("cccd".equals(column)) {
            column = "govIdNo";
        }
        // Bước 3: whitelist
        if (!ALLOWED_COLUMNS.contains(column)) {
            column = DEFAULT_COLUMN;
        }
        // Bước 4: chiều sắp xếp
        boolean ascending = !"desc".equalsIgnoreCase(dir);
        return new Spec(column, ascending);
    }

    /**
     * Sắp xếp tại chỗ danh sách theo {@link Spec}.
     * <p>
     * No-op khi list null, &lt; 2 phần tử, hoặc spec null.
     * Lấy comparator theo cột rồi reverse nếu giảm dần.
     * @param rows danh sách (mutate nếu ≥ 2 phần tử)
     * @param spec đặc tả sắp xếp
     */
    public static void sort(List<ExamRegistrationDTO> rows, Spec spec) {
        // Bước 1: điều kiện tối thiểu để sort
        if (rows == null || rows.size() < 2 || spec == null) {
            return;
        }
        // Bước 2: comparator theo cột
        Comparator<ExamRegistrationDTO> comparator = comparatorFor(spec.getColumn());
        // Bước 3: đảo chiều nếu desc
        if (!spec.isAscending()) {
            comparator = comparator.reversed();
        }
        rows.sort(comparator);
    }

    /**
     * Tạo {@link Comparator} theo tên cột whitelist (mặc định so sánh SBD số).
     * @param column tên cột đã parse
     * @return comparator không null
     */
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

    /**
     * Rút số nguyên từ chữ số trong SBD; thiếu/lỗi → {@code -1} (xếp trước/sau tùy chiều).
     * @param c hồ sơ thí sinh
     * @return số SBD hoặc -1
     */
    private static int sbdNumber(ExamRegistrationDTO c) {
        if (c == null || c.getSbd() == null) {
            return -1;
        }
        // Chỉ giữ chữ số rồi parse
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

    /**
     * Epoch millis của ngày sinh; null DTO/DOB → {@code 0}.
     * @param c hồ sơ thí sinh
     * @return millis hoặc 0
     */
    private static long dobMillis(ExamRegistrationDTO c) {
        Date dob = c != null ? c.getDateOfBirth() : null;
        return dob != null ? dob.getTime() : 0L;
    }

    /**
     * Điểm null coi như {@code -1} khi so sánh (chưa có điểm xếp trước khi tăng dần).
     * @param score điểm hoặc null
     * @return giá trị so sánh
     */
    private static int scoreOrMinus(Integer score) {
        return score != null ? score : -1;
    }

    /**
     * Chuẩn hóa chuỗi so sánh: trim, token {@code "-"} → rỗng, lower-case.
     * @param value chuỗi gốc
     * @return khóa so sánh (không null)
     */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        String text = value.trim();
        return "-".equals(text) ? "" : text.toLowerCase();
    }
}
