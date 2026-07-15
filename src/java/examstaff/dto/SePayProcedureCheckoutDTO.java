package examstaff.dto;

/** Kết quả tạo checkout SePay từ bàn thủ tục. */
public class SePayProcedureCheckoutDTO {

    public enum Status {
        READY,
        ALREADY_PAID,
        NO_PHOTO,
        NOT_CONFIGURED,
        NO_ENROLLMENT,
        INVALID_AMOUNT,
        PROFILE_NOT_FOUND,
        FAILED
    }

    private Status status = Status.FAILED;
    private String checkoutHtml;
    private String invoiceNumber;
    private String message;
    private ExamRegistrationDTO profile;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getCheckoutHtml() {
        return checkoutHtml;
    }

    public void setCheckoutHtml(String checkoutHtml) {
        this.checkoutHtml = checkoutHtml;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ExamRegistrationDTO getProfile() {
        return profile;
    }

    public void setProfile(ExamRegistrationDTO profile) {
        this.profile = profile;
    }
}
