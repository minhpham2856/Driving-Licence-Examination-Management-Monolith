package dao.impl;

import dbconnection.DBContext;

import dao.Db2CandidateSql;

import dao.ExamRegistrationDAO;

import dto.exam.ExamRegistrationDTO;

import model.ExamRegistration;
import util.examstaff.AllocationPassRules;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExamRegistrationDAOImpl extends DBContext implements ExamRegistrationDAO {

    // Lay model dang ky theo id
    @Override
    public ExamRegistration findById(int id) {
        String sql = "SELECT * FROM ExamRegistration WHERE ExamRegistrationId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ExamRegistration r = new ExamRegistration();
                    r.setId(rs.getInt("ExamRegistrationId"));
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
    // Lay dang ky theo id

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
    // Lay thi sinh theo ca va SBD
    }

    @Override
    public ExamRegistrationDTO getBySessionAndSbd(int sessionId, String sbd) {
        if (sbd == null || sbd.isBlank()) {
            return null;
        }
        int candidateNo = util.FormatUtil.parseCandidateNo(sbd.trim());
        if (candidateNo <= 0) {
            return null;
        }
        String sql = Db2CandidateSql.CANDIDATE_SELECT
                + """
                 WHERE ee.SessionId = ?
                   AND COALESCE(
                     TRY_CAST(c.CandidateNumber AS INT),
                     TRY_CAST(SUBSTRING(c.CandidateNumber, CHARINDEX('-', c.CandidateNumber) + 1, 10) AS INT)
                   ) = ?
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            ps.setInt(2, candidateNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToExamRegistration(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    // Danh sach thi sinh theo ca
        return null;
    }

    @Override
    public List<ExamRegistrationDTO> getCandidatesBySession(int sessionId) {
        List<ExamRegistrationDTO> list = queryCandidates(Db2CandidateSql.CANDIDATE_SELECT,
                " WHERE ee.SessionId = ? ORDER BY candidateNo", sessionId, 0);
        if (!list.isEmpty()) {
            return list;
        }
    // Danh sach thi sinh theo ngay thi
        return queryCandidates(Db2CandidateSql.CANDIDATE_SELECT_MINIMAL,
                " WHERE ee.SessionId = ? ORDER BY candidateNo", sessionId, 0);
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
    // query candidates
            return util.ExamEnrollmentMergeUtil.deduplicateByCandidate(list);
        }
        return loadCandidatesByExamSessions(examId);
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
    // Tai candidates by exam sessions
            System.err.println("ExamRegistrationDAO query failed: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    private List<ExamRegistrationDTO> loadCandidatesByExamSessions(int examId) {
        List<ExamRegistrationDTO> combined = new ArrayList<>();
        Connection conn = getConnection();
        if (conn == null) {
            return combined;
        }
        String sessionSql = "SELECT SessionId FROM [Session] WHERE ExamId = ? ORDER BY StartTime";
        try (PreparedStatement ps = conn.prepareStatement(sessionSql)) {
            ps.setInt(1, examId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    combined.addAll(getCandidatesBySession(rs.getInt("SessionId")));
                }
            }
    // Lay thi sinh theo ngay thi va SBD
        } catch (SQLException e) {
            System.err.println("loadCandidatesByExamSessions failed: " + e.getMessage());
            e.printStackTrace();
        }
        return util.ExamEnrollmentMergeUtil.deduplicateByCandidate(combined);
    }

    @Override
    public ExamRegistrationDTO getByExamAndSbd(int examId, String sbd) {
        if (sbd == null || sbd.isBlank()) {
            return null;
        }
        String trimmed = sbd.trim();
    // Lay tat ca dang ky
        for (ExamRegistrationDTO c : getCandidatesByExam(examId)) {
            if (trimmed.equals(c.getSbd())) {
                return c;
            }
        }
        return null;
    }

    @Override
    public List<ExamRegistrationDTO> getAllCandidates() {
        List<ExamRegistrationDTO> list = new ArrayList<>();
        String sql = Db2CandidateSql.CANDIDATE_SELECT
                + " ORDER BY CAST(s.StartTime AS DATE) DESC, candidateNo";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
    // Cap nhat co mat / vang
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
    // Cap nhat trang thai thanh toan
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
    // Xoa giao dich thanh toan da hoan tat
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
    // Xac dinh exam id for candidate
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

    private int resolveExamIdForCandidate(int candidateId) throws SQLException {
        String sql = """
                SELECT TOP 1 s.ExamId
                FROM ExamEnrollment ee
                INNER JOIN [Session] s ON s.SessionId = ee.SessionId
                WHERE ee.CandidateId = ?
    // Gan may tinh cho thi sinh
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
            String checkPaper = "SELECT TheoryPaperId FROM TheoryPaper WHERE ExamEnrollmentId = ?";
            try (PreparedStatement ps = getConnection().prepareStatement(checkPaper)) {
                ps.setInt(1, examCandidateId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        paperId = rs.getInt("TheoryPaperId");
                    }
                }
            }
            try (PreparedStatement ps = getConnection().prepareStatement(
                    "UPDATE ExamEnrollment SET ExamDeviceId = ? WHERE ExamEnrollmentId = ?")) {
                ps.setInt(1, deviceId);
                ps.setInt(2, examCandidateId);
                ps.executeUpdate();
            }
            if (paperId == -1) {
                String ins = "INSERT INTO TheoryPaper (ExamEnrollmentId, ExamDeviceId, StartedAt) VALUES (?, ?, GETDATE())";
                try (PreparedStatement ps = getConnection().prepareStatement(ins)) {
                    ps.setInt(1, examCandidateId);
                    ps.setInt(2, deviceId);
                    return ps.executeUpdate() > 0;
    // Cap nhat phong da phan bo
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
    public boolean updateAllocatedRoom(int candidateId, int sessionId, int areaId, String areaName) {
        if (candidateId <= 0 || sessionId <= 0 || areaId <= 0) {
            return false;
        }
        if (validateUniqueTheoryAllocation(candidateId, sessionId) != null) {
            return false;
        }
        String sql = """
                UPDATE ExamEnrollment
                SET AllocatedExamAreaId = ?,
                    ExamDeviceId = NULL
                WHERE CandidateId = ? AND SessionId = ?
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, areaId);
            ps.setInt(2, candidateId);
            ps.setInt(3, sessionId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public String validateUniqueTheoryAllocation(int candidateId, int sessionId) {
        if (candidateId <= 0 || sessionId <= 0) {
            return "Không xác định được ca thi để phân phòng.";
        }
        int examId = resolveExamIdForSession(sessionId);
        if (examId <= 0) {
            return "Không xác định được kỳ thi.";
        }
        String sql = "SELECT ee.SessionId, "
                + util.examstaff.SessionLabel.SQL_SHIFT_ONLY + " AS SessionName, "
                + "ea.ExamAreaId, ea.AreaName "
                + "FROM ExamEnrollment ee "
                + "INNER JOIN [Session] s ON s.SessionId = ee.SessionId "
                + "LEFT JOIN ExamArea ea ON ea.ExamAreaId = ee.AllocatedExamAreaId "
                + "WHERE ee.CandidateId = ? "
                + "  AND s.ExamId = ? "
                + "  AND ee.AllocatedExamAreaId IS NOT NULL "
                + "  AND ee.SessionId <> ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            ps.setInt(2, examId);
            ps.setInt(3, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String sessionName = rs.getString("SessionName");
                    String areaName = rs.getString("AreaName");
                    if (sessionName == null || sessionName.isBlank()) {
                        sessionName = "ca #" + rs.getInt("SessionId");
                    }
                    if (areaName == null || areaName.isBlank()) {
                        areaName = "phòng #" + rs.getInt("ExamAreaId");
                    }
                    return "Thí sinh đã được phân phòng \"" + areaName.trim()
                            + "\" ở ca \"" + sessionName.trim()
                            + "\". Trong một kỳ thi, mỗi thí sinh chỉ được một ca và một phòng thi.";
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Không kiểm tra được phân phòng hiện tại của thí sinh.";
        }
        return null;
    }

    private int resolveExamIdForSession(int sessionId) {
        String sql = "SELECT ExamId FROM [Session] WHERE SessionId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ExamId");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public boolean updateDevice(int id, String deviceCode) {
        if (deviceCode == null || deviceCode.isEmpty()) {
            return true;
        }
        return updateComputer(id, deviceCode);
    }

    @Override
    public boolean updateScores(int id, Integer theoryScore, String theoryPassed,
            Integer practicalScore, String practicalPassed) {
        return updateScores(id, 0, theoryScore, theoryPassed, practicalScore, practicalPassed);
    }

    @Override
    public boolean updateScores(int id, int sessionId, Integer theoryScore, String theoryPassed,
            Integer practicalScore, String practicalPassed) {
        try {
            boolean ok = true;
            if (theoryScore != null) {
                boolean passed = theoryPassed != null
                        ? "passed".equalsIgnoreCase(theoryPassed)
                        : AllocationPassRules.isTheoryPassed(
                                AllocationPassRules.normalizeLicense(findLicenseClassByCandidate(id), null),
                                theoryScore);
                ok = upsertSectionScore(id, sessionId, "Theory", theoryScore, passed) && ok;
            }
            if (practicalScore != null) {
                boolean passed = practicalPassed != null
                        ? "passed".equalsIgnoreCase(practicalPassed)
                        : AllocationPassRules.isPracticalPassed(practicalScore);
                ok = upsertSectionScore(id, sessionId, "Practical", practicalScore, passed) && ok;
            }
            return ok;
        } catch (SQLException e) {
            System.err.println("[updateScores] FAILED candidateId=" + id + " sessionId=" + sessionId
                    + " theory=" + theoryScore + " practical=" + practicalScore
                    + " -> " + e.getClass().getSimpleName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updateTheoryCorrectCount(int id, int correctCount, int passThreshold) {
        try {
    // Cap nhat diem duong truong
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
    // Cap nhat ho so co ban
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updateRoadScore(int id, Integer roadScore, String roadPassed) {
        // Luồng road test đã bị loại khỏi examstaff/public-call.
        // Giữ method để tương thích interface cũ nhưng không còn ghi điểm đường trường.
        return true;
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
    public boolean updateExaminerProfile(int id, String fullName, Date dob, String govIdNo,
            String email, String phoneNo, String address, String sex, String reasonForTaking) {
        String sqlCand = """
                UPDATE Candidate
                SET FullName = ?, DateOfBirth = ?, GovernmentIdNumber = ?, PhoneNumber = ?, Email = ?,
                    Address = ?, Sex = ?, ReasonForTaking = ?
                WHERE CandidateId = ?
                """;
        String sqlProf = """
                UPDATE Profile
                SET FullName = ?, DateOfBirth = ?, GovernmentIdNumber = ?, PhoneNumber = ?, Address = ?
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
                ps.setString(6, address);
                ps.setString(7, sex);
                ps.setString(8, reasonForTaking);
                ps.setInt(9, id);
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
    // Them dang ky thi (qua Profile)
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
    public boolean insert(ExamRegistrationDTO reg) {
        try {
            getConnection().setAutoCommit(false);
            SessionContext ctx = loadSessionContext(reg.getExamSessionId());
            if (ctx == null) {
                getConnection().rollback();
                return false;
            }
            PersonSnapshot snap = loadProfileSnapshot(reg.getPersonId());
            String candidateNumber = util.FormatUtil.buildCandidateNumber(ctx.licenseCode, reg.getCandidateNo());
            String reason = reg.getReasonForTaking();
            if (reason == null || reason.isBlank()) {
                reason = reg.getNotes();
            }
            int takeNo = reg.getTakeNo() > 0 ? reg.getTakeNo() : 1;
            String sqlCand = """
                    INSERT INTO Candidate (CandidateNumber, FullName, DateOfBirth, PhoneNumber, Email, Sex,
                        GovernmentIdNumber, Address, TakeTheory, TakeLayout, TakeRoad,
                        TakeNo, ReasonForTaking, IsAbsent, IsSuspended)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, 0)
                    """;
            int candidateId;
            try (PreparedStatement ps = getConnection().prepareStatement(sqlCand, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, candidateNumber);
                ps.setString(2, snap.fullName);
                ps.setTimestamp(3, snap.dob);
                ps.setString(4, snap.phone);
                ps.setString(5, snap.email);
                ps.setBoolean(6, parseSexBit(snap.sex));
                ps.setString(7, snap.govId);
                ps.setString(8, snap.address);
                setNullableBoolean(ps, 9, reg.getTakeTheory());
                setNullableBoolean(ps, 10, reg.getTakePractical());
                setNullableBoolean(ps, 11, Boolean.FALSE);
                ps.setInt(12, takeNo);
                ps.setString(13, reason);
                ps.executeUpdate();
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (!gk.next()) {
                        getConnection().rollback();
                        return false;
                    }
                    candidateId = gk.getInt(1);
                }
            }
            String sqlEc = """
                    INSERT INTO ExamEnrollment (CandidateId, SessionId, SectionStatus, SignaturePrinted)
                    VALUES (?, ?, N'Pending', 0)
                    """;
    // Import DSTS vao Candidate + ExamEnrollment
            try (PreparedStatement ps = getConnection().prepareStatement(sqlEc)) {
                ps.setInt(1, candidateId);
                ps.setInt(2, reg.getExamSessionId());
                ps.executeUpdate();
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

    @Override
    public boolean insertFromDstsImport(ExamRegistrationDTO reg) {
        try {
            getConnection().setAutoCommit(false);
            SessionContext ctx = loadSessionContext(reg.getExamSessionId());
            if (ctx == null) {
                getConnection().rollback();
                return false;
            }
            PersonSnapshot snap = snapshotFromReg(reg);
            String candidateNumber = util.FormatUtil.buildCandidateNumber(ctx.licenseCode, reg.getCandidateNo());
            String reason = reg.getReasonForTaking();
            if (reason == null || reason.isBlank()) {
                reason = reg.getNotes();
            }
            int takeNo = reg.getTakeNo() > 0 ? reg.getTakeNo() : 1;
            String sqlCand = """
                    INSERT INTO Candidate (CandidateNumber, FullName, DateOfBirth, PhoneNumber, Email, Sex,
                        GovernmentIdNumber, Address, TakeTheory, TakeLayout, TakeRoad,
                        TakeNo, ReasonForTaking, IsAbsent, IsSuspended)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, 0)
                    """;
            int candidateId;
            try (PreparedStatement ps = getConnection().prepareStatement(sqlCand, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, candidateNumber);
                ps.setString(2, snap.fullName);
                ps.setTimestamp(3, snap.dob);
                ps.setString(4, snap.phone);
                ps.setString(5, snap.email);
                ps.setBoolean(6, parseSexBit(snap.sex));
                ps.setString(7, snap.govId);
                ps.setString(8, snap.address);
                setNullableBoolean(ps, 9, reg.getTakeTheory());
                setNullableBoolean(ps, 10, reg.getTakePractical());
                setNullableBoolean(ps, 11, Boolean.FALSE);
                ps.setInt(12, takeNo);
                ps.setString(13, reason);
                ps.executeUpdate();
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (!gk.next()) {
                        getConnection().rollback();
                        return false;
                    }
                    candidateId = gk.getInt(1);
                }
            }
            if (!ensureExamEnrollmentsForImport(candidateId, ctx.examId,
                    reg.getTakeTheory(), reg.getTakePractical())) {
                getConnection().rollback();
                return false;
            }
            getConnection().commit();
            reg.setId(candidateId);
            return true;
        } catch (SQLException e) {
            try {
                getConnection().rollback();
            } catch (SQLException ignored) {
            }
            System.err.println("insertFromDstsImport failed for " + reg.getGovIdNo() + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                getConnection().setAutoCommit(true);
            // upsert exam score
            } catch (SQLException ignored) {
            }
        }
        return false;
    }

    @Override
    public boolean ensureExamEnrollmentForSession(int candidateId, int sessionId) {
        if (candidateId <= 0 || sessionId <= 0) {
            return false;
        }
        try {
            if (getExamEnrollmentIdForSession(candidateId, sessionId) != null) {
                return true;
            }
            if (loadSessionContext(sessionId) == null) {
                return false;
            }
            String sql = """
                    INSERT INTO ExamEnrollment (CandidateId, SessionId, SectionStatus, SignaturePrinted)
                    VALUES (?, ?, N'Pending', 0)
                    """;
            try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
                ps.setInt(1, candidateId);
                ps.setInt(2, sessionId);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean ensureExamEnrollmentsForImport(int candidateId, int examId,
            Boolean takeTheory, Boolean takePractical) {
        if (candidateId <= 0 || examId <= 0) {
            return false;
        }
        String sql = """
                SELECT s.SessionId, es.SectionName
                FROM [Session] s
                JOIN Session_ExamSection ses ON ses.SessionId = s.SessionId
                JOIN ExamSection es ON es.ExamSectionId = ses.ExamSectionId
                WHERE s.ExamId = ?
                ORDER BY s.StartTime, s.SessionId, es.ExamSectionId
                """;
        boolean ok = true;
        int enrolledCount = 0;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int sessionId = rs.getInt("SessionId");
                    String sectionKind = resolveSectionKind(rs.getString("SectionName"));
                    if (sectionKind == null || !shouldEnrollForSection(sectionKind, takeTheory, takePractical)) {
                        continue;
                    }
                    if (ensureExamEnrollmentForSession(candidateId, sessionId)) {
                        enrolledCount++;
                    } else {
                        ok = false;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return ok && enrolledCount > 0;
    }

    @Override
    public java.util.Set<String> findAvailableSectionKindsForExam(int examId) {
        java.util.LinkedHashSet<String> kinds = new java.util.LinkedHashSet<>();
        if (examId <= 0) {
            return kinds;
        }
        String sql = """
                SELECT DISTINCT es.SectionName
                FROM [Session] s
                JOIN Session_ExamSection ses ON ses.SessionId = s.SessionId
                JOIN ExamSection es ON es.ExamSectionId = ses.ExamSectionId
                WHERE s.ExamId = ?
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String kind = util.examstaff.ImportSectionMatch.resolveSectionKind(rs.getString("SectionName"));
                    if (kind != null) {
                        kinds.add(kind);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return kinds;
    }

    @Override
    public Integer findCandidateIdByGovIdAndExam(String govId, int examId) {
        if (govId == null || govId.isBlank() || examId <= 0) {
            return null;
        }
        String sql = """
                SELECT TOP 1 c.CandidateId
                FROM Candidate c
                INNER JOIN ExamEnrollment ee ON ee.CandidateId = c.CandidateId
                INNER JOIN [Session] s ON s.SessionId = ee.SessionId
                WHERE c.GovernmentIdNumber = ? AND s.ExamId = ?
                ORDER BY c.CandidateId DESC
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, govId.trim());
            ps.setInt(2, examId);
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
    public Integer findCandidateIdByGovId(String govId) {
        if (govId == null || govId.isBlank()) {
            return null;
        }
        String sql = """
                SELECT TOP 1 c.CandidateId
                FROM Candidate c
                WHERE c.GovernmentIdNumber = ?
                ORDER BY c.CandidateId
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, govId.trim());
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

    private static boolean shouldEnrollForSection(String sectionKind, Boolean takeTheory,
            Boolean takePractical) {
        return switch (sectionKind) {
            case util.examstaff.ImportSectionMatch.THEORY ->
                util.examstaff.ImportSectionMatch.wantsSection(takeTheory);
            case util.examstaff.ImportSectionMatch.PRACTICAL ->
                util.examstaff.ImportSectionMatch.wantsSection(takePractical);
            default -> false;
        };
    }

    private static String resolveSectionKind(String sectionName) {
        return util.examstaff.ImportSectionMatch.resolveSectionKind(sectionName);
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
    // Tim exam score id
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
    // Danh dau vang mat
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
    // Danh dau dinh chi thi
                FROM ExamScore es
                JOIN ExamResult er ON er.ExamResultId = es.ExamResultId
                WHERE er.ExamEnrollmentId = ? AND es.ExamSectionId = ?
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examCandidateId);
            ps.setInt(2, sectionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ExamScoreId");
                }
    // Huy dinh chi thi
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
    // Huy danh dau vang
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean markSuspended(int candidateId) {
            // delete absent exam results
            // reset section status after absent undo
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
    // Tim CandidateId theo Profile va ca
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
    // Tim CandidateId theo CCCD va ca
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
                INNER JOIN ExamEnrollment ee ON ee.CandidateId = c.CandidateId
                INNER JOIN Profile p ON p.GovernmentIdNumber = c.GovernmentIdNumber
                WHERE p.ProfileId = ? AND ee.SessionId = ?
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, profileId);
            ps.setInt(2, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
    // delete absent exam results
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
    public Integer findCandidateIdByGovIdAndSession(String govId, int sessionId) {
        if (govId == null || govId.isBlank() || sessionId <= 0) {
            return null;
        }
        String sql = """
                SELECT c.CandidateId
                FROM Candidate c
                INNER JOIN ExamEnrollment ee ON ee.CandidateId = c.CandidateId
                WHERE c.GovernmentIdNumber = ? AND ee.SessionId = ?
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, govId.trim());
            ps.setInt(2, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("CandidateId");
                }
            }
        } catch (SQLException e) {
    // reset section status after absent undo
            e.printStackTrace();
        }
        return null;
    }

    private void deleteAbsentExamResults(int candidateId) throws SQLException {
        Integer examCandidateId = getExamEnrollmentId(candidateId);
        if (examCandidateId == null) {
            return;
        }
        String delDeductions = """
                DELETE sd FROM Score_Deduction sd
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
    // upsert exam score
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
                UPDATE ExamEnrollment
                SET SectionStatus = N'Pending'
                WHERE CandidateId = ?
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            ps.executeUpdate();
        }
    }

    private boolean upsertSectionScore(int candidateId, String sectionKeyword, int score) throws SQLException {
        return upsertSectionScore(candidateId, 0, sectionKeyword, score, score >= 80);
    }

    private boolean upsertSectionScore(int candidateId, String sectionKeyword, int score, boolean passed)
            throws SQLException {
        return upsertSectionScore(candidateId, 0, sectionKeyword, score, passed);
    }

    private boolean upsertSectionScore(int candidateId, int sessionId, String sectionKeyword, int score, boolean passed)
            throws SQLException {
        Integer examEnrollmentId = resolveExamEnrollmentForScore(candidateId, sessionId);
        if (examEnrollmentId == null) {
            System.err.println("[upsertSectionScore] no ExamEnrollment: candidateId=" + candidateId
                    + " sessionId=" + sessionId + " section=" + sectionKeyword);
            return false;
        }
        Integer sectionId = findSectionIdForCandidate(examEnrollmentId, sectionKeyword);
        if (sectionId == null && "Theory".equalsIgnoreCase(sectionKeyword)) {
            sectionId = findTheorySectionIdByCandidate(candidateId);
        }
        if (sectionId == null && "Practical".equalsIgnoreCase(sectionKeyword)) {
            sectionId = findPracticalSectionIdByCandidate(candidateId);
        }
        if (sectionId == null && sessionId > 0) {
            sectionId = findSectionIdBySession(sessionId, sectionKeyword);
        }
        if (sectionId == null && examEnrollmentId != null) {
            Integer enrollSessionId = getSessionIdForEnrollment(examEnrollmentId);
            if (enrollSessionId != null && enrollSessionId > 0) {
                sectionId = findSectionIdBySession(enrollSessionId, sectionKeyword);
            }
        }
        if (sectionId == null) {
            System.err.println("[upsertSectionScore] no ExamSection: candidateId=" + candidateId
                    + " enrollmentId=" + examEnrollmentId + " sessionId=" + sessionId
                    + " section=" + sectionKeyword);
            return false;
        }
        int scoreSessionId = sessionId > 0 ? sessionId : 0;
        if (scoreSessionId <= 0 && examEnrollmentId != null) {
            Integer enrollSessionId = getSessionIdForEnrollment(examEnrollmentId);
            if (enrollSessionId != null && enrollSessionId > 0) {
                scoreSessionId = enrollSessionId;
            }
        }
        if (scoreSessionId > 0) {
            Integer sessionEnrollment = getExamEnrollmentIdForSession(candidateId, scoreSessionId);
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
    // ensure exam enrollment id
            }
        }
        if (scoreId == -1) {
            String ins = "INSERT INTO ExamScore (ExamResultId, ExamSectionId, Score) VALUES (?, ?, ?)";
            try (PreparedStatement ps = getConnection().prepareStatement(ins)) {
                ps.setInt(1, resultId);
                ps.setInt(2, sectionId);
    // Tim or create exam result
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
        String sql = """
                SELECT TOP 1 es.ExamSectionId
                FROM ExamEnrollment ee
                JOIN Session_ExamSection ses ON ses.SessionId = ee.SessionId
                JOIN ExamSection es ON es.ExamSectionId = ses.ExamSectionId
                WHERE ee.CandidateId = ?
                  AND (es.SectionName LIKE N'%Lý thuyết%' OR es.SectionName LIKE '%Theory%')
                ORDER BY ee.ExamEnrollmentId DESC
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

    private Integer findPracticalSectionIdByCandidate(int candidateId) throws SQLException {
        String sql = """
                SELECT TOP 1 es.ExamSectionId
                FROM ExamEnrollment ee
                JOIN Session_ExamSection ses ON ses.SessionId = ee.SessionId
                JOIN ExamSection es ON es.ExamSectionId = ses.ExamSectionId
                WHERE ee.CandidateId = ?
                  AND (es.SectionName LIKE N'%Sa hình%'
                       OR es.SectionName LIKE N'%Thực hành trong hình%'
                       OR es.SectionName LIKE '%Practical%')
                ORDER BY ee.ExamEnrollmentId DESC
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

    private Integer ensureExamEnrollmentId(int candidateId) throws SQLException {
        return ensureExamEnrollmentId(candidateId, 0);
    }

    private Integer ensureExamEnrollmentId(int candidateId, int preferredSessionId) throws SQLException {
        if (preferredSessionId > 0) {
            return getExamEnrollmentIdForSession(candidateId, preferredSessionId);
        }
        return getExamEnrollmentId(candidateId);
    }

    /**
     * Ưu tiên ghi danh đúng ca; nếu ca trên URL không khớp (sidebar / session cũ) thì fallback
     * sang bất kỳ ExamEnrollment hợp lệ của thí sinh.
     */
    private Integer resolveExamEnrollmentForScore(int candidateId, int sessionId) throws SQLException {
        if (sessionId > 0) {
            Integer forSession = getExamEnrollmentIdForSession(candidateId, sessionId);
            if (forSession != null) {
                return forSession;
            }
        }
        return ensureExamEnrollmentId(candidateId, 0);
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
    // Tim section id for candidate via licence
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
    // query section id
    }

    private Integer findSectionIdForCandidate(int examEnrollmentId, String keyword) throws SQLException {
        Integer fromSession = findSectionIdForCandidateViaSession(examEnrollmentId, keyword);
        if (fromSession != null) {
            return fromSession;
        }
        return findSectionIdForCandidateViaLicence(examEnrollmentId, keyword);
    }

    private Integer findSectionIdForCandidateViaSession(int examEnrollmentId, String keyword) throws SQLException {
        String sql = """
                SELECT TOP 1 es.ExamSectionId
                FROM ExamEnrollment ee
                JOIN [Session] curS ON curS.SessionId = ee.SessionId
                JOIN Exam curExam ON curExam.ExamId = curS.ExamId
                JOIN Session_ExamSection ses ON ses.SessionId IN (
                    SELECT s.SessionId
                    FROM [Session] s
                    INNER JOIN Exam e ON e.ExamId = s.ExamId
                    WHERE e.LicenceId = curExam.LicenceId
                )
                JOIN ExamSection es ON es.ExamSectionId = ses.ExamSectionId
                WHERE ee.ExamEnrollmentId = ?
                  AND (es.SectionName LIKE ? OR es.SectionName LIKE ? OR es.SectionName LIKE ? OR es.SectionName LIKE ?)
                ORDER BY
                    CASE WHEN ses.SessionId = ee.SessionId THEN 0 ELSE 1 END,
                    es.ExamSectionId
                """;
        return querySectionId(sql, examEnrollmentId, keyword);
    }

    private Integer findSectionIdForCandidateViaLicence(int examEnrollmentId, String keyword) throws SQLException {
        String sql = """
                SELECT TOP 1 es.ExamSectionId
                FROM ExamEnrollment ee
                JOIN [Session] curS ON curS.SessionId = ee.SessionId
                JOIN Exam curExam ON curExam.ExamId = curS.ExamId
                JOIN Licence_ExamSection les ON les.LicenceId = curExam.LicenceId
                JOIN ExamSection es ON es.ExamSectionId = les.ExamSectionId
                WHERE ee.ExamEnrollmentId = ?
                  AND (es.SectionName LIKE ? OR es.SectionName LIKE ? OR es.SectionName LIKE ? OR es.SectionName LIKE ?)
                ORDER BY es.ExamSectionId
                """;
        return querySectionId(sql, examEnrollmentId, keyword);
    }

    private Integer findSectionIdBySession(int sessionId, String keyword) throws SQLException {
        String sql = """
                SELECT TOP 1 es.ExamSectionId
                FROM Session_ExamSection ses
                JOIN ExamSection es ON es.ExamSectionId = ses.ExamSectionId
                WHERE ses.SessionId = ?
                  AND (es.SectionName LIKE ? OR es.SectionName LIKE ? OR es.SectionName LIKE ? OR es.SectionName LIKE ?)
                ORDER BY es.ExamSectionId
                """;
        String likeVi = "%" + (keyword.equals("Theory") ? "Lý thuyết"
                : keyword.equals("Practical") ? "Sa hình" : "Đường") + "%";
        String likePracticalAlt = keyword.equals("Practical") ? "%Thực hành trong hình%" : likeVi;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            ps.setString(2, "%" + keyword + "%");
            ps.setString(3, likeVi);
            ps.setString(4, keyword.equals("Practical") ? likePracticalAlt : "%Thực hành%");
            ps.setString(5, "%Road%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ExamSectionId");
                }
            }
        }
        return null;
    }

    private Integer querySectionId(String sql, int examEnrollmentId, String keyword) throws SQLException {
        String likeVi = "%" + (keyword.equals("Theory") ? "Lý thuyết"
                : keyword.equals("Practical") ? "Sa hình" : "Đường") + "%";
        String likePracticalAlt = keyword.equals("Practical") ? "%Thực hành trong hình%" : likeVi;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examEnrollmentId);
            ps.setString(2, "%" + keyword + "%");
            ps.setString(3, likeVi);
            ps.setString(4, keyword.equals("Practical") ? likePracticalAlt : "%Thực hành%");
            ps.setString(5, "%Road%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ExamSectionId");
                }
            }
        }
        return null;
    }

    private Integer getSessionIdForEnrollment(int examEnrollmentId) throws SQLException {
        String sql = "SELECT SessionId FROM ExamEnrollment WHERE ExamEnrollmentId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examEnrollmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("SessionId");
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
        // sqlexception

    private Integer getExamEnrollmentIdForSession(int candidateId, int sessionId) throws SQLException {
    // snapshot from reg
        String sql = """
                SELECT ee.ExamEnrollmentId
                FROM ExamEnrollment ee
                WHERE ee.CandidateId = ? AND ee.SessionId = ?
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            ps.setInt(2, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ExamEnrollmentId");
                }
    // Tim user id by email
            }
        }
        return null;
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
                    s.sex = rs.getBoolean("Sex") ? "Nam" : "Nữ";
                    s.govId = rs.getString("GovernmentIdNumber");
                    s.address = rs.getString("Address");
                    return s;
                }
            }
    // read bit
        }
        throw new SQLException("Profile not found: " + profileId);
    }

    private PersonSnapshot snapshotFromReg(ExamRegistrationDTO reg) {
        PersonSnapshot s = new PersonSnapshot();
        s.fullName = reg.getFullName();
        if (reg.getDateOfBirth() != null) {
            s.dob = new java.sql.Timestamp(reg.getDateOfBirth().getTime());
        }
        s.phone = reg.getPhoneNo();
        s.email = reg.getEmail();
        s.sex = reg.getSex() != null && !reg.getSex().isBlank() ? reg.getSex().trim() : "Nam";
        s.govId = reg.getGovIdNo();
        s.address = reg.getAddress();
        return s;
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

    private static boolean readBit(ResultSet rs, String column) throws SQLException {
        boolean value = rs.getBoolean(column);
        if (rs.wasNull()) {
            return false;
        }
        return value;
    }

    private static void setNullableBoolean(PreparedStatement ps, int index, Boolean value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.BIT);
        } else {
            ps.setBoolean(index, value);
        }
    }

    private static boolean parseSexBit(String sex) {
        if (sex == null || sex.isBlank()) {
            return true;
        }
        String s = sex.trim().toLowerCase(Locale.ROOT);
        if ("1".equals(s) || "true".equals(s)) {
            return true;
        }
        if ("0".equals(s) || "false".equals(s)) {
            return false;
        }
        return !(s.equals("nữ") || s.equals("nu") || s.equals("female"));
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
        er.setExamSessionId(rs.getInt("examSessionId"));
        try {
            er.setExamEnrollmentId(rs.getInt("examEnrollmentId"));
        } catch (SQLException ignored) {
            er.setExamEnrollmentId(0);
        }
        er.setPersonId(rs.getInt("personId"));
        er.setCandidateNo(rs.getInt("candidateNo"));
        er.setRegistrationType(rs.getString("registrationType"));
        er.setIsPaymentCompleted(readBit(rs, "isPaymentCompleted"));
        er.setIsPresent(readBit(rs, "isPresent"));
        er.setPresentMarkedAt(rs.getTimestamp("presentMarkedAt"));
        er.setFullName(rs.getString("fullName"));
        er.setGovIdNo(rs.getString("govIdNo"));
        er.setDateOfBirth(rs.getDate("dateOfBirth"));
        er.setGender(readBit(rs, "gender"));
        er.setPhoneNo(rs.getString("phoneNo"));
        er.setEmail(rs.getString("email"));
        er.setPhotoUrl(rs.getString("photoUrl"));
        er.setLicenseCode(rs.getString("licenseCode"));
        er.setComputerCode(rs.getString("computerCode"));
        er.setAddress(rs.getString("address"));
        er.setReasonForTaking(rs.getString("reasonForTaking"));
        try {
            er.setTakeTheory(readNullableBoolean(rs, "takeTheory"));
            er.setTakePractical(readNullableBoolean(rs, "takePractical"));
        } catch (SQLException ignored) {
            er.setTakeTheory(null);
            er.setTakePractical(null);
        }
        er.setExamDate(rs.getDate("examDate"));
        try {
            er.setSectionStatus(rs.getString("sectionStatus"));
            er.setSignaturePrinted(readBit(rs, "signaturePrinted"));
        } catch (SQLException ignored) {
            er.setSectionStatus("Pending");
            er.setSignaturePrinted(false);
        }

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
    // Dong bo trang thai phan thi theo ca
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

    @Override
    public void syncSectionStatusesForSession(int sessionId) {
        try {
            String q = "SELECT CandidateId FROM ExamEnrollment WHERE SessionId = ?";
    // Lay section status
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
                Integer examEnrollmentId = getExamEnrollmentIdForSession(cid, sessionId);
                if (examEnrollmentId == null) {
    // Cap nhat section status
                    continue;
                }
                int scoreId = findExamScoreId(examEnrollmentId, sectionId);
                String currentStatus = getSectionStatus(cid, sessionId);
                if (scoreId > 0 && (currentStatus == null || "Pending".equalsIgnoreCase(currentStatus))) {
                    updateSectionStatus(cid, sessionId, "Done");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Tim section id for session
    @Override
    public boolean markSignaturePrinted(int candidateId, int sessionId) {
        String sql = """
                UPDATE ExamEnrollment
                SET SignaturePrinted = 1
                WHERE CandidateId = ? AND SessionId = ?
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
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
    // translate keyword
        String sql = """
                UPDATE ExamEnrollment
                SET SectionStatus = N'Done'
                WHERE CandidateId = ? AND SessionId = ?
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            ps.setInt(2, sessionId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    // Xac dinh section keyword for session
        return false;
    }

    private String getSectionStatus(int candidateId, int sessionId) throws SQLException {
        String sql = """
                SELECT SectionStatus
                FROM ExamEnrollment
                WHERE CandidateId = ? AND SessionId = ?
                """;
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
        String sql = """
                UPDATE ExamEnrollment
                SET SectionStatus = ?
                WHERE CandidateId = ? AND SessionId = ?
    // Kiem tra theory passed
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, candidateId);
            ps.setInt(3, sessionId);
            ps.executeUpdate();
        }
    }

    private Integer findSectionIdForSession(int sessionId, String sectionKeyword) throws SQLException {
        String sql = """
    // Chot diem va trang thai phan thi
                SELECT TOP 1 ses.ExamSectionId
                FROM Session_ExamSection ses
                JOIN ExamSection es ON es.ExamSectionId = ses.ExamSectionId
                WHERE ses.SessionId = ? AND (es.SectionName LIKE ? OR es.SectionName LIKE ?)
    // Dieu chinh so lan tru diem
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

    private String translateKeyword(String key) {
        if ("Theory".equalsIgnoreCase(key)) {
            return "Lý thuyết";
        }
        if ("Practical".equalsIgnoreCase(key)) {
            return "Sa hình";
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
                }
            }
        }
        return "Theory";
    }

    private String findLicenseClassByCandidate(int candidateId) throws SQLException {
        String sql = """
                SELECT TOP 1 l.LicenceClass
                FROM Candidate c
                JOIN ExamEnrollment ee ON ee.CandidateId = c.CandidateId
                JOIN [Session] s ON s.SessionId = ee.SessionId
                JOIN Exam ex ON ex.ExamId = s.ExamId
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

    private static final class SessionContext {
        int examId;
        int licenceId;
        String licenseCode;
    }

    private static final class PersonSnapshot {
        String fullName;
        Timestamp dob;
        String phone;
        String email;
        String sex;
        String govId;
        String address;
    }
}
