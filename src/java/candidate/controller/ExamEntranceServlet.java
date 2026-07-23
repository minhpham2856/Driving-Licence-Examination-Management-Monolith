package candidate.controller;

import candidate.dto.CandidateExamContextDTO;
import candidate.dto.CandidateExamResultDTO;
import candidate.service.CandidateExamAccessService;
import candidate.service.impl.CandidateExamAccessServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import shared.Attributes;
import shared.model.Question;

@WebServlet(urlPatterns = {
    "/exam/login",
    "/exam/entrance",
    "/exam/info",
    "/exam/questions",
    "/exam/submit",
    "/exam/result"
})
public class ExamEntranceServlet extends HttpServlet {

    public static final String CANDIDATE_EXAM_CONTEXT = "candidateExamContext";
    private static final String CANDIDATE_EXAM_RESULT = "candidateExamResult";
    private static final String FAILURE_COUNT = "candidateEntranceFailureCount";
    private static final String LOCKED_UNTIL = "candidateEntranceLockedUntil";
    private static final int MAX_FAILURES = 5;
    private static final long LOCK_MILLIS = 60_000L;

    private final CandidateExamAccessService accessService = new CandidateExamAccessServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        if ("/exam/login".equals(path)) {
            request.getRequestDispatcher("/views/exam/exam-login.jsp").forward(request, response);
            return;
        }
        if (!hasActiveExamSession(request)) {
            forward403(request, response);
            return;
        }
        if ("/exam/entrance".equals(path)) {
            request.getRequestDispatcher("/views/exam/exam-entrance.jsp").forward(request, response);
            return;
        }

        CandidateExamContextDTO context = current(request);
        if (context == null) {
            response.sendRedirect(request.getContextPath() + "/exam/entrance");
            return;
        }
        if ("/exam/info".equals(path)) {
            request.setAttribute("candidateExam", context);
            request.getRequestDispatcher("/views/exam/exam-candidate-info.jsp").forward(request, response);
            return;
        }
        if ("/exam/questions".equals(path)) {
            if (context.getQuestions() == null && !accessService.start(context)) {
                response.sendError(HttpServletResponse.SC_CONFLICT, "Không thể tạo đề thi.");
                return;
            }
            request.setAttribute("candidateExam", context);
            request.setAttribute("questions", context.getQuestions());
            request.setAttribute("durationSeconds", context.getDurationMinutes() * 60);
            request.getRequestDispatcher("/views/exam/exam-questions.jsp").forward(request, response);
            return;
        }

        CandidateExamResultDTO result = (CandidateExamResultDTO) request.getSession()
                .getAttribute(CANDIDATE_EXAM_RESULT);
        if (result == null) {
            response.sendRedirect(request.getContextPath() + "/exam/questions");
            return;
        }
        request.setAttribute("result", result);
        request.getRequestDispatcher("/views/exam/exam-results.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        if ("/exam/login".equals(path)) {
            loginExam(request, response);
            return;
        }
        if (!hasActiveExamSession(request)) {
            forward403(request, response);
            return;
        }
        if ("/exam/submit".equals(path)) {
            submitExam(request, response);
            return;
        }
        authenticateCandidate(request, response);
    }

    private void loginExam(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String examCodeOrId = trim(firstNonBlank(request.getParameter("examCode"), request.getParameter("examId")));
        String examPassword = trim(request.getParameter("examPassword"));
        int examId = accessService.loginExam(examCodeOrId, examPassword);
        if (examId <= 0) {
            showLoginError(request, response);
            return;
        }

        HttpSession session = request.getSession(true);
        session.setAttribute(Attributes.Session.ACTIVE_CANDIDATE_EXAM_ID, examId);
        session.removeAttribute(CANDIDATE_EXAM_CONTEXT);
        session.removeAttribute(CANDIDATE_EXAM_RESULT);
        session.removeAttribute(FAILURE_COUNT);
        session.removeAttribute(LOCKED_UNTIL);
        response.sendRedirect(request.getContextPath() + "/exam/entrance");
    }

    private void authenticateCandidate(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(true);
        Integer examId = activeExamId(session);
        if (examId == null) {
            forward403(request, response);
            return;
        }

        long now = System.currentTimeMillis();
        Long lockedUntil = (Long) session.getAttribute(LOCKED_UNTIL);
        if (lockedUntil != null && lockedUntil > now) {
            showEntranceError(request, response);
            return;
        }

        CandidateExamContextDTO context = accessService.authenticate(examId, request.getParameter("sbd"));
        if (context == null) {
            registerFailure(session, now);
            showEntranceError(request, response);
            return;
        }

        session.removeAttribute(FAILURE_COUNT);
        session.removeAttribute(LOCKED_UNTIL);
        session.removeAttribute(CANDIDATE_EXAM_RESULT);
        session.setAttribute(CANDIDATE_EXAM_CONTEXT, context);
        response.sendRedirect(request.getContextPath() + "/exam/info");
    }

    private void submitExam(HttpServletRequest request, HttpServletResponse response) throws IOException {
        CandidateExamContextDTO context = current(request);
        if (context == null || context.getQuestions() == null) {
            response.sendRedirect(request.getContextPath() + "/exam/entrance");
            return;
        }
        Map<Integer, String> answers = new HashMap<>();
        for (Question question : context.getQuestions()) {
            String answer = request.getParameter("ans_" + question.getQuestionId());
            if (answer != null) {
                answers.put(question.getQuestionId(), answer);
            }
        }
        CandidateExamResultDTO result = accessService.submit(context, answers);
        if (result == null) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        context.setQuestions(null);
        request.getSession().setAttribute(CANDIDATE_EXAM_RESULT, result);
        response.sendRedirect(request.getContextPath() + "/exam/result");
    }

    private void registerFailure(HttpSession session, long now) {
        Integer current = (Integer) session.getAttribute(FAILURE_COUNT);
        int failures = current == null ? 1 : current + 1;
        if (failures >= MAX_FAILURES) {
            session.setAttribute(LOCKED_UNTIL, now + LOCK_MILLIS);
            failures = 0;
        }
        session.setAttribute(FAILURE_COUNT, failures);
    }

    private boolean hasActiveExamSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return activeExamId(session) != null;
    }

    private Integer activeExamId(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute(Attributes.Session.ACTIVE_CANDIDATE_EXAM_ID);
        return value instanceof Integer && ((Integer) value) > 0 ? (Integer) value : null;
    }

    private CandidateExamContextDTO current(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null
                : (CandidateExamContextDTO) session.getAttribute(CANDIDATE_EXAM_CONTEXT);
    }

    private void showLoginError(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("error", "Mã kỳ thi hoặc mật khẩu không hợp lệ.");
        request.getRequestDispatcher("/views/exam/exam-login.jsp").forward(request, response);
    }

    private void showEntranceError(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("error", "Số báo danh không hợp lệ hoặc thí sinh chưa đủ điều kiện vào thi.");
        request.getRequestDispatcher("/views/exam/exam-entrance.jsp").forward(request, response);
    }

    private void forward403(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        request.getRequestDispatcher("/views/general/error-403.jsp").forward(request, response);
    }

    private String firstNonBlank(String first, String second) {
        String trimmed = trim(first);
        if (trimmed != null) {
            return trimmed;
        }
        return trim(second);
    }

    private String trim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
