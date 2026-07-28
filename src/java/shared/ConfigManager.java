package shared;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public final class ConfigManager {

    // Reload .env every 3 seconds.
    private static final long RELOAD_INTERVAL_MS = 3000;

    // Cached environment values.
    private static volatile Map<String, String> ENV = loadDotEnv();

    // Last reload timestamp.
    private static volatile long lastLoadedAt = System.currentTimeMillis();

    private ConfigManager() {
    }

    // Returns config value, preferring OS environment variables.
    public static String get(String key) {
        maybeReload();
        String fromOs = System.getenv(key);
        if (fromOs != null && !fromOs.isBlank()) {
            return fromOs.trim();
        }
        return ENV.get(key);
    }

    // Reloads .env if the cache has expired.
    private static synchronized void maybeReload() {
        long now = System.currentTimeMillis();
        if (now - lastLoadedAt < RELOAD_INTERVAL_MS) {
            return;
        }
        lastLoadedAt = now;
        ENV = loadDotEnv();
    }

    // Returns config value or the default.
    public static String get(String key, String defaultValue) {
        String value = get(key);
        return value != null ? value : defaultValue;
    }

    // Returns config value as an integer.
    public static int getInt(String key, int defaultValue) {
        String value = get(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    // Returns config value as a boolean.
    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value.trim());
    }

    // Loads configuration from project-root .env only.
    private static Map<String, String> loadDotEnv() {
        Map<String, String> map = new HashMap<>();
        Path path = envFilePath();
        if (path == null || !Files.isRegularFile(path)) {
            return map;
        }
        try {
            parseFile(path, map);
        } catch (IOException ignored) {
        }
        return map;
    }

    // Single location: <projectRoot>/.env
    private static Path envFilePath() {
        Path projectRoot = projectRoot();
        if (projectRoot == null) {
            return null;
        }
        return projectRoot.resolve(".env");
    }

    // Parses key-value pairs from a .env file.
    private static void parseFile(Path path, Map<String, String> map) throws IOException {
        for (String line : Files.readAllLines(path)) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }
            int eq = trimmed.indexOf('=');
            if (eq <= 0) {
                continue;
            }
            String key = trimmed.substring(0, eq).trim();
            String value = trimmed.substring(eq + 1).trim();
            if ((value.startsWith("\"") && value.endsWith("\""))
                    || (value.startsWith("'") && value.endsWith("'"))) {
                value = value.substring(1, value.length() - 1);
            }
            if (!System.getenv().containsKey(key)) {
                map.put(key, value);
            }
        }
    }

    // Finds the project root directory (folder that has src/ and .env).
    private static Path projectRoot() {
        Path fromCwd = findRoot(Paths.get(System.getProperty("user.dir", ".")).toAbsolutePath().normalize());
        if (fromCwd != null) {
            return fromCwd;
        }
        try {
            Path start = Paths.get(ConfigManager.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            if (Files.isRegularFile(start)) {
                start = start.getParent();
            }
            return findRoot(start.toAbsolutePath().normalize());
        } catch (Exception ignored) {
            return null;
        }
    }

    // Searches parent directories for the project root.
    private static Path findRoot(Path start) {
        Path current = start;
        for (int i = 0; i < 10 && current != null; i++) {
            if (Files.isDirectory(current.resolve("src"))
                    && Files.isRegularFile(current.resolve(".env"))) {
                return current;
            }
            current = current.getParent();
        }
        return null;
    }
}
