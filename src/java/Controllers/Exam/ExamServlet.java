package Controllers.Exam;

import Constants.TheoryExamRules;
import DAO.CandidateExamDAO;
import DAO.Impl.CandidateExamDAOImpl;
import Models.CandidateExamContext;
import Models.ExamResultView;
import Models.Question;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


@WebServlet(urlPatterns = {
        "/exam/entrance", "/exam/info", "/exam/face",
        "/exam/questions", "/exam/submit", "/exam/result"
})
public class ExamServlet extends HttpServlet {

    private final CandidateExamDAO dao = new CandidateExamDAOImpl();
    private static final String CTX_KEY = "examCtx";
    private static final String RESULT_KEY = "examResultView";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String path = req.getServletPath();
        String ctx = req.getContextPath();

        switch (path) {
            case "/exam/entrance" ->
                req.getRequestDispatcher("/views/exam/exam-entrance.jsp").forward(req, resp);
            case "/exam/info" -> showInfo(req, resp);
            case "/exam/face" -> resp.sendRedirect(ctx + "/exam/questions"); // FaceID để sau
            case "/exam/questions" -> showQuestions(req, resp);
            case "/exam/result" -> showResult(req, resp);
            default -> resp.sendRedirect(ctx + "/exam/entrance");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String path = req.getServletPath();
        String ctx = req.getContextPath();

        switch (path) {
            case "/exam/entrance" -> doLogin(req, resp);
            case "/exam/face" -> {
                CandidateExamContext c = current(req);
                if (c == null) { resp.sendRedirect(ctx + "/exam/entrance"); return; }
                if (!"true".equals(req.getParameter("confirmed"))) {
                    resp.sendRedirect(ctx + "/exam/info?err=1"); return;
                }
                resp.sendRedirect(ctx + "/exam/questions");
            }
            case "/exam/submit" -> doSubmit(req, resp);
            default -> resp.sendRedirect(ctx + "/exam/entrance");
        }
    }

    // ---- Login bằng SBD (CandidateNumber) ----
    private void doLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        String sbd = req.getParameter("sbd");
        if (sbd == null || sbd.trim().isEmpty()) {
            req.setAttribute("error", "Vui lòng nhập số báo danh.");
            req.getRequestDispatcher("/views/exam/exam-entrance.jsp").forward(req, resp);
            return;
        }
        CandidateExamContext c = dao.findContextByCandidateNumber(sbd.trim());
        if (c == null) {
            req.setAttribute("error", "Không tìm thấy thí sinh với SBD này, hoặc chưa được phân ca thi.");
            req.getRequestDispatcher("/views/exam/exam-entrance.jsp").forward(req, resp);
            return;
        }
        // Reset mọi trạng thái phiên thi cũ khi có SBD mới
        HttpSession s = req.getSession(true);
        s.removeAttribute(RESULT_KEY);
        s.setAttribute(CTX_KEY, c);
        resp.sendRedirect(req.getContextPath() + "/exam/info");
    }

    private void showInfo(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        CandidateExamContext c = current(req);
        if (c == null) { resp.sendRedirect(req.getContextPath() + "/exam/entrance"); return; }
        req.setAttribute("cand", c);
        req.getRequestDispatcher("/views/exam/exam-candidate-info.jsp").forward(req, resp);
    }

    // ---- Bắt đầu làm bài: tạo paper + bốc đề (1 lần, cache trong session) ----
    private void showQuestions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        CandidateExamContext c = current(req);
        if (c == null) { resp.sendRedirect(req.getContextPath() + "/exam/entrance"); return; }

        if (c.getQuestions() == null || c.getQuestions().isEmpty()) {
            TheoryExamRules.Rule rule = TheoryExamRules.resolve(c.getLicenceClass());
            int device = dao.findAssignedDevice(c.getExamCandidateId(), c.getSessionId());
            int paperId = dao.startTheoryPaper(c.getExamCandidateId(), device);

            c.setDeviceId(device);
            c.setTheoryPaperId(paperId);
            c.setNumQuestions(rule.numQuestions);
            c.setPassThreshold(rule.passThreshold);
            c.setDurationMinutes(rule.durationMinutes);
            c.setQuestions(dao.loadRandomQuestions(c.getLicenceId(), rule.numQuestions));
            c.setStartedAtMillis(System.currentTimeMillis());
            req.getSession().setAttribute(CTX_KEY, c);
        }

        if (c.getQuestions() == null || c.getQuestions().isEmpty()) {
            req.setAttribute("error", "Không tải được đề thi cho hạng " + c.getLicenceClass()
                    + ". Vui lòng báo giám thị kiểm tra dữ liệu ngân hàng câu hỏi.");
            req.setAttribute("cand", c);
            req.getRequestDispatcher("/views/exam/exam-candidate-info.jsp").forward(req, resp);
            return;
        }

        long elapsed = (System.currentTimeMillis() - c.getStartedAtMillis()) / 1000;
        long remaining = Math.max(c.getDurationMinutes() * 60L - elapsed, 0);
        req.setAttribute("questions", c.getQuestions());
        req.setAttribute("durationSeconds", remaining);
        req.setAttribute("totalQuestions", c.getQuestions().size());
        req.setAttribute("passThreshold", c.getPassThreshold());
        req.getRequestDispatcher("/views/exam/exam-questions.jsp").forward(req, resp);
    }

    private void doSubmit(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        CandidateExamContext c = current(req);
        if (c == null || c.getQuestions() == null) {
            resp.sendRedirect(req.getContextPath() + "/exam/entrance"); return;
        }
        Map<Integer, String> answers = new HashMap<>();
        for (Question q : c.getQuestions()) {
            String v = req.getParameter("ans_" + q.getQuestionId());
            if (v != null && !v.isBlank()) answers.put(q.getQuestionId(), v.trim());
        }
        ExamResultView view = dao.submitAndGrade(
                c.getTheoryPaperId(), c.getExamCandidateId(), c.getQuestions(), answers, c.getPassThreshold());
        view.setStartTime(fmtClock(c.getStartedAtMillis()));
        view.setEndTime(fmtClock(System.currentTimeMillis()));

        c.setQuestions(null);
        req.getSession().setAttribute(CTX_KEY, c);
        req.getSession().setAttribute(RESULT_KEY, view);
        resp.sendRedirect(req.getContextPath() + "/exam/result");
    }

    private void showResult(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        CandidateExamContext c = current(req);
        ExamResultView view = (ExamResultView) req.getSession().getAttribute(RESULT_KEY);
        if (c == null || view == null) { resp.sendRedirect(req.getContextPath() + "/exam/entrance"); return; }
        req.setAttribute("candidate", c);
        req.setAttribute("result", view);
        req.getRequestDispatcher("/views/exam/exam-results.jsp").forward(req, resp);
    }

    private CandidateExamContext current(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        return s == null ? null : (CandidateExamContext) s.getAttribute(CTX_KEY);
    }

    private static String fmtClock(long millis) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTimeInMillis(millis);
        return String.format("%02dh%02d", cal.get(java.util.Calendar.HOUR_OF_DAY),
                cal.get(java.util.Calendar.MINUTE));
    }
}