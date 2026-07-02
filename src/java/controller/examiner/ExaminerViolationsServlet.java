package controller.examiner;
import dto.ExaminerSlotDTO;
import filter.ExaminerPortalFilter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import model.User;
import service.ExaminerActionsService;
import service.ExaminerDataService;
import service.impl.ExaminerActionsServiceImpl;
import service.impl.ExaminerDataServiceImpl;
import util.ExaminerCandidateSort;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
@WebServlet(urlPatterns = {
    "/views/examiner/violations",
    "/views/examiner/violation-confirm",
    "/views/examiner/violation-undo"
})
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 5 * 1024 * 1024,
        maxRequestSize = 10 * 1024 * 1024)
public class ExaminerViolationsServlet extends HttpServlet {
    private static final String UPLOAD_SUBDIR = "uploads/violations";
    protected final ExaminerDataService viewDataService = new ExaminerDataServiceImpl();
    protected final ExaminerActionsService examinerService = new ExaminerActionsServiceImpl();
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = requireSession(request, response);
        if (session == null) {
            return;
        }
        Integer sessionId = activeSessionId(session);
        String path = stripContextPath(request);
        Integer sbd = parseSbdParam(request.getParameter("sbd"));
        if (sessionId != null && sessionId > 0) {
            if ("/views/examiner/violations".equals(path)) {
                Map<String, Object> data = viewDataService.getViolationData(sessionId, sbd);
                for (Map.Entry<String, Object> mapEntry : data.entrySet()) {
                    request.setAttribute(mapEntry.getKey(), mapEntry.getValue());
                }
                Object candidatesObj = request.getAttribute("candidates");
                if (candidatesObj instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> candidates = (List<Map<String, Object>>) candidatesObj;
                    ExaminerCandidateSort.applyCandidateSort(request, candidates);
                    request.setAttribute("candidates", candidates);
                }
            } else {
                if (sbd == null || request.getAttribute("candidate") == null) {
                    Map<String, Object> data = viewDataService.getViolationData(sessionId, sbd);
                    for (Map.Entry<String, Object> mapEntry : data.entrySet()) {
                        request.setAttribute(mapEntry.getKey(), mapEntry.getValue());
                    }
                    if (request.getAttribute("candidate") == null) {
                        response.sendRedirect(request.getContextPath() + "/views/examiner/violations?error=noSbd");
                        return;
                    }
                }
                if ("/views/examiner/violation-confirm".equals(path)) {
                    Object candidateObj = request.getAttribute("candidate");
                    if (candidateObj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> candidateMap = (Map<String, Object>) candidateObj;
                        if (Boolean.TRUE.equals(candidateMap.get("suspended"))) {
                            response.sendRedirect(request.getContextPath() + "/views/examiner/violations?error=alreadySuspended");
                            return;
                        }
                    }
                } else if ("/views/examiner/violation-undo".equals(path)) {
                    Object candidateObj = request.getAttribute("candidate");
                    if (candidateObj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> candidateMap = (Map<String, Object>) candidateObj;
                        if (!Boolean.TRUE.equals(candidateMap.get("suspended"))) {
                            response.sendRedirect(request.getContextPath() + "/views/examiner/violations?error=notSuspended");
                            return;
                        }
                    }
                }
            }
        } else {
            response.sendRedirect(request.getContextPath() + "/views/examiner/violations?error=noSession");
            return;
        }
        String jsp = switch (path) {
            case "/views/examiner/violations" -> "/views/examiner/violations.jsp";
            case "/views/examiner/violation-confirm" -> "/views/examiner/violation-confirm.jsp";
            case "/views/examiner/violation-undo" -> "/views/examiner/violation-undo.jsp";
            default -> "/views/examiner/violations.jsp";
        };
        request.getRequestDispatcher(jsp).forward(request, response);
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = requireSession(request, response);
        if (session == null) {
            return;
        }
        Integer sessionId = activeSessionId(session);
        if (sessionId == null || sessionId <= 0) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        String path = stripContextPath(request);
        if ("/views/examiner/violation-confirm".equals(path)) {
            handleRecordViolation(request, response, session, sessionId);
            return;
        }
        if ("/views/examiner/violation-undo".equals(path)) {
            handleUndoSuspension(request, response, sessionId);
            return;
        }
        doGet(request, response);
    }
    private void handleRecordViolation(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, int sessionId) throws IOException, ServletException {
        Integer sbd = parseSbdParam(request.getParameter("sbd"));
        String reasonCode = request.getParameter("reasonCode");
        String reasonDetail = request.getParameter("reasonDetail");
        if (reasonCode == null || reasonCode.isBlank()) {
            Map<String, Object> data = viewDataService.getViolationData(sessionId, sbd);
            for (Map.Entry<String, Object> mapEntry : data.entrySet()) {
                request.setAttribute(mapEntry.getKey(), mapEntry.getValue());
            }
            request.setAttribute("violationError", "Vui lòng chọn lý do vi phạm.");
            request.getRequestDispatcher("/views/examiner/violation-confirm.jsp").forward(request, response);
            return;
        }
        String evidencePath = null;
        try {
            Part evidencePart = request.getPart("evidenceFile");
            evidencePath = saveViolationEvidence(request, evidencePart, sessionId);
        } catch (IOException | ServletException e) {
            Map<String, Object> data = viewDataService.getViolationData(sessionId, sbd);
            for (Map.Entry<String, Object> mapEntry : data.entrySet()) {
                request.setAttribute(mapEntry.getKey(), mapEntry.getValue());
            }
            request.setAttribute("violationError", e.getMessage() != null ? e.getMessage() : "Không tải được file minh chứng.");
            request.getRequestDispatcher("/views/examiner/violation-confirm.jsp").forward(request, response);
            return;
        }
        String returnTo = request.getParameter("returnTo");
        if (returnTo == null || returnTo.isBlank()) {
            returnTo = "/views/examiner/violations";
        }
        int[] deductionIds = parseDeductionIds(request.getParameterValues("deductionId"));
        if (sbd == null) {
            response.sendRedirect(request.getContextPath() + "/views/examiner/violations?error=noSbd");
            return;
        }
        boolean saved = examinerService.recordViolation(
                sessionId, sbd, reasonCode, reasonDetail, evidencePath, deductionIds,
                ((User) session.getAttribute("user")).getUserId(),
                ExaminerPortalFilter.isTheorySession(session),
                resolveSectionName(session));
        if (saved) {
            response.sendRedirect(request.getContextPath() + returnTo + "?suspended="
                    + urlEncode(sbd));
            return;
        }
        response.sendRedirect(request.getContextPath() + "/views/examiner/violation-confirm?sbd="
                + urlEncode(sbd) + "&error=saveFailed&returnTo=" + returnTo);
    }
    private void handleUndoSuspension(HttpServletRequest request, HttpServletResponse response,
            int sessionId) throws IOException, ServletException {
        Integer sbd = parseSbdParam(request.getParameter("sbd"));
        String reasonCode = request.getParameter("reasonCode");
        String reasonDetail = request.getParameter("reasonDetail");
        if (reasonCode == null || reasonCode.isBlank()) {
            Map<String, Object> data = viewDataService.getViolationData(sessionId, sbd);
            for (Map.Entry<String, Object> mapEntry : data.entrySet()) {
                request.setAttribute(mapEntry.getKey(), mapEntry.getValue());
            }
            request.setAttribute("undoError", "Vui lòng chọn lý do hoàn tác.");
            request.getRequestDispatcher("/views/examiner/violation-undo.jsp").forward(request, response);
            return;
        }
        if (sbd == null) {
            response.sendRedirect(request.getContextPath() + "/views/examiner/violations?error=noSbd");
            return;
        }
        boolean undone = examinerService.undoSuspension(sessionId, sbd, reasonCode, reasonDetail,
                ((User) request.getSession().getAttribute("user")).getUserId());
        if (undone) {
            response.sendRedirect(request.getContextPath() + "/views/examiner/violations?undoSuspended="
                    + urlEncode(sbd));
            return;
        }
        response.sendRedirect(request.getContextPath() + "/views/examiner/violation-undo?sbd="
                + urlEncode(sbd) + "&error=undoFailed");
    }
    private String saveViolationEvidence(HttpServletRequest request, Part filePart, int sessionId) {
        if (filePart == null || filePart.getSize() <= 0) {
            return null;
        }
        try {
            String webRoot = request.getServletContext().getRealPath("/");
            String datePrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String dirPath = webRoot + File.separator + UPLOAD_SUBDIR + File.separator + sessionId;
            File dir = new File(dirPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String fileName = datePrefix + "_" + System.currentTimeMillis() + "_" + getSubmittedFileName(filePart);
            Path target = new File(dir, fileName).toPath();
            try (InputStream in = filePart.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return UPLOAD_SUBDIR + "/" + sessionId + "/" + fileName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    private static String getSubmittedFileName(Part part) {
        String cd = part.getHeader("Content-Disposition");
        if (cd == null) {
            return "unknown";
        }
        for (String token : cd.split(";")) {
            String t = token.trim();
            if (t.startsWith("filename")) {
                String val = t.substring(t.indexOf('=') + 1).trim().replace("\"", "");
                int idx = Math.max(val.lastIndexOf('/'), val.lastIndexOf('\\'));
                return idx >= 0 ? val.substring(idx + 1) : val;
            }
        }
        return "unknown";
    }
    private HttpSession requireSession(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
        return session;
    }
    private Integer activeSessionId(HttpSession session) {
        if (session == null) {
            return null;
        }
        return (Integer) session.getAttribute(ExaminerPortalFilter.ATTR_ACTIVE_SESSION_ID);
    }
    private String stripContextPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String ctx = request.getContextPath();
        if (ctx != null && !ctx.isEmpty() && uri.startsWith(ctx)) {
            return uri.substring(ctx.length());
        }
        return uri;
    }
    private Integer parseSbdParam(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            int sbd = Integer.parseInt(raw.trim());
            return sbd > 0 ? sbd : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
    private String urlEncode(int value) {
        return URLEncoder.encode(String.valueOf(value), StandardCharsets.UTF_8);
    }
    private int[] parseDeductionIds(String[] values) {
        if (values == null || values.length == 0) {
            return new int[0];
        }
        int[] ids = new int[values.length];
        int count = 0;
        for (String value : values) {
            try {
                int id = Integer.parseInt(value.trim());
                if (id > 0) {
                    ids[count++] = id;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        if (count == ids.length) {
            return ids;
        }
        int[] trimmed = new int[count];
        System.arraycopy(ids, 0, trimmed, 0, count);
        return trimmed;
    }
    private String resolveSectionName(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object slotObj = session.getAttribute(ExaminerPortalFilter.ATTR_SLOT);
        if (slotObj instanceof ExaminerSlotDTO) {
            return ((ExaminerSlotDTO) slotObj).getExamTypeName();
        }
        Object name = session.getAttribute(ExaminerPortalFilter.ATTR_EXAM_SECTION_NAME);
        return name != null ? String.valueOf(name) : null;
    }
}
