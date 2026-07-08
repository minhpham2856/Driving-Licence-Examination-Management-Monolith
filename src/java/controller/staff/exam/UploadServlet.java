package controller.staff.exam;

import service.ExamStaffSessionQueryService;
import service.impl.ExamStaffSessionQueryServiceImpl;

import dto.exam.ExamRegistrationDTO;

import dto.SessionDTO;

import controller.staff.exam.support.ExamStaffPageBinder;
import controller.staff.exam.support.StaffAuditLogSupport;
import service.CandidateDstsImportService;
import service.impl.CandidateDstsImportServiceImpl;
import dto.examstaff.CandidateDstsImportCommitResultDTO;
import dto.examstaff.CandidateDstsImportPreviewDTO;
import util.CandidateDstsCsvSamples;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/views/staff/examstaff/upload")
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 2,
                 maxFileSize = 1024 * 1024 * 15,
                 maxRequestSize = 1024 * 1024 * 30)
public class UploadServlet extends HttpServlet {

    private final ExamStaffSessionQueryService sessionQueryService = new ExamStaffSessionQueryServiceImpl();
    private final CandidateDstsImportService importService = new CandidateDstsImportServiceImpl();

    // Xu ly yeu cau GET
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");
        HttpSession session = request.getSession();

        if ("downloadTemplate".equals(action)) {
            response.setContentType("text/csv; charset=UTF-8");
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"" + CandidateDstsCsvSamples.TEMPLATE_FILENAME + "\"");

