package service.impl;


import service.XmlService;
import dto.xml.XmlExportDocument;
import dto.xml.XmlExportTable;
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

// Implementation of XmlService providing Excel and XML document generation using Apache POI and raw string builders
public class XmlServiceImpl implements XmlService {

    // Overloaded method to export a simple table without preamble rows to an Excel output stream
    @Override
    public void exportToExcel(String sheetName, List<String> headers, List<List<Object>> rows, OutputStream out)
            throws IOException {
        // Delegates to the main export method, passing null for the preamble rows
        exportToExcel(sheetName, null, headers, rows, out);
    }

    // Main Excel export method handling optional preamble rows, column headers, and data rows
    @Override
    public void exportToExcel(String sheetName, List<List<Object>> preambleRows, List<String> headers,
            List<List<Object>> rows, OutputStream out) throws IOException {

        // Use a try-with-resources block to ensure the POI XSSFWorkbook is closed automatically
        try (Workbook workbook = new XSSFWorkbook()) {
            // Create a new sheet, using a default name if the provided name is null or blank
            Sheet sheet = workbook.createSheet((sheetName == null || sheetName.isBlank()) ? "newSheet" : sheetName);

            // Create a cell style for the column headers to make them bold
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // Create a cell style for formatting Date objects into the standard Vietnamese dd/MM/yyyy format
            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("dd/MM/yyyy"));

            // Track the current row index as we write down the sheet
            int rowIndex = 0;
            
            // --- Write Preamble Rows ---
            if (preambleRows != null) {
                // Iterate through each preamble row definition
                for (List<Object> preambleRow : preambleRows) {
                    // Create a new row in the sheet and increment the index
                    Row row = sheet.createRow(rowIndex++);
                    // Skip if the row definition is null (acts as an empty line)
                    if (preambleRow == null) {
                        continue;
                    }
                    // Iterate through each cell value in the preamble row
                    for (int col = 0; col < preambleRow.size(); col++) {
                        // Create a cell and write the formatted value into it
                        writeCell(row.createCell(col), preambleRow.get(col), dateStyle);
                    }
                }
            }

            // Track the maximum number of columns to auto-size later
            int headerColCount = 0;
            
            // --- Write Header Row ---
            if (headers != null && !headers.isEmpty()) {
                // Create the header row
                Row headerRow = sheet.createRow(rowIndex++);
                // Set the column count based on the number of headers
                headerColCount = headers.size();
                // Iterate through each header string
                for (int col = 0; col < headers.size(); col++) {
                    Cell headerCell = headerRow.createCell(col);
                    // Set the text value
                    headerCell.setCellValue(headers.get(col));
                    // Apply the bold formatting style
                    headerCell.setCellStyle(headerStyle);
                }
            }

            // --- Write Data Rows ---
            if (rows != null) {
                // Iterate through each data row
                for (List<Object> rowData : rows) {
                    // Create a new row in the sheet
                    Row row = sheet.createRow(rowIndex++);
                    // Iterate through each cell value in the data row
                    for (int col = 0; col < rowData.size(); col++) {
                        // Create a cell and write the formatted value into it
                        writeCell(row.createCell(col), rowData.get(col), dateStyle);
                    }
                    // Update the max column count if this row has more cells than the headers
                    headerColCount = Math.max(headerColCount, rowData.size());
                }
            }

            // --- Auto-size Columns ---
            for (int col = 0; col < headerColCount; col++) {
                // Instruct POI to auto-size the column width based on content
                sheet.autoSizeColumn(col);
            }

            // Write the completed workbook to the output stream
            workbook.write(out);
        }
    }

    // Helper method to write a value into a POI cell, handling different data types appropriately
    private void writeCell(Cell cell, Object value, CellStyle dateStyle) {
        if (value == null) {
            // Set cell as blank if the value is null
            cell.setBlank();
        } else if (value instanceof Number) {
            // Write numeric values as doubles so Excel recognises them as numbers
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof Boolean) {
            // Write boolean values directly
            cell.setCellValue((Boolean) value);
        } else if (value instanceof Date) {
            // Write date values and apply the dd/MM/yyyy date style
            cell.setCellValue((Date) value);
            cell.setCellStyle(dateStyle);
        } else {
            // Fallback for all other types (Strings, enums): convert to string and write
            cell.setCellValue(value.toString());
        }
    }

    // Exports a structured XML document object model directly to the output stream
    @Override
    public void exportToXml(XmlExportDocument document, OutputStream out) throws IOException {
        // Validate that the document and its root element are provided
        if (document == null || document.rootElement() == null || document.rootElement().isBlank()) {
            throw new IllegalArgumentException("XML root element is required.");
        }

        // Use a StringBuilder with an initial capacity to avoid reallocation
        StringBuilder sb = new StringBuilder(4096);
        // Append the standard XML declaration
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        // Open the root element tag
        appendOpenTag(sb, document.rootElement());
        sb.append('\n');

        // --- Append Metadata ---
        if (document.metadata() != null && !document.metadata().isEmpty()) {
            // Write all key-value metadata elements inside the root
            appendMetadata(sb, document.metadata(), "  ");
        }

        // --- Append Tables ---
        if (document.tables() != null) {
            // Iterate through each table definition
            for (XmlExportTable table : document.tables()) {
                // Write the table elements
                appendTable(sb, table, "  ");
            }
        }

        // Close the root element tag
        appendCloseTag(sb, document.rootElement());
        sb.append('\n');
        // Convert the assembled string to UTF-8 bytes and write to the output stream
        out.write(sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    // Recursively appends metadata elements from a Map, handling nested Maps
    private void appendMetadata(StringBuilder sb, Map<String, Object> metadata, String indent) {
        // Iterate through each map entry
        for (Map.Entry<String, Object> entry : metadata.entrySet()) {
            // Append the individual value or recurse if it's a nested map
            appendValueElement(sb, entry.getKey(), entry.getValue(), indent);
        }
    }

    // Appends a single XML element, handling basic values and nested Maps
    private void appendValueElement(StringBuilder sb, String elementName, Object value, String indent) {
        // If the value is a nested map, create a parent tag and recurse
        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> nestedMap = (Map<String, Object>) value;
            // Open the parent tag
            appendOpenTag(sb, indent, elementName);
            sb.append('\n');
            // Recurse into the nested map with increased indentation
            appendMetadata(sb, nestedMap, indent + "  ");
            // Close the parent tag
            appendCloseTag(sb, indent, elementName);
            sb.append('\n');
            return;
        }

        // Handle primitive values: open tag, escaped string content, close tag
        sb.append(indent).append('<').append(elementName).append('>');
        sb.append(escapeXml(formatXmlValue(value)));
        appendCloseTag(sb, elementName);
        sb.append('\n');
    }

    // Appends a structured XML table containing list elements, item elements, and field elements
    private void appendTable(StringBuilder sb, XmlExportTable table, String indent) {
        // Validate that the table and its list element name are provided
        if (table == null || table.listElement() == null || table.listElement().isBlank()) {
            return;
        }

        // Open the parent list tag (e.g. <thiSinhList>)
        appendOpenTag(sb, indent, table.listElement());
        sb.append('\n');

        // Prepare indentation strings for nested elements
        String itemIndent = indent + "  ";
        String fieldIndent = itemIndent + "  ";
        
        // Extract the field names and data rows from the table model
        List<String> fields = table.fieldElements();
        List<List<Object>> rows = table.rows();

        if (rows != null) {
            // Iterate through each data row
            for (List<Object> row : rows) {
                // Open the item tag (e.g. <thiSinh>)
                appendOpenTag(sb, itemIndent, table.itemElement());
                sb.append('\n');
                
                // Write the field elements if both fields and row data exist
                if (fields != null && row != null) {
                    // Determine how many columns to process (minimum of defined fields and available data)
                    int count = Math.min(fields.size(), row.size());
                    for (int i = 0; i < count; i++) {
                        // Append each field as a child element
                        appendValueElement(sb, fields.get(i), row.get(i), fieldIndent);
                    }
                }
                
                // Close the item tag
                appendCloseTag(sb, itemIndent, table.itemElement());
                sb.append('\n');
            }
        }

        // Close the parent list tag
        appendCloseTag(sb, indent, table.listElement());
        sb.append('\n');
    }

    // Formats Java objects into strings suitable for XML text nodes
    private static String formatXmlValue(Object value) {
        // Return an empty string for null values
        if (value == null) {
            return "";
        }
        // Format Date objects using the standard Vietnamese dd/MM/yyyy pattern
        if (value instanceof Date) {
            Date date = (Date) value;
            return new java.text.SimpleDateFormat("dd/MM/yyyy").format(date);
        }
        // Fallback to the object's string representation
        return value.toString();
    }

    // Escapes special characters to ensure valid XML text nodes
    private static String escapeXml(String text) {
        // Return an empty string if the text is null or empty
        if (text == null || text.isEmpty()) {
            return "";
        }
        // Replace XML reserved characters with their corresponding entities
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
