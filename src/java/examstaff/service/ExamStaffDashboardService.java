package examstaff.service;

import examstaff.dto.ExamSummaryDTO;
import examstaff.dto.ExamStaffDashboardViewDTO;

import java.util.List;

public interface ExamStaffDashboardService {

    ExamStaffDashboardViewDTO buildView(List<ExamSummaryDTO> allExams, int examId);
}
