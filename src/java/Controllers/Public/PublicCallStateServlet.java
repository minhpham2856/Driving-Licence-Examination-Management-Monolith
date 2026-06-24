package Controllers.Public;

import Utils.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;

@WebServlet("/api/public-call/state")
public class PublicCallStateServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        PublicCallHelper.Snapshot snapshot = PublicCallHelper.loadSnapshot(request);

        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");

        StringBuilder json = new StringBuilder(512);
        json.append('{');
        JsonUtil.appendJsonField(json, "sessionId", snapshot.getSessionId(), true);
        JsonUtil.appendJsonField(json, "isCallingActive", snapshot.isCallingActive(), true);
        JsonUtil.appendJsonField(json, "shiftEnded", snapshot.isShiftEnded(), true);
        JsonUtil.appendJsonField(json, "updatedAtMs", snapshot.getUpdatedAtMs(), true);

        if (snapshot.getCurrentSession() != null && snapshot.getCurrentSession().getExamDate() != null) {
            String examDate = new SimpleDateFormat("dd/MM/yyyy")
                    .format(snapshot.getCurrentSession().getExamDate());
            JsonUtil.appendJsonField(json, "examDate", examDate, true);
        } else {
            json.append("\"examDate\":null,");
        }

        json.append("\"calling\":");
        JsonUtil.appendCandidateJson(json, snapshot.getCallingCandidate());
        json.append(',');
        json.append("\"next\":");
        JsonUtil.appendCandidateJson(json, snapshot.getNextCandidate());
        json.append('}');

        response.getWriter().write(json.toString());
    }
}
