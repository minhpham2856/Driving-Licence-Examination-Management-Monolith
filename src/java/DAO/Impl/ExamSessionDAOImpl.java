package DAO.Impl;

import DBConnection.DBContext;
import DAO.ExamSessionDAO;
import Models.ExamSession;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExamSessionDAOImpl extends DBContext implements ExamSessionDAO {

    @Override
    public ExamSession getById(int id) {
        String sql = """
                     select es.*, lt.licenseCode, et.typeName as examTypeName, ea.areaName 
                     from ExamSession es
                     join LicenseType lt on es.licenseTypeId = lt.id
                     join ExamType et on es.examTypeId = et.id
                     join ExamArea ea on es.areaId = ea.id
                     where es.id = ?
                     """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToExamSession(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<ExamSession> getActiveSessions() {
        List<ExamSession> list = new ArrayList<>();
        // All scheduled, open or in-progress sessions for today or upcoming (to test easily, we just grab all scheduled/open/inprogress ones)
        String sql = """
                     select es.*, lt.licenseCode, et.typeName as examTypeName, ea.areaName 
                     from ExamSession es
                     join LicenseType lt on es.licenseTypeId = lt.id
                     join ExamType et on es.examTypeId = et.id
                     join ExamArea ea on es.areaId = ea.id
                     where es.status in ('Scheduled', 'Open', 'InProgress')
                     order by es.examDate, es.shiftStartTime
                     """;

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToExamSession(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<ExamSession> getAllSessions() {
        List<ExamSession> list = new ArrayList<>();
        String sql = """
                     select es.*, lt.licenseCode, et.typeName as examTypeName, ea.areaName 
                     from ExamSession es
                     join LicenseType lt on es.licenseTypeId = lt.id
                     join ExamType et on es.examTypeId = et.id
                     join ExamArea ea on es.areaId = ea.id
                     order by es.examDate desc, es.shiftStartTime desc
                     """;

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToExamSession(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public boolean updateStatus(int sessionId, String status) {
        String sql = """
                     update ExamSession 
                     set status = ? 
                     where id = ?
                     """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, sessionId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private ExamSession mapResultSetToExamSession(ResultSet rs) throws SQLException {
        ExamSession es = new ExamSession();
        es.setId(rs.getInt("id"));
        es.setSessionName(rs.getString("sessionName"));
        es.setLicenseTypeId(rs.getInt("licenseTypeId"));
        es.setExamTypeId(rs.getInt("examTypeId"));
        es.setExamDate(rs.getDate("examDate"));
        es.setShiftStartTime(rs.getTime("shiftStartTime"));
        es.setShiftEndTime(rs.getTime("shiftEndTime"));
        es.setAreaId(rs.getInt("areaId"));
        es.setStatus(rs.getString("status"));
        es.setMaxCandidates(rs.getInt("maxCandidates"));
        es.setRegisteredCount(rs.getInt("registeredCount"));
        es.setCreatedAt(rs.getTimestamp("createdAt"));
        
        // Joined helper fields
        es.setLicenseCode(rs.getString("licenseCode"));
        es.setExamTypeName(rs.getString("examTypeName"));
        es.setAreaName(rs.getString("areaName"));
        return es;
    }
}
