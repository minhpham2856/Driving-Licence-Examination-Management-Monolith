package managingstaff.util;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
        if (CloudinaryDocumentReader.supports(documentUrl)) {
            CloudinaryDocumentReader.Resource resource = CloudinaryDocumentReader.read(documentUrl);
            String contentType = resource.contentType().toLowerCase();
            String suffix = contentType.contains("pdf") ? ".pdf"
                    : contentType.contains("png") ? ".png"
                    : contentType.contains("gif") ? ".gif" : ".jpg";
            Path temp = Files.createTempFile("dlem-dossier-", suffix);
            Files.write(temp, resource.bytes());
            temp.toFile().deleteOnExit();
            return temp;
        }
        if (documentUrl.startsWith("http://") || documentUrl.startsWith("https://")) {
            return download(documentUrl);
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

    private static Path download(String url) throws IOException {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();
            HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
            HttpResponse<byte[]> response = client.send(request,
                    HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IOException("Không thể tải tài liệu Cloudinary (HTTP "
                        + response.statusCode() + ").");
            }
            String contentType = response.headers().firstValue("Content-Type").orElse("").toLowerCase();
            String suffix = contentType.contains("pdf") ? ".pdf"
                    : contentType.contains("png") ? ".png"
                    : contentType.contains("gif") ? ".gif" : ".jpg";
            Path temp = Files.createTempFile("dlem-dossier-", suffix);
            Files.write(temp, response.body());
            temp.toFile().deleteOnExit();
            return temp;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IOException("Tải tài liệu Cloudinary bị gián đoạn.", ex);
        } catch (IllegalArgumentException ex) {
            throw new IOException("URL tài liệu Cloudinary không hợp lệ.", ex);
        }
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
