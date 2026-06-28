package dao.impl;

import dbconnection.DBContext;

import dao.Db2CandidateSql;
import dao.CandidateDAO;

import dto.candidate.CandidateDTO;

import model.candidate.Candidate;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CandidateDAOImpl extends DBContext implements CandidateDAO {

    @Override
    public Candidate findById(int id) {
        String sql = "SELECT * FROM Candidate WHERE CandidateId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
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
        String sql = "SELECT c.* FROM Candidate c INNER JOIN Exam_Candidate ec ON ec.CandidateId = c.CandidateId WHERE ec.SessionId = ? AND c.CandidateNumber = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
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
        c.setAbsent(rs.getBoolean("IsAbsent"));
        c.setSuspended(rs.getBoolean("IsSuspended"));

        return c;
    }

    @Override
    public CandidateDTO getById(int id) {
        String sql = Db2CandidateSql.CANDIDATE_SELECT + " WHERE c.CandidateId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
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
            try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
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
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
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
    public List<CandidateDTO> getAllCandidates() {
        List<CandidateDTO> list = new ArrayList<>();
        String sql = Db2CandidateSql.CANDIDATE_SELECT
                + " ORDER BY CAST(s.StartTime AS DATE) DESC, candidateNo";
        try (PreparedStatement ps = getConnection().prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
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
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
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
            try (PreparedStatement ps = getConnection().prepareStatement(check)) {
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
            try (PreparedStatement ps = getConnection().prepareStatement(ins)) {
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
        String sql = "SELECT TOP 1 ExamId FROM Exam_Candidate WHERE CandidateId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
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
            Integer examCandidateId = getExamCandidateId(id);
            if (examCandidateId == null) {
                return false;
            }
            int deviceId = -1;
            if (computerCode != null && !computerCode.isEmpty()) {
                String compSql = "SELECT ExamDeviceId FROM ExamDevice WHERE DeviceName = ? OR DeviceName LIKE ?";
                try (PreparedStatement ps = getConnection().prepareStatement(compSql)) {
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
            String checkPaper = "SELECT TheoryPaperId FROM TheoryPaper WHERE ExamCandidateId = ?";
            try (PreparedStatement ps = getConnection().prepareStatement(checkPaper)) {
                ps.setInt(1, examCandidateId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        paperId = rs.getInt("TheoryPaperId");
                    }
                }
            }
            if (paperId == -1) {
                String ins = "INSERT INTO TheoryPaper (ExamCandidateId, ExamDeviceId, StartedAt) VALUES (?, ?, GETDATE())";
                try (PreparedStatement ps = getConnection().prepareStatement(ins)) {
                    ps.setInt(1, examCandidateId);
                    ps.setInt(2, deviceId);
                    return ps.executeUpdate() > 0;
                }
            }
            String upd = "UPDATE TheoryPaper SET ExamDeviceId = ? WHERE TheoryPaperId = ?";
            try (PreparedStatement ps = getConnection().prepareStatement(upd)) {
                ps.setInt(1, deviceId);
                ps.setInt(2, paperId);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Retrieves the ExamCandidateId for the candidate's first enrollment.
     */
    private Integer getExamCandidateId(int candidateId) throws SQLException {
        String sql = "SELECT ExamCandidateId FROM Exam_Candidate WHERE CandidateId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ExamCandidateId");
                }
            }
        }
        return null;
    }

    /**
     * Retrieves the ExamCandidateId for a candidate in a specific session.
     */
    private Integer getExamCandidateIdForSession(int candidateId, int sessionId) throws SQLException {
        String sql = "SELECT ExamCandidateId FROM Exam_Candidate WHERE CandidateId = ? AND SessionId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            ps.setInt(2, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ExamCandidateId");
                }
            }
        }
        return null;
    }

    /**
     * Ensures an ExamCandidateId exists; throws if not found.
     */
    private Integer ensureExamCandidateId(int candidateId) throws SQLException {
        Integer id = getExamCandidateId(candidateId);
        if (id == null) {
            throw new SQLException("ExamCandidate record not found for CandidateId: " + candidateId);
        }
        return id;
    }

    /**
     * Records room allocation info into the candidate's registration notes.
     *
     * @param id the CandidateId
     * @param areaId the ExamAreaId
     * @param areaName the area name
     * @return true if updated
     */
    @Override
    public boolean updateAllocatedRoom(int id, int areaId, String areaName) {
        return updateApplicationNotes(id, "AllocatedRoom:" + areaId + ":" + areaName);
    }

    /**
     * Records a device code into the candidate's registration notes.
     *
     * @param id the CandidateId
     * @param deviceCode the device identifier
     * @return true if updated
     */
    @Override
    public boolean updateDevice(int id, String deviceCode) {
        String notesVal = (deviceCode != null && !deviceCode.isEmpty()) ? "Device: " + deviceCode : null;
        return updateApplicationNotes(id, notesVal);
    }

    /**
     * Updates the Notes field on the registration linked to this candidate.
     */
    private boolean updateApplicationNotes(int id, String notes) {
        String sql = """
                UPDATE ExamRegistration
                SET Notes = ?
                WHERE ExamRegistrationId = (SELECT ExamRegistrationId FROM Candidate WHERE CandidateId = ?)
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
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

    /**
     * Updates theory and/or practical scores for a candidate. Each score is
     * upserted into the ExamScore table via the ExamResult chain.
     *
     * @param id the CandidateId
     * @param theoryScore optional theory score value
     * @param theoryPassed optional pass indicator (passed/failed)
     * @param practicalScore optional practical score value
     * @param practicalPassed optional pass indicator
     * @return true if all provided scores were updated
     */
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

    /**
     * Updates the theory score by recording the number of correct answers.
     * Pass/fail is determined by comparing correctCount against passThreshold.
     *
     * @param id the CandidateId
     * @param correctCount number of correct answers
     * @param passThreshold minimum required correct answers to pass
     * @return true if updated
     */
    @Override
    public boolean updateTheoryCorrectCount(int id, int correctCount, int passThreshold) {
        try {
            Integer examCandidateId = ensureExamCandidateId(id);
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

    /**
     * Looks up the ExamSectionId for the theory section (Lý thuyết/Theory).
     */
    private Integer findTheorySectionIdByCandidate(int candidateId) throws SQLException {
        String sql = """
                SELECT TOP 1 ExamSectionId FROM ExamSection
                WHERE SectionName LIKE N'%Lý thuyết%' OR SectionName LIKE '%Theory%'
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("ExamSectionId");
            }
        }
        return null;
    }

    /**
     * Updates the road test score (Đường trường) for a candidate.
     *
     * @param id the CandidateId
     * @param roadScore the road test score
     * @param roadPassed optional pass indicator
     * @return true if updated
     */
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

    /**
     * Updates the candidate's basic profile fields across Candidate and Profile
     * tables within a single transaction. Also updates the User email if
     * provided.
     *
     * @param id the CandidateId
     * @param fullName the new full name
     * @param dob the new date of birth
     * @param govIdNo the new government ID number
     * @param email optional new email (User table)
     * @param phoneNo the new phone number
     * @return true if all updates succeeded
     */
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
            getConnection().setAutoCommit(false);
            try (PreparedStatement ps = getConnection().prepareStatement(sqlCand)) {
                ps.setString(1, fullName);
                ps.setDate(2, dob);
                ps.setString(3, govIdNo);
                ps.setString(4, phoneNo);
                ps.setInt(5, id);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = getConnection().prepareStatement(sqlProf)) {
                ps.setString(1, fullName);
                ps.setDate(2, dob);
                ps.setString(3, govIdNo);
                ps.setString(4, phoneNo);
                ps.setInt(5, id);
                ps.executeUpdate();
            }
            if (email != null) {
                try (PreparedStatement ps = getConnection().prepareStatement(sqlUser)) {
                    ps.setString(1, email);
                    ps.setInt(2, id);
                    ps.executeUpdate();
                }
            }
            getConnection().commit();
            return true;
        } catch (SQLException e) {
            try {
                getConnection().rollback();
            } catch (SQLException ignored) {
            }
            e.printStackTrace();
        } finally {
            try {
                getConnection().setAutoCommit(true);
            } catch (SQLException ignored) {
            }
        }
        return false;
    }

    /**
     * Comprehensive profile update used by examiners, including address, sex,
     * and reason-for-taking fields. Updates Candidate, Profile, and optionally
     * User tables within a single transaction.
     *
     * @param id the CandidateId
     * @param fullName the full name
     * @param dob the date of birth
     * @param govIdNo the government ID number
     * @param email optional email (User table)
     * @param phoneNo the phone number
     * @param address the address
     * @param sex the sex string (Nam/Nu)
     * @param reasonForTaking the reason for taking the exam
     * @return true if all updates succeeded
     */
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
            getConnection().setAutoCommit(false);
            try (PreparedStatement ps = getConnection().prepareStatement(sqlCand)) {
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
            try (PreparedStatement ps = getConnection().prepareStatement(sqlProf)) {
                ps.setString(1, fullName);
                ps.setDate(2, dob);
                ps.setString(3, govIdNo);
                ps.setString(4, phoneNo);
                ps.setString(5, address);
                ps.setInt(6, id);
                ps.executeUpdate();
            }
            if (email != null) {
                try (PreparedStatement ps = getConnection().prepareStatement(sqlUser)) {
                    ps.setString(1, email);
                    ps.setInt(2, id);
                    ps.executeUpdate();
                }
            }
            getConnection().commit();
            return true;
        } catch (SQLException e) {
            try {
                getConnection().rollback();
            } catch (SQLException ignored) {
            }
            e.printStackTrace();
        } finally {
            try {
                getConnection().setAutoCommit(true);
            } catch (SQLException ignored) {
            }
        }
        return false;
    }

    @Override
    public boolean updatePhoto(int id, String photoUrl) {
        String sql = "UPDATE Candidate SET PhotoImageUrl = ? WHERE CandidateId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
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
            getConnection().setAutoCommit(false);
            SessionContext ctx = loadSessionContext(reg.getExamSessionId());
            if (ctx == null) {
                getConnection().rollback();
                return false;
            }
            int applicationId = findOrCreateApplication(reg, ctx.licenceId);
            if (applicationId <= 0) {
                getConnection().rollback();
                return false;
            }
            int userId = findUserIdByProfile(reg.getPersonId());
            PersonSnapshot snap = loadProfileSnapshot(reg.getPersonId());
            int candidateNumber = reg.getCandidateNo();
            String sqlCand = """
                    INSERT INTO Candidate (CandidateNumber, FullName, DateOfBirth, PhoneNumber, Sex,
                        GovernmentIdNumber, Address, UserId, ExamRegistrationId)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """;
            int candidateId;
            try (PreparedStatement ps = getConnection().prepareStatement(sqlCand, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, candidateNumber);
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
                        getConnection().rollback();
                        return false;
                    }
                    candidateId = gk.getInt(1);
                }
            }
            String sqlEc = "INSERT INTO Exam_Candidate (ExamId, CandidateId, SessionId) VALUES (?, ?, ?)";
            try (PreparedStatement ps = getConnection().prepareStatement(sqlEc)) {
                ps.setInt(1, ctx.examId);
                ps.setInt(2, candidateId);
                ps.setInt(3, reg.getExamSessionId());
                ps.executeUpdate();
            }
            if (reg.isPresent()) {
                updatePresent(candidateId, true);
            }
            getConnection().commit();
            reg.setId(candidateId);
            return true;
        } catch (SQLException e) {
            try {
                getConnection().rollback();
            } catch (SQLException ignored) {
            }
            e.printStackTrace();
        } finally {
            try {
                getConnection().setAutoCommit(true);
            } catch (SQLException ignored) {
            }
        }
        return false;
    }

    /**
     * Loads session context (examId, licenceId, licenseCode) for candidate
     * registration.
     */
    private SessionContext loadSessionContext(int sessionId) throws SQLException {
        String sql = """
                SELECT s.ExamId, e.LicenceId, l.LicenceClass
                FROM [Session] s
                JOIN Exam e ON e.ExamId = s.ExamId
                JOIN Licence l ON l.LicenceId = e.LicenceId
                WHERE s.SessionId = ?
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    SessionContext ctx = new SessionContext();
                    ctx.examId = rs.getInt("ExamId");
                    ctx.licenceId = rs.getInt("LicenceId");
                    ctx.licenseCode = rs.getString("LicenceClass");
                    return ctx;
                }
            }
        }
        return null;
    }

    /**
     * Finds an existing ExamRegistration for the profile+licence, or creates a
     * new one.
     */
    private int findOrCreateApplication(CandidateDTO reg, int licenceId) throws SQLException {
        String check = "SELECT ExamRegistrationId FROM ExamRegistration WHERE ProfileId = ? AND LicenceId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(check)) {
            ps.setInt(1, reg.getPersonId());
            ps.setInt(2, licenceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int appId = rs.getInt("ExamRegistrationId");
                    try (PreparedStatement upd = getConnection().prepareStatement(
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
        try (PreparedStatement ps = getConnection().prepareStatement(ins, Statement.RETURN_GENERATED_KEYS)) {
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

    /**
     * Retrieves the UserId associated with a Profile.
     */
    private int findUserIdByProfile(int profileId) throws SQLException {
        String sql = "SELECT UserId FROM Profile WHERE ProfileId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, profileId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("UserId");
                }
            }
        }
        return 0;
    }

    /**
     * Loads a snapshot of profile fields for copying into the Candidate record.
     */
    private PersonSnapshot loadProfileSnapshot(int profileId) throws SQLException {
        String sql = "SELECT FullName, DateOfBirth, PhoneNumber, Sex, GovernmentIdNumber, Address FROM Profile WHERE ProfileId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
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

    /**
     * Applies score deductions for a candidate's practical/road section. Starts
     * from a base score of 100, deducts points for each non-critical deduction,
     * and sets score to 0 if any critical deduction is applied. Pass threshold
     * is 80.
     *
     * @param candidateId the CandidateId
     * @param deductionIds array of ScoreDeduction IDs to apply
     * @param sectionKeyword the section keyword (defaults to "Practical")
     * @return true if deductions were applied successfully
     */
    @Override
    public boolean applyScoreDeductions(int candidateId, int[] deductionIds, String sectionKeyword) {
        if (deductionIds == null || deductionIds.length == 0) {
            return false;
        }
        try {
            Integer examCandidateId = ensureExamCandidateId(candidateId);
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
                try (PreparedStatement ps = getConnection().prepareStatement(ins)) {
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
            try (PreparedStatement ps = getConnection().prepareStatement(sumSql)) {
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
            try (PreparedStatement ps = getConnection().prepareStatement(upd)) {
                ps.setDouble(1, finalScore);
                ps.setInt(2, examScoreId);
                ps.executeUpdate();
            }
            int resultId = findOrCreateExamResult(examCandidateId, passed);
            String updResult = "UPDATE ExamResult SET IsPassed = ? WHERE ExamResultId = ?";
            try (PreparedStatement ps = getConnection().prepareStatement(updResult)) {
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

    /**
     * Adjusts the occurrence count of a score deduction (increment or
     * decrement). When delta reduces to zero or below, the deduction record is
     * removed. After adjustment, the final score is recalculated from all
     * deductions.
     *
     * @param candidateId the CandidateId
     * @param sessionId the SessionId
     * @param deductionId the ScoreDeduction ID
     * @param delta positive to increment, negative to decrement
     * @return true if adjustment succeeded
     */
    @Override
    public boolean adjustScoreDeductionOccurrence(int candidateId, int sessionId, int deductionId, int delta) {
        if (candidateId <= 0 || sessionId <= 0 || deductionId <= 0 || delta == 0) {
            return false;
        }
        try {
            Integer examCandidateId = getExamCandidateIdForSession(candidateId, sessionId);
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
                try (PreparedStatement ps = getConnection().prepareStatement(upsert)) {
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
                try (PreparedStatement ps = getConnection().prepareStatement(dec)) {
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

    /**
     * Finalises the score entry for a candidate by recalculating deductions,
     * updating the score, and setting SectionStatus to AwaitingSignature.
     *
     * @param candidateId the CandidateId
     * @param sessionId the SessionId
     * @param sectionKeyword the section keyword (defaults to "Practical")
     * @return true if finalisation succeeded
     */
    @Override
    public boolean finalizeScoreEntry(int candidateId, int sessionId, String sectionKeyword) {
        if (candidateId <= 0 || sessionId <= 0) {
            System.out.println("[finalizeScoreEntry] FAIL: invalid candidateId=" + candidateId + " sessionId=" + sessionId);
            return false;
        }
        try {
            Integer examCandidateId = getExamCandidateIdForSession(candidateId, sessionId);
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
                    UPDATE Exam_Candidate
                    SET SectionStatus = ?, SignaturePrinted = 0
                    WHERE CandidateId = ? AND SessionId = ?
                    """;
            try (PreparedStatement ps = getConnection().prepareStatement(upd)) {
                ps.setString(1, "AwaitingSignature");
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

    /**
     * Retrieves all currently applied score deductions for a candidate in a
     * session.
     *
     * @param candidateId the CandidateId
     * @param sessionId the SessionId
     * @return list of maps with deduction id, reason, points, critical flag,
     * occurrence count, recordedAt
     */
    @Override
    public List<Map<String, Object>> findAppliedScoreDeductions(int candidateId, int sessionId) {
        List<Map<String, Object>> rows = new ArrayList<>();
        Integer examCandidateId = null;
        try {
            examCandidateId = getExamCandidateIdForSession(candidateId, sessionId);
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
                    WHERE er.ExamCandidateId = ?
                      AND es.ExamSectionId = (
                          SELECT TOP 1 ses.ExamSectionId
                          FROM Session_ExamSection ses
                          WHERE ses.SessionId = ?
                      )
                    ORDER BY sded.RecordedAt DESC, sd.SortOrder, sd.ScoreDeductionId
                    """;
            try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
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

    /**
     * Recalculates the final score from all applied deductions and updates
     * ExamScore + ExamResult.
     */
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
        try (PreparedStatement ps = getConnection().prepareStatement(sumSql)) {
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
        try (PreparedStatement ps = getConnection().prepareStatement(upd)) {
            ps.setDouble(1, finalScore);
            ps.setInt(2, examScoreId);
            ps.executeUpdate();
        }
        int resultId = findOrCreateExamResult(examCandidateId, passed);
        String updResult = "UPDATE ExamResult SET IsPassed = ? WHERE ExamResultId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(updResult)) {
            ps.setBoolean(1, passed);
            ps.setInt(2, resultId);
            ps.executeUpdate();
        }
    }

    /**
     * Marks a candidate as absent (IsAbsent = 1).
     *
     * @param candidateId the CandidateId
     * @return true if updated
     */
    @Override
    public boolean markAbsent(int candidateId) {
        String sql = "UPDATE Candidate SET IsAbsent = 1 WHERE CandidateId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Clears the absent marking (IsAbsent = 0).
     *
     * @param candidateId the CandidateId
     * @return true if updated
     */
    @Override
    public boolean clearAbsentMarking(int candidateId) {
        String sql = "UPDATE Candidate SET IsAbsent = 0 WHERE CandidateId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Finds a CandidateId by profile and session enrollment.
     *
     * @param profileId the ProfileId
     * @param sessionId the SessionId
     * @return the CandidateId, or null if not found
     */
    @Override
    public Integer findCandidateIdByProfileAndSession(int profileId, int sessionId) {
        String sql = """
                SELECT c.CandidateId
                FROM Candidate c
                INNER JOIN Exam_Candidate ec ON ec.CandidateId = c.CandidateId
                INNER JOIN ExamRegistration er ON er.ExamRegistrationId = c.ExamRegistrationId
                WHERE er.ProfileId = ? AND ec.SessionId = ?
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
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

    /**
     * Marks a candidate as suspended (IsSuspended = 1).
     *
     * @param candidateId the CandidateId
     * @return true if updated
     */
    @Override
    public boolean markSuspended(int candidateId) {
        String sql = "UPDATE Candidate SET IsSuspended = 1 WHERE CandidateId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Removes the suspension mark (IsSuspended = 0).
     *
     * @param candidateId the CandidateId
     * @return true if updated
     */
    @Override
    public boolean undoSuspension(int candidateId) {
        String sql = "UPDATE Candidate SET IsSuspended = 0 WHERE CandidateId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Synchronises section statuses for all candidates in a session,
     * transitioning from Pending to Done when a score record exists.
     *
     * @param sessionId the SessionId
     */
    @Override
    public void syncSectionStatusesForSession(int sessionId) {
        // This is imported from ExamRegistrationDAOImpl
        try {
            String q = "SELECT CandidateId FROM Exam_Candidate WHERE SessionId = ?";
            List<Integer> cids = new ArrayList<>();
            try (PreparedStatement ps = getConnection().prepareStatement(q)) {
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
                Integer examCandidateId = getExamCandidateIdForSession(cid, sessionId);
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

    /**
     * Reads the current SectionStatus from Exam_Candidate.
     */
    private String getSectionStatus(int candidateId, int sessionId) throws SQLException {
        String sql = "SELECT SectionStatus FROM Exam_Candidate WHERE CandidateId = ? AND SessionId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
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
        String sql = "UPDATE Exam_Candidate SET SectionStatus = ? WHERE CandidateId = ? AND SessionId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, candidateId);
            ps.setInt(3, sessionId);
            ps.executeUpdate();
        }
    }

    /**
     * Marks a candidate's signature as printed for a session.
     *
     * @param candidateId the CandidateId
     * @param sessionId the SessionId
     * @return true if updated
     */
    @Override
    public boolean markSignaturePrinted(int candidateId, int sessionId) {
        String sql = "UPDATE Exam_Candidate SET SignaturePrinted = 1 WHERE CandidateId = ? AND SessionId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            ps.setInt(2, sessionId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Marks a section as Done after signature is printed. If the candidate
     * passed, automatically enrolls them into the next session (e.g. theory ->
     * practical).
     *
     * @param candidateId the CandidateId
     * @param sessionId the SessionId
     * @return true if completed
     */
    @Override
    public boolean completeSection(int candidateId, int sessionId) {
        try {
            int examId = resolveExamIdForCandidate(candidateId);
            if (examId <= 0) {
                return false;
            }
            String doneSql = """
                    UPDATE Exam_Candidate
                    SET SectionStatus = ?
                    WHERE CandidateId = ? AND SessionId = ?
                    """;
            try (PreparedStatement ps = getConnection().prepareStatement(doneSql)) {
                ps.setString(1, "Done");
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

    /**
     * Checks if the candidate passed their current section to enrol in the next
     * one.
     */
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

    /**
     * Resolves the section name (e.g. "Lý thuyết", "Sa hình") for a session.
     */
    private String resolveSectionNameForSession(int sessionId) throws SQLException {
        String sql = """
                SELECT TOP 1 es.SectionName
                FROM Session_ExamSection ses
                JOIN ExamSection es ON es.ExamSectionId = ses.ExamSectionId
                WHERE ses.SessionId = ?
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("SectionName");
                }
            }
        }
        return null;
    }

    /**
     * Enrolls the candidate into the next chronological session of the same
     * exam.
     */
    private void enrollNextSection(int candidateId, int sessionId, int examId) throws SQLException {
        String nextSessionSql = """
                SELECT TOP 1 s2.SessionId
                FROM [Session] s1
                JOIN [Session] s2 ON s2.ExamId = s1.ExamId AND s2.StartTime > s1.StartTime
                WHERE s1.SessionId = ?
                ORDER BY s2.StartTime ASC
                """;
        int nextSessionId = 0;
        try (PreparedStatement ps = getConnection().prepareStatement(nextSessionSql)) {
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
                    SELECT 1 FROM Exam_Candidate
                    WHERE CandidateId = ? AND SessionId = ? AND ExamId = ?
                )
                INSERT INTO Exam_Candidate (ExamId, CandidateId, SessionId, SectionStatus, SignaturePrinted)
                VALUES (?, ?, ?, ?, 0)
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(insertSql)) {
            ps.setInt(1, candidateId);
            ps.setInt(2, nextSessionId);
            ps.setInt(3, examId);
            ps.setInt(4, examId);
            ps.setInt(5, candidateId);
            ps.setInt(6, nextSessionId);
            ps.setString(7, enums.CandidateStatus.PENDING.getStatus());
            ps.executeUpdate();
        }
    }

    /**
     * Upserts a section score with default pass threshold of 80.
     */
    private boolean upsertSectionScore(int candidateId, String sectionKeyword, int scoreVal)
            throws SQLException {
        return upsertSectionScore(candidateId, sectionKeyword, scoreVal, scoreVal >= 80);
    }

    /**
     * Upserts a section score with an explicit pass flag.
     */
    private boolean upsertSectionScore(int candidateId, String sectionKeyword, int scoreVal, boolean passed)
            throws SQLException {
        Integer examCandidateId = ensureExamCandidateId(candidateId);
        if (examCandidateId == null) {
            return false;
        }
        Integer sectionId = findSectionIdForCandidate(examCandidateId, sectionKeyword);
        if (sectionId == null) {
            return false;
        }
        return upsertExamScore(examCandidateId, sectionId, scoreVal, passed);
    }

    /**
     * Finds the ExamSectionId matching a keyword for a candidate's session,
     * with Vietnamese name fallback.
     */
    private Integer findSectionIdForCandidate(int examCandidateId, String sectionKeyword) throws SQLException {
        String sql = """
                SELECT TOP 1 ses.ExamSectionId
                FROM Session_ExamSection ses
                JOIN Exam_Candidate ec ON ec.SessionId = ses.SessionId
                JOIN ExamSection es ON es.ExamSectionId = ses.ExamSectionId
                WHERE ec.ExamCandidateId = ? AND (es.SectionName LIKE ? OR es.SectionName LIKE ?)
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examCandidateId);
            ps.setString(2, "%" + sectionKeyword + "%");
            ps.setString(3, "%" + translateKeyword(sectionKeyword) + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ExamSectionId");
                }
            }
        }
        // Fallback to global sections if session-specific section not found
        String fb = "SELECT ExamSectionId FROM ExamSection WHERE SectionName LIKE ? OR SectionName LIKE ?";
        try (PreparedStatement ps = getConnection().prepareStatement(fb)) {
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

    /**
     * Finds the ExamSectionId for a session matching the given keyword.
     */
    private Integer findSectionIdForSession(int sessionId, String sectionKeyword) throws SQLException {
        String sql = """
                SELECT TOP 1 ses.ExamSectionId
                FROM Session_ExamSection ses
                JOIN ExamSection es ON es.ExamSectionId = ses.ExamSectionId
                WHERE ses.SessionId = ? AND (es.SectionName LIKE ? OR es.SectionName LIKE ?)
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
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

    /**
     * Translates English section keywords to Vietnamese for LIKE matching.
     */
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

    /**
     * Determines the section keyword (Theory/Practical/Road) from the session's
     * section name.
     */
    private String resolveSectionKeywordForSession(int sessionId) throws SQLException {
        String sql = """
                SELECT TOP 1 es.SectionName
                FROM Session_ExamSection ses
                JOIN ExamSection es ON es.ExamSectionId = ses.ExamSectionId
                WHERE ses.SessionId = ?
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
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

    /**
     * Inserts or updates an ExamScore record for a candidate+section
     * combination.
     */
    private boolean upsertExamScore(int examCandidateId, int sectionId, double score, boolean passed)
            throws SQLException {
        int resultId = findOrCreateExamResult(examCandidateId, passed);
        if (resultId <= 0) {
            return false;
        }
        String check = "SELECT ExamScoreId FROM ExamScore WHERE ExamResultId = ? AND ExamSectionId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(check)) {
            ps.setInt(1, resultId);
            ps.setInt(2, sectionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int scoreId = rs.getInt("ExamScoreId");
                    String upd = "UPDATE ExamScore SET Score = ? WHERE ExamScoreId = ?";
                    try (PreparedStatement up = getConnection().prepareStatement(upd)) {
                        up.setDouble(1, score);
                        up.setInt(2, scoreId);
                        return up.executeUpdate() > 0;
                    }
                }
            }
        }
        String ins = "INSERT INTO ExamScore (ExamResultId, ExamSectionId, Score) VALUES (?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(ins)) {
            ps.setInt(1, resultId);
            ps.setInt(2, sectionId);
            ps.setDouble(3, score);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Finds an existing ExamResult for a candidate, or creates one with the
     * given pass status.
     */
    private int findOrCreateExamResult(int examCandidateId, boolean passed) throws SQLException {
        String check = "SELECT ExamResultId FROM ExamResult WHERE ExamCandidateId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(check)) {
            ps.setInt(1, examCandidateId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ExamResultId");
                }
            }
        }
        String ins = "INSERT INTO ExamResult (ExamCandidateId, IsPassed, ResultDate) VALUES (?, ?, GETDATE())";
        try (PreparedStatement ps = getConnection().prepareStatement(ins, Statement.RETURN_GENERATED_KEYS)) {
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

    /**
     * Finds the ExamScoreId for a candidate+section via the ExamResult chain.
     */
    private int findExamScoreId(int examCandidateId, int sectionId) throws SQLException {
        String sql = """
                SELECT es.ExamScoreId
                FROM ExamScore es
                JOIN ExamResult er ON er.ExamResultId = es.ExamResultId
                WHERE er.ExamCandidateId = ? AND es.ExamSectionId = ?
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
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

    /**
     * Determines if a theory score is passing (>=32 for <=35 questions, >=80
     * for larger tests).
     */
    private static boolean isTheoryPassed(int score) {
        if (score <= 35) {
            return score >= 32;
        }
        return score >= 80;
    }

    /**
     * Maps a ResultSet row (from CANDIDATE_SELECT) into a rich CandidateDTO
     * with all computed fields.
     */
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
