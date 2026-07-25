package admin.service.impl;

import admin.model.AccountView;
import admin.model.RoleOption;
import admin.service.AccountExcelService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class AccountExcelServiceImpl implements AccountExcelService {

    /** Cột của file import — thứ tự này phải khớp với readImport(). */
    private static final String[] IMPORT_HEADERS = {
        "Tên đăng nhập *", "Email *", "Vai trò *", "Họ và tên *", "Số điện thoại *",
        "Ngày sinh (dd/mm/yyyy) *", "Giới tính *", "Số CCCD/CMND *", "Địa chỉ", "Trạng thái"
    };

    private static final String[] EXPORT_HEADERS = {
        "STT", "Tên đăng nhập", "Họ và tên", "Email", "Số điện thoại",
        "Vai trò", "Giới tính", "Ngày sinh", "Số CCCD/CMND", "Địa chỉ", "Trạng thái"
    };

    private static final String SHEET_IMPORT = "Danh sách tài khoản";

    // ---------------------------------------------------------------- template

    @Override
    public void writeTemplate(List<RoleOption> roles, OutputStream out) throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet(SHEET_IMPORT);
            sheet.setDisplayGridlines(false);

            CellStyle titleStyle = titleStyle(wb);
            CellStyle noteStyle = noteStyle(wb);
            CellStyle headerStyle = headerStyle(wb);
            CellStyle sampleStyle = dataStyle(wb, false);

            Row titleRow = sheet.createRow(0);
            titleRow.setHeightInPoints(28);
            Cell title = titleRow.createCell(0);
            title.setCellValue("BIỂU MẪU IMPORT TÀI KHOẢN HỆ THỐNG");
            title.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, IMPORT_HEADERS.length - 1));

            Row noteRow = sheet.createRow(1);
            noteRow.setHeightInPoints(32);
            Cell note = noteRow.createCell(0);
            note.setCellValue("Điền dữ liệu từ dòng 4. Cột có dấu * là bắt buộc. "
                    + "Vai trò hợp lệ: " + roleNames(roles)
                    + ". Giới tính: Nam / Nữ. Trạng thái: Hoạt động / Khóa (bỏ trống = Hoạt động). "
                    + "Mật khẩu do hệ thống tự sinh và gửi về email của từng tài khoản.");
            note.setCellStyle(noteStyle);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, IMPORT_HEADERS.length - 1));

            Row header = sheet.createRow(2);
            header.setHeightInPoints(30);
            for (int i = 0; i < IMPORT_HEADERS.length; i++) {
                writeCell(header, i, IMPORT_HEADERS[i], headerStyle);
            }

            // Một dòng ví dụ để Admin biết định dạng
            Row sample = sheet.createRow(3);
            String firstRole = roles != null && !roles.isEmpty() ? roles.get(0).getRoleName() : "Cán bộ kỳ thi";
            String[] sampleData = {
                "nguyenvana", "nguyenvana@example.com", firstRole, "Nguyễn Văn A", "0901234567",
                "01/01/1995", "Nam", "012345678901", "Hà Nội", "Hoạt động"
            };
            for (int i = 0; i < sampleData.length; i++) {
                writeCell(sample, i, sampleData[i], sampleStyle);
            }

            autoSize(sheet, IMPORT_HEADERS.length);
            wb.write(out);
        }
    }

    private String roleNames(List<RoleOption> roles) {
        if (roles == null || roles.isEmpty()) return "(chưa có vai trò)";
        StringBuilder sb = new StringBuilder();
        for (RoleOption r : roles) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(r.getRoleName());
        }
        return sb.toString();
    }

    // ------------------------------------------------------------------ export

    @Override
    public void writeAccounts(List<AccountView> accounts, OutputStream out) throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Tài khoản hệ thống");
            sheet.setDisplayGridlines(false);
            sheet.createFreezePane(0, 3);
            sheet.getPrintSetup().setLandscape(true);
            sheet.setFitToPage(true);

            CellStyle titleStyle = titleStyle(wb);
            CellStyle headerStyle = headerStyle(wb);
            CellStyle textStyle = dataStyle(wb, false);
            CellStyle centerStyle = dataStyle(wb, true);

            Row titleRow = sheet.createRow(0);
            titleRow.setHeightInPoints(30);
            Cell title = titleRow.createCell(0);
            title.setCellValue("DANH SÁCH TÀI KHOẢN HỆ THỐNG");
            title.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, EXPORT_HEADERS.length - 1));

            Row header = sheet.createRow(2);
            header.setHeightInPoints(28);
            for (int i = 0; i < EXPORT_HEADERS.length; i++) {
                writeCell(header, i, EXPORT_HEADERS[i], headerStyle);
            }

            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
            int rowIndex = 3;
            int stt = 1;
            if (accounts != null) {
                for (AccountView a : accounts) {
                    Row row = sheet.createRow(rowIndex++);
                    row.setHeightInPoints(24);
                    writeCell(row, 0, stt++, centerStyle);
                    writeCell(row, 1, safe(a.getUsername()), textStyle);
                    writeCell(row, 2, safe(a.getFullName()), textStyle);
                    writeCell(row, 3, safe(a.getEmail()), textStyle);
                    writeCell(row, 4, safe(a.getPhone()), centerStyle);
                    writeCell(row, 5, safe(a.getRole()), textStyle);
                    writeCell(row, 6, a.isSexMale() ? "Nam" : "Nữ", centerStyle);
                    writeCell(row, 7, a.getDateOfBirth() == null ? "-" : df.format(a.getDateOfBirth()), centerStyle);
                    writeCell(row, 8, safe(a.getGovId()), centerStyle);
                    writeCell(row, 9, safe(a.getAddress()), textStyle);
                    writeCell(row, 10, a.isActive() ? "Hoạt động" : "Khóa / Vô hiệu", centerStyle);
                }
            }

            autoSize(sheet, EXPORT_HEADERS.length);
            wb.write(out);
        }
    }

    // ------------------------------------------------------------------ import

    @Override
    public List<ImportRow> readImport(InputStream in) throws IOException {
        List<ImportRow> rows = new ArrayList<>();
        try (Workbook wb = new XSSFWorkbook(in)) {
            Sheet sheet = wb.getSheetAt(0);
            if (sheet == null) return rows;

            int headerRowIdx = findHeaderRow(sheet);
            for (int i = headerRowIdx + 1; i <= sheet.getLastRowNum(); i++) {
                Row r = sheet.getRow(i);
                if (r == null) continue;

                ImportRow row = new ImportRow();
                row.rowNumber = i + 1; // Excel hiển thị 1-based
                row.username = str(r, 0);
                row.email = str(r, 1);
                row.roleName = str(r, 2);
                row.fullName = str(r, 3);
                row.phone = str(r, 4);
                row.dateOfBirth = dateStr(r, 5);
                row.sex = str(r, 6);
                row.govId = str(r, 7);
                row.address = str(r, 8);
                row.status = str(r, 9);

                if (!row.isBlank()) rows.add(row);
            }
        }
        return rows;
    }

    /** Tìm dòng tiêu đề (dòng chứa "Tên đăng nhập"); mặc định dòng thứ 3 của biểu mẫu. */
    private int findHeaderRow(Sheet sheet) {
        int limit = Math.min(sheet.getLastRowNum(), 20);
        for (int i = 0; i <= limit; i++) {
            Row r = sheet.getRow(i);
            if (r == null) continue;
            String first = str(r, 0);
            if (first != null && first.toLowerCase().startsWith("tên đăng nhập")) return i;
        }
        return 2;
    }

    private String str(Row row, int col) {
        Cell c = row.getCell(col);
        if (c == null) return "";
        switch (c.getCellType()) {
            case STRING:
                return c.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(c)) {
                    return new SimpleDateFormat("dd/MM/yyyy").format(c.getDateCellValue());
                }
                // Số điện thoại / CCCD nhập dạng số -> bỏ phần thập phân
                double d = c.getNumericCellValue();
                if (d == Math.floor(d) && !Double.isInfinite(d)) {
                    return String.valueOf((long) d);
                }
                return String.valueOf(d);
            case BOOLEAN:
                return String.valueOf(c.getBooleanCellValue());
            case FORMULA:
                try {
                    return c.getStringCellValue().trim();
                } catch (IllegalStateException e) {
                    return String.valueOf((long) c.getNumericCellValue());
                }
            default:
                return "";
        }
    }

    /** Trả về ngày sinh chuẩn hóa yyyy-MM-dd, chấp nhận ô kiểu Date hoặc chuỗi dd/MM/yyyy. */
    private String dateStr(Row row, int col) {
        Cell c = row.getCell(col);
        if (c == null) return "";
        if (c.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(c)) {
            Date d = c.getDateCellValue();
            LocalDate ld = LocalDateTime.ofInstant(d.toInstant(), ZoneId.systemDefault()).toLocalDate();
            return ld.toString();
        }
        String raw = str(row, col);
        return normalizeDate(raw);
    }

    /** dd/MM/yyyy hoặc dd-MM-yyyy hoặc yyyy-MM-dd -> yyyy-MM-dd; không parse được thì trả nguyên văn. */
    public static String normalizeDate(String raw) {
        if (raw == null) return "";
        String s = raw.trim();
        if (s.isEmpty()) return "";
        if (s.matches("\\d{4}-\\d{1,2}-\\d{1,2}")) {
            String[] p = s.split("-");
            return String.format("%04d-%02d-%02d",
                    Integer.parseInt(p[0]), Integer.parseInt(p[1]), Integer.parseInt(p[2]));
        }
        if (s.matches("\\d{1,2}[/-]\\d{1,2}[/-]\\d{4}")) {
            String[] p = s.split("[/-]");
            return String.format("%04d-%02d-%02d",
                    Integer.parseInt(p[2]), Integer.parseInt(p[1]), Integer.parseInt(p[0]));
        }
        return s;
    }

    // ------------------------------------------------------------------ styles

    private CellStyle titleStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setBold(true);
        f.setFontHeightInPoints((short) 14);
        s.setFont(f);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        return s;
    }

    private CellStyle noteStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setItalic(true);
        f.setFontHeightInPoints((short) 10);
        s.setFont(f);
        s.setAlignment(HorizontalAlignment.LEFT);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        s.setWrapText(true);
        return s;
    }

    private CellStyle headerStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setBold(true);
        f.setColor(IndexedColors.WHITE.getIndex());
        s.setFont(f);
        s.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        s.setWrapText(true);
        border(s);
        return s;
    }

    private CellStyle dataStyle(Workbook wb, boolean center) {
        CellStyle s = wb.createCellStyle();
        s.setAlignment(center ? HorizontalAlignment.CENTER : HorizontalAlignment.LEFT);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        s.setWrapText(true);
        border(s);
        return s;
    }

    private void border(CellStyle s) {
        s.setBorderTop(BorderStyle.THIN);
        s.setBorderBottom(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN);
        s.setBorderRight(BorderStyle.THIN);
    }

    private void writeCell(Row row, int col, String value, CellStyle style) {
        Cell c = row.createCell(col);
        c.setCellValue(value);
        c.setCellStyle(style);
    }

    private void writeCell(Row row, int col, int value, CellStyle style) {
        Cell c = row.createCell(col);
        c.setCellValue(value);
        c.setCellStyle(style);
    }

    private void autoSize(Sheet sheet, int columns) {
        for (int i = 0; i < columns; i++) {
            sheet.autoSizeColumn(i);
            int w = sheet.getColumnWidth(i);
            // giới hạn bề rộng để bảng không bị kéo quá dài
            sheet.setColumnWidth(i, Math.min(Math.max(w + 600, 2800), 12000));
        }
    }

    private String safe(String s) {
        return s == null || s.trim().isEmpty() ? "-" : s.trim();
    }
}
