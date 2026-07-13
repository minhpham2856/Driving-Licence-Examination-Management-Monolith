package examiner.controller;

import auth.dto.UserDTO;
import examiner.dto.EnrollmentDTO;
import examiner.dto.ExportContextDTO;
import examiner.filter.ExaminerFilter;
import examiner.service.AuditService;
import examiner.service.FileService;
import examiner.service.impl.AuditServiceImpl;
import examiner.service.impl.DocxServiceImpl;
import examiner.service.impl.ExcelServiceImpl;
import examiner.service.impl.EnrollmentServiceImpl;
import shared.Attributes;
import shared.enums.AuditAction;
import shared.enums.AuditEntity;
import shared.enums.FileName;
import shared.enums.FileType;
import shared.enums.SectionType;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import shared.model.ExaminerSchedule;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import static shared.util.FormatUtil.formatPositiveInt;
import static examiner.util.FormatUtil.formatDocumentType;
import static examiner.util.FormatUtil.isCandidateResultDocument;
import static examiner.util.FormatUtil.resolveDocumentErrorMessage;
import examiner.service.EnrollmentService;

@WebServlet(urlPatterns = {
    "/examiner/export/candidates",
    "/examiner/export/result",
    "/examiner/export/violations",
    "/examiner/export/audit",
    "/examiner/export/docx"
})
// File download controller: streams Excel or DOCX exports for candidates, results, violations, audit, and per-candidate forms.
public class ExportServlet extends HttpServlet {

    private final FileService excelService = new ExcelServiceImpl();
    private final FileService docxService = new DocxServiceImpl();
    private final AuditService auditService = new AuditServiceImpl();
    private final EnrollmentService enrollmentService = new EnrollmentServiceImpl();

    // Generate the requested export file, set download headers, stream bytes, and log an audit entry.
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        String type = resolveDocumentType(path, request);
        if (type == null || type.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu loại tài liệu.");
            return;
        }

        // ctx bundles examId, schedule, section from session for service layer.
        ExportContextDTO ctx = requireExportContext(request, response);
        if (ctx == null) {
            return;
        }

        HttpSession session = request.getSession(false);
        String searchQuery = request.getParameter("q");
        FileType format = resolveFileType(request);
        int sbd = formatPositiveInt(request.getParameter("sbd"));
        String normalizedType = formatDocumentType(type.trim());

