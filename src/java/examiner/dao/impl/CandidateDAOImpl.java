package examiner.dao.impl;

import shared.dbconnection.DBContext;
import examiner.dao.CandidateDAO;
import shared.model.Candidate;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

// JDBC implementation for Candidate; examiner module DAO layer only.
public class CandidateDAOImpl extends DBContext implements CandidateDAO {

    private static final String BASE_SELECT =
            "SELECT CandidateId, CandidateNumber, FullName, DateOfBirth, PhoneNumber, Email, Sex, "
            + "GovernmentIdNumber, Address, TakeTheory, TakeLayout, TakeNo, "
            + "ReasonForTaking, PhotoImageUrl, IsAbsent, IsSuspended FROM Candidate";

    // Loads one candidate row by primary key.
    @Override
    public Candidate get(int candidateId) {
        String sql = BASE_SELECT + " WHERE CandidateId = ?";
        return querySingle(sql, ps -> ps.setInt(1, candidateId));
    }

    // Loads candidate rows for a list of ids.
    @Override
    public List<Candidate> getAllByIds(List<Integer> candidateIds) {
        if (candidateIds == null || candidateIds.isEmpty()) {
            return new ArrayList<>();
        }
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

    // Inserts a new candidate and returns generated CandidateId.
    @Override
    public int add(Candidate candidate) {
        String sql = "INSERT INTO Candidate (CandidateNumber, FullName, DateOfBirth, PhoneNumber, Email, Sex, "
                + "GovernmentIdNumber, Address, TakeTheory, TakeLayout, TakeNo, "
                + "ReasonForTaking, PhotoImageUrl, IsAbsent, IsSuspended) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, candidate.getCandidateNumber());
            ps.setString(2, candidate.getFullName());
            ps.setTimestamp(3, candidate.getDateOfBirth());
            ps.setString(4, candidate.getPhoneNumber());
            ps.setString(5, candidate.getEmail());
            ps.setBoolean(6, candidate.isSex());
            ps.setString(7, candidate.getGovernmentIdNumber());
            ps.setString(8, candidate.getAddress());
            if (candidate.getTakeTheory() != null) {
                ps.setBoolean(9, candidate.getTakeTheory());
            } else {
                ps.setNull(9, Types.BIT);
            }
            if (candidate.getTakeLayout() != null) {
                ps.setBoolean(10, candidate.getTakeLayout());
            } else {
                ps.setNull(10, Types.BIT);
            }
            ps.setInt(11, candidate.getTakeNo());
            ps.setString(12, candidate.getReasonForTaking());
            ps.setString(13, candidate.getPhotoImageUrl());
            ps.setBoolean(14, candidate.isAbsent());
            ps.setBoolean(15, candidate.isSuspended());
            if (ps.executeUpdate() > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Updates all columns on an existing candidate row.
    @Override
    public boolean update(Candidate candidate) {
        String sql = "UPDATE Candidate SET CandidateNumber=?, FullName=?, DateOfBirth=?, PhoneNumber=?, Email=?, Sex=?, "
                + "GovernmentIdNumber=?, Address=?, TakeTheory=?, TakeLayout=?, TakeNo=?, "
                + "ReasonForTaking=?, PhotoImageUrl=?, IsAbsent=?, IsSuspended=? WHERE CandidateId=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, candidate.getCandidateNumber());
            ps.setString(2, candidate.getFullName());
            ps.setTimestamp(3, candidate.getDateOfBirth());
            ps.setString(4, candidate.getPhoneNumber());
            ps.setString(5, candidate.getEmail());
            ps.setBoolean(6, candidate.isSex());
            ps.setString(7, candidate.getGovernmentIdNumber());
            ps.setString(8, candidate.getAddress());
            if (candidate.getTakeTheory() != null) {
                ps.setBoolean(9, candidate.getTakeTheory());
            } else {
                ps.setNull(9, Types.BIT);
            }
            if (candidate.getTakeLayout() != null) {
                ps.setBoolean(10, candidate.getTakeLayout());
            } else {
                ps.setNull(10, Types.BIT);
            }
            ps.setInt(11, candidate.getTakeNo());
            ps.setString(12, candidate.getReasonForTaking());
            ps.setString(13, candidate.getPhotoImageUrl());
            ps.setBoolean(14, candidate.isAbsent());
            ps.setBoolean(15, candidate.isSuspended());
            ps.setInt(16, candidate.getCandidateId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Updates only the IsAbsent flag on a candidate row.
    @Override
    public boolean updateAbsent(int candidateId, boolean absent) {
        String sql = "UPDATE Candidate SET IsAbsent = ? WHERE CandidateId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setBoolean(1, absent);
            ps.setInt(2, candidateId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Updates only the IsSuspended flag on a candidate row.
    @Override
    public boolean updateSuspended(int candidateId, boolean suspended) {
        String sql = "UPDATE Candidate SET IsSuspended = ? WHERE CandidateId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setBoolean(1, suspended);
            ps.setInt(2, candidateId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private interface PreparedStatementBinder {
        void bind(PreparedStatement ps) throws SQLException;
    }

    // Private helper: query single.
    private Candidate querySingle(String sql, PreparedStatementBinder binder) {
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Private helper: query list.
    private List<Candidate> queryList(String sql, PreparedStatementBinder binder) {
        List<Candidate> list = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Private helper: map.
    private Candidate map(ResultSet rs) throws SQLException {
        Candidate candidate = new Candidate();
        candidate.setCandidateId(rs.getInt("CandidateId"));
        candidate.setCandidateNumber(rs.getString("CandidateNumber"));
        candidate.setFullName(rs.getString("FullName"));
        candidate.setDateOfBirth(rs.getTimestamp("DateOfBirth"));
        candidate.setPhoneNumber(rs.getString("PhoneNumber"));
        candidate.setEmail(rs.getString("Email"));
        candidate.setSex(rs.getBoolean("Sex"));
        candidate.setGovernmentIdNumber(rs.getString("GovernmentIdNumber"));
        candidate.setAddress(rs.getString("Address"));
        candidate.setTakeTheory((Boolean) rs.getObject("TakeTheory"));
        candidate.setTakeLayout((Boolean) rs.getObject("TakeLayout"));
        candidate.setTakeNo(rs.getInt("TakeNo"));
        candidate.setReasonForTaking(rs.getString("ReasonForTaking"));
        candidate.setPhotoImageUrl(rs.getString("PhotoImageUrl"));
        candidate.setAbsent(rs.getBoolean("IsAbsent"));
        candidate.setSuspended(rs.getBoolean("IsSuspended"));
        return candidate;
    }
}

