package registrant.dao.impl;

import examstaff.dao.Db2ExamSchemaSql;
import examstaff.enums.PaymentStatus;
import registrant.enums.CandidateSectionStatus;
import registrant.enums.ExamRegistrationLifecycleStatus;
import shared.dbconnection.DBContext;
import registrant.dao.Db2CandidateSql;
import registrant.dao.ExamRegistrationDAO;
import registrant.dto.exam.ExamRegistration;
import registrant.dto.RegistrantSectionRegistrationBlock;
import registrant.dto.exam.SessionExamSectionInfo;
import registrant.dto.exam.SessionScheduleInfo;
import registrant.util.RegistrantExamResultEmailNotifier;
import registrant.util.RegistrantExamSupport;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExamRegistrationDAOImpl extends DBContext implements ExamRegistrationDAO {

    private String lastInsertError;

    @Override
    public String getLastInsertError() {
        return lastInsertError;
    }

    @Override
    public ExamRegistration getById(int id) {
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

    @Override
    public ExamRegistration getBySessionAndSbd(int sessionId, String sbd) {
        if (sbd == null || !sbd.contains("-")) {
            return null;
        }
        try {
            int candidateNo = Integer.parseInt(sbd.split("-")[1]);
            String sql = Db2CandidateSql.CANDIDATE_SELECT
                    + " WHERE ee.ExamId = ? AND TRY_CAST(SUBSTRING(c.CandidateNumber, CHARINDEX('-', c.CandidateNumber) + 1, 10) AS INT) = ?";
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

    @Override
    public List<ExamRegistration> getCandidatesBySession(int sessionId) {
        List<ExamRegistration> list = new ArrayList<>();
        String sql = Db2CandidateSql.CANDIDATE_SELECT
                + " WHERE ee.ExamId = ? ORDER BY candidateNo";
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

    @Override
    public List<ExamRegistration> getAllCandidates() {
        List<ExamRegistration> list = new ArrayList<>();
        String sql = Db2CandidateSql.CANDIDATE_SELECT
                + " ORDER BY CAST(ex.ExamDate AS DATE) DESC, candidateNo";
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

    @Override
    public boolean updatePayment(int id, boolean isPaymentCompleted) {
        if (!isPaymentCompleted) {
            return true;
        }
        try {
            Integer enrollmentId = resolveExamEnrollmentIdForCandidate(id);
            if (enrollmentId == null) {
                return false; // chưa enroll ngày thi
            }
            String check = """
                    SELECT TOP 1 PaymentId FROM Payment
                    WHERE ExamEnrollmentId = ? AND PaymentStatus IN (""" + PaymentStatus.sqlInClause() + """
                    )
                    """;
            try (PreparedStatement ps = getConnection().prepareStatement(check)) {
                ps.setInt(1, enrollmentId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return true;
                    }
                }
            }
            String ins = """
                    INSERT INTO Payment (PaymentStatus, PaymentMethod, TransactionReference, TotalAmount, PaidAt, ExamEnrollmentId)
                    VALUES (?, 'Cash', ?, 200000, GETDATE(), ?)
                    """;
            try (PreparedStatement ps = getConnection().prepareStatement(ins)) {
                ps.setString(1, PaymentStatus.COMPLETED.getDisplayName());
                ps.setString(2, "REF-" + System.currentTimeMillis() % 1000000);
                ps.setInt(3, enrollmentId);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Integer resolveExamEnrollmentIdForCandidate(int candidateId) throws SQLException {
        String sql = """
                SELECT TOP 1 ExamEnrollmentId
                FROM ExamEnrollment
                WHERE CandidateId = ?
                ORDER BY ExamEnrollmentId DESC
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ExamEnrollmentId");
                }
            }
        }
        return null;
    }

    @Override
    public boolean updateComputer(int id, String computerCode) {
        try {
            Integer enrollmentId = ensureExamEnrollmentId(id);
            if (enrollmentId == null) {
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
            try (PreparedStatement ps = getConnection().prepareStatement(
                    "UPDATE ExamEnrollment SET ExamDeviceId = ? WHERE ExamEnrollmentId = ?")) {
                ps.setInt(1, deviceId);
                ps.setInt(2, enrollmentId);
                ps.executeUpdate();
            }
            Integer theorySectionRowId = findTheoryEnrollmentSectionId(enrollmentId);
            if (theorySectionRowId == null) {
                return false;
            }
            try (PreparedStatement ps = getConnection().prepareStatement(
                    "UPDATE ExamEnrollmentSection SET ExamDeviceId = ? WHERE ExamEnrollmentSectionId = ?")) {
                ps.setInt(1, deviceId);
                ps.setInt(2, theorySectionRowId);
                ps.executeUpdate();
            }
            // TheoryPaper gắn ExamEnrollmentSectionId
            String checkPaper = "SELECT TheoryPaperId FROM TheoryPaper WHERE ExamEnrollmentSectionId = ?";
            try (PreparedStatement ps = getConnection().prepareStatement(checkPaper)) {
                ps.setInt(1, theorySectionRowId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return true;
                    }
                }
            }
            String ins = "INSERT INTO TheoryPaper (ExamEnrollmentSectionId, StartedAt) VALUES (?, GETDATE())";
            try (PreparedStatement ps = getConnection().prepareStatement(ins)) {
                ps.setInt(1, theorySectionRowId);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
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
            Integer enrollmentId = ensureExamEnrollmentId(id);
            if (enrollmentId == null) {
                return false;
            }
            Integer sectionId = findTheorySectionIdByCandidate(id);
            if (sectionId == null) {
                sectionId = findSectionIdForCandidate(enrollmentId, "Theory");
            }
            if (sectionId == null) {
                return false;
            }
            return upsertExamScore(enrollmentId, sectionId, correctCount, correctCount >= passThreshold);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
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
    public boolean updateCandidateNumber(int id, String candidateNumber) {
        if (candidateNumber == null || candidateNumber.isBlank()) {
            return false;
        }
        String sql = "UPDATE Candidate SET CandidateNumber = ? WHERE CandidateId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, candidateNumber.trim());
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean insert(ExamRegistration reg) {
        lastInsertError = null;
        try {
            getConnection().setAutoCommit(false);
            SessionContext ctx = loadSessionContext(reg.getExamSessionId());
            if (ctx == null) {
                lastInsertError = "Không tìm thấy ca thi hoặc đợt thi đã đóng.";
                getConnection().rollback();
                return false;
            }
            int applicationId = findOrCreateApplication(reg, ctx.licenceId, ctx.examId);
            if (applicationId <= 0) {
                lastInsertError = "Không thể tạo hồ sơ đăng ký ca thi.";
                getConnection().rollback();
                return false;
            }
            appendExamIdMarker(applicationId, ctx.examId);
            if (reg.isPresent()) {
                try (PreparedStatement ps = getConnection().prepareStatement(
                        "UPDATE ExamRegistration SET RegistrationStatus = ? WHERE ExamRegistrationId = ?")) {
                    ps.setString(1, ExamRegistrationLifecycleStatus.CHECKED_IN);
                    ps.setInt(2, applicationId);
                    ps.executeUpdate();
                }
            }
            getConnection().commit();
            reg.setId(applicationId);
            return true;
        } catch (SQLException e) {
            try {
                getConnection().rollback();
            } catch (SQLException ignored) {
            }
            lastInsertError = mapInsertSqlError(e);
            e.printStackTrace();
        } finally {
            try {
                getConnection().setAutoCommit(true);
            } catch (SQLException ignored) {
            }
        }
        return false;
    }

    private static String mapInsertSqlError(SQLException e) {
        String msg = e.getMessage();
        if (msg == null) {
            return null;
        }
        if (msg.contains("GovernmentIdNumber") || msg.contains("CandidateNumber")) {
            return "Thông tin thí sinh đã tồn tại trong hệ thống. Nếu bạn đã đăng ký trước đó, hãy kiểm tra mục Lịch thi & kết quả.";
        }
        if (msg.contains("ExamRegistration") || msg.contains("#EXAM_ID#")) {
            return "Bạn đã đăng ký đợt thi này rồi.";
        }
        return null;
    }

    @Override
    public boolean applyScoreDeductions(int candidateId, int[] deductionIds, String sectionKeyword) {
        if (deductionIds == null || deductionIds.length == 0) {
            return false;
        }
        try {
            Integer enrollmentId = ensureExamEnrollmentId(candidateId);
            if (enrollmentId == null) {
                return false;
            }
            String keyword = sectionKeyword != null && !sectionKeyword.isBlank() ? sectionKeyword : "Practical";
            Integer sectionId = findSectionIdForCandidate(enrollmentId, keyword);
            if (sectionId == null) {
                sectionId = findSectionIdForCandidate(enrollmentId, "Practical");
            }
            if (sectionId == null) {
                return false;
            }

            upsertExamScore(enrollmentId, sectionId, 100, true);
            int examScoreId = findExamScoreId(enrollmentId, sectionId);
            if (examScoreId <= 0) {
                return false;
            }

            for (int deductionId : deductionIds) {
                if (deductionId <= 0) {
                    continue;
                }
                String ins = """
                        IF NOT EXISTS (
                            SELECT 1 FROM DeductionRecord
                            WHERE ExamScoreId = ? AND ScoreDeductionId = ?
                        )
                        INSERT INTO DeductionRecord (ExamScoreId, ScoreDeductionId, OccurrenceCount, RecordedAt)
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
                    SELECT sd.Points, sd.IsCritical
                    FROM DeductionRecord dr
                    JOIN ScoreDeduction sd ON sd.ScoreDeductionId = dr.ScoreDeductionId
                    WHERE dr.ExamScoreId = ?
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
            int resultId = findOrCreateExamResult(enrollmentId, passed);
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

    private int findExamScoreId(int examEnrollmentId, int sectionId) throws SQLException {
        String sql = """
                SELECT es.ExamScoreId
                FROM ExamScore es
                JOIN ExamResult er ON er.ExamResultId = es.ExamResultId
                WHERE er.ExamEnrollmentId = ? AND es.ExamSectionId = ?
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examEnrollmentId);
            ps.setInt(2, sectionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ExamScoreId");
                }
            }
        }
        return -1;
    }

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

    @Override
    public Integer findCandidateIdByProfileAndSession(int profileId, int sessionId) {
        // sessionId UI = ExamId; trả ExamRegistrationId
        String sql = """
                SELECT TOP 1 er.ExamRegistrationId
                FROM ExamRegistration er
                WHERE er.ProfileId = ?
                  AND er.Notes LIKE N'%#EXAM_ID#' + CAST(? AS NVARCHAR(20)) + N'#%'
                  AND """ + ExamRegistrationLifecycleStatus.SQL_LIFECYCLE_ONLY + """
                  AND """ + ExamRegistrationLifecycleStatus.SQL_EXCLUDE_PROFILE_DOC + """
                ORDER BY er.ExamRegistrationId DESC
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, profileId);
            ps.setInt(2, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ExamRegistrationId");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public SessionExamSectionInfo findPrimarySectionForSession(int sessionId) {
        if (sessionId <= 0) {
            return null;
        }
        // sessionId = ExamId
        String sql = """
                SELECT TOP 1 es.ExamSectionId, es.SectionName
                FROM ExamSection es
                WHERE es.ExamId = ?
                ORDER BY CASE WHEN es.SectionType IN (""" + Db2ExamSchemaSql.THEORY_SECTION_TYPES + """
                ) THEN 0 ELSE 1 END, es.ExamSectionId
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new SessionExamSectionInfo(rs.getInt("ExamSectionId"), rs.getString("SectionName"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public RegistrantSectionRegistrationBlock findActiveSectionRegistration(int profileId, int licenceId, int sectionId) {
        if (profileId <= 0 || licenceId <= 0 || sectionId <= 0) {
            return null;
        }
        String sql = """
                SELECT TOP 1 er.ExamRegistrationId, er.RegistrationStatus, es.SectionName, e.ExamCode AS SessionName
                FROM ExamRegistration er
                INNER JOIN Exam e ON er.Notes LIKE N'%#EXAM_ID#' + CAST(e.ExamId AS NVARCHAR(20)) + N'#%'
                INNER JOIN ExamSection es ON es.ExamId = e.ExamId
                WHERE er.ProfileId = ?
                  AND er.LicenceId = ?
                  AND es.ExamSectionId = ?
                  AND """ + ExamRegistrationLifecycleStatus.SQL_LIFECYCLE_ONLY + """
                  AND """ + ExamRegistrationLifecycleStatus.SQL_EXCLUDE_PROFILE_DOC + """
                ORDER BY er.ExamRegistrationId DESC
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, profileId);
            ps.setInt(2, licenceId);
            ps.setInt(3, sectionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    RegistrantSectionRegistrationBlock block = new RegistrantSectionRegistrationBlock();
                    block.setCandidateId(rs.getInt("ExamRegistrationId"));
                    block.setRegistrationStatus(rs.getString("RegistrationStatus"));
                    block.setSectionName(rs.getString("SectionName"));
                    block.setSessionName(rs.getString("SessionName"));
                    return block;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public SessionScheduleInfo findSessionSchedule(int sessionId) {
        if (sessionId <= 0) {
            return null;
        }
        // sessionId = ExamId
        String sql = """
                SELECT e.ExamId AS SessionId, e.ExamCode AS SessionName,
                       CAST(e.ExamDate AS DATE) AS examDate, e.LicenceId, l.LicenceClass
                FROM Exam e
                INNER JOIN Licence l ON l.LicenceId = e.LicenceId
                WHERE e.ExamId = ?
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapSessionSchedule(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<SessionScheduleInfo> listActiveSessionSchedulesByProfileId(int profileId) {
        if (profileId <= 0) {
            return List.of();
        }
        String sql = """
                SELECT e.ExamId AS SessionId, e.ExamCode AS SessionName,
                       CAST(e.ExamDate AS DATE) AS examDate, e.LicenceId, l.LicenceClass
                FROM ExamRegistration er
                INNER JOIN Exam e ON er.Notes LIKE N'%#EXAM_ID#' + CAST(e.ExamId AS NVARCHAR(20)) + N'#%'
                INNER JOIN Licence l ON l.LicenceId = e.LicenceId
                WHERE er.ProfileId = ?
                  AND """ + ExamRegistrationLifecycleStatus.SQL_LIFECYCLE_ONLY + """
                  AND """ + ExamRegistrationLifecycleStatus.SQL_EXCLUDE_PROFILE_DOC + """
                ORDER BY e.ExamDate ASC
                """;
        List<SessionScheduleInfo> rows = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, profileId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(mapSessionSchedule(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rows;
    }

    private static SessionScheduleInfo mapSessionSchedule(ResultSet rs) throws SQLException {
        SessionScheduleInfo info = new SessionScheduleInfo();
        info.setSessionId(rs.getInt("SessionId"));
        info.setLicenceId(rs.getInt("LicenceId"));
        info.setUiLicenceCode(RegistrantExamSupport.toUiLicenceCode(rs.getString("LicenceClass")));
        info.setSessionName(rs.getString("SessionName"));
        Date examDate = rs.getDate("examDate");
        if (examDate == null) {
            Timestamp ts = rs.getTimestamp("examDate");
            if (ts != null) {
                examDate = new Date(ts.getTime());
            }
        }
        info.setExamDate(examDate);
        return info;
    }

    @Override
    public boolean candidateBelongsToProfile(int candidateId, int profileId) {
        if (candidateId <= 0 || profileId <= 0) {
            return false;
        }
        // candidateId = ExamRegistrationId (portal)
        String sql = """
                SELECT TOP 1 1
                FROM ExamRegistration
                WHERE ExamRegistrationId = ? AND ProfileId = ?
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            ps.setInt(2, profileId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean requestExamCancellation(int candidateId, int profileId, String reason) {
        if (candidateId <= 0 || profileId <= 0 || !candidateBelongsToProfile(candidateId, profileId)) {
            return false;
        }
        String sql = """
                SELECT er.RegistrationStatus, er.ExamRegistrationId, er.Notes
                FROM ExamRegistration er
                WHERE er.ExamRegistrationId = ? AND er.ProfileId = ?
                """;
        try {
            getConnection().setAutoCommit(false);
            String currentStatus = null;
            String existingNotes = null;
            int examRegistrationId = -1;
            try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
                ps.setInt(1, candidateId);
                ps.setInt(2, profileId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        getConnection().rollback();
                        return false;
                    }
                    currentStatus = rs.getString("RegistrationStatus");
                    examRegistrationId = rs.getInt("ExamRegistrationId");
                    existingNotes = rs.getString("Notes");
                }
            }
            // Portal chưa có SBD → luôn pending
            if (!ExamRegistrationLifecycleStatus.canRequestCancellation(currentStatus, true)) {
                getConnection().rollback();
                return false;
            }
            String note = mergeNotesPreserveExamId(existingNotes, buildCancellationNote(reason));
            try (PreparedStatement upd = getConnection().prepareStatement(
                    "UPDATE ExamRegistration SET RegistrationStatus = ?, Notes = ? WHERE ExamRegistrationId = ?")) {
                upd.setString(1, ExamRegistrationLifecycleStatus.CANCEL_REQUESTED);
                upd.setString(2, note);
                upd.setInt(3, examRegistrationId);
                if (upd.executeUpdate() <= 0) {
                    getConnection().rollback();
                    return false;
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

    private static String buildCancellationNote(String reason) {
        String trimmed = reason != null ? reason.trim() : "";
        if (trimmed.isEmpty()) {
            return "Thí sinh gửi yêu cầu hủy đăng ký ca thi.";
        }
        return "Thí sinh gửi yêu cầu hủy đăng ký ca thi. Lý do: " + trimmed;
    }

    /** Giữ marker #EXAM_ID#{id}# khi ghi đè Notes. */
    private static String mergeNotesPreserveExamId(String existingNotes, String newText) {
        String marker = extractExamIdMarker(existingNotes);
        if (marker == null) {
            return newText;
        }
        return marker + " " + newText;
    }

    private static String extractExamIdMarker(String notes) {
        if (notes == null) {
            return null;
        }
        int start = notes.indexOf("#EXAM_ID#");
        if (start < 0) {
            return null;
        }
        int end = notes.indexOf('#', start + "#EXAM_ID#".length());
        if (end < 0) {
            return null;
        }
        return notes.substring(start, end + 1);
    }

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

    /** Xóa điểm/kết quả khi đánh vắng; giữ TheoryPaper. */
    private void deleteAbsentExamResults(int candidateId) throws SQLException {
        Integer enrollmentId = ensureExamEnrollmentId(candidateId);
        if (enrollmentId == null) {
            return;
        }
        String delDeductions = """
                DELETE dr FROM DeductionRecord dr
                JOIN ExamScore es ON es.ExamScoreId = dr.ExamScoreId
                JOIN ExamResult er ON er.ExamResultId = es.ExamResultId
                WHERE er.ExamEnrollmentId = ?
                """;
        String delScores = """
                DELETE es FROM ExamScore es
                JOIN ExamResult er ON er.ExamResultId = es.ExamResultId
                WHERE er.ExamEnrollmentId = ?
                """;
        String delResult = "DELETE FROM ExamResult WHERE ExamEnrollmentId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(delDeductions)) {
            ps.setInt(1, enrollmentId);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = getConnection().prepareStatement(delScores)) {
            ps.setInt(1, enrollmentId);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = getConnection().prepareStatement(delResult)) {
            ps.setInt(1, enrollmentId);
            ps.executeUpdate();
        }
    }

    private void resetSectionStatusAfterAbsentUndo(int candidateId) throws SQLException {
        // Status nằm trên ExamEnrollmentSection, không phải ExamEnrollment
        String sql = """
                UPDATE ees
                SET Status = ?, CompletedAt = NULL
                FROM ExamEnrollmentSection ees
                JOIN ExamEnrollment ee ON ee.ExamEnrollmentId = ees.ExamEnrollmentId
                JOIN ExamSection es ON es.ExamSectionId = ees.ExamSectionId
                WHERE ee.CandidateId = ?
                  AND es.SectionType IN (""" + Db2ExamSchemaSql.THEORY_SECTION_TYPES + """
                )
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, CandidateSectionStatus.PENDING);
            ps.setInt(2, candidateId);
            ps.executeUpdate();
        }
    }

    private boolean upsertSectionScore(int candidateId, String sectionKeyword, int score) throws SQLException {
        return upsertSectionScore(candidateId, sectionKeyword, score, score >= 80);
    }

    private boolean upsertSectionScore(int candidateId, String sectionKeyword, int score, boolean passed)
            throws SQLException {
        Integer enrollmentId = ensureExamEnrollmentId(candidateId);
        if (enrollmentId == null) {
            return false;
        }
        Integer sectionId = findSectionIdForCandidate(enrollmentId, sectionKeyword);
        if (sectionId == null) {
            return false;
        }
        return upsertExamScore(enrollmentId, sectionId, score, passed);
    }

    private boolean upsertExamScore(int examEnrollmentId, int sectionId, int score, boolean passed)
            throws SQLException {
        int resultId = findOrCreateExamResult(examEnrollmentId, passed);
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
        Integer candidateId = resolveCandidateIdByExamEnrollmentId(examEnrollmentId);
        if (candidateId != null) {
            RegistrantExamResultEmailNotifier.trySendAfterScoreSaved(candidateId);
        }
        return true;
    }

    private Integer resolveCandidateIdByExamEnrollmentId(int examEnrollmentId) throws SQLException {
        String sql = "SELECT CandidateId FROM ExamEnrollment WHERE ExamEnrollmentId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examEnrollmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("CandidateId");
                }
            }
        }
        return null;
    }

    private Integer findTheorySectionIdByCandidate(int candidateId) throws SQLException {
        String sql = """
                SELECT TOP 1 es.ExamSectionId
                FROM ExamEnrollment ee
                JOIN ExamSection es ON es.ExamId = ee.ExamId
                WHERE ee.CandidateId = ?
                  AND es.SectionType IN (""" + Db2ExamSchemaSql.THEORY_SECTION_TYPES + """
                )
                ORDER BY ee.ExamEnrollmentId DESC, es.ExamSectionId
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

    /** LOOKUP ExamEnrollment only — never INSERT. */
    private Integer ensureExamEnrollmentId(int candidateId) throws SQLException {
        return resolveExamEnrollmentIdForCandidate(candidateId);
    }

    private int findOrCreateExamResult(int examEnrollmentId, boolean passed) throws SQLException {
        String check = "SELECT ExamResultId FROM ExamResult WHERE ExamEnrollmentId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(check)) {
            ps.setInt(1, examEnrollmentId);
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
        String ins = "INSERT INTO ExamResult (ExamEnrollmentId, IsPassed) VALUES (?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(ins, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, examEnrollmentId);
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

    private Integer findSectionIdForCandidate(int examEnrollmentId, String keyword) throws SQLException {
        String types = "Theory".equalsIgnoreCase(keyword)
                ? Db2ExamSchemaSql.THEORY_SECTION_TYPES
                : Db2ExamSchemaSql.PRACTICAL_SECTION_TYPES;
        String sql = """
                SELECT TOP 1 ees.ExamSectionId
                FROM ExamEnrollmentSection ees
                JOIN ExamSection es ON es.ExamSectionId = ees.ExamSectionId
                WHERE ees.ExamEnrollmentId = ?
                  AND es.SectionType IN (""" + types + """
                )
                ORDER BY ees.ExamEnrollmentSectionId
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examEnrollmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ExamSectionId");
                }
            }
        }
        // Fallback: lấy ExamSection theo ExamId của enrollment
        String fallback = """
                SELECT TOP 1 es.ExamSectionId
                FROM ExamEnrollment ee
                JOIN ExamSection es ON es.ExamId = ee.ExamId
                WHERE ee.ExamEnrollmentId = ?
                  AND es.SectionType IN (""" + types + """
                )
                ORDER BY es.ExamSectionId
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(fallback)) {
            ps.setInt(1, examEnrollmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ExamSectionId");
                }
            }
        }
        return null;
    }

    private Integer findTheoryEnrollmentSectionId(int examEnrollmentId) throws SQLException {
        String sql = """
                SELECT TOP 1 ees.ExamEnrollmentSectionId
                FROM ExamEnrollmentSection ees
                JOIN ExamSection es ON es.ExamSectionId = ees.ExamSectionId
                WHERE ees.ExamEnrollmentId = ?
                  AND es.SectionType IN (""" + Db2ExamSchemaSql.THEORY_SECTION_TYPES + """
                )
                ORDER BY ees.ExamEnrollmentSectionId
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examEnrollmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ExamEnrollmentSectionId");
                }
            }
        }
        return null;
    }

    private int findOrCreateApplication(ExamRegistration reg, int licenceId, int examId) throws SQLException {
        // Tái sử dụng ER lifecycle cùng profile+licence+exam hoặc INSERT mới
        int existingLifecycleId = findExistingExamLifecycleRow(reg.getPersonId(), licenceId, examId);
        if (existingLifecycleId > 0) {
            updateExamLifecycleRow(existingLifecycleId, reg);
            return existingLifecycleId;
        }
        return insertExamLifecycleRow(reg, licenceId);
    }

    private int findExistingExamLifecycleRow(int profileId, int licenceId, int examId) throws SQLException {
        String sql = """
                SELECT TOP 1 er.ExamRegistrationId
                FROM ExamRegistration er
                WHERE er.ProfileId = ? AND er.LicenceId = ?
                  AND er.Notes LIKE N'%#EXAM_ID#' + CAST(? AS NVARCHAR(20)) + N'#%'
                  AND """ + ExamRegistrationLifecycleStatus.SQL_LIFECYCLE_ONLY + """
                  AND """ + ExamRegistrationLifecycleStatus.SQL_EXCLUDE_PROFILE_DOC + """
                ORDER BY er.ExamRegistrationId DESC
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, profileId);
            ps.setInt(2, licenceId);
            ps.setInt(3, examId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ExamRegistrationId");
                }
            }
        }
        return -1;
    }

    private void updateExamLifecycleRow(int examRegistrationId, ExamRegistration reg) throws SQLException {
        // Chỉ đổi status — Notes/#EXAM_ID# do appendExamIdMarker giữ
        String status = resolveExamLifecycleStatus(reg);
        try (PreparedStatement upd = getConnection().prepareStatement(
                "UPDATE ExamRegistration SET RegistrationStatus = ? WHERE ExamRegistrationId = ?")) {
            upd.setString(1, status);
            upd.setInt(2, examRegistrationId);
            upd.executeUpdate();
        }
    }

    private int insertExamLifecycleRow(ExamRegistration reg, int licenceId) throws SQLException {
        String status = resolveExamLifecycleStatus(reg);
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

    private void appendExamIdMarker(int examRegistrationId, int examId) throws SQLException {
        String marker = "#EXAM_ID#" + examId + "#";
        String sql = """
                UPDATE ExamRegistration
                SET Notes = CASE
                    WHEN Notes IS NULL OR LTRIM(RTRIM(Notes)) = N'' THEN ?
                    WHEN Notes LIKE N'%#EXAM_ID#%' THEN Notes
                    ELSE Notes + N' ' + ?
                END
                WHERE ExamRegistrationId = ?
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, marker);
            ps.setString(2, marker);
            ps.setInt(3, examRegistrationId);
            ps.executeUpdate();
        }
    }

    private static String resolveExamLifecycleStatus(ExamRegistration reg) {
        if (reg.isPresent()) {
            return ExamRegistrationLifecycleStatus.CHECKED_IN;
        }
        if ("WalkIn".equals(reg.getRegistrationType())) {
            return ExamRegistrationLifecycleStatus.WALK_IN;
        }
        return ExamRegistrationLifecycleStatus.PRE_REGISTERED;
    }

    private SessionContext loadSessionContext(int sessionId) throws SQLException {
        // sessionId UI = ExamId
        String sql = """
                SELECT e.ExamId, e.LicenceId
                FROM Exam e
                WHERE e.ExamId = ?
                  AND e.[Status] IN (N'Chưa diễn ra', N'Đang diễn ra', N'Open', N'Scheduled', N'InProgress')
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    SessionContext ctx = new SessionContext();
                    ctx.examId = rs.getInt("ExamId");
                    ctx.licenceId = rs.getInt("LicenceId");
                    return ctx;
                }
            }
        }
        return null;
    }

    private ExamRegistration mapResultSetToExamRegistration(ResultSet rs) throws SQLException {
        ExamRegistration er = new ExamRegistration();
        er.setId(rs.getInt("id"));
        er.setExamSessionId(rs.getInt("examSessionId"));
        er.setPersonId(rs.getInt("personId"));
        er.setCandidateNo(rs.getInt("candidateNo"));
        try {
            er.setCandidateNumber(rs.getString("candidateNumber"));
        } catch (SQLException ignored) {
        }
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

    @Override
    public void syncSectionStatusesForSession(int sessionId) {
        if (sessionId <= 0) {
            return;
        }
        // sessionId UI = ExamId
        try {
            String testingSql = """
                    UPDATE ees SET Status = ?
                    FROM ExamEnrollmentSection ees
                    JOIN ExamEnrollment ee ON ee.ExamEnrollmentId = ees.ExamEnrollmentId
                    JOIN Candidate c ON c.CandidateId = ee.CandidateId AND c.IsAbsent = 0
                    JOIN ExamSection es ON es.ExamSectionId = ees.ExamSectionId
                    JOIN TheoryPaper tp ON tp.ExamEnrollmentSectionId = ees.ExamEnrollmentSectionId
                    WHERE ee.ExamId = ?
                      AND es.SectionType IN (""" + Db2ExamSchemaSql.THEORY_SECTION_TYPES + """
                    )
                      AND tp.StartedAt IS NOT NULL AND tp.SubmittedAt IS NULL
                      AND ees.Status = ?
                    """;
            try (PreparedStatement ps = getConnection().prepareStatement(testingSql)) {
                ps.setString(1, CandidateSectionStatus.TESTING);
                ps.setInt(2, sessionId);
                ps.setString(3, CandidateSectionStatus.PENDING);
                ps.executeUpdate();
            }

            String theoryAwaitSql = """
                    UPDATE ees SET Status = ?
                    FROM ExamEnrollmentSection ees
                    JOIN ExamEnrollment ee ON ee.ExamEnrollmentId = ees.ExamEnrollmentId
                    JOIN Candidate c ON c.CandidateId = ee.CandidateId AND c.IsAbsent = 0
                    JOIN ExamSection es ON es.ExamSectionId = ees.ExamSectionId
                    JOIN TheoryPaper tp ON tp.ExamEnrollmentSectionId = ees.ExamEnrollmentSectionId
                    WHERE ee.ExamId = ?
                      AND es.SectionType IN (""" + Db2ExamSchemaSql.THEORY_SECTION_TYPES + """
                    )
                      AND tp.SubmittedAt IS NOT NULL
                      AND ees.Status IN (?, ?)
                    """;
            try (PreparedStatement ps = getConnection().prepareStatement(theoryAwaitSql)) {
                ps.setString(1, CandidateSectionStatus.AWAITING_SIGNATURE);
                ps.setInt(2, sessionId);
                ps.setString(3, CandidateSectionStatus.PENDING);
                ps.setString(4, CandidateSectionStatus.TESTING);
                ps.executeUpdate();
            }

            String scoreAwaitSql = """
                    UPDATE ees SET Status = ?
                    FROM ExamEnrollmentSection ees
                    JOIN ExamEnrollment ee ON ee.ExamEnrollmentId = ees.ExamEnrollmentId
                    JOIN Candidate c ON c.CandidateId = ee.CandidateId AND c.IsAbsent = 0
                    JOIN ExamSection es ON es.ExamSectionId = ees.ExamSectionId
                    JOIN ExamResult er ON er.ExamEnrollmentId = ee.ExamEnrollmentId
                    WHERE ee.ExamId = ?
                      AND es.SectionType IN (""" + Db2ExamSchemaSql.THEORY_SECTION_TYPES + """
                    )
                      AND ees.Status IN (?, ?)
                    """;
            try (PreparedStatement ps = getConnection().prepareStatement(scoreAwaitSql)) {
                ps.setString(1, CandidateSectionStatus.AWAITING_SIGNATURE);
                ps.setInt(2, sessionId);
                ps.setString(3, CandidateSectionStatus.PENDING);
                ps.setString(4, CandidateSectionStatus.TESTING);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean markSignaturePrinted(int candidateId, int sessionId) {
        // sessionId UI = ExamId; CompletedAt = đã in biên bản
        String sql = """
                UPDATE ees SET CompletedAt = COALESCE(ees.CompletedAt, GETDATE())
                FROM ExamEnrollment ee
                JOIN ExamEnrollmentSection ees ON ees.ExamEnrollmentId = ee.ExamEnrollmentId
                JOIN ExamSection es ON es.ExamSectionId = ees.ExamSectionId
                WHERE ee.CandidateId = ? AND ee.ExamId = ?
                  AND ees.Status = ?
                  AND es.SectionType IN (""" + Db2ExamSchemaSql.THEORY_SECTION_TYPES + """
                )
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            ps.setInt(2, sessionId);
            ps.setString(3, CandidateSectionStatus.AWAITING_SIGNATURE);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean completeSection(int candidateId, int sessionId) {
        try {
            String checkSql = """
                    SELECT TOP 1 ees.Status, ees.CompletedAt, ee.ExamId
                    FROM ExamEnrollment ee
                    JOIN ExamEnrollmentSection ees ON ees.ExamEnrollmentId = ee.ExamEnrollmentId
                    JOIN ExamSection es ON es.ExamSectionId = ees.ExamSectionId
                    WHERE ee.CandidateId = ? AND ee.ExamId = ?
                      AND es.SectionType IN (""" + Db2ExamSchemaSql.THEORY_SECTION_TYPES + """
                    )
                    ORDER BY ees.ExamEnrollmentSectionId
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
                    status = rs.getString("Status");
                    printed = rs.getTimestamp("CompletedAt") != null;
                    examId = rs.getInt("ExamId");
                }
            }
            if (!CandidateSectionStatus.AWAITING_SIGNATURE.equals(status) || !printed) {
                return false;
            }

            String doneSql = """
                    UPDATE ees SET Status = ?
                    FROM ExamEnrollment ee
                    JOIN ExamEnrollmentSection ees ON ees.ExamEnrollmentId = ee.ExamEnrollmentId
                    JOIN ExamSection es ON es.ExamSectionId = ees.ExamSectionId
                    WHERE ee.CandidateId = ? AND ee.ExamId = ?
                      AND es.SectionType IN (""" + Db2ExamSchemaSql.THEORY_SECTION_TYPES + """
                    )
                    """;
            try (PreparedStatement ps = getConnection().prepareStatement(doneSql)) {
                ps.setString(1, CandidateSectionStatus.DONE);
                ps.setInt(2, candidateId);
                ps.setInt(3, sessionId);
                if (ps.executeUpdate() <= 0) {
                    return false;
                }
            }

            if (isPassedForNextSection(candidateId, sessionId)) {
                enrollNextSection(candidateId, examId);
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isPassedForNextSection(int candidateId, int sessionId) throws SQLException {
        ExamRegistration reg = getById(candidateId);
        if (reg == null || reg.getExamSessionId() != sessionId) {
            reg = null;
            List<ExamRegistration> list = getCandidatesBySession(sessionId);
            for (ExamRegistration item : list) {
                if (item.getId() == candidateId) {
                    reg = item;
                    break;
                }
            }
        }
        if (reg == null) {
            return false;
        }
        String sectionName = resolveSectionNameForExam(sessionId);
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

    private String resolveSectionNameForExam(int examId) throws SQLException {
        String sql = """
                SELECT TOP 1 es.SectionName
                FROM ExamSection es
                WHERE es.ExamId = ?
                ORDER BY CASE WHEN es.SectionType IN (""" + Db2ExamSchemaSql.THEORY_SECTION_TYPES + """
                ) THEN 0 ELSE 1 END, es.ExamSectionId
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("SectionName");
                }
            }
        }
        return null;
    }

    /** Không INSERT ExamEnrollment — chỉ đảm bảo phần TH nếu đã có enrollment. */
    private void enrollNextSection(int candidateId, int examId) throws SQLException {
        Integer enrollmentId = ensureExamEnrollmentId(candidateId);
        if (enrollmentId == null) {
            return;
        }
        // Kiểm tra enrollment đúng ExamId
        String checkEnroll = """
                SELECT ExamEnrollmentId FROM ExamEnrollment
                WHERE ExamEnrollmentId = ? AND CandidateId = ? AND ExamId = ?
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(checkEnroll)) {
            ps.setInt(1, enrollmentId);
            ps.setInt(2, candidateId);
            ps.setInt(3, examId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    // enrollment mới nhất khác ExamId → lookup đúng exam
                    enrollmentId = null;
                }
            }
        }
        if (enrollmentId == null) {
            String byExam = """
                    SELECT ExamEnrollmentId FROM ExamEnrollment
                    WHERE CandidateId = ? AND ExamId = ?
                    """;
            try (PreparedStatement ps = getConnection().prepareStatement(byExam)) {
                ps.setInt(1, candidateId);
                ps.setInt(2, examId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        enrollmentId = rs.getInt("ExamEnrollmentId");
                    }
                }
            }
        }
        if (enrollmentId == null) {
            return; // không tạo ExamEnrollment
        }
        Integer practicalSectionId = null;
        String findSec = """
                SELECT TOP 1 ExamSectionId FROM ExamSection
                WHERE ExamId = ? AND SectionType IN (""" + Db2ExamSchemaSql.PRACTICAL_SECTION_TYPES + """
                )
                ORDER BY ExamSectionId
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(findSec)) {
            ps.setInt(1, examId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    practicalSectionId = rs.getInt("ExamSectionId");
                }
            }
        }
        if (practicalSectionId == null) {
            return;
        }
        String insertSec = """
                IF NOT EXISTS (
                    SELECT 1 FROM ExamEnrollmentSection
                    WHERE ExamEnrollmentId = ? AND ExamSectionId = ?
                )
                INSERT INTO ExamEnrollmentSection (ExamEnrollmentId, ExamSectionId, Status)
                VALUES (?, ?, ?)
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(insertSec)) {
            ps.setInt(1, enrollmentId);
            ps.setInt(2, practicalSectionId);
            ps.setInt(3, enrollmentId);
            ps.setInt(4, practicalSectionId);
            ps.setString(5, CandidateSectionStatus.PENDING);
            ps.executeUpdate();
        }
    }

    private static boolean isTheoryPassed(int score) {
        if (score <= 35) {
            return score >= 32;
        }
        return score >= 80;
    }

    private static final class SessionContext {
        int examId;
        int licenceId;
    }
}
