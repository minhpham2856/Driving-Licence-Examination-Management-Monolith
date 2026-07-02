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

@WebServlet("/examiner/export/violations/xml")
public class ExportViolationsXmlServlet extends ExaminerExportServlet {

    private final FileService fileService = new FileServiceImpl();
    private final ExaminerExportService exportService = new ExaminerExportServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        ExaminerExportContext ctx = requireExportContext(request, response);
        if (ctx == null) {
            return;
        }

        ExaminerExportPayload payload = exportService.buildViolationsExport(ctx);
        prepareXmlDownload(response, "bien-ban-vi-pham.xml");

        OutputStream out = response.getOutputStream();
        fileService.exportToXml(payload.toXmlDocument(), out);
        flush(out);
    }
}
