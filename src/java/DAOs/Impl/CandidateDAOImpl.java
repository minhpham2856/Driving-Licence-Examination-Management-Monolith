package DAOs.Impl;

import Utils.ExamConstants;
import DBConnection.DBContext;
import DAOs.Db2CandidateSql;
import DAOs.CandidateDAO;
import DTOs.CandidateDTO;
import Models.Candidate;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CandidateDAOImpl implements CandidateDAO {

    private final DBContext ctx;

    public CandidateDAOImpl() {
        this.ctx = new DBContext();
    }

    @Override
    public Candidate findById(int id) {
        String sql = "SELECT * FROM Candidate WHERE CandidateId = ?";
        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapToCandidateModel(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Candidate findByNumber(int sessionId, String candidateNumber) {
        String sql = "SELECT c.* FROM Candidate c INNER JOIN ExamEnrollment ec ON ec.CandidateId = c.CandidateId WHERE ec.SessionId = ? AND c.CandidateNumber = ?";
        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            ps.setString(2, candidateNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapToCandidateModel(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Candidate mapToCandidateModel(ResultSet rs) throws SQLException {
        Candidate c = new Candidate();
        c.setCandidateId(rs.getInt("CandidateId"));
        c.setCandidateNumber(rs.getString("CandidateNumber"));
        c.setFullName(rs.getString("FullName"));
        c.setDateOfBirth(rs.getTimestamp("DateOfBirth"));
        c.setPhoneNumber(rs.getString("PhoneNumber"));
        c.setSex(rs.getString("Sex"));
        c.setGovernmentIdNumber(rs.getString("GovernmentIdNumber"));
        c.setAddress(rs.getString("Address"));
        c.setTakeTheory(rs.getBoolean("TakeTheory"));
        c.setTakePractical(rs.getBoolean("TakePractical"));
        c.setTakeRoadLayout(rs.getBoolean("TakeRoadLayout"));
        c.setTakeOnRoad(rs.getBoolean("TakeOnRoad"));
        c.setReasonForTaking(rs.getString("ReasonForTaking"));
        c.setPhotoImageUrl(rs.getString("PhotoImageUrl"));
        c.setIsAbsent(rs.getBoolean("IsAbsent"));
        c.setIsSuspended(rs.getBoolean("IsSuspended"));
        c.setUserId(rs.getInt("UserId"));
        c.setTakeNo(rs.getInt("TakeNo"));
        return c;
    }

    @Override
    public CandidateDTO getById(int id) {
        String sql = Db2CandidateSql.CANDIDATE_SELECT + " WHERE c.CandidateId = ?";
        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCandidateDTO(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public CandidateDTO getBySessionAndSbd(int sessionId, String sbd) {
        if (sbd == null || sbd.isBlank()) {
            return null;
        }
        String normalized = sbd.trim();
        try {
            String sql = Db2CandidateSql.CANDIDATE_SELECT
                    + """
                     WHERE ec.SessionId = ?
                       AND (
                            c.CandidateNumber = ?
                            OR TRY_CAST(c.CandidateNumber AS INT) = TRY_CAST(? AS INT)
                            OR TRY_CAST(SUBSTRING(c.CandidateNumber, CHARINDEX('-', c.CandidateNumber) + 1, 10) AS INT) = TRY_CAST(? AS INT)
                       )
                    """;
            try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
                ps.setInt(1, sessionId);
                ps.setString(2, normalized);
                ps.setString(3, normalized);
                ps.setString(4, normalized);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToCandidateDTO(rs);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<CandidateDTO> getCandidatesBySession(int sessionId) {
        List<CandidateDTO> list = new ArrayList<>();
        String sql = Db2CandidateSql.CANDIDATE_SELECT
                + " WHERE ec.SessionId = ? ORDER BY candidateNo";
        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToCandidateDTO(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<CandidateDTO> getAll() {
        List<CandidateDTO> list = new ArrayList<>();
        String sql = Db2CandidateSql.CANDIDATE_SELECT
                + " ORDER BY CAST(s.StartTime AS DATE) DESC, candidateNo";
        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToCandidateDTO(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public boolean updatePresent(int id, boolean isPresent) {
        String sql = """
                UPDATE ExamRegistration
                SET RegistrationStatus = ?
                WHERE ExamRegistrationId = (SELECT ExamRegistrationId FROM Candidate WHERE CandidateId = ?)
                """;
        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
            ps.setString(1, isPresent ? "CheckedIn" : "PreRegistered");
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updatePayment(int id, boolean isPaymentCompleted) {
        if (!isPaymentCompleted) {
            return true;
        }
        try {
            String check = """
                    SELECT TOP 1 PaymentId FROM Payment
                    WHERE CandidateId = ? AND PaymentStatus IN ('Completed', 'Paid')
                    """;
            try (PreparedStatement ps = ctx.getConnection().prepareStatement(check)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return true;
                    }
                }
            }
            int examId = resolveExamIdForCandidate(id);
            if (examId <= 0) {
                return false;
            }
            String ins = """
                    INSERT INTO Payment (PaymentStatus, PaymentMethod, TransactionReference, TotalAmount, PaidAt, CandidateId, ExamId)
                    VALUES ('Completed', 'Cash', ?, 200000, GETDATE(), ?, ?)
                    """;
            try (PreparedStatement ps = ctx.getConnection().prepareStatement(ins)) {
                ps.setString(1, "REF-" + System.currentTimeMillis() % 1000000);
                ps.setInt(2, id);
                ps.setInt(3, examId);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private int resolveExamIdForCandidate(int candidateId) throws SQLException {
        String sql = "SELECT TOP 1 ExamId FROM ExamEnrollment WHERE CandidateId = ?";
        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ExamId");
                }
            }
        }
        return -1;
    }

    @Override
    public boolean updateComputer(int id, String computerCode) {
        try {
            Integer examCandidateId = getExamEnrollmentId(id);
            if (examCandidateId == null) {
                return false;
            }
            int deviceId = -1;
            if (computerCode != null && !computerCode.isEmpty()) {
                String compSql = "SELECT ExamDeviceId FROM ExamDevice WHERE DeviceName = ? OR DeviceName LIKE ?";
                try (PreparedStatement ps = ctx.getConnection().prepareStatement(compSql)) {
                    ps.setString(1, computerCode);
                    ps.setString(2, "%" + computerCode + "%");
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            deviceId = rs.getInt("ExamDeviceId");
                        }
                    }
                }
            }
            if (deviceId == -1) {
                return false;
            }
            int paperId = -1;
            String checkPaper = "SELECT TheoryPaperId FROM TheoryPaper WHERE ExamEnrollmentId = ?";
            try (PreparedStatement ps = ctx.getConnection().prepareStatement(checkPaper)) {
                ps.setInt(1, examCandidateId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        paperId = rs.getInt("TheoryPaperId");
                    }
                }
            }
            if (paperId == -1) {
                String ins = "INSERT INTO TheoryPaper (ExamEnrollmentId, ExamDeviceId, StartedAt) VALUES (?, ?, GETDATE())";
                try (PreparedStatement ps = ctx.getConnection().prepareStatement(ins)) {
                    ps.setInt(1, examCandidateId);
                    ps.setInt(2, deviceId);
                    return ps.executeUpdate() > 0;
                }
            }
            String upd = "UPDATE TheoryPaper SET ExamDeviceId = ? WHERE TheoryPaperId = ?";
            try (PreparedStatement ps = ctx.getConnection().prepareStatement(upd)) {
                ps.setInt(1, deviceId);
                ps.setInt(2, paperId);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Integer getExamEnrollmentId(int candidateId) throws SQLException {
        String sql = "SELECT ExamEnrollmentId FROM ExamEnrollment WHERE CandidateId = ?";
        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ExamEnrollmentId");
                }
            }
        }
        return null;
    }

    private Integer getExamEnrollmentIdForSession(int candidateId, int sessionId) throws SQLException {
        String sql = "SELECT ExamEnrollmentId FROM ExamEnrollment WHERE CandidateId = ? AND SessionId = ?";
        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            ps.setInt(2, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ExamEnrollmentId");
                }
            }
        }
        return null;
    }

    private Integer ensureExamEnrollmentId(int candidateId) throws SQLException {
        Integer id = getExamEnrollmentId(candidateId);
        if (id == null) {
            throw new SQLException("ExamCandidate record not found for CandidateId: " + candidateId);
        }
        return id;
    }

    @Override
    public boolean updateAllocatedRoom(int id, int areaId, String areaName) {
        return updateApplicationNotes(id, "AllocatedRoom:" + areaId + ":" + areaName);
    }

    @Override
    public boolean updateDevice(int id, String deviceCode) {
        String notesVal = (deviceCode != null && !deviceCode.isEmpty()) ? "Device: " + deviceCode : null;
        return updateApplicationNotes(id, notesVal);
    }

    private boolean updateApplicationNotes(int id, String notes) {
        String sql = """
                UPDATE ExamRegistration
                SET Notes = ?
                WHERE ExamRegistrationId = (SELECT ExamRegistrationId FROM Candidate WHERE CandidateId = ?)
                """;
        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
            if (notes == null) {
                ps.setNull(1, Types.NVARCHAR);
            } else {
                ps.setString(1, notes);
            }
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updateScores(int id, Integer theoryScore, String theoryPassed,
            Integer practicalScore, String practicalPassed) {
        try {
            boolean ok = true;
            if (theoryScore != null) {
                boolean passed = theoryPassed != null
                        ? "passed".equalsIgnoreCase(theoryPassed)
                        : isTheoryPassed(theoryScore);
                ok = upsertSectionScore(id, "Theory", theoryScore, passed) && ok;
            }
            if (practicalScore != null) {
                boolean passed = practicalPassed != null
                        ? "passed".equalsIgnoreCase(practicalPassed)
                        : practicalScore >= 80;
                ok = upsertSectionScore(id, "Practical", practicalScore, passed) && ok;
            }
            return ok;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updateTheoryCorrectCount(int id, int correctCount, int passThreshold) {
        try {
            Integer examCandidateId = ensureExamEnrollmentId(id);
            if (examCandidateId == null) {
                return false;
            }
            Integer sectionId = findTheorySectionIdByCandidate(id);
            if (sectionId == null) {
                sectionId = findSectionIdForCandidate(examCandidateId, "Theory");
            }
            if (sectionId == null) {
                return false;
            }
            return upsertExamScore(examCandidateId, sectionId, correctCount, correctCount >= passThreshold);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Integer findTheorySectionIdByCandidate(int candidateId) throws SQLException {
        String sql = """
                SELECT TOP 1 ExamSectionId FROM ExamSection
                WHERE SectionName LIKE N'%Lý thuyết%' OR SectionName LIKE '%Theory%'
                """;
        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("ExamSectionId");
            }
        }
        return null;
    }

    @Override
    public boolean updateRoadScore(int id, Integer roadScore, String roadPassed) {
        try {
            if (roadScore != null) {
                return upsertSectionScore(id, "Road", roadScore);
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updateProfile(int id, String fullName, Date dob, String govIdNo, String email, String phoneNo) {
        String sqlCand = """
                UPDATE Candidate
                SET FullName = ?, DateOfBirth = ?, GovernmentIdNumber = ?, PhoneNumber = ?
                WHERE CandidateId = ?
                """;
        String sqlProf = """
                UPDATE Profile
                SET FullName = ?, DateOfBirth = ?, GovernmentIdNumber = ?, PhoneNumber = ?
                WHERE ProfileId = (SELECT er.ProfileId FROM Candidate c JOIN ExamRegistration er ON er.ExamRegistrationId = c.ExamRegistrationId WHERE c.CandidateId = ?)
                """;
        String sqlUser = """
                UPDATE [User] SET Email = ?
                WHERE UserId = (SELECT UserId FROM Candidate WHERE CandidateId = ?)
                """;
        try {
            ctx.getConnection().setAutoCommit(false);
            try (PreparedStatement ps = ctx.getConnection().prepareStatement(sqlCand)) {
                ps.setString(1, fullName);
                ps.setDate(2, dob);
                ps.setString(3, govIdNo);
                ps.setString(4, phoneNo);
                ps.setInt(5, id);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = ctx.getConnection().prepareStatement(sqlProf)) {
                ps.setString(1, fullName);
                ps.setDate(2, dob);
                ps.setString(3, govIdNo);
                ps.setString(4, phoneNo);
                ps.setInt(5, id);
                ps.executeUpdate();
            }
            if (email != null) {
                try (PreparedStatement ps = ctx.getConnection().prepareStatement(sqlUser)) {
                    ps.setString(1, email);
                    ps.setInt(2, id);
                    ps.executeUpdate();
                }
            }
            ctx.getConnection().commit();
            return true;
        } catch (SQLException e) {
            try {
                ctx.getConnection().rollback();
            } catch (SQLException ignored) {
            }
            e.printStackTrace();
        } finally {
            try {
                ctx.getConnection().setAutoCommit(true);
            } catch (SQLException ignored) {
            }
        }
        return false;
    }

    @Override
    public boolean updateExaminerProfile(int id, String fullName, Date dob, String govIdNo,
            String email, String phoneNo, String address, String sex, String reasonForTaking) {
        String sqlCand = """
                UPDATE Candidate
                SET FullName = ?, DateOfBirth = ?, GovernmentIdNumber = ?, PhoneNumber = ?,
                    Address = ?, Sex = ?, ReasonForTaking = ?
                WHERE CandidateId = ?
                """;
        String sqlProf = """
                UPDATE Profile
                SET FullName = ?, DateOfBirth = ?, GovernmentIdNumber = ?, PhoneNumber = ?, Address = ?
                WHERE ProfileId = (
                    SELECT er.ProfileId FROM Candidate c
                    JOIN ExamRegistration er ON er.ExamRegistrationId = c.ExamRegistrationId
                    WHERE c.CandidateId = ?
                )
                """;
        String sqlUser = """
                UPDATE [User] SET Email = ?
                WHERE UserId = (SELECT UserId FROM Candidate WHERE CandidateId = ?)
                """;
        try {
            ctx.getConnection().setAutoCommit(false);
            try (PreparedStatement ps = ctx.getConnection().prepareStatement(sqlCand)) {
                ps.setString(1, fullName);
                ps.setDate(2, dob);
                ps.setString(3, govIdNo);
                ps.setString(4, phoneNo);
                ps.setString(5, address);
                ps.setString(6, sex);
                ps.setString(7, reasonForTaking);
                ps.setInt(8, id);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = ctx.getConnection().prepareStatement(sqlProf)) {
                ps.setString(1, fullName);
                ps.setDate(2, dob);
                ps.setString(3, govIdNo);
                ps.setString(4, phoneNo);
                ps.setString(5, address);
                ps.setInt(6, id);
                ps.executeUpdate();
            }
            if (email != null) {
                try (PreparedStatement ps = ctx.getConnection().prepareStatement(sqlUser)) {
                    ps.setString(1, email);
                    ps.setInt(2, id);
                    ps.executeUpdate();
                }
            }
            ctx.getConnection().commit();
            return true;
        } catch (SQLException e) {
            try {
                ctx.getConnection().rollback();
            } catch (SQLException ignored) {
            }
            e.printStackTrace();
        } finally {
            try {
                ctx.getConnection().setAutoCommit(true);
            } catch (SQLException ignored) {
            }
        }
        return false;
    }

    @Override
    public boolean updatePhoto(int id, String photoUrl) {
        String sql = "UPDATE Candidate SET PhotoImageUrl = ? WHERE CandidateId = ?";
        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
            if (photoUrl != null) {
                ps.setString(1, photoUrl);
            } else {
                ps.setNull(1, Types.NVARCHAR);
            }
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean insert(CandidateDTO reg) {
        try {
            ctx.getConnection().setAutoCommit(false);
            SessionContext sessionCtx = loadSessionContext(reg.getExamSessionId());
            if (sessionCtx == null) {
                ctx.getConnection().rollback();
                return false;
            }
            int applicationId = findOrCreateApplication(reg, sessionCtx.licenceId);
            if (applicationId <= 0) {
                ctx.getConnection().rollback();
                return false;
            }
            int userId = findUserIdByProfile(reg.getPersonId());
            PersonSnapshot snap = loadProfileSnapshot(reg.getPersonId());
            String candidateNumber = ExamConstants.buildCandidateNumber(sessionCtx.licenseCode, reg.getCandidateNo());
            String sqlCand = """
                    INSERT INTO Candidate (CandidateNumber, FullName, DateOfBirth, PhoneNumber, Sex,
                        GovernmentIdNumber, Address, UserId, ExamRegistrationId)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """;
            int candidateId;
            try (PreparedStatement ps = ctx.getConnection().prepareStatement(sqlCand, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, candidateNumber);
                ps.setString(2, snap.fullName);
                ps.setTimestamp(3, snap.dob);
                ps.setString(4, snap.phone);
                ps.setString(5, snap.sex);
                ps.setString(6, snap.govId);
                ps.setString(7, snap.address);
                if (userId > 0) {
                    ps.setInt(8, userId);
                } else {
                    ps.setNull(8, java.sql.Types.INTEGER);
                }
                ps.setInt(9, applicationId);
                ps.executeUpdate();
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (!gk.next()) {
                        ctx.getConnection().rollback();
                        return false;
                    }
                    candidateId = gk.getInt(1);
                }
            }
            String sqlEc = "INSERT INTO ExamEnrollment (ExamId, CandidateId, SessionId) VALUES (?, ?, ?)";
            try (PreparedStatement ps = ctx.getConnection().prepareStatement(sqlEc)) {
                ps.setInt(1, sessionCtx.examId);
                ps.setInt(2, candidateId);
                ps.setInt(3, reg.getExamSessionId());
                ps.executeUpdate();
            }
            if (reg.isPresent()) {
                updatePresent(candidateId, true);
            }
            ctx.getConnection().commit();
            reg.setId(candidateId);
            return true;
        } catch (SQLException e) {
            try {
                ctx.getConnection().rollback();
            } catch (SQLException ignored) {
            }
            e.printStackTrace();
        } finally {
            try {
                ctx.getConnection().setAutoCommit(true);
            } catch (SQLException ignored) {
            }
        }
        return false;
    }

    private SessionContext loadSessionContext(int sessionId) throws SQLException {
        String sql = """
                SELECT s.ExamId, e.LicenceId, l.LicenceClass
                FROM [Session] s
                JOIN Exam e ON e.ExamId = s.ExamId
                JOIN Licence l ON l.LicenceId = e.LicenceId
                WHERE s.SessionId = ?
                """;
        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    SessionContext sessionCtx = new SessionContext();
                    sessionCtx.examId = rs.getInt("ExamId");
                    sessionCtx.licenceId = rs.getInt("LicenceId");
                    sessionCtx.licenseCode = rs.getString("LicenceClass");
                    return sessionCtx;
                }
            }
        }
        return null;
    }

    private int findOrCreateApplication(CandidateDTO reg, int licenceId) throws SQLException {
        String check = "SELECT ExamRegistrationId FROM ExamRegistration WHERE ProfileId = ? AND LicenceId = ?";
        try (PreparedStatement ps = ctx.getConnection().prepareStatement(check)) {
            ps.setInt(1, reg.getPersonId());
            ps.setInt(2, licenceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int appId = rs.getInt("ExamRegistrationId");
                    try (PreparedStatement upd = ctx.getConnection().prepareStatement(
                            "UPDATE ExamRegistration SET RegistrationStatus = ?, Notes = ? WHERE ExamRegistrationId = ?")) {
                        upd.setString(1, reg.getRegistrationType() != null ? reg.getRegistrationType() : "WalkIn");
                        upd.setString(2, reg.getNotes());
                        upd.setInt(3, appId);
                        upd.executeUpdate();
                    }
                    return appId;
                }
            }
        }
        String ins = """
                INSERT INTO ExamRegistration (RegistrationStatus, Notes, ProfileId, LicenceId)
                VALUES (?, ?, ?, ?)
                """;
        try (PreparedStatement ps = ctx.getConnection().prepareStatement(ins, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, reg.getRegistrationType() != null ? reg.getRegistrationType() : "WalkIn");
            ps.setString(2, reg.getNotes());
            ps.setInt(3, reg.getPersonId());
            ps.setInt(4, licenceId);
            ps.executeUpdate();
            try (ResultSet gk = ps.getGeneratedKeys()) {
                if (gk.next()) {
                    return gk.getInt(1);
                }
            }
        }
        return 0;
    }

    private int findUserIdByProfile(int profileId) throws SQLException {
        String sql = "SELECT UserId FROM Profile WHERE ProfileId = ?";
        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
            ps.setInt(1, profileId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("UserId");
                }
            }
        }
        return 0;
    }

    private PersonSnapshot loadProfileSnapshot(int profileId) throws SQLException {
        String sql = "SELECT FullName, DateOfBirth, PhoneNumber, Sex, GovernmentIdNumber, Address FROM Profile WHERE ProfileId = ?";
        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
            ps.setInt(1, profileId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    PersonSnapshot snap = new PersonSnapshot();
                    snap.fullName = rs.getString("FullName");
                    snap.dob = rs.getTimestamp("DateOfBirth");
                    snap.phone = rs.getString("PhoneNumber");
                    snap.sex = rs.getString("Sex");
                    snap.govId = rs.getString("GovernmentIdNumber");
                    snap.address = rs.getString("Address");
                    return snap;
                }
            }
        }
        return null;
    }

    @Override
    public boolean applyScoreDeductions(int candidateId, int[] deductionIds, String sectionKeyword) {
        if (deductionIds == null || deductionIds.length == 0) {
            return false;
        }
        try {
            Integer examCandidateId = ensureExamEnrollmentId(candidateId);
            if (examCandidateId == null) {
                return false;
            }
            String keyword = sectionKeyword != null && !sectionKeyword.isBlank() ? sectionKeyword : "Practical";
            Integer sectionId = findSectionIdForCandidate(examCandidateId, keyword);
            if (sectionId == null) {
                sectionId = findSectionIdForCandidate(examCandidateId, "Practical");
            }
            if (sectionId == null) {
                return false;
            }

            upsertExamScore(examCandidateId, sectionId, 100, true);
            int examScoreId = findExamScoreId(examCandidateId, sectionId);
            if (examScoreId <= 0) {
                return false;
            }

            for (int deductionId : deductionIds) {
                if (deductionId <= 0) {
                    continue;
                }
                String ins = """
                        IF NOT EXISTS (
                            SELECT 1 FROM Score_Deduction
                            WHERE ExamScoreId = ? AND ScoreDeductionId = ?
                        )
                        INSERT INTO Score_Deduction (ExamScoreId, ScoreDeductionId, OccurrenceCount, RecordedAt)
                        VALUES (?, ?, 1, GETDATE())
                        """;
                try (PreparedStatement ps = ctx.getConnection().prepareStatement(ins)) {
                    ps.setInt(1, examScoreId);
                    ps.setInt(2, deductionId);
                    ps.setInt(3, examScoreId);
                    ps.setInt(4, deductionId);
                    ps.executeUpdate();
                }
            }

            double finalScore = 100;
            boolean critical = false;
            String sumSql = """
                    SELECT sd.Points, sd.IsCritical, sded.OccurrenceCount
                    FROM Score_Deduction sded
                    JOIN ScoreDeduction sd ON sd.ScoreDeductionId = sded.ScoreDeductionId
                    WHERE sded.ExamScoreId = ?
                    """;
            try (PreparedStatement ps = ctx.getConnection().prepareStatement(sumSql)) {
                ps.setInt(1, examScoreId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        if (rs.getBoolean("IsCritical")) {
                            critical = true;
                        } else {
                            int count = Math.max(1, rs.getInt("OccurrenceCount"));
                            finalScore -= rs.getDouble("Points") * count;
                        }
                    }
                }
            }
            if (critical) {
                finalScore = 0;
            } else {
                finalScore = Math.max(0, finalScore);
            }
            boolean passed = !critical && finalScore >= 80;
            String upd = "UPDATE ExamScore SET Score = ? WHERE ExamScoreId = ?";
            try (PreparedStatement ps = ctx.getConnection().prepareStatement(upd)) {
                ps.setDouble(1, finalScore);
                ps.setInt(2, examScoreId);
                ps.executeUpdate();
            }
            int resultId = findOrCreateExamResult(examCandidateId, passed);
            String updResult = "UPDATE ExamResult SET IsPassed = ? WHERE ExamResultId = ?";
            try (PreparedStatement ps = ctx.getConnection().prepareStatement(updResult)) {
                ps.setBoolean(1, passed);
                ps.setInt(2, resultId);
                ps.executeUpdate();
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean adjustScoreDeductionOccurrence(int candidateId, int sessionId, int deductionId, int delta) {
        if (candidateId <= 0 || sessionId <= 0 || deductionId <= 0 || delta == 0) {
            return false;
        }
        try {
            Integer examCandidateId = getExamEnrollmentIdForSession(candidateId, sessionId);
            if (examCandidateId == null) {
                return false;
            }
            String sectionKeyword = resolveSectionKeywordForSession(sessionId);
            Integer sectionId = findSectionIdForSession(sessionId, sectionKeyword);
            if (sectionId == null) {
                return false;
            }
            upsertExamScore(examCandidateId, sectionId, 100, true);
            int examScoreId = findExamScoreId(examCandidateId, sectionId);
            if (examScoreId <= 0) {
                return false;
            }

            if (delta > 0) {
                String upsert = """
                        IF EXISTS (
                            SELECT 1 FROM Score_Deduction
                            WHERE ExamScoreId = ? AND ScoreDeductionId = ?
                        )
                            UPDATE Score_Deduction
                            SET OccurrenceCount = OccurrenceCount + ?,
                                RecordedAt = GETDATE()
                            WHERE ExamScoreId = ? AND ScoreDeductionId = ?
                        ELSE
                            INSERT INTO Score_Deduction (ExamScoreId, ScoreDeductionId, OccurrenceCount, RecordedAt)
                            VALUES (?, ?, ?, GETDATE())
                        """;
                try (PreparedStatement ps = ctx.getConnection().prepareStatement(upsert)) {
                    ps.setInt(1, examScoreId);
                    ps.setInt(2, deductionId);
                    ps.setInt(3, delta);
                    ps.setInt(4, examScoreId);
                    ps.setInt(5, deductionId);
                    ps.setInt(6, examScoreId);
                    ps.setInt(7, deductionId);
                    ps.setInt(8, delta);
                    ps.executeUpdate();
                }
            } else {
                String dec = """
                        IF EXISTS (
                            SELECT 1 FROM Score_Deduction
                            WHERE ExamScoreId = ? AND ScoreDeductionId = ?
                              AND OccurrenceCount + ? <= 0
                        )
                            DELETE FROM Score_Deduction
                            WHERE ExamScoreId = ? AND ScoreDeductionId = ?
                        ELSE IF EXISTS (
                            SELECT 1 FROM Score_Deduction
                            WHERE ExamScoreId = ? AND ScoreDeductionId = ?
                        )
                            UPDATE Score_Deduction
                            SET OccurrenceCount = OccurrenceCount + ?,
                                RecordedAt = GETDATE()
                            WHERE ExamScoreId = ? AND ScoreDeductionId = ?
                        """;
                try (PreparedStatement ps = ctx.getConnection().prepareStatement(dec)) {
                    ps.setInt(1, examScoreId);
                    ps.setInt(2, deductionId);
                    ps.setInt(3, delta);
                    ps.setInt(4, examScoreId);
                    ps.setInt(5, deductionId);
                    ps.setInt(6, examScoreId);
                    ps.setInt(7, deductionId);
                    ps.setInt(8, delta);
                    ps.setInt(9, examScoreId);
                    ps.setInt(10, deductionId);
                    ps.executeUpdate();
                }
            }
            recalculateScoreFromDeductions(examCandidateId, sectionId, examScoreId);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean finalizeScoreEntry(int candidateId, int sessionId, String sectionKeyword) {
        if (candidateId <= 0 || sessionId <= 0) {
            System.out.println("[finalizeScoreEntry] FAIL: invalid candidateId=" + candidateId + " sessionId=" + sessionId);
            return false;
        }
        try {
            Integer examCandidateId = getExamEnrollmentIdForSession(candidateId, sessionId);
            if (examCandidateId == null) {
                System.out.println("[finalizeScoreEntry] FAIL: examCandidateId is null for candidateId=" + candidateId + " sessionId=" + sessionId);
                return false;
            }
            String keyword = sectionKeyword != null && !sectionKeyword.isBlank() ? sectionKeyword : "Practical";
            System.out.println("[finalizeScoreEntry] candidateId=" + candidateId + " sessionId=" + sessionId + " examCandidateId=" + examCandidateId + " keyword=" + keyword);
            Integer sectionId = findSectionIdForSession(sessionId, keyword);
            if (sectionId == null) {
                System.out.println("[finalizeScoreEntry] sectionId null via Session, trying via Candidate...");
                sectionId = findSectionIdForCandidate(examCandidateId, keyword);
            }
            if (sectionId == null) {
                System.out.println("[finalizeScoreEntry] FAIL: sectionId is null for keyword=" + keyword);
                return false;
            }
            System.out.println("[finalizeScoreEntry] sectionId=" + sectionId);
            upsertExamScore(examCandidateId, sectionId, 100, true);
            int examScoreId = findExamScoreId(examCandidateId, sectionId);
            if (examScoreId <= 0) {
                System.out.println("[finalizeScoreEntry] FAIL: examScoreId <= 0 after upsert");
                return false;
            }
            System.out.println("[finalizeScoreEntry] examScoreId=" + examScoreId + " — now recalculating deductions...");
            recalculateScoreFromDeductions(examCandidateId, sectionId, examScoreId);
            String upd = """
                    UPDATE ExamEnrollment
                    SET SectionStatus = ?, SignaturePrinted = 0
                    WHERE CandidateId = ? AND SessionId = ?
                    """;
            try (PreparedStatement ps = ctx.getConnection().prepareStatement(upd)) {
                ps.setString(1, ExamConstants.CANDIDATE_AWAITING_SIGNATURE);
                ps.setInt(2, candidateId);
                ps.setInt(3, sessionId);
                int rows = ps.executeUpdate();
                System.out.println("[finalizeScoreEntry] SUCCESS: status set to AwaitingSignature, rows=" + rows);
                return rows > 0;
            }
        } catch (SQLException e) {
            System.out.println("[finalizeScoreEntry] SQLException: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<Map<String, Object>> findAppliedScoreDeductions(int candidateId, int sessionId) {
        List<Map<String, Object>> rows = new ArrayList<>();
        Integer examCandidateId = null;
        try {
            examCandidateId = getExamEnrollmentIdForSession(candidateId, sessionId);
            if (examCandidateId == null) {
                return rows;
            }
            String sql = """
                    SELECT sd.ScoreDeductionId,
                           sd.Reason,
                           sd.Points,
                           sd.IsCritical,
                           sded.OccurrenceCount,
                           sded.RecordedAt
                    FROM Score_Deduction sded
                    JOIN ScoreDeduction sd ON sd.ScoreDeductionId = sded.ScoreDeductionId
                    JOIN ExamScore es ON es.ExamScoreId = sded.ExamScoreId
                    JOIN ExamResult er ON er.ExamResultId = es.ExamResultId
                    WHERE er.ExamEnrollmentId = ?
                      AND es.ExamSectionId = (
                          SELECT TOP 1 ses.ExamSectionId
                          FROM Session_ExamSection ses
                          WHERE ses.SessionId = ?
                      )
                    ORDER BY sded.RecordedAt DESC, sd.SortOrder, sd.ScoreDeductionId
                    """;
            try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
                ps.setInt(1, examCandidateId);
                ps.setInt(2, sessionId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> row = new LinkedHashMap<>();
                        row.put("id", rs.getInt("ScoreDeductionId"));
                        row.put("reason", rs.getString("Reason"));
                        row.put("points", rs.getBigDecimal("Points"));
                        row.put("critical", rs.getBoolean("IsCritical"));
                        row.put("occurrenceCount", rs.getInt("OccurrenceCount"));
                        row.put("recordedAt", rs.getTimestamp("RecordedAt"));
                        rows.add(row);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rows;
    }

    private void recalculateScoreFromDeductions(int examCandidateId, int sectionId, int examScoreId)
            throws SQLException {
        double finalScore = 100;
        boolean critical = false;
        String sumSql = """
                SELECT sd.Points, sd.IsCritical, sded.OccurrenceCount
                FROM Score_Deduction sded
                JOIN ScoreDeduction sd ON sd.ScoreDeductionId = sded.ScoreDeductionId
                WHERE sded.ExamScoreId = ?
                """;
        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sumSql)) {
            ps.setInt(1, examScoreId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (rs.getBoolean("IsCritical")) {
                        critical = true;
                    } else {
                        int count = Math.max(1, rs.getInt("OccurrenceCount"));
                        finalScore -= rs.getDouble("Points") * count;
                    }
                }
            }
        }
        if (critical) {
            finalScore = 0;
        } else {
            finalScore = Math.max(0, finalScore);
        }
        boolean passed = !critical && finalScore >= 80;
        String upd = "UPDATE ExamScore SET Score = ? WHERE ExamScoreId = ?";
        try (PreparedStatement ps = ctx.getConnection().prepareStatement(upd)) {
            ps.setDouble(1, finalScore);
            ps.setInt(2, examScoreId);
            ps.executeUpdate();
        }
        int resultId = findOrCreateExamResult(examCandidateId, passed);
        String updResult = "UPDATE ExamResult SET IsPassed = ? WHERE ExamResultId = ?";
        try (PreparedStatement ps = ctx.getConnection().prepareStatement(updResult)) {
            ps.setBoolean(1, passed);
            ps.setInt(2, resultId);
            ps.executeUpdate();
        }
    }

    @Override
    public boolean markAbsent(int candidateId) {
        String sql = "UPDATE Candidate SET IsAbsent = 1 WHERE CandidateId = ?";
        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean clearAbsentMarking(int candidateId) {
        String sql = "UPDATE Candidate SET IsAbsent = 0 WHERE CandidateId = ?";
        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Integer findCandidateIdByProfileAndSession(int profileId, int sessionId) {
        String sql = """
                SELECT c.CandidateId
                FROM Candidate c
                INNER JOIN ExamEnrollment ec ON ec.CandidateId = c.CandidateId
                INNER JOIN ExamRegistration er ON er.ExamRegistrationId = c.ExamRegistrationId
                WHERE er.ProfileId = ? AND ec.SessionId = ?
                """;
        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
            ps.setInt(1, profileId);
            ps.setInt(2, sessionId);
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
    public boolean markSuspended(int candidateId) {
        String sql = "UPDATE Candidate SET IsSuspended = 1 WHERE CandidateId = ?";
        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean undoSuspension(int candidateId) {
        String sql = "UPDATE Candidate SET IsSuspended = 0 WHERE CandidateId = ?";
        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void syncSectionStatusesForSession(int sessionId) {
        try {
            String q = "SELECT CandidateId FROM ExamEnrollment WHERE SessionId = ?";
            List<Integer> cids = new ArrayList<>();
            try (PreparedStatement ps = ctx.getConnection().prepareStatement(q)) {
                ps.setInt(1, sessionId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        cids.add(rs.getInt("CandidateId"));
                    }
                }
            }
            String sectionKeyword = resolveSectionKeywordForSession(sessionId);
            Integer sectionId = findSectionIdForSession(sessionId, sectionKeyword);
            if (sectionId == null) {
                return;
            }
            for (int cid : cids) {
                Integer examCandidateId = getExamEnrollmentIdForSession(cid, sessionId);
                if (examCandidateId == null) {
                    continue;
                }
                int scoreId = findExamScoreId(examCandidateId, sectionId);
                String currentStatus = getSectionStatus(cid, sessionId);
                if (scoreId > 0 && (currentStatus == null || "Pending".equalsIgnoreCase(currentStatus))) {
                    updateSectionStatus(cid, sessionId, "Done");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String getSectionStatus(int candidateId, int sessionId) throws SQLException {
        String sql = "SELECT SectionStatus FROM ExamEnrollment WHERE CandidateId = ? AND SessionId = ?";
        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            ps.setInt(2, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("SectionStatus");
                }
            }
        }
        return null;
    }

    private void updateSectionStatus(int candidateId, int sessionId, String status) throws SQLException {
        String sql = "UPDATE ExamEnrollment SET SectionStatus = ? WHERE CandidateId = ? AND SessionId = ?";
        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, candidateId);
            ps.setInt(3, sessionId);
            ps.executeUpdate();
        }
    }

    @Override
    public boolean markSignaturePrinted(int candidateId, int sessionId) {
        String sql = "UPDATE ExamEnrollment SET SignaturePrinted = 1 WHERE CandidateId = ? AND SessionId = ?";
        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            ps.setInt(2, sessionId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean completeSection(int candidateId, int sessionId) {
        try {
            int examId = resolveExamIdForCandidate(candidateId);
            if (examId <= 0) {
                return false;
            }
            String doneSql = """
                    UPDATE ExamEnrollment
                    SET SectionStatus = ?
                    WHERE CandidateId = ? AND SessionId = ?
                    """;
            try (PreparedStatement ps = ctx.getConnection().prepareStatement(doneSql)) {
                ps.setString(1, ExamConstants.CANDIDATE_DONE);
                ps.setInt(2, candidateId);
                ps.setInt(3, sessionId);
                if (ps.executeUpdate() <= 0) {
                    return false;
                }
            }

            if (isPassedForNextSection(candidateId, sessionId)) {
                enrollNextSection(candidateId, sessionId, examId);
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isPassedForNextSection(int candidateId, int sessionId) throws SQLException {
        CandidateDTO reg = getById(candidateId);
        if (reg == null || reg.getExamSessionId() != sessionId) {
            reg = null;
            List<CandidateDTO> list = getCandidatesBySession(sessionId);
            for (CandidateDTO item : list) {
                if (item.getId() == candidateId) {
                    reg = item;
                    break;
                }
            }
        }
        if (reg == null) {
            return false;
        }
        String sectionName = resolveSectionNameForSession(sessionId);
        if (sectionName == null) {
            return "passed".equalsIgnoreCase(reg.getTheoryPassed());
        }
        String normalized = sectionName.toLowerCase();
        if (normalized.contains("lý thuyết") || normalized.contains("ly thuyet") || normalized.contains("theory")) {
            return "passed".equalsIgnoreCase(reg.getTheoryPassed());
        }
        if (normalized.contains("đường") || normalized.contains("duong") || normalized.contains("road")) {
            return "passed".equalsIgnoreCase(reg.getRoadTestPassed());
        }
        return "passed".equalsIgnoreCase(reg.getPracticalPassed());
    }

    private String resolveSectionNameForSession(int sessionId) throws SQLException {
        String sql = """
                SELECT TOP 1 es.SectionName
                FROM Session_ExamSection ses
                JOIN ExamSection es ON es.ExamSectionId = ses.ExamSectionId
                WHERE ses.SessionId = ?
                """;
        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("SectionName");
                }
            }
        }
        return null;
    }

    private void enrollNextSection(int candidateId, int sessionId, int examId) throws SQLException {
        String nextSessionSql = """
                SELECT TOP 1 s2.SessionId
                FROM [Session] s1
                JOIN [Session] s2 ON s2.ExamId = s1.ExamId AND s2.StartTime > s1.StartTime
                WHERE s1.SessionId = ?
                ORDER BY s2.StartTime ASC
                """;
        int nextSessionId = 0;
        try (PreparedStatement ps = ctx.getConnection().prepareStatement(nextSessionSql)) {
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    nextSessionId = rs.getInt("SessionId");
                }
            }
        }
        if (nextSessionId <= 0) {
            return;
        }
        String insertSql = """
                IF NOT EXISTS (
                    SELECT 1 FROM ExamEnrollment
                    WHERE CandidateId = ? AND SessionId = ? AND ExamId = ?
                )
                INSERT INTO ExamEnrollment (ExamId, CandidateId, SessionId, SectionStatus, SignaturePrinted)
                VALUES (?, ?, ?, ?, 0)
                """;
        try (PreparedStatement ps = ctx.getConnection().prepareStatement(insertSql)) {
            ps.setInt(1, candidateId);
            ps.setInt(2, nextSessionId);
            ps.setInt(3, examId);
            ps.setInt(4, examId);
            ps.setInt(5, candidateId);
            ps.setInt(6, nextSessionId);
            ps.setString(7, ExamConstants.CANDIDATE_PENDING);
            ps.executeUpdate();
        }
    }

    private boolean upsertSectionScore(int candidateId, String sectionKeyword, int scoreVal)
            throws SQLException {
        return upsertSectionScore(candidateId, sectionKeyword, scoreVal, scoreVal >= 80);
    }

    private boolean upsertSectionScore(int candidateId, String sectionKeyword, int scoreVal, boolean passed)
            throws SQLException {
        Integer examCandidateId = ensureExamEnrollmentId(candidateId);
        if (examCandidateId == null) {
            return false;
        }
        Integer sectionId = findSectionIdForCandidate(examCandidateId, sectionKeyword);
        if (sectionId == null) {
            return false;
        }
        return upsertExamScore(examCandidateId, sectionId, scoreVal, passed);
    }

    private Integer findSectionIdForCandidate(int examCandidateId, String sectionKeyword) throws SQLException {
        String sql = """
                SELECT TOP 1 ses.ExamSectionId
                FROM Session_ExamSection ses
                JOIN ExamEnrollment ec ON ec.SessionId = ses.SessionId
                JOIN ExamSection es ON es.ExamSectionId = ses.ExamSectionId
                WHERE ec.ExamEnrollmentId = ? AND (es.SectionName LIKE ? OR es.SectionName LIKE ?)
                """;
        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
            ps.setInt(1, examCandidateId);
            ps.setString(2, "%" + sectionKeyword + "%");
            ps.setString(3, "%" + translateKeyword(sectionKeyword) + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ExamSectionId");
                }
            }
        }
        String fb = "SELECT ExamSectionId FROM ExamSection WHERE SectionName LIKE ? OR SectionName LIKE ?";
        try (PreparedStatement ps = ctx.getConnection().prepareStatement(fb)) {
            ps.setString(1, "%" + sectionKeyword + "%");
            ps.setString(2, "%" + translateKeyword(sectionKeyword) + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ExamSectionId");
                }
            }
        }
        return null;
    }

    private Integer findSectionIdForSession(int sessionId, String sectionKeyword) throws SQLException {
        String sql = """
                SELECT TOP 1 ses.ExamSectionId
                FROM Session_ExamSection ses
                JOIN ExamSection es ON es.ExamSectionId = ses.ExamSectionId
                WHERE ses.SessionId = ? AND (es.SectionName LIKE ? OR es.SectionName LIKE ?)
                """;
        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            ps.setString(2, "%" + sectionKeyword + "%");
            ps.setString(3, "%" + translateKeyword(sectionKeyword) + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ExamSectionId");
                }
            }
        }
        return null;
    }

    private String translateKeyword(String key) {
        if ("Theory".equalsIgnoreCase(key)) {
            return "Lý thuyết";
        }
        if ("Practical".equalsIgnoreCase(key)) {
            return "Sa hình";
        }
        if ("Road".equalsIgnoreCase(key)) {
            return "Đường trường";
        }
        return key;
    }

    private String resolveSectionKeywordForSession(int sessionId) throws SQLException {
        String sql = """
                SELECT TOP 1 es.SectionName
                FROM Session_ExamSection ses
                JOIN ExamSection es ON es.ExamSectionId = ses.ExamSectionId
                WHERE ses.SessionId = ?
                """;
        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("SectionName");
                    if (name.contains("Lý thuyết") || name.contains("Theory")) {
                        return "Theory";
                    }
                    if (name.contains("Sa hình") || name.contains("Practical")) {
                        return "Practical";
                    }
                    if (name.contains("Đường") || name.contains("Road")) {
                        return "Road";
                    }
                }
            }
        }
        return "Practical";
    }

    private boolean upsertExamScore(int examCandidateId, int sectionId, double score, boolean passed)
            throws SQLException {
        int resultId = findOrCreateExamResult(examCandidateId, passed);
        if (resultId <= 0) {
            return false;
        }
        String check = "SELECT ExamScoreId FROM ExamScore WHERE ExamResultId = ? AND ExamSectionId = ?";
        try (PreparedStatement ps = ctx.getConnection().prepareStatement(check)) {
            ps.setInt(1, resultId);
            ps.setInt(2, sectionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int scoreId = rs.getInt("ExamScoreId");
                    String upd = "UPDATE ExamScore SET Score = ? WHERE ExamScoreId = ?";
                    try (PreparedStatement up = ctx.getConnection().prepareStatement(upd)) {
                        up.setDouble(1, score);
                        up.setInt(2, scoreId);
                        return up.executeUpdate() > 0;
                    }
                }
            }
        }
        String ins = "INSERT INTO ExamScore (ExamResultId, ExamSectionId, Score) VALUES (?, ?, ?)";
        try (PreparedStatement ps = ctx.getConnection().prepareStatement(ins)) {
            ps.setInt(1, resultId);
            ps.setInt(2, sectionId);
            ps.setDouble(3, score);
            return ps.executeUpdate() > 0;
        }
    }

    private int findOrCreateExamResult(int examCandidateId, boolean passed) throws SQLException {
        String check = "SELECT ExamResultId FROM ExamResult WHERE ExamEnrollmentId = ?";
        try (PreparedStatement ps = ctx.getConnection().prepareStatement(check)) {
            ps.setInt(1, examCandidateId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ExamResultId");
                }
            }
        }
        String ins = "INSERT INTO ExamResult (ExamEnrollmentId, IsPassed, ResultDate) VALUES (?, ?, GETDATE())";
        try (PreparedStatement ps = ctx.getConnection().prepareStatement(ins, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, examCandidateId);
            ps.setBoolean(2, passed);
            ps.executeUpdate();
            try (ResultSet gk = ps.getGeneratedKeys()) {
                if (gk.next()) {
                    return gk.getInt(1);
                }
            }
        }
        return 0;
    }

    private int findExamScoreId(int examCandidateId, int sectionId) throws SQLException {
        String sql = """
                SELECT es.ExamScoreId
                FROM ExamScore es
                JOIN ExamResult er ON er.ExamResultId = es.ExamResultId
                WHERE er.ExamEnrollmentId = ? AND es.ExamSectionId = ?
                """;
        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
            ps.setInt(1, examCandidateId);
            ps.setInt(2, sectionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ExamScoreId");
                }
            }
        }
        return 0;
    }

    private static boolean isTheoryPassed(int score) {
        if (score <= 35) {
            return score >= 32;
        }
        return score >= 80;
    }

    private CandidateDTO mapResultSetToCandidateDTO(ResultSet rs) throws SQLException {
        CandidateDTO er = new CandidateDTO();
        er.setId(rs.getInt("id"));
        er.setExamSessionId(rs.getInt("examSessionId"));
        er.setPersonId(rs.getInt("personId"));
        er.setCandidateNo(rs.getInt("candidateNo"));
        er.setRegistrationType(rs.getString("registrationType"));
        er.setIsPaymentCompleted(rs.getBoolean("isPaymentCompleted"));
        er.setIsPresent(rs.getBoolean("isPresent"));
        er.setPresentMarkedAt(rs.getTimestamp("presentMarkedAt"));
        er.setFullName(rs.getString("fullName"));
        er.setGovIdNo(rs.getString("govIdNo"));
        er.setDateOfBirth(rs.getDate("dateOfBirth"));
        er.setGender(rs.getBoolean("gender"));
        er.setPhoneNo(rs.getString("phoneNo"));
        er.setEmail(rs.getString("email"));
        er.setPhotoUrl(rs.getString("photoUrl"));
        er.setLicenseCode(rs.getString("licenseCode"));
        er.setComputerCode(rs.getString("computerCode"));
        er.setAddress(rs.getString("address"));
        er.setReasonForTaking(rs.getString("reasonForTaking"));
        er.setExamDate(rs.getDate("examDate"));
        try {
            er.setSectionStatus(rs.getString("sectionStatus"));
            er.setSignaturePrinted(rs.getBoolean("signaturePrinted"));
        } catch (SQLException ignored) {
            er.setSectionStatus("Pending");
            er.setSignaturePrinted(false);
        }

        String notes = rs.getString("notes");
        er.setNotes(notes);
        boolean isAbsent = rs.getBoolean("isAbsent");
        if (!isAbsent && notes != null && "Absent".equalsIgnoreCase(notes.trim())) {
            isAbsent = true;
        }
        er.setAbsent(isAbsent);
        er.setSuspended(rs.getBoolean("isSuspended"));

        if (notes != null && notes.startsWith("AllocatedRoom:")) {
            String[] parts = notes.split(":", 3);
            if (parts.length >= 2) {
                try {
                    er.setAllocatedAreaId(Integer.parseInt(parts[1]));
                    if (parts.length >= 3) {
                        er.setAllocatedAreaName(parts[2]);
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        } else {
            int areaIdVal = rs.getInt("allocatedAreaId");
            if (!rs.wasNull()) {
                er.setAllocatedAreaId(areaIdVal);
                er.setAllocatedAreaName(rs.getString("allocatedAreaName"));
            }
        }

        if (notes != null && notes.startsWith("Device: ")) {
            er.setDeviceCode(notes.replace("Device: ", ""));
        } else {
            er.setDeviceCode("");
        }

        int tScoreVal = rs.getInt("theoryScore");
        if (isAbsent || rs.wasNull()) {
            er.setTheoryScore(null);
            er.setTheoryPassed("none");
        } else {
            er.setTheoryScore(tScoreVal);
            er.setTheoryPassed(isTheoryPassed(tScoreVal) ? "passed" : "failed");
        }

        int pScoreVal = rs.getInt("practicalScore");
        if (isAbsent || rs.wasNull()) {
            er.setPracticalScore(null);
            er.setPracticalPassed("none");
        } else {
            er.setPracticalScore(pScoreVal);
            er.setPracticalPassed(pScoreVal >= 80 ? "passed" : "failed");
        }

        int rScoreVal = rs.getInt("roadTestScore");
        if (isAbsent || rs.wasNull()) {
            er.setRoadTestScore(null);
            er.setRoadTestPassed("none");
        } else {
            er.setRoadTestScore(rScoreVal);
            er.setRoadTestPassed(rScoreVal >= 80 ? "passed" : "failed");
        }

        return er;
    }

    private static final class SessionContext {

        int examId;
        int licenceId;
        String licenseCode;
    }

    private static final class PersonSnapshot {

        String fullName;
        Timestamp dob;
        String phone;
        String sex;
        String govId;
        String address;
    }
}
