package dto;

import java.util.List;

// Immutable record representing a single table in an XML export document.
public record XmlExportTable(
        // XML wrapper element name enclosing all items (e.g. "danhSachThiSinh")
        String listElement,
        // XML element name for each row (e.g. "thiSinh")
        String itemElement,
        // Ordered child element names used as XML tags for each column value
        List<String> fieldElements,
        // Header labels used in the Excel sheet header row
        List<String> headers,
        // Data rows => each inner list contains cell values aligned with fieldElements/headers
        List<List<Object>> rows) {

}
