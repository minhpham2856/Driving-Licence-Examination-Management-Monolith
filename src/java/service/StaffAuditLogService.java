package service;

public interface StaffAuditLogService {

    void logAction(int userId, String action, String details);

    void logAction(int userId, String action, String details, int recordId);
}
