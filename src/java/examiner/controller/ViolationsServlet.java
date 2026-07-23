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
import examiner.dto.CandidateRowDTO;
import examiner.dto.ServiceResult;
import examiner.service.impl.ActionServiceImpl;
import examiner.service.impl.ExamViewServiceImpl;
import examiner.util.ListUtil;
import static shared.util.FormatUtil.formatPositiveInt;
import static shared.util.FormatUtil.formatString;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import shared.storage.CloudinaryDocumentStorage;

@WebServlet(urlPatterns = {"/examiner/violations"})
@MultipartConfig(maxFileSize = 5 * 1024 * 1024, maxRequestSize = 6 * 1024 * 1024)
// Violations page: lists candidates and supports one-click suspend or undo-suspend actions.
public class ViolationsServlet extends HttpServlet {

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
        Integer activeExamId = (Integer) session.getAttribute(ExaminerFilter.ATTR_ACTIVE_EXAM_ID);
        if (activeExamId != null && activeExamId > 0) {
            SectionType sectionType = ExaminerFilter.resolveSectionType(session);
            // Search filter is applied in-memory after DB load on violations.jsp.
            String search = ListUtil.normalizeSearch(request.getParameter("q"));
            List<CandidateRowDTO> candidates = viewService.getAllFilteredByExam(
                    activeExamId, sectionType, formatString(search));
            ListUtil.applySortAndSearch(request, candidates);
            request.setAttribute("candidates", candidates);
            request.setAttribute("candidateQueue", candidates);
            int selectedSbd = formatPositiveInt(request.getParameter("sbd"));
            if (selectedSbd > 0) {
                request.setAttribute("candidate", viewService.getCandidateViewRow(
                        activeExamId, selectedSbd, sectionType));
                request.setAttribute("violationReasons",
                        viewService.getViolationViewByExam(activeExamId, selectedSbd, sectionType)
                                .get("violationReasons"));
            }
        }
        // Forward even without active exam so JSP can show empty state.
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
        Integer activeExamId = (Integer) session.getAttribute(ExaminerFilter.ATTR_ACTIVE_EXAM_ID);
        if (activeExamId == null || activeExamId <= 0) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        int sbd = formatPositiveInt(request.getParameter("sbd"));
        if (sbd <= 0) {
            response.sendRedirect(request.getContextPath() + "/examiner/violations?error=noCandidateNumber");
            return;
        }
        UserDTO user = (UserDTO) session.getAttribute(Attributes.Session.USER);
        Integer userId = user != null ? user.getUserId() : null;
        SectionType sectionType = ExaminerFilter.resolveSectionType(session);
        String action = request.getParameter("action");
        String redirect;
        // undoSuspend clears Candidate.IsSuspended; default action sets suspend flag.
        if ("undoSuspend".equals(action)) {
            ServiceResult<Void> result = actionService.undoSuspension(activeExamId, sbd, userId, sectionType);
            redirect = result.isSuccess()
                    ? "/examiner/violations?sbd=" + urlEncode(sbd) + "&unsuspended=1"
                    : "/examiner/violations?sbd=" + urlEncode(sbd) + "&error=unsuspendFailed";
        } else if ("createViolation".equals(action)) {
            Part evidence = request.getPart("evidenceFile");
            if (!isValidEvidence(evidence)) {
                response.sendRedirect(request.getContextPath()
                        + "/examiner/violations?sbd=" + urlEncode(sbd) + "&mode=create&error=evidenceInvalid");
                return;
            }
            String storedRef;
            try {
                storedRef = CloudinaryDocumentStorage.upload(
                        evidence, sbd, "exam-violation", extensionOf(evidence.getSubmittedFileName()));
            } catch (IOException ex) {
                response.sendRedirect(request.getContextPath()
                        + "/examiner/violations?sbd=" + urlEncode(sbd) + "&mode=create&error=evidenceUploadFailed");
                return;
            }
            ServiceResult<Void> result = actionService.recordViolation(activeExamId, sbd, userId,
                    request.getParameter("reasonCode"), request.getParameter("reasonDetail"),
                    storedRef, sectionType);
            if (!result.isSuccess()) {
                try { CloudinaryDocumentStorage.destroy(storedRef); } catch (IOException ignored) {}
            }
            redirect = result.isSuccess()
                    ? "/examiner/violations?suspended=" + urlEncode(sbd)
                    : "/examiner/violations?sbd=" + urlEncode(sbd) + "&mode=create&error=violationFailed";
        } else {
            // Default: one-click suspend (action=suspend or missing).
            ServiceResult<Void> result = actionService.markSuspended(activeExamId, sbd, userId, null, null,
                    sectionType);
            redirect = result.isSuccess()
                    ? "/examiner/violations?suspended=" + urlEncode(sbd)
                    : "/examiner/violations?sbd=" + urlEncode(sbd) + "&error=suspendFailed";
        }
        response.sendRedirect(request.getContextPath() + redirect);
    }

    private String urlEncode(int value) {
        return URLEncoder.encode(String.valueOf(value), StandardCharsets.UTF_8);
    }

    private boolean isValidEvidence(Part part) {
        if (part == null || part.getSize() <= 0 || part.getSize() > 5 * 1024 * 1024) {
            return false;
        }
        String type = part.getContentType();
        return "image/jpeg".equals(type) || "image/png".equals(type) || "image/webp".equals(type);
    }

    private String extensionOf(String fileName) {
        int dot = fileName != null ? fileName.lastIndexOf('.') : -1;
        return dot >= 0 ? fileName.substring(dot + 1).toLowerCase() : "jpg";
    }
}
