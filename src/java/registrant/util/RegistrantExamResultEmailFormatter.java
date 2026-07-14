package registrant.util;

import registrant.dto.RegistrantExamResultEmailData;
import java.text.SimpleDateFormat;
import java.util.Locale;

/** Mẫu email bảng điểm sát hạch — plain text và HTML. */
public final class RegistrantExamResultEmailFormatter {

    public record FormattedEmail(String subject, String textBody, String htmlBody) {
    }

    private RegistrantExamResultEmailFormatter() {
    }

    public static FormattedEmail format(RegistrantExamResultEmailData data) {
        if (data == null) {
            return null;
        }
        String licence = safe(data.getLicenceClass(), "—");
        String examTitle = safe(data.getExamTitle(), "Kỳ thi sát hạch");
        String subject = String.format("[Lái Vui] Bảng điểm — Hạng %s — %s", licence, examTitle);

        String textBody = buildTextBody(data);
        String htmlBody = buildHtmlBody(data);
        return new FormattedEmail(subject, textBody, htmlBody);
    }

    private static String buildTextBody(RegistrantExamResultEmailData data) {
        String name = safe(data.getRecipientName(), "bạn");
        StringBuilder sb = new StringBuilder();
        sb.append("Xin chào ").append(name).append(",\n\n");
        sb.append("Ban sát hạch Lái Vui gửi bảng điểm cập nhật của bạn:\n\n");
        sb.append("——— THÔNG TIN KỲ THI ———\n");
        appendLine(sb, "Kỳ thi", data.getExamTitle());
        appendLine(sb, "Hạng GPLX", data.getLicenceClass() != null ? "Hạng " + data.getLicenceClass() : null);
        appendLine(sb, "Ngày thi", formatDate(data.getExamDate()));
        appendLine(sb, "Số báo danh", data.getSbdDisplay());
        appendLine(sb, "Phần thi", data.getExamSectionName());
        sb.append("\n——— BẢNG ĐIỂM ———\n");
        appendScoreLine(sb, "Lý thuyết", data.getTheoryScore(), data.getTheoryResultLabel());
        appendScoreLine(sb, "Thực hành / Sa hình", data.getPracticalScore(), data.getPracticalResultLabel());
        appendScoreLine(sb, "Đường trường", data.getRoadScore(), data.getRoadScore() != null
                ? String.valueOf(data.getRoadScore()) : null);
        appendLine(sb, "Kết quả chung", data.getOverallResultLabel());
        sb.append("\nChi tiết đầy đủ: đăng nhập Lái Vui → Lịch thi & kết quả.\n\n");
        sb.append("Trân trọng,\nHệ thống Lái Vui\n");
        return sb.toString();
    }

    private static String buildHtmlBody(RegistrantExamResultEmailData data) {
        String name = escapeHtml(safe(data.getRecipientName(), "bạn"));
        return """
                <!DOCTYPE html>
                <html lang="vi">
                <head><meta charset="UTF-8"></head>
                <body style="font-family:Segoe UI,Arial,sans-serif;background:#f8fafc;color:#0f172a;margin:0;padding:24px;">
                  <div style="max-width:560px;margin:0 auto;background:#fff;border:1px solid #e2e8f0;border-radius:12px;overflow:hidden;">
                    <div style="background:#0052cc;color:#fff;padding:16px 20px;">
                      <div style="font-size:18px;font-weight:700;">Lái Vui — Bảng điểm sát hạch</div>
                      <div style="font-size:13px;opacity:.92;margin-top:4px;">Thông báo qua Gmail</div>
                    </div>
                    <div style="padding:20px;">
                      <p style="margin:0 0 12px;font-size:14px;line-height:1.6;">Xin chào <strong>%s</strong>,</p>
                      <p style="margin:0 0 16px;font-size:14px;line-height:1.6;color:#475569;">
                        Ban sát hạch gửi bảng điểm cập nhật của bạn:
                      </p>
                      <table style="width:100%%;border-collapse:collapse;font-size:13px;margin-bottom:16px;">
                        %s
                      </table>
                      <p style="margin:0;font-size:12px;color:#64748b;line-height:1.5;">
                        Xem chi tiết trên cổng thí sinh: <strong>Lịch thi &amp; kết quả</strong>.
                      </p>
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(name, buildHtmlRows(data));
    }

    private static String buildHtmlRows(RegistrantExamResultEmailData data) {
        StringBuilder rows = new StringBuilder();
        rows.append(htmlRow("Kỳ thi", data.getExamTitle()));
        rows.append(htmlRow("Hạng GPLX", data.getLicenceClass() != null ? "Hạng " + data.getLicenceClass() : "—"));
        rows.append(htmlRow("Ngày thi", formatDate(data.getExamDate())));
        rows.append(htmlRow("Số báo danh", data.getSbdDisplay()));
        rows.append(htmlRow("Phần thi", data.getExamSectionName()));
        rows.append(htmlSectionHeader("Bảng điểm"));
        rows.append(htmlScoreRow("Lý thuyết", formatScoreCell(data.getTheoryScore(), data.getTheoryResultLabel())));
        rows.append(htmlScoreRow("Thực hành / Sa hình",
                formatScoreCell(data.getPracticalScore(), data.getPracticalResultLabel())));
        if (data.getRoadScore() != null) {
            rows.append(htmlScoreRow("Đường trường", String.valueOf(data.getRoadScore())));
        }
        rows.append(htmlRow("Kết quả chung", data.getOverallResultLabel()));
        return rows.toString();
    }

    private static String htmlSectionHeader(String title) {
        return """
                <tr>
                  <td colspan="2" style="padding:10px 12px 6px;font-size:11px;font-weight:700;letter-spacing:.04em;
                      text-transform:uppercase;color:#64748b;background:#f8fafc;border-top:1px solid #e2e8f0;">
                    %s
                  </td>
                </tr>
                """.formatted(escapeHtml(title));
    }

    private static String htmlRow(String label, String value) {
        return """
                <tr>
                  <td style="padding:8px 12px;border-top:1px solid #e2e8f0;color:#64748b;width:38%%;">%s</td>
                  <td style="padding:8px 12px;border-top:1px solid #e2e8f0;font-weight:600;">%s</td>
                </tr>
                """.formatted(escapeHtml(safe(label, "—")), escapeHtml(safe(value, "—")));
    }

    private static String htmlScoreRow(String label, String value) {
        return htmlRow(label, value);
    }

    private static String formatScoreCell(Integer score, String label) {
        if (score == null && (label == null || label.isBlank())) {
            return "Chưa có";
        }
        if (label != null && !label.isBlank()) {
            return label;
        }
        return score != null ? String.valueOf(score) : "—";
    }

    private static void appendLine(StringBuilder sb, String label, String value) {
        sb.append(label).append(": ").append(safe(value, "—")).append('\n');
    }

    private static void appendScoreLine(StringBuilder sb, String label, Integer score, String resultLabel) {
        if (score == null && (resultLabel == null || resultLabel.isBlank())) {
            appendLine(sb, label, "Chưa có");
            return;
        }
        String value = resultLabel != null && !resultLabel.isBlank() ? resultLabel
                : (score != null ? String.valueOf(score) : "—");
        appendLine(sb, label, value);
    }

    private static String formatDate(java.util.Date date) {
        if (date == null) {
            return "—";
        }
        return new SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("vi-VN")).format(date);
    }

    private static String safe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private static String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
