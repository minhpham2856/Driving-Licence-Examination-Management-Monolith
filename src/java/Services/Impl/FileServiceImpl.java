package Services.Impl;

import Services.FileService;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class FileServiceImpl implements FileService {

    @Override
    public void exportToExcel(String sheetName, List<String> headers, List<List<Object>> rows, OutputStream out)
            throws IOException {

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet((sheetName == null || sheetName.isBlank()) ? "newSheet" : sheetName);

            // create header styling
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // format java.util.Date as excel dates.
            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("dd/MM/yyyy"));

            // header row
            int rowIndex = 0;
            if (headers != null && !headers.isEmpty()) {
                Row headerRow = sheet.createRow(rowIndex++);
                for (int col = 0; col < headers.size(); col++) {
                    Cell headerCell = headerRow.createCell(col);
                    headerCell.setCellValue(headers.get(col));
                    headerCell.setCellStyle(headerStyle);
                }
            }

            // data rows
            if (rows != null) {
                for (List<Object> rowData : rows) {
                    Row row = sheet.createRow(rowIndex++);
                    for (int col = 0; col < rowData.size(); col++) {
                        writeCell(row.createCell(col), rowData.get(col), dateStyle);
                    }
                }
            }

            if (headers != null) {
                for (int col = 0; col < headers.size(); col++) {
                    sheet.autoSizeColumn(col);
                }
            }

            workbook.write(out);
        }
    }

    private void writeCell(Cell cell, Object value, CellStyle dateStyle) {
        if (value == null) {
            cell.setBlank();
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else if (value instanceof Date) {
            cell.setCellValue((Date) value);
            cell.setCellStyle(dateStyle);
        } else {
            cell.setCellValue(value.toString());
        }
    }
}
