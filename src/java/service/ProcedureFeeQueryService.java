package service;

import dto.examstaff.ProcedureFeeResultDTO;
import dto.exam.ExamRegistrationDTO;

public interface ProcedureFeeQueryService {

    ProcedureFeeResultDTO resolveProcedureFees(ExamRegistrationDTO profile);
}
