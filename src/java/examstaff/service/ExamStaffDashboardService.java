package examstaff.service;

import dto.ExamSummaryDTO;
import examstaff.dto.ExamStaffDashboardViewDTO;

import java.util.List;

public interface ExamStaffDashboardService {

    ExamStaffDashboardViewDTO buildView(List<ExamSummaryDTO> allSessions, int examId);
}
