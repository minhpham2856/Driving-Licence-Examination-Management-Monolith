package Config;

/**
 * <b>Đọc cấu hình cổng thanh toán SEPay từ file .env</b> (thông qua {@link EnvLoader}).
 *
 * <p><b>Đọc file này khi nào?</b> Khi cần biết URL checkout, IPN, merchant ID đang dùng gì,
 * hoặc khi debug lỗi "Yêu cầu không hợp lệ" / thanh toán xong mà DB vẫn Pending.</p>
 *
 * <h3>Hai loại URL quan trọng (hay nhầm lẫn)</h3>
 * <ul>
 *   <li><b>appBaseUrl</b> — URL public (ngrok / domain thật). SEPay <i>server</i> gọi IPN về đây.
 *       Máy dev chạy localhost thì SEPay không gọi được → phải dùng ngrok.</li>
 *   <li><b>returnBaseUrl</b> — URL trình duyệt <i>thí sinh</i> quay về sau thanh toán.
 *       Thường là localhost khi dev.</li>
 * </ul>
 *
 * <h3>Các biến .env</h3>
 * <ul>
 *   <li>{@code sepay.env} — {@code sandbox} (test) hoặc {@code production} (tiền thật)</li>
 *   <li>{@code sepay.merchantId} — {@code SP-TEST-*} hoặc {@code SP-LIVE-*}</li>
 *   <li>{@code sepay.secretKey} — ký form checkout + xác thực IPN (header X-Secret-Key)</li>
 *   <li>{@code sepay.checkoutUrl} — endpoint POST form sang SEPay</li>
 *   <li>{@code sepay.appBaseUrl} — base URL cho IPN webhook</li>
 *   <li>{@code sepay.returnBaseUrl} — base URL cho success/error/cancel</li>
 *   <li>{@code sepay.debug} — {@code true} mới mở trang /sepay-status (dev only)</li>
 * </ul>
 */
public final class SepayConfig {

    /** URL mặc định khi test — không dùng tài khoản ngân hàng production. */
    private static final String SANDBOX_CHECKOUT = "https://pay-sandbox.sepay.vn/v1/checkout/init";
    /** URL khi go-live — QR hiện TK ngân hàng đã liên kết merchant SP-LIVE. */
    private static final String PRODUCTION_CHECKOUT = "https://pay.sepay.vn/v1/checkout/init";
    /** Giá trị mẫu trong .env.example — coi như chưa cấu hình. */
    private static final String PLACEHOLDER_MERCHANT = "YOUR_SANDBOX_MERCHANT_ID";
    private static final String PLACEHOLDER_SECRET = "YOUR_SANDBOX_SECRET_KEY";

    private SepayConfig() {
    }

    /**
     * Merchant + secret đã điền thật (không còn placeholder YOUR_SANDBOX_...).
     * Service gọi trước khi cho phép đăng ký thi có thanh toán.
     */
    public static boolean isConfigured() {
        String merchantId = getMerchantId();
        String secretKey = getSecretKey();
        return !merchantId.isBlank()
                && !secretKey.isBlank()
                && !PLACEHOLDER_MERCHANT.equalsIgnoreCase(merchantId)
                && !PLACEHOLDER_SECRET.equalsIgnoreCase(secretKey);
    }

    /** Giá trị {@code sepay.env}; mặc định sandbox nếu không khai báo. */
    public static String getEnv() {
        return EnvLoader.get("sepay.env", "sandbox");
    }

    /** {@code true} khi không phải production — dùng để chọn checkout URL mặc định. */
    public static boolean isSandbox() {
        return !"production".equalsIgnoreCase(getEnv());
    }

    /**
     * Bật trang chẩn đoán {@code /registrant/payment/sepay-status}.
     * Chỉ đặt {@code sepay.debug=true} khi dev; production luôn false (trả 404).
     */
    public static boolean isDebugEnabled() {
        return "true".equalsIgnoreCase(trim(EnvLoader.get("sepay.debug", "false")));
    }

    /**
     * Mã merchant trên my.sepay.vn — gửi trong field {@code merchant} của form checkout.
     * SP-LIVE = production, SP-TEST = sandbox.
     */
    public static String getMerchantId() {
        return trim(EnvLoader.get("sepay.merchantId", ""));
    }

    /**
     * Khóa bí mật merchant — dùng ký HMAC form và so khớp header IPN {@code X-Secret-Key}.
     * Không hiển thị ra UI.
     */
    public static String getSecretKey() {
        return trim(EnvLoader.get("sepay.secretKey", ""));
    }

