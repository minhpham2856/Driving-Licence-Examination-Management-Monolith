package DAOs.Impl;

import DAOs.DossierDAO;
import DBConnection.DBContext;
import DTOs.DossierDTO;
import Models.Document;
import Models.Profile;
import Models.User;
import Utils.ExamConstants;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DossierDAOImpl extends DBContext implements DossierDAO {

    private static final String DOSSIER_SELECT = """
            SELECT u.UserId, u.Username, u.Email, u.PasswordHash, u.[Role], u.[Status],
                   p.ProfileId, p.FullName, p.DateOfBirth, p.PhoneNumber, p.Sex,
                   p.GovernmentIdNumber, p.Address,
                   er.ExamRegistrationId, er.RegistrationStatus, er.Notes,
                   l.LicenceClass
            FROM [User] u
            JOIN Profile p ON p.UserId = u.UserId
            LEFT JOIN ExamRegistration er ON er.ExamRegistrationId = (
                SELECT TOP 1 er2.ExamRegistrationId
                FROM ExamRegistration er2
                WHERE er2.ProfileId = p.ProfileId
                ORDER BY er2.ExamRegistrationId DESC
            )
            LEFT JOIN Licence l ON l.LicenceId = er.LicenceId
            """;

    @Override
    public DossierDTO findByUserId(int userId) {
        return findOne(DOSSIER_SELECT + " WHERE u.UserId = ?", userId);
    }

    @Override
    public DossierDTO findByRegistrationId(int registrationId) {
        return findOne(DOSSIER_SELECT + " WHERE er.ExamRegistrationId = ?", registrationId);
    }

    @Override
    public List<DossierDTO> findAllRegistrants() {
        List<DossierDTO> dossiers = new ArrayList<>();
        String sql = DOSSIER_SELECT
                + " WHERE u.[Role] = 'Registrant'"
                + " ORDER BY p.FullName, u.UserId";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                DossierDTO dossier = mapDossier(rs);
                loadDocuments(dossier);
                dossiers.add(dossier);
            }
            return dossiers;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tải danh sách thí sinh", e);
        }
    }

    private DossierDTO findOne(String sql, int id) {
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                DossierDTO dossier = mapDossier(rs);
                loadDocuments(dossier);
                return dossier;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tải hồ sơ", e);
        }
    }

    @Override
    public List<DossierDTO> findSubmitted() {
        List<DossierDTO> dossiers = new ArrayList<>();
        String sql = DOSSIER_SELECT
                + " WHERE er.RegistrationStatus IN "
                + "('Draft','Pending','Submitted','NeedSupplement','Rejected')"
                + " ORDER BY CASE er.RegistrationStatus"
                + " WHEN 'Submitted' THEN 1"
                + " WHEN 'Pending' THEN 2"
                + " WHEN 'NeedSupplement' THEN 3"
                + " WHEN 'Draft' THEN 4"
                + " WHEN 'Rejected' THEN 5"
                + " ELSE 6 END, er.ExamRegistrationId DESC";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                DossierDTO dossier = mapDossier(rs);
                loadDocuments(dossier);
                dossiers.add(dossier);
            }
            return dossiers;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tải danh sách hồ sơ", e);
        }
    }

    @Override
    public int ensureRegistration(int profileId, String licenceClass, String source, String applicantType) {
        String normalizedClass = switch (licenceClass) {
            case "A" -> "A2";
            case "B" -> "B2";
            default -> licenceClass;
        };
        try {
            int licenceId = findLicenceId(normalizedClass);
            if (licenceId <= 0) return 0;
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
                    if (rs.next()) return rs.getInt(1);
                }
            }
            String notes = "SOURCE=" + source + ";APPLICANT_TYPE=" + applicantType;
            String insert = """
                    INSERT INTO ExamRegistration (RegistrationStatus, Notes, ProfileId, LicenceId)
                    VALUES ('Draft', ?, ?, ?)
                    """;
            try (PreparedStatement ps = getConnection().prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, notes);
                ps.setInt(2, profileId);
                ps.setInt(3, licenceId);
                if (ps.executeUpdate() == 0) return 0;
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
        String sql = """
                IF EXISTS (SELECT 1 FROM Document WHERE ProfileId = ? AND DocumentType = ?)
                    UPDATE Document SET DocumentUrl = ?, Notes = N'Đã tải lên'
                    WHERE ProfileId = ? AND DocumentType = ?
                ELSE
                    INSERT INTO Document (DocumentType, DocumentUrl, Notes, ProfileId)
                    VALUES (?, ?, N'Đã tải lên', ?)
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, profileId);
            ps.setString(2, documentType);
            ps.setString(3, documentUrl);
            ps.setInt(4, profileId);
            ps.setString(5, documentType);
            ps.setString(6, documentType);
            ps.setString(7, documentUrl);
            ps.setInt(8, profileId);
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

    private DossierDTO mapDossier(ResultSet rs) throws SQLException {
        DossierDTO dossier = new DossierDTO();
        User user = new User();
        user.setId(rs.getInt("UserId"));
        user.setUsername(rs.getString("Username"));
        user.setEmail(rs.getString("Email"));
        user.setPasswordHash(rs.getString("PasswordHash"));
        user.setRole(ExamConstants.roleFromName(rs.getString("Role")));
        user.setIsActive(rs.getBoolean("Status"));

        Profile profile = new Profile();
        profile.setId(rs.getInt("ProfileId"));
        profile.setUserId(user.getId());
        profile.setFullName(rs.getString("FullName"));
        profile.setDateOfBirth(rs.getTimestamp("DateOfBirth"));
        profile.setPhoneNo(rs.getString("PhoneNumber"));
        profile.setGender(ExamConstants.genderFromSex(rs.getString("Sex")));
        profile.setGovIdNo(rs.getString("GovernmentIdNumber"));
        profile.setAddress(rs.getString("Address"));
        user.setProfile(profile);

        dossier.setUser(user);
        dossier.setProfile(profile);
        dossier.setRegistrationId(rs.getInt("ExamRegistrationId"));
        String status = rs.getString("RegistrationStatus");
        dossier.setStatus(status == null ? "Draft" : status);
        dossier.setNotes(rs.getString("Notes"));
        dossier.setLicenceClass(rs.getString("LicenceClass"));
        return dossier;
    }

    private void loadDocuments(DossierDTO dossier) throws SQLException {
        String sql = "SELECT * FROM Document WHERE ProfileId = ? ORDER BY DocumentId";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, dossier.getProfile().getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Document document = new Document(
                            rs.getInt("DocumentId"),
                            rs.getString("DocumentType"),
                            rs.getString("DocumentUrl"),
                            rs.getString("Notes"),
                            rs.getInt("ProfileId"));
                    String key = normalizeDocumentType(document.getDocumentType(), dossier);
                    dossier.getDocuments().put(key, document);
                }
            }
        }
    }

    private String normalizeDocumentType(String documentType, DossierDTO dossier) {
        if (documentType == null) return "OTHER_" + dossier.getDocuments().size();
        String normalized = documentType.trim().toUpperCase();
        if (normalized.equals("PORTRAIT")
                || normalized.contains("CHÂN DUNG")
                || normalized.contains("CHAN DUNG")
                || normalized.contains("ẢNH THẺ")) {
            return "PORTRAIT";
        }
        if (normalized.equals("ID_FRONT") || normalized.contains("CCCD MẶT TRƯỚC")) {
            return "ID_FRONT";
        }
        if (normalized.equals("ID_BACK") || normalized.contains("CCCD MẶT SAU")) {
            return "ID_BACK";
        }
        if (normalized.equals("CCCD")) {
            return dossier.getDocuments().containsKey("ID_FRONT") ? "ID_BACK" : "ID_FRONT";
        }
        if (normalized.equals("HEALTH_CERTIFICATE")
                || normalized.contains("KHÁM SK")
                || normalized.contains("SỨC KHỎE")
                || normalized.contains("SUC KHOE")) {
            return "HEALTH_CERTIFICATE";
        }
        if (normalized.equals("GRADUATION_CERTIFICATE")
                || normalized.contains("TOT NGHIEP")
                || normalized.contains("CHUNG CHI")
                || normalized.contains("DAO TAO")) {
            return "GRADUATION_CERTIFICATE";
        }
        return documentType;
    }
}
