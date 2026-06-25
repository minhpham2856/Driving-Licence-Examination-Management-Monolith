package Utils;

import jakarta.servlet.http.Part;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.TreeMap;
import java.util.UUID;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * Upload hồ sơ thí sinh lên Cloudinary (authenticated) — phục vụ xem khi chờ duyệt.
 * <p>
 * Cấu trúc: {@code dlem/pending/p{profileId}/{DocumentType}/{uuid}}.
 * Trong DB: {@code cloudinary:image:dlem/pending/p42/IdFront/a1b2c3d4}.
 */
public final class CloudinaryDocumentStorage {

    private static final String REF_PREFIX = "cloudinary:";
    private static final int DEFAULT_SIGNED_URL_TTL_SECONDS = 1800;

    private CloudinaryDocumentStorage() {
    }

    public static boolean isConfigured() {
        return !blank(ConfigManager.get("CLOUDINARY_CLOUD_NAME"))
                && !blank(ConfigManager.get("CLOUDINARY_API_KEY"))
                && !blank(ConfigManager.get("CLOUDINARY_API_SECRET"));
    }

    public static boolean isCloudinaryRef(String storedRef) {
        return storedRef != null && storedRef.startsWith(REF_PREFIX);
    }

    public static CloudinaryRef parseRef(String storedRef) {
        if (!isCloudinaryRef(storedRef)) {
            return null;
        }
        String body = storedRef.substring(REF_PREFIX.length());
        int first = body.indexOf(':');
        if (first <= 0 || first >= body.length() - 1) {
            return null;
        }
        String resourceType = body.substring(0, first).trim();
        String publicId = body.substring(first + 1).trim();
        if (resourceType.isEmpty() || publicId.isEmpty()) {
            return null;
        }
        return new CloudinaryRef(resourceType, publicId);
    }

    public static String buildStoredRef(String resourceType, String publicId) {
        return REF_PREFIX + resourceType + ":" + publicId;
    }

    /**
     * Upload multipart từ {@link Part}, trả về tham chiếu lưu DB.
     */
    public static String upload(Part part, int profileId, String docType, String ext) throws IOException {
        if (!isConfigured()) {
            throw new IOException("Cloudinary chưa được cấu hình (CLOUDINARY_CLOUD_NAME, API_KEY, API_SECRET).");
        }
        if (part == null || part.getSize() <= 0) {
            throw new IOException("Tệp upload trống.");
        }

        String resourceType = isPdf(ext) ? "raw" : "image";
        String publicId = buildPublicId(profileId, docType, ext);
        byte[] bytes;
        try (InputStream in = part.getInputStream()) {
            bytes = in.readAllBytes();
        }

        String submitted = part.getSubmittedFileName();
        String fileName = submitted != null && !submitted.isBlank()
                ? RegistrantUploadStorage.sanitizeFileName(submitted)
                : docType + "." + ext;

        long timestamp = Instant.now().getEpochSecond();
        TreeMap<String, String> signParams = new TreeMap<>();
        signParams.put("context", buildRegistrantContext(profileId, docType));
        signParams.put("public_id", publicId);
        signParams.put("tags", buildRegistrantTags(profileId, docType));
        signParams.put("timestamp", String.valueOf(timestamp));
        signParams.put("type", accessType());

        String signature = signApi(signParams);

        TreeMap<String, String> formFields = new TreeMap<>(signParams);
        formFields.put("signature", signature);
        formFields.put("api_key", apiKey());

        String responseBody = multipartPost(
                apiUrl(resourceType, "upload"),
                fileName,
                probeContentType(ext),
                bytes,
                formFields);

        String returnedId = extractJsonString(responseBody, "public_id");
        if (returnedId == null || returnedId.isBlank()) {
            String error = extractJsonString(responseBody, "error");
            if (error != null && !error.isBlank()) {
                throw new IOException("Cloudinary: " + error);
            }
            throw new IOException("Cloudinary không trả về public_id.");
        }
        return buildStoredRef(resourceType, returnedId);
    }

