package examstaff.dto;

/**
 * DTO kết quả tạo checkout SePay từ bàn thủ tục — trả về BLL → ProcedureServlet
 * sau khi staff khởi tạo thanh toán online (QR/chuyển khoản) thay vì thu tiền mặt.
 *
 * Vai trò trong luồng examstaff:
 *
 * Nhánh SePay trên desk thủ tục (candidatecall.jsp + procedure.jsp):
 * service kiểm tra ảnh, enrollment, cấu hình gateway và số tiền; populate Status,
 * checkoutHtml (iframe/form SePay), invoiceNumber, message và
 * profile cập nhật. Servlet bind attribute để JSP render QR hoặc thông báo lỗi.
 *
 * Enum Status:
 * - Status.READY — checkout sẵn sàng hiển thị.
 * - Status.ALREADY_PAID, Status.NO_PHOTO, Status.NO_ENROLLMENT — điều kiện tiên quyết.
 * - Status.NOT_CONFIGURED, Status.INVALID_AMOUNT, Status.PROFILE_NOT_FOUND — cấu hình/dữ liệu.
 * - Status.FAILED — mặc định / lỗi không phân loại.
 *
 * Ai tạo / tiêu thụ:
 * Tạo: ProcedureServiceImpl / workflow SePay trong support.procedure.
 * Tiêu thụ: ProcedureServlet — action checkout SePay trên bàn thủ tục.
 */
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
