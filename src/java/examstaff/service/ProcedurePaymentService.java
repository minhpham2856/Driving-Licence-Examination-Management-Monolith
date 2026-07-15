package examstaff.service;

import examstaff.dto.exam.ExamRegistrationDTO;
import examstaff.dto.ProcedureFeeResultDTO;

public interface ProcedurePaymentService {

    ProcedureFeeResultDTO previewFees(int candidateId, String licenseCode, boolean requiresRoadTest);

    boolean recordProcedureCashPayment(ExamRegistrationDTO profile);
}