    public static void destroy(String storedRef) throws IOException {
        CloudinaryRef ref = parseRef(storedRef);
        if (ref == null) {
            return;
        }
        if (!isConfigured()) {
            return;
        }

        long timestamp = Instant.now().getEpochSecond();
        TreeMap<String, String> signParams = new TreeMap<>();
        signParams.put("public_id", ref.publicId);
        signParams.put("timestamp", String.valueOf(timestamp));
        signParams.put("type", accessType());

        String signature = signApi(signParams);

        TreeMap<String, String> formFields = new TreeMap<>(signParams);
        formFields.put("signature", signature);
        formFields.put("api_key", apiKey());

        String responseBody = formPost(apiUrl(ref.resourceType, "destroy"), formFields);
        if (responseBody.contains("\"result\":\"ok\"") || responseBody.contains("\"result\":\"not found\"")) {
            return;
        }
        String error = extractJsonString(responseBody, "error");
        if (error != null && !error.isBlank()) {
            throw new IOException("Cloudinary destroy: " + error);
        }
    }

    /** URL có chữ ký, hết hạn sau TTL cấu hình (mặc định 30 phút). */
    public static String signedDeliveryUrl(String resourceType, String publicId) {
        if (!isConfigured() || blank(publicId)) {
            return null;
        }
        long ttl = signedUrlTtlSeconds();
        long timestamp = Instant.now().getEpochSecond() + ttl;

        TreeMap<String, String> signParams = new TreeMap<>();
        signParams.put("public_id", publicId);
        signParams.put("timestamp", String.valueOf(timestamp));
        String signature = signApi(signParams);

        return "https://res.cloudinary.com/"
                + cloudName()
                + "/" + resourceType
                + "/" + accessType()
                + "/" + publicId
                + "?api_key=" + urlEncode(apiKey())
                + "&timestamp=" + timestamp
                + "&signature=" + signature;
    }

    private static String buildPublicId(int profileId, String docType, String ext) {
        String safeType = RegistrantUploadStorage.sanitizeFileName(docType);
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String base = folderPrefix()
                + "/p" + profileId
                + "/" + safeType
                + "/" + suffix;
        if (isPdf(ext)) {
            return base + ".pdf";
        }
        return base;
    }

    /** Metadata tìm kiếm trên Cloudinary Media Library (profile, loại giấy tờ). */
    private static String buildRegistrantContext(int profileId, String docType) {
        return "profile_id=" + profileId
                + "|profile_code=HS-" + profileId
                + "|document_type=" + RegistrantUploadStorage.sanitizeFileName(docType)
                + "|source=registrant"
                + "|lifecycle=pending";
    }

    private static String buildRegistrantTags(int profileId, String docType) {
        String typeTag = RegistrantUploadStorage.sanitizeFileName(docType)
                .replace('_', '-')
                .toLowerCase();
        return "dlem,registrant,pending,p" + profileId + "," + typeTag;
    }

    private static String folderPrefix() {
        String configured = ConfigManager.get("CLOUDINARY_FOLDER_PREFIX", "dlem/pending");
        return configured != null ? configured.trim() : "dlem/pending";
    }

    private static String accessType() {
        String configured = ConfigManager.get("CLOUDINARY_ACCESS_TYPE", "authenticated");
        return configured != null && !configured.isBlank() ? configured.trim() : "authenticated";
    }

    private static long signedUrlTtlSeconds() {
        String raw = ConfigManager.get("CLOUDINARY_SIGNED_URL_TTL_SECONDS", String.valueOf(DEFAULT_SIGNED_URL_TTL_SECONDS));
        try {
            long parsed = Long.parseLong(raw.trim());
            return parsed > 60 ? parsed : DEFAULT_SIGNED_URL_TTL_SECONDS;
        } catch (NumberFormatException ex) {
            return DEFAULT_SIGNED_URL_TTL_SECONDS;
        }
    }

    private static String cloudName() {
        return ConfigManager.get("CLOUDINARY_CLOUD_NAME", "").trim();
    }

    private static String apiKey() {
        return ConfigManager.get("CLOUDINARY_API_KEY", "").trim();
    }

    private static String apiSecret() {
        return ConfigManager.get("CLOUDINARY_API_SECRET", "").trim();
    }

    private static String apiUrl(String resourceType, String action) {
        return "https://api.cloudinary.com/v1_1/" + cloudName() + "/" + resourceType + "/" + action;
    }

