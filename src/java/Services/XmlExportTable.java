package Services;

import java.util.List;

public record XmlExportTable(
        String listElement,
        String itemElement,
        List<String> fieldElements,
        List<String> headers,
        List<List<Object>> rows) {
}
