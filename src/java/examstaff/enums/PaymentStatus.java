package examstaff.enums;

/**
 * Enum trạng thái thanh toán lệ phí thủ tục — khớp chuỗi VI trên UI và alias EN legacy
 * trên CSDL cũ (COMPLETED, PAID).
 *
 * Vai trò trong luồng examstaff:
 * Xác định thí sinh đã thu phí hay còn chờ trước khi vào hàng đợi gọi / phân bổ phòng.
 * normalize mặc định CHO_THANH_TOAN khi chuỗi không khớp.
 * isCompleted coi HOAN_TAT, COMPLETED, PAID là đã thanh toán.
 * sqlInClause cung cấp đoạn IN cho query JDBC lọc payment hoàn tất.
 *
 * Giá trị trạng thái:
 * - HOAN_TAT — nhãn VI chính thức “Hoàn tất”.
 * - CHO_THANH_TOAN — mặc định khi chưa thu / không nhận diện được.
 * - COMPLETED, PAID — đồng nghĩa hoàn tất (EN legacy).
 *
 * Ai sử dụng:
 * ProcedurePaymentServiceImpl, ProcedureFeeQueryServiceImpl,
 * PaymentDAOImpl, ExamStaffCandidateViewDAOImpl, ExamRegistrationDAOImpl,
 * CandidateCallServlet — cờ isPaymentCompleted trên ExamRegistrationDTO.
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
     * @param displayName nhãn VI hoặc EN legacy
     */
    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Lấy chuỗi trạng thái dùng để hiển thị hoặc so khớp.
     * @return display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * So khớp chuỗi trạng thái thanh toán với displayName (ignore case).
     * @param value chuỗi từ DB/UI
     * @return true nếu khớp hằng này
     */
    public boolean matches(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        return displayName.equalsIgnoreCase(value.trim());
    }

    /**
     * Chuẩn hóa chuỗi về enum; không khớp → CHO_THANH_TOAN.
     * <p>
     * Duyệt toàn bộ values() và gọi matches.
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
     * Đã thanh toán xong nếu normalize ra HOAN_TAT, COMPLETED hoặc PAID.
     * @param value chuỗi trạng thái
     * @return true nếu đã hoàn tất
     */
    public static boolean isCompleted(String value) {
        PaymentStatus status = normalize(value);
        return status == HOAN_TAT || status == COMPLETED || status == PAID;
    }

    /**
     * Danh sách giá trị PaymentStatus hợp lệ khi ghi/đọc CSDL (đoạn IN clause).
     * Chỉ gồm các trạng thái “đã hoàn tất” (không gồm chờ thanh toán).
     * @return chuỗi N'…', N'…', N'…'
     */
    public static String sqlInClause() {
        return "N'" + HOAN_TAT.displayName + "', N'" + COMPLETED.displayName + "', N'" + PAID.displayName + "'";
    }
}
