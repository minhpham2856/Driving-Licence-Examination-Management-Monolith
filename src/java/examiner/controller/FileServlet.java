package examiner.controller;

import auth.dto.UserDTO;
import examiner.dto.EnrollmentDTO;
import examiner.dto.ExportContextDTO;
import examiner.dto.PrintPreviewDTO;
import examiner.filter.ExaminerFilter;
import examiner.service.AuditService;
import examiner.service.EnrollmentService;
import examiner.service.FileService;
import examiner.service.ProgressService;
import examiner.service.impl.AuditServiceImpl;
import examiner.service.impl.EnrollmentServiceImpl;
import examiner.service.impl.FileServiceImpl;
import examiner.service.impl.ProgressServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import shared.Attributes;
import shared.enums.AuditAction;
import shared.enums.AuditEntity;
import shared.enums.CandidateStatus;
import shared.enums.FileName;
import shared.enums.FileType;
import shared.enums.SectionType;
import shared.model.ExaminerSchedule;

import static examiner.util.FormatUtil.formatDocumentType;
import static examiner.util.FormatUtil.formatPrintAuditMessage;
import static examiner.util.FormatUtil.isCandidateResultDocument;
import static examiner.util.FormatUtil.isSessionDocumentType;
import static examiner.util.FormatUtil.resolveDocumentErrorMessage;
import static shared.util.FormatUtil.formatPositiveInt;

@WebServlet(urlPatterns = {
    "/examiner/print",
    "/examiner/export/candidates",
    "/examiner/export/result",
    "/examiner/export/violations",
    "/examiner/export/audit"
})
public class FileServlet extends HttpServlet {

    private static final String EXPORT_PREFIX = "/examiner/export/";

    private final FileService fileService = new FileServiceImpl();
    private final AuditService auditService = new AuditServiceImpl();
    private final EnrollmentService enrollmentService = new EnrollmentServiceImpl();
    private final ProgressService progressService = new ProgressServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        if ("/examiner/print".equals(path)) {
            handlePrint(request, response);
            return;
        }
        if (path != null && path.startsWith(EXPORT_PREFIX)) {
            handleExcelExport(request, response);
            return;
        }
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    private void handleExcelExport(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String type = resolveExportType(request.getServletPath());
        if (type == null || type.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu loại tài liệu.");
            return;
        }

        ExportContextDTO ctx = requireExportContext(request, response);
        if (ctx == null) {
            return;
        }

        int sbd = formatPositiveInt(request.getParameter("sbd"));
        String normalizedType = formatDocumentType(type.trim());
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            fileService.export(ctx, normalizedType, FileType.EXCEL, request.getParameter("q"), sbd, buffer);
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

        prepareExcelDownload(response, buildFilename(normalizedType));
        response.setContentLength(buffer.size());
        OutputStream out = response.getOutputStream();
        buffer.writeTo(out);
        out.flush();
        logAudit(request.getSession(false), ctx, AuditAction.EXPORT,
                buildExcelAuditMessage(normalizedType, sbd), sbd);
    }

