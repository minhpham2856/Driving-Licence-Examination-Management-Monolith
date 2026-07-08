package controller.exam;

import dto.ServiceResult;
import dto.TheoryEntranceDTO;
import dto.TheorySubmitDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Question;
import service.TheoryService;
import service.impl.TheoryServiceImpl;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = {
    "/exam/entrance",
    "/exam/info",
    "/exam/face",
    "/exam/questions",
    "/exam/save",
    "/exam/submit",
    "/exam/results"
})
public class ExamFlowServlet extends HttpServlet {

    private final TheoryService theoryService = new TheoryServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = stripPath(request);
        switch (path) {
            case "/exam/entrance" -> {
                request.getRequestDispatcher("/views/exam/exam-entrance.jsp").forward(request, response);
            }
            case "/exam/info" -> forwardInfo(request, response);
            case "/exam/face" -> forwardFace(request, response);
            case "/exam/questions" -> forwardQuestions(request, response);
            case "/exam/results" -> forwardResults(request, response);
            default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = stripPath(request);
        switch (path) {
            case "/exam/entrance" -> handleEntrancePost(request, response);
            case "/exam/face" -> handleFacePost(request, response);
            case "/exam/save" -> handleSavePost(request, response);
            case "/exam/submit" -> handleSubmitPost(request, response);
            default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void handleEntrancePost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int sbd = parseSbd(request.getParameter("sbd"));
        ServiceResult<TheoryEntranceDTO> result = theoryService.validateEntrance(sbd);
        if (!result.isSuccess()) {
            String errorCode = result.getData() != null ? result.getData().getErrorCode() : "error";
            response.sendRedirect(buildUrl(request, "/exam/entrance")
                    + "?error=" + urlEncode(errorCode)
                    + "&msg=" + urlEncode(result.getMessage())
                    + "&sbd=" + sbd);
            return;
        }
        TheoryEntranceDTO data = result.getData();
        HttpSession session = request.getSession(true);
        session.setAttribute("examSessionId", data.getSessionId());
        session.setAttribute("examSbd", data.getSbd());
        response.sendRedirect(buildUrl(request, "/exam/info"));
    }

    private void forwardInfo(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("examSbd") == null) {
            response.sendRedirect(buildUrl(request, "/exam/entrance"));
            return;
        }
        int sbd = (Integer) session.getAttribute("examSbd");
        ServiceResult<TheoryEntranceDTO> result = theoryService.validateEntrance(sbd);
        if (!result.isSuccess()) {
            String errorCode = result.getData() != null ? result.getData().getErrorCode() : "error";
            response.sendRedirect(buildUrl(request, "/exam/entrance")
                    + "?error=" + urlEncode(errorCode));
            return;
        }
        TheoryEntranceDTO data = result.getData();
        request.setAttribute("candidateName", data.getFullName());
        request.setAttribute("sbd", String.valueOf(data.getSbd()));
        request.setAttribute("dob", data.getDob());
        request.setAttribute("cccd", data.getGovIdNo());
        request.setAttribute("licenseClass", data.getLicenceClass());
        request.getRequestDispatcher("/views/exam/exam-candidate-info.jsp").forward(request, response);
    }

    private void forwardFace(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!requireExamSession(request, response)) {
            return;
        }
        request.getRequestDispatcher("/views/exam/exam-face.jsp").forward(request, response);
    }

    private void handleFacePost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect(buildUrl(request, "/exam/entrance"));
            return;
        }
        int sessionId = (Integer) session.getAttribute("examSessionId");
        int sbd = (Integer) session.getAttribute("examSbd");
        double rate = theoryService.scanFace(sessionId, sbd);
        response.sendRedirect(buildUrl(request, "/exam/questions") + "?faceMatch=" + urlEncode(String.valueOf(rate)));
    }

    private void forwardQuestions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect(buildUrl(request, "/exam/entrance"));
            return;
        }
        int sessionId = (Integer) session.getAttribute("examSessionId");
        int sbd = (Integer) session.getAttribute("examSbd");
        List<Question> questions = theoryService.loadExamQuestions(sessionId, sbd);
        request.setAttribute("questions", questions);
        request.setAttribute("totalQuestions", questions.size());
        request.setAttribute("faceMatchRate", request.getParameter("faceMatch"));
        String current = request.getParameter("currentQuestion");
        int currentQuestion = 1;
        if (current != null && !current.isBlank()) {
            try {
                currentQuestion = Integer.parseInt(current.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        request.setAttribute("currentQuestion", currentQuestion);
        request.getRequestDispatcher("/views/exam/exam-questions.jsp").forward(request, response);
    }

    private void handleSavePost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect(buildUrl(request, "/exam/entrance"));
            return;
        }
        int sessionId = (Integer) session.getAttribute("examSessionId");
        int sbd = (Integer) session.getAttribute("examSbd");
        Map<Integer, String> answers = parseAnswers(request);
        theoryService.saveDraftAnswers(sessionId, sbd, answers);
        response.sendRedirect(buildUrl(request, "/exam/questions") + "?saved=1");
    }

    private void handleSubmitPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect(buildUrl(request, "/exam/entrance"));
            return;
        }
        int sessionId = (Integer) session.getAttribute("examSessionId");
        int sbd = (Integer) session.getAttribute("examSbd");
        Map<Integer, String> answers = parseAnswers(request);
        ServiceResult<TheorySubmitDTO> result = theoryService.submitExam(sessionId, sbd, answers);
        if (!result.isSuccess()) {
            String errorCode = result.getData() != null ? result.getData().getErrorCode() : "error";
            response.sendRedirect(buildUrl(request, "/exam/entrance") + "?error=" + urlEncode(errorCode));
            return;
        }
        TheorySubmitDTO data = result.getData();
        session.setAttribute("lastSubmitCorrect", data.getCorrect());
        session.setAttribute("lastSubmitTotal", data.getTotal());
        session.setAttribute("lastSubmitPassed", data.isPassed());
        response.sendRedirect(buildUrl(request, "/exam/results"));
    }

    private void forwardResults(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect(buildUrl(request, "/exam/entrance"));
            return;
        }
        request.setAttribute("correctCount", session.getAttribute("lastSubmitCorrect"));
        request.setAttribute("totalQuestions", session.getAttribute("lastSubmitTotal"));
        request.setAttribute("passed", session.getAttribute("lastSubmitPassed"));
        request.getRequestDispatcher("/views/exam/exam-results.jsp").forward(request, response);
    }

    private Map<Integer, String> parseAnswers(HttpServletRequest request) {
        Map<Integer, String> answers = new HashMap<>();
        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            String key = entry.getKey();
            if (key.startsWith("q_")) {
                try {
                    int questionNo = Integer.parseInt(key.substring(2));
                    String[] values = entry.getValue();
                    if (values != null && values.length > 0 && values[0] != null && !values[0].isBlank()) {
                        answers.put(questionNo, values[0]);
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return answers;
    }

    private boolean requireExamSession(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("examSbd") == null) {
            response.sendRedirect(buildUrl(request, "/exam/entrance"));
            return false;
        }
        return true;
    }

    private int parseSbd(String raw) {
        if (raw == null || raw.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String stripPath(HttpServletRequest request) {
        return request.getServletPath();
    }

    private String buildUrl(HttpServletRequest request, String path) {
        return request.getContextPath() + path;
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
