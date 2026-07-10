package controller.examiner;

import dto.ExportContextDTO;
import enums.DocumentFormat;
import enums.SectionType;
import filter.ExaminerFilter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.ExaminerSchedule;
import service.DocumentService;
import service.impl.DocxServiceImpl;
import service.impl.ExcelServiceImpl;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebServlet(urlPatterns = {
    "/examiner/export/candidates",
    "/examiner/export/candidates/xml",
    "/examiner/export/results",
    "/examiner/export/results/xml",
    "/examiner/export/minutes",
    "/examiner/export/minutes/xml",
    "/examiner/export/violations",
    "/examiner/export/violations/xml",
    "/examiner/export/audit",
    "/examiner/export/audit/xml",
    "/examiner/export/docx"
})
public class ExportServlet extends HttpServlet {

    private final DocxServiceImpl docxService = new DocxServiceImpl();
    private final ExcelServiceImpl excelService = new ExcelServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String[] parts = request.getServletPath().split("/");
        String type;
        DocumentFormat format;
        if (parts.length >= 5 && "xml".equals(parts[4])) {
            type = parts[3];
            format = DocumentFormat.XML;
        } else if (parts.length >= 4 && "docx".equals(parts[3])) {
            type = request.getParameter("type");
            format = DocumentFormat.DOCX;
        } else {
            type = parts.length >= 4 ? parts[3] : null;
            format = DocumentFormat.EXCEL;
        }
        if (type == null || type.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu loại tài liệu.");
            return;
        }
        ExportContextDTO ctx = requireExportContext(request, response);
        if (ctx == null) {
            return;
        }
        String searchQuery = request.getParameter("q");
        OutputStream out = response.getOutputStream();
        try {
            if (format == DocumentFormat.DOCX) {
                handleDocx(request, response, out, type, ctx, searchQuery);
            } else {
                prepareExcelOrXml(response, buildFilename(type, format), format);
                excelService.export(ctx, type, format, searchQuery, out);
            }
        } finally {
            out.flush();
        }
    }

    private void handleDocx(HttpServletRequest request, HttpServletResponse response, OutputStream out,
            String type, ExportContextDTO ctx, String searchQuery) throws IOException {
        if (isSessionDocumentType(type)) {
            prepareDocxDownload(response, buildFilename(type, DocumentFormat.DOCX));
            docxService.export(ctx, type, DocumentFormat.DOCX, searchQuery, out);
            return;
        }
        int sbd = parseSbd(request.getParameter("sbd"));
        if (sbd <= 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu số báo danh.");
            return;
        }
        prepareDocxDownload(response, buildPrintFilename(type, sbd));
        docxService.print(ctx, type, sbd, out);
    }

    private static boolean isSessionDocumentType(String type) {
        return switch (type.toLowerCase()) {
            case "candidates", "results", "minutes", "violations", "audit" ->
                true;
            default ->
                false;
        };
    }

    private static String buildFilename(String type, DocumentFormat format) {
        String base = switch (type.toLowerCase()) {
            case "candidates" ->
                "danh-sach-thi-sinh";
            case "results" ->
                "ket-qua-thi";
            case "minutes" ->
                "bien-ban-thi";
            case "violations" ->
                "vi-pham";
            case "audit" ->
                "nhat-ky";
            default ->
                "tai-lieu";
        };
        String ext = format == DocumentFormat.XML ? "xml"
                : format == DocumentFormat.DOCX ? "docx" : "xlsx";
        return base + "." + ext;
    }

    private static String buildPrintFilename(String type, int sbd) {
        String base = switch (type.toLowerCase()) {
            case "bb1" ->
                "bb1-ly-thuyet";
            case "bb2" ->
                "bb2-thuc-hanh-trong-hinh";
            case "bb3" ->
                "bb3-thuc-hanh-tren-duong";
            default ->
                "tai-lieu";
        };
        return base + "-sbd-" + sbd + ".docx";
    }

    private static int parseSbd(String raw) {
        if (raw == null || raw.isBlank()) {
            return 0;
        }
        try {
            int value = Integer.parseInt(raw.trim());
            return value > 0 ? value : 0;
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private void prepareExcelOrXml(HttpServletResponse response, String filename, DocumentFormat format) {
        if (format == DocumentFormat.XML) {
            prepareXmlDownload(response, filename);
        } else {
            prepareExcelDownload(response, filename);
        }
    }

    private ExportContextDTO requireExportContext(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }
        Integer activeSessionId = (Integer) session.getAttribute(ExaminerFilter.ATTR_ACTIVE_SESSION_ID);
        if (activeSessionId == null || activeSessionId <= 0) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        ExaminerSchedule schedule = (ExaminerSchedule) session.getAttribute(ExaminerFilter.ATTR_EXAMINER_SCHEDULE);
        SectionType section = (SectionType) session.getAttribute(ExaminerFilter.ATTR_EXAM_SECTION);
        boolean isTheory = section == SectionType.THEORY;
        String sectionName = section.getValue();
        return new ExportContextDTO(activeSessionId, schedule, isTheory, sectionName);
    }

    private void prepareExcelDownload(HttpServletResponse response, String filename) {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"; filename*=UTF-8''" + encoded);
    }

    private void prepareXmlDownload(HttpServletResponse response, String filename) {
        response.setContentType("application/xml; charset=UTF-8");
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"; filename*=UTF-8''" + encoded);
    }

    private void prepareDocxDownload(HttpServletResponse response, String filename) {
        response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"; filename*=UTF-8''" + encoded);
    }
}
