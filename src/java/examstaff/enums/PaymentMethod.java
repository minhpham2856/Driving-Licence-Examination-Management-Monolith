package examstaff.enums;

/**
 * Enum phương thức thanh toán lệ phí thủ tục trong ExamStaff.
 * Mã {@link #getCode()} khớp giá trị cột Payment trên CSDL (hiện chỉ {@link #CASH}).
 *
 * Vai trò trong luồng examstaff:
 * Khi staff xác nhận thu lệ phí tại bàn thủ tục ({@code ProcedureServlet}),
 * BLL ghi Payment với {@code method = Cash}. Enum giữ contract ổn định giữa UI, service và DAO
 * thay vì hard-code chuỗi rải rác.
 *
 * Giá trị hiện có:
 * - {@link #CASH} — thanh toán tiền mặt tại quầy; {@code code = "Cash"}.
 * <p>Mở rộng SePay/chuyển khoản có thể thêm hằng mới cùng pattern {@code code}.</p>
 *
 * Ai sử dụng:
 * {@code ProcedurePaymentServiceImpl}, {@code ProcedureWorkflowServiceImpl},
 * {@code PaymentDAOImpl} — ghi và đọc bản ghi thanh toán thủ tục.
 */
public enum PaymentMethod {
    /** Thanh toán tiền mặt tại quầy. */
    CASH("Cash");

    /** Mã kỹ thuật lưu trên Payment (ví dụ {@code Cash}). */
    private final String code;

    /**
     * Gán mã phương thức thanh toán.
     * @param code mã EN ngắn
     */
    PaymentMethod(String code) {
        this.code = code;
    }

    /**
     * Lấy mã phương thức để ghi/đọc CSDL.
     * @return mã (ví dụ {@code "Cash"})
     */
    public String getCode() {
        return code;
    }
}
