package examstaff.controller.staff.exam;

import examstaff.controller.staff.exam.adapter.ExamStaffSelectionFacade;
import examstaff.controller.staff.exam.binder.CandidateDossierViewBinder;
import examstaff.dto.CandidateDossierViewDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import examstaff.controller.staff.exam.module.ExamStaffWebModule;
import examstaff.service.ExamStaffServices;
import examstaff.service.CandidateDossierService;

import java.io.IOException;

@WebServlet("/examstaff/candidate-dossier")
public class CandidateDossierServlet extends HttpServlet {

    private static final ExamStaffWebModule MODULE = ExamStaffWebModule.getInstance();

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