        String auditMessage = buildExportMessage(normalizedType, format, sbd);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        FileService fileService = format == FileType.DOCX ? docxService : excelService;
        try {
            // Buffer in memory so headers can be set only after successful generation.
            fileService.export(ctx, normalizedType, format, searchQuery, sbd, buffer);
        } catch (IOException ex) {
            if (!response.isCommitted()) {
                response.resetBuffer();
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        resolveDocumentErrorMessage(ex, "Không thể xuất tài liệu."));
            }
            return;
        }

        if (buffer.size() == 0) {
            if (!response.isCommitted()) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Tài liệu trống.");
            }
            return;
        }

        prepareDownloadHeaders(response, normalizedType, format, sbd);
        response.setContentLength(buffer.size());
        OutputStream out = response.getOutputStream();
        buffer.writeTo(out);
        out.flush();
        // Audit after bytes are written so failed streams are not logged as exports.
        logExportAudit(session, ctx, auditMessage, sbd);
    }

    // Set Content-Type and Content-Disposition headers for the chosen export format and filename.
    private void prepareDownloadHeaders(HttpServletResponse response, String type, FileType format, int sbd) {
        if (format == FileType.DOCX) {
            if (isPerCandidateResultExport(type, sbd)) {
                prepareDocxDownload(response, buildPrintFilename("result", sbd));
                return;
            }
            prepareDocxDownload(response, buildFilename(type) + ".docx");
            return;
        }
        prepareExcelDownload(response, buildFilename(type));
    }

    // Return true when exporting a per-candidate result DOCX for a specific SBD.
    private static boolean isPerCandidateResultExport(String type, int sbd) {
        if (type == null || sbd <= 0) {
            return false;
        }
        return isCandidateResultDocument(type, sbd);
    }

    // Record an audit log entry for the export action against the candidate or exam session.
    private void logExportAudit(HttpSession session, ExportContextDTO ctx, String message, int sbd) {
        Integer userId = resolveUserId(session);
        if (userId == null) {
            return;
        }
        if (sbd > 0) {
            EnrollmentDTO enrollment = enrollmentService.getByExamAndSbd(
                    ctx.examId(), sbd, ctx.section());
            if (enrollment != null && enrollment.getCandidateId() > 0) {
                auditService.logAction(userId, AuditAction.EXPORT, AuditEntity.CANDIDATE, message, enrollment.getCandidateId());
                return;
            }
        }
        auditService.logAction(userId, AuditAction.EXPORT, AuditEntity.EXAM_SESSION, message, ctx.examId());
    }

    // Extract the logged-in user id from session, or null when not authenticated.
    private static Integer resolveUserId(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object raw = session.getAttribute(Attributes.Session.USER);
        if (!(raw instanceof UserDTO)) {
            return null;
        }
        UserDTO user = (UserDTO) raw;
        if (user.getUserId() <= 0) {
            return null;
        }
        return user.getUserId();
    }

    // Derive the document type from the servlet path or docx endpoint query parameter.
    private static String resolveDocumentType(String path, HttpServletRequest request) {
        if ("/examiner/export/docx".equals(path)) {
            return request.getParameter("type");
        }
        if (path == null || !path.startsWith("/examiner/export/")) {
            return null;
        }
        String segment = path.substring("/examiner/export/".length());
        if ("results".equals(segment)) {
            return "result";
        }
        return segment;
    }

    // Determine output format from servlet path or type query parameter (defaults to Excel).
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

    // Map document type to the canonical download filename stem from FileName enum.
    private static String buildFilename(String type) {
        if (type == null) {
            return FileName.DEFAULT.getValue();
        }
        return switch (type.trim().toLowerCase()) {
            case "candidates" ->
                FileName.CANDIDATES.getValue();
            case "result", "results" ->
                FileName.RESULTS.getValue();
            case "violations" ->
                FileName.VIOLATIONS.getValue();
            case "audit" ->
                FileName.AUDIT.getValue();
            default ->
                FileName.DEFAULT.getValue();
        };
    }

    // Build the Vietnamese audit message describing what was exported and in which format.
    private static String buildExportMessage(String type, FileType format, int sbd) {
        String formatLabel = format == FileType.DOCX ? "DOCX" : "Excel";
        String normalized = formatDocumentType(type);
        String label = switch (normalized) {
            case "candidates" ->
                "danh sách thí sinh";
            case "result", "results" ->
                sbd > 0 ? "biên bản" : "kết quả thi";
            case "violations" ->
                "danh sách vi phạm";
            case "audit" ->
                "nhật ký";
            case "bb1" ->
                "BB1";
            case "bb2" ->
                "BB2";
            default ->
                type != null ? type : "tài liệu";
        };
        if (sbd > 0) {
            return "Xuất " + label + " SBD " + sbd + " (" + formatLabel + ")";
        }
        return "Xuất " + label + " (" + formatLabel + ")";
    }

    // Build the download filename for a per-candidate DOCX form including the SBD suffix.
    private static String buildPrintFilename(String type, int sbd) {
        String base = switch (formatDocumentType(type)) {
            case "bb1" ->
                FileName.BB1.getValue();
            case "bb2" ->
                FileName.BB2.getValue();
            case "result" ->
                FileName.RESULT.getValue();
            default ->
                FileName.DEFAULT.getValue();
        };
        return base + "-sbd-" + sbd + ".docx";
    }

    // Validate session and build export context; send error response and return null when invalid.
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
        SectionType sectionType = ExaminerFilter.resolveSectionType(session);
        boolean isTheory = sectionType == SectionType.THEORY;
        return new ExportContextDTO(activeExamId, schedule, isTheory, sectionType);
    }

    // Set response headers for an Excel (.xlsx) file download with UTF-8 encoded filename.
    private void prepareExcelDownload(HttpServletResponse response, String filename) {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String encoded = URLEncoder.encode(filename + ".xlsx", StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + filename + ".xlsx\"; filename*=UTF-8''" + encoded);
    }

    // Set response headers for a DOCX file download with UTF-8 encoded filename.
    private void prepareDocxDownload(HttpServletResponse response, String filename) {
        response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + filename + "\"; filename*=UTF-8''" + encoded);
    }
}
