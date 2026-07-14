package Controllers.Auth.Public;

import DAO.Impl.PersonDAOImpl;
import Models.CandidateDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@MultipartConfig(maxFileSize = 15 * 1024 * 1024, maxRequestSize = 16 * 1024 * 1024)
@WebServlet(name = "ConfirmImportServlet", urlPatterns = {"/examiner/upload", "/examiner/confirm-import"})
public class ConfirmImportServlet extends HttpServlet {

    private static final DateTimeFormatter OUTPUT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Set<String> VALID_LICENSE_CLASSES = Set.of("A1", "A2", "B1", "B2", "C");

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        if (request.getServletPath().endsWith("/confirm-import")) {
            confirmImport(request, response);
            return;
        }

        previewUpload(request, response);
    }

    private void previewUpload(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        HttpSession session = request.getSession();
        session.removeAttribute("importedCandidates");
        session.removeAttribute("successMessage");
        session.removeAttribute("errorMessage");

        Part filePart = request.getPart("file");
        String fileName = getSubmittedFileName(filePart);

        if (filePart == null || filePart.getSize() == 0 || fileName.isBlank()) {
            setErrorAndRedirect(request, response, "Vui lòng chọn file Excel/CSV để tải lên.");
            return;
        }

        try (InputStream input = filePart.getInputStream()) {
            List<CandidateDTO> candidates;
            String lowerName = fileName.toLowerCase(Locale.ROOT);
            if (lowerName.endsWith(".csv")) {
                candidates = parseCsv(input);
            } else if (lowerName.endsWith(".xlsx")) {
                candidates = parseXlsx(input);
            } else if (lowerName.endsWith(".xls")) {
                throw new IllegalArgumentException("File .xls chưa được hỗ trợ. Vui lòng lưu file thành .xlsx hoặc .csv rồi tải lại.");
            } else {
                throw new IllegalArgumentException("Chỉ hỗ trợ file .xlsx hoặc .csv.");
            }

            if (candidates.isEmpty()) {
                throw new IllegalArgumentException("File không có dữ liệu thí sinh.");
            }

            session.setAttribute("importedCandidates", candidates);
            session.setAttribute("successMessage", "Đã đọc " + candidates.size() + " thí sinh hợp lệ theo cột A-F. Vui lòng kiểm tra bảng xem trước rồi xác nhận import.");
        } catch (Exception ex) {
            session.setAttribute("errorMessage", ex.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/manager/exam-schedules/create");
    }

    private void confirmImport(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession();
        List<CandidateDTO> listCandidates = (List<CandidateDTO>) session.getAttribute("importedCandidates");

        if (listCandidates == null || listCandidates.isEmpty()) {
            setErrorAndRedirect(request, response, "Không tìm thấy danh sách thí sinh chờ xác nhận.");
            return;
        }

        PersonDAOImpl personDao = new PersonDAOImpl();
        boolean isSuccess = personDao.insertCandidateList(listCandidates);

        if (isSuccess) {
            session.removeAttribute("importedCandidates");
            session.setAttribute("successMessage", "Đã import thành công " + listCandidates.size() + " thí sinh vào hệ thống.");
        } else {
            session.setAttribute("errorMessage", "Import thất bại. Vui lòng kiểm tra kết nối database và cấu trúc bảng SQL.");
        }

        response.sendRedirect(request.getContextPath() + "/manager/exam-schedules/create");
    }

    private List<CandidateDTO> parseCsv(InputStream input) throws IOException {
        List<List<String>> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank()) {
                    rows.add(parseCsvLine(removeBom(line)));
                }
            }
        }
        return toCandidates(rows);
    }

    private List<CandidateDTO> parseXlsx(InputStream input) throws Exception {
        Map<String, byte[]> entries = readZipEntries(input);
        List<String> sharedStrings = readSharedStrings(entries.get("xl/sharedStrings.xml"));
        byte[] sheetXml = entries.get("xl/worksheets/sheet1.xml");
        if (sheetXml == null) {
            throw new IllegalArgumentException("Không tìm thấy sheet đầu tiên trong file .xlsx.");
        }

        Document document = parseXml(sheetXml);
        NodeList rowNodes = document.getElementsByTagName("row");
        List<List<String>> rows = new ArrayList<>();

        for (int i = 0; i < rowNodes.getLength(); i++) {
            Element rowElement = (Element) rowNodes.item(i);
            List<String> row = new ArrayList<>();
            for (int c = 0; c < 6; c++) {
                row.add("");
            }

            NodeList cellNodes = rowElement.getElementsByTagName("c");
            for (int j = 0; j < cellNodes.getLength(); j++) {
                Element cell = (Element) cellNodes.item(j);
                int columnIndex = columnIndex(cell.getAttribute("r"));
                if (columnIndex >= 0 && columnIndex < 6) {
                    row.set(columnIndex, readCellValue(cell, sharedStrings, columnIndex));
                }
            }

            if (row.stream().anyMatch(value -> !value.isBlank())) {
                rows.add(row);
            }
        }

        return toCandidates(rows);
    }

    private List<CandidateDTO> toCandidates(List<List<String>> rows) {
        if (rows.isEmpty()) {
            return List.of();
        }

        List<CandidateDTO> candidates = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int startRow = looksLikeHeader(rows.get(0)) ? 1 : 0;

        for (int i = startRow; i < rows.size(); i++) {
            List<String> row = rows.get(i);
            while (row.size() < 6) {
                row.add("");
            }

            String sbd = normalizeSbd(row.get(0));                 // Cột A
            String name = clean(row.get(1));                       // Cột B
            String dob = normalizeDate(row.get(2));                // Cột C
            String cccd = normalizeDigits(row.get(3), 12);         // Cột D
            String phone = normalizePhone(row.get(4));             // Cột E
            String licenseClass = clean(row.get(5)).toUpperCase(Locale.ROOT); // Cột F
            int displayRow = i + 1;

            if (sbd.isBlank()) {
                errors.add("Cột A, hàng " + displayRow + ": thiếu số báo danh.");
            }
            if (name.isBlank()) {
                errors.add("Cột B, hàng " + displayRow + ": thiếu họ và tên.");
            }
            if (dob.isBlank()) {
                errors.add("Cột C, hàng " + displayRow + ": ngày sinh phải đúng định dạng DD/MM/YYYY.");
            }
            if (!cccd.matches("\\d{9,12}")) {
                errors.add("Cột D, hàng " + displayRow + ": CCCD/CMND phải có 9-12 chữ số.");
            }
            if (!phone.matches("0\\d{9}")) {
                errors.add("Cột E, hàng " + displayRow + ": số điện thoại phải bắt đầu bằng 0 và đủ 10 chữ số.");
            }
            if (!VALID_LICENSE_CLASSES.contains(licenseClass)) {
                errors.add("Cột F, hàng " + displayRow + ": hạng GPLX chỉ nhận A1, A2, B1, B2 hoặc C.");
            }

            candidates.add(new CandidateDTO(sbd, name, dob, cccd, licenseClass, phone));
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join("<br>", errors));
        }

        return candidates;
    }

    private boolean looksLikeHeader(List<String> row) {
        String joined = String.join(" ", row).toLowerCase(Locale.ROOT);
        return joined.contains("số báo danh") || joined.contains("so bao danh") || joined.contains("họ và tên") || joined.contains("ngày sinh");
    }

    private String readCellValue(Element cell, List<String> sharedStrings, int columnIndex) {
        String type = cell.getAttribute("t");

        if ("inlineStr".equals(type)) {
            return getText(cell, "t");
        }

        String raw = getText(cell, "v");
        if (raw.isBlank()) {
            return "";
        }

        if ("s".equals(type)) {
            int sharedIndex = Integer.parseInt(raw);
            return sharedIndex >= 0 && sharedIndex < sharedStrings.size() ? sharedStrings.get(sharedIndex) : "";
        }

        if (columnIndex == 2 && raw.matches("\\d+(\\.\\d+)?")) {
            return excelSerialDateToText(raw);
        }

        return raw;
    }

    private String excelSerialDateToText(String raw) {
        long serial = Math.round(Double.parseDouble(raw));
        return LocalDate.of(1899, 12, 30).plusDays(serial).format(OUTPUT_DATE);
    }

    private List<String> readSharedStrings(byte[] xml) throws Exception {
        if (xml == null) {
            return List.of();
        }

        Document document = parseXml(xml);
        NodeList nodes = document.getElementsByTagName("si");
        List<String> values = new ArrayList<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            values.add(nodes.item(i).getTextContent());
        }
        return values;
    }

    private Document parseXml(byte[] xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        return factory.newDocumentBuilder().parse(new ByteArrayInputStream(xml));
    }

    private Map<String, byte[]> readZipEntries(InputStream input) throws IOException {
        Map<String, byte[]> entries = new HashMap<>();
        try (ZipInputStream zip = new ZipInputStream(input)) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    ByteArrayOutputStream output = new ByteArrayOutputStream();
                    zip.transferTo(output);
                    entries.put(entry.getName(), output.toByteArray());
                }
            }
        }
        return entries;
    }

    private List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (ch == ',' && !inQuotes) {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        values.add(current.toString());
        return values;
    }

    private int columnIndex(String cellRef) {
        int index = 0;
        boolean hasLetter = false;
        for (int i = 0; i < cellRef.length(); i++) {
            char ch = cellRef.charAt(i);
            if (Character.isLetter(ch)) {
                hasLetter = true;
                index = index * 26 + (Character.toUpperCase(ch) - 'A' + 1);
            }
        }
        return hasLetter ? index - 1 : -1;
    }

    private String getText(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) {
            return "";
        }
        Node node = nodes.item(0);
        return node == null ? "" : clean(node.getTextContent());
    }

    private String normalizeSbd(String value) {
        String cleaned = clean(value);
        if (cleaned.matches("\\d+(\\.0+)?")) {
            long number = Math.round(Double.parseDouble(cleaned));
            return number < 1000 ? String.format("%03d", number) : Long.toString(number);
        }
        return cleaned;
    }

    private String normalizeDate(String value) {
        String cleaned = clean(value);
        if (cleaned.isBlank()) {
            return "";
        }
        if (cleaned.matches("\\d+(\\.\\d+)?")) {
            return excelSerialDateToText(cleaned);
        }

        for (String pattern : List.of("d/M/yyyy", "dd/MM/yyyy", "d-MM-yyyy", "dd-MM-yyyy")) {
            try {
                return LocalDate.parse(cleaned, DateTimeFormatter.ofPattern(pattern)).format(OUTPUT_DATE);
            } catch (DateTimeParseException ignored) {
            }
        }
        return "";
    }

    private String normalizeDigits(String value, int maxLength) {
        String digits = clean(value).replaceAll("\\.0+$", "").replaceAll("[^0-9]", "");
        if (digits.length() == maxLength - 1 && !digits.startsWith("0")) {
            digits = "0" + digits;
        }
        return digits;
    }

    private String normalizePhone(String value) {
        String digits = normalizeDigits(value, 10);
        if (digits.length() == 9 && !digits.startsWith("0")) {
            digits = "0" + digits;
        }
        return digits;
    }

    private String clean(String value) {
        return value == null ? "" : removeBom(value).trim();
    }

    private String removeBom(String value) {
        return value != null && value.startsWith("\uFEFF") ? value.substring(1) : value;
    }

    private String getSubmittedFileName(Part part) {
        if (part == null) {
            return "";
        }
        String header = part.getHeader("content-disposition");
        if (header == null) {
            return "";
        }
        for (String token : header.split(";")) {
            String trimmed = token.trim();
            if (trimmed.startsWith("filename=")) {
                return trimmed.substring(trimmed.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return "";
    }

    private void setErrorAndRedirect(HttpServletRequest request, HttpServletResponse response, String message)
            throws IOException {
        request.getSession().setAttribute("errorMessage", message);
        response.sendRedirect(request.getContextPath() + "/manager/exam-schedules/create");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if ("clear".equals(request.getParameter("action"))) {
            HttpSession session = request.getSession();
            session.removeAttribute("importedCandidates");
            session.removeAttribute("successMessage");
            session.removeAttribute("errorMessage");
        }
        response.sendRedirect(request.getContextPath() + "/manager/exam-schedules/create");
    }
}
