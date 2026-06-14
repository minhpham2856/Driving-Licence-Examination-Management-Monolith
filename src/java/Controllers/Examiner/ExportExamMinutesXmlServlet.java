package Controllers.Examiner;

import Services.ExaminerExportContext;
import Services.ExaminerExportPayload;
import Services.ExaminerExportService;
import Services.FileService;
import Services.Impl.ExaminerExportServiceImpl;
import Services.Impl.FileServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.OutputStream;

@WebServlet("/examiner/export/minutes/xml")
public class ExportExamMinutesXmlServlet extends ExaminerExportServlet {

    private final FileService fileService = new FileServiceImpl();
    private final ExaminerExportService exportService = new ExaminerExportServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        ExaminerExportContext ctx = requireExportContext(request, response);
        if (ctx == null) {
            return;
        }

        ExaminerExportPayload payload = exportService.buildMinutesExport(ctx);
        prepareXmlDownload(response, "bien-ban-thi.xml");

        OutputStream out = response.getOutputStream();
        fileService.exportToXml(payload.toXmlDocument(), out);
        flush(out);
    }
}
