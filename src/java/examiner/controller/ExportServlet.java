package examiner.controller;

import examiner.dto.ExportContextDTO;
import shared.Attributes;
import shared.enums.FileName;
import shared.enums.FileType;
import shared.enums.SectionType;
import examiner.filter.ExaminerFilter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import shared.model.ExaminerSchedule;
import examiner.service.impl.DocxServiceImpl;
import examiner.service.impl.ExcelServiceImpl;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebServlet(urlPatterns = {
    "/examiner/export/candidates",
    "/examiner/export/result",
    "/examiner/export/violations",
    "/examiner/export/audit",
    "/examiner/export/docx"
})
public class ExportServlet extends HttpServlet {

    private final DocxServiceImpl docxService = new DocxServiceImpl();
    private final ExcelServiceImpl excelService = new ExcelServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        String type = resolveDocumentType(path, request);
        if (type == null || type.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu loại tài liệu.");
            return;
        }
        ExportContextDTO ctx = requireExportContext(request, response);
        if (ctx == null) {
            return;
        }
        String searchQuery = request.getParameter("q");
        if (searchQuery == null || searchQuery.isBlank()) {
            searchQuery = request.getParameter("sbd");
        }
        FileType format = resolveFileType(request);
        OutputStream out = response.getOutputStream();
        try {
            if ("result".equalsIgnoreCase(type) && format == FileType.DOCX) {
                handleIndividualResultDocx(request, response, out, ctx);
                return;
            }
            if (format == FileType.DOCX) {
                handleDocx(request, response, out, type, ctx, searchQuery);
                return;
            }
            prepareExcelDownload(response, buildFilename(type));
            excelService.export(ctx, type, FileType.EXCEL, searchQuery, out);
        } finally {
            out.flush();
        }
    }

    private static String resolveDocumentType(String path, HttpServletRequest request) {
        if ("/examiner/export/docx".equals(path)) {
            return request.getParameter("type");
        }
        if (path == null || !path.startsWith("/examiner/export/")) {
            return null;
        }
        return path.substring("/examiner/export/".length());
    }

    private static FileType resolveFileType(HttpServletRequest request) {
        String typeParam = request.getParameter("type");
        if (typeParam != null && "docx".equalsIgnoreCase(typeParam.trim())) {
            return FileType.DOCX;
        }
        if ("/examiner/export/docx".equals(request.getServletPath())) {
            return FileType.DOCX;
        }
        return FileType.EXCEL;
    }

    private void handleIndividualResultDocx(HttpServletRequest request, HttpServletResponse response,
            OutputStream out, ExportContextDTO ctx) throws IOException {
        int sbd = parseSbd(request.getParameter("sbd"));
        if (sbd <= 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu số báo danh.");
            return;
        }
        // [tmp_minhpn] DOCX template for individual candidate result will be added later.
        prepareDocxDownload(response, FileName.RESULT.getValue() + "-sbd-" + sbd + ".docx");
        docxService.print(ctx, "result", sbd, out);
    }

    private void handleDocx(HttpServletRequest request, HttpServletResponse response, OutputStream out,
            String type, ExportContextDTO ctx, String searchQuery) throws IOException {
        if (isSessionDocumentType(type)) {
            prepareDocxDownload(response, buildFilename(type) + ".docx");
            docxService.export(ctx, type, FileType.DOCX, searchQuery, out);
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
        if (type == null) {
            return false;
        }
        return switch (type.trim().toLowerCase()) {
            case "candidates", "result", "violations", "audit" -> true;
            default -> false;
        };
    }

    private static String buildFilename(String type) {
        if (type == null) {
            return FileName.DEFAULT.getValue();
        }
        return switch (type.trim().toLowerCase()) {
            case "candidates" -> FileName.CANDIDATES.getValue();
            case "result" -> FileName.RESULTS.getValue();
            case "violations" -> FileName.VIOLATIONS.getValue();
            case "audit" -> FileName.AUDIT.getValue();
            default -> FileName.DEFAULT.getValue();
        };
    }

    private static String buildPrintFilename(String type, int sbd) {
        String base = switch (type == null ? "" : type.trim().toLowerCase()) {
            case "bb1" -> FileName.BB1.getValue();
            case "bb2" -> FileName.BB2.getValue();
            case "result" -> FileName.RESULT.getValue();
            default -> FileName.DEFAULT.getValue();
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

    private ExportContextDTO requireExportContext(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }
        Integer activeExamId = (Integer) session.getAttribute(Attributes.Examiner.ACTIVE_EXAM_ID);
        if (activeExamId == null || activeExamId <= 0) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        ExaminerSchedule schedule = (ExaminerSchedule) session.getAttribute(Attributes.Examiner.SCHEDULE);
        SectionType section = (SectionType) session.getAttribute(Attributes.Examiner.EXAM_SECTION);
        boolean isTheory = section == SectionType.THEORY;
        String sectionName = section != null ? section.getValue() : "";
        return new ExportContextDTO(activeExamId, schedule, isTheory, sectionName);
    }

    private void prepareExcelDownload(HttpServletResponse response, String filename) {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String encoded = URLEncoder.encode(filename + ".xlsx", StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + filename + ".xlsx\"; filename*=UTF-8''" + encoded);
    }

    private void prepareDocxDownload(HttpServletResponse response, String filename) {
        response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + filename + "\"; filename*=UTF-8''" + encoded);
    }
}
