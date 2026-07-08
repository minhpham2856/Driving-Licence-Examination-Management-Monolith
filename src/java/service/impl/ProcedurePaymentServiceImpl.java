package service.impl;

import dao.PaymentDAO;
import dao.impl.PaymentDAOImpl;
import dto.exam.ExamRegistrationDTO;
import dto.examstaff.ProcedureFeeResultDTO;
import enums.PaymentMethod;
import enums.PaymentStatus;
import model.Payment;
import service.ExamRegistrationService;
import service.ProcedureFeeQueryService;
import service.ProcedurePaymentService;

public class ProcedurePaymentServiceImpl implements ProcedurePaymentService {

    private final PaymentDAO paymentDAO = new PaymentDAOImpl();
    private final ExamRegistrationService registrationService = new ExamRegistrationServiceImpl();
    private final ProcedureFeeQueryService feeQueryService = new ProcedureFeeQueryServiceImpl();

    @Override
    public ProcedureFeeResultDTO previewFees(int candidateId, String licenseCode, boolean requiresRoadTest) {
        ExamRegistrationDTO profile = registrationService.getById(candidateId);
        if (profile == null) {
            profile = new ExamRegistrationDTO();
            profile.setId(candidateId);
            profile.setLicenseCode(licenseCode);
        }
        return feeQueryService.resolveProcedureFees(profile);
    }

    @Override
    public boolean recordProcedureCashPayment(ExamRegistrationDTO profile) {
        if (profile == null) {
            return false;
        }
        ProcedureFeeResultDTO fees = feeQueryService.resolveProcedureFees(profile);
        double total = fees != null ? fees.getFeeTotal() : 0;
        if (total <= 0) {
            total = 200_000;
        }
        int enrollmentId = profile.getExamEnrollmentId();
        if (enrollmentId <= 0) {
            enrollmentId = paymentDAO.resolveEnrollmentId(profile.getId());
        }
        return recordCashPayment(profile.getId(), enrollmentId, total);
    }

    @Override
    public int resolveEnrollmentId(int candidateId) {
        return paymentDAO.resolveEnrollmentId(candidateId);
    }

    @Override
    public boolean recordCashPayment(int candidateId, int enrollmentId, double totalAmount) {
        Payment payment = new Payment();
        payment.setExamEnrollmentId(enrollmentId);
        payment.setTotalAmount(totalAmount);
        payment.setPaymentStatus(PaymentStatus.HOAN_TAT.getDisplayName());
        payment.setPaymentMethod(PaymentMethod.CASH.getCode());
        payment.setTransactionReference("REF-" + System.currentTimeMillis() % 1_000_000);
        if (paymentDAO.insert(payment)) {
            return true;
        }
        return registrationService.updatePayment(candidateId, true);
    }

    @Override
    public boolean clearCompletedPayments(int candidateId) {
        return registrationService.clearCompletedPayments(candidateId);
    }
}
