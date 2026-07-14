package examstaff.enums;

/** Phương thức thanh toán lệ phí trong ExamStaff. */
public enum PaymentMethod {
    /** Thanh toán tiền mặt. */
    CASH("Cash");

    private final String code;

    PaymentMethod(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
