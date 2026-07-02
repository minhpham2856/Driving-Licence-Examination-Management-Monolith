package controller.staff.exam;

import dao.ExamSessionDAO;

import dao.FeeDAO;

import dao.PaymentDAO;

import dao.impl.ExamSessionDAOImpl;

import dao.impl.FeeDAOImpl;

import dao.impl.PaymentDAOImpl;

import dto.exam.ExamRegistrationDTO;

import dto.SessionDTO;

import model.Fee;

import model.Payment;

import util.ProcedureFeeTotals;

import jakarta.servlet.ServletException;

import jakarta.servlet.annotation.WebServlet;

import jakarta.servlet.http.HttpServlet;

import jakarta.servlet.http.HttpServletRequest;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import java.util.ArrayList;

import java.util.List;

@WebServlet("/views/staff/examstaff/candidate-dossier")

public class CandidateDossierServlet extends HttpServlet {

    private final ExamSessionDAO sessionDAO = new ExamSessionDAOImpl();

    private final FeeDAO feeDAO = new FeeDAOImpl();

    private final PaymentDAO payDAO = new PaymentDAOImpl();

    @Override

    // Xu ly yeu cau GET
    protected void doGet(HttpServletRequest request, HttpServletResponse response)

            throws ServletException, IOException {

        String sbd = request.getParameter("sbd");

        if (sbd == null || sbd.trim().isEmpty()) {

            response.sendRedirect("candidatecall");

            return;

        }

        ExamRegistrationDTO profile = ExamStaffViewHelper.resolveCandidateBySbd(

                request, request.getSession(), sbd.trim());

        if (profile == null) {

            response.sendRedirect("candidatecall");

            return;

        }

        String webRoot = request.getServletContext().getRealPath("/");

        CandidatePhotoHelper.normalizeQueue(webRoot, List.of(profile));

        boolean hasPhotoFile = CandidatePhotoHelper.findPhotoFile(

                request.getServletContext(), webRoot, profile.getPhotoUrl()) != null;

        request.setAttribute("hasPhotoFile", hasPhotoFile);

        SessionDTO examSession = sessionDAO.getById(profile.getExamSessionId());

        Payment payment = payDAO.getByCandidateId(profile.getId());

        String licenseCode = profile.getLicenseCode();

        if (licenseCode == null || licenseCode.isBlank()) {

            licenseCode = profile.getClazz();

        }

        boolean requiresRoadTest = profile.isRequiresRoadTest();

        List<Fee> feeLines = new ArrayList<>();

        boolean feesFromPayment = false;

        if (payment != null && payment.getPaymentId() > 0) {

            feeLines = feeDAO.getFeesByPaymentId(payment.getPaymentId());

            feesFromPayment = !feeLines.isEmpty();

        }

        if (feeLines.isEmpty()) {

            feeLines = feeDAO.getProcedureFees(licenseCode, requiresRoadTest);

            feesFromPayment = false;

        }

        double feeTotal = resolveFeeTotal(profile, payment, feeLines, licenseCode, requiresRoadTest);

        request.setAttribute("profile", profile);

        request.setAttribute("examSession", examSession);

        request.setAttribute("payment", payment);

        request.setAttribute("feeLines", feeLines);

        request.setAttribute("feeTotal", feeTotal);

        request.setAttribute("feesFromPayment", feesFromPayment);

        request.setAttribute("dossierTitle", DossierFormHelper.resolveTitle(licenseCode));

        request.setAttribute("dossierSubtitle", DossierFormHelper.resolveSubtitle(licenseCode));

        request.setAttribute("autoPrint", "true".equalsIgnoreCase(request.getParameter("print")));

        request.getRequestDispatcher("/views/staff/examstaff/candidate-dossier.jsp")

                .forward(request, response);

    }
    // Xac dinh fee total

    private double resolveFeeTotal(ExamRegistrationDTO profile, Payment payment, List<Fee> feeLines,

            String licenseCode, boolean requiresRoadTest) {

        double fromPayment = ProcedureFeeTotals.resolvePaidAmount(payment, feeLines);

        if (fromPayment > 0) {

            return fromPayment;

        }

        return feeDAO.sumProcedureFees(licenseCode, requiresRoadTest);

    }

}
