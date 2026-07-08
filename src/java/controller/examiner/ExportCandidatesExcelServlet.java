package controller.examiner;

import dto.ExportContextDTO;
import enums.DocumentFormat;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.DocumentService;
import service.impl.DocumentServiceImpl;

import java.io.IOException;
import java.io.OutputStream;

@WebServlet("/examiner/export/candidates")
public class ExportCandidatesExcelServlet extends BaseExaminerExportServlet {

    private final DocumentService documentService = new DocumentServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ExportContextDTO ctx = requireExportContext(request, response);
        if (ctx == null) {
            return;
        }
        prepareExcelDownload(response, "danh-sach-thi-sinh.xlsx");
        OutputStream out = response.getOutputStream();
        documentService.export(ctx, "candidates", DocumentFormat.EXCEL, null, out);
        flush(out);
    }
}
