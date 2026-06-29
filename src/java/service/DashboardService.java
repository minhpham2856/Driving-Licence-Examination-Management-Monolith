package service;
import dto.AuditRowDTO;
import java.util.List;
public interface DashboardService {
    int getTotalExamCenters();
    int getTotalUsers();
    int getTotalExams();
    int getTotalComputers();
    List<AuditRowDTO> getRecentActivities(int limit);
}
