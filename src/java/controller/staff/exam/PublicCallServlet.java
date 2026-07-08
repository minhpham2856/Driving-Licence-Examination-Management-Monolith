package controller.staff.exam;

import dto.examstaff.PublicCallSnapshotDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.view.CallBoardState;
import repository.CallBoardRepository;
import repository.ServletContextCallBoardRepository;
import service.CallBoardSyncService;
import service.ExamStaffServices;
import service.PublicCallQueryService;
import util.Utf8EncodingUtil;

import java.io.IOException;

@WebServlet("/views/public/public-call")
public class PublicCallServlet extends HttpServlet {

    private final PublicCallQueryService publicCallQueryService;
    private final CallBoardSyncService callBoardSyncService;

    public PublicCallServlet() {
        this(ExamStaffServices.get().publicCallQuery(), ExamStaffServices.get().callBoardSync());
    }

    PublicCallServlet(PublicCallQueryService publicCallQueryService,
            CallBoardSyncService callBoardSyncService) {
        this.publicCallQueryService = publicCallQueryService;
        this.callBoardSyncService = callBoardSyncService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Utf8EncodingUtil.apply(request, response);

        int sessionId = BaseExamStaffServlet.resolveActiveSessionId(request);
        CallBoardRepository repository = new ServletContextCallBoardRepository(getServletContext());
        CallBoardState board = callBoardSyncService.getState(repository, sessionId);
        PublicCallSnapshotDTO snapshot = publicCallQueryService.loadSnapshot(
                sessionId, request.getServletContext().getRealPath("/"), board);

        boolean hasSession = snapshot.getSessionId() > 0;
        request.setAttribute("noActiveSession", !hasSession);
        request.setAttribute("sessionId", hasSession ? snapshot.getSessionId() : null);
        request.setAttribute("currentSession", snapshot.getCurrentSession());
        request.setAttribute("callingCandidate", snapshot.getCallingCandidate());
        request.setAttribute("nextCandidate", snapshot.getNextCandidate());
        request.setAttribute("isCallingActive", snapshot.isCallingActive());
        request.setAttribute("shiftEnded", snapshot.isShiftEnded());
        request.setAttribute("waitingQueue", snapshot.getWaitingQueue());

        request.getRequestDispatcher("/views/public/public-call.jsp").forward(request, response);
    }
}
