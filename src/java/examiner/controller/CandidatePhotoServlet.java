package examiner.controller;

import examiner.dto.CandidateRowDTO;
import examiner.filter.ExaminerFilter;
import examiner.service.ExamViewService;
import examiner.service.impl.ExamViewServiceImpl;
import examiner.util.CandidatePhotoFiles;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import shared.Attributes;
import shared.enums.SectionType;

// Streams a candidate portrait for examiner print/detail pages by SBD.
@WebServlet("/examiner/candidate-photo")
public class CandidatePhotoServlet extends HttpServlet {

    private final ExamViewService viewService = new ExamViewServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String sbdRaw = request.getParameter("sbd");
        if (sbdRaw == null || sbdRaw.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        Integer activeExamId = (Integer) session.getAttribute(Attributes.Examiner.ACTIVE_EXAM_ID);
        if (activeExamId == null || activeExamId <= 0) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        int sbd;
        try {
            sbd = Integer.parseInt(sbdRaw.trim());
        } catch (NumberFormatException ex) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        if (sbd <= 0) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        SectionType sectionType = ExaminerFilter.resolveSectionType(session);
        CandidateRowDTO candidate = viewService.getCandidateViewRow(activeExamId, sbd, sectionType);
        if (candidate == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String photoUrl = candidate.getPhotoImageUrl();
        if (photoUrl == null || photoUrl.isBlank()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        if (CandidatePhotoFiles.isRemoteUrl(photoUrl)) {
            response.sendRedirect(photoUrl.trim());
            return;
        }

        File photoFile = CandidatePhotoFiles.findPhotoFile(photoUrl);
        if (photoFile == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String contentType = Files.probeContentType(photoFile.toPath());
        if (contentType == null || contentType.isBlank()) {
            contentType = "image/jpeg";
        }
        response.setContentType(contentType);
        response.setHeader("Cache-Control", "private, max-age=300");
        Files.copy(photoFile.toPath(), response.getOutputStream());
    }
}
