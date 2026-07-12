package examstaff.dao.impl;

import examstaff.dao.CandidateDAO;
import examstaff.dao.Db2ExamSchemaSql;
import examstaff.dao.ExamEnrollmentDAO;
import shared.dbconnection.DBContext;
import shared.enums.SectionStatus;
import shared.model.Candidate;
import shared.model.ExamEnrollment;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ExamEnrollmentDAOImpl extends DBContext implements ExamEnrollmentDAO {

    private static final String ENROLLMENT_SELECT = """
            SELECT ee.ExamEnrollmentId, ee.CandidateId, ee.ExamId,
                   theoryEes.Status AS SectionStatus,
                   CAST(CASE
                     WHEN theoryEes.CompletedAt IS NOT NULL
                      AND theoryEes.Status = N'AwaitingSignature' THEN 1
                     ELSE 0
                   END AS BIT) AS SignaturePrinted,
                   ee.AllocatedExamAreaId, COALESCE(theoryEes.ExamDeviceId, ee.ExamDeviceId) AS ExamDeviceId
            FROM ExamEnrollment ee
            LEFT JOIN ExamSection theorySec ON theorySec.ExamId = ee.ExamId
              AND theorySec.SectionType IN (""" + Db2ExamSchemaSql.THEORY_SECTION_TYPES + """
            )
            LEFT JOIN ExamEnrollmentSection theoryEes
              ON theoryEes.ExamEnrollmentId = ee.ExamEnrollmentId
             AND theoryEes.ExamSectionId = theorySec.ExamSectionId
            """;

    private final CandidateDAO candidateDAO = new CandidateDAOImpl();

    @Override
    public ExamEnrollment getById(int examEnrollmentId) {
        String sql = ENROLLMENT_SELECT + " WHERE ee.ExamEnrollmentId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examEnrollmentId);
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
    public int insert(ExamEnrollment enrollment) {
        String sql = "INSERT INTO ExamEnrollment (CandidateId, ExamId, AllocatedExamAreaId, ExamDeviceId) "
                + "VALUES (?,?,?,?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
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
                        int id = keys.getInt(1);
                        ensureTheorySectionRow(getConnection(), id, enrollment.getExamId());
                        if (enrollment.getSectionStatus() != null) {
                            updateTheoryStatus(enrollment.getCandidateId(), enrollment.getExamId(),
                                    enrollment.getSectionStatus());
                        }
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public boolean update(ExamEnrollment enrollment) {
        String sql = "UPDATE ExamEnrollment SET AllocatedExamAreaId = ?, ExamDeviceId = ? WHERE ExamEnrollmentId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            if (enrollment.getAllocatedExamAreaId() != null) {
                ps.setInt(1, enrollment.getAllocatedExamAreaId());
            } else {
                ps.setNull(1, java.sql.Types.INTEGER);
            }
            if (enrollment.getExamDeviceId() != null) {
                ps.setInt(2, enrollment.getExamDeviceId());
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }
            ps.setInt(3, enrollment.getExamEnrollmentId());
            boolean ok = ps.executeUpdate() > 0;
            if (ok && enrollment.getSectionStatus() != null) {
                ExamEnrollment current = getById(enrollment.getExamEnrollmentId());
                if (current != null) {
                    updateTheoryStatus(current.getCandidateId(), current.getExamId(), enrollment.getSectionStatus());
                }
            }
            return ok;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(int examEnrollmentId) {
        String sql = "DELETE FROM ExamEnrollment WHERE ExamEnrollmentId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examEnrollmentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM ExamEnrollment";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public List<ExamEnrollment> getByExamId(int examId) {
        List<ExamEnrollment> list = new ArrayList<>();
        String sql = ENROLLMENT_SELECT + " WHERE ee.ExamId = ? ORDER BY ee.CandidateId";
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
    public boolean updateExaminerProfile(int candidateId, String fullName, Date dob, String govIdNo,
            String email, String phoneNo, String address, String sexDb, String reasonForTaking) {
        Candidate candidate = candidateDAO.getById(candidateId);
        if (candidate == null || fullName == null || fullName.isBlank()) {
            return false;
        }
        candidate.setFullName(fullName.trim());
        if (dob != null) {
            candidate.setDateOfBirth(new Timestamp(dob.getTime()));
        }
        candidate.setGovernmentIdNumber(govIdNo != null ? govIdNo.trim() : null);
        if (phoneNo != null) {
            candidate.setPhoneNumber(phoneNo);
        }
        if (address != null) {
            candidate.setAddress(address);
        }
        if (reasonForTaking != null) {
            candidate.setReasonForTaking(reasonForTaking);
        }
        candidate.setSex("NÃ¡Â»Â¯".equalsIgnoreCase(sexDb) || "1".equals(sexDb));
        return candidateDAO.update(candidate);
    }

    @Override
    public boolean markAbsent(int candidateId) {
        Candidate candidate = candidateDAO.getById(candidateId);
        if (candidate == null) {
            return false;
        }
        candidate.setAbsent(true);
        return candidateDAO.update(candidate);
    }

    @Override
    public boolean clearAbsentMarking(int candidateId) {
        Candidate candidate = candidateDAO.getById(candidateId);
        if (candidate == null) {
            return false;
        }
        candidate.setAbsent(false);
        return candidateDAO.update(candidate);
    }

    @Override
    public boolean assignExamDevice(int regId, int examId, int deviceId) {
        ExamEnrollment enrollment = getById(regId);
        if (enrollment == null || enrollment.getExamId() != examId) {
            return false;
        }
        enrollment.setExamDeviceId(deviceId);
        return update(enrollment);
    }

    @Override
    public ExamEnrollment getByExamAndCandidate(int examId, int candidateId) {
        String sql = ENROLLMENT_SELECT + " WHERE ee.ExamId = ? AND ee.CandidateId = ?";
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

    private static void ensureTheorySectionRow(Connection conn, int examEnrollmentId, int examId)
            throws SQLException {
        ExamEnrollmentSectionSupport.ensureSections(conn, examEnrollmentId, examId, true, true);
    }

    private boolean updateTheoryStatus(int candidateId, int examId, String status) {
        try {
            return ExamEnrollmentSectionSupport.updateTheoryStatus(getConnection(), candidateId, examId,
                    status != null ? status : "ChÃ†Â°a thi");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static ExamEnrollment mapEnrollment(ResultSet rs) throws SQLException {
        ExamEnrollment enrollment = new ExamEnrollment();
        enrollment.setExamEnrollmentId(rs.getInt("ExamEnrollmentId"));
        enrollment.setCandidateId(rs.getInt("CandidateId"));
        enrollment.setExamId(rs.getInt("ExamId"));
        enrollment.setSectionStatus(rs.getString("SectionStatus"));
        enrollment.setSignaturePrinted(rs.getBoolean("SignaturePrinted"));
        int allocatedAreaId = rs.getInt("AllocatedExamAreaId");
        if (!rs.wasNull()) {
            enrollment.setAllocatedExamAreaId(allocatedAreaId);
        }
        int deviceId = rs.getInt("ExamDeviceId");
        if (!rs.wasNull()) {
            enrollment.setExamDeviceId(deviceId);
        }
        return enrollment;
    }

    // --- mainTest-only methods (thÃƒÂªm tÃ¡Â»Â« union interface) ---

    @Override
    public List<ExamEnrollment> searchByExam(int examId, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return getByExamId(examId);
        }
        List<ExamEnrollment> list = new ArrayList<>();
        String sql = ENROLLMENT_SELECT
                + " JOIN Candidate c ON c.CandidateId = ee.CandidateId"
                + " WHERE ee.ExamId = ?"
                + " AND (c.FullName LIKE ? OR c.GovernmentIdNumber LIKE ?)"
                + " ORDER BY ee.CandidateId";
        String like = "%" + keyword.trim() + "%";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examId);
            ps.setString(2, like);
            ps.setString(3, like);
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
    public ExamEnrollment getLatestByCandidateId(int candidateId) {
        String sql = ENROLLMENT_SELECT
                + " WHERE ee.CandidateId = ?"
                + " ORDER BY ee.ExamEnrollmentId DESC";
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
        String sql = "SELECT ee.CandidateId FROM ExamEnrollment ee"
                + " JOIN Candidate c ON c.CandidateId = ee.CandidateId"
                + " WHERE c.GovernmentIdNumber = ? AND ee.ExamId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, governmentIdNumber);
            ps.setInt(2, examId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}


