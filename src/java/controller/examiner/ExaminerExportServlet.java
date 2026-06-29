package controller.examiner;


import enums.SectionType;

import dto.ExaminerSlotDTO;

import dto.ExaminerExportContext;
import service.ExaminerSessionContextService;
import jakarta.servlet.http.HttpServlet;
import util.ExaminerUtil;
import service.ExaminerDataService;
import service.impl.ExaminerDataServiceImpl;
import service.ExaminerActionsService;
import service.impl.ExaminerActionsServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

 // Abstract base class for all examiner report export servlets.
abstract class ExaminerExportServlet extends HttpServlet {

         // Resolves the current examination context from the HTTP session.
    protected ExaminerExportContext requireExportContext(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // Retrieve the existing HTTP session; do not create a new one
        HttpSession session = request.getSession(false);
        // Reject unauthenticated requests ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â no session means no login
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Ban can dang nhap.");
            return null;
        }

        // Read the active session ID from the cached session context attributes
        Integer activeSessionId = (Integer) session.getAttribute(ExaminerSessionContextService.ATTR_ACTIVE_SESSION_ID);
        // Reject if no active exam session is in progress
        if (activeSessionId == null || activeSessionId <= 0) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Chua co ca thi dang dien ra.");
            return null;
        }

        // Read the examiner's slot assignment from the session cache
        ExaminerSlotDTO slot = (ExaminerSlotDTO) session.getAttribute(ExaminerSessionContextService.ATTR_SLOT);
        // Default to THEORY section type if the attribute is missing or invalid
        SectionType sectionType = SectionType.THEORY;
        Object sectionTypeObj = session.getAttribute(ExaminerSessionContextService.ATTR_SECTION_TYPE);
        // Cast to SectionType enum if the attribute is of the correct type
        if (sectionTypeObj instanceof SectionType) {
            sectionType = (SectionType) sectionTypeObj;
        }
        // Read the human-readable section name (e.g. "Ly thuyet", "Thuc hanh")
        String sectionName = (String) session.getAttribute(ExaminerSessionContextService.ATTR_EXAM_SECTION_NAME);
        // Bundle all context parameters into an immutable record
        return new ExaminerExportContext(activeSessionId, slot, sectionType, sectionName);
    }

         // Sets response headers for an Excel (XLSX) download.
    protected void prepareExcelDownload(HttpServletResponse response, String filename) {
        // Set the MIME type for OOXML spreadsheet format
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        // URL-encode the filename for the Content-Disposition header (RFC 5987 filename*)
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        // Set both legacy filename and RFC-5987 filename* for broad browser compatibility
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"; filename*=UTF-8''" + encoded);
    }

         // Sets response headers for an XML download.
    protected void prepareXmlDownload(HttpServletResponse response, String filename) {
        // Set the MIME type for XML with UTF-8 encoding
        response.setContentType("application/xml; charset=UTF-8");
        // URL-encode the filename for the Content-Disposition header
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        // Set both legacy filename and RFC-5987 filename* for broad browser compatibility
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"; filename*=UTF-8''" + encoded);
    }

    // Sets response headers for a ZIP (DOCX batch) download.
    protected void prepareZipDownload(HttpServletResponse response, String filename) {
        response.setContentType("application/zip");
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"; filename*=UTF-8''" + encoded);
    }

    // Flushes the output stream.
    protected void flush(OutputStream out) throws IOException {
        out.flush();
    }
}







