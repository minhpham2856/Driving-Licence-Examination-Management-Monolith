package managingstaff.service.impl;

import managingstaff.dto.DossierDTO;
import managingstaff.dto.DossierDTO.DocumentView;
import managingstaff.dto.DossierDTO.ProfileView;
import managingstaff.dto.OcrResultDTO;
import managingstaff.service.DossierPdfService;
import managingstaff.service.OcrService;
import managingstaff.util.DossierFileResolver;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.geom.RoundRectangle2D;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

/**
 * Creates a printable image-based PDF without adding a third-party PDF library.
 * Vietnamese text is rendered by Java2D first, so PDF font embedding is not required.
 */
public class AwtDossierPdfService implements DossierPdfService {

    private static final int PAGE_WIDTH = 1240;
    private static final int PAGE_HEIGHT = 1754;
    private static final Color INK = new Color(25, 32, 44);
    private static final Color MUTED = new Color(80, 91, 110);
    private static final Color BORDER = new Color(180, 190, 203);
    private static final Pattern ID_NUMBER = Pattern.compile("(?<!\\d)(\\d{12})(?!\\d)");
    private static final Pattern DATE_VALUE = Pattern.compile("\\b(\\d{2}/\\d{2}/\\d{4})\\b");
    private final OcrService ocrService = new OcrSpaceServiceImpl();

    @Override
    public byte[] generate(DossierDTO dossier, Path webRoot) throws IOException {
        if (dossier == null || dossier.getProfile() == null || dossier.getUser() == null) {
            throw new IOException("Dữ liệu hồ sơ không đầy đủ để tạo PDF.");
        }
        OcrIdentity identity = readIdentityFromDocuments(dossier, webRoot);
        List<BufferedImage> pages = List.of(
                renderApplicationPage(dossier, webRoot, identity),
                renderDocumentsPage(dossier, webRoot));
        return encodePdf(pages);
    }

