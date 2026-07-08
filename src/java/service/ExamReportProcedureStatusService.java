package service;

import dto.exam.ExamRegistrationDTO;
import dto.examstaff.ExamReportProcedureStatusDTO;

import java.util.List;

public interface ExamReportProcedureStatusService {

    ExamReportProcedureStatusDTO analyze(List<ExamRegistrationDTO> candidates, String webRoot);
}
