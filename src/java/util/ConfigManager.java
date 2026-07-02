package util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigManager {

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
        List<Path> paths = candidatePaths();
        for (int i = 0; i < paths.size(); i++) {
            Path path = paths.get(i);
            if (!Files.isRegularFile(path)) {
                continue;
            }
            try {
                parseFile(path);
                return;
            } catch (IOException e) {
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
        }

        String catalinaHome = System.getProperty("catalina.home");
        if (catalinaHome != null && !catalinaHome.trim().isEmpty()) {
            paths.add(Paths.get(catalinaHome, ".env"));
            paths.add(Paths.get(catalinaHome, "conf", ".env"));
        }

        return paths;
    }

    private static void parseFile(Path path) throws IOException {
        List<String> lines = Files.readAllLines(path);
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            int eq = line.indexOf('=');
            if (eq <= 0) {
                continue;
            }

            String key = line.substring(0, eq).trim();
            String value = line.substring(eq + 1).trim();

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
