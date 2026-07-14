package examstaff.controller.staff.exam;

import examstaff.dto.CandidatePhotoStreamDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import examstaff.controller.staff.exam.adapter.ExamStaffSelectionFacade;
import examstaff.controller.staff.exam.module.ExamStaffWebModule;
import examstaff.service.ExamStaffServices;
import examstaff.service.CandidatePhotoService;

import java.io.IOException;
import java.nio.file.Files;

/**
 * Stream ảnh thí sinh đã chụp theo SBD (binary response, không forward JSP).
 */
@WebServlet("/views/staff/examstaff/candidate-photo")
public class CandidatePhotoServlet extends HttpServlet {

    private static final ExamStaffWebModule MODULE = ExamStaffWebModule.getInstance();

    private static final ExamStaffServices SERVICES = MODULE.services();

    private final CandidatePhotoService photoService = SERVICES.photos();
    private final ExamStaffSelectionFacade selectionFacade = MODULE.selectionFacade();

    /**
     * GET ảnh: resolve file theo kỳ/SBD; 404 nếu thiếu; ngược lại ghi bytes + content-type.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String sbd = request.getParameter("sbd");
        if (sbd == null || sbd.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        int examId = selectionFacade.resolveExamId(request, request.getSession(), null, 0);
        CandidatePhotoStreamDTO photo = photoService.resolvePhoto(
                request.getServletContext().getRealPath("/"), examId, examId, sbd.trim());

        if (photo.getStatus() != CandidatePhotoStreamDTO.Status.FOUND) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.setContentType(photo.getContentType());
        response.setHeader("Cache-Control", "private, max-age=300");
        Files.copy(photo.getPhotoFile().toPath(), response.getOutputStream());
    }
}
