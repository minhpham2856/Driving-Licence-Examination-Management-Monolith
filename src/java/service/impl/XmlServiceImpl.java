package service.impl;
import dto.*;
import model.*;

import java.text.*;


import service.XmlService;
import dto.XmlExportDocument;
import dto.XmlExportTable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


public class XmlServiceImpl implements XmlService {

    
    @Override
    public void exportToExcel(String sheetName, List<String> headers, List<List<Object>> rows, OutputStream out)
            throws IOException {
        
        exportToExcel(sheetName, null, headers, rows, out);
    }

    
    @Override
    public void exportToExcel(String sheetName, List<List<Object>> preambleRows, List<String> headers,
            List<List<Object>> rows, OutputStream out) throws IOException {

        
        try (Workbook workbook = new XSSFWorkbook()) {
            
            Sheet sheet = workbook.createSheet((sheetName == null || sheetName.isBlank()) ? "newSheet" : sheetName);

            
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            
            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("dd/MM/yyyy"));

            
            int rowIndex = 0;
            
            
            if (preambleRows != null) {
                
                for (List<Object> preambleRow : preambleRows) {
                    
                    Row row = sheet.createRow(rowIndex++);
                    
                    if (preambleRow == null) {
                        continue;
                    }
                    
                    for (int col = 0; col < preambleRow.size(); col++) {
                        
                        writeCell(row.createCell(col), preambleRow.get(col), dateStyle);
                    }
                }
            }

            
            int headerColCount = 0;
            
            
            if (headers != null && !headers.isEmpty()) {
                
                Row headerRow = sheet.createRow(rowIndex++);
                
                headerColCount = headers.size();
                
                for (int col = 0; col < headers.size(); col++) {
                    Cell headerCell = headerRow.createCell(col);
                    
                    headerCell.setCellValue(headers.get(col));
                    
                    headerCell.setCellStyle(headerStyle);
                }
            }

            
            if (rows != null) {
                
                for (List<Object> rowData : rows) {
                    
                    Row row = sheet.createRow(rowIndex++);
                    
                    for (int col = 0; col < rowData.size(); col++) {
                        
                        writeCell(row.createCell(col), rowData.get(col), dateStyle);
                    }
                    
                    headerColCount = Math.max(headerColCount, rowData.size());
                }
            }

            
            for (int col = 0; col < headerColCount; col++) {
                
                sheet.autoSizeColumn(col);
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

    
    @Override
    public void exportToXml(XmlExportDocument document, OutputStream out) throws IOException {
        
        if (document == null || document.rootElement() == null || document.rootElement().isBlank()) {
            throw new IllegalArgumentException("XML root element is required.");
        }

        
        StringBuilder sb = new StringBuilder(4096);
        
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        
        appendOpenTag(sb, document.rootElement());
        sb.append('\n');

        
        if (document.metadata() != null && !document.metadata().isEmpty()) {
            
            appendMetadata(sb, document.metadata(), "  ");
        }

        
        if (document.tables() != null) {
            
            for (XmlExportTable table : document.tables()) {
                
                appendTable(sb, table, "  ");
            }
        }

        
        appendCloseTag(sb, document.rootElement());
        sb.append('\n');
        
        out.write(sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    
    private void appendMetadata(StringBuilder sb, Map<String, Object> metadata, String indent) {
        
        for (Map.Entry<String, Object> entry : metadata.entrySet()) {
            
            appendValueElement(sb, entry.getKey(), entry.getValue(), indent);
        }
    }

    
    private void appendValueElement(StringBuilder sb, String elementName, Object value, String indent) {
        
        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> nestedMap = (Map<String, Object>) value;
            
            appendOpenTag(sb, indent, elementName);
            sb.append('\n');
            
            appendMetadata(sb, nestedMap, indent + "  ");
            
            appendCloseTag(sb, indent, elementName);
            sb.append('\n');
            return;
        }

        
        sb.append(indent).append('<').append(elementName).append('>');
        sb.append(escapeXml(formatXmlValue(value)));
        appendCloseTag(sb, elementName);
        sb.append('\n');
    }

    
    private void appendTable(StringBuilder sb, XmlExportTable table, String indent) {
        
        if (table == null || table.listElement() == null || table.listElement().isBlank()) {
            return;
        }

        
        appendOpenTag(sb, indent, table.listElement());
        sb.append('\n');

        
        String itemIndent = indent + "  ";
        String fieldIndent = itemIndent + "  ";
        
        
        List<String> fields = table.fieldElements();
        List<List<Object>> rows = table.rows();

        if (rows != null) {
            
            for (List<Object> row : rows) {
                
                appendOpenTag(sb, itemIndent, table.itemElement());
                sb.append('\n');
                
                
                if (fields != null && row != null) {
                    
                    int count = Math.min(fields.size(), row.size());
                    for (int i = 0; i < count; i++) {
                        
                        appendValueElement(sb, fields.get(i), row.get(i), fieldIndent);
                    }
                }
                
                
                appendCloseTag(sb, itemIndent, table.itemElement());
                sb.append('\n');
            }
        }

        
        appendCloseTag(sb, indent, table.listElement());
        sb.append('\n');
    }

    
    private static String formatXmlValue(Object value) {
        
        if (value == null) {
            return "";
        }
        
        if (value instanceof Date) {
            Date date = (Date) value;
            return new SimpleDateFormat("dd/MM/yyyy").format(date);
        }
        
        return value.toString();
    }

    
    private static String escapeXml(String text) {
        
        if (text == null || text.isEmpty()) {
            return "";
        }
        
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    // Helper to append an un-indented open tag
    private static void appendOpenTag(StringBuilder sb, String tag) {
        sb.append('<').append(tag).append('>');
    }

    // Helper to append an indented open tag
    private static void appendOpenTag(StringBuilder sb, String indent, String tag) {
        sb.append(indent).append('<').append(tag).append('>');
    }

    // Helper to append an un-indented close tag
    private static void appendCloseTag(StringBuilder sb, String tag) {
        sb.append("</").append(tag).append('>');
    }

    // Helper to append an indented close tag
    private static void appendCloseTag(StringBuilder sb, String indent, String tag) {
        sb.append(indent).append("</").append(tag).append('>');
    }
}

