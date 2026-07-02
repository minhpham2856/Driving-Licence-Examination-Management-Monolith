package examstaff.service;

import examstaff.dto.ProcedureFeeResultDTO;
import examstaff.dto.exam.ExamRegistrationDTO;

public interface ProcedureFeeQueryService {

    ProcedureFeeResultDTO resolveProcedureFees(ExamRegistrationDTO profile);
}
