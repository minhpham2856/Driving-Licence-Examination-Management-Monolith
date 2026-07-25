package payment.dto.sepay;

/**
 * DTO đầu vào tạo phiên checkout SePay tại bàn thu lệ phí (bước checkout).
 * Desk điền amountVnd, order_invoice_number (sinh từ SePayInvoice),
 * mô tả đơn và URL success/error/cancel trỏ về SePayReturnServlet.
 * SePayPaymentServiceImpl.createCheckout validate, ký HMAC
 * và chuyển thành SePayCheckoutSession để POST lên cổng SePay.
 */
public class SePayCheckoutRequest {

    private long amountVnd;
    private String orderInvoiceNumber;
    private String orderDescription;
    private String customerId;
    private String paymentMethod;
    private String successUrl;
    private String errorUrl;
    private String cancelUrl;

    public long getAmountVnd() {
        return amountVnd;
    }

    public void setAmountVnd(long amountVnd) {
        this.amountVnd = amountVnd;
    }

    public String getOrderInvoiceNumber() {
        return orderInvoiceNumber;
    }

    public void setOrderInvoiceNumber(String orderInvoiceNumber) {
        this.orderInvoiceNumber = orderInvoiceNumber;
    }

    public String getOrderDescription() {
        return orderDescription;
    }

    public void setOrderDescription(String orderDescription) {
        this.orderDescription = orderDescription;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getSuccessUrl() {
        return successUrl;
    }

    public void setSuccessUrl(String successUrl) {
        this.successUrl = successUrl;
    }

    public String getErrorUrl() {
        return errorUrl;
    }

    public void setErrorUrl(String errorUrl) {
        this.errorUrl = errorUrl;
    }

    public String getCancelUrl() {
        return cancelUrl;
    }

    public void setCancelUrl(String cancelUrl) {
        this.cancelUrl = cancelUrl;
    }
}
