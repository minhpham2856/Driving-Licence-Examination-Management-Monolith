package dao.impl;

import dao.AdminStatsDAO;
import dbconnection.DBContext;
import dto.admin.RecentActivityDTO;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class AdminStatsDAOImpl extends DBContext implements AdminStatsDAO {

    @Override
    public int count(String tableName) {
        String sql = "SELECT COUNT(*) FROM " + tableName;
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public int countUsers()      { return count("[User]"); }

    @Override
    public int countExamAreas()  { return count("ExamArea"); }

    @Override
    public int countExams()      { return count("Exam"); }

    @Override
    public int countDevices()    { return count("ExamDevice"); }

    @Override
    public List<RecentActivityDTO> recentActivity(int limit) {
        List<RecentActivityDTO> list = new ArrayList<>();
        String sql = "SELECT TOP (" + limit + ") a.Action, a.TableName, a.RecordId, a.ChangedAt, "
                   + "u.Username "
                   + "FROM Audit a LEFT JOIN [User] u ON a.ChangedBy = u.UserId "
                   + "ORDER BY a.ChangedAt DESC";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            while (rs.next()) {
                RecentActivityDTO r = new RecentActivityDTO();
                r.setAction(rs.getString("Action"));
                r.setModule(rs.getString("TableName"));
                r.setRecordId(rs.getString("RecordId"));
                java.sql.Timestamp ts = rs.getTimestamp("ChangedAt");
                r.setTimestamp((ts == null) ? "" : fmt.format(ts));
                String un = rs.getString("Username");
                if (un == null || un.isEmpty()) un = "Hệ thống";
                r.setUsername(un);
                list.add(r);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
