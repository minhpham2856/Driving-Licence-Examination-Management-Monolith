package examiner.dto;

import java.util.List;
import java.util.Map;

// Immutable record holding all data required for an examiner export operation.
public record ExportPayloadDTO(
        // Name displayed on the Excel worksheet tab (e.g. "Danh sach thi sinh")
        String excelSheetName,
        // Root element name in the generated XML document (e.g. "danhSachThiSinh")
        String xmlRootElement,
        // Key-value pairs placed in the metadata/header section of the export
        Map<String, Object> metadata,
        // Ordered list of data tables - each table has its own headers and rows
        List<XmlExportTable> tables,
        // Optional rows prepended before the main data table in Excel (null if none)
        List<List<Object>> excelPreambleRows) {
    // Returns the headers from the first (primary) table, or an empty list if no tables exist.

    public List<String> primaryHeaders() {
        // Return empty list if no tables are present to prevent index-out-of-bounds
        return tables.isEmpty() ? List.of() : tables.get(0).headers();
    }
    // Returns the data rows from the first (primary) table, or an empty list if no tables exist.

    public List<List<Object>> primaryRows() {
        // Return empty list if no tables are present to prevent index-out-of-bounds
        return tables.isEmpty() ? List.of() : tables.get(0).rows();
    }
    // Converts this payload into an {@link XmlExportDocument} for XML serialisation.

    public XmlExportDocument toXmlDocument() {
        return new XmlExportDocument(xmlRootElement, metadata, tables);
    }
}
