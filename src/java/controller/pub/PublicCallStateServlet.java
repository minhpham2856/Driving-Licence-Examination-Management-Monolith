package controller.pub;

import controller.pub.support.PublicCallJsonBinder;
import dto.examstaff.PublicCallSnapshotDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.view.CallBoardState;
import repository.CallBoardRepository;
import repository.ServletContextCallBoardRepository;
import service.CallBoardSyncService;
import service.ExamStaffServices;
import service.PublicCallQueryService;
import util.JsonUtil;
import util.Utf8EncodingUtil;

import java.io.IOException;
import java.text.SimpleDateFormat;

@WebServlet("/api/public-call/state")
public class PublicCallStateServlet extends HttpServlet {

    private final PublicCallQueryService publicCallQueryService;
    private final CallBoardSyncService callBoardSyncService;

    public PublicCallStateServlet() {
        this(ExamStaffServices.get().publicCallQuery(), ExamStaffServices.get().callBoardSync());
    }

    PublicCallStateServlet(PublicCallQueryService publicCallQueryService,
            CallBoardSyncService callBoardSyncService) {
        this.publicCallQueryService = publicCallQueryService;
        this.callBoardSyncService = callBoardSyncService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int sessionId = resolveActiveSessionId(request);
        CallBoardRepository repository = new ServletContextCallBoardRepository(getServletContext());
        CallBoardState board = callBoardSyncService.getState(repository, sessionId);
        PublicCallSnapshotDTO snapshot = publicCallQueryService.loadSnapshot(
                sessionId, request.getServletContext().getRealPath("/"), board);

        Utf8EncodingUtil.applyJson(response);
        response.setHeader("Cache-Control", "no-store");
        response.getWriter().write(toStateJson(snapshot));
    }

    private int resolveActiveSessionId(HttpServletRequest request) {
        if (request == null) {
            return 0;
        }
        String[] values = request.getParameterValues("sessionId");
        if (values == null || values.length == 0) {
            values = request.getParameterValues("examSessionId");
        }
        if (values != null && values.length > 0) {
            for (int i = values.length - 1; i >= 0; i--) {
                if (values[i] == null || values[i].isBlank()) {
                    continue;
                }
                try {
                    int parsed = Integer.parseInt(values[i].trim());
                    if (parsed > 0) {
                        return parsed;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object selected = session.getAttribute("selectedSessionId");
            if (selected instanceof Integer id && id > 0) {
                return id;
            }
        }
        CallBoardRepository repository = new ServletContextCallBoardRepository(request.getServletContext());
        Integer active = repository.getActiveSessionId();
        return active != null ? active : 0;
    }

    private String toStateJson(PublicCallSnapshotDTO snapshot) {
        if (snapshot == null) {
            return "{}";
        }

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
        return json.toString();
    }
}
