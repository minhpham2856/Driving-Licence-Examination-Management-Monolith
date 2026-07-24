package examstaff.enums;

/**
 * Enum phương thức thanh toán lệ phí thủ tục trong ExamStaff.
 * Mã getCode() khớp giá trị cột Payment trên CSDL (hiện chỉ CASH).
 *
 * Vai trò trong luồng examstaff:
 * Khi staff xác nhận thu lệ phí tại bàn thủ tục (ProcedureServlet),
 * BLL ghi Payment với method = Cash. Enum giữ contract ổn định giữa UI, service và DAO
 * thay vì hard-code chuỗi rải rác.
 *
 * Giá trị hiện có:
 * - CASH — thanh toán tiền mặt tại quầy; code = "Cash".
 * <p>Mở rộng SePay/chuyển khoản có thể thêm hằng mới cùng pattern code.</p>
 *
 * Ai sử dụng:
 * ProcedurePaymentServiceImpl, ProcedureWorkflowServiceImpl,
 * PaymentDAOImpl — ghi và đọc bản ghi thanh toán thủ tục.
 */
public enum PaymentMethod {
    /** Thanh toán tiền mặt tại quầy. */
    CASH("Cash");

    /** Mã kỹ thuật lưu trên Payment (ví dụ Cash). */
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
     * @return mã (ví dụ "Cash")
     */
    public String getCode() {
        return code;
    }
}
