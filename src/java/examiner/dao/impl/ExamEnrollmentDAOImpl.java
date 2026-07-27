package examiner.dao.impl;

import examiner.dao.CandidateDAO;
import examiner.dao.ExamEnrollmentDAO;
import shared.dbconnection.DBContext;
import shared.model.ExamEnrollment;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

// JDBC implementation for ExamEnrollment; examiner module DAO layer only.
public class ExamEnrollmentDAOImpl extends DBContext implements ExamEnrollmentDAO {

    private static final String ENROLLMENT_COLUMNS =
            "ExamEnrollmentId, CandidateId, ExamId, AllocatedExamAreaId, ExamDeviceId";

    private final CandidateDAO candidateDAO = new CandidateDAOImpl();

    // Updates device and allocated area on an enrollment row.
    @Override
    public boolean update(ExamEnrollment enrollment) {
        String sql = "UPDATE ExamEnrollment SET ExamDeviceId = ?, AllocatedExamAreaId = ? "
                + "WHERE ExamEnrollmentId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            if (enrollment.getExamDeviceId() != null) {
                ps.setInt(1, enrollment.getExamDeviceId());
            } else {
                ps.setNull(1, java.sql.Types.INTEGER);
            }
            if (enrollment.getAllocatedExamAreaId() != null) {
                ps.setInt(2, enrollment.getAllocatedExamAreaId());
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }
            ps.setInt(3, enrollment.getExamEnrollmentId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Inserts a new exam-day enrollment and returns generated id.
    @Override
    public int add(ExamEnrollment enrollment) {
        String sql = "INSERT INTO ExamEnrollment (CandidateId, ExamId, AllocatedExamAreaId, ExamDeviceId) "
                + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, enrollment.getCandidateId());
            ps.setInt(2, enrollment.getExamId());
            if (enrollment.getAllocatedExamAreaId() != null) {
                ps.setInt(3, enrollment.getAllocatedExamAreaId());
            } else {
                ps.setNull(3, java.sql.Types.INTEGER);
            }
            if (enrollment.getExamDeviceId() != null) {
                ps.setInt(4, enrollment.getExamDeviceId());
            } else {
                ps.setNull(4, java.sql.Types.INTEGER);
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

    // Lists all enrollments for one exam.
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

    // Searches enrollments in one exam by candidate number, name, or gov id.
    @Override
    public List<ExamEnrollment> getFilteredByExam(int examId, String keyword) {
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

    // Lists enrollments and candidate display fields with one simple query.
    @Override
    public List<ExamEnrollment> getWithCandidateByExam(int examId, String keyword) {
        List<ExamEnrollment> list = new ArrayList<>();
        if (examId <= 0) {
            return list;
        }
        StringBuilder sql = new StringBuilder()
                .append("SELECT ee.ExamEnrollmentId, ee.CandidateId, ee.ExamId, ")
                .append("ee.AllocatedExamAreaId, ee.ExamDeviceId, ")
                .append("c.CandidateNumber, c.FullName, c.DateOfBirth, c.PhoneNumber, c.Email, c.Sex, ")
                .append("c.GovernmentIdNumber, c.Address, c.TakeTheory, c.TakeLayout, c.TakeNo, ")
                .append("c.ReasonForTaking, c.PhotoImageUrl, c.IsAbsent, c.IsSuspended ")
                .append("FROM ExamEnrollment ee ")
                .append("JOIN Candidate c ON c.CandidateId = ee.CandidateId ")
                .append("WHERE ee.ExamId = ? ");
        boolean hasSearch = keyword != null && !keyword.isBlank();
        if (hasSearch) {
            sql.append("AND (c.CandidateNumber LIKE ? OR c.FullName LIKE ? OR c.GovernmentIdNumber LIKE ?) ");
        }
        sql.append("ORDER BY TRY_CONVERT(int, c.CandidateNumber), c.CandidateNumber");

        try (PreparedStatement ps = getConnection().prepareStatement(sql.toString())) {
            ps.setInt(1, examId);
            if (hasSearch) {
                String like = "%" + keyword.trim() + "%";
                ps.setString(2, like);
                ps.setString(3, like);
                ps.setString(4, like);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapEnrollmentWithCandidate(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Loads the enrollment row for one exam and candidate pair.
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

    // Loads the most recent enrollment row for one candidate.
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

    // Loads enrollment for gov id within one exam.
    @Override
    public ExamEnrollment getIfByGovIdAndExam(String governmentIdNumber, int examId) {
        if (governmentIdNumber == null || governmentIdNumber.isBlank() || examId <= 0) {
            return null;
        }
        String sql = "SELECT TOP 1 " + ENROLLMENT_COLUMNS
                + " FROM ExamEnrollment e "
                + "JOIN Candidate c ON c.CandidateId = e.CandidateId "
                + "WHERE e.ExamId = ? AND c.GovernmentIdNumber = ? "
                + "ORDER BY e.ExamEnrollmentId DESC";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examId);
            ps.setString(2, governmentIdNumber.trim());
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

    // Sets IsAbsent=true on the candidate linked to this enrollment.
    @Override
    public boolean markAbsent(int candidateId) {
        return candidateDAO.updateAbsent(candidateId, true);
    }

    // Clears IsAbsent on the candidate linked to this enrollment.
    @Override
    public boolean clearAbsentMarking(int candidateId) {
        return candidateDAO.updateAbsent(candidateId, false);
    }

    // Assigns an exam device to a candidate enrollment for one exam.
    @Override
    public boolean assignExamDevice(int candidateId, int examId, int deviceId) {
        ExamEnrollment enrollment = getByExamAndCandidate(examId, candidateId);
        if (enrollment == null) {
            return false;
        }
        enrollment.setExamDeviceId(deviceId);
        return update(enrollment);
    }

    // Private helper: map enrollment.
    private ExamEnrollment mapEnrollment(ResultSet rs) throws SQLException {
        ExamEnrollment enrollment = new ExamEnrollment();
        enrollment.setExamEnrollmentId(rs.getInt("ExamEnrollmentId"));
        enrollment.setCandidateId(rs.getInt("CandidateId"));
        enrollment.setExamId(rs.getInt("ExamId"));
        int areaId = rs.getInt("AllocatedExamAreaId");
        if (!rs.wasNull()) {
            enrollment.setAllocatedExamAreaId(areaId);
        }
        int deviceId = rs.getInt("ExamDeviceId");
        if (!rs.wasNull()) {
            enrollment.setExamDeviceId(deviceId);
        }
        return enrollment;
    }

    private ExamEnrollment mapEnrollmentWithCandidate(ResultSet rs) throws SQLException {
        ExamEnrollment enrollment = mapEnrollment(rs);
        shared.model.Candidate candidate = new shared.model.Candidate();
        candidate.setCandidateId(rs.getInt("CandidateId"));
        candidate.setCandidateNumber(rs.getString("CandidateNumber"));
        candidate.setFullName(rs.getString("FullName"));
        candidate.setDateOfBirth(rs.getTimestamp("DateOfBirth"));
        candidate.setPhoneNumber(rs.getString("PhoneNumber"));
        candidate.setEmail(rs.getString("Email"));
        candidate.setSex(rs.getBoolean("Sex"));
        candidate.setGovernmentIdNumber(rs.getString("GovernmentIdNumber"));
        candidate.setAddress(rs.getString("Address"));
        candidate.setTakeTheory(readBooleanObject(rs, "TakeTheory"));
        candidate.setTakeLayout(readBooleanObject(rs, "TakeLayout"));
        candidate.setTakeNo(rs.getInt("TakeNo"));
        candidate.setReasonForTaking(rs.getString("ReasonForTaking"));
        candidate.setPhotoImageUrl(rs.getString("PhotoImageUrl"));
        candidate.setAbsent(rs.getBoolean("IsAbsent"));
        candidate.setSuspended(rs.getBoolean("IsSuspended"));
        enrollment.setCandidate(candidate);
        return enrollment;
    }

    private Boolean readBooleanObject(ResultSet rs, String column) throws SQLException {
        boolean value = rs.getBoolean(column);
        return rs.wasNull() ? null : value;
    }
}
