package examstaff.dao.impl;

import dbconnection.DBContext;
import examstaff.dao.CandidateDAO;
import examstaff.model.Candidate;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class CandidateDAOImpl extends DBContext implements CandidateDAO {

    private static final String BASE_SELECT =
            "SELECT CandidateId, CandidateNumber, FullName, DateOfBirth, PhoneNumber, Sex, "
            + "GovernmentIdNumber, Address, TakeTheory, TakeLayout, TakeNo, "
            + "ReasonForTaking, PhotoImageUrl, IsAbsent, IsSuspended FROM Candidate";

    @Override
    public Candidate getById(int candidateId) {
        String sql = BASE_SELECT + " WHERE CandidateId = ?";
        return querySingle(sql, ps -> ps.setInt(1, candidateId));
    }

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

    @Override
    public int insert(Candidate candidate) {
        String sql = "INSERT INTO Candidate (CandidateNumber, FullName, DateOfBirth, PhoneNumber, Sex, "
                + "GovernmentIdNumber, Address, TakeTheory, TakeLayout, TakeNo, "
                + "ReasonForTaking, PhotoImageUrl, IsAbsent, IsSuspended) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, candidate.getCandidateNumber());
            ps.setString(2, candidate.getFullName());
            ps.setTimestamp(3, candidate.getDateOfBirth());
            ps.setString(4, candidate.getPhoneNumber());
            ps.setBoolean(5, candidate.isSex());
            ps.setString(6, candidate.getGovernmentIdNumber());
            ps.setString(7, candidate.getAddress());
            if (candidate.getTakeTheory() != null) {
                ps.setBoolean(8, candidate.getTakeTheory());
            } else {
                ps.setNull(8, Types.BIT);
            }
            if (candidate.getTakeLayout() != null) {
                ps.setBoolean(9, candidate.getTakeLayout());
            } else {
                ps.setNull(9, Types.BIT);
            }
            ps.setInt(10, candidate.getTakeNo());
            ps.setString(11, candidate.getReasonForTaking());
            ps.setString(12, candidate.getPhotoImageUrl());
            ps.setBoolean(13, candidate.isAbsent());
            ps.setBoolean(14, candidate.isSuspended());
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

    @Override
    public boolean update(Candidate candidate) {
        String sql = "UPDATE Candidate SET CandidateNumber=?, FullName=?, DateOfBirth=?, PhoneNumber=?, Sex=?, "
                + "GovernmentIdNumber=?, Address=?, TakeTheory=?, TakeLayout=?, TakeNo=?, "
                + "ReasonForTaking=?, PhotoImageUrl=?, IsAbsent=?, IsSuspended=? WHERE CandidateId=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, candidate.getCandidateNumber());
            ps.setString(2, candidate.getFullName());
            ps.setTimestamp(3, candidate.getDateOfBirth());
            ps.setString(4, candidate.getPhoneNumber());
            ps.setBoolean(5, candidate.isSex());
            ps.setString(6, candidate.getGovernmentIdNumber());
            ps.setString(7, candidate.getAddress());
            if (candidate.getTakeTheory() != null) {
                ps.setBoolean(8, candidate.getTakeTheory());
            } else {
                ps.setNull(8, Types.BIT);
            }
            if (candidate.getTakeLayout() != null) {
                ps.setBoolean(9, candidate.getTakeLayout());
            } else {
                ps.setNull(9, Types.BIT);
            }
            ps.setInt(10, candidate.getTakeNo());
            ps.setString(11, candidate.getReasonForTaking());
            ps.setString(12, candidate.getPhotoImageUrl());
            ps.setBoolean(13, candidate.isAbsent());
            ps.setBoolean(14, candidate.isSuspended());
            ps.setInt(15, candidate.getCandidateId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

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

    @Override
    public boolean updateExaminerProfile(int candidateId, String fullName, Date dateOfBirth,
            String governmentIdNumber, String phoneNumber, String address, boolean sex, String reasonForTaking) {
        String sql = "UPDATE Candidate SET FullName = ?, DateOfBirth = ?, GovernmentIdNumber = ?, "
                + "PhoneNumber = ?, Address = ?, Sex = ?, ReasonForTaking = ? WHERE CandidateId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, fullName);
            if (dateOfBirth != null) {
                ps.setTimestamp(2, new Timestamp(dateOfBirth.getTime()));
            } else {
                ps.setNull(2, Types.TIMESTAMP);
            }
            ps.setString(3, governmentIdNumber);
            ps.setString(4, phoneNumber);
            ps.setString(5, address);
            ps.setBoolean(6, sex);
            ps.setString(7, reasonForTaking);
            ps.setInt(8, candidateId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private interface PreparedStatementBinder {
        void bind(PreparedStatement ps) throws SQLException;
    }

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

    private Candidate map(ResultSet rs) throws SQLException {
        Candidate candidate = new Candidate();
        candidate.setCandidateId(rs.getInt("CandidateId"));
        candidate.setCandidateNumber(rs.getString("CandidateNumber"));
        candidate.setFullName(rs.getString("FullName"));
        candidate.setDateOfBirth(rs.getTimestamp("DateOfBirth"));
        candidate.setPhoneNumber(rs.getString("PhoneNumber"));
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
