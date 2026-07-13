package Services.Impl;

import DTOs.DossierDTO;
import Services.ApprovedCandidateExcelService;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ApprovedCandidateExcelServiceImpl implements ApprovedCandidateExcelService {

    private static final String[] HEADERS = {
        "STT", "Mã hồ sơ", "Họ và tên", "Ngày sinh", "Giới tính", "Số CCCD",
        "Số điện thoại", "Email", "Địa chỉ thường trú", "Hạng GPLX", "Nguồn hồ sơ", "Trạng thái"
    };

    @Override
    public void writeApprovedCandidates(String licenceClass, List<DossierDTO> dossiers, OutputStream output)
            throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Hồ sơ " + licenceClass);
            sheet.setDisplayGridlines(false);
            sheet.createFreezePane(0, 5);
            sheet.getPrintSetup().setLandscape(true);
            sheet.getPrintSetup().setFitWidth((short) 1);
            sheet.setFitToPage(true);
            sheet.setRepeatingRows(new CellRangeAddress(4, 4, -1, -1));

            CellStyle titleStyle = titleStyle(workbook);
            CellStyle metaStyle = metaStyle(workbook);
            CellStyle headerStyle = headerStyle(workbook);
            CellStyle textStyle = dataStyle(workbook, false);
            CellStyle centeredStyle = dataStyle(workbook, true);
            CellStyle dateStyle = dataStyle(workbook, true);
            dateStyle.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("dd/MM/yyyy"));
            CellStyle idStyle = dataStyle(workbook, true);
            idStyle.setDataFormat(workbook.createDataFormat().getFormat("@"));

            Row titleRow = sheet.createRow(0);
            titleRow.setHeightInPoints(30);
            Cell title = titleRow.createCell(0);
            title.setCellValue("DANH SÁCH THÍ SINH ĐÃ DUYỆT ĐỀ NGHỊ SÁT HẠCH CẤP GPLX HẠNG " + licenceClass);
            title.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, HEADERS.length - 1));

            Row unitRow = sheet.createRow(1);
            Cell unit = unitRow.createCell(0);
            unit.setCellValue("Đơn vị lập danh sách: Trung tâm sát hạch Lái Vui");
            unit.setCellStyle(metaStyle);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, HEADERS.length - 1));

            Row dateRow = sheet.createRow(2);
            Cell date = dateRow.createCell(0);
            date.setCellValue("Ngày lập danh sách: "
                    + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            date.setCellStyle(metaStyle);
            sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, HEADERS.length - 1));

            Row header = sheet.createRow(4);
            header.setHeightInPoints(32);
            for (int column = 0; column < HEADERS.length; column++) {
                Cell cell = header.createCell(column);
                cell.setCellValue(HEADERS[column]);
                cell.setCellStyle(headerStyle);
            }

            int rowIndex = 5;
            int sequence = 1;
            for (DossierDTO dossier : dossiers) {
                Row row = sheet.createRow(rowIndex++);
                row.setHeightInPoints(29);
                writeNumber(row, 0, sequence++, centeredStyle);
                writeNumber(row, 1, dossier.getRegistrationId(), centeredStyle);
                writeText(row, 2, dossier.getProfile().getFullName(), textStyle);
                Cell dob = row.createCell(3);
                if (dossier.getProfile().getDateOfBirth() != null) {
                    dob.setCellValue(dossier.getProfile().getDateOfBirth());
                }
                dob.setCellStyle(dateStyle);
                writeText(row, 4, dossier.getProfile().getSex(), centeredStyle);
                writeText(row, 5, dossier.getProfile().getGovIdNo(), idStyle);
                writeText(row, 6, dossier.getProfile().getPhoneNo(), idStyle);
                writeText(row, 7, dossier.getUser().getEmail(), textStyle);
                writeText(row, 8, dossier.getProfile().getAddress(), textStyle);
                writeText(row, 9, dossier.getLicenceDisplayClass(), centeredStyle);
                writeText(row, 10, dossier.getSourceLabel(), textStyle);
                writeText(row, 11, dossier.getStatusLabel(), centeredStyle);
            }

            int lastDataRow = Math.max(4, rowIndex - 1);
            sheet.setAutoFilter(new CellRangeAddress(4, lastDataRow, 0, HEADERS.length - 1));

            Row totalRow = sheet.createRow(rowIndex + 1);
            Cell total = totalRow.createCell(0);
            total.setCellValue("Tổng số hồ sơ đã duyệt hạng " + licenceClass + ": " + dossiers.size());
            total.setCellStyle(metaStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowIndex + 1, rowIndex + 1, 0, HEADERS.length - 1));

            int signatureRowIndex = rowIndex + 4;
            Row signatureRow = sheet.createRow(signatureRowIndex);
            signatureRow.setHeightInPoints(24);
            writeSignature(sheet, signatureRow, 0, 5, "NGƯỜI LẬP DANH SÁCH", metaStyle);
            writeSignature(sheet, signatureRow, 6, 11, "ĐẠI DIỆN TRUNG TÂM", metaStyle);
            Row signatureNote = sheet.createRow(signatureRowIndex + 1);
            writeSignature(sheet, signatureNote, 0, 5, "(Ký, ghi rõ họ tên)", metaStyle);
            writeSignature(sheet, signatureNote, 6, 11, "(Ký, đóng dấu, ghi rõ họ tên)", metaStyle);

            int[] widths = {7, 14, 28, 14, 11, 19, 17, 30, 40, 13, 20, 16};
            for (int i = 0; i < widths.length; i++) sheet.setColumnWidth(i, widths[i] * 256);

            workbook.getProperties().getCoreProperties().setCreator("Trung tâm sát hạch Lái Vui");
            workbook.getProperties().getCoreProperties().setTitle(
                    "Danh sách hồ sơ đã duyệt hạng " + licenceClass);
            workbook.write(output);
        }
    }

    private static CellStyle titleStyle(Workbook workbook) {
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

    private static CellStyle metaStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private static CellStyle headerStyle(Workbook workbook) {
        CellStyle style = dataStyle(workbook, true);
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setWrapText(true);
        return style;
    }

    private static CellStyle dataStyle(Workbook workbook, boolean centered) {
        CellStyle style = workbook.createCellStyle();
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setAlignment(centered ? HorizontalAlignment.CENTER : HorizontalAlignment.LEFT);
        style.setWrapText(true);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setTopBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setLeftBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setRightBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        return style;
    }

    private static void writeText(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value == null ? "" : value);
        cell.setCellStyle(style);
    }

    private static void writeNumber(Row row, int column, int value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private static void writeSignature(Sheet sheet, Row row, int from, int to,
            String value, CellStyle style) {
        Cell cell = row.createCell(from);
        cell.setCellValue(value);
        CellStyle centered = sheet.getWorkbook().createCellStyle();
        centered.cloneStyleFrom(style);
        centered.setAlignment(HorizontalAlignment.CENTER);
        cell.setCellStyle(centered);
        sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), from, to));
    }
}
