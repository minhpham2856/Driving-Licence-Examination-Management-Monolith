package examstaff.service;

import examstaff.dto.ProcedureFeeResultDTO;
import dto.exam.ExamRegistrationDTO;

public interface ProcedureFeeQueryService {

    ProcedureFeeResultDTO resolveProcedureFees(ExamRegistrationDTO profile);
}
