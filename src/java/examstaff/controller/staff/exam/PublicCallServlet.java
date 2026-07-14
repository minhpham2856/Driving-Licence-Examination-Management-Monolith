package examstaff.controller.staff.exam;

import examstaff.controller.pub.binder.PublicCallViewBinder;
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
 * Trang Public Call (JSP): resolve kỳ active → đọc CallBoard → snapshot → bind view.
 * Chỉ đọc; staff {@link CandidateCallServlet} mới ghi trạng thái gọi.
 */
@WebServlet("/views/public/public-call")
public class PublicCallServlet extends HttpServlet {

    private static final ExamStaffWebModule MODULE = ExamStaffWebModule.getInstance();

    private static final ExamStaffServices SERVICES = MODULE.services();

    private final PublicCallQueryService publicCallQueryService;
    private final CallBoardHttpFacade callBoardHttp = MODULE.callBoardHttp();

    /** Constructor mặc định — lấy service từ composition root. */
    public PublicCallServlet() {
        this(SERVICES.publicCallQuery());
    }

    /**
     * Constructor inject (test / wiring tay).
     *
     * @param publicCallQueryService service dựng snapshot Public Call
     */
    PublicCallServlet(PublicCallQueryService publicCallQueryService) {
        this.publicCallQueryService = publicCallQueryService;
    }

    /**
     * Render màn hình bảng gọi công khai.
     * examId: query/session → fallback active exam trên CallBoard.
     */
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
