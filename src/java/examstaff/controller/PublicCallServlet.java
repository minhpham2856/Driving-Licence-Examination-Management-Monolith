package examstaff.controller;

import examstaff.controller.PublicCallViewBinder;
import examstaff.controller.CallBoardHttpFacade;
import examstaff.controller.ExamStaffHttpSupport;
import examstaff.dto.PublicCallSnapshotDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import examstaff.dto.CallBoardState;
import examstaff.controller.ExamStaffWebModule;
import examstaff.service.ExamStaffServices;
import examstaff.service.PublicCallQueryService;
import examstaff.util.Utf8EncodingHelper;

import java.io.IOException;

@WebServlet("/examstaff/public-call")
public class PublicCallServlet extends HttpServlet {

    private static final ExamStaffWebModule MODULE = ExamStaffWebModule.getInstance();

    private static final ExamStaffServices SERVICES = MODULE.services();

    private final PublicCallQueryService publicCallQueryService;
    private final CallBoardHttpFacade callBoardHttp = MODULE.callBoardHttp();

    public PublicCallServlet() {
        this(SERVICES.publicCallQuery());
    }

    PublicCallServlet(PublicCallQueryService publicCallQueryService) {
        this.publicCallQueryService = publicCallQueryService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Utf8EncodingHelper.apply(request, response);

        int examId = SERVICES.selection().resolveActiveExamId(
                ExamStaffHttpSupport.parseExamIdParam(request),
                ExamStaffHttpSupport.readSelectedExamId(request),
                callBoardHttp.dao(getServletContext()).getActiveExamId());
        CallBoardState board = callBoardHttp.getState(getServletContext(), examId);
        PublicCallSnapshotDTO snapshot = publicCallQueryService.loadSnapshot(
                examId, request.getServletContext().getRealPath("/"), board);

        PublicCallViewBinder.bind(request, snapshot);
        request.getRequestDispatcher("/views/public/public-call.jsp").forward(request, response);
    }
}
