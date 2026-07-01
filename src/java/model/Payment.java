package model;
import java.sql.Timestamp;
public class Payment {
    private int paymentId;
    private String paymentStatus;
    private String paymentMethod;
    private String transactionReference;
    private double totalAmount;
    private Timestamp paidAt;
    private int examEnrollmentId;
    public Payment() {
    }
    public Payment(int paymentId, String paymentStatus, String paymentMethod, String transactionReference,
            double totalAmount, Timestamp paidAt, int examEnrollmentId) {
        this.paymentId = paymentId;
        this.paymentStatus = paymentStatus;
        this.paymentMethod = paymentMethod;
        this.transactionReference = transactionReference;
        this.totalAmount = totalAmount;
        this.paidAt = paidAt;
        this.examEnrollmentId = examEnrollmentId;
    }
    public int getPaymentId() {
        return paymentId;
    }
    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }
    public int getId() {
        return paymentId;
    }
    public void setId(int paymentId) {
        this.paymentId = paymentId;
    }
    public String getPaymentStatus() {
        return paymentStatus;
    }
    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
    public String getPaymentMethod() {
        return paymentMethod;
    }
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    public String getTransactionReference() {
        return transactionReference;
    }
    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }
    public double getTotalAmount() {
        return totalAmount;
    }
    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }
    public Timestamp getPaidAt() {
        return paidAt;
    }
    public void setPaidAt(Timestamp paidAt) {
        this.paidAt = paidAt;
    }
    public int getExamEnrollmentId() {
        return examEnrollmentId;
    }
    public void setExamEnrollmentId(int examEnrollmentId) {
        this.examEnrollmentId = examEnrollmentId;
    }
}
