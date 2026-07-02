package util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class EnvConfig {

    private static final Logger LOGGER = Logger.getLogger(EnvConfig.class.getName());
    private static final Map<String, String> FILE_VALUES = new HashMap<>();

    static {
        loadDotEnvFile();
    }

    private EnvConfig() {
    }

    public static String get(String key) {
        return get(key, null);
    }

    public static String get(String key, String defaultValue) {
        String fromOs = System.getenv(key);
        if (fromOs != null && !fromOs.isBlank()) {
            return fromOs.trim();
        }

        String fromFile = FILE_VALUES.get(key);
        if (fromFile != null && !fromFile.isBlank()) {
            return fromFile.trim();
        }

        return defaultValue;
    }

    private static void loadDotEnvFile() {
        for (Path path : candidatePaths()) {
            if (!Files.isRegularFile(path)) {
                continue;
            }

            try {
                parseFile(path);
                LOGGER.info("Loaded environment from " + path.toAbsolutePath());
                return;
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Could not read " + path, e);
            }
        }
    }

    private static Iterable<Path> candidatePaths() {
        java.util.List<Path> paths = new java.util.ArrayList<>();

        String explicitPath = System.getenv("DLEM_ENV_FILE");
        if (explicitPath != null && !explicitPath.isBlank()) {
            paths.add(Paths.get(explicitPath.trim()));
        }

        explicitPath = System.getProperty("dlem.env.file");
        if (explicitPath != null && !explicitPath.isBlank()) {
            paths.add(Paths.get(explicitPath.trim()));
        }

        Path userDir = Paths.get(System.getProperty("user.dir", ".")).toAbsolutePath();
        Path current = userDir;
        for (int i = 0; i < 6 && current != null; i++) {
            paths.add(current.resolve(".env"));
            current = current.getParent();
        }

        String catalinaBase = System.getProperty("catalina.base");
        if (catalinaBase != null && !catalinaBase.isBlank()) {
            paths.add(Paths.get(catalinaBase, ".env"));
        }

        return paths;
    }

    private static void parseFile(Path path) throws IOException {
        for (String line : Files.readAllLines(path)) {
            line = line.trim();
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
                FILE_VALUES.put(key, value);
            }
        }
    }
}
