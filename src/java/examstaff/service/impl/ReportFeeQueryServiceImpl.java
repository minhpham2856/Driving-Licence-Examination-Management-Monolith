package examstaff.service.impl;

import examstaff.dao.FeeDAO;
import examstaff.dao.PaymentDAO;
import examstaff.dao.impl.FeeDAOImpl;
import examstaff.dao.impl.PaymentDAOImpl;
import examstaff.dto.ReportPaymentSummaryDTO;
import examstaff.enums.PaymentStatus;
import shared.model.Fee;
import shared.model.Payment;
import examstaff.service.ReportFeeQueryService;
import examstaff.util.ProcedureFeeTotals;

import java.util.ArrayList;
import java.util.List;

public class ReportFeeQueryServiceImpl implements ReportFeeQueryService {

    private final PaymentDAO paymentDAO = new PaymentDAOImpl();
    private final FeeDAO feeDAO = new FeeDAOImpl();

    @Override
    public ReportPaymentSummaryDTO findPaymentSummary(int candidateId) {
        ReportPaymentSummaryDTO summary = new ReportPaymentSummaryDTO();
        Payment payment = paymentDAO.getByCandidateId(candidateId);
        if (payment == null || payment.getPaymentId() <= 0 || !isActiveProcedurePayment(payment)) {
            return summary;
        }
        List<Fee> feeLines = feeDAO.getFeesByPaymentId(payment.getPaymentId());
        if (feeLines == null) {
            feeLines = new ArrayList<>();
        }
        summary.setPayment(payment);
        summary.setFeeLines(feeLines);
        summary.setLineTotal(ProcedureFeeTotals.resolvePaidAmount(payment, feeLines));
        return summary;
    }

    private static boolean isActiveProcedurePayment(Payment payment) {
        return PaymentStatus.isCompleted(payment.getPaymentStatus());
    }
}
