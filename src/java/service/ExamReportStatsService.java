package service;

import dto.exam.ExamRegistrationDTO;
import dto.examstaff.ExamReportStatsDTO;

import java.util.List;

public interface ExamReportStatsService {

    ExamReportStatsDTO computeStats(List<ExamRegistrationDTO> candidates);
}
