package payment.dto.sepay;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * DTO phiên checkout SePay sau khi ký form (bước đầu luồng checkout → IPN → return).
 * Chứa URL cổng pay.sepay.vn/.../checkout/init, order_invoice_number
 * (mã DLEM-CHK-... gắn Candidate/Enrollment) và map field POST đã ký HMAC.
 * Desk dùng buildAutoSubmitHtml để chuyển khách sang SePay; chưa ghi bảng Payment — ghi nhận thật sự qua webhook IPN.
 */
public class SePayCheckoutSession {

    private String checkoutUrl;
    private String orderInvoiceNumber;
    private final Map<String, String> formFields = new LinkedHashMap<>();

    public String getCheckoutUrl() {
        return checkoutUrl;
    }

    public void setCheckoutUrl(String checkoutUrl) {
        this.checkoutUrl = checkoutUrl;
    }

    public String getOrderInvoiceNumber() {
        return orderInvoiceNumber;
    }

    public void setOrderInvoiceNumber(String orderInvoiceNumber) {
        this.orderInvoiceNumber = orderInvoiceNumber;
    }

    public Map<String, String> getFormFields() {
        return Collections.unmodifiableMap(formFields);
    }

    public void putFormField(String key, String value) {
        formFields.put(key, value);
    }
}
