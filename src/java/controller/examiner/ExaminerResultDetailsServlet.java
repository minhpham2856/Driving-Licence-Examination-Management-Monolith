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
import java.util.Map;

@WebServlet(urlPatterns = {
    "/views/examiner/result-details",
    "/views/examiner/result-details-edit"
})
public class ExaminerResultDetailsServlet extends HttpServlet {
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
        String action = request.getParameter("action");

        if (sessionId != null && sessionId > 0) {
            if (Boolean.TRUE.equals(session.getAttribute("isTheory"))) {
                response.sendRedirect(request.getContextPath() + "/views/examiner/candidate-call?error=theoryNoResultEdit");
                return;
            }

            if ("/views/examiner/result-details-edit".equals(path) && "adjustDeduction".equals(action)) {
                if (sbd == null) {
                    response.sendRedirect(request.getContextPath() + path + "?error=noSbd");
                    return;
                }
                int deductionId;
                int delta;
                try {
                    deductionId = Integer.parseInt(request.getParameter("deductionId"));
                    delta = Integer.parseInt(request.getParameter("delta"));
                } catch (Exception e) {
                    response.sendRedirect(request.getContextPath() + path + "?sbd=" + urlEncode(sbd) + "&error=invalidDeduction");
                    return;
                }
                if (!callService.adjustScoreDeduction(sessionId, sbd, deductionId, delta, ((User) session.getAttribute("user")).getUserId()).isSuccess()) {
                    response.sendRedirect(request.getContextPath() + path + "?sbd=" + urlEncode(sbd) + "&error=deductionFailed");
                    return;
                }
                response.sendRedirect(request.getContextPath() + path + "?sbd=" + urlEncode(sbd));
                return;
            }

            if (sbd != null) {
                boolean isTheory = Boolean.TRUE.equals(session.getAttribute("isTheory"));
                String sectionName = resolveSectionName(session);

                if ("/views/examiner/result-details-edit".equals(path)) {
                    Map<String, Object> data = viewDataService.getScoreEntryData(sessionId, sbd, sectionName);
                    if (data != null) {
                        for (Map.Entry<String, Object> mapEntry : data.entrySet()) {
                            request.setAttribute(mapEntry.getKey(), mapEntry.getValue());
                        }
                    }
                } else {
                    request.setAttribute("candidateQueue", viewDataService.loadCandidateRows(sessionId, isTheory, sectionName));
                    CandidateRowDTO candidate = viewDataService.getCandidateViewRow(sessionId, sbd, isTheory, sectionName);
                    if (candidate != null) {
                        request.setAttribute("candidate", candidate);
                    }
                }
            }
        }

        String jsp = switch (path) {
            case "/views/examiner/result-details" -> "/views/examiner/result-details.jsp";
            case "/views/examiner/result-details-edit" -> "/views/examiner/result-details-edit.jsp";
            default -> "/views/examiner/result-details.jsp";
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
        if ("/views/examiner/result-details-edit".equals(path)) {
            Integer sbd = null;
            try {
                if (request.getParameter("sbd") != null) {
                    sbd = Integer.parseInt(request.getParameter("sbd").trim());
                }
            } catch (NumberFormatException e) {}

            if (sbd == null) {
                response.sendRedirect(request.getContextPath() + "/views/examiner/result-details?error=noSbd");
                return;
            }

            String reason = request.getParameter("reasonCode");
            String reasonDetail = request.getParameter("reasonDetail");
            String password = request.getParameter("confirmPassword");
            User user = (User) session.getAttribute("user");
            if (!callService.logPracticalScoreEditReason(sessionId, sbd, user, password, reason, reasonDetail, user.getUserId()).isSuccess()) {
                request.setAttribute("editError", "Lưu lý do thất bại. Vui lòng kiểm tra lại mật khẩu xác nhận.");
                doGet(request, response);
                return;
            }
            
            response.sendRedirect(request.getContextPath() + "/views/examiner/result-details?sbd="
                    + urlEncode(sbd) + "&reasonSaved=1");
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
