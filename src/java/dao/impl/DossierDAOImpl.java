package dao.impl;
import dao.DossierDAO;
import dbconnection.DBContext;
import enums.DocumentNote;
import enums.RegistrationStatus;
import java.util.Locale;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
public class DossierDAOImpl extends DBContext implements DossierDAO {
    @Override
    public int ensureRegistration(int profileId, String licenceClass, String source, String applicantType) {
        String normalizedClass = normalizeManagedLicence(licenceClass);
        if (normalizedClass.isEmpty()) {
            return 0;
        }
        try {
            int licenceId = findLicenceId(normalizedClass);
            if (licenceId <= 0) {
                return 0;
            }
            String check = """
                    SELECT TOP 1 ExamRegistrationId
                    FROM ExamRegistration
                    WHERE ProfileId = ? AND LicenceId = ?
                    ORDER BY ExamRegistrationId DESC
                    """;
            try (PreparedStatement ps = getConnection().prepareStatement(check)) {
                ps.setInt(1, profileId);
                ps.setInt(2, licenceId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
            String notes = "SOURCE=" + source + ";APPLICANT_TYPE=" + applicantType;
            String insert = """
                    INSERT INTO ExamRegistration (RegistrationStatus, Notes, ProfileId, LicenceId)
                    VALUES (?, ?, ?, ?)
                    """;
            try (PreparedStatement ps = getConnection().prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, RegistrationStatus.BAN_NHAP.getDisplayName());
                ps.setString(2, notes);
                ps.setInt(3, profileId);
                ps.setInt(4, licenceId);
                if (ps.executeUpdate() == 0) {
                    return 0;
                }
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    return keys.next() ? keys.getInt(1) : 0;
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tạo hồ sơ đăng ký", e);
        }
    }
    private int findLicenceId(String licenceClass) throws SQLException {
        String sql = "SELECT LicenceId FROM Licence WHERE LicenceClass = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, licenceClass);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
    @Override
    public boolean saveDocument(int profileId, String documentType, String documentUrl) {
        String uploadedNote = DocumentNote.DA_TAI_LEN.getDisplayName();
        String sql = """
                IF EXISTS (SELECT 1 FROM Document WHERE ProfileId = ? AND DocumentType = ?)
                    UPDATE Document SET DocumentUrl = ?, Notes = ?
                    WHERE ProfileId = ? AND DocumentType = ?
                ELSE
                    INSERT INTO Document (DocumentType, DocumentUrl, Notes, ProfileId)
                    VALUES (?, ?, ?, ?)
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, profileId);
            ps.setString(2, documentType);
            ps.setString(3, documentUrl);
            ps.setString(4, uploadedNote);
            ps.setInt(5, profileId);
            ps.setString(6, documentType);
            ps.setString(7, documentType);
            ps.setString(8, documentUrl);
            ps.setString(9, uploadedNote);
            ps.setInt(10, profileId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể lưu tài liệu", e);
        }
    }
    @Override
    public boolean updateStatus(int registrationId, String status, String message, int actorUserId) {
        String sql = """
                UPDATE ExamRegistration
                SET RegistrationStatus = ?,
                    Notes = CONCAT(
                        CASE WHEN Notes IS NULL OR Notes = '' THEN '' ELSE Notes + ';' END,
                        'MESSAGE=', ?, ';REVIEWED_BY=', ?
                    )
                WHERE ExamRegistrationId = ?
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, message == null ? "" : message.replace(";", ","));
            ps.setInt(3, actorUserId);
            ps.setInt(4, registrationId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể cập nhật trạng thái hồ sơ", e);
        }
    }

    private static String normalizeManagedLicence(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        String lc = raw.trim().toUpperCase(Locale.ROOT);
        return switch (lc) {
            case "A2" -> "A";
            case "B", "B2" -> "B1";
            case "A1", "A", "B1" -> lc;
            default -> "";
        };
    }
}
