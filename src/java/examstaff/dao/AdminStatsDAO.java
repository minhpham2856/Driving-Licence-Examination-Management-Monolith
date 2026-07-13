package examstaff.dao;

import examstaff.dto.admin.RecentActivityDTO;
import java.util.List;

public interface AdminStatsDAO {
    int count(String tableName);
    int countUsers();
    int countExamAreas();
    int countExams();
    int countDevices();
    List<RecentActivityDTO> recentActivity(int limit);
}
