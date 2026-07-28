package shared;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ConfigManager {

    /**
     * Nạp lại file .env tối đa mỗi RELOAD_INTERVAL_MS mili-giây thay vì chỉ đọc
     * một lần lúc classloader khởi tạo. Nhờ vậy sửa .env (vd điền email/app password)
     * có hiệu lực trong vài giây mà KHÔNG cần restart lại Tomcat.
     */
    private static final long RELOAD_INTERVAL_MS = 3000;
    private static volatile Map<String, String> ENV = loadDotEnv();
    private static volatile long lastLoadedAt = System.currentTimeMillis();

    private ConfigManager() {
    }

    // OS env first, then values from .env file.
    public static String get(String key) {
        maybeReload();
        String fromOs = System.getenv(key);
        if (fromOs != null && !fromOs.isBlank()) {
            return fromOs.trim();
        }
        return ENV.get(key);
    }

    private static synchronized void maybeReload() {
        long now = System.currentTimeMillis();
        if (now - lastLoadedAt < RELOAD_INTERVAL_MS) return;
        lastLoadedAt = now;
        ENV = loadDotEnv();
    }

    public static String get(String key, String defaultValue) {
        String value = get(key);
        return value != null ? value : defaultValue;
    }

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

    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value.trim());
    }

    private static Map<String, String> loadDotEnv() {
        Map<String, String> map = new HashMap<>();
        for (Path path : candidatePaths()) {
            if (!Files.isRegularFile(path)) {
                continue;
            }
            try {
                parseFile(path, map);
                return map;
            } catch (IOException ignored) {
            }
        }
        return map;
    }

    private static List<Path> candidatePaths() {
        List<Path> paths = new ArrayList<>();

        String explicitPath = System.getenv("DLEM_ENV_FILE");
        if (explicitPath != null && !explicitPath.isBlank()) {
            paths.add(Paths.get(explicitPath.trim()));
        }

        explicitPath = System.getProperty("dlem.env.file");
        if (explicitPath != null && !explicitPath.isBlank()) {
            paths.add(Paths.get(explicitPath.trim()));
        }

        Path projectRoot = projectRoot();
        if (projectRoot != null) {
            paths.add(projectRoot.resolve(".env"));
        }

        Path userDir = Paths.get(System.getProperty("user.dir", ".")).toAbsolutePath().normalize();
        Path current = userDir;
        for (int i = 0; i < 10 && current != null; i++) {
            paths.add(current.resolve(".env"));
            current = current.getParent();
        }

        String catalinaBase = System.getProperty("catalina.base");
        if (catalinaBase != null && !catalinaBase.isBlank()) {
            paths.add(Paths.get(catalinaBase, ".env"));
            paths.add(Paths.get(catalinaBase, "conf", ".env"));
            paths.add(Paths.get(catalinaBase, "webapps", "Driving-Licence-Examination-Management-Monolith", "WEB-INF", ".env"));
        }

        String catalinaHome = System.getProperty("catalina.home");
        if (catalinaHome != null && !catalinaHome.isBlank()) {
            paths.add(Paths.get(catalinaHome, ".env"));
            paths.add(Paths.get(catalinaHome, "conf", ".env"));
        }

        return paths;
    }

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
