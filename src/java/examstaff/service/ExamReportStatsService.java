package examstaff.service;

import dto.exam.ExamRegistrationDTO;
import examstaff.dto.ExamReportStatsDTO;

import java.util.List;

public interface ExamReportStatsService {

    ExamReportStatsDTO computeStats(List<ExamRegistrationDTO> candidates, int examId);
}
