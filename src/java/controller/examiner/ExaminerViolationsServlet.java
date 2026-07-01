package controller.examiner;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import filter.ExaminerFilter;
import model.User;
import service.CallService;
import service.ExamViewService;
import dto.CandidateRowDTO;
import service.impl.CallServiceImpl;
import service.impl.ExamViewServiceImpl;
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

    protected final ExamViewService viewDataService = new ExamViewServiceImpl();
    protected final CallService callService = new CallServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        Integer activeExamId = (Integer) session.getAttribute(ExaminerFilter.ATTR_ACTIVE_EXAM_ID);
        String path = stripContextPath(request);
        Integer sbd = null;
        try {
            if (request.getParameter("sbd") != null) {
                sbd = Integer.parseInt(request.getParameter("sbd").trim());
            }
        } catch (NumberFormatException e) {
        }

        String search = request.getParameter("q");

        if (activeExamId != null && activeExamId > 0) {
            boolean isTheory = Boolean.TRUE.equals(session.getAttribute("isTheory"));
            String sectionName = resolveSectionName(session);

            if ("/views/examiner/violations".equals(path)) {
                List<CandidateRowDTO> candidates = viewDataService.loadCandidateRows(activeExamId, isTheory, sectionName);
                if (search != null && !search.isBlank()) {
                    String q = search.trim().toLowerCase();
                    List<CandidateRowDTO> filtered = new java.util.ArrayList<>();
                    for (CandidateRowDTO row : candidates) {
                        String sbdVal = String.valueOf(row.getCandidateNumber());
                        String name = row.getFullName() != null ? row.getFullName() : "";
                        String gov = row.getGovernmentId() != null ? row.getGovernmentId() : "";
                        if (sbdVal.toLowerCase().contains(q)
                                || name.toLowerCase().contains(q)
                                || gov.toLowerCase().contains(q)) {
                            filtered.add(row);
                        }
                    }
                    candidates = filtered;
                    request.setAttribute("searchActive", true);
                    request.setAttribute("searchQuery", search.trim());
                }
                request.setAttribute("candidates", candidates);
                request.setAttribute("candidateQueue", candidates);

                if (sbd != null && sbd > 0) {
                    Map<String, Object> data = viewDataService.getViolationData(activeExamId, sbd);
                    if (data != null) {
                        for (Map.Entry<String, Object> mapEntry : data.entrySet()) {
                            request.setAttribute(mapEntry.getKey(), mapEntry.getValue());
                        }
                    }
                }
            } else if ("/views/examiner/violation-confirm".equals(path) || "/views/examiner/violation-undo".equals(path)) {
                if (sbd != null && sbd > 0) {
                    Map<String, Object> data = viewDataService.getViolationData(activeExamId, sbd);
                    if (data != null) {
                        for (Map.Entry<String, Object> mapEntry : data.entrySet()) {
                            request.setAttribute(mapEntry.getKey(), mapEntry.getValue());
                        }
                    }
                }
            }
        }

        String jsp = switch (path) {
            case "/views/examiner/violations" ->
                "/views/examiner/violations.jsp";
            case "/views/examiner/violation-confirm" ->
                "/views/examiner/violation-confirm.jsp";
            case "/views/examiner/violation-undo" ->
                "/views/examiner/violation-undo.jsp";
            default ->
                "/views/examiner/violations.jsp";
        };
        request.getRequestDispatcher(jsp).forward(request, response);
    }

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

        String path = stripContextPath(request);
        Integer sbd = null;
        try {
            if (request.getParameter("sbd") != null) {
                sbd = Integer.parseInt(request.getParameter("sbd").trim());
            }
        } catch (NumberFormatException e) {
        }

        if ("/views/examiner/violation-confirm".equals(path)) {
            if (sbd == null) {
                response.sendRedirect(request.getContextPath() + "/views/examiner/violations?error=noCandidateNumber");
                return;
            }

            String reasonCode = request.getParameter("reasonCode");
            String reasonDetail = request.getParameter("reasonDetail");
            Part filePart = request.getPart("evidenceFile");

            String evidencePath = null;
            if (filePart != null && filePart.getSize() > 0) {
                String uploadsDirStr = getServletContext().getRealPath("") + File.separator + "uploads" + File.separator + "violations";
                File uploadDir = new File(uploadsDirStr);
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }

                String datePrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                String fileName = datePrefix + "_" + activeExamId + "_" + sbd + "_" + System.currentTimeMillis() + "_" + getSubmittedFileName(filePart);
                Path destination = new File(uploadDir, fileName).toPath();
                try (InputStream input = filePart.getInputStream()) {
                    Files.copy(input, destination, StandardCopyOption.REPLACE_EXISTING);
                    evidencePath = "uploads/violations/" + fileName;
                } catch (Exception e) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/violation-confirm?sbd="
                            + urlEncode(sbd) + "&error=uploadFailed");
                    return;
                }
            }

            dto.ServiceResult<Void> result = callService.recordViolation(activeExamId, sbd,
                    ((User) session.getAttribute("user")).getUserId(),
                    reasonCode,
                    reasonDetail,
                    evidencePath);

            if (result.isSuccess()) {
                response.sendRedirect(request.getContextPath() + "/views/examiner/violations?sbd="
                        + urlEncode(sbd) + "&violationRecorded=1");
                return;
            }

            request.setAttribute("errorMsg", "Không thể ghi nhận vi phạm: " + result.getMessage());
            doGet(request, response);
            return;
        }

        doGet(request, response);
    }

    private String getSubmittedFileName(Part part) {
        for (String cd : part.getHeader("content-disposition").split(";")) {
            if (cd.trim().startsWith("filename")) {
                String fileName = cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
                return fileName.substring(fileName.lastIndexOf('/') + 1).substring(fileName.lastIndexOf('\\') + 1);
            }
        }
        return "unknown.jpg";
    }

    private String urlEncode(int value) {
        return URLEncoder.encode(String.valueOf(value), StandardCharsets.UTF_8);
    }

    private String stripContextPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String ctx = request.getContextPath();
        if (ctx != null && !ctx.isEmpty() && uri.startsWith(ctx)) {
            return uri.substring(ctx.length());
        }
        return uri;
    }

    private String resolveSectionName(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object name = session.getAttribute("examSectionName");
        return name != null ? String.valueOf(name) : null;
    }
}
