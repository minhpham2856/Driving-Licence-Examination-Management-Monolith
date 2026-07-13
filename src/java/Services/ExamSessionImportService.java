package Services;

import DBConnection.DBContext;
import DTOs.ExamRegistrationDTO;
import DTOs.ExamSessionImportDraft;
import Models.ExamSection;
import Utils.ExamConstants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Đối soát danh sách chính thức và tạo kỳ/phiên thi cùng danh sách thí sinh
 * trong một transaction. Import không đồng nghĩa với điểm danh nên trạng
 * thái hồ sơ vẫn là Approved và trạng thái phần thi ban đầu là Pending.
 */
public class ExamSessionImportService {

    private static final Set<String> ALLOWED_LICENCE_CLASSES = Set.of("A1", "A", "B1");

    public List<ExamSection> findSections() {
        String sql = """
                SELECT DISTINCT es.ExamSectionId, es.SectionName
                FROM ExamSection es
                JOIN Licence_ExamSection les ON les.ExamSectionId = es.ExamSectionId
                JOIN Licence l ON l.LicenceId = les.LicenceId
                WHERE l.LicenceClass IN ('A1', 'A', 'B1')
                ORDER BY es.ExamSectionId
                """;
        List<ExamSection> sections = new ArrayList<>();
        try (Connection connection = openConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                sections.add(new ExamSection(rs.getInt("ExamSectionId"), rs.getString("SectionName")));
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Không tải được danh sách phần thi.", ex);
        }
        return sections;
    }

