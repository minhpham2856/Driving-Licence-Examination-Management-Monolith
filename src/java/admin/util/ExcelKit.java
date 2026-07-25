package admin.util;

import java.text.SimpleDateFormat;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;

/** Tiện ích tạo/đọc file Excel dùng chung cho các màn hình quản trị. */
public final class ExcelKit {

    private ExcelKit() {}

    // ------------------------------------------------------------------ styles

    public static CellStyle titleStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setBold(true);
        f.setFontHeightInPoints((short) 14);
        s.setFont(f);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        return s;
    }

    public static CellStyle noteStyle(Workbook wb) {
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

    public static CellStyle headerStyle(Workbook wb) {
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

    public static CellStyle dataStyle(Workbook wb, boolean center) {
        CellStyle s = wb.createCellStyle();
        s.setAlignment(center ? HorizontalAlignment.CENTER : HorizontalAlignment.LEFT);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        s.setWrapText(true);
        border(s);
        return s;
    }

    private static void border(CellStyle s) {
        s.setBorderTop(BorderStyle.THIN);
        s.setBorderBottom(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN);
        s.setBorderRight(BorderStyle.THIN);
    }

    // ------------------------------------------------------------------- write

    public static void writeCell(Row row, int col, String value, CellStyle style) {
        Cell c = row.createCell(col);
        c.setCellValue(value == null ? "" : value);
        c.setCellStyle(style);
    }

    public static void writeCell(Row row, int col, int value, CellStyle style) {
        Cell c = row.createCell(col);
        c.setCellValue(value);
        c.setCellStyle(style);
    }

    /** Tự giãn cột nhưng kẹp trong khoảng hợp lý để bảng không quá rộng. */
    public static void autoSize(Sheet sheet, int columns) {
        for (int i = 0; i < columns; i++) {
            sheet.autoSizeColumn(i);
            int w = sheet.getColumnWidth(i);
            sheet.setColumnWidth(i, Math.min(Math.max(w + 600, 2800), 12000));
        }
    }

    public static String safe(String s) {
        return s == null || s.trim().isEmpty() ? "-" : s.trim();
    }

    // -------------------------------------------------------------------- read

    /** Đọc ô về chuỗi; số nguyên không kèm phần thập phân (giữ đúng SĐT/CCCD). */
    public static String str(Row row, int col) {
        if (row == null) return "";
        Cell c = row.getCell(col);
        if (c == null) return "";
        switch (c.getCellType()) {
            case STRING:
                return c.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(c)) {
                    return new SimpleDateFormat("dd/MM/yyyy").format(c.getDateCellValue());
                }
                double d = c.getNumericCellValue();
                if (d == Math.floor(d) && !Double.isInfinite(d)) return String.valueOf((long) d);
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

    /** dd/MM/yyyy, dd-MM-yyyy hoặc yyyy-MM-dd -> yyyy-MM-dd; không parse được thì giữ nguyên. */
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

    /** Tìm dòng tiêu đề theo chữ bắt đầu cột A; mặc định dòng index 2 của biểu mẫu. */
    public static int findHeaderRow(Sheet sheet, String firstHeaderPrefix) {
        int limit = Math.min(sheet.getLastRowNum(), 20);
        String want = firstHeaderPrefix.toLowerCase();
        for (int i = 0; i <= limit; i++) {
            Row r = sheet.getRow(i);
            if (r == null) continue;
            String first = str(r, 0);
            if (first != null && first.toLowerCase().startsWith(want)) return i;
        }
        return 2;
    }
}
