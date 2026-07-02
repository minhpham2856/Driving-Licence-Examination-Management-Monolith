package dto.xml;

import java.util.List;
import java.util.Map;

public record XmlExportDocument(
        String rootElement,
        Map<String, Object> metadata,
        List<XmlExportTable> tables) {
}
