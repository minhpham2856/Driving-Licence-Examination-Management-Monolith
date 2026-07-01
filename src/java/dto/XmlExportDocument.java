package dto;
import java.util.List;
import java.util.Map;
 // Immutable record representing a complete XML export document model.
public record XmlExportDocument(
        // Root XML element name wrapping the entire document (e.g. "danhSachThiSinh")
        String rootElement,
        // Key-value metadata pairs written as child elements under the root
        Map<String, Object> metadata,
        // Ordered list of data tables — each rendered as a separate XML section
        List<XmlExportTable> tables) {
}
