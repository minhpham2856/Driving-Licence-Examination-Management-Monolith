package managingstaff.service.impl;

import managingstaff.dto.AuditDTO;
import managingstaff.service.AuditLogExcelService;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class AuditLogExcelServiceImpl implements AuditLogExcelService {

    private static final String[] HEADERS = {
        "STT", "Thời gian", "Người thực hiện", "Hành động", "Đối tượng",
        "Mã đối tượng", "Giá trị cũ", "Giá trị mới", "Chi tiết", "Lý do"
    };

    @Override
    public void writeAuditLogs(List<AuditDTO> logs, OutputStream output) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Nhật ký thao tác");
            sheet.setDisplayGridlines(false);
            sheet.createFreezePane(0, 3);
            sheet.getPrintSetup().setLandscape(true);
            sheet.getPrintSetup().setFitWidth((short) 1);
            sheet.setFitToPage(true);

            CellStyle titleStyle = titleStyle(workbook);
            CellStyle headerStyle = headerStyle(workbook);
            CellStyle textStyle = dataStyle(workbook, false);
            CellStyle centerStyle = dataStyle(workbook, true);
            CellStyle dateStyle = dataStyle(workbook, true);
            dateStyle.setDataFormat(workbook.getCreationHelper().createDataFormat()
                    .getFormat("dd/mm/yyyy hh:mm:ss"));

            Row titleRow = sheet.createRow(0);
            titleRow.setHeightInPoints(30);
            Cell title = titleRow.createCell(0);
            title.setCellValue("SỔ NHẬT KÝ THAO TÁC QUẢN LÝ");
            title.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, HEADERS.length - 1));

            Row header = sheet.createRow(2);
            header.setHeightInPoints(30);
            for (int column = 0; column < HEADERS.length; column++) {
                writeCell(header, column, HEADERS[column], headerStyle);
            }

            int rowIndex = 3;
            int sequence = 1;
            for (AuditDTO log : logs) {
                Row row = sheet.createRow(rowIndex++);
                row.setHeightInPoints(28);
                writeCell(row, 0, sequence++, centerStyle);
                Cell timeCell = row.createCell(1);
                if (log.getChangedAt() != null) {
                    LocalDateTime value = log.getChangedAt().toLocalDateTime();
                    timeCell.setCellValue(value);
                }
                timeCell.setCellStyle(dateStyle);
                writeCell(row, 2, safe(log.getChangerName()), textStyle);
                writeCell(row, 3, actionLabel(log.getAction()), centerStyle);
                writeCell(row, 4, safe(log.getTableName()), textStyle);
                writeCell(row, 5, log.getRecordId() == null ? "-" : log.getRecordId(), centerStyle);
                writeCell(row, 6, safe(log.getOldValue()), textStyle);
                writeCell(row, 7, safe(log.getNewValue()), textStyle);
                writeCell(row, 8, safe(log.getDetails()), textStyle);
                writeCell(row, 9, safe(log.getReason()), textStyle);
            }

            int[] widths = {8, 21, 24, 16, 24, 15, 28, 28, 48, 32};
            for (int column = 0; column < widths.length; column++) {
                sheet.setColumnWidth(column, widths[column] * 256);
            }
            sheet.setAutoFilter(new CellRangeAddress(2, Math.max(2, rowIndex - 1), 0, HEADERS.length - 1));
            workbook.write(output);
        }
    }

    private static CellStyle titleStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private static CellStyle headerStyle(XSSFWorkbook workbook) {
        CellStyle style = dataStyle(workbook, true);
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private static CellStyle dataStyle(XSSFWorkbook workbook, boolean centered) {
        CellStyle style = workbook.createCellStyle();
        style.setWrapText(true);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setAlignment(centered ? HorizontalAlignment.CENTER : HorizontalAlignment.LEFT);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setTopBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setLeftBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setRightBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        return style;
    }

    private static void writeCell(Row row, int column, Object value, CellStyle style) {
        Cell cell = row.createCell(column);
        if (value instanceof Number number) {
            cell.setCellValue(number.doubleValue());
        } else {
            cell.setCellValue(safeForExcel(value == null ? "-" : String.valueOf(value)));
        }
        cell.setCellStyle(style);
    }

    private static String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private static String safeForExcel(String value) {
        if (value.length() > 1 && "=+-@".indexOf(value.charAt(0)) >= 0) {
            return "'" + value;
        }
        return value;
    }

    private static String actionLabel(String action) {
        if (action == null) return "Cập nhật";
        return switch (action.toUpperCase()) {
            case "APPROVE" -> "Duyệt";
            case "INSERT" -> "Thêm";
            case "DELETE" -> "Xóa";
            case "EXPORT" -> "Xuất Excel";
            case "ASSIGN" -> "Phân công";
            case "IMPORT" -> "Nhập dữ liệu";
            case "WARNING" -> "Cảnh báo";
            case "SYSTEM" -> "Hệ thống";
            default -> "Cập nhật";
        };
    }
}
