package controller.staff.exam;

import controller.staff.exam.support.CandidateDossierViewBinder;
import dto.examstaff.CandidateDossierViewDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.CandidateDossierService;
import service.impl.CandidateDossierServiceImpl;

import java.io.IOException;

@WebServlet("/views/staff/examstaff/candidate-dossier")
public class CandidateDossierServlet extends HttpServlet {

    private final CandidateDossierService dossierService = new CandidateDossierServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String sbd = request.getParameter("sbd");
        if (sbd == null || sbd.trim().isEmpty()) {
            response.sendRedirect("candidatecall");
            return;
        }

        int examId = ExamStaffViewHelper.ensureExamId(request, request.getSession(),
                ExamStaffViewHelper.loadAllSessions());
        CandidateDossierViewDTO view = dossierService.loadDossier(
                examId, sbd, request.getServletContext().getRealPath("/"));
        if (view.getProfile() == null) {
            response.sendRedirect("candidatecall");
            return;
        }

        boolean autoPrint = "true".equalsIgnoreCase(request.getParameter("print"));
        CandidateDossierViewBinder.bind(request, view, autoPrint);
        request.getRequestDispatcher("/views/staff/examstaff/candidate-dossier.jsp").forward(request, response);
    }
}
