package controller.pub;

import util.JsonUtil;
import util.Utf8EncodingHelper;
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

        Utf8EncodingHelper.applyJson(response);
        response.setHeader("Cache-Control", "no-store");

        StringBuilder json = new StringBuilder(512);
        json.append('{');
        JsonUtil.appendJsonField(json, "sessionId", snapshot.getSessionId(), true);
        JsonUtil.appendJsonField(json, "isCallingActive", snapshot.isCallingActive(), true);
        JsonUtil.appendJsonField(json, "deskBusy", snapshot.isDeskBusy(), true);
        JsonUtil.appendJsonField(json, "shiftEnded", snapshot.isShiftEnded(), true);
        JsonUtil.appendJsonField(json, "updatedAtMs", snapshot.getUpdatedAtMs(), true);

        if (snapshot.getCurrentSession() != null && snapshot.getCurrentSession().getExamDate() != null) {
            String examDate = new SimpleDateFormat("dd/MM/yyyy")
                    .format(snapshot.getCurrentSession().getExamDate());
            JsonUtil.appendJsonField(json, "examDate", examDate, true);
        } else {
            json.append("\"examDate\":null,");
        }

        if (snapshot.getDeskSbd() != null && !snapshot.getDeskSbd().isBlank()) {
            JsonUtil.appendJsonField(json, "deskSbd", snapshot.getDeskSbd(), true);
        } else {
            json.append("\"deskSbd\":null,");
        }

        json.append("\"calling\":");
        JsonUtil.appendCandidateJson(json, snapshot.getCallingCandidate());
        json.append(',');
        json.append("\"next\":");
        JsonUtil.appendCandidateJson(json, snapshot.getNextCandidate());
        json.append(',');
        json.append("\"waitingQueue\":");
        JsonUtil.appendCandidateArrayJson(json, snapshot.getWaitingQueue());
        json.append('}');

        response.getWriter().write(json.toString());
    }
}
