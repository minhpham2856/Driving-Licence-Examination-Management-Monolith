package service;
import dto.RecentActivityDTO;
import java.util.List;
public interface AdminDashboardService {
    int getTotalExamCenters();
    int getTotalUsers();
    int getTotalExamSessions();
    int getTotalComputers();
    List<RecentActivityDTO> getRecentActivities(int limit);
}
