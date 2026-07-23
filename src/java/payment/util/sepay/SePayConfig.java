package payment.util.sepay;

import shared.ConfigManager;

/**
 * Đọc cấu hình SePay từ {@code .env} / biến môi trường (qua {@link ConfigManager}).
 * <p>
 * Key chính trong {@code .env}:
 * <ul>
 *   <li>{@code SEPAY_MERCHANT_ID}, {@code SEPAY_SECRET_KEY} — bắt buộc để checkout</li>
 *   <li>{@code SEPAY_ENV} — {@code sandbox} hoặc {@code production}</li>
 *   <li>{@code SEPAY_APP_BASE_URL} — URL public (thường ngrok) cho <b>IPN</b></li>
 *   <li>{@code SEPAY_RETURN_BASE_URL} — URL trình duyệt (localhost) cho success/error/<b>cancel</b>
 *       để tránh trang “Visit Site” của ngrok free</li>
 * </ul>
 */
public final class SePayConfig {

    private SePayConfig() {
    }

    /** Đủ merchant + secret thì bàn thủ tục cho phép nút Thu SePay. */
    public static boolean isConfigured() {
        return !blank(merchantId()) && !blank(secretKey());
    }

    public static String merchantId() {
        return ConfigManager.get("SEPAY_MERCHANT_ID", "");
    }

    public static String secretKey() {
        return ConfigManager.get("SEPAY_SECRET_KEY", "");
    }

    /** Secret header IPN ({@code X-Secret-Key}); mặc định = SECRET_KEY. */
    public static String ipnSecret() {
        return ConfigManager.get("SEPAY_IPN_SECRET", secretKey());
    }

    /** {@code true} khi không phải production (mặc định sandbox nếu thiếu ENV). */
    public static boolean sandbox() {
        String env = ConfigManager.get("SEPAY_ENV", SePayConstants.ENV_SANDBOX);
        return !SePayConstants.ENV_PRODUCTION.equalsIgnoreCase(env.trim());
    }

    /**
     * URL POST form HTML checkout (QR / chuyển khoản).
     * Dùng domain <b>pay.sepay.vn</b> (sandbox: pay-sandbox), <b>không</b> dùng pgapi
     * — pgapi là REST API; POST form lên đó sẽ 404.
     */
    public static String checkoutInitUrl() {
        String override = ConfigManager.get("SEPAY_CHECKOUT_URL");
        if (!blank(override)) {
            return override.trim();
        }
        // Cho phép override từng môi trường; fallback biến PGAPI cũ nếu ai đã set nhầm
        String base = sandbox()
                ? firstNonBlank(
                        ConfigManager.get("SEPAY_SANDBOX_CHECKOUT_URL"),
                        ConfigManager.get("SEPAY_SANDBOX_PGAPI_URL"),
                        "https://pay-sandbox.sepay.vn")
                : firstNonBlank(
                        ConfigManager.get("SEPAY_PRODUCTION_CHECKOUT_URL"),
                        ConfigManager.get("SEPAY_PRODUCTION_PGAPI_URL"),
                        "https://pay.sepay.vn");
        return trimTrailingSlash(base) + SePayConstants.CHECKOUT_INIT_PATH;
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (!blank(value)) {
                return value.trim();
            }
        }
        return "";
    }

    /** Base public cho IPN webhook (SePay server → app). */
    public static String appBaseUrl() {
        return trimTrailingSlash(ConfigManager.get("SEPAY_APP_BASE_URL", ""));
    }

    /**
     * Base URL khi <b>trình duyệt</b> bị SePay redirect (success / error / cancel).
     * Ưu tiên {@code SEPAY_RETURN_BASE_URL}=localhost khi đang tunnel ngrok free.
     */
    public static String browserReturnBaseUrl() {
        String override = ConfigManager.get("SEPAY_RETURN_BASE_URL");
        if (!blank(override)) {
            return trimTrailingSlash(override);
        }
        return appBaseUrl();
    }

    public static String defaultSuccessUrl() {
        return ConfigManager.get("SEPAY_SUCCESS_URL", browserReturnBaseUrl() + "/payment/sepay/success");
    }

    public static String defaultErrorUrl() {
        return ConfigManager.get("SEPAY_ERROR_URL", browserReturnBaseUrl() + "/payment/sepay/error");
    }

    public static String defaultCancelUrl() {
        return ConfigManager.get("SEPAY_CANCEL_URL", browserReturnBaseUrl() + "/payment/sepay/cancel");
    }

    private static String trimTrailingSlash(String url) {
        if (url == null || url.isBlank()) {
            return "";
        }
        String trimmed = url.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
