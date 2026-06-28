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

 // Servlet that exports violations (bien ban vi pham) as an Excel (XLSX) file.
@WebServlet("/examiner/export/violations")
public class ExportViolationsExcelServlet extends ExaminerExportServlet {

    // Service responsible for writing data to an Excel workbook output stream
    private final XmlService fileService = new XmlServiceImpl();
    // Service responsible for assembling the violations export payload (audit + score deductions)
    private final ExaminerExportService exportService = new ExaminerExportServiceImpl();

    // Handles the GET request to generate and download the violations Excel file
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Validate session and extract the active exam context; abort if invalid
        ExaminerExportContext ctx = requireExportContext(request, response);
        if (ctx == null) return;

        // Build the violations export payload containing both audit violations and score deductions
        ExaminerExportPayload payload = exportService.buildViolationsExport(ctx);
        // Set response headers for Excel XLSX download with Vietnamese filename
        prepareExcelDownload(response, "bien-ban-vi-pham.xlsx");

        // Write the Excel workbook to the response output stream
        OutputStream out = response.getOutputStream();
        fileService.exportToExcel(payload.excelSheetName(), payload.primaryHeaders(), payload.primaryRows(), out);
        // Flush the output stream to ensure all data reaches the client
        flush(out);
    }
}




