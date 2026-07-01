package controller.examiner;

import enums.SectionType;
import filter.ExaminerFilter;
import model.User;
import service.CallService;
import service.ExamViewService;
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

@WebServlet("/old_views/examiner/score-entry")
public class ExaminerScoreEntryServlet extends HttpServlet {
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
        Integer sbd = null;
        try {
            if (request.getParameter("sbd") != null) {
                sbd = Integer.parseInt(request.getParameter("sbd").trim());
            }
        } catch (NumberFormatException e) {}
        
        String action = request.getParameter("action");
        User user = (User) session.getAttribute("user");
        boolean isTheory = Boolean.TRUE.equals(session.getAttribute("isTheory"));
        String sectionName = resolveSectionName(session);

        if (activeExamId != null && activeExamId > 0) {
            if (isTheory && request.getParameter("error") == null) {
                response.sendRedirect(request.getContextPath() + "/old_views/examiner/candidate-call?error=theoryNoScoreEntry");
                return;
            }

            if (action != null) {
                if ("adjustDeduction".equals(action)) {
                    if (sbd == null) {
                        response.sendRedirect(request.getContextPath() + "/old_views/examiner/score-entry?error=noSbd");
                        return;
                    }
                    int deductionId;
                    int delta;
                    try {
                        deductionId = Integer.parseInt(request.getParameter("deductionId"));
                        delta = Integer.parseInt(request.getParameter("delta"));
                    } catch (Exception e) {
                        response.sendRedirect(request.getContextPath() + "/old_views/examiner/score-entry?sbd="
                                + urlEncode(sbd) + "&error=invalidDeduction");
                        return;
                    }
                    
                    if (!callService.adjustScoreDeduction(activeExamId, sbd, deductionId, delta, user.getUserId()).isSuccess()) {
                        response.sendRedirect(request.getContextPath() + "/old_views/examiner/score-entry?sbd="
                                + urlEncode(sbd) + "&error=deductionFailed");
                        return;
                    }
                    response.sendRedirect(request.getContextPath() + "/old_views/examiner/score-entry?sbd="
                            + urlEncode(sbd));
                    return;
                }
                
                if (handleScoreEntryAction(request, response, session, activeExamId, action, sbd, user, isTheory, sectionName)) {
                    return;
                }
            }

            if (sbd != null) {
                Map<String, Object> data = viewDataService.getScoreEntryData(activeExamId, sbd, sectionName);
                if (data != null) {
                    for (Map.Entry<String, Object> mapEntry : data.entrySet()) {
                        request.setAttribute(mapEntry.getKey(), mapEntry.getValue());
                    }
                }
            }
        }

        request.getRequestDispatcher("/old_views/examiner/score-entry.jsp").forward(request, response);
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

        if ("finalize".equals(request.getParameter("action"))) {
            Integer sbd = null;
            try {
                if (request.getParameter("sbd") != null) {
                    sbd = Integer.parseInt(request.getParameter("sbd").trim());
                }
            } catch (NumberFormatException e) {}
            
            if (sbd == null) {
                response.sendRedirect(request.getContextPath() + "/old_views/examiner/score-entry?error=noSbd");
                return;
            }

            if (!callService.finalizeScoreEntry(activeExamId, sbd, ((User) session.getAttribute("user")).getUserId()).isSuccess()) {
                response.sendRedirect(request.getContextPath() + "/old_views/examiner/score-entry?sbd="
                        + urlEncode(sbd) + "&error=finalizeFailed");
                return;
            }
            
            response.sendRedirect(request.getContextPath() + "/old_views/examiner/score-entry?finalized=1");
            return;
        }

        doGet(request, response);
    }

    private boolean handleScoreEntryAction(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, int activeExamId, String action, Integer sbd, User user,
            boolean isTheory, String sectionName) throws IOException {
        
        SectionType examSection = SectionType.fromValue(sectionName);
        String destination = resolveCallDestination(session);
        
        switch (action) {
            case "call" -> {
                if (sbd == null) {
                    response.sendRedirect(request.getContextPath() + "/old_views/examiner/score-entry?error=noCandidate");
                    return true;
                }
                
                if (!callService.callScoreEntryCandidate(activeExamId, sbd, user, user.getUserId(),
                        examSection, isTheory, sectionName, destination, true).isSuccess()) {
                    response.sendRedirect(request.getContextPath() + "/old_views/examiner/score-entry?error=callFailed&sbd="
                            + urlEncode(sbd));
                    return true;
                }
                
                response.sendRedirect(request.getContextPath() + "/old_views/examiner/score-entry?sbd="
                        + urlEncode(sbd) + "&scoreCalled=1");
                return true;
            }
            case "deferAbsent" -> {
                if (sbd == null) {
                    response.sendRedirect(request.getContextPath() + "/old_views/examiner/score-entry?error=noSbd");
                    return true;
                }
                
                callService.undoPresent(activeExamId, sbd, user.getUserId());
                
                response.sendRedirect(request.getContextPath() + "/old_views/examiner/score-entry?deferred="
                        + urlEncode(sbd));
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    private String urlEncode(int value) {
        return URLEncoder.encode(String.valueOf(value), StandardCharsets.UTF_8);
    }

    private String resolveSectionName(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object name = session.getAttribute("examSectionName");
        return name != null ? String.valueOf(name) : null;
    }

    private String resolveCallDestination(HttpSession session) {
        if (session == null) {
            return "Khu vực thi";
        }
        Object sectionName = session.getAttribute("examSectionName");
        if (sectionName != null && !String.valueOf(sectionName).isBlank()) {
            return String.valueOf(sectionName);
        }
        return "Khu vực thi";
    }
}
