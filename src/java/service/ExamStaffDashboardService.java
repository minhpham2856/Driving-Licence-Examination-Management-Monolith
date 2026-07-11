package service;

import dto.ExamSummaryDTO;
import dto.examstaff.ExamStaffDashboardViewDTO;

import java.util.List;

public interface ExamStaffDashboardService {

    ExamStaffDashboardViewDTO buildView(List<ExamSummaryDTO> allSessions, int examId);
}
