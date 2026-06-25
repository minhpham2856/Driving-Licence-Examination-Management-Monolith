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

 // Servlet that exports exam results as an XML file.
@WebServlet("/examiner/export/results/xml")
public class ExportResultsXmlServlet extends ExaminerExportServlet {

    // Service responsible for writing data to an XML output stream
    private final FileService fileService = new FileServiceImpl();
    // Service responsible for assembling the results export payload from session data
    private final ExaminerExportService exportService = new ExaminerExportServiceImpl();

    // Handles the GET request to generate and download the exam results XML file
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Validate session and extract the active exam context; abort if invalid
        ExaminerExportContext ctx = requireExportContext(request, response);
        if (ctx == null) return;

        // Build the results export payload with pass/fail data and candidate scores
        ExaminerExportPayload payload = exportService.buildResultsExport(ctx);
        // Set response headers for XML download with Vietnamese filename
        prepareXmlDownload(response, "ket-qua-thi.xml");

        // Write the XML document to the response output stream
        OutputStream out = response.getOutputStream();
        fileService.exportToXml(payload.toXmlDocument(), out);
        // Flush the output stream to ensure all data reaches the client
        flush(out);
    }
}
