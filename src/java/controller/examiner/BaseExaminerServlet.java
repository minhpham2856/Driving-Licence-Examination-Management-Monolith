package controller.examiner;

import dto.CandidateRowDTO;
import enums.ExamSection;
import enums.ExamSessionStatus;
import filter.ExaminerFilter;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.ExaminerSchedule;
import model.Session;
import model.User;
import service.ExamViewService;
import util.ExaminerCandidateSort;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

abstract class BaseExaminerServlet extends HttpServlet {

    protected HttpSession requireSession(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
        return session;
    }

    protected Integer getActiveSessionId(HttpSession session) {
        if (session == null) {
            return null;
        }
        return (Integer) session.getAttribute(ExaminerFilter.ATTR_ACTIVE_SESSION_ID);
    }

    protected Integer parseSbdParam(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            int sbd = Integer.parseInt(raw.trim());
            return sbd > 0 ? sbd : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    protected int[] parseSbdParams(String[] values) {
        if (values == null || values.length == 0) {
            return new int[0];
        }
        List<Integer> parsed = new ArrayList<>();
        for (String value : values) {
            Integer sbd = parseSbdParam(value);
            if (sbd != null) {
                parsed.add(sbd);
            }
        }
        int[] result = new int[parsed.size()];
        for (int i = 0; i < parsed.size(); i++) {
            result[i] = parsed.get(i);
        }
        return result;
    }

    protected String encodeSbd(int sbd) {
        return URLEncoder.encode(String.valueOf(sbd), StandardCharsets.UTF_8);
    }

    protected String stripContextPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String ctx = request.getContextPath();
        if (ctx != null && !ctx.isEmpty() && uri.startsWith(ctx)) {
            return uri.substring(ctx.length());
        }
        return uri;
    }

    protected static boolean isSessionEnded(String status) {
        return ExamSessionStatus.isEnded(status);
    }

    protected static void applyExaminerSessionContext(HttpSession httpSession, ExaminerSchedule schedule,
            Session session, ExamSection examSection) {
        if (httpSession == null || schedule == null || session == null) {
            return;
        }
        boolean isTheory = examSection == ExamSection.THEORY;
        schedule.setSession(session);
        httpSession.setAttribute(ExaminerFilter.ATTR_EXAMINER_SCHEDULE, schedule);
        httpSession.setAttribute(ExaminerFilter.ATTR_ACTIVE_SESSION_ID, session.getSessionId());
        httpSession.setAttribute(ExaminerFilter.ATTR_EXAM_SECTION, examSection);
        httpSession.setAttribute(ExaminerFilter.ATTR_EXAM_SECTION_NAME, examSection.getValue());
        httpSession.setAttribute(ExaminerFilter.ATTR_SECTION_THEORY, isTheory);
        httpSession.setAttribute(ExaminerFilter.ATTR_HAS_ACTIVE, Boolean.TRUE);
        httpSession.setAttribute(ExaminerFilter.ATTR_MESSAGE, null);
    }

    protected ExaminerSchedule getExaminerSchedule(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object scheduleObj = session.getAttribute(ExaminerFilter.ATTR_EXAMINER_SCHEDULE);
        if (scheduleObj instanceof ExaminerSchedule) {
            return (ExaminerSchedule) scheduleObj;
        }
        return null;
    }

    protected ExamSection getExamSection(HttpSession session) {
        if (session == null) {
            return ExamSection.THEORY;
        }
        Object sectionObj = session.getAttribute(ExaminerFilter.ATTR_EXAM_SECTION);
        if (sectionObj instanceof ExamSection) {
            return (ExamSection) sectionObj;
        }
        return ExamSection.THEORY;
    }

    protected String getSectionDisplayName(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object name = session.getAttribute(ExaminerFilter.ATTR_EXAM_SECTION_NAME);
        return name != null ? String.valueOf(name) : null;
    }

    protected String getCallDestination(HttpSession session) {
        if (session == null) {
            return "Khu vực thi";
        }
        ExaminerSchedule schedule = getExaminerSchedule(session);
        if (schedule != null && schedule.getExamArea() != null
                && schedule.getExamArea().getAreaName() != null
                && !schedule.getExamArea().getAreaName().isBlank()) {
            return schedule.getExamArea().getAreaName();
        }
        Object sectionName = session.getAttribute(ExaminerFilter.ATTR_EXAM_SECTION_NAME);
        if (sectionName != null && !String.valueOf(sectionName).isBlank()) {
            return String.valueOf(sectionName);
        }
        return "Khu vực thi thực hành";
    }

    protected static void applyModelAttributes(HttpServletRequest request, java.util.Map<String, Object> data) {
        if (data == null) {
            return;
        }
        for (java.util.Map.Entry<String, Object> entry : data.entrySet()) {
            request.setAttribute(entry.getKey(), entry.getValue());
        }
    }

    protected void applyCandidateListAttributes(HttpServletRequest request, HttpSession session,
            ExamViewService dataService, int sessionId, Integer sbd, String search) {
        boolean isTheory = ExaminerFilter.isTheorySession(session);
        String sectionName = getSectionDisplayName(session);
        List<CandidateRowDTO> candidates = dataService.loadCandidateRows(sessionId, isTheory, sectionName);
        if (search != null && !search.isBlank()) {
            candidates = dataService.filterCandidateRows(candidates, search);
            request.setAttribute("searchActive", true);
            request.setAttribute("searchQuery", search.trim());
        }
        request.setAttribute("candidates", candidates);
        request.setAttribute("candidateQueue", candidates);
        if (sbd != null && sbd > 0) {
            CandidateRowDTO candidate = dataService.getCandidateViewRow(sessionId, sbd, isTheory, sectionName);
            if (candidate != null) {
                request.setAttribute("candidate", candidate);
            }
        }
    }

    protected void applyCandidateSort(HttpServletRequest request, List<CandidateRowDTO> candidates) {
        if (candidates == null) {
            return;
        }
        ExaminerCandidateSort.Spec spec = ExaminerCandidateSort.parse(
                request.getParameter("sort"), request.getParameter("dir"));
        ExaminerCandidateSort.sort(candidates, spec);
        request.setAttribute("sortBy", spec.getColumn());
        request.setAttribute("sortDir", spec.isAscending() ? "asc" : "desc");
    }
}
