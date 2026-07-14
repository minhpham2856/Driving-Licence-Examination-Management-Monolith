package payment.dto;

import java.sql.Timestamp;

public class PaymentRecord {
    private int id;
    /** CandidateId — dùng resolve ExamEnrollmentId khi insert. */
    private int candidateId;
    /** FK Payment.ExamEnrollmentId (DLEM_DB_2). */
    private int examEnrollmentId;
    private double amount;
    private String paymentStatus;
    private String paymentMethod;
    private Timestamp paymentDate;
    private String transactionReference;
    private String notes;

    public PaymentRecord() {
    }

    public PaymentRecord(int id, int candidateId, double amount, String paymentStatus,
            String paymentMethod, Timestamp paymentDate, String transactionReference, String notes) {
        this.id = id;
        this.candidateId = candidateId;
        this.amount = amount;
        this.paymentStatus = paymentStatus;
        this.paymentMethod = paymentMethod;
        this.paymentDate = paymentDate;
        this.transactionReference = transactionReference;
        this.notes = notes;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(int candidateId) {
        this.candidateId = candidateId;
    }

    public int getExamEnrollmentId() {
        return examEnrollmentId;
    }

    public void setExamEnrollmentId(int examEnrollmentId) {
        this.examEnrollmentId = examEnrollmentId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
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

    public Timestamp getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Timestamp paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
