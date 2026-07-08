package controller.pub;

import controller.pub.support.PublicCallJsonBinder;
import controller.staff.exam.support.ExamStaffHttpSupport;
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
import util.Utf8EncodingHelper;

import java.io.IOException;

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
        int sessionId = ExamStaffHttpSupport.resolveActiveSessionId(request);
        CallBoardRepository repository = new ServletContextCallBoardRepository(getServletContext());
        CallBoardState board = callBoardSyncService.getState(repository, sessionId);
        PublicCallSnapshotDTO snapshot = publicCallQueryService.loadSnapshot(
                sessionId, request.getServletContext().getRealPath("/"), board);

        Utf8EncodingHelper.applyJson(response);
        response.setHeader("Cache-Control", "no-store");
        response.getWriter().write(PublicCallJsonBinder.toStateJson(snapshot));
    }
}
