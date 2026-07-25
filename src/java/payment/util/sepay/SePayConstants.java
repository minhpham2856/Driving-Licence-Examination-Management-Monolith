package payment.util.sepay;

/**
 * Hằng số tham chiếu spec SePay Payment Gateway (developer.sepay.vn).
 * Gom giá trị dùng xuyên suốt checkout (operation, currency, đường dẫn init),
 * IPN (ORDER_PAID, header X-Secret-Key, X-SePay-Signature)
 * và môi trường sandbox/production. Không chứa logic — tránh magic string trong
 * SePayConfig, SePaySignature và SePayIpnParser.
 */
public final class SePayConstants {

    public static final String ENV_SANDBOX = "sandbox";
    public static final String ENV_PRODUCTION = "production";

    public static final String OPERATION_PURCHASE = "PURCHASE";
    public static final String CURRENCY_VND = "VND";

    public static final String NOTIFICATION_ORDER_PAID = "ORDER_PAID";

    public static final String CHECKOUT_INIT_PATH = "/v1/checkout/init";

    public static final String HEADER_SECRET_KEY = "X-Secret-Key";
    public static final String HEADER_SIGNATURE = "X-SePay-Signature";
    public static final String HEADER_TIMESTAMP = "X-SePay-Timestamp";

    private SePayConstants() {
    }
}
