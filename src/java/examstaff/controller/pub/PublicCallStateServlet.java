package examstaff.controller.pub;

import examstaff.controller.pub.binder.PublicCallJsonBinder;
import examstaff.controller.staff.exam.adapter.CallBoardHttpFacade;
import examstaff.controller.staff.exam.http.ExamStaffHttpSupport;
import examstaff.dto.PublicCallSnapshotDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import examstaff.dto.view.CallBoardState;
import examstaff.controller.staff.exam.module.ExamStaffWebModule;
import examstaff.service.ExamStaffServices;
import examstaff.service.PublicCallQueryService;
import examstaff.util.Utf8EncodingHelper;

import java.io.IOException;

/**
 * API JSON trạng thái Public Call (poll từ {@code public-call.js}).
 * Cùng nguồn dữ liệu với {@link examstaff.controller.staff.exam.PublicCallServlet}, khác output.
 */
@WebServlet("/api/public-call/state")
public class PublicCallStateServlet extends HttpServlet {

    private static final ExamStaffWebModule MODULE = ExamStaffWebModule.getInstance();

    private static final ExamStaffServices SERVICES = MODULE.services();

    private final PublicCallQueryService publicCallQueryService;
    private final CallBoardHttpFacade callBoardHttp = MODULE.callBoardHttp();

    /** Constructor mặc định — lấy service từ composition root. */
    public PublicCallStateServlet() {
        this(SERVICES.publicCallQuery());
    }

    /**
     * Constructor inject (test / wiring tay).
     *
     * @param publicCallQueryService service dựng snapshot Public Call
     */
    PublicCallStateServlet(PublicCallQueryService publicCallQueryService) {
        this.publicCallQueryService = publicCallQueryService;
    }

    /**
     * Trả JSON no-store: calling, next, waitingQueue, deskBusy, pause/end flags.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int examId = SERVICES.selection().resolveActiveExamId(
                ExamStaffHttpSupport.parseExamIdParam(request),
                ExamStaffHttpSupport.readSelectedExamId(request),
                callBoardHttp.dao(getServletContext()).getActiveExamId());
        CallBoardState board = callBoardHttp.getState(getServletContext(), examId);
        PublicCallSnapshotDTO snapshot = publicCallQueryService.loadSnapshot(
                examId, request.getServletContext().getRealPath("/"), board);

        Utf8EncodingHelper.applyJson(response);
        response.setHeader("Cache-Control", "no-store");
        response.getWriter().write(PublicCallJsonBinder.toStateJson(snapshot));
    }
}
