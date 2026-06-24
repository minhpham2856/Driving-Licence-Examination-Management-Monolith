package DAO.Impl;

import Constants.Db2Mappings;
import DBConnection.DBContext;
import DAO.Db2CandidateSql;
import DAO.ExamRegistrationDAO;
import DAO.FeeDAO;
import DAO.Impl.FeeDAOImpl;
import DAO.Impl.PaymentDAOImpl;
import DAO.PaymentDAO;
import Models.ExamRegistration;
import Models.Fee;
import Models.Payment;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExamRegistrationDAOImpl extends DBContext implements ExamRegistrationDAO {

    @Override
    public ExamRegistration getById(int id) {
        String sql = Db2CandidateSql.CANDIDATE_SELECT + " WHERE c.CandidateId = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
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
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
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
    public ExamRegistration getBySbd(String sbd) {
        if (sbd == null || !sbd.contains("-")) {
            return null;
        }
        String[] parts = sbd.trim().split("-", 2);
        if (parts.length < 2) {
            return null;
        }
        String licenseCode = parts[0].trim();
        int candidateNo;
        try {
            candidateNo = Integer.parseInt(parts[1].trim());
        } catch (NumberFormatException ex) {
            return null;
        }
        String sql = Db2CandidateSql.CANDIDATE_SELECT
                + """
                 WHERE l.LicenceClass = ?
                   AND TRY_CAST(SUBSTRING(c.CandidateNumber, CHARINDEX('-', c.CandidateNumber) + 1, 10) AS INT) = ?
                 ORDER BY ec.ExamCandidateId DESC
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, licenseCode);
            ps.setInt(2, candidateNo);
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
    public List<ExamRegistration> getCandidatesBySession(int sessionId) {
        Integer examId = resolveExamIdFromSession(sessionId);
        if (examId != null) {
            return getCandidatesByExamId(examId);
        }
        List<ExamRegistration> list = new ArrayList<>();
        String sql = Db2CandidateSql.CANDIDATE_SELECT
                + " WHERE ec.SessionId = ? ORDER BY candidateNo";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
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
    public List<ExamRegistration> getCandidatesByExamId(int examId) {
        ensureAllExamSessionsEnrollment(examId);
        List<ExamRegistration> list = new ArrayList<>();
        String sql = Db2CandidateSql.CANDIDATE_SELECT
                + """
                 WHERE ex.ExamId = ?
                   AND ec.ExamCandidateId = (
                       SELECT MIN(ec2.ExamCandidateId)
                       FROM Exam_Candidate ec2
                       WHERE ec2.CandidateId = c.CandidateId AND ec2.ExamId = ex.ExamId
                   )
                 ORDER BY candidateNo
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, examId);
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
        try (PreparedStatement ps = connection.prepareStatement(sql);
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
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
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
        ExamRegistration reg = getById(id);
        FeeDAO feeDAO = new FeeDAOImpl();
        double amount = 0;
        if (reg != null) {
            amount = feeDAO.sumProcedureFees(reg.getLicenseCode(), reg.isRequiresRoadTest());
        }
        if (amount <= 0) {
            amount = feeDAO.sumProcedureFees("B", true);
        }
        return updatePayment(id, isPaymentCompleted, amount);
    }

    @Override
    public boolean updatePayment(int id, boolean isPaymentCompleted, double totalAmount) {
        if (!isPaymentCompleted) {
            return true;
        }
        try {
            String check = """
                    SELECT TOP 1 PaymentId FROM Payment
                    WHERE CandidateId = ? AND PaymentStatus IN ('Completed', 'Paid')
                    """;
            try (PreparedStatement ps = connection.prepareStatement(check)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return true;
                    }
                }
            }
            ExamRegistration reg = getById(id);
            FeeDAO feeDAO = new FeeDAOImpl();
            List<Fee> fees = reg != null
                    ? feeDAO.getProcedureFees(reg.getLicenseCode(), reg.isRequiresRoadTest())
                    : List.of();
            Payment payment = new Payment();
            payment.setExamRegistrationId(id);
            payment.setAmount(totalAmount);
            payment.setPaymentStatus("Completed");
            payment.setPaymentMethod("Cash");
            payment.setTransactionReference("REF-" + System.currentTimeMillis() % 1000000);
            payment.setNotes("Thu phí, lệ phí tại bàn thủ tục (bảng Fee)");
            PaymentDAO payDAO = new PaymentDAOImpl();
            return payDAO.insertWithFees(payment, fees);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private int resolveExamIdForCandidate(int candidateId) throws SQLException {
        String sql = "SELECT TOP 1 ExamId FROM Exam_Candidate WHERE CandidateId = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
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
            Integer examCandidateId = getExamCandidateIdAny(id);
            if (examCandidateId == null) {
                return false;
            }
            int deviceId = -1;
            if (computerCode != null && !computerCode.isEmpty()) {
                String compSql = "SELECT ExamDeviceId FROM ExamDevice WHERE DeviceName = ? OR DeviceName LIKE ?";
                try (PreparedStatement ps = connection.prepareStatement(compSql)) {
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
            try (PreparedStatement ps = connection.prepareStatement(checkPaper)) {
                ps.setInt(1, examCandidateId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        paperId = rs.getInt("TheoryPaperId");
                    }
                }
            }
            if (paperId == -1) {
                String ins = "INSERT INTO TheoryPaper (ExamCandidateId, ExamDeviceId, StartedAt) VALUES (?, ?, GETDATE())";
                try (PreparedStatement ps = connection.prepareStatement(ins)) {
                    ps.setInt(1, examCandidateId);
                    ps.setInt(2, deviceId);
                    return ps.executeUpdate() > 0;
                }
            }
            String upd = "UPDATE TheoryPaper SET ExamDeviceId = ? WHERE TheoryPaperId = ?";
            try (PreparedStatement ps = connection.prepareStatement(upd)) {
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
    public boolean clearAllocatedRoom(int candidateId) {
        ExamRegistration reg = getById(candidateId);
        if (reg == null) {
            return false;
        }
        String notes = reg.getNotes();
        if (notes != null && notes.startsWith("AllocatedRoom:")) {
            return updateApplicationNotes(candidateId, null);
        }
        return true;
    }

    @Override
    public boolean updateDevice(int id, String deviceCode) {
        String notesVal = (deviceCode != null && !deviceCode.isEmpty()) ? "Device: " + deviceCode : null;
        return updateApplicationNotes(id, notesVal);
    }

    @Override
    public boolean updateScores(int id, int sessionId, Integer theoryScore, String theoryPassed,
            Integer practicalScore, String practicalPassed) {
        Integer examId = resolveExamIdFromSession(sessionId);
        if (examId == null) {
            return false;
        }
        return updateScoresForExam(id, examId, theoryScore, theoryPassed, practicalScore, practicalPassed);
    }

    @Override
    public boolean updateScoresForExam(int id, int examId, Integer theoryScore, String theoryPassed,
            Integer practicalScore, String practicalPassed) {
        try {
            boolean ok = true;
            if (theoryScore != null) {
                ok = upsertSectionScoreForExam(id, examId, "Theory", theoryScore) && ok;
            }
            if (practicalScore != null) {
                ok = upsertSectionScoreForExam(id, examId, "Practical", practicalScore) && ok;
            }
            return ok;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updateRoadScore(int id, int sessionId, Integer roadScore, String roadPassed) {
        Integer examId = resolveExamIdFromSession(sessionId);
        if (examId == null) {
            return false;
        }
        return updateRoadScoreForExam(id, examId, roadScore, roadPassed);
    }

    @Override
    public boolean updateRoadScoreForExam(int id, int examId, Integer roadScore, String roadPassed) {
        try {
            if (roadScore != null) {
                return upsertSectionScoreForExam(id, examId, "Road", roadScore);
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Integer resolveSessionIdForSection(int examId, String sectionKeyword) {
        try {
            return findSessionIdForExamSection(examId, sectionKeyword);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
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
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(sqlCand)) {
                ps.setString(1, fullName);
                ps.setDate(2, dob);
                ps.setString(3, govIdNo);
                ps.setString(4, phoneNo);
                ps.setInt(5, id);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = connection.prepareStatement(sqlProf)) {
                ps.setString(1, fullName);
                ps.setDate(2, dob);
                ps.setString(3, govIdNo);
                ps.setString(4, phoneNo);
                ps.setInt(5, id);
                ps.executeUpdate();
            }
            if (email != null) {
                try (PreparedStatement ps = connection.prepareStatement(sqlUser)) {
                    ps.setString(1, email);
                    ps.setInt(2, id);
                    ps.executeUpdate();
                }
            }
            connection.commit();
            return true;
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ignored) {
            }
            e.printStackTrace();
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException ignored) {
            }
        }
        return false;
    }

    @Override
    public boolean updatePhoto(int id, String photoUrl) {
        String sql = "UPDATE Candidate SET PhotoImageUrl = ? WHERE CandidateId = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
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
    public boolean insert(ExamRegistration reg) {
        try {
            connection.setAutoCommit(false);
            SessionContext ctx = loadSessionContext(reg.getExamSessionId());
            if (ctx == null) {
                connection.rollback();
                return false;
            }
            int applicationId = findOrCreateApplication(reg, ctx.licenceId);
            if (applicationId <= 0) {
                connection.rollback();
                return false;
            }
            int userId = findUserIdByProfile(reg.getPersonId());
            if (userId <= 0) {
                connection.rollback();
                return false;
            }
            PersonSnapshot snap = loadProfileSnapshot(reg.getPersonId());
            String candidateNumber = Db2Mappings.buildCandidateNumber(ctx.licenseCode, reg.getCandidateNo());
            String sqlCand = """
                    INSERT INTO Candidate (CandidateNumber, FullName, DateOfBirth, PhoneNumber, Sex,
                        GovernmentIdNumber, Address, UserId, ExamRegistrationId)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """;
            int candidateId;
            try (PreparedStatement ps = connection.prepareStatement(sqlCand, Statement.RETURN_GENERATED_KEYS)) {
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
                    if (!gk.next()) {
                        connection.rollback();
                        return false;
                    }
                    candidateId = gk.getInt(1);
                }
            }
            enrollCandidateInAllExamSessions(candidateId, ctx.examId);
            if (reg.isPresent()) {
                updatePresent(candidateId, true);
            }
            connection.commit();
            reg.setId(candidateId);
            return true;
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ignored) {
            }
            e.printStackTrace();
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException ignored) {
            }
        }
        return false;
    }

    @Override
    public boolean markAbsent(int candidateId) {
        return updateApplicationNotes(candidateId, "Absent");
    }

    @Override
    public boolean clearAbsentMarking(int candidateId) {
        try {
            connection.setAutoCommit(false);
            deleteCandidateScores(candidateId);
            String sql = """
                    UPDATE ExamRegistration SET Notes = NULL                    WHERE ExamRegistrationId = (SELECT ExamRegistrationId FROM Candidate WHERE CandidateId = ?)
                    """;
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, candidateId);
                ps.executeUpdate();
            }
            connection.commit();
            return true;
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ignored) {
            }
            e.printStackTrace();
        } finally {
            try {
                connection.setAutoCommit(true);
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
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
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

    private boolean updateApplicationNotes(int candidateId, String notes) {
        String sql = """
                UPDATE ExamRegistration SET Notes = ?                WHERE ExamRegistrationId = (SELECT ExamRegistrationId FROM Candidate WHERE CandidateId = ?)
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
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

    private void deleteCandidateScores(int candidateId) throws SQLException {
        Integer examCandidateId = getExamCandidateIdAny(candidateId);
        if (examCandidateId == null) {
            return;
        }
        String delScores = """
                DELETE es FROM ExamScore es
                JOIN ExamResult er ON er.ExamResultId = es.ExamResultId
                WHERE er.ExamCandidateId = ?
                """;
        String delDeductions = """
                DELETE sd FROM Score_Deduction sd
                JOIN ExamScore es ON es.ExamScoreId = sd.ExamScoreId
                JOIN ExamResult er ON er.ExamResultId = es.ExamResultId
                WHERE er.ExamCandidateId = ?
                """;
        String delResult = "DELETE FROM ExamResult WHERE ExamCandidateId = ?";
        String delPaper = "DELETE FROM TheoryPaper WHERE ExamCandidateId = ?";
        try (PreparedStatement ps = connection.prepareStatement(delDeductions)) {
            ps.setInt(1, examCandidateId);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = connection.prepareStatement(delScores)) {
            ps.setInt(1, examCandidateId);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = connection.prepareStatement(delResult)) {
            ps.setInt(1, examCandidateId);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = connection.prepareStatement(delPaper)) {
            ps.setInt(1, examCandidateId);
            ps.executeUpdate();
        }
    }

    private boolean upsertSectionScoreForExam(int candidateId, int examId, String sectionKeyword, int score)
            throws SQLException {
        Integer sessionId = findSessionIdForExamSection(examId, sectionKeyword);
        if (sessionId == null) {
            return false;
        }
        return upsertSectionScore(candidateId, sessionId, sectionKeyword, score);
    }

    private boolean upsertSectionScore(int candidateId, int sessionId, String sectionKeyword, int score)
            throws SQLException {
        Integer examCandidateId = ensureExamCandidateId(candidateId, sessionId);
        if (examCandidateId == null) {
            return false;
        }
        Integer sectionId = findSectionIdForCandidate(examCandidateId, sectionKeyword);
        if (sectionId == null) {
            return false;
        }
        int resultId = findOrCreateExamResult(examCandidateId, score >= 80);
        String check = "SELECT ExamScoreId FROM ExamScore WHERE ExamResultId = ? AND ExamSectionId = ?";
        int scoreId = -1;
        try (PreparedStatement ps = connection.prepareStatement(check)) {
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
            try (PreparedStatement ps = connection.prepareStatement(ins)) {
                ps.setInt(1, resultId);
                ps.setInt(2, sectionId);
                ps.setDouble(3, score);
                ps.executeUpdate();
            }
        } else {
            String upd = "UPDATE ExamScore SET Score = ? WHERE ExamScoreId = ?";
            try (PreparedStatement ps = connection.prepareStatement(upd)) {
                ps.setDouble(1, score);
                ps.setInt(2, scoreId);
                ps.executeUpdate();
            }
        }
        return true;
    }

    private Integer ensureExamCandidateId(int candidateId, int sessionId) throws SQLException {
        Integer examCandidateId = getExamCandidateId(candidateId, sessionId);
        if (examCandidateId != null) {
            return examCandidateId;
        }
        int examId = -1;
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT TOP 1 s.ExamId FROM [Session] s WHERE s.SessionId = ?")) {
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    examId = rs.getInt("ExamId");
                }
            }
        }
        if (examId <= 0) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT TOP 1 ex.ExamId FROM Candidate c "
                    + "JOIN ExamRegistration er ON er.ExamRegistrationId = c.ExamRegistrationId "
                    + "JOIN Exam ex ON ex.LicenceId = er.LicenceId "
                    + "WHERE c.CandidateId = ?")) {
                ps.setInt(1, candidateId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        examId = rs.getInt("ExamId");
                    }
                }
            }
        }
        if (examId <= 0 || sessionId <= 0) {
            return null;
        }
        String ins = "INSERT INTO Exam_Candidate (ExamId, CandidateId, SessionId) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(ins, Statement.RETURN_GENERATED_KEYS)) {
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
        return getExamCandidateId(candidateId, sessionId);
    }

    private int findOrCreateExamResult(int examCandidateId, boolean passed) throws SQLException {
        String check = "SELECT ExamResultId FROM ExamResult WHERE ExamCandidateId = ?";
        try (PreparedStatement ps = connection.prepareStatement(check)) {
            ps.setInt(1, examCandidateId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int resultId = rs.getInt("ExamResultId");
                    try (PreparedStatement upd = connection.prepareStatement(
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
        try (PreparedStatement ps = connection.prepareStatement(ins, Statement.RETURN_GENERATED_KEYS)) {
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
        String sectionFilter = switch (keyword) {
            case "Theory" -> "(es.SectionName LIKE N'%Lý thuyết%' OR es.SectionName LIKE '%Theory%')";
            case "Practical" ->
                "(es.SectionName LIKE N'%Thực hành%' OR es.SectionName LIKE N'%Sa hình%' OR es.SectionName LIKE '%Practical%')";
            case "Road" -> "(es.SectionName LIKE N'%Đường%' OR es.SectionName LIKE '%Road%')";
            default -> "1 = 0";
        };
        String sql = """
                SELECT TOP 1 es.ExamSectionId
                FROM Exam_Candidate ec
                JOIN Session_ExamSection ses ON ses.SessionId = ec.SessionId
                JOIN ExamSection es ON es.ExamSectionId = ses.ExamSectionId
                WHERE ec.ExamCandidateId = ?
                  AND
                """ + sectionFilter;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, examCandidateId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ExamSectionId");
                }
            }
        }
        return null;
    }

    private Integer getExamCandidateId(int candidateId, int sessionId) throws SQLException {
        String sql = """
                SELECT ec.ExamCandidateId FROM Exam_Candidate ec
                WHERE ec.CandidateId = ? AND ec.SessionId = ?
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
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

    private void enrollCandidateInAllExamSessions(int candidateId, int examId) throws SQLException {
        String sql = """
                INSERT INTO Exam_Candidate (ExamId, CandidateId, SessionId)
                SELECT s.ExamId, ?, s.SessionId
                FROM [Session] s
                WHERE s.ExamId = ?
                  AND NOT EXISTS (
                      SELECT 1 FROM Exam_Candidate ec
                      WHERE ec.CandidateId = ? AND ec.SessionId = s.SessionId
                  )
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            ps.setInt(2, examId);
            ps.setInt(3, candidateId);
            ps.executeUpdate();
        }
    }

    private void ensureAllExamSessionsEnrollment(int examId) {
        String sql = """
                INSERT INTO Exam_Candidate (ExamId, CandidateId, SessionId)
                SELECT s.ExamId, ec.CandidateId, s.SessionId
                FROM (
                    SELECT DISTINCT CandidateId FROM Exam_Candidate WHERE ExamId = ?
                ) ec
                CROSS JOIN [Session] s
                WHERE s.ExamId = ?
                  AND NOT EXISTS (
                      SELECT 1 FROM Exam_Candidate ec2
                      WHERE ec2.CandidateId = ec.CandidateId AND ec2.SessionId = s.SessionId
                  )
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, examId);
            ps.setInt(2, examId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Integer resolveExamIdFromSession(int sessionId) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT ExamId FROM [Session] WHERE SessionId = ?")) {
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ExamId");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Integer findSessionIdForExamSection(int examId, String keyword) throws SQLException {
        String sectionFilter = switch (keyword) {
            case "Theory" -> "(es.SectionName LIKE N'%Lý thuyết%' OR es.SectionName LIKE '%Theory%')";
            case "Practical" ->
                "(es.SectionName LIKE N'%Thực hành%' OR es.SectionName LIKE N'%Sa hình%' OR es.SectionName LIKE '%Practical%')";
            case "Road" -> "(es.SectionName LIKE N'%Đường%' OR es.SectionName LIKE '%Road%')";
            default -> "1 = 0";
        };
        String sql = """
                SELECT TOP 1 s.SessionId
                FROM [Session] s
                JOIN Session_ExamSection ses ON ses.SessionId = s.SessionId
                JOIN ExamSection es ON es.ExamSectionId = ses.ExamSectionId
                WHERE s.ExamId = ?
                  AND
                """ + sectionFilter + """
                 ORDER BY s.StartTime
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, examId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("SessionId");
                }
            }
        }
        return null;
    }

    private Integer getExamCandidateIdAny(int candidateId) throws SQLException {
        String sql = """
                SELECT TOP 1 ec.ExamCandidateId FROM Exam_Candidate ec
                WHERE ec.CandidateId = ?
                ORDER BY ec.ExamCandidateId DESC
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
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
        String check = "SELECT ExamRegistrationId FROM ExamRegistration WHERE ProfileId = ? AND LicenceId = ?";
        try (PreparedStatement ps = connection.prepareStatement(check)) {
            ps.setInt(1, reg.getPersonId());
            ps.setInt(2, licenceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int appId = rs.getInt("ExamRegistrationId");
                    String status = "WalkIn".equals(reg.getRegistrationType()) ? "WalkIn" : "PreRegistered";
                    if (reg.isPresent()) {
                        status = "CheckedIn";
                    }
                    try (PreparedStatement upd = connection.prepareStatement(
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
        try (PreparedStatement ps = connection.prepareStatement(ins, Statement.RETURN_GENERATED_KEYS)) {
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

    private int findUserIdByProfile(int profileId) throws SQLException {
        String sql = "SELECT UserId FROM Profile WHERE ProfileId = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
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
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
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

    private SessionContext loadSessionContext(int sessionId) throws SQLException {
        String sql = """
                SELECT s.ExamId, e.LicenceId, l.LicenceClass AS licenseCode
                FROM [Session] s
                JOIN Exam e ON e.ExamId = s.ExamId
                JOIN Licence l ON l.LicenceId = e.LicenceId
                WHERE s.SessionId = ?
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
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

        String notes = rs.getString("notes");
        er.setNotes(notes);
        boolean isAbsent = notes != null && "Absent".equalsIgnoreCase(notes.trim());

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
            er.setTheoryPassed(tScoreVal >= 80 ? "passed" : "failed");
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
        er.setProcedureCompletedAt(rs.getTimestamp("procedureCompletedAt"));
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
