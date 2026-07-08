package service;

import dto.exam.ExamRegistrationDTO;
import dto.examstaff.ProcedureFeeResultDTO;

public interface ProcedurePaymentService {

    ProcedureFeeResultDTO previewFees(int candidateId, String licenseCode, boolean requiresRoadTest);

    boolean recordCashPayment(int candidateId, int enrollmentId, double totalAmount);

    boolean recordProcedureCashPayment(ExamRegistrationDTO profile);

    int resolveEnrollmentId(int candidateId);

    boolean clearCompletedPayments(int candidateId);
}
