package examiner.controller;

import auth.dto.UserDTO;
import examiner.dto.EnrollmentDTO;
import examiner.dto.ExportContextDTO;
import examiner.dto.PrintPreviewDTO;
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
import shared.enums.SectionType;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import shared.model.ExaminerSchedule;
import static shared.util.FormatUtil.formatPositiveInt;
import static examiner.util.FormatUtil.formatDocumentType;
import static examiner.util.FormatUtil.formatPrintAuditMessage;
import static examiner.util.FormatUtil.isCandidateResultDocument;
import static examiner.util.FormatUtil.isSessionDocumentType;
import static examiner.util.FormatUtil.resolveDocumentErrorMessage;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import examiner.service.EnrollmentService;

@WebServlet(urlPatterns = {
    "/examiner/print",
    "/examiner/print/docx"
})
// Print preview controller: renders session-wide table prints or per-candidate BB1/BB2 forms for browser printing.
public class PrintServlet extends HttpServlet {

    private final FileService excelService = new ExcelServiceImpl();
    private final FileService docxService = new DocxServiceImpl();
    private final AuditService auditService = new AuditServiceImpl();
    private final EnrollmentService enrollmentService = new EnrollmentServiceImpl();

    // Build print preview model for session tables or per-candidate forms and forward to the print JSP.
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ExportContextDTO ctx = requireExportContext(request, response);
        if (ctx == null) {
            return;
        }

        // type selects which report or BB form to render; sbd required for per-candidate forms.
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

        String searchQuery = request.getParameter("q");
        // Session tables use ExcelService preview; BB forms use DocxService preview.
        FileService fileService = isSessionTablePrint(normalizedType, sbd) ? excelService : docxService;
        try {
            PrintPreviewDTO preview = fileService.print(ctx, normalizedType, sbd, searchQuery);
            if (preview.tablePayload() != null) {
                // table.jsp reads payload and printedAt for session-wide exports.
                request.setAttribute("payload", preview.tablePayload());
                request.setAttribute("printedAt",
                        new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()));
            }
            if (preview.bbModel() != null) {
                // bb1.jsp / bb2.jsp expect flattened answer grid attributes.
                Map<String, Object> model = preview.bbModel();
                request.setAttribute("bb", model);
                request.setAttribute("answerListA", model.get("answerListA"));
                request.setAttribute("answerListB", model.get("answerListB"));
                request.setAttribute("marksA", model.get("marksA"));
                request.setAttribute("marksB", model.get("marksB"));
            }
            request.setAttribute("docTitle", preview.docTitle());
            request.setAttribute("autoPrint", Boolean.TRUE);
            request.getRequestDispatcher(preview.jspPath()).forward(request, response);
            logPrintAudit(request.getSession(false), ctx, formatPrintAuditMessage(normalizedType, sbd), sbd);
        } catch (ServletException | IOException ex) {
            if (!response.isCommitted()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        resolveDocumentErrorMessage(ex, "Không thể in tài liệu."));
            }
        }
    }

    // Return true when print preview uses session-wide table.jsp rather than BB forms.
    private static boolean isSessionTablePrint(String type, int sbd) {
        if (isCandidateResultDocument(type, sbd)) {
            return false;
        }
        return isSessionDocumentType(type);
    }

    // Record an audit log entry for the print action against the candidate or exam session.
    private void logPrintAudit(HttpSession session, ExportContextDTO ctx, String message, int sbd) {
        try {
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
        } catch (Exception ignored) {
            // Ghi audit không được chặn thao tác in.
        }
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

    // Validate session and build export context; send error response and return null when invalid.
    private ExportContextDTO requireExportContext(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }
        Integer activeExamId = (Integer) session.getAttribute(ExaminerFilter.ATTR_ACTIVE_EXAM_ID);
        if (activeExamId == null || activeExamId <= 0) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        ExaminerSchedule schedule = (ExaminerSchedule) session.getAttribute(ExaminerFilter.ATTR_EXAMINER_SCHEDULE);
        SectionType sectionType = ExaminerFilter.resolveSectionType(session);
        boolean isTheory = sectionType == SectionType.THEORY;
        return new ExportContextDTO(activeExamId, schedule, isTheory, sectionType);
    }
}
