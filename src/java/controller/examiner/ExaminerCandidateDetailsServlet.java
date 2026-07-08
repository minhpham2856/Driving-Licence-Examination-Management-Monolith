package controller.examiner;

import model.User;
import service.CallService;
import service.ExamViewService;
import dto.CandidateRowDTO;
import service.impl.CallServiceImpl;
import service.impl.ExamViewServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

@WebServlet(urlPatterns = {
    "/views/examiner/candidate-details",
    "/views/examiner/candidate-details-edit",
    "/views/examiner/candidate-paper"
})
public class ExaminerCandidateDetailsServlet extends HttpServlet {
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

        Integer sessionId = (Integer) session.getAttribute("activeSessionId");
        String path = stripContextPath(request);
        Integer sbd = null;
        try {
            if (request.getParameter("sbd") != null) {
                sbd = Integer.parseInt(request.getParameter("sbd").trim());
            }
        } catch (NumberFormatException e) {}
        
        String search = request.getParameter("q");

        if (sessionId != null && sessionId > 0) {
            boolean isTheory = Boolean.TRUE.equals(session.getAttribute("isTheory"));
            String sectionName = resolveSectionName(session);

            List<CandidateRowDTO> candidates = viewDataService.loadCandidateRows(sessionId, isTheory, sectionName);
            request.setAttribute("candidates", candidates);
            request.setAttribute("candidateQueue", candidates);

            if (sbd != null && sbd > 0) {
                CandidateRowDTO candidate = viewDataService.getCandidateViewRow(sessionId, sbd, isTheory, sectionName);
                if (candidate != null) {
                    request.setAttribute("candidate", candidate);
                }
                
                if ("/views/examiner/candidate-paper".equals(path)) {
                    Map<String, Object> ansData = viewDataService.getPaperAnswersData(sessionId, sbd, request.getContextPath());
                    if (ansData != null) {
                        for (Map.Entry<String, Object> mapEntry : ansData.entrySet()) {
                            request.setAttribute(mapEntry.getKey(), mapEntry.getValue());
                        }
                    }
                }
            }
        }

        String jsp = switch (path) {
            case "/views/examiner/candidate-details" -> "/views/examiner/candidate-details.jsp";
            case "/views/examiner/candidate-details-edit" -> "/views/examiner/candidate-details-edit.jsp";
            case "/views/examiner/candidate-paper" -> "/views/examiner/candidate-paper.jsp";
            default -> "/views/examiner/candidate-details.jsp";
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
        Integer sessionId = (Integer) session.getAttribute("activeSessionId");
        if (sessionId == null || sessionId <= 0) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String path = stripContextPath(request);
        if ("/views/examiner/candidate-details-edit".equals(path)) {
            Integer sbd = null;
            try {
                if (request.getParameter("sbd") != null) {
                    sbd = Integer.parseInt(request.getParameter("sbd").trim());
                }
            } catch (NumberFormatException e) {}

            if (sbd == null) {
                response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-details?error=noSbd");
                return;
            }

            Date dob = null;
            String dobStr = request.getParameter("dateOfBirth");
            if (dobStr != null && !dobStr.isBlank()) {
                try {
                    dob = new Date(new SimpleDateFormat("yyyy-MM-dd").parse(dobStr).getTime());
                } catch (ParseException e) {
                    // Ignore or handle
                }
            }

            dto.ServiceResult<Void> result = callService.updateCandidateProfile(
                    sessionId,
                    sbd,
                    ((User) session.getAttribute("user")).getUserId(),
                    request.getParameter("fullName"),
                    dob,
                    request.getParameter("govIdNo"),
                    request.getParameter("phoneNo"),
                    request.getParameter("address"),
                    request.getParameter("sex"),
                    request.getParameter("reasonForTaking")
            );

            if (result.isSuccess()) {
                response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-details-edit?sbd="
                        + urlEncode(sbd) + "&saved=1");
                return;
            }

            boolean isTheory = Boolean.TRUE.equals(session.getAttribute("isTheory"));
            String sectionName = resolveSectionName(session);
            List<CandidateRowDTO> candidates = viewDataService.loadCandidateRows(sessionId, isTheory, sectionName);
            request.setAttribute("candidates", candidates);
            request.setAttribute("candidateQueue", candidates);
            CandidateRowDTO candidate = viewDataService.getCandidateViewRow(sessionId, sbd, isTheory, sectionName);
            if (candidate != null) {
                request.setAttribute("candidate", candidate);
            }

            request.setAttribute("profileError", "Không lưu được thông tin: " + result.getMessage());
            request.getRequestDispatcher("/views/examiner/candidate-details-edit.jsp").forward(request, response);
            return;
        }
        doGet(request, response);
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

    private String urlEncode(int value) {
        return URLEncoder.encode(String.valueOf(value), StandardCharsets.UTF_8);
    }
}
