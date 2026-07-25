package examiner.dto;

import java.util.List;
import java.util.Map;

// Immutable record holding all data required for an examiner export operation.
public record ExportPayloadDTO(
        String excelSheetName,
        String xmlRootElement,
        Map<String, Object> metadata,
        List<XmlExportTable> tables,
        List<List<Object>> excelPreambleRows) {
}
