package Utils;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class DossierFileResolver {

    private DossierFileResolver() {
    }

    public static Path resolve(Path runtimeWebRoot, String documentUrl) throws IOException {
        if (runtimeWebRoot == null || documentUrl == null || documentUrl.isBlank()) {
            throw new IOException("Không xác định được đường dẫn tài liệu.");
        }
        String cleanUrl = documentUrl.split("\\?", 2)[0].replace('\\', '/');
        cleanUrl = URLDecoder.decode(cleanUrl, StandardCharsets.UTF_8);
        while (cleanUrl.startsWith("/")) {
            cleanUrl = cleanUrl.substring(1);
        }

        Path root = runtimeWebRoot.toAbsolutePath().normalize();
        Path runtimeFile = root.resolve(cleanUrl).normalize();
        if (!runtimeFile.startsWith(root)) {
            throw new IOException("Đường dẫn tài liệu không hợp lệ.");
        }
        if (Files.isRegularFile(runtimeFile)) {
            return runtimeFile;
        }

        Path sourceWebRoot = findSourceWebRoot(root);
        if (sourceWebRoot != null) {
            Path sourceFile = sourceWebRoot.resolve(cleanUrl).normalize();
            if (sourceFile.startsWith(sourceWebRoot) && Files.isRegularFile(sourceFile)) {
                return sourceFile;
            }
        }
        throw new IOException("Không tìm thấy tệp tài liệu trên máy chủ.");
    }

    private static Path findSourceWebRoot(Path runtimeWebRoot) {
        Path parent = runtimeWebRoot.getParent();
        if (parent != null && "build".equalsIgnoreCase(parent.getFileName().toString())) {
            Path projectRoot = parent.getParent();
            if (projectRoot != null) {
                return projectRoot.resolve("web").toAbsolutePath().normalize();
            }
        }
        return null;
    }
}
