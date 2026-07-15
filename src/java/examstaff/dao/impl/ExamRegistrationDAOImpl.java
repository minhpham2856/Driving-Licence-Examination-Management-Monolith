package examstaff.dao.impl;

import examstaff.dbconnection.DBContext;
import examstaff.dao.Db2CandidateSql;
import examstaff.dao.ExamRegistrationDAO;
import examstaff.dto.exam.ExamRegistrationDTO;
import examstaff.util.AllocationPassRules;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExamRegistrationDAOImpl extends DBContext implements ExamRegistrationDAO {

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

    @Override
    public ExamRegistrationDTO getByExamAndSbd(int examId, String sbd) {
        if (sbd == null || sbd.isBlank()) {
            return null;
        }
        int candidateNo = examstaff.util.FormatUtil.parseCandidateNo(sbd.trim());
        if (candidateNo <= 0) {
            String trimmed = sbd.trim();
            for (ExamRegistrationDTO c : getCandidatesByExam(examId)) {
                if (trimmed.equals(c.getSbd())) {
                    return c;
                }
            }
            return null;
        }
        String sql = Db2CandidateSql.CANDIDATE_SELECT
                + """
                 WHERE ee.ExamId = ?
                   AND COALESCE(
                     TRY_CAST(c.CandidateNumber AS INT),
                     TRY_CAST(SUBSTRING(c.CandidateNumber, CHARINDEX('-', c.CandidateNumber) + 1, 10) AS INT)
                   ) = ?
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examId);
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
    public List<ExamRegistrationDTO> getCandidatesByExam(int examId) {
        if (examId <= 0) {
            return List.of();
        }
        List<ExamRegistrationDTO> list = queryCandidates(Db2CandidateSql.CANDIDATE_SELECT,
                " WHERE ex.ExamId = ? ORDER BY candidateNo, ee.ExamEnrollmentId", examId, 0);
        if (list.isEmpty()) {
            list = queryCandidates(Db2CandidateSql.CANDIDATE_SELECT_MINIMAL,
                    " WHERE ex.ExamId = ? ORDER BY candidateNo, ee.ExamEnrollmentId", examId, 0);
        }
        if (!list.isEmpty()) {
            return examstaff.util.ExamEnrollmentMergeUtil.deduplicateByCandidate(list);
        }
        return list;
    }

    private List<ExamRegistrationDTO> queryCandidates(String selectSql, String whereSql, int bindInt, int bindInt2) {
        List<ExamRegistrationDTO> list = new ArrayList<>();
        Connection conn = getConnection();
        if (conn == null) {
            System.err.println("ExamRegistrationDAO: database connection unavailable");
            return list;
        }
        String sql = selectSql + whereSql;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bindInt);
            if (bindInt2 > 0) {
                ps.setInt(2, bindInt2);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToExamRegistration(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("ExamRegistrationDAO query failed: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public boolean updatePresent(int id, boolean isPresent) {
        if (id <= 0) {
            return false;
        }
        if (!isPresent) {
            return true;
        }
        String sql = """
                UPDATE Candidate
                SET IsAbsent = 0
                WHERE CandidateId = ? AND ISNULL(IsAbsent, 0) = 1
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() >= 0;
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
                    SELECT TOP 1 p.PaymentId
                    FROM Payment p
                    INNER JOIN ExamEnrollment ee ON ee.ExamEnrollmentId = p.ExamEnrollmentId
                    WHERE ee.CandidateId = ? AND p.PaymentStatus IN (N'Completed', N'Paid', N'Hoàn tất')
                    """;
            try (PreparedStatement ps = getConnection().prepareStatement(check)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return true;
                    }
                }
            }
            Integer enrollmentId = getExamEnrollmentId(id);
            if (enrollmentId == null) {
                return false;
            }
            String ins = """
                    INSERT INTO Payment (PaymentStatus, PaymentMethod, TransactionReference, TotalAmount, PaidAt, ExamEnrollmentId)
                    VALUES ('Completed', 'Cash', ?, 200000, GETDATE(), ?)
                    """;
            try (PreparedStatement ps = getConnection().prepareStatement(ins)) {
                ps.setString(1, "REF-" + System.currentTimeMillis() % 1000000);
                ps.setInt(2, enrollmentId);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean clearCompletedPayments(int candidateId) {
        if (candidateId <= 0) {
            return false;
        }
        String sql = """
                DELETE p
                FROM Payment p
                INNER JOIN ExamEnrollment ee ON ee.ExamEnrollmentId = p.ExamEnrollmentId
                WHERE ee.CandidateId = ? AND p.PaymentStatus IN (N'Completed', N'Paid', N'Hoàn tất')
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updateAllocatedRoom(int candidateId, int examId, int areaId, String areaName) {
        if (candidateId <= 0 || examId <= 0 || areaId <= 0) {
            return false;
        }
        try {
            return ExamEnrollmentSectionSupport.updateTheoryAllocation(
                    getConnection(), candidateId, examId, areaId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updatePracticalAllocatedRoom(int candidateId, int examId, int areaId, String areaName) {
        if (candidateId <= 0 || examId <= 0 || areaId <= 0) {
            return false;
        }
        try {
            return ExamEnrollmentSectionSupport.updatePracticalAllocation(
                    getConnection(), candidateId, examId, areaId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public String validateUniqueTheoryAllocation(int candidateId, int examId) {
        if (candidateId <= 0 || examId <= 0) {
            return "Không xác định được kỳ thi để phân phòng.";
        }
        String sql = """
                SELECT ea.AreaName
                FROM ExamEnrollment ee
                JOIN ExamEnrollmentSection ees ON ees.ExamEnrollmentId = ee.ExamEnrollmentId
                JOIN ExamSection es ON es.ExamSectionId = ees.ExamSectionId
                LEFT JOIN ExamArea ea ON ea.ExamAreaId = ees.ExamAreaId
                WHERE ee.CandidateId = ? AND ee.ExamId = ?
                  AND ees.ExamAreaId IS NOT NULL
                  AND es.SectionType IN (""" + examstaff.dao.Db2ExamSchemaSql.THEORY_SECTION_TYPES + """
                )
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            ps.setInt(2, examId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String areaName = rs.getString("AreaName");
                    if (areaName == null || areaName.isBlank()) {
                        areaName = "đã phân";
                    }
                    return "Thí sinh đã được phân phòng \"" + areaName.trim() + "\" trong kỳ thi này.";
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Không kiểm tra được phân phòng hiện tại của thí sinh.";
        }
        return null;
    }

    @Override
    public boolean updateScores(int id, Integer theoryScore, String theoryPassed,
            Integer practicalScore, String practicalPassed) {
        return updateScores(id, 0, theoryScore, theoryPassed, practicalScore, practicalPassed);
    }

    private boolean updateScores(int id, int examId, Integer theoryScore, String theoryPassed,
            Integer practicalScore, String practicalPassed) {
        try {
            boolean ok = true;
            if (theoryScore != null) {
                boolean passed = theoryPassed != null
                        ? "passed".equalsIgnoreCase(theoryPassed)
                        : AllocationPassRules.isTheoryPassed(
                                AllocationPassRules.normalizeLicense(findLicenseClassByCandidate(id), null),
                                theoryScore);
                ok = upsertSectionScore(id, examId, "Theory", theoryScore, passed) && ok;
            }
            if (practicalScore != null) {
                boolean passed = practicalPassed != null
                        ? "passed".equalsIgnoreCase(practicalPassed)
                        : AllocationPassRules.isPracticalPassed(practicalScore);
                ok = upsertSectionScore(id, examId, "Practical", practicalScore, passed) && ok;
            }
            return ok;
        } catch (SQLException e) {
            System.err.println("[updateScores] FAILED candidateId=" + id + " examId=" + examId
                    + " theory=" + theoryScore + " practical=" + practicalScore
                    + " -> " + e.getClass().getSimpleName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updateProfile(int id, String fullName, Date dob, String govIdNo, String email, String phoneNo) {
        String sqlCand = """
                UPDATE Candidate
                SET FullName = ?, DateOfBirth = ?, GovernmentIdNumber = ?, PhoneNumber = ?, Email = ?
                WHERE CandidateId = ?
                """;
        String sqlProf = """
                UPDATE Profile
                SET FullName = ?, DateOfBirth = ?, GovernmentIdNumber = ?, PhoneNumber = ?
                WHERE ProfileId = (
                    SELECT TOP 1 p.ProfileId
                    FROM Profile p
                    INNER JOIN Candidate c ON c.GovernmentIdNumber = p.GovernmentIdNumber
                    WHERE c.CandidateId = ?
                )
                """;
        String sqlUser = """
                UPDATE [User] SET Email = ?
                WHERE UserId IN (
                    SELECT TOP 1 p.UserId
                    FROM Profile p
                    INNER JOIN Candidate c ON c.GovernmentIdNumber = p.GovernmentIdNumber
                    WHERE c.CandidateId = ?
                )
                """;
        try {
            getConnection().setAutoCommit(false);
            try (PreparedStatement ps = getConnection().prepareStatement(sqlCand)) {
                ps.setString(1, fullName);
                ps.setDate(2, dob);
                ps.setString(3, govIdNo);
                ps.setString(4, phoneNo);
                if (email != null && !email.isBlank()) {
                    ps.setString(5, email.trim());
                } else {
                    ps.setNull(5, Types.NVARCHAR);
                }
                ps.setInt(6, id);
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
            if (email != null && !email.isBlank()) {
                try (PreparedStatement ps = getConnection().prepareStatement(sqlUser)) {
                    ps.setString(1, email.trim());
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
        String sql = """
                UPDATE Candidate
                SET IsAbsent = 0
                WHERE CandidateId = ? AND IsAbsent = 1
                """;
        try {
            getConnection().setAutoCommit(false);
            deleteAbsentExamResults(candidateId);
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

    private void deleteAbsentExamResults(int candidateId) throws SQLException {
        Integer examCandidateId = getExamEnrollmentId(candidateId);
        if (examCandidateId == null) {
            return;
        }
        String delDeductions = """
                DELETE sd FROM DeductionRecord sd
                JOIN ExamScore es ON es.ExamScoreId = sd.ExamScoreId
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
        ExamEnrollmentSectionSupport.resetTheoryStatus(getConnection(), candidateId);
    }

    private boolean upsertSectionScore(int candidateId, int examId, String sectionKeyword, int score, boolean passed)
            throws SQLException {
        Integer examEnrollmentId = resolveExamEnrollmentForScore(candidateId, examId);
        if (examEnrollmentId == null) {
            System.err.println("[upsertSectionScore] no ExamEnrollment: candidateId=" + candidateId
                    + " examId=" + examId + " section=" + sectionKeyword);
            return false;
        }
        Integer sectionId = findSectionIdForCandidate(examEnrollmentId, sectionKeyword);
        if (sectionId == null && "Theory".equalsIgnoreCase(sectionKeyword)) {
            sectionId = findTheorySectionIdByCandidate(candidateId);
        }
        if (sectionId == null && "Practical".equalsIgnoreCase(sectionKeyword)) {
            sectionId = findPracticalSectionIdByCandidate(candidateId);
        }
        if (sectionId == null && examId > 0) {
            sectionId = findSectionIdByExam(examId, sectionKeyword);
        }
        if (sectionId == null) {
            Integer enrollExamId = getExamIdForEnrollment(examEnrollmentId);
            if (enrollExamId != null && enrollExamId > 0) {
                sectionId = findSectionIdByExam(enrollExamId, sectionKeyword);
            }
        }
        if (sectionId == null) {
            System.err.println("[upsertSectionScore] no ExamSection: candidateId=" + candidateId
                    + " enrollmentId=" + examEnrollmentId + " examId=" + examId
                    + " section=" + sectionKeyword);
            return false;
        }
        int scoreExamId = examId > 0 ? examId : 0;
        if (scoreExamId <= 0) {
            Integer enrollExamId = getExamIdForEnrollment(examEnrollmentId);
            if (enrollExamId != null && enrollExamId > 0) {
                scoreExamId = enrollExamId;
            }
        }
        if (scoreExamId > 0) {
            Integer sessionEnrollment = getExamEnrollmentIdForExam(candidateId, scoreExamId);
            if (sessionEnrollment != null) {
                examEnrollmentId = sessionEnrollment;
            }
        }
        return upsertExamScore(examEnrollmentId, sectionId, score, passed);
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
        return true;
    }

    private Integer findTheorySectionIdByCandidate(int candidateId) throws SQLException {
        int examId = resolveExamIdForCandidate(candidateId);
        if (examId <= 0) {
            return null;
        }
        return ExamEnrollmentSectionSupport.findSectionId(
                getConnection(), examId, examstaff.dao.Db2ExamSchemaSql.THEORY_SECTION_TYPES);
    }

    private Integer findPracticalSectionIdByCandidate(int candidateId) throws SQLException {
        int examId = resolveExamIdForCandidate(candidateId);
        if (examId <= 0) {
            return null;
        }
        return ExamEnrollmentSectionSupport.findSectionId(
                getConnection(), examId, examstaff.dao.Db2ExamSchemaSql.PRACTICAL_SECTION_TYPES);
    }

    private int resolveExamIdForCandidate(int candidateId) throws SQLException {
        String sql = """
                SELECT TOP 1 ee.ExamId
                FROM ExamEnrollment ee
                WHERE ee.CandidateId = ?
                ORDER BY ee.ExamEnrollmentId DESC
                """;
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
     * Ưu tiên ghi danh đúng ca; nếu ca trên URL không khớp (sidebar / session cũ) thì fallback
     * sang bất kỳ ExamEnrollment hợp lệ của thí sinh.
     */
    private Integer resolveExamEnrollmentForScore(int candidateId, int examId) throws SQLException {
        if (examId > 0) {
            Integer forExam = getExamEnrollmentIdForExam(candidateId, examId);
            if (forExam != null) {
                return forExam;
            }
        }
        return getExamEnrollmentId(candidateId);
    }

    private int findOrCreateExamResult(int examCandidateId, boolean passed) throws SQLException {
        String check = "SELECT ExamResultId FROM ExamResult WHERE ExamEnrollmentId = ?";
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
        String ins = "INSERT INTO ExamResult (ExamEnrollmentId, IsPassed) VALUES (?, ?)";
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

    private Integer findSectionIdForCandidate(int examEnrollmentId, String keyword) throws SQLException {
        Integer examId = getExamIdForEnrollment(examEnrollmentId);
        if (examId == null || examId <= 0) {
            return null;
        }
        String types = "Theory".equalsIgnoreCase(keyword)
                ? examstaff.dao.Db2ExamSchemaSql.THEORY_SECTION_TYPES
                : examstaff.dao.Db2ExamSchemaSql.PRACTICAL_SECTION_TYPES;
        Integer fromEnrollment = ExamEnrollmentSectionSupport.findSectionIdForEnrollment(
                getConnection(), examEnrollmentId, types);
        if (fromEnrollment != null) {
            return fromEnrollment;
        }
        return ExamEnrollmentSectionSupport.findSectionId(getConnection(), examId, types);
    }

    private Integer findSectionIdByExam(int examId, String keyword) throws SQLException {
        if (examId <= 0) {
            return null;
        }
        String types = "Theory".equalsIgnoreCase(keyword)
                ? examstaff.dao.Db2ExamSchemaSql.THEORY_SECTION_TYPES
                : examstaff.dao.Db2ExamSchemaSql.PRACTICAL_SECTION_TYPES;
        return ExamEnrollmentSectionSupport.findSectionId(getConnection(), examId, types);
    }

    private Integer getExamIdForEnrollment(int examEnrollmentId) throws SQLException {
        String sql = "SELECT ExamId FROM ExamEnrollment WHERE ExamEnrollmentId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examEnrollmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ExamId");
                }
            }
        }
        return null;
    }

    private Integer getExamEnrollmentId(int candidateId) throws SQLException {
        String sql = """
                SELECT TOP 1 ee.ExamEnrollmentId
                FROM ExamEnrollment ee
                WHERE ee.CandidateId = ?
                ORDER BY ee.ExamEnrollmentId DESC
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

    private Integer getExamEnrollmentIdForExam(int candidateId, int examId) throws SQLException {
        String sql = """
                SELECT ee.ExamEnrollmentId
                FROM ExamEnrollment ee
                WHERE ee.CandidateId = ? AND ee.ExamId = ?
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            ps.setInt(2, examId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ExamEnrollmentId");
                }
            }
        }
        return null;
    }

    private String findLicenseClassByCandidate(int candidateId) throws SQLException {
        String sql = """
                SELECT TOP 1 l.LicenceClass
                FROM Candidate c
                JOIN ExamEnrollment ee ON ee.CandidateId = c.CandidateId
                JOIN Exam ex ON ex.ExamId = ee.ExamId
                JOIN Licence l ON l.LicenceId = ex.LicenceId
                WHERE c.CandidateId = ?
                ORDER BY ee.ExamEnrollmentId DESC
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        }
        return null;
    }

    private static boolean readBit(ResultSet rs, String column) throws SQLException {
        boolean value = rs.getBoolean(column);
        if (rs.wasNull()) {
            return false;
        }
        return value;
    }

    private static Boolean readNullableBoolean(ResultSet rs, String column) throws SQLException {
        boolean value = rs.getBoolean(column);
        if (rs.wasNull()) {
            return null;
        }
        return value;
    }

    private ExamRegistrationDTO mapResultSetToExamRegistration(ResultSet rs) throws SQLException {
        ExamRegistrationDTO er = new ExamRegistrationDTO();
        er.setId(rs.getInt("id"));
        er.setExamId(rs.getInt("examId"));
        try {
            er.setExamEnrollmentId(rs.getInt("examEnrollmentId"));
        } catch (SQLException ignored) {
            er.setExamEnrollmentId(0);
        }
        er.setCandidateNo(rs.getInt("candidateNo"));
        er.setRegistrationType(rs.getString("registrationType"));
        er.setIsPaymentCompleted(readBit(rs, "isPaymentCompleted"));
        er.setIsPresent(readBit(rs, "isPresent"));
        er.setPresentMarkedAt(rs.getTimestamp("presentMarkedAt"));
        er.setFullName(rs.getString("fullName"));
        er.setGovIdNo(rs.getString("govIdNo"));
        er.setDateOfBirth(rs.getDate("dateOfBirth"));
        er.setPhoneNo(rs.getString("phoneNo"));
        er.setEmail(rs.getString("email"));
        er.setPhotoUrl(rs.getString("photoUrl"));
        er.setLicenseCode(rs.getString("licenseCode"));
        er.setComputerCode(rs.getString("computerCode"));
        try {
            er.setTakeTheory(readNullableBoolean(rs, "takeTheory"));
            er.setTakePractical(readNullableBoolean(rs, "takePractical"));
        } catch (SQLException ignored) {
            er.setTakeTheory(null);
            er.setTakePractical(null);
        }
        er.setExamDate(rs.getDate("examDate"));

        String notes = rs.getString("notes");
        er.setNotes(notes);
        boolean isAbsent = readBit(rs, "isAbsent");
        if (!isAbsent && notes != null && "Absent".equalsIgnoreCase(notes.trim())) {
            isAbsent = true;
        }
        er.setAbsent(isAbsent);
        er.setSuspended(readBit(rs, "isSuspended"));

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

        try {
            int pracAreaId = rs.getInt("practicalAllocatedAreaId");
            if (!rs.wasNull()) {
                er.setPracticalAllocatedAreaId(pracAreaId);
                er.setPracticalAllocatedAreaName(rs.getString("practicalAllocatedAreaName"));
            }
        } catch (SQLException ignored) {
        }

        String licenseForPass = AllocationPassRules.normalizeLicense(er.getLicenseCode(), er.getClazz());

        int tScoreVal = rs.getInt("theoryScore");
        if (isAbsent || rs.wasNull() || er.skipsTheory()) {
            er.setTheoryScore(null);
            er.setTheoryPassed("none");
        } else {
            er.setTheoryScore(tScoreVal);
            er.setTheoryPassed(AllocationPassRules.isTheoryPassed(licenseForPass, tScoreVal) ? "passed" : "failed");
        }

        int pScoreVal = rs.getInt("practicalScore");
        if (isAbsent || rs.wasNull() || er.skipsPractical()) {
            er.setPracticalScore(null);
            er.setPracticalPassed("none");
        } else {
            er.setPracticalScore(pScoreVal);
            er.setPracticalPassed(AllocationPassRules.isPracticalPassed(pScoreVal) ? "passed" : "failed");
        }

        return er;
    }
}
