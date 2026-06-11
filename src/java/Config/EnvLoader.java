package Config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * <b>Đọc file .env vào bộ nhớ (cache)</b> — mọi chỗ cần cấu hình SEPay đều đi qua class này.
 *
 * <p>File .env là text dạng {@code key=value} (vd. {@code sepay.merchantId=SP-LIVE-...}).
 * Không commit secret lên git; chỉ giữ .env.example làm mẫu.</p>
 *
 * <p><b>Thứ tự merge</b> (file/load sau ghi đè key trùng của file trước):</p>
 * <ol>
 *   <li>Walk-up .env từ thư mục làm việc, catalina — ưu tiên thấp</li>
 *   <li>{@code WEB-INF/others/config/sepay.env} — bản trong WAR</li>
 *   <li>{@code web.xml} param {@code dlem.env.path} — thường root project .env</li>
 *   <li>{@link System#getenv(String)} — biến OS ghi đè tất cả khi {@link #get(String)} được gọi</li>
 * </ol>
 *
 * <p>{@link EnvConfigListener} gọi {@link #reload()} khi Tomcat start.</p>
 */
public final class EnvLoader {

    private static final String CLASSPATH_ENV = "config/dlem.env";

    private static volatile Map<String, String> cached = Map.of();
    private static volatile boolean loadAttempted;
    private static volatile String loadSummary = "not loaded";
    private static final List<Path> extraPaths = new ArrayList<>();

    private EnvLoader() {
    }

    public static void addPath(Path path) {
        if (path == null) {
            return;
        }
        synchronized (extraPaths) {
            if (!extraPaths.contains(path)) {
                extraPaths.add(path);
            }
        }
    }

    public static void reload() {
        synchronized (EnvLoader.class) {
            loadAttempted = false;
            cached = Map.of();
            ensureLoaded();
        }
    }

    /**
     * Lấy giá trị biến: ưu tiên biến môi trường hệ điều hành, không có thì đọc từ file .env đã cache.
     */
    public static String get(String key) {
        String fromEnv = System.getenv(key);
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv.trim();
        }
        return getFromFile(key).orElse(null);
    }

    public static String get(String key, String defaultValue) {
        String value = get(key);
        return value != null && !value.isBlank() ? value : defaultValue;
    }

    public static String getLoadSummary() {
        ensureLoaded();
        return loadSummary;
    }

    public static boolean hasSepayConfig() {
        ensureLoaded();
        return cached.containsKey("sepay.merchantId") && cached.containsKey("sepay.secretKey");
    }

    private static Optional<String> getFromFile(String key) {
        ensureLoaded();
        return Optional.ofNullable(cached.get(key));
    }

    /**
     * Lazy-load: chỉ đọc disk lần đầu có code gọi get() — tránh đọc .env khi class được load sớm.
     * Double-checked locking để thread-safe trên Tomcat.
     */
    private static void ensureLoaded() {
        if (loadAttempted) {
            return;
        }
        synchronized (EnvLoader.class) {
            if (loadAttempted) {
                return;
            }
            loadAttempted = true;
            LoadResult result = loadAllSources();
            cached = Collections.unmodifiableMap(result.values());
            loadSummary = result.summary();
        }
    }

    private static LoadResult loadAllSources() {
        Map<String, String> merged = new HashMap<>();
        Set<String> loadedFrom = new LinkedHashSet<>();
        List<String> tried = new ArrayList<>();

        List<Path> fallbackPaths = new ArrayList<>();
        List<Path> priorityPaths = new ArrayList<>();
        partitionCandidatePaths(fallbackPaths, priorityPaths);

        for (Path path : fallbackPaths) {
            mergePathIfExists(path, merged, loadedFrom, tried);
        }
        mergeClasspath(merged, loadedFrom, tried);
        for (Path path : priorityPaths) {
            mergePathIfExists(path, merged, loadedFrom, tried);
        }

        String summary;
        if (loadedFrom.isEmpty()) {
            summary = "Không tìm thấy file .env. Đã thử: " + String.join("; ", tried)
                    + ". Đặt .env ở thư mục gốc project hoặc -Ddlem.env.path=... hoặc copy sang web/WEB-INF/others/config/sepay.env";
        } else {
            summary = "Đã nạp từ: " + String.join("; ", loadedFrom);
        }

        return new LoadResult(merged, summary);
    }

    private static void mergeClasspath(Map<String, String> merged, Set<String> loadedFrom, List<String> tried) {
        String resource = "/" + CLASSPATH_ENV;
        tried.add("classpath:" + resource);
        try (InputStream input = EnvLoader.class.getResourceAsStream(resource)) {
            if (input == null) {
                return;
            }
            mergeReader(input, merged);
            loadedFrom.add("classpath:" + resource);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void mergeFile(Path path, Map<String, String> merged) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            mergeReader(reader, merged);
        }
    }

    private static void mergeReader(BufferedReader reader, Map<String, String> merged) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            parseLine(line).ifPresent(entry -> merged.put(entry.getKey(), entry.getValue()));
        }
    }

    private static void mergeReader(InputStream input, Map<String, String> merged) throws IOException {
        try (BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(input, StandardCharsets.UTF_8))) {
            mergeReader(reader, merged);
        }
    }

    private static void partitionCandidatePaths(List<Path> fallbackPaths, List<Path> priorityPaths) {
        Set<String> seen = new LinkedHashSet<>();

        String userDir = System.getProperty("user.dir", ".");
        collectWalkUpEnvFiles(Paths.get(userDir), fallbackPaths);

        String catalinaBase = System.getProperty("catalina.base");
        if (catalinaBase != null) {
            addUniquePath(fallbackPaths, seen, Paths.get(catalinaBase, "conf", "dlem.env"));
            addUniquePath(fallbackPaths, seen, Paths.get(catalinaBase, ".env"));
        }

        synchronized (extraPaths) {
            for (Path path : extraPaths) {
                addUniquePath(priorityPaths, seen, path);
            }
        }

        String explicit = System.getProperty("dlem.env.path");
        if (explicit != null && !explicit.isBlank()) {
            addUniquePath(priorityPaths, seen, Paths.get(explicit));
        }
    }

    private static void addUniquePath(List<Path> target, Set<String> seen, Path path) {
        String key = path.toAbsolutePath().normalize().toString();
        if (seen.add(key)) {
            target.add(path);
        }
    }

    private static void mergePathIfExists(
            Path path,
            Map<String, String> merged,
            Set<String> loadedFrom,
            List<String> tried) {
        tried.add(path.toAbsolutePath().normalize().toString());
        if (!Files.isRegularFile(path)) {
            return;
        }
        try {
            mergeFile(path, merged);
            loadedFrom.add(path.toAbsolutePath().normalize().toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void collectWalkUpEnvFiles(Path start, List<Path> out) {
        Path current = start.toAbsolutePath().normalize();
        for (int depth = 0; depth < 12 && current != null; depth++) {
            Path env = current.resolve(".env");
            if (Files.isRegularFile(env) && !out.contains(env)) {
                out.add(env);
            }
            current = current.getParent();
        }
    }

    private static Optional<Map.Entry<String, String>> parseLine(String line) {
        String trimmed = line.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("#")) {
            return Optional.empty();
        }
        int eq = trimmed.indexOf('=');
        if (eq <= 0) {
            return Optional.empty();
        }
        String key = stripBom(trimmed.substring(0, eq).trim());
        String value = stripBom(trimmed.substring(eq + 1).trim());
        if ((value.startsWith("\"") && value.endsWith("\""))
                || (value.startsWith("'") && value.endsWith("'"))) {
            value = value.substring(1, value.length() - 1);
        }
        return Optional.of(Map.entry(key, value.trim()));
    }

    private static String stripBom(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        if (text.charAt(0) == '\uFEFF') {
            return text.substring(1);
        }
        return text;
    }

    private record LoadResult(Map<String, String> values, String summary) {
    }
}