    private static String trim(String value) {
        return value != null ? value.trim() : "";
    }

    /** Merchant production (tiền vào TK thật đã liên kết trên my.sepay.vn). */
    public static boolean isLiveMerchant() {
        return getMerchantId().toUpperCase().startsWith("SP-LIVE");
    }

    /** Merchant test sandbox. */
    public static boolean isTestMerchant() {
        return getMerchantId().toUpperCase().startsWith("SP-TEST");
    }

    /**
     * URL mà form HTML POST tới khi bắt đầu thanh toán.
     * Ưu tiên {@code sepay.checkoutUrl} trong .env; không có thì suy từ {@link #isSandbox()}.
     */
    public static String getCheckoutUrl() {
        String url = trim(EnvLoader.get("sepay.checkoutUrl", ""));
        if (!url.isBlank()) {
            return url;
        }
        return isSandbox() ? SANDBOX_CHECKOUT : PRODUCTION_CHECKOUT;
    }

    /**
     * Phát hiện cấu hình lệch môi trường — nguyên nhân phổ biến QR không hiện TK ngân hàng thật.
     * <p>Ví dụ: merchant SP-LIVE nhưng checkoutUrl vẫn trỏ pay-sandbox.sepay.vn.</p>
     *
     * @return chuỗi cảnh báo tiếng Việt, hoặc {@code null} nếu khớp
     */
    public static String environmentMismatchWarning() {
        if (getMerchantId().isBlank()) {
            return null;
        }

        String checkout = getCheckoutUrl().toLowerCase();
        boolean sandboxCheckout = checkout.contains("sandbox");

        if (isLiveMerchant() && (isSandbox() || sandboxCheckout)) {
            return "Bạn đang dùng merchant SP-LIVE (production) nhưng sepay.env hoặc checkoutUrl vẫn là sandbox. "
                    + "Để hiện tài khoản ngân hàng thật: sepay.env=production và "
                    + "sepay.checkoutUrl=https://pay.sepay.vn/v1/checkout/init";
        }
        if (isTestMerchant() && (!isSandbox() || !sandboxCheckout)) {
            return "Merchant SP-TEST phải dùng sandbox: sepay.env=sandbox và "
                    + "sepay.checkoutUrl=https://pay-sandbox.sepay.vn/v1/checkout/init";
        }
        return null;
    }

    /**
     * URL gốc ứng dụng mà SEPay có thể gọi từ internet (ngrok / domain deploy).
     * Bắt buộc có để IPN hoạt động — localhost không nhận được webhook từ SEPay.
     */
    public static String getAppBaseUrl() {
        return stripTrailingSlash(nullToEmpty(EnvLoader.get("sepay.appBaseUrl", "")));
    }

    /**
     * URL gốc cho trình duyệt quay về sau thanh toán (thường localhost khi dev).
     * Nếu không khai báo → fallback {@link #getAppBaseUrl()} (có thể redirect về ngrok).
     */
    public static String getReturnBaseUrl() {
        String configured = stripTrailingSlash(nullToEmpty(EnvLoader.get("sepay.returnBaseUrl", "")));
        if (!configured.isBlank()) {
            return configured;
        }
        return getAppBaseUrl();
    }

    /**
     * URL webhook IPN — khai báo trùng trên my.sepay.vn.
     * SEPay POST JSON {@code ORDER_PAID} về đây khi khách chuyển tiền thành công.
     */
    public static String ipnUrl() {
        return getAppBaseUrl() + "/registrant/payment/sepay-ipn";
    }

    /** Trang hiển thị khi thanh toán thành công (chỉ UX; DB cập nhật qua IPN). */
    public static String successUrl() {
        return getReturnBaseUrl() + "/registrant/payment/sepay-success";
    }

    /** Trang hiển thị khi thanh toán lỗi. */
    public static String errorUrl() {
        return getReturnBaseUrl() + "/registrant/payment/sepay-error";
    }

    /** Trang hiển thị khi thí sinh hủy trên cổng thanh toán. */
    public static String cancelUrl() {
        return getReturnBaseUrl() + "/registrant/payment/sepay-cancel";
    }

    /** Mô tả file .env nào đã được nạp — dùng log / trang debug. */
    public static String describeLoadState() {
        return EnvLoader.getLoadSummary();
    }

    /** Bỏ dấu / cuối URL để nối path an toàn (tránh //registrant/...). */
    private static String stripTrailingSlash(String url) {
        if (url == null || url.isBlank()) {
            return "";
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private static String nullToEmpty(String value) {
        return trim(value);
    }
}
