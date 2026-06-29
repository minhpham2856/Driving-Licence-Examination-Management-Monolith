package dao.impl;

import dao.AuditDAO;
import model.Audit;
import java.util.ArrayList;
import java.util.List;

public class AuditDAOImpl implements AuditDAO {
    @Override
    public List<Audit> getByUserId(int userId) { return new ArrayList<>(); }
    @Override
    public int insert(Audit audit) { return 0; }
    @Override
    public List<Audit> findAll() { return new ArrayList<>(); }
    @Override
    public List<Audit> getRecentLogs(int limit) { return new ArrayList<>(); }
    @Override
    public List<Audit> getLogsForSessionPaginated(int sessionId, int page, int pageSize, String searchQuery) { return new ArrayList<>(); }
    @Override
    public int getLogsCountForSession(int sessionId, String searchQuery) { return 0; }
    @Override
    public List<Audit> getViolationLogsForSession(int sessionId, int limit) { return new ArrayList<>(); }
}
