package examstaff.enums;

/**
 * Trạng thái thanh toán lệ phí (giá trị hiển thị / khớp chuỗi CSDL).
 * Hỗ trợ cả nhãn VI và alias EN legacy ({@link #COMPLETED}, {@link #PAID}).
 */
public enum PaymentStatus {
    /** Đã hoàn tất thanh toán (nhãn VI chính). */
    HOAN_TAT("Hoàn tất"),
    /** Chờ thanh toán — mặc định khi chuỗi không khớp. */
    CHO_THANH_TOAN("Chờ thanh toán"),
    /** Đồng nghĩa hoàn tất (EN — legacy DB). */
    COMPLETED("Completed"),
    /** Đồng nghĩa đã thanh toán (EN — legacy DB). */
    PAID("Paid");

    /** Chuỗi khớp với CSDL / UI (so sánh không phân biệt hoa thường). */
    private final String displayName;

    /**
     * Gán chuỗi hiển thị / so khớp cho hằng trạng thái.
     *
     * @param displayName nhãn VI hoặc EN legacy
     */
    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Lấy chuỗi trạng thái dùng để hiển thị hoặc so khớp.
     *
     * @return display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * So khớp chuỗi trạng thái thanh toán với {@link #displayName} (ignore case).
     *
     * @param value chuỗi từ DB/UI
     * @return {@code true} nếu khớp hằng này
     */
    public boolean matches(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        return displayName.equalsIgnoreCase(value.trim());
    }

    /**
     * Chuẩn hóa chuỗi về enum; không khớp → {@link #CHO_THANH_TOAN}.
     * <p>
     * Duyệt toàn bộ {@link #values()} và gọi {@link #matches}.
     *
     * @param value chuỗi trạng thái (null/blank → chờ thanh toán)
     * @return enum tương ứng
     */
    public static PaymentStatus normalize(String value) {
        // Bước 1: thiếu dữ liệu → mặc định chờ
        if (value == null || value.isBlank()) {
            return CHO_THANH_TOAN;
        }
        String trimmed = value.trim();
        // Bước 2: khớp từng hằng theo displayName
        for (PaymentStatus status : values()) {
            if (status.matches(trimmed)) {
                return status;
            }
        }
        // Bước 3: không biết → chờ thanh toán
        return CHO_THANH_TOAN;
    }

    /**
     * Đã thanh toán xong nếu normalize ra {@link #HOAN_TAT}, {@link #COMPLETED} hoặc {@link #PAID}.
     *
     * @param value chuỗi trạng thái
     * @return {@code true} nếu đã hoàn tất
     */
    public static boolean isCompleted(String value) {
        PaymentStatus status = normalize(value);
        return status == HOAN_TAT || status == COMPLETED || status == PAID;
    }

    /**
     * Danh sách giá trị PaymentStatus hợp lệ khi ghi/đọc CSDL (đoạn IN clause).
     * Chỉ gồm các trạng thái “đã hoàn tất” (không gồm chờ thanh toán).
     *
     * @return chuỗi {@code N'…', N'…', N'…'}
     */
    public static String sqlInClause() {
        return "N'" + HOAN_TAT.displayName + "', N'" + COMPLETED.displayName + "', N'" + PAID.displayName + "'";
    }
}
