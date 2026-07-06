package controller.examiner;

import dto.ExaminerCandidateRowDTO;
import filter.ExaminerFilter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;
import service.ExaminerActionsService;
import service.ExaminerDataService;
import service.impl.ExaminerActionsServiceImpl;
import service.impl.ExaminerDataServiceImpl;
import util.ExamSessionState;
import util.ExaminerCandidateSort;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@WebServlet(urlPatterns = {
    "/views/examiner/violations",
    "/views/examiner/violation-detail",
    "/views/examiner/violation-confirm",
    "/views/examiner/violation-undo"
})
@MultipartConfig(maxFileSize = 5 * 1024 * 1024)
public class ExaminerViolationsServlet extends BaseExaminerServlet {

    protected final ExaminerDataService viewDataService = new ExaminerDataServiceImpl();
    protected final ExaminerActionsService examinerService = new ExaminerActionsServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = requireSession(request, response);
        if (session == null) {
            return;
        }
        Integer sessionId = getActiveSessionId(session);
        String path = stripContextPath(request);
        Integer sbd = parseSbdParam(request.getParameter("sbd"));
        if ("/views/examiner/violation-undo".equals(path)) {
            String target = request.getContextPath() + "/views/examiner/violation-detail";
            if (sbd != null) {
                target += "?sbd=" + encodeSbd(sbd);
            }
            response.sendRedirect(target);
            return;
        }
        if (sessionId != null && sessionId > 0) {
            if ("/views/examiner/violations".equals(path)) {
                loadViolationList(request, sessionId, sbd);
            } else if ("/views/examiner/violation-detail".equals(path)
                    || "/views/examiner/violation-confirm".equals(path)) {
                if (sbd == null) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/violations?error=noSbd");
                    return;
                }
                Map<String, Object> data = viewDataService.getViolationData(sessionId, sbd);
                for (Map.Entry<String, Object> mapEntry : data.entrySet()) {
                    request.setAttribute(mapEntry.getKey(), mapEntry.getValue());
                }
                if (request.getAttribute("candidate") == null) {
                    response.sendRedirect(request.getContextPath() + "/views/examiner/violations?error=noSbd");
                    return;
                }
            }
        } else {
            response.sendRedirect(request.getContextPath() + "/views/examiner/violations?error=noSession");
            return;
        }
        String jsp = switch (path) {
            case "/views/examiner/violations" -> "/views/examiner/violations.jsp";
            case "/views/examiner/violation-detail" -> "/views/examiner/violation-detail.jsp";
            case "/views/examiner/violation-confirm" -> "/views/examiner/violation-confirm.jsp";
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
        Integer sessionId = getActiveSessionId(session);
        if (sessionId == null || sessionId <= 0) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        String path = stripContextPath(request);
        if (!"/views/examiner/violation-confirm".equals(path)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        Integer sbd = parseSbdParam(request.getParameter("sbd"));
        if (sbd == null) {
            response.sendRedirect(request.getContextPath() + "/views/examiner/violations?error=noSbd");
            return;
        }
        User user = (User) session.getAttribute("user");
        int userId = user != null ? user.getUserId() : 0;
        String reasonCode = request.getParameter("reasonCode");
        String reasonDetail = request.getParameter("reasonDetail");
        String evidencePath = saveEvidence(request);
        boolean isTheory = ExaminerFilter.isTheorySession(session);
        String sectionName = getSectionDisplayName(session);
        if (!examinerService.recordViolation(
                buildViolationCommand(session, sessionId, sbd, user, reasonCode, reasonDetail, evidencePath, new int[0])).isSuccess()) {
            response.sendRedirect(request.getContextPath() + "/views/examiner/violation-confirm?sbd="
                    + encodeSbd(sbd) + "&error=recordFailed");
            return;
        }
        ExamSessionState.removeCandidate(getServletContext(), sessionId, sbd);
        response.sendRedirect(request.getContextPath() + "/views/examiner/violation-detail?sbd="
                + encodeSbd(sbd) + "&violationDone=" + encodeSbd(sbd));
    }

    private void loadViolationList(HttpServletRequest request, int sessionId, Integer sbd) {
        Map<String, Object> data = viewDataService.getViolationData(sessionId, sbd);
        for (Map.Entry<String, Object> mapEntry : data.entrySet()) {
            request.setAttribute(mapEntry.getKey(), mapEntry.getValue());
        }
        Object candidatesObj = request.getAttribute("candidates");
        if (candidatesObj instanceof List<?> candidates) {
            @SuppressWarnings("unchecked")
            List<ExaminerCandidateRowDTO> rows = (List<ExaminerCandidateRowDTO>) candidates;
            ExaminerCandidateSort.applyCandidateSort(request, rows);
            request.setAttribute("candidates", rows);
        }
    }

    private String saveEvidence(HttpServletRequest request) {
        try {
            var part = request.getPart("evidenceFile");
            if (part == null || part.getSize() <= 0) {
                return null;
            }
            String fileName = part.getSubmittedFileName();
            if (fileName == null || fileName.isBlank()) {
                return null;
            }
            String ext = "";
            int dot = fileName.lastIndexOf('.');
            if (dot >= 0) {
                ext = fileName.substring(dot);
            }
            String stored = UUID.randomUUID() + ext;
            Path dir = Paths.get(request.getServletContext().getRealPath("/uploads/violations"));
            Files.createDirectories(dir);
            Path target = dir.resolve(stored);
            part.write(target.toString());
            return "uploads/violations/" + stored;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
