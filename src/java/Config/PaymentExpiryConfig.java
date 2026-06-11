package Config;

/**
 * Cấu hình thời gian chờ thanh toán sau khi đăng ký (mặc định 15 phút).
 * Có thể ghi đè bằng {@code sepay.paymentWindowMinutes} trong .env.
 */
public final class PaymentExpiryConfig {

    private static final int DEFAULT_WINDOW_MINUTES = 15;
    private static final int MIN_MINUTES = 10;
    private static final int MAX_MINUTES = 15;

    private PaymentExpiryConfig() {
    }

    /** Số phút thí sinh được phép hoàn tất thanh toán (10–15). */
    public static int getWindowMinutes() {
        String raw = EnvLoader.get("sepay.paymentWindowMinutes", String.valueOf(DEFAULT_WINDOW_MINUTES));
        try {
            int value = Integer.parseInt(raw.trim());
            if (value < MIN_MINUTES) {
                return MIN_MINUTES;
            }
            if (value > MAX_MINUTES) {
                return MAX_MINUTES;
            }
            return value;
        } catch (NumberFormatException ex) {
            return DEFAULT_WINDOW_MINUTES;
        }
    }
}
