package shared.model;

public class PaymentFee {

    private int paymentFeeId;
    private int paymentId;
    private int feeId;
    private Payment payment;
    private Fee fee;

    public PaymentFee() {
    }

    public PaymentFee(int paymentFeeId, int paymentId, int feeId) {
        this.paymentFeeId = paymentFeeId;
        this.paymentId = paymentId;
        this.feeId = feeId;
    }

    public int getPaymentFeeId() {
        return paymentFeeId;
    }

    public void setPaymentFeeId(int paymentFeeId) {
        this.paymentFeeId = paymentFeeId;
    }

    public int getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    public int getFeeId() {
        return feeId;
    }

    public void setFeeId(int feeId) {
        this.feeId = feeId;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public Fee getFee() {
        return fee;
    }

    public void setFee(Fee fee) {
        this.fee = fee;
    }
}

