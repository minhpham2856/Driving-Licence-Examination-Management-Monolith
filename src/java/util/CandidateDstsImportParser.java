package util;

import dto.exam.ExamRegistrationDTO;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Import danh sách thí sinh format DSTS / PC08 (10 cột).
 */
public final class CandidateDstsImportParser {

    public static final int COLUMN_COUNT = 10;

    private CandidateDstsImportParser() {
    }

    public static final class ParseResult {
        private final List<ExamRegistrationDTO> rows;
        private final boolean hasInvalidRows;

        public ParseResult(List<ExamRegistrationDTO> rows, boolean hasInvalidRows) {
            this.rows = rows != null ? rows : List.of();
            this.hasInvalidRows = hasInvalidRows;
        }

        public List<ExamRegistrationDTO> getRows() {
            return rows;
        }

        public boolean hasInvalidRows() {
            return hasInvalidRows;
        }
    }

    public static ParseResult parse(byte[] fileBytes, String fileName, String examLicenseCode) throws IOException {
        if (fileBytes == null || fileBytes.length == 0) {
            throw new IOException("Tệp rỗng.");
        }
        String lowerName = fileName == null ? "" : fileName.toLowerCase(Locale.ROOT);
        if (lowerName.endsWith(".xlsx") || lowerName.endsWith(".xls")) {
            return parseExcel(fileBytes, examLicenseCode);
        }
        if (lowerName.endsWith(".csv") || lowerName.endsWith(".txt")) {
            return parseCsv(fileBytes, examLicenseCode);
        }
        throw new IOException("Định dạng không hỗ trợ. Chấp nhận .xlsx, .xls, .csv, .txt.");
    }