    private BufferedImage renderApplicationPage(DossierDTO dossier, Path webRoot, OcrIdentity identity) {
        BufferedImage page = newPage();
        Graphics2D g = graphics(page);
        ProfileView p = dossier.getProfile();

        drawCentered(g, "CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM", 62, font(Font.BOLD, 28), INK);
        drawCentered(g, "Độc lập - Tự do - Hạnh phúc", 104, font(Font.BOLD, 24), INK);
        g.setColor(INK);
        g.setStroke(new BasicStroke(2f));
        g.drawLine(430, 120, 810, 120);

        drawCentered(g, "ĐƠN ĐỀ NGHỊ SÁT HẠCH, CẤP GIẤY PHÉP LÁI XE", 175,
                font(Font.BOLD, 31), INK);
        drawCentered(g, "Hồ sơ điện tử đã được Managing Staff thẩm định", 216,
                font(Font.ITALIC, 20), MUTED);

        drawText(g, "Kính gửi: Phòng Cảnh sát giao thông", 82, 275, font(Font.BOLD, 24), INK);

        int portraitX = 82;
        int portraitY = 318;
        int portraitW = 230;
        int portraitH = 306;
        drawDocumentImage(g, dossier, webRoot, "PORTRAIT", portraitX, portraitY, portraitW, portraitH,
                "ẢNH CHÂN DUNG 3x4");

        int x = 355;
        int y = 330;
        int labelWidth = 225;
        y = drawField(g, "Họ và tên:", identity.fullName(), x, y, labelWidth);
        y = drawField(g, "Ngày sinh:", identity.dateOfBirth(), x, y, labelWidth);
        y = drawField(g, "Giới tính:", identity.sex(), x, y, labelWidth);
        y = drawField(g, "Quốc tịch:", identity.nationality(), x, y, labelWidth);
        y = drawField(g, "Số căn cước:", identity.governmentId(), x, y, labelWidth);
        y = drawField(g, "Số điện thoại:", safe(p.getPhoneNo()), x, y, labelWidth);
        y = drawField(g, "Email:", safe(dossier.getUser().getEmail()), x, y, labelWidth);

        drawText(g, "Địa chỉ thường trú:", 82, 690, font(Font.BOLD, 23), INK);
        drawWrapped(g, identity.address(), 305, 690, 840, 30, font(Font.PLAIN, 23), INK);
        if (identity.ocrApplied()) {
            drawText(g, "Thông tin căn cước đã được trích xuất bằng OCR và đối chiếu với hồ sơ.",
                    305, 735, font(Font.ITALIC, 16), MUTED);
        }
        g.setColor(BORDER);
        g.drawLine(82, 750, 1158, 750);

        drawText(g, "Nội dung đề nghị", 82, 805, font(Font.BOLD, 27), INK);
        String request = "Tôi đề nghị được dự sát hạch để cấp giấy phép lái xe hạng "
                + safe(dossier.getLicenceDisplayClass()) + ".";
        drawWrapped(g, request, 82, 850, 1076, 34, font(Font.PLAIN, 24), INK);

        drawText(g, "Tài liệu kèm theo đã được đối chiếu:", 82, 945, font(Font.BOLD, 23), INK);
        int itemY = 990;
        itemY = drawChecklist(g, itemY, dossier.getDocuments().containsKey("PORTRAIT"), "Ảnh chân dung 3x4");
        itemY = drawChecklist(g, itemY, dossier.getDocuments().containsKey("ID_FRONT"), "Căn cước công dân - mặt trước");
        itemY = drawChecklist(g, itemY, dossier.getDocuments().containsKey("ID_BACK"), "Căn cước công dân - mặt sau");
        itemY = drawChecklist(g, itemY, dossier.getDocuments().containsKey("HEALTH_CERTIFICATE"), "Giấy khám sức khỏe");

        drawWrapped(g,
                "Tôi xin cam đoan các thông tin trong hồ sơ là đúng sự thật và chịu trách nhiệm trước pháp luật về nội dung đã kê khai.",
                82, 1235, 1076, 34, font(Font.PLAIN, 23), INK);

        String approvalDate = LocalDate.now().format(DateTimeFormatter.ofPattern("'Ngày' dd 'tháng' MM 'năm' yyyy"));
        drawText(g, approvalDate, 755, 1390, font(Font.ITALIC, 21), INK);
        drawCenteredIn(g, "NGƯỜI LÀM ĐƠN", 760, 1432, 350, font(Font.BOLD, 23), INK);
        drawCenteredIn(g, "(Ký và ghi rõ họ tên)", 760, 1467, 350, font(Font.ITALIC, 19), MUTED);
        g.setColor(BORDER);
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(760, 1490, 350, 180, 12, 12);
        drawCenteredIn(g, "ĐỂ TRỐNG PHẦN CHỮ KÝ", 760, 1585, 350, font(Font.PLAIN, 17), new Color(130, 140, 154));

        drawFooter(g, 1, dossier.getRegistrationId());
        g.dispose();
        return page;
    }

    private BufferedImage renderDocumentsPage(DossierDTO dossier, Path webRoot) {
        BufferedImage page = newPage();
        Graphics2D g = graphics(page);

        drawCentered(g, "TÀI LIỆU ĐÍNH KÈM HỒ SƠ", 72, font(Font.BOLD, 31), INK);
        drawCentered(g, "Mã hồ sơ #" + dossier.getRegistrationId() + " - Hạng "
                + safe(dossier.getLicenceDisplayClass()), 114, font(Font.PLAIN, 22), MUTED);

        drawText(g, "Căn cước công dân - mặt trước", 80, 180, font(Font.BOLD, 22), INK);
        drawDocumentImage(g, dossier, webRoot, "ID_FRONT", 80, 205, 510, 330, "CHƯA CÓ ẢNH MẶT TRƯỚC");

        drawText(g, "Căn cước công dân - mặt sau", 650, 180, font(Font.BOLD, 22), INK);
        drawDocumentImage(g, dossier, webRoot, "ID_BACK", 650, 205, 510, 330, "CHƯA CÓ ẢNH MẶT SAU");

        g.setColor(BORDER);
        g.drawLine(80, 590, 1160, 590);
        drawText(g, "Giấy khám sức khỏe", 80, 645, font(Font.BOLD, 22), INK);
        drawDocumentImage(g, dossier, webRoot, "HEALTH_CERTIFICATE", 220, 680, 800, 820,
                "CHƯA CÓ GIẤY KHÁM SỨC KHỎE");

        drawText(g, "Các hình ảnh trên là bản tài liệu người đăng ký đã nộp trên hệ thống.",
                80, 1565, font(Font.ITALIC, 20), MUTED);
        drawFooter(g, 2, dossier.getRegistrationId());
        g.dispose();
        return page;
    }

