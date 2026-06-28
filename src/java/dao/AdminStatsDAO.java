package dao;

import model.admin.RecentActivityModel;
import java.util.List;

public interface AdminStatsDAO {

    int count(String tableName);

    int countUsers();

    int countExamAreas();

    int countExams();

    int countDevices();

    List<RecentActivityModel> recentActivity(int limit);
}
