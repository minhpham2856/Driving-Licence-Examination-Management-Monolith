package dao.impl;

import java.sql.*;

import dbconnection.DBContext;
import dao.CandidateDAO;
import model.Candidate;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CandidateDAOImpl extends DBContext implements CandidateDAO {

    private static final String BASE_SELECT = 
        "SELECT CandidateId, CandidateNumber, FullName, DateOfBirth, PhoneNumber, Sex, " +
        "GovernmentIdNumber, Address, TakeTheory, TakeLayout, TakeRoad, TakeNo, " +
        "ReasonForTaking, PhotoImageUrl, IsAbsent, IsSuspended FROM Candidate";

    @Override
    public Candidate getById(int candidateId) {
        String sql = BASE_SELECT + " WHERE CandidateId = ?";
        return querySingle(sql, ps -> ps.setInt(1, candidateId));
    }

    @Override
    public Candidate getByNumber(int candidateNumber) {
        String sql = BASE_SELECT + " WHERE CandidateNumber = ?";
        return querySingle(sql, ps -> ps.setString(1, String.valueOf(candidateNumber)));
    }

    @Override
    public List<Candidate> getAllByIds(List<Integer> candidateIds) {
        if (candidateIds == null || candidateIds.isEmpty()) return new ArrayList<>();
        StringBuilder sql = new StringBuilder(BASE_SELECT).append(" WHERE CandidateId IN (");
        for (int i = 0; i < candidateIds.size(); i++) {
            sql.append(i == 0 ? "?" : ",?");
        }
        sql.append(")");
        return queryList(sql.toString(), ps -> {
            for (int i = 0; i < candidateIds.size(); i++) {
                ps.setInt(i + 1, candidateIds.get(i));
            }
        });
    }

    @Override
    public int insert(Candidate c) {
        String sql = "INSERT INTO Candidate (CandidateNumber, FullName, DateOfBirth, PhoneNumber, Sex, " +
                     "GovernmentIdNumber, Address, TakeTheory, TakeLayout, TakeRoad, TakeNo, " +
                     "ReasonForTaking, PhotoImageUrl, IsAbsent, IsSuspended) " +
                     "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, String.valueOf(c.getCandidateNumber()));
            ps.setString(2, c.getFullName());
            ps.setTimestamp(3, c.getDateOfBirth());
            ps.setString(4, c.getPhoneNumber());
            ps.setBoolean(5, c.isSex());
            ps.setString(6, c.getGovernmentIdNumber());
            ps.setString(7, c.getAddress());
            
            if (c.getTakeTheory() != null) ps.setBoolean(8, c.getTakeTheory());
            else ps.setNull(8, Types.BIT);
            
            if (c.getTakePractical() != null) ps.setBoolean(9, c.getTakePractical()); // maps to TakeLayout
            else ps.setNull(9, Types.BIT);
            
            if (c.getTakeRoadLayout() != null) ps.setBoolean(10, c.getTakeRoadLayout()); // maps to TakeRoad
            else ps.setNull(10, Types.BIT);
            
            ps.setInt(11, c.getTakeNo());
            ps.setString(12, c.getReasonForTaking());
            ps.setString(13, c.getPhotoImageUrl());
            ps.setBoolean(14, c.isAbsent());
            ps.setBoolean(15, c.isSuspended());
            
            if (ps.executeUpdate() > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public boolean update(Candidate c) {
        String sql = "UPDATE Candidate SET CandidateNumber=?, FullName=?, DateOfBirth=?, PhoneNumber=?, Sex=?, " +
                     "GovernmentIdNumber=?, Address=?, TakeTheory=?, TakeLayout=?, TakeRoad=?, TakeNo=?, " +
                     "ReasonForTaking=?, PhotoImageUrl=?, IsAbsent=?, IsSuspended=? WHERE CandidateId=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, String.valueOf(c.getCandidateNumber()));
            ps.setString(2, c.getFullName());
            ps.setTimestamp(3, c.getDateOfBirth());
            ps.setString(4, c.getPhoneNumber());
            ps.setBoolean(5, c.isSex());
            ps.setString(6, c.getGovernmentIdNumber());
            ps.setString(7, c.getAddress());
            
            if (c.getTakeTheory() != null) ps.setBoolean(8, c.getTakeTheory());
            else ps.setNull(8, Types.BIT);
            
            if (c.getTakePractical() != null) ps.setBoolean(9, c.getTakePractical()); // maps to TakeLayout
            else ps.setNull(9, Types.BIT);
            
            if (c.getTakeRoadLayout() != null) ps.setBoolean(10, c.getTakeRoadLayout()); // maps to TakeRoad
            else ps.setNull(10, Types.BIT);
            
            ps.setInt(11, c.getTakeNo());
            ps.setString(12, c.getReasonForTaking());
            ps.setString(13, c.getPhotoImageUrl());
            ps.setBoolean(14, c.isAbsent());
            ps.setBoolean(15, c.isSuspended());
            ps.setInt(16, c.getCandidateId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(int candidateId) {
        String sql = "DELETE FROM Candidate WHERE CandidateId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    @Override
    public List<Candidate> findAll() {
        return queryList(BASE_SELECT, ps -> {});
    }

    private interface PreparedStatementBinder {
        void bind(PreparedStatement ps) throws SQLException;
    }

    private Candidate querySingle(String sql, PreparedStatementBinder binder) {
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<Candidate> queryList(String sql, PreparedStatementBinder binder) {
        List<Candidate> list = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private Candidate map(ResultSet rs) throws SQLException {
        Candidate c = new Candidate();
        c.setCandidateId(rs.getInt("CandidateId"));
        c.setCandidateNumber(parseCandidateNumber(rs.getString("CandidateNumber")));
        c.setFullName(rs.getString("FullName"));
        c.setDateOfBirth(rs.getTimestamp("DateOfBirth"));
        c.setPhoneNumber(rs.getString("PhoneNumber"));
        c.setSex(rs.getBoolean("Sex"));
        c.setGovernmentIdNumber(rs.getString("GovernmentIdNumber"));
        c.setAddress(rs.getString("Address"));
        
        c.setTakeTheory((Boolean) rs.getObject("TakeTheory"));
        c.setTakePractical((Boolean) rs.getObject("TakeLayout")); 
        c.setTakeRoadLayout((Boolean) rs.getObject("TakeRoad")); 
        
        c.setTakeNo(rs.getInt("TakeNo"));
        c.setReasonForTaking(rs.getString("ReasonForTaking"));
        c.setPhotoImageUrl(rs.getString("PhotoImageUrl"));
        c.setAbsent(rs.getBoolean("IsAbsent"));
        c.setSuspended(rs.getBoolean("IsSuspended"));
        return c;
    }

    private static int parseCandidateNumber(String raw) {
        if (raw == null || raw.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
