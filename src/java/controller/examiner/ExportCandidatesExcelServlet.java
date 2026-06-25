package controller.examiner;


import dto.examiner.ExaminerExportContext;
import dto.examiner.ExaminerExportPayload;
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

 // Servlet that exports the candidate list as an Excel (XLSX) file.
@WebServlet("/examiner/export/candidates")
public class ExportCandidatesExcelServlet extends ExaminerExportServlet {

    // Service responsible for writing data to an Excel workbook output stream
    private final XmlService fileService = new XmlServiceImpl();
    // Service responsible for assembling the candidate list export payload
    private final ExaminerExportService exportService = new ExaminerExportServiceImpl();

    // Handles the GET request to generate and download the candidate list Excel file
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Validate session and extract the active exam context; abort if invalid
        ExaminerExportContext ctx = requireExportContext(request, response);
        if (ctx == null) return;

        // Build the candidate list export payload from session data
        ExaminerExportPayload payload = exportService.buildCandidatesExport(ctx);
        // Set response headers for Excel XLSX download with Vietnamese filename
        prepareExcelDownload(response, "danh-sach-thi-sinh.xlsx");

        // Write the Excel workbook to the response output stream
        OutputStream out = response.getOutputStream();
        fileService.exportToExcel(payload.excelSheetName(), payload.primaryHeaders(), payload.primaryRows(), out);
        // Flush the output stream to ensure all data reaches the client
        flush(out);
    }
}

