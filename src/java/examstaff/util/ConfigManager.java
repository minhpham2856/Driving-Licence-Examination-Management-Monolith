package examstaff.util;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
/** Đọc cấu hình từ classpath ({@code config.props} / {@code .env}) và biến môi trường. */
public final class ConfigManager {
    private static final Properties props = new Properties();
    static {
        loadProperties();
    }
    private ConfigManager() {
    }
    /** Nạp properties UTF-8 từ classpath (im lặng nếu lỗi I/O). */
    private static void loadProperties() {
        try (InputStream input = openConfigStream()) {
            if (input != null) {
                props.load(new InputStreamReader(input, StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
        }
    }
    /** Mở stream {@code config/config.props}, fallback {@code config/.env}. */
    private static InputStream openConfigStream() {
        ClassLoader cl = ConfigManager.class.getClassLoader();
        InputStream is = cl.getResourceAsStream("config/config.props");
        return (is != null) ? is : cl.getResourceAsStream("config/.env");
    }
    /**
     * Lấy giá trị: ưu tiên env, rồi file properties.
     *
     * @param key khóa cấu hình
     * @return giá trị hoặc {@code null}
     */
    public static String get(String key) {
        String envValue = System.getenv(key);
        if (envValue != null) {
            return envValue;
        }
        return props.getProperty(key);
    }
    /**
     * Như {@link #get(String)} nhưng có default khi thiếu.
     *
     * @param key          khóa
     * @param defaultValue giá trị mặc định
     * @return giá trị hoặc {@code defaultValue}
     */
    public static String get(String key, String defaultValue) {
        String value = get(key);
        return (value != null) ? value : defaultValue;
    }
}
