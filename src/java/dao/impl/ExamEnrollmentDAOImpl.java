package dao.impl;

import dao.CandidateDAO;
import dao.ExamEnrollmentDAO;
import dbconnection.DBContext;
import dto.CandidateEnrollmentDTO;
import dto.CandidateProfileDTO;
import model.Candidate;
import model.ExamEnrollment;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ExamEnrollmentDAOImpl extends DBContext implements ExamEnrollmentDAO {

    private static final String ENROLLMENT_SELECT = """
            SELECT c.CandidateId, c.CandidateNumber, c.FullName, c.DateOfBirth, c.PhoneNumber, c.Sex,
                   c.GovernmentIdNumber, c.Address, c.ReasonForTaking, c.PhotoImageUrl,
                   c.IsAbsent, c.IsSuspended,
                   ee.ExamEnrollmentId, ee.SessionId, ee.SectionStatus, ee.SignaturePrinted, ee.ExamDeviceId
            FROM ExamEnrollment ee
            INNER JOIN Candidate c ON c.CandidateId = ee.CandidateId
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
        String sql = "INSERT INTO ExamEnrollment (CandidateId, SessionId, SectionStatus, SignaturePrinted, ExamDeviceId) "
                + "VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, enrollment.getCandidateId());
            ps.setInt(2, enrollment.getSessionId());
            ps.setString(3, enrollment.getSectionStatus() != null ? enrollment.getSectionStatus() : "Pending");
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
    public List<ExamEnrollment> getBySessionId(int sessionId) {
        List<ExamEnrollment> list = new ArrayList<>();
        String sql = ENROLLMENT_SELECT + " WHERE ee.SessionId = ? ORDER BY c.CandidateNumber";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, sessionId);
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
    public List<CandidateEnrollmentDTO> getCandidatesBySession(int sessionId) {
        List<CandidateEnrollmentDTO> list = new ArrayList<>();
        String sql = ENROLLMENT_SELECT + " WHERE ee.SessionId = ? ORDER BY c.CandidateNumber";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapDto(rs));
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
        Candidate c = candidateDAO.getById(candidateId);
        if (c == null || fullName == null || fullName.isBlank()) {
            return false;
        }
        c.setFullName(fullName.trim());
        if (dob != null) {
            c.setDateOfBirth(new Timestamp(dob.getTime()));
        }
        c.setGovernmentIdNumber(govIdNo != null ? govIdNo.trim() : null);
        if (phoneNo != null) {
            c.setPhoneNumber(phoneNo);
        }
        if (address != null) {
            c.setAddress(address);
        }
        if (reasonForTaking != null) {
            c.setReasonForTaking(reasonForTaking);
        }
        c.setSex("Nữ".equalsIgnoreCase(sexDb) || "1".equals(sexDb));
        return candidateDAO.update(c);
    }

    @Override
    public boolean markAbsent(int candidateId) {
        Candidate c = candidateDAO.getById(candidateId);
        if (c == null) {
            return false;
        }
        c.setAbsent(true);
        return candidateDAO.update(c);
    }

    @Override
    public boolean clearAbsentMarking(int candidateId) {
        Candidate c = candidateDAO.getById(candidateId);
        if (c == null) {
            return false;
        }
        c.setAbsent(false);
        return candidateDAO.update(c);
    }

    @Override
    public boolean assignExamDevice(int regId, int sessionId, int deviceId) {
        ExamEnrollment e = getById(regId);
        if (e == null || e.getSessionId() != sessionId) {
            return false;
        }
        e.setExamDeviceId(deviceId);
        return update(e);
    }

    @Override
    public ExamEnrollment getBySessionAndCandidate(int sessionId, int candidateId) {
        String sql = ENROLLMENT_SELECT + " WHERE ee.SessionId = ? AND ee.CandidateId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, sessionId);
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

    private static ExamEnrollment mapEnrollment(ResultSet rs) throws SQLException {
        ExamEnrollment e = new ExamEnrollment();
        e.setExamEnrollmentId(rs.getInt("ExamEnrollmentId"));
        e.setCandidateId(rs.getInt("CandidateId"));
        e.setSessionId(rs.getInt("SessionId"));
        e.setSectionStatus(rs.getString("SectionStatus"));
        e.setSignaturePrinted(rs.getBoolean("SignaturePrinted"));
        int deviceId = rs.getInt("ExamDeviceId");
        if (!rs.wasNull()) {
            e.setExamDeviceId(deviceId);
        }
        return e;
    }

    private static CandidateEnrollmentDTO mapDto(ResultSet rs) throws SQLException {
        CandidateProfileDTO profile = new CandidateProfileDTO();
        profile.setCandidateId(rs.getInt("CandidateId"));
        profile.setCandidateNumber(parseCandidateNumber(rs.getString("CandidateNumber")));
        profile.setFullName(rs.getString("FullName"));
        profile.setGovernmentIdNumber(rs.getString("GovernmentIdNumber"));
        profile.setAbsent(rs.getBoolean("IsAbsent"));
        profile.setSuspended(rs.getBoolean("IsSuspended"));
        profile.setPhotoImageUrl(rs.getString("PhotoImageUrl"));

        ExamEnrollment enrollment = mapEnrollment(rs);
        CandidateEnrollmentDTO dto = new CandidateEnrollmentDTO(profile, enrollment);
        dto.setDateOfBirth(rs.getTimestamp("DateOfBirth"));
        dto.setPhoneNo(rs.getString("PhoneNumber"));
        dto.setAddress(rs.getString("Address"));
        dto.setReasonForTaking(rs.getString("ReasonForTaking"));
        dto.setSex(rs.getBoolean("Sex"));
        return dto;
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
