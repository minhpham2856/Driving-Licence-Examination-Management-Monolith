package examstaff.service;

import examstaff.dto.exam.ExamRegistrationDTO;
import examstaff.dto.ProcedureFeeResultDTO;

public interface ProcedurePaymentService {

    ProcedureFeeResultDTO previewFees(int candidateId, String licenseCode, boolean requiresRoadTest);

    boolean recordCashPayment(int candidateId, int enrollmentId, double totalAmount);

    boolean recordProcedureCashPayment(ExamRegistrationDTO profile);

    int resolveEnrollmentId(int candidateId);

    boolean clearCompletedPayments(int candidateId);
}
