package managingstaff.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import managingstaff.dto.ExamRegistrationDTO;
import managingstaff.dto.ExamSessionImportDraft;
import shared.dbconnection.DBContext;

/**
 * Creates one Exam and its official candidate roster using the mainTest schema.
 * The whole operation is atomic: Exam, ExamSection, Candidate and
 * ExamEnrollment rows are either all committed or all rolled back.
 */
public class ExamSessionImportService {

    private static final Set<String> ALLOWED_LICENCES = Set.of("A1", "A", "B1");
    private static final Set<String> ALLOWED_SECTIONS = Set.of(
            "Lý thuyết", "Thực hành trong hình");

    public List<String> validateApprovedCandidates(
            List<ExamRegistrationDTO> candidates, int licenceId) {
        List<String> results = new ArrayList<>();
        if (candidates == null || candidates.isEmpty()) return results;

        String registrationSql = """
                SELECT TOP 1 er.RegistrationStatus
                FROM Profile p
                JOIN ExamRegistration er ON er.ProfileId=p.ProfileId
                WHERE p.GovernmentIdNumber=?
                ORDER BY er.ExamRegistrationId DESC
                """;
        String duplicateSql = """
                SELECT TOP 1 GovernmentIdNumber, CandidateNumber
                FROM Candidate
                WHERE GovernmentIdNumber=? OR CandidateNumber=?
                """;

        try (Connection connection = openConnection();
             PreparedStatement registrationPs = connection.prepareStatement(registrationSql);
             PreparedStatement duplicatePs = connection.prepareStatement(duplicateSql)) {
            for (ExamRegistrationDTO candidate : candidates) {
                if (candidate.isInvalid()) {
                    results.add(null);
                    continue;
                }

                registrationPs.setString(1, candidate.getGovIdNo());
                String status = null;
                try (ResultSet rs = registrationPs.executeQuery()) {
                    if (rs.next()) status = rs.getString(1);
                }
                if (status == null) {
                    results.add("CCCD không thuộc hồ sơ đã duyệt trong hệ thống");
                    continue;
                }
                if (!isApprovedStatus(status)) {
                    results.add("Hồ sơ chưa được duyệt (" + status + ")");
                    continue;
                }

                duplicatePs.setString(1, candidate.getGovIdNo());
                duplicatePs.setString(2, candidateNumber(candidate.getCandidateNo()));
                try (ResultSet rs = duplicatePs.executeQuery()) {
                    if (!rs.next()) {
                        results.add(null);
                    } else if (candidate.getGovIdNo().equals(rs.getString("GovernmentIdNumber"))) {
                        results.add("Thí sinh đã được xếp vào kỳ thi khác");
                    } else {
                        results.add("SBD đã được sử dụng bởi thí sinh khác");
                    }
                }
            }
            return results;
        } catch (SQLException ex) {
            throw new IllegalStateException("Không thể đối soát hồ sơ thí sinh.", ex);
        }
    }

    public ImportResult createSessionWithCandidates(
            ExamSessionImportDraft draft, List<ExamRegistrationDTO> candidates) {
        if (draft == null || candidates == null || candidates.isEmpty()) {
            throw new IllegalArgumentException(
                    "Không có dữ liệu kỳ thi hoặc danh sách thí sinh để lưu.");
        }

        Connection connection = null;
        try {
            connection = openConnection();
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

            validateDraft(connection, draft, candidates.size());
            int examId = insertExam(connection, draft);
            int examSectionId = insertExamSection(connection, draft, examId);

            int imported = 0;
            for (ExamRegistrationDTO row : candidates) {
                insertApprovedCandidate(connection, draft, examId, examSectionId, row);
                imported++;
            }

            connection.commit();
            return new ImportResult(examId, examId, imported);
        } catch (SQLException ex) {
            rollbackQuietly(connection);
            throw new IllegalStateException(readableMessage(ex), ex);
        } catch (RuntimeException ex) {
            rollbackQuietly(connection);
            throw ex;
        } finally {
            closeQuietly(connection);
        }
    }

