package dao.impl;

import dao.ExamEnrollmentDAO;
import dbconnection.DBContext;
import model.exam.ExamEnrollment;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ExamEnrollmentDAOImpl extends DBContext implements ExamEnrollmentDAO {
    @Override
    public ExamEnrollment findBySessionAndCandidate(int sessionId, int candidateId) {
        String sql = "SELECT * FROM ExamEnrollment WHERE SessionId = ? AND CandidateId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            ps.setInt(2, candidateId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ExamEnrollment ec = new ExamEnrollment();
                    ec.setExamEnrollmentId(rs.getInt("ExamEnrollmentId"));
                    ec.setCandidateId(rs.getInt("CandidateId"));
                    ec.setSessionId(rs.getInt("SessionId"));
                    ec.setSectionStatus(rs.getString("SectionStatus"));
                    ec.setSignaturePrinted(rs.getBoolean("SignaturePrinted"));
                    int devId = rs.getInt("ExamDeviceId");
                    if (!rs.wasNull()) {
                        ec.setExamDeviceId(devId);
                    }
                    return ec;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean assignExamDevice(int candidateId, int sessionId, int targetDevice) {
        String sql = "UPDATE ExamEnrollment SET ExamDeviceId = ? WHERE CandidateId = ? AND SessionId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, targetDevice);
            ps.setInt(2, candidateId);
            ps.setInt(3, sessionId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<ExamEnrollment> getEnrollmentsBySession(int sessionId) {
        List<ExamEnrollment> list = new ArrayList<>();
        String sql = "SELECT * FROM ExamEnrollment WHERE SessionId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ExamEnrollment ec = new ExamEnrollment();
                    ec.setExamEnrollmentId(rs.getInt("ExamEnrollmentId"));
                    ec.setCandidateId(rs.getInt("CandidateId"));
                    ec.setSessionId(rs.getInt("SessionId"));
                    ec.setSectionStatus(rs.getString("SectionStatus"));
                    ec.setSignaturePrinted(rs.getBoolean("SignaturePrinted"));
                    int devId = rs.getInt("ExamDeviceId");
                    if (!rs.wasNull()) {
                        ec.setExamDeviceId(devId);
                    }
                    list.add(ec);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<ExamEnrollment> findBySessionId(int sessionId) {
        return getEnrollmentsBySession(sessionId);
    }

    @Override
    public boolean update(ExamEnrollment e) {
        String sql = "UPDATE ExamEnrollment SET SectionStatus=?, SignaturePrinted=?, ExamDeviceId=? WHERE ExamEnrollmentId=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, e.getSectionStatus());
            ps.setBoolean(2, e.isSignaturePrinted());
            if (e.getExamDeviceId() != null) {
                ps.setInt(3, e.getExamDeviceId());
            } else {
                ps.setNull(3, java.sql.Types.INTEGER);
            }
            ps.setInt(4, e.getExamEnrollmentId());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }
}
