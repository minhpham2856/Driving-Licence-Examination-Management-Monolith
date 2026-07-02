package controller.staff.exam;

import dto.exam.ExamRegistrationDTO;

import jakarta.servlet.ServletException;

import jakarta.servlet.annotation.WebServlet;

import jakarta.servlet.http.HttpServlet;

import jakarta.servlet.http.HttpServletRequest;

import jakarta.servlet.http.HttpServletResponse;

import java.io.File;

import java.io.IOException;

import java.nio.file.Files;

@WebServlet("/views/staff/examstaff/candidate-photo")

public class CandidatePhotoServlet extends HttpServlet {

    @Override

    // Xu ly yeu cau GET
    protected void doGet(HttpServletRequest request, HttpServletResponse response)

            throws ServletException, IOException {

        String sbd = request.getParameter("sbd");

        if (sbd == null || sbd.trim().isEmpty()) {

            response.sendError(HttpServletResponse.SC_NOT_FOUND);

            return;

        }

        ExamRegistrationDTO reg = ExamStaffViewHelper.resolveCandidateBySbd(

                request, request.getSession(), sbd.trim());

        if (reg == null || reg.getPhotoUrl() == null || reg.getPhotoUrl().isBlank()) {

            response.sendError(HttpServletResponse.SC_NOT_FOUND);

            return;

        }

        String webRoot = request.getServletContext().getRealPath("/");

        File file = CandidatePhotoHelper.findPhotoFile(request.getServletContext(), webRoot, reg.getPhotoUrl());

        if (file == null || !file.isFile()) {

            response.sendError(HttpServletResponse.SC_NOT_FOUND);

            return;

        }

        String name = file.getName().toLowerCase();

        String contentType = name.endsWith(".png") ? "image/png" : "image/jpeg";

        response.setContentType(contentType);

        response.setHeader("Cache-Control", "private, max-age=300");

        Files.copy(file.toPath(), response.getOutputStream());

    }

}
