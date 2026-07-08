package service.impl;

import dao.FeeDAO;
import dao.PaymentDAO;
import dao.impl.FeeDAOImpl;
import dao.impl.PaymentDAOImpl;
import dto.examstaff.ReportPaymentSummaryDTO;
import enums.PaymentStatus;
import model.Fee;
import model.Payment;
import service.ReportFeeQueryService;
import util.ProcedureFeeTotals;

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
