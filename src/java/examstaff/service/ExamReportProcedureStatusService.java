package examstaff.service;

import dto.exam.ExamRegistrationDTO;
import examstaff.dto.ExamReportProcedureStatusDTO;

import java.util.List;

public interface ExamReportProcedureStatusService {

    ExamReportProcedureStatusDTO analyze(List<ExamRegistrationDTO> candidates, String webRoot);
}
