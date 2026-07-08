package controller.staff.exam;

import dto.examstaff.CandidatePhotoStreamDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.CandidatePhotoLookupService;
import service.impl.CandidatePhotoLookupServiceImpl;

import java.io.IOException;
import java.nio.file.Files;

@WebServlet("/views/staff/examstaff/candidate-photo")
public class CandidatePhotoServlet extends HttpServlet {

    private final CandidatePhotoLookupService photoLookupService = new CandidatePhotoLookupServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String sbd = request.getParameter("sbd");
        if (sbd == null || sbd.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        int examId = ExamStaffViewHelper.resolveExamId(request, request.getSession(), null, 0);
        int sessionId = ExamStaffViewHelper.resolveSessionId(request, request.getSession(), null, 0);
        CandidatePhotoStreamDTO photo = photoLookupService.resolvePhoto(
                request.getServletContext().getRealPath("/"), examId, sessionId, sbd.trim());

        if (photo.getStatus() != CandidatePhotoStreamDTO.Status.FOUND) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.setContentType(photo.getContentType());
        response.setHeader("Cache-Control", "private, max-age=300");
        Files.copy(photo.getPhotoFile().toPath(), response.getOutputStream());
    }
}
