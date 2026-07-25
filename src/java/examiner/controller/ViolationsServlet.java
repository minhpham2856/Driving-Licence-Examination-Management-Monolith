package examiner.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.Part;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import examiner.filter.ExaminerFilter;
import auth.dto.UserDTO;
import shared.Attributes;
import shared.enums.SectionType;
import examiner.service.ActionService;
import examiner.service.ExamViewService;
import examiner.dto.ServiceResult;
import examiner.service.impl.ActionServiceImpl;
import examiner.service.impl.ExamViewServiceImpl;
import examiner.util.RequestUtil;
import static shared.util.FormatUtil.formatPositiveInt;
import java.io.IOException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import shared.storage.CloudinaryDocumentStorage;

@WebServlet(urlPatterns = {"/examiner/violations"})
@MultipartConfig(maxFileSize = 5 * 1024 * 1024, maxRequestSize = 6 * 1024 * 1024)
// Violations page: lists candidates and supports one-click suspend or undo-suspend actions.
public class ViolationsServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ViolationsServlet.class.getName());
    private static final long MAX_EVIDENCE_BYTES = 5L * 1024L * 1024L;

    private final ExamViewService viewService = new ExamViewServiceImpl();
    private final ActionService actionService = new ActionServiceImpl();

    // Serve the violations list with searchable candidate rows for the active exam session.
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        Integer activeExamId = (Integer) session.getAttribute(Attributes.Examiner.ACTIVE_EXAM_ID);
        String mode = request.getParameter("mode");
        int selectedSbd = formatPositiveInt(request.getParameter("sbd"));
        if (!"create".equals(mode) || selectedSbd <= 0) {
            response.sendRedirect(request.getContextPath() + "/examiner/action");
            return;
        }
        if (activeExamId != null && activeExamId > 0) {
            SectionType sectionType = ExaminerFilter.resolveSectionType(session);
            request.setAttribute(Attributes.Request.CANDIDATE, viewService.getCandidateViewRow(
                    activeExamId, selectedSbd, sectionType));
            request.setAttribute(Attributes.Examiner.VIOLATION_REASONS,
                    viewService.getViolationViewByExam(activeExamId, selectedSbd, sectionType)
                            .get(Attributes.Examiner.VIOLATION_REASONS));
        }
        request.getRequestDispatcher("/views/examiner/violations.jsp").forward(request, response);
    }

    // Suspend or undo-suspend the selected candidate and redirect with outcome flash params.
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        Integer activeExamId = (Integer) session.getAttribute(Attributes.Examiner.ACTIVE_EXAM_ID);
        if (activeExamId == null || activeExamId <= 0) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        int sbd = formatPositiveInt(request.getParameter("sbd"));
        if (sbd <= 0) {
            response.sendRedirect(request.getContextPath() + "/examiner/action?error=noSbd");
            return;
        }
        UserDTO user = (UserDTO) session.getAttribute(Attributes.Session.USER);
        Integer userId = user != null ? user.getUserId() : null;
        SectionType sectionType = ExaminerFilter.resolveSectionType(session);
        String action = request.getParameter("action");
        String from = request.getParameter("from");
        String actionHome = "/examiner/action";
        // undoSuspend clears Candidate.IsSuspended; createViolation records evidence + suspend.
        if ("undoSuspend".equals(action)) {
            ServiceResult<Void> result = actionService.undoSuspension(activeExamId, sbd, userId, sectionType);
            String redirect = result.isSuccess()
                    ? actionHome + "?unsuspended=" + RequestUtil.urlEncode(sbd)
                    : actionHome + "?sbd=" + RequestUtil.urlEncode(sbd) + "&error=unsuspendFailed";
            response.sendRedirect(request.getContextPath() + redirect);
            return;
        }
        if ("createViolation".equals(action)) {
            if (user == null || !actionService.verifyPassword(user.toUser(), request.getParameter("confirmPassword"))) {
                redirectCreate(request, response, sbd, "passwordIncorrect", null);
                return;
            }
            Part evidence;
            try {
                evidence = request.getPart("evidenceFile");
            } catch (IllegalStateException ex) {
                redirectCreate(request, response, sbd, "evidenceTooLarge", null);
                return;
            }
            if (!isValidEvidence(evidence)) {
                redirectCreate(request, response, sbd, "evidenceInvalid", null);
                return;
            }
            if (!CloudinaryDocumentStorage.isConfigured()) {
                redirectCreate(request, response, sbd, "evidenceCloudinaryMissing", null);
                return;
            }
            String storedRef;
            try {
                storedRef = CloudinaryDocumentStorage.upload(
                        evidence, sbd, "exam-violation", extensionOf(evidence.getSubmittedFileName()));
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "Could not upload violation evidence for SBD " + sbd, ex);
                redirectCreate(request, response, sbd, "evidenceUploadFailed", safeUploadMessage(ex));
                return;
            }
            ServiceResult<Void> result = actionService.recordViolation(activeExamId, sbd, userId,
                    request.getParameter("reasonCode"), request.getParameter("reasonDetail"),
                    storedRef, sectionType);
            if (!result.isSuccess()) {
                try { CloudinaryDocumentStorage.destroy(storedRef); } catch (IOException ignored) {}
            }
            String redirect = result.isSuccess()
                    ? actionHome + "?suspended=" + RequestUtil.urlEncode(sbd)
                    : "/examiner/violations?sbd=" + RequestUtil.urlEncode(sbd)
                        + "&mode=create&error=violationFailed"
                        + (from != null ? "&from=" + RequestUtil.urlEncode(from) : "");
            response.sendRedirect(request.getContextPath() + redirect);
            return;
        }
        // Default one-click suspend is disabled; require reason + evidence form.
        response.sendRedirect(request.getContextPath()
                + "/examiner/violations?sbd=" + RequestUtil.urlEncode(sbd) + "&mode=create&from=action");
    }

    private void redirectCreate(HttpServletRequest request, HttpServletResponse response, int sbd,
            String error, String uploadMessage) throws IOException {
        String redirect = request.getContextPath()
                + "/examiner/violations?sbd=" + RequestUtil.urlEncode(sbd)
                + "&mode=create&error=" + RequestUtil.urlEncode(error);
        if (uploadMessage != null && !uploadMessage.isBlank()) {
            redirect += "&uploadMessage=" + RequestUtil.urlEncode(uploadMessage);
        }
        response.sendRedirect(redirect);
    }

    private boolean isValidEvidence(Part part) {
        if (part == null || part.getSize() <= 0 || part.getSize() > MAX_EVIDENCE_BYTES) {
            return false;
        }
        String type = part.getContentType();
        String ext = extensionOf(part.getSubmittedFileName());
        boolean allowedType = "image/jpeg".equals(type) || "image/png".equals(type) || "image/webp".equals(type);
        boolean allowedExtension = "jpg".equals(ext) || "jpeg".equals(ext) || "png".equals(ext) || "webp".equals(ext);
        return allowedType && allowedExtension;
    }

    private String extensionOf(String fileName) {
        int dot = fileName != null ? fileName.lastIndexOf('.') : -1;
        return dot >= 0 ? fileName.substring(dot + 1).toLowerCase(Locale.ROOT) : "jpg";
    }

    private String safeUploadMessage(IOException ex) {
        String message = ex != null ? ex.getMessage() : null;
        if (message == null || message.isBlank()) {
            return "Không nhận được phản hồi từ Cloudinary.";
        }
        String cleaned = message.replaceAll("[\\r\\n\\t]+", " ").trim();
        return cleaned.length() > 180 ? cleaned.substring(0, 180) + "..." : cleaned;
    }
}
