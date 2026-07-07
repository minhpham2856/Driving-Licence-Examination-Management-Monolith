package controller.staff.exam;

import service.ExamRegistrationService;

import dao.ExamSessionDAO;

import service.impl.ExamRegistrationServiceImpl;

import dao.impl.ExamSessionDAOImpl;

import dto.exam.ExamRegistrationDTO;

import dto.SessionDTO;

import util.CandidateDstsCsvSamples;
import util.CandidateDstsImportParser;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/views/staff/examstaff/upload")
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 2,
                 maxFileSize = 1024 * 1024 * 15,
                 maxRequestSize = 1024 * 1024 * 30)
public class UploadServlet extends HttpServlet {

    private final ExamRegistrationService regDAO = new ExamRegistrationServiceImpl();
    private final ExamSessionDAO sessionDAO = new ExamSessionDAOImpl();

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

        if ("downloadTestFile".equals(action)) {
            response.setContentType("text/csv; charset=UTF-8");
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"" + CandidateDstsCsvSamples.TEST_FILENAME + "\"");

            response.getOutputStream().write(CandidateDstsCsvSamples.testCsvBytes());
            response.getOutputStream().flush();
            return;
        }

        if ("downloadBulkTestFile".equals(action)) {
            response.setContentType("text/csv; charset=UTF-8");
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"" + CandidateDstsCsvSamples.BULK_TEST_FILENAME + "\"");

            response.getOutputStream().write(CandidateDstsCsvSamples.bulkTestCsvBytes());
            response.getOutputStream().flush();
            return;
        }

        if ("save".equals(action)) {
            List<ExamRegistrationDTO> previewList = (List<ExamRegistrationDTO>) session.getAttribute("previewCandidates");
            Integer selectedSessionId = (Integer) session.getAttribute("selectedImportSessionId");
            if (selectedSessionId == null || selectedSessionId <= 0) {
                List<SessionDTO> allSessions = sessionDAO.getAllSessions();
                int examId = ExamStaffViewHelper.resolveExamId(request, session, allSessions, 0);
                selectedSessionId = ExamStaffViewHelper.resolvePrimarySessionId(allSessions, examId);
            }

            if (previewList != null && !previewList.isEmpty()) {
                int importedCount = 0;
                int skippedCount = 0;
                for (ExamRegistrationDTO reg : previewList) {
                    try {
                        String dupAction = request.getParameter("dupAction_" + reg.getGovIdNo());
                        if (reg.isDuplicate() && "skip".equals(dupAction)) {
                            skippedCount++;
                            continue;
                        }
                        if (reg.isInvalid()) {
                            skippedCount++;
                            continue;
                        }

                        Integer existingId = regDAO.findCandidateIdByGovIdAndSession(
                                reg.getGovIdNo(), selectedSessionId);
                        boolean regExists = existingId != null;

                        if (regExists) {
                            int regId = existingId;
                            reg.setId(regId);
                            reg.setExamSessionId(selectedSessionId);
                            reg.setIsPresent(true);
                            regDAO.updatePresent(regId, true);
                            regDAO.updatePhoto(regId, null);
                            importedCount++;
                        } else {
                            reg.setExamSessionId(selectedSessionId);
                            reg.setIsPresent(true);
                            if (regDAO.insertFromDstsImport(reg)) {
                                regDAO.updatePhoto(reg.getId(), null);
                                importedCount++;
                            } else {
                                skippedCount++;
                            }
                        }
                    } catch (Exception ex) {
                        System.err.println("Error importing: " + reg.getFullName() + " - " + ex.getMessage());
                        ex.printStackTrace();
                        skippedCount++;
                    }
                }

                session.removeAttribute("previewCandidates");
                session.removeAttribute("validImportCount");
                Integer selectedExamId = (Integer) session.getAttribute("selectedExamId");
                int examId = selectedExamId != null && selectedExamId > 0
                        ? selectedExamId
                        : ExamStaffViewHelper.resolveExamId(request, session, sessionDAO.getAllSessions(), 0);
                String webRoot = request.getServletContext().getRealPath("/");
                ExamStaffViewHelper.refreshCandidateQueue(session, examId, webRoot);
                session.setAttribute("importedCount", importedCount);
                session.setAttribute("importSkippedCount", skippedCount);

                String uploadedFile = (String) session.getAttribute("uploadedFileName");
                if (uploadedFile == null) {
                    uploadedFile = "danh_sach.xlsx";
                }
                SessionDTO importSession = sessionDAO.getById(selectedSessionId);
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
                request, session, sessionDAO, request.getServletContext().getRealPath("/"), false);
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

        String sessionParam = request.getParameter("examSessionId");
        List<SessionDTO> allSessions = sessionDAO.getAllSessions();
        int examId = ExamStaffViewHelper.resolveExamId(request, session, allSessions, 0);
        int selectedSessionId = ExamStaffViewHelper.resolvePrimarySessionId(allSessions, examId);
        if (sessionParam != null && !sessionParam.isEmpty()) {
            try {
                int paramSessionId = Integer.parseInt(sessionParam.trim());
                SessionDTO paramSession = ExamStaffViewHelper.findSessionById(allSessions, paramSessionId);
                if (paramSession == null) {
                    paramSession = sessionDAO.getById(paramSessionId);
                }
                if (paramSession != null && paramSession.getExamId() == examId) {
                    selectedSessionId = paramSessionId;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        session.setAttribute("selectedImportSessionId", selectedSessionId);

        SessionDTO importSession = sessionDAO.getById(selectedSessionId);
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
                CandidateDstsImportParser.ParseResult parsed = CandidateDstsImportParser.parse(
                        fileBytes, fileName, examLicenseCode);

                boolean hasInvalidRows = parsed.hasInvalidRows();
                int validImportCount = 0;
                for (ExamRegistrationDTO reg : parsed.getRows()) {
                    if (reg.isInvalid()) {
                        continue;
                    }
                    String cccd = reg.getGovIdNo();
                    if (cccd == null || cccd.isBlank()) {
                        continue;
                    }
                    if (regDAO.findCandidateIdByGovIdAndSession(cccd, selectedSessionId) != null) {
                        reg.setDuplicate(true);
                    }
                    validImportCount++;
                }

                session.setAttribute("previewCandidates", parsed.getRows());
                session.setAttribute("hasInvalidRows", hasInvalidRows);
                session.setAttribute("validImportCount", validImportCount);
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
        List<java.util.Map<String, String>> sessionAuditLogs = (List<java.util.Map<String, String>>) session.getAttribute("sessionAuditLogs");
        if (sessionAuditLogs == null) {
            sessionAuditLogs = new ArrayList<>();
            session.setAttribute("sessionAuditLogs", sessionAuditLogs);
        }
        java.util.Map<String, String> audit = new java.util.HashMap<>();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm");
        audit.put("time", sdf.format(new java.util.Date()));
        audit.put("action", action);
        audit.put("details", details);
        sessionAuditLogs.add(0, audit);

        util.AuditLogHelper.persist(session, action, details, recordId);
    }
}