    /**
     * Trả về null nếu dòng có thể được nhập; ngược lại trả về lý do từ chối.
     */
    public String validateApprovedCandidate(String governmentIdNumber, int licenceId, int candidateNo) {
        if (governmentIdNumber == null || governmentIdNumber.isBlank()) {
            return "Thiếu CCCD";
        }
        if (candidateNo <= 0) {
            return "SBD không hợp lệ";
        }

        String applicationSql = """
                SELECT TOP 1 er.RegistrationStatus
                FROM Profile p
                JOIN ExamRegistration er ON er.ProfileId = p.ProfileId
                WHERE p.GovernmentIdNumber = ? AND er.LicenceId = ?
                ORDER BY er.ExamRegistrationId DESC
                """;
        String candidateSql = """
                SELECT TOP 1 CandidateId, CandidateNumber, GovernmentIdNumber
                FROM Candidate
                WHERE GovernmentIdNumber = ? OR CandidateNumber = ?
                """;
        String candidateNumber = ExamConstants.buildCandidateNumber(null, candidateNo);

        try (Connection connection = openConnection()) {
            String status = null;
            try (PreparedStatement ps = connection.prepareStatement(applicationSql)) {
                ps.setString(1, governmentIdNumber);
                ps.setInt(2, licenceId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        status = rs.getString("RegistrationStatus");
                    }
                }
            }
            if (status == null) {
                return "Không tìm thấy hồ sơ đăng ký đúng hạng";
            }
            if (!"Approved".equalsIgnoreCase(status)) {
                return "Hồ sơ chưa được duyệt (" + status + ")";
            }

            try (PreparedStatement ps = connection.prepareStatement(candidateSql)) {
                ps.setString(1, governmentIdNumber);
                ps.setString(2, candidateNumber);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String existingGovId = rs.getString("GovernmentIdNumber");
                        if (governmentIdNumber.equals(existingGovId)) {
                            return "Thí sinh đã được xếp vào kỳ/phiên thi khác";
                        }
                        return "SBD đã được sử dụng bởi thí sinh khác";
                    }
                }
            }
            return null;
        } catch (SQLException ex) {
            throw new IllegalStateException("Không thể đối soát hồ sơ thí sinh.", ex);
        }
    }

    /**
     * Đối soát cả tệp trên cùng một kết nối để không mở một kết nối mới cho
     * từng thí sinh. Danh sách kết quả có cùng thứ tự với danh sách đầu vào.
     */
    public List<String> validateApprovedCandidates(List<ExamRegistrationDTO> candidates, int licenceId) {
        List<String> results = new ArrayList<>();
        if (candidates == null || candidates.isEmpty()) {
            return results;
        }

        String applicationSql = """
                SELECT TOP 1 er.RegistrationStatus
                FROM Profile p
                JOIN ExamRegistration er ON er.ProfileId = p.ProfileId
                WHERE p.GovernmentIdNumber = ? AND er.LicenceId = ?
                ORDER BY er.ExamRegistrationId DESC
                """;
        String candidateSql = """
                SELECT TOP 1 CandidateId, CandidateNumber, GovernmentIdNumber
                FROM Candidate
                WHERE GovernmentIdNumber = ? OR CandidateNumber = ?
                """;

        try (Connection connection = openConnection();
             PreparedStatement applicationPs = connection.prepareStatement(applicationSql);
             PreparedStatement candidatePs = connection.prepareStatement(candidateSql)) {
            for (ExamRegistrationDTO candidate : candidates) {
                if (candidate.isInvalid()) {
                    results.add(null);
                    continue;
                }

                String status = null;
                applicationPs.setString(1, candidate.getGovIdNo());
                applicationPs.setInt(2, licenceId);
                try (ResultSet rs = applicationPs.executeQuery()) {
                    if (rs.next()) {
                        status = rs.getString("RegistrationStatus");
                    }
                }
                if (status == null) {
                    results.add("Không tìm thấy hồ sơ đăng ký đúng hạng");
                    continue;
                }
                if (!"Approved".equalsIgnoreCase(status)) {
                    results.add("Hồ sơ chưa được duyệt (" + status + ")");
                    continue;
                }

                String candidateNumber = ExamConstants.buildCandidateNumber(null, candidate.getCandidateNo());
                candidatePs.setString(1, candidate.getGovIdNo());
                candidatePs.setString(2, candidateNumber);
                try (ResultSet rs = candidatePs.executeQuery()) {
                    if (rs.next()) {
                        String existingGovId = rs.getString("GovernmentIdNumber");
                        results.add(candidate.getGovIdNo().equals(existingGovId)
                                ? "Thí sinh đã được xếp vào kỳ/phiên thi khác"
                                : "SBD đã được sử dụng bởi thí sinh khác");
                    } else {
                        results.add(null);
                    }
                }
            }
            return results;
        } catch (SQLException ex) {
            throw new IllegalStateException("Không thể đối soát hồ sơ thí sinh.", ex);
        }
    }

    public ImportResult createSessionWithCandidates(ExamSessionImportDraft draft,
                                                     List<ExamRegistrationDTO> candidates) {
        if (draft == null || candidates == null || candidates.isEmpty()) {
            throw new IllegalArgumentException("Không có dữ liệu phiên thi hoặc danh sách thí sinh để lưu.");
        }

        Connection connection = null;
        try {
            connection = openConnection();
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

            validateDraft(connection, draft, candidates.size());
            int examId = insertExam(connection, draft);
            int sessionId = insertSession(connection, draft, examId);
            insertAreaAndSection(connection, draft, sessionId);

            int imported = 0;
            for (ExamRegistrationDTO candidate : candidates) {
                insertApprovedCandidate(connection, draft, sessionId, examId, candidate);
                imported++;
            }

            connection.commit();
            return new ImportResult(examId, sessionId, imported);
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

    private void validateDraft(Connection connection, ExamSessionImportDraft draft, int candidateCount)
            throws SQLException {
        if (draft.getStartTime() == null || draft.getEndTime() == null
                || !draft.getEndTime().after(draft.getStartTime())) {
            throw new IllegalArgumentException("Thời gian phiên thi không hợp lệ.");
        }

        String licenceSql = "SELECT LicenceClass FROM Licence WHERE LicenceId = ?";
        String licenceClass = null;
        try (PreparedStatement ps = connection.prepareStatement(licenceSql)) {
            ps.setInt(1, draft.getLicenceId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    licenceClass = rs.getString(1);
                }
            }
        }
        if (licenceClass == null || !ALLOWED_LICENCE_CLASSES.contains(licenceClass.toUpperCase())) {
            throw new IllegalArgumentException("Chỉ hỗ trợ hạng A1, A và B1.");
        }

        String areaSql = "SELECT Capacity FROM ExamArea WHERE ExamAreaId = ?";
        int capacity = 0;
        try (PreparedStatement ps = connection.prepareStatement(areaSql)) {
            ps.setInt(1, draft.getExamAreaId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    capacity = rs.getInt(1);
                }
            }
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("Khu vực/phòng thi không tồn tại.");
        }
        if (candidateCount > capacity) {
            throw new IllegalArgumentException("Danh sách có " + candidateCount
                    + " thí sinh, vượt sức chứa " + capacity + " của khu vực thi.");
        }

        String sectionSql = """
                SELECT COUNT(*)
                FROM Licence_ExamSection
                WHERE LicenceId = ? AND ExamSectionId = ?
                """;
        try (PreparedStatement ps = connection.prepareStatement(sectionSql)) {
            ps.setInt(1, draft.getLicenceId());
            ps.setInt(2, draft.getExamSectionId());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || rs.getInt(1) == 0) {
                    throw new IllegalArgumentException("Phần thi không áp dụng cho hạng " + licenceClass + ".");
                }
            }
        }

        String overlapSql = """
                SELECT COUNT(*)
                FROM [Session] s
                JOIN Session_ExamArea sea ON sea.SessionId = s.SessionId
                WHERE sea.ExamAreaId = ?
                  AND s.[Status] <> 'Cancelled'
                  AND ? < s.EndTime AND ? > s.StartTime
                """;
        try (PreparedStatement ps = connection.prepareStatement(overlapSql)) {
            ps.setInt(1, draft.getExamAreaId());
            ps.setTimestamp(2, draft.getStartTime());
            ps.setTimestamp(3, draft.getEndTime());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new IllegalArgumentException("Khu vực/phòng thi đã có phiên khác trong khoảng thời gian này.");
                }
            }
        }
    }

    private int insertExam(Connection connection, ExamSessionImportDraft draft) throws SQLException {
        String code = buildExamCode(connection, draft.getLicenceClass(), draft.getStartTime());
        String sql = """
                INSERT INTO Exam (ExamCode, ExamDate, CentreName, [Status], LicenceId)
                VALUES (?, ?, ?, 'Scheduled', ?)
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, code);
            ps.setTimestamp(2, draft.getStartTime());
            ps.setString(3, draft.getCentreName());
            ps.setInt(4, draft.getLicenceId());
            ps.executeUpdate();
            return readGeneratedId(ps, "Không lấy được mã kỳ thi vừa tạo.");
        }
    }

    private int insertSession(Connection connection, ExamSessionImportDraft draft, int examId)
            throws SQLException {
        String sql = """
                INSERT INTO [Session] (SessionName, StartTime, EndTime, [Status], ExamId)
                VALUES (?, ?, ?, 'Scheduled', ?)
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, draft.getSessionName());
            ps.setTimestamp(2, draft.getStartTime());
            ps.setTimestamp(3, draft.getEndTime());
            ps.setInt(4, examId);
            ps.executeUpdate();
            return readGeneratedId(ps, "Không lấy được mã phiên thi vừa tạo.");
        }
    }

    private void insertAreaAndSection(Connection connection, ExamSessionImportDraft draft, int sessionId)
            throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO Session_ExamArea (SessionId, ExamAreaId) VALUES (?, ?)")) {
            ps.setInt(1, sessionId);
            ps.setInt(2, draft.getExamAreaId());
            ps.executeUpdate();
        }
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO Session_ExamSection (SessionId, ExamSectionId) VALUES (?, ?)")) {
            ps.setInt(1, sessionId);
            ps.setInt(2, draft.getExamSectionId());
            ps.executeUpdate();
        }
    }

    private void insertApprovedCandidate(Connection connection, ExamSessionImportDraft draft,
                                         int sessionId, int examId, ExamRegistrationDTO row)
            throws SQLException {
        String applicationSql = """
                SELECT TOP 1 er.ExamRegistrationId, p.UserId, p.FullName, p.DateOfBirth,
                       p.PhoneNumber, p.Sex, p.GovernmentIdNumber, p.Address
                FROM Profile p
                JOIN ExamRegistration er ON er.ProfileId = p.ProfileId
                WHERE p.GovernmentIdNumber = ?
                  AND er.LicenceId = ?
                  AND er.RegistrationStatus = 'Approved'
                ORDER BY er.ExamRegistrationId DESC
                """;

        try (PreparedStatement ps = connection.prepareStatement(applicationSql)) {
            ps.setString(1, row.getGovIdNo());
            ps.setInt(2, draft.getLicenceId());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("Hồ sơ CCCD " + row.getGovIdNo()
                            + " không còn ở trạng thái Approved.");
                }
                ensureCandidateIsAvailable(connection, row.getGovIdNo(), row.getCandidateNo());

                String candidateSql = """
                        INSERT INTO Candidate (CandidateNumber, FullName, DateOfBirth, PhoneNumber, Sex,
                            GovernmentIdNumber, Address, UserId, ExamRegistrationId)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """;
                int candidateId;
                try (PreparedStatement candidatePs = connection.prepareStatement(
                        candidateSql, Statement.RETURN_GENERATED_KEYS)) {
                    candidatePs.setString(1, ExamConstants.buildCandidateNumber(
                            draft.getLicenceClass(), row.getCandidateNo()));
                    candidatePs.setString(2, rs.getString("FullName"));
                    candidatePs.setTimestamp(3, rs.getTimestamp("DateOfBirth"));
                    candidatePs.setString(4, rs.getString("PhoneNumber"));
                    candidatePs.setString(5, rs.getString("Sex"));
                    candidatePs.setString(6, rs.getString("GovernmentIdNumber"));
                    candidatePs.setString(7, rs.getString("Address"));
                    candidatePs.setInt(8, rs.getInt("UserId"));
                    candidatePs.setInt(9, rs.getInt("ExamRegistrationId"));
                    candidatePs.executeUpdate();
                    candidateId = readGeneratedId(candidatePs, "Không tạo được thí sinh " + row.getGovIdNo());
                }

                String enrollmentSql = """
                        INSERT INTO Exam_Candidate
                            (ExamId, CandidateId, SessionId, SectionStatus, SignaturePrinted)
                        VALUES (?, ?, ?, ?, 0)
                        """;
                try (PreparedStatement enrollmentPs = connection.prepareStatement(enrollmentSql)) {
                    enrollmentPs.setInt(1, examId);
                    enrollmentPs.setInt(2, candidateId);
                    enrollmentPs.setInt(3, sessionId);
                    enrollmentPs.setString(4, ExamConstants.CANDIDATE_PENDING);
                    enrollmentPs.executeUpdate();
                }
            }
        }
    }

    private void ensureCandidateIsAvailable(Connection connection, String governmentIdNumber, int candidateNo)
            throws SQLException {
        String sql = """
                SELECT TOP 1 GovernmentIdNumber
                FROM Candidate
                WHERE GovernmentIdNumber = ? OR CandidateNumber = ?
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, governmentIdNumber);
            ps.setString(2, ExamConstants.buildCandidateNumber(null, candidateNo));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String existingGovId = rs.getString(1);
                    if (governmentIdNumber.equals(existingGovId)) {
                        throw new IllegalArgumentException("Thí sinh CCCD " + governmentIdNumber
                                + " đã thuộc kỳ/phiên thi khác.");
                    }
                    throw new IllegalArgumentException("SBD " + candidateNo + " đã được sử dụng.");
                }
            }
        }
    }

    private String buildExamCode(Connection connection, String licenceClass, Timestamp startTime)
            throws SQLException {
        String datePart = startTime.toLocalDateTime().toLocalDate().toString().replace("-", "");
        String prefix = "EX-" + licenceClass.toUpperCase() + "-" + datePart;
        String sql = "SELECT COUNT(*) FROM Exam WHERE ExamCode LIKE ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, prefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                int count = rs.next() ? rs.getInt(1) : 0;
                return count == 0 ? prefix : prefix + "-" + (count + 1);
            }
        }
    }

    private int readGeneratedId(PreparedStatement ps, String error) throws SQLException {
        try (ResultSet keys = ps.getGeneratedKeys()) {
            if (keys.next()) {
                return keys.getInt(1);
            }
        }
        throw new SQLException(error);
    }

    private Connection openConnection() throws SQLException {
        Connection connection = new DBContext().getConnection();
        if (connection == null) {
            throw new SQLException("Không kết nối được cơ sở dữ liệu.");
        }
        return connection;
    }

    private void rollbackQuietly(Connection connection) {
        if (connection != null) {
            try {
                connection.rollback();
            } catch (SQLException ignored) {
            }
        }
    }

    private void closeQuietly(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
            }
        }
    }

    private String readableMessage(SQLException ex) {
        String message = ex.getMessage();
        if (message != null && (message.contains("UQ") || message.toLowerCase().contains("duplicate"))) {
            return "Dữ liệu bị trùng SBD, CCCD hoặc liên kết phiên thi.";
        }
        return "Không thể tạo phiên thi và lưu danh sách. Toàn bộ giao dịch đã được hoàn tác.";
    }

    public static class ImportResult {
        private final int examId;
        private final int sessionId;
        private final int importedCount;

        public ImportResult(int examId, int sessionId, int importedCount) {
            this.examId = examId;
            this.sessionId = sessionId;
            this.importedCount = importedCount;
        }

        public int getExamId() {
            return examId;
        }

        public int getSessionId() {
            return sessionId;
        }

        public int getImportedCount() {
            return importedCount;
        }
    }
}
