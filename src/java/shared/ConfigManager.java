package shared;

import io.github.cdimascio.dotenv.Dotenv;

public final class ConfigManager {

    // load environment variables
    private static final Dotenv DOTENV = Dotenv.configure()
            .ignoreIfMissing()
            .load();

    // get configuration value
    public static String get(String key) {
        return DOTENV.get(key);
    }

    // get configuration value with default
    public static String get(String key, String defaultValue) {
        String value = DOTENV.get(key);

        if (value == null) {
            return defaultValue;
        }

        return value;
    }

    // get integer configuration
    public static int getInt(String key, int defaultValue) {
        String value = DOTENV.get(key);

        if (value == null) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    // get boolean configuration
    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = DOTENV.get(key);

        if (value == null) {
            return defaultValue;
        }

        return Boolean.parseBoolean(value);
    }
}
