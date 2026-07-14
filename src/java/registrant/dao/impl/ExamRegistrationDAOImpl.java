package registrant.dao.impl;

import registrant.enums.CandidateSectionStatus;
import registrant.enums.Db2Mappings;
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

    @Override
    public List<ExamRegistration> getCandidatesBySession(int sessionId) {
        List<ExamRegistration> list = new ArrayList<>();
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

    @Override
    public List<ExamRegistration> getAllCandidates() {
        List<ExamRegistration> list = new ArrayList<>();
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
            String check = """
                    SELECT TOP 1 PaymentId FROM RegistrantPayment
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
                    INSERT INTO RegistrantPayment (PaymentStatus, PaymentMethod, TransactionReference, TotalAmount, PaidAt, CandidateId, ExamId)
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
            int applicationId = findOrCreateApplication(reg, ctx.licenceId);
            if (applicationId <= 0) {
                lastInsertError = "Không thể tạo hồ sơ đăng ký ca thi.";
                getConnection().rollback();
                return false;
            }
            int userId = findUserIdByProfile(reg.getPersonId());
            if (userId <= 0) {
                lastInsertError = "Không tìm thấy tài khoản liên kết hồ sơ.";
                getConnection().rollback();
                return false;
            }
            int candidateId = resolveOrCreateCandidate(reg, ctx, applicationId, userId);
            if (candidateId <= 0) {
                getConnection().rollback();
                return false;
            }
            if (Db2Mappings.isPendingCandidateNumber(reg.getCandidateNumber())
                    || reg.getCandidateNumber() == null
                    || reg.getCandidateNumber().isBlank()) {
                ensurePendingCandidateNumber(candidateId, reg.getPersonId(), reg.getExamSessionId());
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

    private int resolveOrCreateCandidate(ExamRegistration reg, SessionContext ctx,
            int applicationId, int userId) throws SQLException {
        Integer existingId = findCandidateIdByProfile(reg.getPersonId());
        if (existingId != null && existingId > 0) {
            linkCandidateToApplication(existingId, applicationId);
            return existingId;
        }
        PersonSnapshot snap = loadProfileSnapshot(reg.getPersonId());
        String candidateNumber = resolveCandidateNumber(reg, ctx.licenseCode);
        String sqlCand = """
                INSERT INTO Candidate (CandidateNumber, FullName, DateOfBirth, PhoneNumber, Sex,
                    GovernmentIdNumber, Address, UserId, ExamRegistrationId)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sqlCand, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, candidateNumber);
            ps.setString(2, snap.fullName);
            ps.setTimestamp(3, snap.dob);
            ps.setString(4, snap.phone);
            ps.setString(5, snap.sex);
            ps.setString(6, snap.govId);
            ps.setString(7, snap.address);
            ps.setInt(8, userId);
            ps.setInt(9, applicationId);
            ps.executeUpdate();
            try (ResultSet gk = ps.getGeneratedKeys()) {
                if (gk.next()) {
                    return gk.getInt(1);
                }
            }
        } catch (SQLException e) {
            if (isDuplicateKey(e)) {
                Integer byGovId = findCandidateIdByGovernmentId(snap.govId);
                if (byGovId != null && byGovId > 0) {
                    linkCandidateToApplication(byGovId, applicationId);
                    return byGovId;
                }
            }
            throw e;
        }
        lastInsertError = "Không thể tạo bản ghi thí sinh.";
        return -1;
    }

    private void ensurePendingCandidateNumber(int candidateId, int profileId, int sessionId)
            throws SQLException {
        String pending = Db2Mappings.buildPendingCandidateNumber(profileId, sessionId);
        updateCandidateNumber(candidateId, pending);
    }

    private Integer findCandidateIdByProfile(int profileId) throws SQLException {
        String sql = """
                SELECT TOP 1 c.CandidateId
                FROM Candidate c
                INNER JOIN ExamRegistration er ON er.ExamRegistrationId = c.ExamRegistrationId
                WHERE er.ProfileId = ?
                ORDER BY c.CandidateId DESC
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, profileId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("CandidateId");
                }
            }
        }
        return null;
    }

    private Integer findCandidateIdByGovernmentId(String govId) throws SQLException {
        if (govId == null || govId.isBlank()) {
            return null;
        }
        String sql = "SELECT TOP 1 CandidateId FROM Candidate WHERE GovernmentIdNumber = ? ORDER BY CandidateId DESC";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, govId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("CandidateId");
                }
            }
        }
        return null;
    }

    private void linkCandidateToApplication(int candidateId, int applicationId) throws SQLException {
        String sql = "UPDATE Candidate SET ExamRegistrationId = ? WHERE CandidateId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, applicationId);
            ps.setInt(2, candidateId);
            ps.executeUpdate();
        }
    }

    private static boolean isDuplicateKey(SQLException e) {
        int code = e.getErrorCode();
        if (code == 2627 || code == 2601) {
            return true;
        }
        String msg = e.getMessage();
        return msg != null && (msg.contains("UNIQUE") || msg.contains("duplicate"));
    }

    private static String mapInsertSqlError(SQLException e) {
        String msg = e.getMessage();
        if (msg == null) {
            return null;
        }
        if (msg.contains("GovernmentIdNumber") || msg.contains("CandidateNumber")) {
            return "Thông tin thí sinh đã tồn tại trong hệ thống. Nếu bạn đã đăng ký trước đó, hãy kiểm tra mục Lịch thi & kết quả.";
        }
        if (msg.contains("Exam_Candidate")) {
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

    @Override
    public SessionExamSectionInfo findPrimarySectionForSession(int sessionId) {
        if (sessionId <= 0) {
            return null;
        }
        String sql = """
                SELECT TOP 1 ses.ExamSectionId, es.SectionName
                FROM Session_ExamSection ses
                INNER JOIN ExamSection es ON es.ExamSectionId = ses.ExamSectionId
                WHERE ses.SessionId = ?
                ORDER BY ses.ExamSectionId
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
                SELECT TOP 1 c.CandidateId, er.RegistrationStatus, es.SectionName, s.SessionName
                FROM Candidate c
                INNER JOIN ExamRegistration er ON er.ExamRegistrationId = c.ExamRegistrationId
                INNER JOIN Exam_Candidate ec ON ec.CandidateId = c.CandidateId
                INNER JOIN [Session] s ON s.SessionId = ec.SessionId
                INNER JOIN Exam e ON e.ExamId = ec.ExamId
                INNER JOIN Session_ExamSection ses ON ses.SessionId = s.SessionId
                INNER JOIN ExamSection es ON es.ExamSectionId = ses.ExamSectionId
                WHERE er.ProfileId = ?
                  AND e.LicenceId = ?
                  AND ses.ExamSectionId = ?
                  AND er.RegistrationStatus NOT IN (N'Draft', N'Pending', N'Approved', N'Rejected',
                      N'RegistrationRejected', N'Cancelled')
                ORDER BY c.CandidateId DESC
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, profileId);
            ps.setInt(2, licenceId);
            ps.setInt(3, sectionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    RegistrantSectionRegistrationBlock block = new RegistrantSectionRegistrationBlock();
                    block.setCandidateId(rs.getInt("CandidateId"));
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
        String sql = """
                SELECT s.SessionId, s.SessionName, CAST(e.ExamDate AS DATE) AS examDate, e.LicenceId, l.LicenceClass
                FROM [Session] s
                INNER JOIN Exam e ON e.ExamId = s.ExamId
                INNER JOIN Licence l ON l.LicenceId = e.LicenceId
                WHERE s.SessionId = ?
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
                SELECT ec.SessionId, s.SessionName, CAST(e.ExamDate AS DATE) AS examDate, e.LicenceId, l.LicenceClass
                FROM Candidate c
                INNER JOIN ExamRegistration er ON er.ExamRegistrationId = c.ExamRegistrationId
                INNER JOIN Exam_Candidate ec ON ec.CandidateId = c.CandidateId
                INNER JOIN [Session] s ON s.SessionId = ec.SessionId
                INNER JOIN Exam e ON e.ExamId = ec.ExamId
                INNER JOIN Licence l ON l.LicenceId = e.LicenceId
                WHERE er.ProfileId = ?
                  AND er.RegistrationStatus NOT IN (N'Draft', N'Pending', N'Approved', N'Rejected',
                      N'RegistrationRejected', N'Cancelled')
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
        String sql = """
                SELECT TOP 1 1
                FROM Candidate c
                INNER JOIN ExamRegistration er ON er.ExamRegistrationId = c.ExamRegistrationId
                WHERE c.CandidateId = ? AND er.ProfileId = ?
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
                SELECT c.CandidateNumber, er.RegistrationStatus, er.ExamRegistrationId, s.SessionName
                FROM Candidate c
                INNER JOIN ExamRegistration er ON er.ExamRegistrationId = c.ExamRegistrationId
                INNER JOIN Exam_Candidate ec ON ec.CandidateId = c.CandidateId
                INNER JOIN [Session] s ON s.SessionId = ec.SessionId
                WHERE c.CandidateId = ? AND er.ProfileId = ?
                """;
        try {
            getConnection().setAutoCommit(false);
            String candidateNumber = null;
            String currentStatus = null;
            int examRegistrationId = -1;
            try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
                ps.setInt(1, candidateId);
                ps.setInt(2, profileId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        getConnection().rollback();
                        return false;
                    }
                    candidateNumber = rs.getString("CandidateNumber");
                    currentStatus = rs.getString("RegistrationStatus");
                    examRegistrationId = rs.getInt("ExamRegistrationId");
                }
            }
            if (!ExamRegistrationLifecycleStatus.canRequestCancellation(currentStatus,
                    Db2Mappings.isPendingCandidateNumber(candidateNumber))) {
                getConnection().rollback();
                return false;
            }
            String note = buildCancellationNote(reason);
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

    /** Removes exam results/scores created when marking absent; keeps TheoryPaper and answers. */
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

    private void resetSectionStatusAfterAbsentUndo(int candidateId) throws SQLException {
        String sql = """
                UPDATE ec
                SET SectionStatus = ?, SignaturePrinted = 0
                FROM Exam_Candidate ec
                WHERE ec.CandidateId = ?
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
        Integer candidateId = resolveCandidateIdByExamCandidateId(examCandidateId);
        if (candidateId != null) {
            RegistrantExamResultEmailNotifier.trySendAfterScoreSaved(candidateId);
        }
        return true;
    }

    private Integer resolveCandidateIdByExamCandidateId(int examCandidateId) throws SQLException {
        String sql = "SELECT CandidateId FROM Exam_Candidate WHERE ExamCandidateId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examCandidateId);
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

    private Integer findSectionIdForCandidate(int examCandidateId, String keyword) throws SQLException {
        // B2: lý thuyết (exam 2) và thực hành/sa hình (exam 3) dùng cùng LicenceId - không chỉ ExamId hiện tại
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
                  AND (es.SectionName LIKE ? OR es.SectionName LIKE ? OR es.SectionName LIKE ?)
                ORDER BY
                    CASE WHEN ses.SessionId = ec.SessionId THEN 0 ELSE 1 END,
                    es.ExamSectionId
                """;
        String likeVi = "%" + (keyword.equals("Theory") ? "Lý thuyết"
                : keyword.equals("Practical") ? "Thực hành" : "Đường") + "%";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examCandidateId);
            ps.setString(2, "%" + keyword + "%");
            ps.setString(3, likeVi);
            ps.setString(4, "%Road%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ExamSectionId");
                }
            }
        }
        return null;
    }

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

    private int findOrCreateApplication(ExamRegistration reg, int licenceId) throws SQLException {
        /*
         * Không ghi đè hàng workflow tài liệu (Draft/Pending/Approved/Rejected).
         * Chỉ tái sử dụng hàng ca thi (PreRegistered, CheckedIn, …) hoặc INSERT mới.
         */
        int existingLifecycleId = findExistingExamLifecycleRow(reg.getPersonId(), licenceId);
        if (existingLifecycleId > 0) {
            updateExamLifecycleRow(existingLifecycleId, reg);
            return existingLifecycleId;
        }
        return insertExamLifecycleRow(reg, licenceId);
    }

    private int findExistingExamLifecycleRow(int profileId, int licenceId) throws SQLException {
        String sql = """
                SELECT TOP 1 ExamRegistrationId
                FROM ExamRegistration
                WHERE ProfileId = ? AND LicenceId = ?
                  AND RegistrationStatus NOT IN (N'Draft', N'Pending', N'Approved', N'Rejected')
                ORDER BY ExamRegistrationId DESC
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, profileId);
            ps.setInt(2, licenceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ExamRegistrationId");
                }
            }
        }
        return -1;
    }

    private void updateExamLifecycleRow(int examRegistrationId, ExamRegistration reg) throws SQLException {
        String status = resolveExamLifecycleStatus(reg);
        try (PreparedStatement upd = getConnection().prepareStatement(
                "UPDATE ExamRegistration SET RegistrationStatus = ?, Notes = ? WHERE ExamRegistrationId = ?")) {
            upd.setString(1, status);
            upd.setString(2, reg.getNotes());
            upd.setInt(3, examRegistrationId);
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

    private static String resolveExamLifecycleStatus(ExamRegistration reg) {
        if (reg.isPresent()) {
            return "CheckedIn";
        }
        if ("WalkIn".equals(reg.getRegistrationType())) {
            return "WalkIn";
        }
        return "PreRegistered";
    }

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

    private String resolveCandidateNumber(ExamRegistration reg, String licenseCode) {
        if (reg.getCandidateNumber() != null && !reg.getCandidateNumber().isBlank()) {
            return reg.getCandidateNumber().trim();
        }
        if (reg.getCandidateNo() > 0) {
            return Db2Mappings.buildCandidateNumber(licenseCode, reg.getCandidateNo());
        }
        return Db2Mappings.buildPendingCandidateNumber(reg.getPersonId(), reg.getExamSessionId());
    }

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
                ps.setString(1, CandidateSectionStatus.TESTING);
                ps.setInt(2, sessionId);
                ps.setString(3, CandidateSectionStatus.PENDING);
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
                ps.setString(1, CandidateSectionStatus.AWAITING_SIGNATURE);
                ps.setInt(2, sessionId);
                ps.setString(3, CandidateSectionStatus.PENDING);
                ps.setString(4, CandidateSectionStatus.TESTING);
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
        String sql = """
                UPDATE Exam_Candidate
                SET SignaturePrinted = 1
                WHERE CandidateId = ? AND SessionId = ?
                  AND SectionStatus = ?
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
            if (!CandidateSectionStatus.AWAITING_SIGNATURE.equals(status) || !printed) {
                return false;
            }

            String doneSql = """
                    UPDATE Exam_Candidate
                    SET SectionStatus = ?
                    WHERE CandidateId = ? AND SessionId = ?
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
                enrollNextSection(candidateId, sessionId, examId);
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
            ps.setString(7, CandidateSectionStatus.PENDING);
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
