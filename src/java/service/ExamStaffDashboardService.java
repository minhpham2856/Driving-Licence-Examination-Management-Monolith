package service;

import dto.SessionDTO;
import dto.examstaff.ExamStaffDashboardViewDTO;

import java.util.List;

public interface ExamStaffDashboardService {

    ExamStaffDashboardViewDTO buildView(List<SessionDTO> allSessions, int examId);
}
