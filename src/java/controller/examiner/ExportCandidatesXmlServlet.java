package controller.examiner;

import dto.ExaminerExportContext;
import enums.DocumentFormat;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.ExaminerDocumentService;
import service.impl.ExaminerDocumentServiceImpl;

import java.io.IOException;
import java.io.OutputStream;

@WebServlet("/examiner/export/candidates/xml")
public class ExportCandidatesXmlServlet extends BaseExaminerExportServlet {

    private final ExaminerDocumentService documentService = new ExaminerDocumentServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ExaminerExportContext ctx = requireExportContext(request, response);
        if (ctx == null) {
            return;
        }
        prepareXmlDownload(response, "danh-sach-thi-sinh.xml");
        OutputStream out = response.getOutputStream();
        documentService.export(ctx, "candidates", DocumentFormat.XML, null, out);
        flush(out);
    }
}
