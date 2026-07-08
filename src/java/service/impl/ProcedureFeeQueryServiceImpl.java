package service.impl;

import dao.FeeDAO;
import dao.PaymentDAO;
import dao.impl.FeeDAOImpl;
import dao.impl.PaymentDAOImpl;
import dto.exam.ExamRegistrationDTO;
import dto.examstaff.ProcedureFeeResultDTO;
import model.Fee;
import model.Payment;
import service.ProcedureFeeQueryService;
import util.ProcedureFeeTotals;

import java.util.ArrayList;
import java.util.List;

public class ProcedureFeeQueryServiceImpl implements ProcedureFeeQueryService {

    private final FeeDAO feeDAO = new FeeDAOImpl();
    private final PaymentDAO paymentDAO = new PaymentDAOImpl();

    @Override
    public ProcedureFeeResultDTO resolveProcedureFees(ExamRegistrationDTO profile) {
        ProcedureFeeResultDTO result = new ProcedureFeeResultDTO();
        result.setFeeLines(new ArrayList<>());
        if (profile == null) {
            return result;
        }
        String licenseCode = profile.getLicenseCode();
        if (licenseCode == null || licenseCode.isBlank()) {
            licenseCode = profile.getClazz();
        }
        boolean requiresRoadTest = profile.isRequiresRoadTest();

        Payment payment = paymentDAO.getByCandidateId(profile.getId());
        List<Fee> feeLines = new ArrayList<>();
        boolean feesFromPayment = false;
        if (payment != null && payment.getPaymentId() > 0) {
            feeLines = feeDAO.getFeesByPaymentId(payment.getPaymentId());
            feesFromPayment = feeLines != null && !feeLines.isEmpty();
        }
        if (feeLines == null || feeLines.isEmpty()) {
            feeLines = feeDAO.getProcedureFees(licenseCode, requiresRoadTest);
            feesFromPayment = false;
        }

        double feeTotal = ProcedureFeeTotals.resolvePaidAmount(payment, feeLines);
        if (feeTotal <= 0) {
            feeTotal = feeDAO.sumProcedureFees(licenseCode, requiresRoadTest);
        }

        result.setFeeLines(feeLines);
        result.setFeeTotal(feeTotal);
        result.setFeesFromPayment(feesFromPayment);
        return result;
    }
}
