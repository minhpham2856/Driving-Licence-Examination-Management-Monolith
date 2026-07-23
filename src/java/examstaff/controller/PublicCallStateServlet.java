package examstaff.controller;

import examstaff.dao.CallBoardDAO;
import examstaff.dto.CallBoardState;
import examstaff.dto.PublicCallSnapshotDTO;
import examstaff.service.ExamStaffViewService;
import examstaff.service.StaffCallService;
import examstaff.service.impl.ExamStaffViewServiceImpl;
import examstaff.service.impl.StaffCallServiceImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * API JSON trạng thái Public Call (poll từ {@code public-call.js}).
 *
 * Luồng GET (mỗi lần poll ~1–2s):
 * - Lấy {@link CallBoardDAO} singleton (in-memory)
 * - Resolve {@code examId}: query {@code ?examId=} → session selected → {@code dao.activeExamId}
 * - Đọc {@link CallBoardState} runtime (calling / desk / pause / queue order)
 * - Ghép với danh sách thí sinh DB → {@link PublicCallSnapshotDTO}
 * - Serialize JSON ({@link PublicCallSnapshotSupport#toStateJson}), header {@code Cache-Control: no-store}
 * <p>Endpoint này <b>không</b> bắt buộc session examstaff (TV/kiosk có thể poll);
 * màn JSP {@code /examstaff/public-call} vẫn qua filter role.
 */
@WebServlet("/api/public-call/state")
public class PublicCallStateServlet extends HttpServlet {

    private final StaffCallService staffCall = new StaffCallServiceImpl();
    private final ExamStaffViewService viewService = new ExamStaffViewServiceImpl();

    /**
     * GET: trả JSON state (no-store) cho client poll.
     * @throws ServletException không dùng
     * @throws IOException      lỗi ghi body
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        CallBoardDAO dao = ExamStaffHttpSupport.callBoardDao(getServletContext());
        // URL / session / board → kỳ đang chiếu
        int examId = viewService.resolveActiveExamId(
                ExamStaffHttpSupport.parseExamIdParam(request),
                ExamStaffHttpSupport.readSelectedExamId(request),
                staffCall.getActiveCallExamId(dao));
        CallBoardState board = staffCall.getBoardState(dao, examId);
        PublicCallSnapshotDTO snapshot = staffCall.loadPublicSnapshot(examId, board);

        Utf8EncodingHelper.applyJson(response);
        response.setHeader("Cache-Control", "no-store");
        response.getWriter().write(PublicCallSnapshotSupport.toStateJson(snapshot));
    }
}