    private void handlePrint(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ExportContextDTO ctx = requireExportContext(request, response);
        if (ctx == null) {
            return;
        }

        String type = request.getParameter("type");
        if (type == null || type.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu loại tài liệu.");
            return;
        }

        String normalizedType = formatDocumentType(type.trim());
        int sbd = formatPositiveInt(request.getParameter("sbd"));
        if (!isSessionTablePrint(normalizedType, sbd) && sbd <= 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu số báo danh.");
            return;
        }

        try {
            PrintPreviewDTO preview = fileService.print(ctx, normalizedType, sbd, request.getParameter("q"));
            bindPrintPreview(request, preview);
            syncResultPrintedFlag(ctx, normalizedType, sbd);
            request.setAttribute(Attributes.Examiner.Print.DOC_TITLE, preview.docTitle());
            request.setAttribute(Attributes.Examiner.Print.AUTO_PRINT, Boolean.TRUE);
            request.getRequestDispatcher(preview.jspPath()).forward(request, response);
            logAudit(request.getSession(false), ctx, AuditAction.EXPORT,
                    formatPrintAuditMessage(normalizedType, sbd), sbd);
        } catch (ServletException | IOException ex) {
            if (!response.isCommitted()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        resolveDocumentErrorMessage(ex, "Không thể in tài liệu."));
            }
        }
    }

    private static void bindPrintPreview(HttpServletRequest request, PrintPreviewDTO preview) {
        if (preview.tablePayload() != null) {
            request.setAttribute(Attributes.Examiner.Print.PAYLOAD, preview.tablePayload());
            request.setAttribute(Attributes.Examiner.Print.PRINTED_AT,
                    new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()));
        }
        if (preview.printModel() != null) {
            Map<String, Object> model = preview.printModel();
            request.setAttribute(Attributes.Examiner.Print.MODEL, model);
            request.setAttribute(Attributes.Examiner.Print.ANSWER_LIST_A, model.get(Attributes.Examiner.Print.ANSWER_LIST_A));
            request.setAttribute(Attributes.Examiner.Print.ANSWER_LIST_B, model.get(Attributes.Examiner.Print.ANSWER_LIST_B));
            request.setAttribute(Attributes.Examiner.Print.MARKS_A, model.get(Attributes.Examiner.Print.MARKS_A));
            request.setAttribute(Attributes.Examiner.Print.MARKS_B, model.get(Attributes.Examiner.Print.MARKS_B));
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
        SectionType sectionType = ExaminerFilter.resolveSectionType(session);
        return new ExportContextDTO(activeExamId, schedule, sectionType == SectionType.THEORY, sectionType,
                request.getContextPath());
    }

    private void syncResultPrintedFlag(ExportContextDTO ctx, String type, int sbd) {
        if ("violation".equals(formatDocumentType(type))
                || !isCandidateResultDocument(type, sbd) || sbd <= 0 || ctx == null || ctx.section() == null) {
            return;
        }
        EnrollmentDTO enrollment = enrollmentService.getByExamAndSbd(ctx.examId(), sbd, ctx.section());
        if (enrollment == null || enrollment.getExamEnrollmentId() <= 0
                || enrollment.getSectionStatus() != CandidateStatus.AWAITING_SIGNATURE) {
            return;
        }
        progressService.markResultPrinted(enrollment.getExamEnrollmentId(), ctx.section());
    }

    private void logAudit(HttpSession session, ExportContextDTO ctx, AuditAction action, String message, int sbd) {
        try {
            Integer userId = resolveUserId(session);
            if (userId == null) {
                return;
            }
            if (sbd > 0) {
                EnrollmentDTO enrollment = enrollmentService.getByExamAndSbd(ctx.examId(), sbd, ctx.section());
                if (enrollment != null && enrollment.getCandidateId() > 0) {
                    auditService.logAction(userId, action, AuditEntity.CANDIDATE, message, enrollment.getCandidateId());
                    return;
                }
            }
            auditService.logAction(userId, action, AuditEntity.EXAM_SESSION, message, ctx.examId());
        } catch (Exception ignored) {
            // Audit lỗi không được chặn thao tác in/xuất file.
        }
    }

    private static boolean isSessionTablePrint(String type, int sbd) {
        if (isCandidateResultDocument(type, sbd)) {
            return false;
        }
        return isSessionDocumentType(type);
    }

    private static String resolveExportType(String path) {
        if (path == null || !path.startsWith(EXPORT_PREFIX)) {
            return null;
        }
        String segment = path.substring(EXPORT_PREFIX.length());
        return "results".equals(segment) ? "result" : segment;
    }

    private static String buildFilename(String type) {
        if (type == null) {
            return FileName.DEFAULT.getValue();
        }
        return switch (type.trim().toLowerCase()) {
            case "candidates" -> FileName.CANDIDATES.getValue();
            case "result", "results" -> FileName.RESULTS.getValue();
            case "violations" -> FileName.VIOLATIONS.getValue();
            case "audit" -> FileName.AUDIT.getValue();
            default -> FileName.DEFAULT.getValue();
        };
    }

    private static String buildExcelAuditMessage(String type, int sbd) {
        String label = switch (formatDocumentType(type)) {
            case "candidates" -> "danh sách thí sinh";
            case "result", "results" -> sbd > 0 ? "biên bản" : "kết quả thi";
            case "violations" -> "danh sách vi phạm";
            case "audit" -> "nhật ký";
            default -> type != null ? type : "tài liệu";
        };
        if (sbd > 0) {
            return "Xuất " + label + " SBD " + sbd + " (Excel)";
        }
        return "Xuất " + label + " (Excel)";
    }

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

    private static void prepareExcelDownload(HttpServletResponse response, String filename) {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String encoded = URLEncoder.encode(filename + ".xlsx", StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + filename + ".xlsx\"; filename*=UTF-8''" + encoded);
    }
}
