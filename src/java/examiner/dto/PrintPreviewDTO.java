package examiner.dto;

import java.util.Map;

// Print preview data for JSP forward from the examiner file controller.
public record PrintPreviewDTO(
        String jspPath,
        ExportPayloadDTO tablePayload,
        Map<String, Object> printModel,
        String docTitle) {
}
