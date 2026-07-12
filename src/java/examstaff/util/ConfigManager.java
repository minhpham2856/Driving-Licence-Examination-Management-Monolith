package examstaff.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public final class ConfigManager {

    private static final Properties props = new Properties();

    static {
        loadProperties();
    }

    private ConfigManager() {
    }

    private static void loadProperties() {
        try (InputStream input = openConfigStream()) {
            if (input != null) {
                props.load(new InputStreamReader(input, StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
        }
    }

    private static InputStream openConfigStream() {
        ClassLoader cl = ConfigManager.class.getClassLoader();
        InputStream is = cl.getResourceAsStream("config/config.props");
        return (is != null) ? is : cl.getResourceAsStream("config/.env");
    }

    public static String get(String key) {
        String envValue = System.getenv(key);
        if (envValue != null) {
            return envValue;
        }
        return props.getProperty(key);
    }

    public static String get(String key, String defaultValue) {
        String value = get(key);
        return (value != null) ? value : defaultValue;
    }
}
