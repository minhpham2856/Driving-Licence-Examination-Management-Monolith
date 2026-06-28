package dao.impl;




import dbconnection.DBContext;

import dao.Db2CandidateSql;
import dao.ExamRegistrationDAO;

import dto.exam.ExamRegistrationDTO;

import model.exam.ExamRegistration;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of ExamRegistrationDAO providing full candidate registration
 * and exam workflow operations. Mirrors many CandidateDAO operations but returns
 * ExamRegistrationDTO/ExamRegistration objects. Supports transactions for insert/updateProfile,
 * absent management (with exam result cleanup), score deduction, and auto-enrollment
 * for next-section progression.
 *
 * <p>Inner helper classes:
 * <ul>
 *   <li>{@link SessionContext} — holds ExamId, LicenceId, LicenseCode for a session</li>
 *   <li>{@link PersonSnapshot} — holds Profile fields copied into Candidate on insert</li>
 * </ul>
 */
public class ExamRegistrationDAOImpl extends DBContext implements ExamRegistrationDAO {

    /**
     * Retrieves an ExamRegistration model by primary key.
     *
     * @param id the ExamRegistrationId
     * @return the ExamRegistration model, or null if not found
     */
    @Override
    public ExamRegistration findById(int id) {
        String sql = "SELECT * FROM ExamRegistration WHERE Id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ExamRegistration r = new ExamRegistration();
                    r.setId(rs.getInt("Id"));
                    r.setRegistrationStatus(rs.getString("RegistrationStatus"));
                    r.setNotes(rs.getString("Notes"));
                    r.setProfileId(rs.getInt("ProfileId"));
                    r.setLicenceId(rs.getInt("LicenceId"));
                    return r;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Retrieves a rich ExamRegistrationDTO by candidate ID using the CANDIDATE_SELECT join query.
     *
     * @param id the CandidateId
     * @return the ExamRegistrationDTO, or null if not found
     */
    @Override
    public ExamRegistrationDTO getById(int id) {
        String sql = Db2CandidateSql.CANDIDATE_SELECT + " WHERE c.CandidateId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToExamRegistration(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Retrieves a candidate DTO by session and SBD using multiple matching strategies.
     *
     * @param sessionId the SessionId
     * @param sbd       the candidate number (supports hyphenated and raw formats)
     * @return the ExamRegistrationDTO, or null if not found
     */
    @Override
    public ExamRegistrationDTO getBySessionAndSbd(int sessionId, String sbd) {
        if (sbd == null || !sbd.contains("-")) {
            return null;
        }
        try {
            int candidateNo = Integer.parseInt(sbd.split("-")[1]);
            String sql = Db2CandidateSql.CANDIDATE_SELECT
                    + " WHERE ec.SessionId = ? AND TRY_CAST(SUBSTRING(c.CandidateNumber, CHARINDEX('-', c.CandidateNumber) + 1, 10) AS INT) = ?";
            try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
                ps.setInt(1, sessionId);
                ps.setInt(2, candidateNo);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToExamRegistration(rs);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns all candidates registered in a session, ordered by candidate number.
     *
     * @param sessionId the SessionId
     * @return list of ExamRegistrationDTO objects
     */
    @Override
    public List<ExamRegistrationDTO> getCandidatesBySession(int sessionId) {
        List<ExamRegistrationDTO> list = new ArrayList<>();
        String sql = Db2CandidateSql.CANDIDATE_SELECT
                + " WHERE ec.SessionId = ? ORDER BY candidateNo";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToExamRegistration(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Returns all candidates across all sessions, ordered by exam date descending.
     *
     * @return list of all ExamRegistrationDTO objects
     */
    @Override
    public List<ExamRegistrationDTO> getAllCandidates() {
        List<ExamRegistrationDTO> list = new ArrayList<>();
        String sql = Db2CandidateSql.CANDIDATE_SELECT
                + " ORDER BY CAST(s.StartTime AS DATE) DESC, candidateNo";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToExamRegistration(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Updates registration status to CheckedIn or PreRegistered based on presence.
     *
     * @param id        the CandidateId
     * @param isPresent true for CheckedIn, false for PreRegistered
     * @return true if updated
     */
    @Override
    public boolean updatePresent(int id, boolean isPresent) {
        String sql = """
                UPDATE ExamRegistration
                SET RegistrationStatus = ?                WHERE ExamRegistrationId = (SELECT ExamRegistrationId FROM Candidate WHERE CandidateId = ?)
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

    /**
     * Records payment if not already present, creating a 200,000 VND Cash payment.
     *
     * @param id                  the CandidateId
     * @param isPaymentCompleted  if false returns true without action
     * @return true if payment exists or was created
     */
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

    /** Resolves the ExamId from Exam_Candidate for a given candidate. */
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

    /**
     * Assigns a computer device to a candidate for theory testing, creating
     * or updating a TheoryPaper record.
     *
     * @param id           the CandidateId
     * @param computerCode the device name/code
     * @return true if assignment succeeded
     */
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
     * Records room allocation info into registration notes.
     *
     * @param id       the CandidateId
     * @param areaId   the ExamAreaId
     * @param areaName the area name
     * @return true if updated
     */
    @Override
    public boolean updateAllocatedRoom(int id, int areaId, String areaName) {
        return updateApplicationNotes(id, "AllocatedRoom:" + areaId + ":" + areaName);
    }

    /**
     * Records a device code into registration notes.
     *
     * @param id         the CandidateId
     * @param deviceCode the device identifier
     * @return true if updated
     */
    @Override
    public boolean updateDevice(int id, String deviceCode) {
        String notesVal = (deviceCode != null && !deviceCode.isEmpty()) ? "Device: " + deviceCode : null;
        return updateApplicationNotes(id, notesVal);
    }

    /**
     * Updates theory and/or practical scores via upsert into ExamScore.
     *
     * @param id              the CandidateId
     * @param theoryScore     optional theory score
     * @param theoryPassed    optional pass indicator
     * @param practicalScore  optional practical score
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
     * Updates theory score from correct answer count.
     *
     * @param id            the CandidateId
     * @param correctCount  number of correct answers
     * @param passThreshold minimum correct to pass
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
     * Updates the road test score.
     *
     * @param id         the CandidateId
     * @param roadScore  the score value
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
     * Updates candidate profile across Candidate, Profile, and optionally User tables
     * within a single transaction.
     *
     * @param id       the CandidateId
     * @param fullName the new full name
     * @param dob      the new date of birth
     * @param govIdNo  the new government ID
     * @param email    optional new email
     * @param phoneNo  the new phone number
     * @return true if all updates succeeded
     */
    @Override
    public boolean updateProfile(int id, String fullName, Date dob, String govIdNo, String email, String phoneNo) {
        String sqlCand = """
                UPDATE Candidate
                SET FullName = ?, DateOfBirth = ?, GovernmentIdNumber = ?, PhoneNumber = ?                WHERE CandidateId = ?
                """;
        String sqlProf = """
                UPDATE Profile
                SET FullName = ?, DateOfBirth = ?, GovernmentIdNumber = ?, PhoneNumber = ?                WHERE ProfileId = (SELECT er.ProfileId FROM Candidate c JOIN ExamRegistration er ON er.ExamRegistrationId = c.ExamRegistrationId WHERE c.CandidateId = ?)
                """;
        String sqlUser = """
                UPDATE [User] SET Email = ?                WHERE UserId = (SELECT UserId FROM Candidate WHERE CandidateId = ?)
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
     * Comprehensive profile update (examiner use) across Candidate, Profile, and User tables.
     * Includes address, sex, and reason-for-taking fields.
     *
     * @param id              the CandidateId
     * @param fullName        the full name
     * @param dob             date of birth
     * @param govIdNo         government ID number
     * @param email           optional email
     * @param phoneNo         phone number
     * @param address         address
     * @param sex             sex (Nam/Nu)
     * @param reasonForTaking reason for taking the exam
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

    /**
     * Updates the candidate's photo URL.
     *
     * @param id       the CandidateId
     * @param photoUrl the new photo URL/path
     * @return true if updated
     */
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

    /**
     * Inserts a new candidate registration within a transaction, including
     * ExamRegistration (create or reuse), Candidate (from profile snapshot),
     * and Exam_Candidate linking.
     *
     * @param reg the ExamRegistrationDTO (id will be populated on success)
     * @return true if insertion succeeded
     */
    @Override
    public boolean insert(ExamRegistrationDTO reg) {
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
            String candidateNumber = util.FormatUtil.buildCandidateNumber(ctx.licenseCode, reg.getCandidateNo());
            String sqlCand = """
                    INSERT INTO Candidate (CandidateNumber, FullName, DateOfBirth, PhoneNumber, Sex,
                        GovernmentIdNumber, Address, UserId, ExamRegistrationId)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """;
            int candidateId;
            try (PreparedStatement ps = getConnection().prepareStatement(sqlCand, Statement.RETURN_GENERATED_KEYS)) {
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
     * Applies score deductions starting from base 100, recalculates the final score,
     * and updates ExamResult pass/fail status.
     *
     * @param candidateId    the CandidateId
     * @param deductionIds   array of ScoreDeduction IDs
     * @param sectionKeyword the section (defaults to "Practical")
     * @return true if deductions were applied
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
                        INSERT INTO Score_Deduction (ExamScoreId, ScoreDeductionId) VALUES (?, ?)
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
                    SELECT sd.Points, sd.IsCritical
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
                            finalScore -= rs.getDouble("Points");
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

    /** Finds the ExamScoreId for a candidate-section via ExamResult chain. */
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
        return -1;
    }

    /**
     * Marks a candidate as absent.
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
     * Marks a candidate as suspended.
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
     * Removes the suspension mark.
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
     * Clears absent marking within a transaction, also removing absent-related
     * exam results and resetting section status to Pending.
     *
     * @param candidateId the CandidateId
     * @return true if all clearing steps succeeded
     */
    @Override
    public boolean clearAbsentMarking(int candidateId) {
        String sql = "UPDATE Candidate SET IsAbsent = 0 WHERE CandidateId = ?";
        try {
            getConnection().setAutoCommit(false);
            deleteAbsentExamResults(candidateId);
            clearLegacyAbsentNotes(candidateId);
            resetSectionStatusAfterAbsentUndo(candidateId);
            int rows;
            try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
                ps.setInt(1, candidateId);
                rows = ps.executeUpdate();
            }
            if (rows <= 0) {
                getConnection().rollback();
                return false;
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
     * Finds a CandidateId by ProfileId and SessionId via the registration chain.
     *
     * @param profileId the ProfileId
     * @param sessionId the SessionId
     * @return the CandidateId, or null
     */
    @Override
    public Integer findCandidateIdByProfileAndSession(int profileId, int sessionId) {
        String sql = """
                SELECT c.CandidateId
                FROM Candidate c
                JOIN Exam_Candidate ec ON ec.CandidateId = c.CandidateId
                JOIN ExamRegistration er ON er.ExamRegistrationId = c.ExamRegistrationId
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

    /** Updates the Notes field on the registration for a given candidate. */
    private boolean updateApplicationNotes(int candidateId, String notes) {
        String sql = """
                UPDATE ExamRegistration SET Notes = ?                WHERE ExamRegistrationId = (SELECT ExamRegistrationId FROM Candidate WHERE CandidateId = ?)
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            if (notes != null) {
                ps.setString(1, notes);
            } else {
                ps.setNull(1, Types.NVARCHAR);
            }
            ps.setInt(2, candidateId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** Removes the legacy "Absent" note value from ExamRegistration. */
    private void clearLegacyAbsentNotes(int candidateId) throws SQLException {
        String sql = """
                UPDATE ExamRegistration SET Notes = NULL
                WHERE ExamRegistrationId = (SELECT ExamRegistrationId FROM Candidate WHERE CandidateId = ?)
                  AND Notes = N'Absent'
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            ps.executeUpdate();
        }
    }

    /** Removes exam results/scores created when marking absent; keeps TheoryPaper and answers. */
    /** Removes Score_Deduction, ExamScore, and ExamResult records created during absent marking. */
    private void deleteAbsentExamResults(int candidateId) throws SQLException {
        Integer examCandidateId = getExamCandidateId(candidateId);
        if (examCandidateId == null) {
            return;
        }
        String delDeductions = """
                DELETE sd FROM Score_Deduction sd
                JOIN ExamScore es ON es.ExamScoreId = sd.ExamScoreId
                JOIN ExamResult er ON er.ExamResultId = es.ExamResultId
                WHERE er.ExamCandidateId = ?
                """;
        String delScores = """
                DELETE es FROM ExamScore es
                JOIN ExamResult er ON er.ExamResultId = es.ExamResultId
                WHERE er.ExamCandidateId = ?
                """;
        String delResult = "DELETE FROM ExamResult WHERE ExamCandidateId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(delDeductions)) {
            ps.setInt(1, examCandidateId);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = getConnection().prepareStatement(delScores)) {
            ps.setInt(1, examCandidateId);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = getConnection().prepareStatement(delResult)) {
            ps.setInt(1, examCandidateId);
            ps.executeUpdate();
        }
    }

    /** Resets SectionStatus to Pending and clears signature flag after absent undo. */
    private void resetSectionStatusAfterAbsentUndo(int candidateId) throws SQLException {
        String sql = """
                UPDATE ec
                SET SectionStatus = ?, SignaturePrinted = 0
                FROM Exam_Candidate ec
                WHERE ec.CandidateId = ?
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, enums.CandidateStatus.PENDING.getStatus());
            ps.setInt(2, candidateId);
            ps.executeUpdate();
        }
    }

    /** Upserts a section score with default pass threshold of 80. */
    private boolean upsertSectionScore(int candidateId, String sectionKeyword, int score) throws SQLException {
        return upsertSectionScore(candidateId, sectionKeyword, score, score >= 80);
    }

    /** Upserts a section score with explicit pass flag. */
    private boolean upsertSectionScore(int candidateId, String sectionKeyword, int score, boolean passed)
            throws SQLException {
        Integer examCandidateId = ensureExamCandidateId(candidateId);
        if (examCandidateId == null) {
            return false;
        }
        Integer sectionId = findSectionIdForCandidate(examCandidateId, sectionKeyword);
        if (sectionId == null) {
            return false;
        }
        return upsertExamScore(examCandidateId, sectionId, score, passed);
    }

    /** Inserts or updates an ExamScore record for a candidate+section. */
    private boolean upsertExamScore(int examCandidateId, int sectionId, int score, boolean passed)
            throws SQLException {
        int resultId = findOrCreateExamResult(examCandidateId, passed);
        String check = "SELECT ExamScoreId FROM ExamScore WHERE ExamResultId = ? AND ExamSectionId = ?";
        int scoreId = -1;
        try (PreparedStatement ps = getConnection().prepareStatement(check)) {
            ps.setInt(1, resultId);
            ps.setInt(2, sectionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    scoreId = rs.getInt("ExamScoreId");
                }
            }
        }
        if (scoreId == -1) {
            String ins = "INSERT INTO ExamScore (ExamResultId, ExamSectionId, Score) VALUES (?, ?, ?)";
            try (PreparedStatement ps = getConnection().prepareStatement(ins)) {
                ps.setInt(1, resultId);
                ps.setInt(2, sectionId);
                ps.setDouble(3, score);
                ps.executeUpdate();
            }
        } else {
            String upd = "UPDATE ExamScore SET Score = ? WHERE ExamScoreId = ?";
            try (PreparedStatement ps = getConnection().prepareStatement(upd)) {
                ps.setDouble(1, score);
                ps.setInt(2, scoreId);
                ps.executeUpdate();
            }
        }
        return true;
    }

    /** Looks up the theory section ID for a candidate's session. */
    private Integer findTheorySectionIdByCandidate(int candidateId) throws SQLException {
        String sql = """
                SELECT TOP 1 es.ExamSectionId
                FROM Exam_Candidate ec
                JOIN Session_ExamSection ses ON ses.SessionId = ec.SessionId
                JOIN ExamSection es ON es.ExamSectionId = ses.ExamSectionId
                WHERE ec.CandidateId = ?
                  AND (es.SectionName LIKE N'%Lý thuyết%' OR es.SectionName LIKE '%Theory%')
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ExamSectionId");
                }
            }
        }
        return null;
    }

    /** Ensures an ExamCandidateId exists, creating the enrollment if necessary. */
    private Integer ensureExamCandidateId(int candidateId) throws SQLException {
        Integer examCandidateId = getExamCandidateId(candidateId);
        if (examCandidateId != null) {
            return examCandidateId;
        }
        int examId = -1;
        int sessionId = -1;
        try (PreparedStatement ps = getConnection().prepareStatement(
                "SELECT TOP 1 ex.ExamId, ec.SessionId FROM Candidate c "
                + "JOIN ExamRegistration er ON er.ExamRegistrationId = c.ExamRegistrationId "
                + "JOIN Exam ex ON ex.LicenceId = er.LicenceId "
                + "JOIN Exam_Candidate ec ON ec.CandidateId = c.CandidateId "
                + "WHERE c.CandidateId = ?")) {
            ps.setInt(1, candidateId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    examId = rs.getInt("ExamId");
                    sessionId = rs.getInt("SessionId");
                }
            }
        }
        if (examId <= 0) {
            try (PreparedStatement ps = getConnection().prepareStatement(
                    "SELECT TOP 1 ex.ExamId, s.SessionId FROM Candidate c "
                    + "JOIN ExamRegistration er ON er.ExamRegistrationId = c.ExamRegistrationId "
                    + "JOIN Exam ex ON ex.LicenceId = er.LicenceId "
                    + "JOIN [Session] s ON s.ExamId = ex.ExamId "
                    + "WHERE c.CandidateId = ? ORDER BY s.SessionId")) {
                ps.setInt(1, candidateId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        examId = rs.getInt("ExamId");
                        sessionId = rs.getInt("SessionId");
                    }
                }
            }
        }
        if (examId <= 0 || sessionId <= 0) {
            return null;
        }
        String ins = "INSERT INTO Exam_Candidate (ExamId, CandidateId, SessionId) VALUES (?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(ins, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, examId);
            ps.setInt(2, candidateId);
            ps.setInt(3, sessionId);
            ps.executeUpdate();
            try (ResultSet gk = ps.getGeneratedKeys()) {
                if (gk.next()) {
                    return gk.getInt(1);
                }
            }
        }
        return getExamCandidateId(candidateId);
    }

    /** Finds existing ExamResult or creates one with the given pass status. */
    private int findOrCreateExamResult(int examCandidateId, boolean passed) throws SQLException {
        String check = "SELECT ExamResultId FROM ExamResult WHERE ExamCandidateId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(check)) {
            ps.setInt(1, examCandidateId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int resultId = rs.getInt("ExamResultId");
                    try (PreparedStatement upd = getConnection().prepareStatement(
                            "UPDATE ExamResult SET IsPassed = ? WHERE ExamResultId = ?")) {
                        upd.setBoolean(1, passed);
                        upd.setInt(2, resultId);
                        upd.executeUpdate();
                    }
                    return resultId;
                }
            }
        }
        String ins = "INSERT INTO ExamResult (ExamCandidateId, IsPassed) VALUES (?, ?)";
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
        throw new SQLException("Cannot create ExamResult");
    }

    /** Finds the ExamSectionId matching a keyword, with Vietnamese name fallback. */
    private Integer findSectionIdForCandidate(int examCandidateId, String keyword) throws SQLException {
        // B2: lý thuyết (exam 2) và thực hành/sa hình (exam 3) dùng cùng LicenceId — không chỉ ExamId hiện tại
        String sql = """
                SELECT TOP 1 es.ExamSectionId
                FROM Exam_Candidate ec
                JOIN Exam curExam ON curExam.ExamId = ec.ExamId
                JOIN Session_ExamSection ses ON ses.SessionId IN (
                    SELECT s.SessionId
                    FROM [Session] s
                    INNER JOIN Exam e ON e.ExamId = s.ExamId
                    WHERE e.LicenceId = curExam.LicenceId
                )
                JOIN ExamSection es ON es.ExamSectionId = ses.ExamSectionId
                WHERE ec.ExamCandidateId = ?
                  AND (es.SectionName LIKE ? OR es.SectionName LIKE ? OR es.SectionName LIKE ? OR es.SectionName LIKE ?)
                ORDER BY
                    CASE WHEN ses.SessionId = ec.SessionId THEN 0 ELSE 1 END,
                    es.ExamSectionId
                """;
        String likeVi = "%" + (keyword.equals("Theory") ? "Lý thuyết"
                : keyword.equals("Practical") ? "Sa hình" : "Đường") + "%";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examCandidateId);
            ps.setString(2, "%" + keyword + "%");
            ps.setString(3, likeVi);
            ps.setString(4, "%Thực hành%");
            ps.setString(5, "%Road%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ExamSectionId");
                }
            }
        }
        return null;
    }

    /** Retrieves the ExamCandidateId for a candidate. */
    private Integer getExamCandidateId(int candidateId) throws SQLException {
        String sql = """
                SELECT ec.ExamCandidateId FROM Exam_Candidate ec
                WHERE ec.CandidateId = ?
                """;
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

    /** Finds or creates an ExamRegistration record for the given profile and licence. */
    private int findOrCreateApplication(ExamRegistrationDTO reg, int licenceId) throws SQLException {
        String check = "SELECT ExamRegistrationId FROM ExamRegistration WHERE ProfileId = ? AND LicenceId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(check)) {
            ps.setInt(1, reg.getPersonId());
            ps.setInt(2, licenceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int appId = rs.getInt("ExamRegistrationId");
                    String status = "WalkIn".equals(reg.getRegistrationType()) ? "WalkIn" : "PreRegistered";
                    if (reg.isPresent()) {
                        status = "CheckedIn";
                    }
                    try (PreparedStatement upd = getConnection().prepareStatement(
                            "UPDATE ExamRegistration SET RegistrationStatus = ?, Notes = ? WHERE ExamRegistrationId = ?")) {
                        upd.setString(1, status);
                        upd.setString(2, reg.getNotes());
                        upd.setInt(3, appId);
                        upd.executeUpdate();
                    }
                    return appId;
                }
            }
        }
        String status = "WalkIn".equals(reg.getRegistrationType()) ? "WalkIn" : "PreRegistered";
        if (reg.isPresent()) {
            status = "CheckedIn";
        }
        String ins = """
                INSERT INTO ExamRegistration (RegistrationStatus, Notes, ProfileId, LicenceId)
                VALUES (?, ?, ?, ?)
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(ins, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, status);
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
        return -1;
    }

    /** Retrieves the UserId from a Profile. */
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
        return -1;
    }

    /** Loads profile fields into a snapshot for copying into a new Candidate record. */
    private PersonSnapshot loadProfileSnapshot(int profileId) throws SQLException {
        String sql = """
                SELECT FullName, DateOfBirth, PhoneNumber, Sex, GovernmentIdNumber, Address
                FROM Profile WHERE ProfileId = ?
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, profileId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    PersonSnapshot s = new PersonSnapshot();
                    s.fullName = rs.getString("FullName");
                    s.dob = rs.getTimestamp("DateOfBirth");
                    s.phone = rs.getString("PhoneNumber");
                    s.sex = rs.getString("Sex");
                    s.govId = rs.getString("GovernmentIdNumber");
                    s.address = rs.getString("Address");
                    return s;
                }
            }
        }
        throw new SQLException("Profile not found: " + profileId);
    }

    /** Loads session context (examId, licenceId, licenseCode) for registration. */
    private SessionContext loadSessionContext(int sessionId) throws SQLException {
        String sql = """
                SELECT s.ExamId, e.LicenceId, l.LicenceClass AS licenseCode
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
                    ctx.licenseCode = rs.getString("licenseCode");
                    return ctx;
                }
            }
        }
        return null;
    }

    /** Maps a CANDIDATE_SELECT ResultSet row to an ExamRegistrationDTO with computed fields. */
    private ExamRegistrationDTO mapResultSetToExamRegistration(ResultSet rs) throws SQLException {
        ExamRegistrationDTO er = new ExamRegistrationDTO();
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

    /**
     * Synchronises section statuses for all candidates in a session:
     * - Marks testing candidates as Testing when TheoryPaper started but not submitted
     * - Marks submitted theory papers as AwaitingSignature
     * - Marks candidates with ExamResult records as AwaitingSignature
     *
     * @param sessionId the SessionId
     */
    @Override
    public void syncSectionStatusesForSession(int sessionId) {
        if (sessionId <= 0) {
            return;
        }
        try {
            String testingSql = """
                    UPDATE ec SET SectionStatus = ?
                    FROM Exam_Candidate ec
                    JOIN Candidate c ON c.CandidateId = ec.CandidateId AND c.IsAbsent = 0
                    JOIN TheoryPaper tp ON tp.ExamCandidateId = ec.ExamCandidateId
                    WHERE ec.SessionId = ?
                      AND tp.StartedAt IS NOT NULL AND tp.SubmittedAt IS NULL
                      AND ec.SectionStatus = ?
                    """;
            try (PreparedStatement ps = getConnection().prepareStatement(testingSql)) {
                ps.setString(1, "Testing");
                ps.setInt(2, sessionId);
                ps.setString(3, enums.CandidateStatus.PENDING.getStatus());
                ps.executeUpdate();
            }

            String theoryAwaitSql = """
                    UPDATE ec SET SectionStatus = ?
                    FROM Exam_Candidate ec
                    JOIN Candidate c ON c.CandidateId = ec.CandidateId AND c.IsAbsent = 0
                    JOIN TheoryPaper tp ON tp.ExamCandidateId = ec.ExamCandidateId
                    WHERE ec.SessionId = ?
                      AND tp.SubmittedAt IS NOT NULL
                      AND ec.SectionStatus IN (?, ?)
                    """;
            try (PreparedStatement ps = getConnection().prepareStatement(theoryAwaitSql)) {
                ps.setString(1, "AwaitingSignature");
                ps.setInt(2, sessionId);
                ps.setString(3, enums.CandidateStatus.PENDING.getStatus());
                ps.setString(4, "Testing");
                ps.executeUpdate();
            }

            String scoreAwaitSql = """
                    UPDATE ec SET SectionStatus = ?
                    FROM Exam_Candidate ec
                    JOIN Candidate c ON c.CandidateId = ec.CandidateId AND c.IsAbsent = 0
                    JOIN ExamResult er ON er.ExamCandidateId = ec.ExamCandidateId
                    WHERE ec.SessionId = ?
                      AND ec.SectionStatus IN (?, ?)
                    """;
            try (PreparedStatement ps = getConnection().prepareStatement(scoreAwaitSql)) {
                ps.setString(1, "AwaitingSignature");
                ps.setInt(2, sessionId);
                ps.setString(3, enums.CandidateStatus.PENDING.getStatus());
                ps.setString(4, "Testing");
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Marks a candidate's signature as printed, only when status is AwaitingSignature.
     *
     * @param candidateId the CandidateId
     * @param sessionId   the SessionId
     * @return true if updated
     */
    @Override
    public boolean markSignaturePrinted(int candidateId, int sessionId) {
        String sql = """
                UPDATE Exam_Candidate
                SET SignaturePrinted = 1
                WHERE CandidateId = ? AND SessionId = ?
                  AND SectionStatus = ?
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            ps.setInt(2, sessionId);
            ps.setString(3, "AwaitingSignature");
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Completes a section (marks as Done) after signature is printed.
     * Auto-enrolls into the next session if the candidate passed.
     *
     * @param candidateId the CandidateId
     * @param sessionId   the SessionId
     * @return true if completed
     */
    @Override
    public boolean completeSection(int candidateId, int sessionId) {
        try {
            String checkSql = """
                    SELECT ec.SectionStatus, ec.SignaturePrinted, ec.ExamId
                    FROM Exam_Candidate ec
                    WHERE ec.CandidateId = ? AND ec.SessionId = ?
                    """;
            String status = null;
            boolean printed = false;
            int examId = 0;
            try (PreparedStatement ps = getConnection().prepareStatement(checkSql)) {
                ps.setInt(1, candidateId);
                ps.setInt(2, sessionId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return false;
                    }
                    status = rs.getString("SectionStatus");
                    printed = rs.getBoolean("SignaturePrinted");
                    examId = rs.getInt("ExamId");
                }
            }
            if (!"AwaitingSignature".equals(status) || !printed) {
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

    /** Checks if the candidate passed the current section to enroll in the next. */
    private boolean isPassedForNextSection(int candidateId, int sessionId) throws SQLException {
        ExamRegistrationDTO reg = getById(candidateId);
        if (reg == null || reg.getExamSessionId() != sessionId) {
            reg = null;
            List<ExamRegistrationDTO> list = getCandidatesBySession(sessionId);
            for (ExamRegistrationDTO item : list) {
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

    /** Resolves the Vietnamese section name for a session. */
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

    /** Enrolls the candidate into the next chronological session of the same exam. */
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

    /** Determines if a theory score is passing based on question count thresholds. */
    private static boolean isTheoryPassed(int score) {
        if (score <= 35) {
            return score >= 32;
        }
        return score >= 80;
    }

    @Override
    public java.util.List<java.util.Map<String, Object>> findAppliedScoreDeductions(int candidateId, int sessionId) {
        return new java.util.ArrayList<>();
    }

    @Override
    public boolean finalizeScoreEntry(int candidateId, int sessionId, String sectionKeyword) {
        return false;
    }


    @Override
    public boolean adjustScoreDeductionOccurrence(int candidateId, int sessionId, int deductionId, int delta) {
        return false;
    }

    /** Holds session context fields needed during candidate registration. */
    private static final class SessionContext {
        int examId;
        int licenceId;
        String licenseCode;
    }

    /** Holds a copy of Profile fields used to populate a new Candidate record. */
    private static final class PersonSnapshot {
        String fullName;
        Timestamp dob;
        String phone;
        String sex;
        String govId;
        String address;
    }
}


