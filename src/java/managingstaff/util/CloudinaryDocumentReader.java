package managingstaff.util;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import shared.ConfigManager;

/** Đọc tài liệu Cloudinary authenticated mà không công khai API secret ra trình duyệt. */
public final class CloudinaryDocumentReader {
    private static final String PREFIX = "cloudinary:";
    private static final int MAX_DOCUMENT_BYTES = 15 * 1024 * 1024;

    private CloudinaryDocumentReader() { }

    public static boolean supports(String storedRef) {
        return storedRef != null && storedRef.startsWith(PREFIX);
    }

    public static Resource read(String storedRef) throws IOException {
        Ref ref = parse(storedRef);
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(signedDeliveryUrl(ref))).GET().build();
            HttpResponse<byte[]> response = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.NORMAL).build()
                    .send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IOException("Cloudinary từ chối tài liệu (HTTP " + response.statusCode() + ").");
            }
            if (response.body().length > MAX_DOCUMENT_BYTES) {
                throw new IOException("Tài liệu vượt quá giới hạn cho phép.");
            }
            String contentType = response.headers().firstValue("Content-Type")
                    .orElse("raw".equalsIgnoreCase(ref.resourceType) ? "application/octet-stream" : "image/jpeg");
            return new Resource(response.body(), contentType);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IOException("Quá trình tải tài liệu bị gián đoạn.", ex);
        }
    }

    private static Ref parse(String storedRef) throws IOException {
        if (!supports(storedRef)) throw new IOException("Tham chiếu Cloudinary không hợp lệ.");
        String value = storedRef.substring(PREFIX.length());
        int separator = value.indexOf(':');
        if (separator <= 0 || separator == value.length() - 1) {
            throw new IOException("Tham chiếu Cloudinary không hợp lệ.");
        }
        return new Ref(value.substring(0, separator), value.substring(separator + 1));
    }

    private static String signedDeliveryUrl(Ref ref) throws IOException {
        String cloudName = ConfigManager.get("CLOUDINARY_CLOUD_NAME", "").trim();
        String apiSecret = ConfigManager.get("CLOUDINARY_API_SECRET", "").trim();
        String accessType = ConfigManager.get("CLOUDINARY_ACCESS_TYPE", "authenticated").trim();
        if (cloudName.isBlank() || apiSecret.isBlank()) {
            throw new IOException("Cloudinary chưa được cấu hình đầy đủ.");
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-1")
                    .digest((ref.publicId + apiSecret).getBytes(StandardCharsets.UTF_8));
            String signature = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(digest).substring(0, 8);
            return "https://res.cloudinary.com/" + cloudName + "/" + ref.resourceType
                    + "/" + accessType + "/s--" + signature + "--/" + ref.publicId;
        } catch (Exception ex) {
            throw new IOException("Không thể ký URL tài liệu Cloudinary.", ex);
        }
    }

    public record Resource(byte[] bytes, String contentType) { }
    private record Ref(String resourceType, String publicId) { }
}