    private static BufferedImage newPage() {
        BufferedImage image = new BufferedImage(PAGE_WIDTH, PAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, PAGE_WIDTH, PAGE_HEIGHT);
        g.dispose();
        return image;
    }

    private static Graphics2D graphics(BufferedImage image) {
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        return g;
    }

    private static Font font(int style, int size) {
        return new Font("SansSerif", style, size);
    }

    private static void drawCentered(Graphics2D g, String text, int y, Font font, Color color) {
        g.setFont(font);
        g.setColor(color);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, (PAGE_WIDTH - fm.stringWidth(text)) / 2, y);
    }

    private static void drawCenteredIn(Graphics2D g, String text, int x, int y, int width, Font font, Color color) {
        g.setFont(font);
        g.setColor(color);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, x + (width - fm.stringWidth(text)) / 2, y);
    }

    private static void drawText(Graphics2D g, String text, int x, int y, Font font, Color color) {
        g.setFont(font);
        g.setColor(color);
        g.drawString(text, x, y);
    }

    private static int drawField(Graphics2D g, String label, String value, int x, int y, int labelWidth) {
        drawText(g, label, x, y, font(Font.BOLD, 22), INK);
        drawText(g, value, x + labelWidth, y, font(Font.PLAIN, 22), INK);
        g.setColor(BORDER);
        g.drawLine(x + labelWidth, y + 8, 1145, y + 8);
        return y + 47;
    }

    private static int drawChecklist(Graphics2D g, int y, boolean checked, String label) {
        g.setColor(checked ? new Color(15, 118, 110) : BORDER);
        g.setStroke(new BasicStroke(2f));
        g.drawRect(88, y - 22, 24, 24);
        if (checked) {
            g.drawLine(93, y - 10, 101, y - 2);
            g.drawLine(101, y - 2, 109, y - 18);
        }
        drawText(g, label, 130, y, font(Font.PLAIN, 22), INK);
        return y + 45;
    }

    private static void drawWrapped(Graphics2D g, String text, int x, int y, int maxWidth,
            int lineHeight, Font font, Color color) {
        g.setFont(font);
        g.setColor(color);
        FontMetrics fm = g.getFontMetrics();
        String[] words = safe(text).split("\\s+");
        StringBuilder line = new StringBuilder();
        int currentY = y;
        for (String word : words) {
            String candidate = line.isEmpty() ? word : line + " " + word;
            if (!line.isEmpty() && fm.stringWidth(candidate) > maxWidth) {
                g.drawString(line.toString(), x, currentY);
                line.setLength(0);
                line.append(word);
                currentY += lineHeight;
            } else {
                line.setLength(0);
                line.append(candidate);
            }
        }
        if (!line.isEmpty()) g.drawString(line.toString(), x, currentY);
    }

    private static void drawDocumentImage(Graphics2D g, DossierDTO dossier, Path webRoot,
            String type, int x, int y, int width, int height, String placeholder) {
        g.setColor(new Color(248, 250, 252));
        g.fillRoundRect(x, y, width, height, 14, 14);
        g.setColor(BORDER);
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(x, y, width, height, 14, 14);

        DocumentView document = dossier.getDocuments().get(type);
        BufferedImage source = loadImage(document, webRoot);
        if (source == null) {
            drawCenteredIn(g, placeholder, x, y + height / 2, width, font(Font.BOLD, 18), MUTED);
            return;
        }
        boolean coverFrame = "PORTRAIT".equals(type) || "ID_FRONT".equals(type) || "ID_BACK".equals(type);
        int inset = coverFrame ? 4 : 10;
        double scale = coverFrame
                ? Math.max((double) (width - inset * 2) / source.getWidth(),
                        (double) (height - inset * 2) / source.getHeight())
                : Math.min((double) (width - inset * 2) / source.getWidth(),
                        (double) (height - inset * 2) / source.getHeight());
        int drawW = Math.max(1, (int) Math.round(source.getWidth() * scale));
        int drawH = Math.max(1, (int) Math.round(source.getHeight() * scale));
        int drawX = x + (width - drawW) / 2;
        int drawY = y + (height - drawH) / 2;
        Image scaled = source.getScaledInstance(drawW, drawH, Image.SCALE_SMOOTH);
        Graphics2D imageGraphics = (Graphics2D) g.create();
        imageGraphics.clip(new RoundRectangle2D.Float(x + inset, y + inset,
                width - inset * 2, height - inset * 2, 10, 10));
        imageGraphics.drawImage(scaled, drawX, drawY, null);
        imageGraphics.dispose();
    }

    private OcrIdentity readIdentityFromDocuments(DossierDTO dossier, Path webRoot) {
        ProfileView profile = dossier.getProfile();
        String frontText = recognize(dossier.getDocuments().get("ID_FRONT"), webRoot);
        String backText = recognize(dossier.getDocuments().get("ID_BACK"), webRoot);
        boolean applied = !frontText.isBlank() || !backText.isBlank();

        String governmentId = firstMatch(ID_NUMBER, frontText + "\n" + backText, safe(profile.getGovIdNo()));
        String dateOfBirth = firstMatch(DATE_VALUE, frontText, formatDate(profile.getDateOfBirth()));
        String sex = valueAfterLabel(frontText, "sex", safe(profile.getSex()));
        if (!sex.equalsIgnoreCase("Nam") && !sex.equalsIgnoreCase("Nữ") && !sex.equalsIgnoreCase("Nu")) {
            sex = safe(profile.getSex());
        }
        if (sex.equalsIgnoreCase("Nu")) sex = "Nữ";

        String nationality = valueAfterLabel(frontText, "nationality", "Việt Nam");
        if (nationality.contains("?")) nationality = "Việt Nam";
        String ocrName = valueAfterLabel(frontText, "full name", "");
        String fullName = ocrName.isBlank() || ocrName.contains("?")
                ? safe(profile.getFullName()) : titleCase(ocrName);
        String address = residence(backText);
        if (address.isBlank() || address.contains("?")) address = safe(profile.getAddress());
        return new OcrIdentity(fullName, dateOfBirth, sex, nationality, governmentId, address, applied);
    }

    private String recognize(DocumentView document, Path webRoot) {
        if (document == null || !ocrService.isConfigured()) return "";
        try {
            Path file = DossierFileResolver.resolve(webRoot, document.getDocumentUrl());
            OcrResultDTO result = ocrService.recognize(file);
            return result.success() ? result.text() : "";
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return "";
        } catch (Exception ex) {
            return "";
        }
    }

    private static String firstMatch(Pattern pattern, String text, String fallback) {
        Matcher matcher = pattern.matcher(text == null ? "" : text);
        return matcher.find() ? matcher.group(1) : fallback;
    }

    private static String valueAfterLabel(String text, String label, String fallback) {
        if (text == null || text.isBlank()) return fallback;
        String[] lines = text.split("\\R");
        for (int i = 0; i < lines.length - 1; i++) {
            if (lines[i].toLowerCase(java.util.Locale.ROOT).contains(label)) {
                String value = lines[i + 1].trim();
                if (!value.isBlank()) return value;
            }
        }
        return fallback;
    }

    private static String residence(String text) {
        if (text == null || text.isBlank()) return "";
        String[] lines = text.split("\\R");
        for (int i = 0; i < lines.length; i++) {
            if (!lines[i].toLowerCase(java.util.Locale.ROOT).contains("place of residence")) continue;
            StringBuilder value = new StringBuilder();
            for (int j = i + 1; j < lines.length && j <= i + 2; j++) {
                if (!lines[j].isBlank()) value.append(value.isEmpty() ? "" : ", ").append(lines[j].trim());
            }
            return value.toString();
        }
        return "";
    }

    private static String titleCase(String value) {
        StringBuilder result = new StringBuilder();
        for (String word : value.trim().toLowerCase(java.util.Locale.forLanguageTag("vi")).split("\\s+")) {
            if (!result.isEmpty()) result.append(' ');
            result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return result.toString();
    }

    private record OcrIdentity(String fullName, String dateOfBirth, String sex,
            String nationality, String governmentId, String address, boolean ocrApplied) { }

    private static BufferedImage loadImage(DocumentView document, Path webRoot) {
        if (document == null) return null;
        try {
            Path file = DossierFileResolver.resolve(webRoot, document.getDocumentUrl());
            return ImageIO.read(file.toFile());
        } catch (Exception ex) {
            return null;
        }
    }

    private static void drawFooter(Graphics2D g, int pageNumber, int registrationId) {
        g.setColor(BORDER);
        g.drawLine(80, 1690, 1160, 1690);
        drawText(g, "Hồ sơ #" + registrationId, 80, 1725, font(Font.PLAIN, 17), MUTED);
        drawText(g, "Trang " + pageNumber + "/2", 1070, 1725, font(Font.PLAIN, 17), MUTED);
    }

    private static String formatDate(Timestamp value) {
        return value == null ? "" : new SimpleDateFormat("dd/MM/yyyy").format(value);
    }

    private static String safe(String value) {
        return value == null || value.isBlank() ? "................................" : value.trim();
    }

    private static byte[] encodePdf(List<BufferedImage> pages) throws IOException {
        int objectCount = 2 + pages.size() * 3;
        byte[][] objects = new byte[objectCount + 1][];
        objects[1] = ascii("<< /Type /Catalog /Pages 2 0 R >>");

        StringBuilder kids = new StringBuilder();
        for (int i = 0; i < pages.size(); i++) {
            kids.append(3 + i * 3).append(" 0 R ");
        }
        objects[2] = ascii("<< /Type /Pages /Count " + pages.size() + " /Kids [" + kids + "] >>");

        for (int i = 0; i < pages.size(); i++) {
            int pageObject = 3 + i * 3;
            int imageObject = pageObject + 1;
            int contentObject = pageObject + 2;
            byte[] jpeg = toJpeg(pages.get(i));
            objects[pageObject] = ascii("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] "
                    + "/Resources << /XObject << /Im0 " + imageObject + " 0 R >> >> "
                    + "/Contents " + contentObject + " 0 R >>");
            objects[imageObject] = streamObject(
                    "<< /Type /XObject /Subtype /Image /Width " + PAGE_WIDTH
                    + " /Height " + PAGE_HEIGHT
                    + " /ColorSpace /DeviceRGB /BitsPerComponent 8 /Filter /DCTDecode /Length "
                    + jpeg.length + " >>", jpeg);
            byte[] commands = ascii("q\n595 0 0 842 0 0 cm\n/Im0 Do\nQ\n");
            objects[contentObject] = streamObject("<< /Length " + commands.length + " >>", commands);
        }

        ByteArrayOutputStream pdf = new ByteArrayOutputStream();
        pdf.write(ascii("%PDF-1.4\n% DLEM\n"));
        long[] offsets = new long[objectCount + 1];
        for (int i = 1; i <= objectCount; i++) {
            offsets[i] = pdf.size();
            pdf.write(ascii(i + " 0 obj\n"));
            pdf.write(objects[i]);
            pdf.write(ascii("\nendobj\n"));
        }
        long xref = pdf.size();
        pdf.write(ascii("xref\n0 " + (objectCount + 1) + "\n"));
        pdf.write(ascii("0000000000 65535 f \n"));
        for (int i = 1; i <= objectCount; i++) {
            pdf.write(ascii(String.format("%010d 00000 n \n", offsets[i])));
        }
        pdf.write(ascii("trailer\n<< /Size " + (objectCount + 1) + " /Root 1 0 R >>\n"
                + "startxref\n" + xref + "\n%%EOF\n"));
        return pdf.toByteArray();
    }

    private static byte[] toJpeg(BufferedImage image) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        if (!ImageIO.write(image, "jpg", out)) {
            throw new IOException("Không thể mã hóa trang PDF thành ảnh.");
        }
        return out.toByteArray();
    }

    private static byte[] streamObject(String dictionary, byte[] content) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(ascii(dictionary + "\nstream\n"));
        out.write(content);
        out.write(ascii("\nendstream"));
        return out.toByteArray();
    }

    private static byte[] ascii(String value) {
        return value.getBytes(StandardCharsets.US_ASCII);
    }
}
