package admin.dao;

public interface AdminStatsDAO {
    int count(String table);
    int countActiveAccounts();
}
