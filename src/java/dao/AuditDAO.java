package dao;


import model.user.Audit;

import model.staff.StaffProcedureKpiModel;

import java.util.List;


public interface AuditDAO {

    
    boolean insert(Audit log);

    
    List<Audit> getLogsByUserToday(int userId);

    
    List<Audit> getAllLogsToday();

    
    List<Audit> getLogsByUserAndDate(int userId, String dateStr);

    
    List<Audit> getAllLogsByDate(String dateStr);

    
    List<Audit> getLogsByUserAndDatePaginated(int userId, String dateStr, int page, int pageSize);

    
    List<Audit> getAllLogsByDatePaginated(String dateStr, int page, int pageSize);

    
    int getLogsCountByUserAndDate(int userId, String dateStr);

    
    int getAllLogsCountByDate(String dateStr);

    
    StaffProcedureKpiModel getStaffProcedureKpi(int userId, String filterDate);

    
    List<Audit> getLogsForSessionPaginated(int sessionId, int page, int pageSize);

    
    int getLogsCountForSession(int sessionId);

    
    List<Audit> getLogsForSessionPaginated(int sessionId, int page, int pageSize, String searchQuery);

    
    int getLogsCountForSession(int sessionId, String searchQuery);

    
    List<Audit> getViolationLogsForSession(int sessionId, int limit);
    
    
    List<Audit> getRecentLogs(int limit);
}