    private static ParseResult parseExcel(byte[] fileBytes, String examLicenseCode) throws IOException {
        List<String[]> rawRows = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();
        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(fileBytes))) {
            Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
            if (sheet == null) {
                throw new IOException("Không tìm thấy sheet trong file Excel.");
            }
            int lastRow = sheet.getLastRowNum();
            for (int r = 0; r <= lastRow; r++) {
                Row row = sheet.getRow(r);
                if (row == null) {
                    continue;
                }
                String[] cols = new String[COLUMN_COUNT];
                boolean any = false;
                for (int c = 0; c < COLUMN_COUNT; c++) {
                    Cell cell = row.getCell(c);
                    String val = cell == null ? "" : formatter.formatCellValue(cell).trim();
                    cols[c] = val;
                    if (!val.isEmpty()) {
                        any = true;
                    }
                }
                if (any) {
                    rawRows.add(cols);
                }
            }
        }
        return buildRows(rawRows, examLicenseCode);
    }

    private static ParseResult parseCsv(byte[] fileBytes, String examLicenseCode) throws IOException {
        Charset charset = detectCharset(fileBytes);
        String content = new String(fileBytes, charset);
        if (content.startsWith("\uFEFF")) {
            content = content.substring(1);
        }
        List<String[]> rawRows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new StringReader(content))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] parts = CsvRowParser.parseLine(line);
                if (parts.length < COLUMN_COUNT) {
                    String[] padded = new String[COLUMN_COUNT];
                    System.arraycopy(parts, 0, padded, 0, parts.length);
                    for (int i = parts.length; i < COLUMN_COUNT; i++) {
                        padded[i] = "";
                    }
                    rawRows.add(padded);
                } else if (parts.length > COLUMN_COUNT) {
                    String[] merged = new String[COLUMN_COUNT];
                    System.arraycopy(parts, 0, merged, 0, COLUMN_COUNT - 1);
                    StringBuilder tail = new StringBuilder(parts[COLUMN_COUNT - 1]);
                    for (int i = COLUMN_COUNT; i < parts.length; i++) {
                        tail.append(',').append(parts[i]);
                    }
                    merged[COLUMN_COUNT - 1] = tail.toString().trim();
                    rawRows.add(merged);
                } else {
                    rawRows.add(parts);
                }
            }
        }
        return buildRows(rawRows, examLicenseCode);
    }

    private static ParseResult buildRows(List<String[]> rawRows, String examLicenseCode) throws IOException {
        if (rawRows.isEmpty()) {
            throw new IOException("File không có dòng dữ liệu.");
        }
        int start = 0;
        if (isHeaderRow(rawRows.get(0))) {
            start = 1;
        }
        if (start >= rawRows.size()) {
            throw new IOException("File chỉ có dòng tiêu đề, không có thí sinh.");
        }

        List<ExamRegistrationDTO> result = new ArrayList<>();
        Set<String> seenCccd = new HashSet<>();
        Set<Integer> seenSbd = new HashSet<>();
        boolean hasInvalid = false;

        for (int i = start; i < rawRows.size(); i++) {
            String[] parts = rawRows.get(i);
            ExamRegistrationDTO reg = mapRow(parts, examLicenseCode);

            if (!seenCccd.add(reg.getGovIdNo())) {
                markInvalid(reg, "Trùng CCCD trong file");
                hasInvalid = true;
            }
            if (reg.getCandidateNo() > 0 && !seenSbd.add(reg.getCandidateNo())) {
                markInvalid(reg, "Trùng SBD trong file");
                hasInvalid = true;
            }
            if (reg.isInvalid()) {
                hasInvalid = true;
            }
            result.add(reg);
        }
        return new ParseResult(result, hasInvalid);
    }

    private static boolean isHeaderRow(String[] parts) {
        if (parts == null || parts.length == 0) {
            return false;
        }
        String first = parts[0] == null ? "" : parts[0].toLowerCase(Locale.ROOT);
        if (first.contains("báo danh") || first.contains("bao danh") || first.contains("sbd")) {
            return true;
        }
        String joined = String.join(" ", parts).toLowerCase(Locale.ROOT);
        return joined.contains("nội dung sh") || joined.contains("noi dung sh");
    }

    private static ExamRegistrationDTO mapRow(String[] parts, String examLicenseCode) {
        String sbdRaw = col(parts, 0);
        String fullName = col(parts, 1);
        String cccd = col(parts, 2);
        String dobStr = col(parts, 3);
        String sex = col(parts, 4);
        String address = col(parts, 5);
        String licenseCode = col(parts, 6);
        String shContent = col(parts, 7);
        String phone = col(parts, 8);
        String email = col(parts, 9);

        ExamRegistrationDTO reg = new ExamRegistrationDTO();
        reg.setFullName(fullName);
        reg.setGovIdNo(cccd);
        reg.setSex(normalizeSex(sex));
        reg.setAddress(address);
        reg.setLicenseCode(licenseCode.isEmpty() ? examLicenseCode : licenseCode);
        reg.setPhoneNo(phone);
        reg.setEmail(email);
        reg.setRegistrationType("WalkIn");
        reg.setIsPaymentCompleted(false);
        reg.setIsPresent(true);

        int candidateNo = FormatUtil.parseCandidateNo(sbdRaw);
        if (candidateNo <= 0) {
            try {
                candidateNo = Integer.parseInt(sbdRaw.replaceAll("\\D", ""));
            } catch (NumberFormatException ignored) {
                candidateNo = 0;
            }
        }
        reg.setCandidateNo(candidateNo);

        reg.setDateOfBirth(parseDate(dobStr));
        CandidateShContentParser.apply(shContent, reg);

        List<String> errors = new ArrayList<>();
        if (sbdRaw.isEmpty() || candidateNo <= 0) {
            errors.add("SBD");
        }
        if (fullName.isEmpty()) {
            errors.add("Họ tên");
        }
        if (cccd.isEmpty()) {
            errors.add("CCCD");
        }
        if (dobStr.isEmpty() || reg.getDateOfBirth() == null) {
            errors.add("Ngày sinh");
        }
        if (licenseCode.isEmpty()) {
            errors.add("Hạng GPLX");
        } else if (examLicenseCode != null && !examLicenseCode.isBlank()
                && !CandidateImportLicenseUtil.matchesExam(licenseCode, examLicenseCode)) {
            errors.add("Hạng GPLX không khớp kỳ thi (" + examLicenseCode + ")");
        }
        if (shContent.isEmpty()) {
            errors.add("Nội dung SH");
        }
        if (phone.isEmpty()) {
            errors.add("SĐT");
        }
        if (email.isEmpty()) {
            errors.add("Email");
        } else if (!email.contains("@")) {
            errors.add("Email không hợp lệ");
        }

        if (!errors.isEmpty()) {
            markInvalid(reg, "Thiếu / lỗi: " + String.join(", ", errors));
        }
        return reg;
    }

    private static void markInvalid(ExamRegistrationDTO reg, String message) {
        reg.setInvalid(true);
        String existing = reg.getValidationMessage();
        if (existing == null || existing.isBlank()) {
            reg.setValidationMessage(message);
        } else if (!existing.contains(message)) {
            reg.setValidationMessage(existing + "; " + message);
        }
    }

    private static String col(String[] parts, int index) {
        if (parts == null || index < 0 || index >= parts.length || parts[index] == null) {
            return "";
        }
        return parts[index].trim();
    }

    private static String normalizeSex(String sex) {
        if (sex == null || sex.isBlank()) {
            return "Nam";
        }
        String s = sex.trim();
        if (s.equalsIgnoreCase("nữ") || s.equalsIgnoreCase("nu") || s.equalsIgnoreCase("female")) {
            return "Nữ";
        }
        return "Nam";
    }

    private static Date parseDate(String dobStr) {
        if (dobStr == null || dobStr.isBlank()) {
            return null;
        }
        try {
            if (dobStr.contains("/")) {
                String[] dp = dobStr.split("/");
                if (dp.length == 3) {
                    String year = dp[2].length() == 2 ? "20" + dp[2] : dp[2];
                    return Date.valueOf(year + "-" + pad2(dp[1]) + "-" + pad2(dp[0]));
                }
            }
            if (dobStr.contains("-")) {
                return Date.valueOf(dobStr.trim());
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static String pad2(String part) {
        return part.length() == 1 ? "0" + part : part;
    }

    private static Charset detectCharset(byte[] fileBytes) {
        if (isValidUtf8(fileBytes)) {
            return StandardCharsets.UTF_8;
        }
        for (String name : new String[]{"Windows-1258", "Cp1258", "Cp1252"}) {
            try {
                return Charset.forName(name);
            } catch (Exception ignored) {
            }
        }
        return StandardCharsets.UTF_8;
    }

    private static boolean isValidUtf8(byte[] bytes) {
        int i = 0;
        if (bytes.length >= 3 && (bytes[0] & 0xFF) == 0xEF && (bytes[1] & 0xFF) == 0xBB && (bytes[2] & 0xFF) == 0xBF) {
            i = 3;
        }
        while (i < bytes.length) {
            int b = bytes[i] & 0xFF;
            if (b <= 0x7F) {
                i++;
            } else if ((b & 0xE0) == 0xC0) {
                if (i + 1 >= bytes.length || (bytes[i + 1] & 0xC0) != 0x80) {
                    return false;
                }
                i += 2;
            } else if ((b & 0xF0) == 0xE0) {
                if (i + 2 >= bytes.length || (bytes[i + 1] & 0xC0) != 0x80 || (bytes[i + 2] & 0xC0) != 0x80) {
                    return false;
                }
                i += 3;
            } else if ((b & 0xF8) == 0xF0) {
                if (i + 3 >= bytes.length || (bytes[i + 1] & 0xC0) != 0x80 || (bytes[i + 2] & 0xC0) != 0x80
                        || (bytes[i + 3] & 0xC0) != 0x80) {
                    return false;
                }
                i += 4;
            } else {
                return false;
            }
        }
        return true;
    }
}
