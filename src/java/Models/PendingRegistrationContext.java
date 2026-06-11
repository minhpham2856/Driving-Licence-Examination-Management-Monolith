package Models;

import java.math.BigDecimal;
import java.sql.Timestamp;

/** Đăng ký đang chờ thanh toán — dùng để tiếp tục checkout trong cửa sổ thời gian. */
public class PendingRegistrationContext {

    private int registrationId;
    private int personId;
    private int examSessionId;
    private String invoiceNumber;
    private BigDecimal amount;
    private String licenceCode;
    private String sessionName;
    private Timestamp paymentExpiresAt;

    public int getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(int registrationId) {
        this.registrationId = registrationId;
    }

    public int getPersonId() {
        return personId;
    }

    public void setPersonId(int personId) {
        this.personId = personId;
    }

    public int getExamSessionId() {
        return examSessionId;
    }

    public void setExamSessionId(int examSessionId) {
        this.examSessionId = examSessionId;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getLicenceCode() {
        return licenceCode;
    }

    public void setLicenceCode(String licenceCode) {
        this.licenceCode = licenceCode;
    }

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public Timestamp getPaymentExpiresAt() {
        return paymentExpiresAt;
    }

    public void setPaymentExpiresAt(Timestamp paymentExpiresAt) {
        this.paymentExpiresAt = paymentExpiresAt;
    }
}
