package examstaff.controller;

import examstaff.dto.CandidatePhotoStreamDTO;
import examstaff.service.ExamStaffViewService;
import examstaff.service.impl.ExamStaffViewServiceImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.file.Files;

/**
 * Stream ảnh thí sinh đã chụp: resolve file theo SBD/kỳ → ghi binary response.
 *
 * Vai trò:
 * Endpoint binary phục vụ ảnh chụp thí sinh (JSP {@code &lt;img src="…/candidate-photo?sbd=…"&gt;}).
 * Không forward HTML; trả 404 khi thiếu SBD hoặc file không tồn tại.
 *
 * Luồng GET:
 * - Validate {@code sbd} → 404 nếu rỗng
 * - {@code resolveExamId} + {@code resolvePhoto} qua {@link ExamStaffViewService}
 * - FOUND → Content-Type + Cache-Control private → {@code Files.copy} stream
 *
 * Ai gọi:
 * JSP {@code candidatecall.jsp}, {@code procedure.jsp}, {@code candidate-dossier.jsp},
 * {@code public-call.jsp} (thumbnail thí sinh đang gọi).
 */
@WebServlet("/examstaff/candidate-photo")
public class CandidatePhotoServlet extends HttpServlet {

    private final ExamStaffViewService viewService = new ExamStaffViewServiceImpl();

    /**
     * GET: {@code sbd} bắt buộc → resolvePhoto → 404 nếu thiếu → stream Content-Type + file.
     * @throws ServletException không dùng
     * @throws IOException      lỗi đọc/ghi file
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String sbd = request.getParameter("sbd");
        if (sbd == null || sbd.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        int examId = ExamStaffPageSupport.resolveExamId(request, request.getSession(), null, 0, viewService);
        CandidatePhotoStreamDTO photo = viewService.resolvePhoto(examId, examId, sbd.trim());

        if (photo.getStatus() != CandidatePhotoStreamDTO.Status.FOUND) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.setContentType(photo.getContentType());
        response.setHeader("Cache-Control", "private, max-age=300");
        Files.copy(photo.getPhotoFile().toPath(), response.getOutputStream());
    }
}
