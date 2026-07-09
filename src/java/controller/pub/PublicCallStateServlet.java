package controller.pub;

import controller.pub.binder.PublicCallJsonBinder;
import controller.pub.binder.PublicCallViewBinder;
import controller.staff.exam.adapter.CallBoardHttpFacade;
import controller.staff.exam.http.ExamStaffHttpSupport;
import dto.examstaff.PublicCallSnapshotDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.view.CallBoardState;
import controller.staff.exam.module.ExamStaffWebModule;
import service.ExamStaffServices;
import service.PublicCallQueryService;
import util.Utf8EncodingHelper;

import java.io.IOException;

@WebServlet("/api/public-call/state")
public class PublicCallStateServlet extends HttpServlet {

    private static final ExamStaffWebModule MODULE = new ExamStaffWebModule();

    private static final ExamStaffServices SERVICES = MODULE.services();

    private final PublicCallQueryService publicCallQueryService;
    private final CallBoardHttpFacade callBoardHttp = MODULE.callBoardHttp();

    public PublicCallStateServlet() {
        this(SERVICES.publicCallQuery());
    }

    PublicCallStateServlet(PublicCallQueryService publicCallQueryService) {
        this.publicCallQueryService = publicCallQueryService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int sessionId = SERVICES.selection().resolveActiveSessionId(
                ExamStaffHttpSupport.parseSessionIdParam(request),
                ExamStaffHttpSupport.readSelectedSessionId(request),
                callBoardHttp.dao(getServletContext()).getActiveSessionId());
        CallBoardState board = callBoardHttp.getState(getServletContext(), sessionId);
        PublicCallSnapshotDTO snapshot = publicCallQueryService.loadSnapshot(
                sessionId, request.getServletContext().getRealPath("/"), board);

        Utf8EncodingHelper.applyJson(response);
        response.setHeader("Cache-Control", "no-store");
        response.getWriter().write(PublicCallJsonBinder.toStateJson(snapshot));
    }
}
