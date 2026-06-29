package Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class ConfigManager {

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
        String customPath = System.getenv("DLEM_ENV_FILE");
        InputStream custom = openFile(customPath);
        if (custom != null) {
            return custom;
        }

        String userDir = System.getProperty("user.dir");
        InputStream rootEnv = openFile(userDir == null ? null : Path.of(userDir, ".env").toString());
        if (rootEnv != null) {
            return rootEnv;
        }

        InputStream webInfEnv = openFile(userDir == null ? null : Path.of(userDir, "web", "WEB-INF", ".env").toString());
        if (webInfEnv != null) {
            return webInfEnv;
        }

        InputStream deployedWebInfEnv = openFile(resolveDeployedWebInfEnvPath());
        if (deployedWebInfEnv != null) {
            return deployedWebInfEnv;
        }

        ClassLoader cl = ConfigManager.class.getClassLoader();
        InputStream is = cl.getResourceAsStream("config/config.props");
        if (is != null) {
            return is;
        }
        is = cl.getResourceAsStream("config/.env");
        if (is != null) {
            return is;
        }
        return cl.getResourceAsStream(".env");
    }

    private static InputStream openFile(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }
        try {
            Path p = Path.of(path);
            return Files.isRegularFile(p) ? Files.newInputStream(p) : null;
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }

    private static String resolveDeployedWebInfEnvPath() {
        try {
            URI location = ConfigManager.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI();
            Path classPathRoot = Path.of(location);
            Path webInf = Files.isDirectory(classPathRoot)
                    ? classPathRoot.getParent()
                    : classPathRoot.getParent();
            if (webInf != null && "WEB-INF".equalsIgnoreCase(webInf.getFileName().toString())) {
                return webInf.resolve(".env").toString();
            }
        } catch (Exception e) {
            return null;
        }
        return null;
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
