package examstaff.enums;

/**
 * Phương thức thanh toán lệ phí trong ExamStaff.
 * Hiện chỉ hỗ trợ tiền mặt; mã {@code code} khớp giá trị lưu DB.
 */
public enum PaymentMethod {
    /** Thanh toán tiền mặt tại quầy. */
    CASH("Cash");

    /** Mã kỹ thuật lưu trên Payment (ví dụ {@code Cash}). */
    private final String code;

    /**
     * Gán mã phương thức thanh toán.
     *
     * @param code mã EN ngắn
     */
    PaymentMethod(String code) {
        this.code = code;
    }

    /**
     * Lấy mã phương thức để ghi/đọc CSDL.
     *
     * @return mã (ví dụ {@code "Cash"})
     */
    public String getCode() {
        return code;
    }
}