            response.getOutputStream().write(CandidateDstsCsvSamples.templateCsvBytes());
            response.getOutputStream().flush();
            return;
        }

        if ("save".equals(action)) {
            List<ExamRegistrationDTO> previewList = (List<ExamRegistrationDTO>) session.getAttribute("previewCandidates");
            Integer selectedSessionId = (Integer) session.getAttribute("selectedImportSessionId");
            if (selectedSessionId == null || selectedSessionId <= 0) {
                List<SessionDTO> allSessions = sessionQueryService.listAllSessions();
                int examId = ExamStaffViewHelper.resolveExamId(request, session, allSessions, 0);
                selectedSessionId = resolveImportSessionId(request, session, allSessions, examId);
            }

            if (previewList != null && !previewList.isEmpty()) {
                Map<String, String> duplicateActions = new HashMap<>();
                for (ExamRegistrationDTO reg : previewList) {
                    if (reg.getGovIdNo() != null) {
                        duplicateActions.put(reg.getGovIdNo(),
                                request.getParameter("dupAction_" + reg.getGovIdNo()));
                    }
                }
                CandidateDstsImportCommitResultDTO commit = importService.commit(
                        previewList, selectedSessionId, duplicateActions);
                int importedCount = commit.getImportedCount();
                int skippedCount = commit.getSkippedCount();

                session.removeAttribute("previewCandidates");
                session.removeAttribute("validImportCount");
                Integer selectedExamId = (Integer) session.getAttribute("selectedExamId");
                int examId = selectedExamId != null && selectedExamId > 0
                        ? selectedExamId
                        : ExamStaffViewHelper.resolveExamId(request, session, sessionQueryService.listAllSessions(), 0);
                String webRoot = request.getServletContext().getRealPath("/");
                ExamStaffViewHelper.refreshCandidateQueue(session, examId, selectedSessionId, webRoot,
                        sessionQueryService.listAllSessions());
                if (selectedSessionId > 0) {
                    SessionDTO importSessionDto = sessionQueryService.findBySessionId(selectedSessionId);
                    if (importSessionDto != null && importSessionDto.getExamId() > 0) {
                        ExamStaffPageBinder.persistExamSelection(session, selectedSessionId,
                                importSessionDto.getExamId());
                    }
                    session.setAttribute("selectedImportSessionId", selectedSessionId);
                }
                session.setAttribute("importedCount", importedCount);
                session.setAttribute("importSkippedCount", skippedCount);
                session.setAttribute("importSkipSummary", commit.getSkipSummary());

                String uploadedFile = (String) session.getAttribute("uploadedFileName");
                if (uploadedFile == null) {
                    uploadedFile = "danh_sach.xlsx";
                }
                SessionDTO importSession = sessionQueryService.findBySessionId(selectedSessionId);
                String sessionLabel = importSession != null ? importSession.getSessionName() : ("SessionId " + selectedSessionId);
                String auditDetails = "Import DSTS \"" + uploadedFile + "\": nhập " + importedCount
                        + " thí sinh vào ca " + sessionLabel + " (SessionId=" + selectedSessionId + ")"
                // add audit log
                        + (skippedCount > 0 ? ", bỏ qua " + skippedCount + " dòng" : "");
                addAuditLog(session, "IMPORT Candidates", auditDetails, selectedSessionId);

                response.sendRedirect("upload?importSuccess=true");
                return;
            }
        }

        ExamStaffViewHelper.ExamStaffPageContext pageCtx = ExamStaffViewHelper.prepareExamStaffPage(
                request, session, request.getServletContext().getRealPath("/"), false);
        int examId = pageCtx.getExamId();
        int sessionId = pageCtx.getSessionId();
        SessionDTO currentSession = (SessionDTO) request.getAttribute("currentSession");
        ExamStaffViewHelper.bindImportExamAttributes(request, currentSession, examId);
        session.setAttribute("selectedImportSessionId", sessionId);

        request.getRequestDispatcher("/views/staff/examstaff/upload.jsp").forward(request, response);
    // Xu ly yeu cau POST
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        session.removeAttribute("uploadError");
        session.removeAttribute("hasInvalidRows");
        session.removeAttribute("validImportCount");

        List<SessionDTO> allSessions = sessionQueryService.listAllSessions();
        int examId = ExamStaffViewHelper.resolveExamId(request, session, allSessions, 0);
        int selectedSessionId = resolveImportSessionId(request, session, allSessions, examId);
        session.setAttribute("selectedImportSessionId", selectedSessionId);
        if (selectedSessionId > 0) {
            SessionDTO picked = sessionQueryService.findBySessionId(selectedSessionId);
            if (picked != null && picked.getExamId() > 0) {
                examId = picked.getExamId();
                session.setAttribute("selectedExamId", examId);
            }
        }

        SessionDTO importSession = sessionQueryService.findBySessionId(selectedSessionId);
        String examLicenseCode = importSession != null ? importSession.getLicenseCode() : null;
        if (examLicenseCode != null) {
            session.setAttribute("selectedImportExamLicense", examLicenseCode);
        }

        try {
            Part filePart = request.getPart("fileInput");
            if (filePart != null && filePart.getSize() > 0) {
                String fileName = filePart.getSubmittedFileName();
                session.setAttribute("uploadedFileName", fileName);

                byte[] fileBytes = filePart.getInputStream().readAllBytes();
                CandidateDstsImportPreviewDTO preview = importService.preview(
                        fileBytes, fileName, examLicenseCode, selectedSessionId);

                session.setAttribute("previewCandidates", preview.getRows());
                session.setAttribute("hasInvalidRows", preview.isHasInvalidRows());
                session.setAttribute("validImportCount", preview.getValidImportCount());
                response.sendRedirect("upload?preview=true");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("uploadError", "Lỗi xử lý tệp: " + e.getMessage());
        }

    // add audit log
        response.sendRedirect("upload");
    }

    private void addAuditLog(HttpSession session, String action, String details, int recordId) {
        StaffAuditLogSupport.persistWithSessionFeed(session, action, details, recordId);
    }

    private static int resolveImportSessionId(HttpServletRequest request, HttpSession session,
            List<SessionDTO> allSessions, int examId) {
        String sessionParam = request.getParameter("examSessionId");
        if (sessionParam != null && !sessionParam.isBlank()) {
            try {
                int paramSessionId = Integer.parseInt(sessionParam.trim());
                SessionDTO paramSession = ExamStaffViewHelper.findSessionById(allSessions, paramSessionId);
                if (paramSession != null) {
                    return paramSessionId;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        if (session != null) {
            Object stored = session.getAttribute("selectedImportSessionId");
            if (stored instanceof Integer storedId && storedId > 0) {
                return storedId;
            }
            Object lastLoaded = session.getAttribute("lastLoadedSessionId");
            if (lastLoaded instanceof Integer lastId && lastId > 0) {
                return lastId;
            }
        }
        if (examId > 0) {
            return ExamStaffViewHelper.resolvePrimarySessionId(allSessions, examId);
        }
        return 0;
    }
}
