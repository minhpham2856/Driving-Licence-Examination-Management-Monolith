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

@WebServlet("/examiner/export/violations")
public class ExportViolationsExcelServlet extends BaseExaminerExportServlet {

    private final ExaminerDocumentService documentService = new ExaminerDocumentServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ExaminerExportContext ctx = requireExportContext(request, response);
        if (ctx == null) {
            return;
        }
        prepareExcelDownload(response, ExaminerExportFilenames.withExtension("violations", "xlsx"));
        OutputStream out = response.getOutputStream();
        documentService.export(ctx, "violations", DocumentFormat.EXCEL, null, out);
        flush(out);
    }
}