    public ImportResult importCandidatesIntoSession(
            ExamSessionImportDraft draft, List<ExamRegistrationDTO> candidates) {
        if (draft == null || draft.getExamId() <= 0 || candidates == null || candidates.isEmpty()) {
            throw new IllegalArgumentException("Chưa chọn phiên thi hoặc danh sách thí sinh.");
        }
        Connection connection = null;
        try {
            connection = openConnection();
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            int sectionId = 0;
            String sql = """
                    SELECT TOP 1 es.ExamSectionId
                    FROM Exam e JOIN ExamSection es ON es.ExamId=e.ExamId
                    WHERE e.ExamId=? AND CAST(e.ExamDate AS date)>CAST(GETDATE() AS date)
                      AND e.[Status] IN (N'Chưa diễn ra','Scheduled','Open',N'Mở')
                    ORDER BY es.ExamSectionId
                    """;
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, draft.getExamId());
                try (ResultSet rs = ps.executeQuery()) { if (rs.next()) sectionId = rs.getInt(1); }
            }
            if (sectionId <= 0) throw new IllegalArgumentException("Phiên thi đã bắt đầu hoặc không còn cho phép import.");
            int imported = 0;
            for (ExamRegistrationDTO row : candidates) {
                insertApprovedCandidate(connection, draft, draft.getExamId(), sectionId, row);
                imported++;
            }
            connection.commit();
            return new ImportResult(draft.getExamId(), draft.getExamId(), imported);
        } catch (SQLException ex) {
            rollbackQuietly(connection); throw new IllegalStateException(readableMessage(ex), ex);
        } catch (RuntimeException ex) {
            rollbackQuietly(connection); throw ex;
        } finally { closeQuietly(connection); }
    }

    private void validateDraft(Connection connection, ExamSessionImportDraft draft,
            int candidateCount) throws SQLException {
        if (draft.getStartTime() == null || draft.getEndTime() == null
                || !draft.getEndTime().after(draft.getStartTime())) {
            throw new IllegalArgumentException("Thời gian kỳ thi không hợp lệ.");
        }
        String licence = null;
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT LicenceClass FROM Licence WHERE LicenceId=?")) {
            ps.setInt(1, draft.getLicenceId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) licence = rs.getString(1);
            }
        }
        if (licence == null || !ALLOWED_LICENCES.contains(
                licence.toUpperCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Chỉ hỗ trợ hạng A1, A và B1.");
        }
        if (!ALLOWED_SECTIONS.contains(draft.getSectionType())) {
            throw new IllegalArgumentException("Phần thi không hợp lệ.");
        }

    }

    private int insertExam(Connection connection, ExamSessionImportDraft draft)
            throws SQLException {
        String code = buildExamCode(connection, draft.getLicenceClass(), draft.getStartTime());
        String sql = """
                INSERT INTO Exam
                    (ExamCode, ExamDate, StartTime, EndTime, [Status], CentreName, LicenceId)
                VALUES (?, ?, ?, ?, N'Chưa diễn ra', ?, ?)
                """;
        try (PreparedStatement ps = connection.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, code);
            ps.setTimestamp(2, draft.getStartTime());
            ps.setTimestamp(3, draft.getStartTime());
            ps.setTimestamp(4, draft.getEndTime());
            ps.setString(5, draft.getCentreName());
            ps.setInt(6, draft.getLicenceId());
            ps.executeUpdate();
            return generatedId(ps, "Không lấy được mã kỳ thi vừa tạo.");
        }
    }

    private int insertExamSection(Connection connection, ExamSessionImportDraft draft,
            int examId) throws SQLException {
        int duration = Math.max(1, (int) Duration.between(
                draft.getStartTime().toInstant(), draft.getEndTime().toInstant()).toMinutes());
        String sql = """
                INSERT INTO ExamSection (SectionType, LicenceId, DurationMinutes, ExamId)
                VALUES (?, ?, ?, ?)
                """;
        try (PreparedStatement ps = connection.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, draft.getSectionType());
            ps.setInt(2, draft.getLicenceId());
            ps.setInt(3, duration);
            ps.setInt(4, examId);
            ps.executeUpdate();
            return generatedId(ps, "Không lấy được mã phần thi vừa tạo.");
        }
    }

    private void insertApprovedCandidate(Connection connection, ExamSessionImportDraft draft,
            int examId, int examSectionId, ExamRegistrationDTO row) throws SQLException {
        String applicationSql = """
                SELECT TOP 1 er.ExamRegistrationId,p.FullName, p.DateOfBirth, p.PhoneNumber, p.Sex,
                       p.GovernmentIdNumber, p.Address, u.Email,
                       portrait.DocumentUrl AS PhotoImageUrl
                FROM Profile p
                JOIN [User] u ON u.UserId=p.UserId
                JOIN ExamRegistration er ON er.ProfileId=p.ProfileId
                OUTER APPLY (
                    SELECT TOP 1 d.DocumentUrl
                    FROM Document d JOIN DocumentType dt ON dt.DocumentTypeId=d.DocumentTypeId
                    WHERE d.ProfileId=p.ProfileId
                      AND (UPPER(dt.[Type]) LIKE '%PORTRAIT%' OR dt.[Type] LIKE N'%chân dung%')
                    ORDER BY d.DocumentId DESC
                ) portrait
                WHERE p.GovernmentIdNumber=?
                  AND er.RegistrationStatus IN ('Approved','WaitingExam',N'Duyệt',N'Đã duyệt')
                ORDER BY er.ExamRegistrationId DESC
                """;
        try (PreparedStatement ps = connection.prepareStatement(applicationSql)) {
            ps.setString(1, row.getGovIdNo());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("Hồ sơ CCCD " + row.getGovIdNo()
                            + " không còn ở trạng thái Approved.");
                }
                ensureCandidateIsAvailable(connection, row.getGovIdNo(), row.getCandidateNo());

                int candidateId;
                String candidateSql = """
                        INSERT INTO Candidate
                          (CandidateNumber, FullName, DateOfBirth, PhoneNumber, Email, Sex,
                           GovernmentIdNumber, Address, TakeTheory, TakeLayout, TakeNo,
                           ReasonForTaking, PhotoImageUrl)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1, ?, ?)
                        """;
                try (PreparedStatement candidatePs = connection.prepareStatement(
                        candidateSql, Statement.RETURN_GENERATED_KEYS)) {
                    candidatePs.setString(1, candidateNumber(row.getCandidateNo()));
                    candidatePs.setString(2, rs.getString("FullName"));
                    candidatePs.setTimestamp(3, rs.getTimestamp("DateOfBirth"));
                    candidatePs.setString(4, rs.getString("PhoneNumber"));
                    candidatePs.setString(5, rs.getString("Email"));
                    candidatePs.setBoolean(6, rs.getBoolean("Sex"));
                    candidatePs.setString(7, rs.getString("GovernmentIdNumber"));
                    candidatePs.setString(8, rs.getString("Address"));
                    candidatePs.setBoolean(9, "Lý thuyết".equals(draft.getSectionType()));
                    candidatePs.setBoolean(10, "Thực hành trong hình".equals(draft.getSectionType()));
                    candidatePs.setString(11, "Thi sát hạch hạng " + draft.getLicenceClass());
                    candidatePs.setString(12, rs.getString("PhotoImageUrl"));
                    candidatePs.executeUpdate();
                    candidateId = generatedId(candidatePs,
                            "Không tạo được thí sinh " + row.getGovIdNo());
                }

                int enrollmentId;
                try (PreparedStatement enrollmentPs = connection.prepareStatement(
                        "INSERT INTO ExamEnrollment (CandidateId,ExamId) VALUES (?,?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    enrollmentPs.setInt(1, candidateId);
                    enrollmentPs.setInt(2, examId);
                    enrollmentPs.executeUpdate();
                    enrollmentId = generatedId(enrollmentPs,
                            "Không ghi danh được thí sinh " + row.getGovIdNo());
                }

                try (PreparedStatement sectionPs = connection.prepareStatement(
                        "INSERT INTO ExamEnrollmentSection"
                        + " (ExamEnrollmentId,ExamSectionId,[Status])"
                        + " VALUES (?,?,'Pending')")) {
                    sectionPs.setInt(1, enrollmentId);
                    sectionPs.setInt(2, examSectionId);
                    sectionPs.executeUpdate();
                }
                try (PreparedStatement statusPs = connection.prepareStatement("""
                        UPDATE ExamRegistration
                        SET LicenceId=?,RegistrationStatus='OfficialScheduled'
                        WHERE ExamRegistrationId=?
                        """)) {
                    statusPs.setInt(1, draft.getLicenceId());
                    statusPs.setInt(2, rs.getInt("ExamRegistrationId"));
                    statusPs.executeUpdate();
                }
            }
        }
    }

    private void ensureCandidateIsAvailable(Connection connection, String govId, int number)
            throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT TOP 1 GovernmentIdNumber FROM Candidate"
                + " WHERE GovernmentIdNumber=? OR CandidateNumber=?")) {
            ps.setString(1, govId);
            ps.setString(2, candidateNumber(number));
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return;
                if (govId.equals(rs.getString(1))) {
                    throw new IllegalArgumentException(
                            "Thí sinh CCCD " + govId + " đã thuộc kỳ thi khác.");
                }
                throw new IllegalArgumentException("SBD " + number + " đã được sử dụng.");
            }
        }
    }

    private String buildExamCode(Connection connection, String licence, Timestamp start)
            throws SQLException {
        String date = start.toLocalDateTime().toLocalDate().toString().replace("-", "");
        String time = start.toLocalDateTime().toLocalTime().toString().substring(0, 5).replace(":", "");
        String prefix = licence.toUpperCase(Locale.ROOT) + "-" + date + "-" + time;
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT COUNT(*) FROM Exam WHERE ExamCode LIKE ?")) {
            ps.setString(1, prefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                int count = rs.next() ? rs.getInt(1) : 0;
                return count == 0 ? prefix : prefix + "-" + (count + 1);
            }
        }
    }

    private static String candidateNumber(int number) {
        return number < 1000 ? String.format("%03d", number) : String.valueOf(number);
    }

    private static boolean isApprovedStatus(String status) {
        if (status == null) return false;
        String normalized = status.trim();
        return "Approved".equalsIgnoreCase(normalized)
                || "WaitingExam".equalsIgnoreCase(normalized)
                || "Duyệt".equalsIgnoreCase(normalized)
                || "Đã duyệt".equalsIgnoreCase(normalized);
    }

    private int generatedId(PreparedStatement ps, String error) throws SQLException {
        try (ResultSet keys = ps.getGeneratedKeys()) {
            if (keys.next()) return keys.getInt(1);
        }
        throw new SQLException(error);
    }

    private Connection openConnection() throws SQLException {
        Connection connection = new DBContext().getConnection();
        if (connection == null) throw new SQLException("Không kết nối được database.");
        return connection;
    }

    private static void rollbackQuietly(Connection connection) {
        if (connection == null) return;
        try { connection.rollback(); } catch (SQLException ignored) {}
    }

    private static void closeQuietly(Connection connection) {
        if (connection == null) return;
        try { connection.setAutoCommit(true); } catch (SQLException ignored) {}
        try { connection.close(); } catch (SQLException ignored) {}
    }

    private String readableMessage(SQLException ex) {
        String message = ex.getMessage();
        if (message != null && (message.contains("UNIQUE") || message.contains("duplicate"))) {
            return "Dữ liệu kỳ thi hoặc thí sinh bị trùng. Hệ thống đã hủy toàn bộ giao dịch.";
        }
        return "Không thể tạo kỳ thi và danh sách thí sinh. Hệ thống đã hủy toàn bộ giao dịch.";
    }

    public static final class ImportResult {
        private final int examId;
        private final int sessionId;
        private final int importedCount;

        public ImportResult(int examId, int sessionId, int importedCount) {
            this.examId = examId;
            this.sessionId = sessionId;
            this.importedCount = importedCount;
        }
        public int getExamId() { return examId; }
        public int getSessionId() { return sessionId; }
        public int getImportedCount() { return importedCount; }
    }
}
