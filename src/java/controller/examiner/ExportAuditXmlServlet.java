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

@WebServlet("/examiner/export/audit/xml")
public class ExportAuditXmlServlet extends ExaminerExportServlet {

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
        prepareXmlDownload(response, "nhat-ky.xml");

        OutputStream out = response.getOutputStream();
        fileService.exportToXml(payload.toXmlDocument(), out);
        flush(out);
    }
}
