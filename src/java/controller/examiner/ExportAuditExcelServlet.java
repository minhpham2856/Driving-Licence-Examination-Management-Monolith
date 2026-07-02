package controller.examiner;

import dto.examiner.ExaminerExportContext;
import dto.examiner.ExaminerExportPayload;
import service.ExaminerExportService;
import service.FileService;
import service.impl.ExaminerExportServiceImpl;
import service.impl.FileServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.OutputStream;

@WebServlet("/examiner/export/audit")
public class ExportAuditExcelServlet extends ExaminerExportServlet {

    private final FileService fileService = new FileServiceImpl();
    private final ExaminerExportService exportService = new ExaminerExportServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        ExaminerExportContext ctx = requireExportContext(request, response);
        if (ctx == null) {
            return;
        }

        ExaminerExportPayload payload = exportService.buildAuditExport(ctx, request.getParameter("q"));
        prepareExcelDownload(response, "nhat-ky.xlsx");

        OutputStream out = response.getOutputStream();
        fileService.exportToExcel(payload.excelSheetName(), payload.primaryHeaders(), payload.primaryRows(), out);
        flush(out);
    }
}