    private static String signApi(TreeMap<String, String> params) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (var entry : params.entrySet()) {
            if (!first) {
                sb.append('&');
            }
            sb.append(entry.getKey()).append('=').append(entry.getValue());
            first = false;
        }
        sb.append(apiSecret());
        return DigestUtils.sha1Hex(sb.toString());
    }

    private static String multipartPost(String endpoint, String fileName, String contentType,
            byte[] fileBytes, TreeMap<String, String> fields) throws IOException {
        String boundary = "----DLEM" + UUID.randomUUID();
        HttpURLConnection conn = openPost(endpoint);
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        try (OutputStream out = conn.getOutputStream();
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), true)) {

            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"")
                    .append(fileName).append("\"\r\n");
            writer.append("Content-Type: ").append(contentType).append("\r\n\r\n");
            writer.flush();
            out.write(fileBytes);
            out.flush();
            writer.append("\r\n");

            for (var entry : fields.entrySet()) {
                writer.append("--").append(boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"")
                        .append(entry.getKey()).append("\"\r\n\r\n");
                writer.append(entry.getValue()).append("\r\n");
            }
            writer.append("--").append(boundary).append("--\r\n");
            writer.flush();
        }

        return readResponse(conn);
    }

    private static String formPost(String endpoint, TreeMap<String, String> fields) throws IOException {
        HttpURLConnection conn = openPost(endpoint);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

        StringBuilder body = new StringBuilder();
        boolean first = true;
        for (var entry : fields.entrySet()) {
            if (!first) {
                body.append('&');
            }
            body.append(urlEncode(entry.getKey())).append('=').append(urlEncode(entry.getValue()));
            first = false;
        }

        byte[] bytes = body.toString().getBytes(StandardCharsets.UTF_8);
        conn.setFixedLengthStreamingMode(bytes.length);
        try (OutputStream out = conn.getOutputStream()) {
            out.write(bytes);
        }
        return readResponse(conn);
    }

    private static HttpURLConnection openPost(String endpoint) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(endpoint).openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setConnectTimeout(30_000);
        conn.setReadTimeout(120_000);
        return conn;
    }

    private static String readResponse(HttpURLConnection conn) throws IOException {
        int code = conn.getResponseCode();
        InputStream stream = code >= 400 ? conn.getErrorStream() : conn.getInputStream();
        if (stream == null) {
            throw new IOException("Cloudinary HTTP " + code + " (không có nội dung phản hồi).");
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            if (code >= 400) {
                String error = extractJsonString(sb.toString(), "message");
                if (error == null) {
                    error = sb.toString();
                }
                throw new IOException("Cloudinary HTTP " + code + ": " + error);
            }
            return sb.toString();
        } finally {
            conn.disconnect();
        }
    }

    private static String extractJsonString(String json, String key) {
        if (json == null || key == null) {
            return null;
        }
        String needle = "\"" + key + "\":";
        int idx = json.indexOf(needle);
        if (idx < 0) {
            return null;
        }
        int start = idx + needle.length();
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
            start++;
        }
        if (start >= json.length()) {
            return null;
        }
        if (json.charAt(start) == '"') {
            int end = json.indexOf('"', start + 1);
            return end > start ? json.substring(start + 1, end) : null;
        }
        if (json.startsWith("{", start)) {
            int depth = 0;
            for (int i = start; i < json.length(); i++) {
                char c = json.charAt(i);
                if (c == '{') {
                    depth++;
                } else if (c == '}') {
                    depth--;
                    if (depth == 0) {
                        return json.substring(start, i + 1);
                    }
                }
            }
        }
        return null;
    }

    private static String probeContentType(String ext) {
        if (ext == null) {
            return "application/octet-stream";
        }
        return switch (ext.toLowerCase()) {
            case "png" -> "image/png";
            case "jpg", "jpeg" -> "image/jpeg";
            case "pdf" -> "application/pdf";
            default -> "application/octet-stream";
        };
    }

    private static boolean isPdf(String ext) {
        return ext != null && "pdf".equalsIgnoreCase(ext.trim());
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private static String urlEncode(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (byte b : value.getBytes(StandardCharsets.UTF_8)) {
            char c = (char) (b & 0xFF);
            if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')
                    || c == '-' || c == '_' || c == '.' || c == '~') {
                sb.append(c);
            } else {
                sb.append('%');
                sb.append(String.format("%02X", b & 0xFF));
            }
        }
        return sb.toString();
    }

    public static final class CloudinaryRef {
        public final String resourceType;
        public final String publicId;

        public CloudinaryRef(String resourceType, String publicId) {
            this.resourceType = resourceType;
            this.publicId = publicId;
        }
    }
}
