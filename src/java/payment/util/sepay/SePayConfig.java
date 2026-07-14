package payment.util.sepay;

import shared.ConfigManager;

/** Cấu hình SePay từ .env/env: MERCHANT_ID+SECRET_KEY (checkout), IPN_SECRET, ENV, APP_BASE_URL. */
public final class SePayConfig {

    private SePayConfig() {
    }

    public static boolean isConfigured() {
        return !blank(merchantId()) && !blank(secretKey());
    }

    public static String merchantId() {
        return ConfigManager.get("SEPAY_MERCHANT_ID", "");
    }

    public static String secretKey() {
        return ConfigManager.get("SEPAY_SECRET_KEY", "");
    }

    public static String ipnSecret() {
        return ConfigManager.get("SEPAY_IPN_SECRET", secretKey());
    }

    public static boolean sandbox() {
        String env = ConfigManager.get("SEPAY_ENV", SePayConstants.ENV_SANDBOX);
        return !SePayConstants.ENV_PRODUCTION.equalsIgnoreCase(env.trim());
    }

    public static String checkoutInitUrl() {
        String override = ConfigManager.get("SEPAY_CHECKOUT_URL");
        if (!blank(override)) {
            return override.trim();
        }
        String base = sandbox()
                ? ConfigManager.get("SEPAY_SANDBOX_PGAPI_URL", "https://pgapi-sandbox.sepay.vn")
                : ConfigManager.get("SEPAY_PRODUCTION_PGAPI_URL", "https://pgapi.sepay.vn");
        return trimTrailingSlash(base) + SePayConstants.CHECKOUT_INIT_PATH;
    }

    public static String appBaseUrl() {
        return trimTrailingSlash(ConfigManager.get("SEPAY_APP_BASE_URL", ""));
    }

    public static String defaultSuccessUrl() {
        return ConfigManager.get("SEPAY_SUCCESS_URL", appBaseUrl() + "/payment/sepay/success");
    }

    public static String defaultErrorUrl() {
        return ConfigManager.get("SEPAY_ERROR_URL", appBaseUrl() + "/payment/sepay/error");
    }

    public static String defaultCancelUrl() {
        return ConfigManager.get("SEPAY_CANCEL_URL", appBaseUrl() + "/payment/sepay/cancel");
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
