package service;

import dto.admin.RecentActivityDTO;
import java.util.List;

public interface AdminDashboardService {
    int getTotalExamCenters();
    int getTotalUsers();
    int getTotalExamSessions();
    int getTotalComputers();
    List<RecentActivityDTO> getRecentActivities(int limit);
}
