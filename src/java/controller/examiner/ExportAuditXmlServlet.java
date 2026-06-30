package controller.examiner;

import dto.ExaminerExportContext;
import dto.ExaminerExportPayload;
import service.ExaminerExportService;
import service.XmlService;
import service.impl.ExaminerExportServiceImpl;

import service.impl.XmlServiceImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.OutputStream;

@WebServlet("/examiner/export/audit/xml")
public class ExportAuditXmlServlet extends BaseExaminerExportServlet {

    private final XmlService fileService = new XmlServiceImpl();

    private final ExaminerExportService exportService = new ExaminerExportServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        ExaminerExportContext ctx = requireExportContext(request, response);
        if (ctx == null) {
            return;
        }

        ExaminerExportPayload payload = exportService.buildAuditExport(ctx, request.getParameter("q"));

        prepareXmlDownload(response, "audit.xml");

        OutputStream out = response.getOutputStream();
        fileService.exportToXml(payload.toXmlDocument(), out);

        flush(out);
    }
}
