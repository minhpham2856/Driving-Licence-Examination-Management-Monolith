package Models;

import java.math.BigDecimal;

public class PaymentRecord {

    private int id;
    private int examRegistrationId;
    private int examSessionId;
    private BigDecimal amount;
    private String paymentStatus;
    private String paymentMethod;
    private String transactionReference;
    private java.sql.Timestamp paymentExpiresAt;
    private boolean registrationCancelled;

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

    public int getExamSessionId() {
        return examSessionId;
    }

    public void setExamSessionId(int examSessionId) {
        this.examSessionId = examSessionId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
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

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public java.sql.Timestamp getPaymentExpiresAt() {
        return paymentExpiresAt;
    }

    public void setPaymentExpiresAt(java.sql.Timestamp paymentExpiresAt) {
        this.paymentExpiresAt = paymentExpiresAt;
    }

    public boolean isRegistrationCancelled() {
        return registrationCancelled;
    }

    public void setRegistrationCancelled(boolean registrationCancelled) {
        this.registrationCancelled = registrationCancelled;
    }
}
