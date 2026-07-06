package controller.examiner;

import dto.ExaminerExportContext;
import enums.DocumentFormat;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.ExaminerDocumentService;
import service.impl.ExaminerDocumentServiceImpl;
import util.ExaminerExportFilenames;

import java.io.IOException;
import java.io.OutputStream;

@WebServlet("/examiner/export/violations/xml")
public class ExportViolationsXmlServlet extends BaseExaminerExportServlet {

    private final ExaminerDocumentService documentService = new ExaminerDocumentServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ExaminerExportContext ctx = requireExportContext(request, response);
        if (ctx == null) {
            return;
        }
        prepareXmlDownload(response, ExaminerExportFilenames.withExtension("violations", "xml"));
        OutputStream out = response.getOutputStream();
        documentService.export(ctx, "violations", DocumentFormat.XML, null, out);
        flush(out);
    }
}