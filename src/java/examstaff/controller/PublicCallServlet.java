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
 * Màn hình gọi số công khai (TV): resolve kỳ active → load snapshot → bind → forward JSP.
 *
 * Khác với PublicCallStateServlet:
 * Servlet này render HTML lần đầu (public-call.jsp); JS trên trang sau đó
 * poll /api/public-call/state để cập nhật calling/next/queue mà không reload cả trang.
 * Cả hai đều đọc cùng CallBoardDAO in-memory + cùng StaffCallService.loadPublicSnapshot.
 *
 * Luồng GET:
 * UTF-8 → resolve examId (URL / session / board active) → getBoardState → snapshot → bind request → JSP.
 */
@WebServlet("/examstaff/public-call")
public class PublicCallServlet extends HttpServlet {

    private final StaffCallService staffCall = new StaffCallServiceImpl();
    private final ExamStaffViewService viewService = new ExamStaffViewServiceImpl();

    /**
     * GET: UTF-8 → resolve examId → board state → public snapshot → bind → public-call.jsp.
     * @throws ServletException lỗi forward
     * @throws IOException      lỗi I/O
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Utf8EncodingHelper.apply(request, response);

        // Resolve kỳ đang active: URL → session → board
        CallBoardDAO dao = ExamStaffHttpSupport.callBoardDao(getServletContext());
        int examId = viewService.resolveActiveExamId(
                ExamStaffHttpSupport.parseExamIdParam(request),
                ExamStaffHttpSupport.readSelectedExamId(request),
                staffCall.getActiveCallExamId(dao));
        CallBoardState board = staffCall.getBoardState(dao, examId);
        PublicCallSnapshotDTO snapshot = staffCall.loadPublicSnapshot(examId, board);

        PublicCallSnapshotSupport.bindRequest(request, snapshot);
        request.getRequestDispatcher("/views/public/public-call.jsp").forward(request, response);
    }
}
