package examiner.dao.impl;

import examiner.dao.CandidateDAO;
import examiner.dao.ExamEnrollmentDAO;
import shared.dbconnection.DBContext;
import examiner.enums.CandidateStatus;
import examiner.enums.Sex;
import shared.model.ExamEnrollment;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ExamEnrollmentDAOImpl extends DBContext implements ExamEnrollmentDAO {

    private static final String ENROLLMENT_COLUMNS =
            "ExamEnrollmentId, CandidateId, ExamId, SectionStatus, SignaturePrinted, ExamDeviceId";

    private final CandidateDAO candidateDAO = new CandidateDAOImpl();

    @Override
    public boolean update(ExamEnrollment enrollment) {
        String sql = "UPDATE ExamEnrollment SET SectionStatus = ?, SignaturePrinted = ?, ExamDeviceId = ? "
                + "WHERE ExamEnrollmentId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, enrollment.getSectionStatus());
            ps.setBoolean(2, enrollment.isSignaturePrinted());
            if (enrollment.getExamDeviceId() != null) {
                ps.setInt(3, enrollment.getExamDeviceId());
            } else {
                ps.setNull(3, java.sql.Types.INTEGER);
            }
            ps.setInt(4, enrollment.getExamEnrollmentId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public int insert(ExamEnrollment enrollment) {
        String sql = "INSERT INTO ExamEnrollment (CandidateId, ExamId, SectionStatus, SignaturePrinted, ExamDeviceId) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, enrollment.getCandidateId());
            ps.setInt(2, enrollment.getExamId());
            ps.setString(3, enrollment.getSectionStatus() != null
                    ? enrollment.getSectionStatus() : CandidateStatus.NOT_STARTED.getValue());
            ps.setBoolean(4, enrollment.isSignaturePrinted());
            if (enrollment.getExamDeviceId() != null) {
                ps.setInt(5, enrollment.getExamDeviceId());
            } else {
                ps.setNull(5, java.sql.Types.INTEGER);
            }
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
    public List<ExamEnrollment> getByExamId(int examId) {
        List<ExamEnrollment> list = new ArrayList<>();
        String sql = "SELECT " + ENROLLMENT_COLUMNS
                + " FROM ExamEnrollment WHERE ExamId = ? ORDER BY CandidateId";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapEnrollment(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<ExamEnrollment> searchByExam(int examId, String keyword) {
        List<ExamEnrollment> list = new ArrayList<>();
        if (keyword == null || keyword.isBlank() || examId <= 0) {
            return list;
        }
        String like = "%" + keyword.trim() + "%";
        String sql = "SELECT " + ENROLLMENT_COLUMNS
                + " FROM ExamEnrollment ee"
                + " JOIN Candidate c ON c.CandidateId = ee.CandidateId"
                + " WHERE ee.ExamId = ?"
                + " AND (LOWER(c.CandidateNumber) LIKE LOWER(?)"
                + " OR LOWER(c.FullName) LIKE LOWER(?)"
                + " OR LOWER(c.GovernmentIdNumber) LIKE LOWER(?))"
                + " ORDER BY ee.CandidateId";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examId);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapEnrollment(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public ExamEnrollment getByExamAndCandidate(int examId, int candidateId) {
        String sql = "SELECT " + ENROLLMENT_COLUMNS
                + " FROM ExamEnrollment WHERE ExamId = ? AND CandidateId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examId);
            ps.setInt(2, candidateId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapEnrollment(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ExamEnrollment getLatestByCandidateId(int candidateId) {
        String sql = "SELECT TOP 1 " + ENROLLMENT_COLUMNS
                + " FROM ExamEnrollment WHERE CandidateId = ? ORDER BY ExamEnrollmentId DESC";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapEnrollment(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Integer findCandidateIdByGovIdAndExam(String governmentIdNumber, int examId) {
        if (governmentIdNumber == null || governmentIdNumber.isBlank() || examId <= 0) {
            return null;
        }
        String sql = "SELECT TOP 1 e.CandidateId FROM ExamEnrollment e "
                + "JOIN Candidate c ON c.CandidateId = e.CandidateId "
                + "WHERE e.ExamId = ? AND c.GovernmentIdNumber = ? "
                + "ORDER BY e.ExamEnrollmentId DESC";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examId);
            ps.setString(2, governmentIdNumber.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("CandidateId");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean updateExaminerProfile(int candidateId, String fullName, Date dateOfBirth, String govIdNo,
            String phoneNo, String address, String sexDb, String reasonForTaking) {
        if (candidateId <= 0 || fullName == null || fullName.isBlank()) {
            return false;
        }
        Sex sex = Sex.fromValue(sexDb);
        boolean female = sex != null ? sex.toDbBit() : false;
        return candidateDAO.updateExaminerProfile(candidateId, fullName.trim(), dateOfBirth, govIdNo, phoneNo,
                address, female, reasonForTaking);
    }

    @Override
    public boolean markAbsent(int candidateId) {
        return candidateDAO.updateAbsent(candidateId, true);
    }

    @Override
    public boolean clearAbsentMarking(int candidateId) {
        return candidateDAO.updateAbsent(candidateId, false);
    }

    @Override
    public boolean assignExamDevice(int candidateId, int examId, int deviceId) {
        ExamEnrollment enrollment = getByExamAndCandidate(examId, candidateId);
        if (enrollment == null) {
            return false;
        }
        enrollment.setExamDeviceId(deviceId);
        return update(enrollment);
    }

    private ExamEnrollment mapEnrollment(ResultSet rs) throws SQLException {
        ExamEnrollment enrollment = new ExamEnrollment();
        enrollment.setExamEnrollmentId(rs.getInt("ExamEnrollmentId"));
        enrollment.setCandidateId(rs.getInt("CandidateId"));
        enrollment.setExamId(rs.getInt("ExamId"));
        enrollment.setSectionStatus(rs.getString("SectionStatus"));
        enrollment.setSignaturePrinted(rs.getBoolean("SignaturePrinted"));
        int deviceId = rs.getInt("ExamDeviceId");
        if (!rs.wasNull()) {
            enrollment.setExamDeviceId(deviceId);
        }
        return enrollment;
    }
}

