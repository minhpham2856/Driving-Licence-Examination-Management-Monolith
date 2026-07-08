package controller.examiner;

import dto.ExportContextDTO;
import enums.DocumentFormat;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.DocumentService;
import service.impl.DocumentServiceImpl;
import enums.DocumentName;

import java.io.IOException;
import java.io.OutputStream;

@WebServlet("/examiner/print/docx")
public class ExaminerPrintDocxServlet extends BaseExaminerExportServlet {

    private final DocumentService documentService = new DocumentServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ExportContextDTO ctx = requireExportContext(request, response);
        if (ctx == null) {
            return;
        }
        String documentType = request.getParameter("type");
        if (documentType == null || documentType.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu tham số type.");
            return;
        }
        String normalizedType = documentType.trim().toLowerCase();
        OutputStream out = response.getOutputStream();
        if (isSessionDocumentType(normalizedType)) {
            String filename = DocumentName.withExtension(documentType, "docx");
            prepareDocxInline(response, filename);
            documentService.export(ctx, documentType, DocumentFormat.DOCX, request.getParameter("q"), out);
            flush(out);
            return;
        }
        Integer sbd = parseSbd(request.getParameter("sbd"));
        if (sbd == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu hoặc sai tham số sbd.");
            return;
        }
        String filename = DocumentName.printCandidate(documentType, sbd);
        prepareDocxInline(response, filename);
        documentService.print(ctx, documentType, sbd, out);
        flush(out);
    }

    private static boolean isSessionDocumentType(String normalizedType) {
        return "candidates".equals(normalizedType)
                || "results".equals(normalizedType)
                || "minutes".equals(normalizedType)
                || "violations".equals(normalizedType)
                || "audit".equals(normalizedType);
    }

    private static Integer parseSbd(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            int value = Integer.parseInt(raw.trim());
            return value > 0 ? value : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
