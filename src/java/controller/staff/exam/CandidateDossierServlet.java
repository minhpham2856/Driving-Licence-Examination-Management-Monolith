package controller.staff.exam;

import controller.staff.exam.adapter.ExamStaffSelectionFacade;
import controller.staff.exam.binder.CandidateDossierViewBinder;
import dto.examstaff.CandidateDossierViewDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import controller.staff.exam.module.ExamStaffWebModule;
import service.ExamStaffServices;
import service.CandidateDossierService;

import java.io.IOException;

@WebServlet("/views/staff/examstaff/candidate-dossier")
public class CandidateDossierServlet extends HttpServlet {

    private static final ExamStaffWebModule MODULE = new ExamStaffWebModule();

    private static final ExamStaffServices SERVICES = MODULE.services();

    private final CandidateDossierService dossierService = SERVICES.dossiers();
    private final ExamStaffSelectionFacade selectionFacade = MODULE.selectionFacade();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String sbd = request.getParameter("sbd");
        if (sbd == null || sbd.trim().isEmpty()) {
            response.sendRedirect("candidatecall");
            return;
        }

        int examId = selectionFacade.ensureExamId(request, request.getSession(),
                selectionFacade.loadAllExams());
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
