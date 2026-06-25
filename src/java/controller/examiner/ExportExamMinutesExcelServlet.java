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

 // Servlet that exports exam minutes (bien ban) as an Excel (XLSX) file.
@WebServlet("/examiner/export/minutes")
public class ExportExamMinutesExcelServlet extends ExaminerExportServlet {

    // Service responsible for writing data to an Excel workbook output stream
    private final XmlService fileService = new XmlServiceImpl();
    // Service responsible for assembling the minutes export payload with metadata and preamble
    private final ExaminerExportService exportService = new ExaminerExportServiceImpl();

    // Handles the GET request to generate and download the exam minutes Excel file
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Validate session and extract the active exam context; abort if invalid
        ExaminerExportContext ctx = requireExportContext(request, response);
        if (ctx == null) return;

        // Build the minutes export payload including metadata, statistics, and candidate rows
        ExaminerExportPayload payload = exportService.buildMinutesExport(ctx);
        // Set response headers for Excel XLSX download with Vietnamese filename
        prepareExcelDownload(response, "bien-ban-thi.xlsx");

        // Write the Excel workbook to the response output stream
        OutputStream out = response.getOutputStream();
        fileService.exportToExcel(payload.excelSheetName(), payload.primaryHeaders(), payload.primaryRows(), out);
        // Flush the output stream to ensure all data reaches the client
        flush(out);
    }
}

