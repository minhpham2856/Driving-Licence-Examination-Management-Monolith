package Models.payment.sepay;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Kết quả tạo phiên checkout — dùng redirect form POST tới SePay. */
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
