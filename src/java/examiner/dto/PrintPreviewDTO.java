package examiner.dto;

import java.util.Map;

// Print preview data for JSP forward from PrintServlet.
public record PrintPreviewDTO(
        String jspPath,
        ExportPayloadDTO tablePayload,
        Map<String, Object> bbModel,
        String docTitle) {
}
