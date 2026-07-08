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

@WebServlet("/examiner/export/violations/xml")
public class ExportViolationsXmlServlet extends BaseExaminerExportServlet {

    private final DocumentService documentService = new DocumentServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ExportContextDTO ctx = requireExportContext(request, response);
        if (ctx == null) {
            return;
        }
        prepareXmlDownload(response, DocumentName.withExtension("violations", "xml"));
        OutputStream out = response.getOutputStream();
        documentService.export(ctx, "violations", DocumentFormat.XML, null, out);
        flush(out);
    }
}