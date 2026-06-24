package Controllers.Staff.ExamStaff;

import DAO.ExamRegistrationDAO;
import DAO.ExamSessionDAO;
import DAO.FeeDAO;
import DAO.Impl.ExamRegistrationDAOImpl;
import DAO.Impl.ExamSessionDAOImpl;
import DAO.Impl.FeeDAOImpl;
import DAO.Impl.PaymentDAOImpl;
import DAO.PaymentDAO;
import Models.ExamRegistration;
import Models.ExamSession;
import Models.Fee;
import Models.Payment;
import Utils.ProcedureFeeTotals;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/views/staff/examstaff/candidate-dossier")
public class CandidateDossierServlet extends HttpServlet {

    private final ExamRegistrationDAO regDAO = new ExamRegistrationDAOImpl();
    private final ExamSessionDAO sessionDAO = new ExamSessionDAOImpl();
    private final FeeDAO feeDAO = new FeeDAOImpl();
    private final PaymentDAO payDAO = new PaymentDAOImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String sbd = request.getParameter("sbd");
        if (sbd == null || sbd.trim().isEmpty()) {
            response.sendRedirect("report");
            return;
        }

        ExamRegistration profile = regDAO.getBySbd(sbd.trim());
        if (profile == null) {
            response.sendRedirect("report");
            return;
        }

        String webRoot = request.getServletContext().getRealPath("/");
        CandidatePhotoHelper.normalizeQueue(webRoot, java.util.Collections.singletonList(profile), regDAO);
        boolean hasPhotoFile = CandidatePhotoHelper.findPhotoFile(
                request.getServletContext(), webRoot, profile.getPhotoUrl()) != null;
        request.setAttribute("hasPhotoFile", hasPhotoFile);

        ExamSession examSession = null;
        try {
            examSession = sessionDAO.getById(profile.getExamSessionId());
        } catch (Exception e) {
            e.printStackTrace();
        }

        Payment payment = payDAO.getByCandidateId(profile.getId());

        List<Fee> feeLines = new java.util.ArrayList<>();
        boolean feesFromPayment = false;
        if (payment != null && payment.getId() > 0) {
            feeLines = feeDAO.getFeesByPaymentId(payment.getId());
            feesFromPayment = !feeLines.isEmpty();
        }
        if (feeLines.isEmpty()) {
            feeLines = feeDAO.getProcedureFees(profile.getLicenseCode(), profile.isRequiresRoadTest());
            feesFromPayment = false;
        }
        double feeTotal = resolveFeeTotal(profile, payment, feeLines);

        request.setAttribute("profile", profile);
        request.setAttribute("examSession", examSession);
        request.setAttribute("payment", payment);
        request.setAttribute("feeLines", feeLines);
        request.setAttribute("feeTotal", feeTotal);
        request.setAttribute("feesFromPayment", feesFromPayment);
        request.setAttribute("dossierTitle", DossierFormHelper.resolveTitle(profile.getLicenseCode()));
        request.setAttribute("dossierSubtitle", DossierFormHelper.resolveSubtitle(profile.getLicenseCode()));
        request.setAttribute("autoPrint", "true".equalsIgnoreCase(request.getParameter("print")));

        request.getRequestDispatcher("/views/staff/examstaff/candidate-dossier.jsp")
                .forward(request, response);
    }

    private double resolveFeeTotal(ExamRegistration profile, Payment payment, List<Fee> feeLines) {
        double fromPayment = ProcedureFeeTotals.resolvePaidAmount(payment, feeLines);
        if (fromPayment > 0) {
            return fromPayment;
        }
        return feeDAO.sumProcedureFees(profile.getLicenseCode(), profile.isRequiresRoadTest());
    }
}
