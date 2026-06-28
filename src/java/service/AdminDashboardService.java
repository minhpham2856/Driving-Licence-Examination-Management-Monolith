package service;

import model.admin.RecentActivityModel;
import java.util.List;

public interface AdminDashboardService {
    int getTotalExamCenters();
    int getTotalUsers();
    int getTotalExamSessions();
    int getTotalComputers();
    List<RecentActivityModel> getRecentActivities(int limit);
}
