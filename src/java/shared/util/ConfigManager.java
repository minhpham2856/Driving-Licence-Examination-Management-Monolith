package shared.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Đọc cấu hình từ biến môi trường OS, rồi file .env (project root, WEB-INF/.env, catalina.base…).
 */
public final class ConfigManager {

    private static final List<Path> extraPaths = new ArrayList<>();
    private static Map<String, String> fileValues = new HashMap<>();
    private static boolean loaded = false;

    private ConfigManager() {
    }

    public static void registerEnvFile(Path path) {
        if (path != null) {
            extraPaths.add(path);
        }
    }

    public static void reload() {
        fileValues.clear();
        loaded = false;
        loadIfNeeded();
    }

    public static String get(String key) {
        return get(key, null);
    }

    public static String get(String key, String defaultValue) {
        loadIfNeeded();

        String fromOs = System.getenv(key);
        if (fromOs != null && !fromOs.trim().isEmpty()) {
            return fromOs.trim();
        }

        String fromFile = fileValues.get(key);
        if (fromFile != null && !fromFile.trim().isEmpty()) {
            return fromFile.trim();
        }

        return defaultValue;
    }

    private static void loadIfNeeded() {
        if (loaded) {
            return;
        }
        loadDotEnvFile();
        loaded = true;
    }

    private static void loadDotEnvFile() {
        for (Path path : candidatePaths()) {
            if (!Files.isRegularFile(path)) {
                continue;
            }
            try {
                parseFile(path);
                return;
            } catch (IOException ignored) {
                // try next path
            }
        }
    }

    private static List<Path> candidatePaths() {
        List<Path> paths = new ArrayList<>(extraPaths);

        String explicitPath = System.getenv("DLEM_ENV_FILE");
        if (explicitPath != null && !explicitPath.trim().isEmpty()) {
            paths.add(Paths.get(explicitPath.trim()));
        }

        explicitPath = System.getProperty("dlem.env.file");
        if (explicitPath != null && !explicitPath.trim().isEmpty()) {
            paths.add(Paths.get(explicitPath.trim()));
        }

        Path userDir = Paths.get(System.getProperty("user.dir", ".")).toAbsolutePath();
        Path current = userDir;
        for (int i = 0; i < 10 && current != null; i++) {
            paths.add(current.resolve(".env"));
            current = current.getParent();
        }

        String catalinaBase = System.getProperty("catalina.base");
        if (catalinaBase != null && !catalinaBase.trim().isEmpty()) {
            paths.add(Paths.get(catalinaBase, ".env"));
            paths.add(Paths.get(catalinaBase, "conf", ".env"));
            paths.add(Paths.get(catalinaBase, "webapps", "Driving-Licence-Examination-Management-Monolith", "WEB-INF", ".env"));
        }

        String catalinaHome = System.getProperty("catalina.home");
        if (catalinaHome != null && !catalinaHome.trim().isEmpty()) {
            paths.add(Paths.get(catalinaHome, ".env"));
            paths.add(Paths.get(catalinaHome, "conf", ".env"));
        }

        return paths;
    }

    private static void parseFile(Path path) throws IOException {
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
                fileValues.put(key, value);
            }
        }
    }
}
