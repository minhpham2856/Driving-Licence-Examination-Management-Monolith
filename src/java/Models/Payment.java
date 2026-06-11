package Models;

import java.sql.Timestamp;

public class Payment {
    private int id;
    private int examRegistrationId;
    private double amount;
    private String paymentStatus; // 'Pending', 'Completed', 'Failed', 'Refunded'
    private String paymentMethod; // 'Cash', 'BankTransfer'
    private Timestamp paymentDate;
    private String transactionReference;
    private String notes;

    public Payment() {
    }

    public Payment(int id, int examRegistrationId, double amount, String paymentStatus, String paymentMethod, Timestamp paymentDate, String transactionReference, String notes) {
        this.id = id;
        this.examRegistrationId = examRegistrationId;
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

    public int getExamRegistrationId() {
        return examRegistrationId;
    }

    public void setExamRegistrationId(int examRegistrationId) {
        this.examRegistrationId = examRegistrationId;
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
