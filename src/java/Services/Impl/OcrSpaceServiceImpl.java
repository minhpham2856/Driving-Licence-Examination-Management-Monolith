package Services.Impl;

import DTOs.OcrResultDTO;
import Services.OcrService;
import Utils.ConfigManager;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Locale;
import java.util.UUID;

public class OcrSpaceServiceImpl implements OcrService {

    private static final long FREE_PLAN_FILE_LIMIT = 1024L * 1024L;
    private static final String DEFAULT_URL = "https://api.ocr.space/parse/image";
    private static final java.util.Set<String> SUPPORTED_EXTENSIONS = java.util.Set.of(
            "png", "jpg", "jpeg", "gif", "tif", "tiff", "bmp", "pdf");

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    @Override
    public boolean isConfigured() {
        String key = ConfigManager.get("OCR_SPACE_API_KEY");
        return key != null && !key.isBlank();
    }

    @Override
    public OcrResultDTO recognize(Path file) throws IOException, InterruptedException {
        if (!isConfigured()) {
            return OcrResultDTO.failure("Chưa cấu hình OCR_SPACE_API_KEY trong file .env.");
        }
        if (file == null || !Files.isRegularFile(file)) {
            return OcrResultDTO.failure("Không tìm thấy tệp cần OCR.");
        }
        long size = Files.size(file);
        if (size <= 0) {
            return OcrResultDTO.failure("Tệp OCR đang rỗng.");
        }
        if (size > FREE_PLAN_FILE_LIMIT) {
            return OcrResultDTO.failure("OCR.space Free chỉ nhận tệp tối đa 1 MB. Hãy giảm dung lượng ảnh trước khi đọc OCR.");
        }
        String extension = extension(file);
        if (!SUPPORTED_EXTENSIONS.contains(extension)) {
            return OcrResultDTO.failure(
                    "OCR.space không hỗ trợ định dạng ." + extension
                    + ". Hãy dùng JPG, PNG hoặc PDF.");
        }

        String boundary = "----DlemOcr" + UUID.randomUUID().toString().replace("-", "");
        byte[] body = buildMultipartBody(boundary, file);
        String endpoint = ConfigManager.get("OCR_SPACE_API_URL", DEFAULT_URL);
        String apiKey = ConfigManager.get("OCR_SPACE_API_KEY");
        HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint))
                .timeout(Duration.ofSeconds(60))
                .header("apikey", apiKey.trim())
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();

        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            return OcrResultDTO.failure("OCR.space trả về HTTP " + response.statusCode() + ".");
        }

        String json = response.body();
        String parsedText = extractJsonString(json, "ParsedText");
        if (parsedText != null && !parsedText.isBlank()) {
            return OcrResultDTO.success(parsedText);
        }
        String error = extractJsonString(json, "ErrorMessage");
        if (error == null || error.isBlank()) {
            error = extractFirstArrayString(json, "ErrorMessage");
        }
        return OcrResultDTO.failure(error == null || error.isBlank()
                ? "OCR.space không nhận dạng được nội dung trong ảnh." : error);
    }

    private static byte[] buildMultipartBody(String boundary, Path file) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        addField(out, boundary, "language", ConfigManager.get("OCR_SPACE_LANGUAGE", "vnm"));
        addField(out, boundary, "isOverlayRequired", "false");
        addField(out, boundary, "detectOrientation", "true");
        addField(out, boundary, "scale", "true");
        addField(out, boundary, "isTable", "false");
        addField(out, boundary, "OCREngine", ConfigManager.get("OCR_SPACE_ENGINE", "2"));

        String contentType = Files.probeContentType(file);
        if (contentType == null || contentType.isBlank()) {
            contentType = contentTypeFromName(file.getFileName().toString());
        }
        writeAscii(out, "--" + boundary + "\r\n");
        writeAscii(out, "Content-Disposition: form-data; name=\"file\"; filename=\""
                + safeFileName(file.getFileName().toString()) + "\"\r\n");
        writeAscii(out, "Content-Type: " + contentType + "\r\n\r\n");
        out.write(Files.readAllBytes(file));
        writeAscii(out, "\r\n--" + boundary + "--\r\n");
        return out.toByteArray();
    }

    private static void addField(ByteArrayOutputStream out, String boundary, String name, String value)
            throws IOException {
        writeAscii(out, "--" + boundary + "\r\n");
        writeAscii(out, "Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n");
        out.write((value == null ? "" : value).getBytes(StandardCharsets.UTF_8));
        writeAscii(out, "\r\n");
    }

    private static void writeAscii(ByteArrayOutputStream out, String value) throws IOException {
        out.write(value.getBytes(StandardCharsets.US_ASCII));
    }

    private static String safeFileName(String name) {
        return name == null ? "document" : name.replaceAll("[^A-Za-z0-9._-]", "_");
    }

    private static String contentTypeFromName(String name) {
        String lower = name == null ? "" : name.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".pdf")) return "application/pdf";
        return "image/jpeg";
    }

    private static String extension(Path file) {
        String name = file == null || file.getFileName() == null
                ? "" : file.getFileName().toString();
        int dot = name.lastIndexOf('.');
        return dot < 0 || dot == name.length() - 1
                ? "không xác định" : name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private static String extractJsonString(String json, String field) {
        if (json == null) return null;
        int key = json.indexOf('"' + field + '"');
        if (key < 0) return null;
        int colon = json.indexOf(':', key + field.length() + 2);
        if (colon < 0) return null;
        int start = skipWhitespace(json, colon + 1);
        if (start >= json.length() || json.charAt(start) != '"') return null;
        return readJsonString(json, start);
    }

    private static String extractFirstArrayString(String json, String field) {
        if (json == null) return null;
        int key = json.indexOf('"' + field + '"');
        if (key < 0) return null;
        int bracket = json.indexOf('[', key + field.length() + 2);
        if (bracket < 0) return null;
        int start = skipWhitespace(json, bracket + 1);
        return start < json.length() && json.charAt(start) == '"' ? readJsonString(json, start) : null;
    }

    private static int skipWhitespace(String value, int index) {
        while (index < value.length() && Character.isWhitespace(value.charAt(index))) index++;
        return index;
    }

    private static String readJsonString(String json, int openingQuote) {
        StringBuilder result = new StringBuilder();
        for (int i = openingQuote + 1; i < json.length(); i++) {
            char ch = json.charAt(i);
            if (ch == '"') return result.toString();
            if (ch != '\\') {
                result.append(ch);
                continue;
            }
            if (++i >= json.length()) break;
            char escaped = json.charAt(i);
            switch (escaped) {
                case '"', '\\', '/' -> result.append(escaped);
                case 'b' -> result.append('\b');
                case 'f' -> result.append('\f');
                case 'n' -> result.append('\n');
                case 'r' -> result.append('\r');
                case 't' -> result.append('\t');
                case 'u' -> {
                    if (i + 4 < json.length()) {
                        try {
                            result.append((char) Integer.parseInt(json.substring(i + 1, i + 5), 16));
                            i += 4;
                        } catch (NumberFormatException ex) {
                            result.append("\\u");
                        }
                    }
                }
                default -> result.append(escaped);
            }
        }
        return result.toString();
    }
}
