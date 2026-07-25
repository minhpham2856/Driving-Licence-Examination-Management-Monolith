package admin.util;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/** Gửi workbook về trình duyệt dưới dạng file .xlsx tải xuống. */
public final class ExcelDownload {

    public static final String XLSX_MIME =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private ExcelDownload() {}

    @FunctionalInterface
    public interface Writer {
        void write(OutputStream out) throws IOException;
    }

    public static void send(HttpServletResponse resp, String fileName, Writer writer) throws IOException {
        resp.setContentType(XLSX_MIME);
        resp.setCharacterEncoding("UTF-8");
        String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()).replace("+", "%20");
        resp.setHeader("Content-Disposition",
                "attachment; filename=\"" + fileName + "\"; filename*=UTF-8''" + encoded);
        try (OutputStream out = resp.getOutputStream()) {
            writer.write(out);
            out.flush();
        }
    }
}
