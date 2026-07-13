package DAOs.Impl;

import DAOs.DossierDAO;
import DBConnection.DBContext;
import DTOs.DossierDTO;
import Models.Document;
import Models.Profile;
import Models.User;
import Utils.ExamConstants;
import java.text.Normalizer;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
        return findRegistrantsByStatus("all");
    }

    @Override
    public List<DossierDTO> findRegistrantsByStatus(String statusFilter) {
        return findRegistrantsByFilters(statusFilter, "");
    }

    @Override
    public List<DossierDTO> findRegistrantsByFilters(String statusFilter, String licenceClass) {
        RegistrantFilter filter = buildRegistrantFilter(statusFilter, licenceClass, "", "");
        return executeDossierList(DOSSIER_SELECT + filter.whereClause()
                + " ORDER BY p.FullName, u.UserId", filter.parameters());
    }

    @Override
    public List<DossierDTO> findRegistrantPage(String statusFilter, String licenceClass,
            String keyword, String accountStatus, int page, int pageSize) {
        int safePage = Math.max(page, 1);
        int safePageSize = Math.max(1, Math.min(pageSize, 100));
        RegistrantFilter filter = buildRegistrantFilter(
                statusFilter, licenceClass, keyword, accountStatus);
        List<Object> parameters = new ArrayList<>(filter.parameters());
        parameters.add((safePage - 1) * safePageSize);
        parameters.add(safePageSize);
        String sql = DOSSIER_SELECT + filter.whereClause()
                + " ORDER BY p.FullName, u.UserId"
                + " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        return executeDossierList(sql, parameters);
    }

    @Override
    public int countRegistrants(String statusFilter, String licenceClass,
            String keyword, String accountStatus) {
        RegistrantFilter filter = buildRegistrantFilter(
                statusFilter, licenceClass, keyword, accountStatus);
        String sql = """
                SELECT COUNT(*)
                FROM [User] u
                JOIN Profile p ON p.UserId = u.UserId
                LEFT JOIN ExamRegistration er ON er.ExamRegistrationId = (
                    SELECT TOP 1 er2.ExamRegistrationId
                    FROM ExamRegistration er2
                    WHERE er2.ProfileId = p.ProfileId
                    ORDER BY er2.ExamRegistrationId DESC
                )
                LEFT JOIN Licence l ON l.LicenceId = er.LicenceId
                """ + filter.whereClause();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            bindParameters(ps, filter.parameters());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể đếm danh sách thí sinh", e);
        }
    }

    @Override
    public Map<String, Integer> countApprovedByLicence() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put("A1", 0);
        counts.put("A", 0);
        counts.put("B1", 0);
        String sql = """
                SELECT l.LicenceClass, COUNT(*) AS Total
                FROM ExamRegistration er
                JOIN Licence l ON l.LicenceId = er.LicenceId
                JOIN Profile p ON p.ProfileId = er.ProfileId
                JOIN [User] u ON u.UserId = p.UserId
                WHERE u.[Role] = 'Registrant'
                  AND er.RegistrationStatus = 'Approved'
                  AND l.LicenceClass IN ('A1','A','B1')
                  AND er.ExamRegistrationId = (
                      SELECT TOP 1 er2.ExamRegistrationId
                      FROM ExamRegistration er2
                      WHERE er2.ProfileId = p.ProfileId
                      ORDER BY er2.ExamRegistrationId DESC
                  )
                GROUP BY l.LicenceClass
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) counts.put(rs.getString("LicenceClass"), rs.getInt("Total"));
            return counts;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể thống kê hồ sơ đã duyệt theo hạng", e);
        }
    }

    @Override
    public Map<String, Integer> countRegistrantsByLicence() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put("A1", 0);
        counts.put("A", 0);
        counts.put("B1", 0);
        String sql = """
                SELECT l.LicenceClass, COUNT(*) AS Total
                FROM [User] u
                JOIN Profile p ON p.UserId = u.UserId
                JOIN ExamRegistration er ON er.ExamRegistrationId = (
                    SELECT TOP 1 er2.ExamRegistrationId
                    FROM ExamRegistration er2
                    WHERE er2.ProfileId = p.ProfileId
                    ORDER BY er2.ExamRegistrationId DESC
                )
                JOIN Licence l ON l.LicenceId = er.LicenceId
                WHERE u.[Role] = 'Registrant' AND l.LicenceClass IN ('A1','A','B1')
                GROUP BY l.LicenceClass
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) counts.put(rs.getString("LicenceClass"), rs.getInt("Total"));
            return counts;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể thống kê thí sinh theo hạng", e);
        }
    }

    @Override
    public int countLockedRegistrants() {
        String sql = "SELECT COUNT(*) FROM [User] WHERE [Role] = 'Registrant' AND ISNULL([Status], 0) = 0";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể thống kê tài khoản đã khóa", e);
        }
    }

    @Override
    public int countCompleteRegistrants() {
        String sql = """
                SELECT COUNT(*)
                FROM [User] u
                JOIN Profile p ON p.UserId = u.UserId
                WHERE u.[Role] = 'Registrant'
                  AND (SELECT COUNT(DISTINCT d.DocumentType)
                       FROM Document d
                       WHERE d.ProfileId = p.ProfileId
                         AND d.DocumentType IN ('PORTRAIT','ID_FRONT','ID_BACK','HEALTH_CERTIFICATE')) = 4
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể thống kê hồ sơ đầy đủ", e);
        }
    }

    @Override
    public Map<String, Integer> countRegistrantStatuses() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put("all", 0);
        counts.put("draft", 0);
        counts.put("pending", 0);
        counts.put("supplement", 0);
        counts.put("approved", 0);
        counts.put("rejected", 0);
        counts.put("present", 0);
        counts.put("completed", 0);

        String sql = """
                SELECT er.RegistrationStatus, COUNT(*) AS Total
                FROM [User] u
                JOIN Profile p ON p.UserId = u.UserId
                LEFT JOIN ExamRegistration er ON er.ExamRegistrationId = (
                    SELECT TOP 1 er2.ExamRegistrationId
                    FROM ExamRegistration er2
                    WHERE er2.ProfileId = p.ProfileId
                    ORDER BY er2.ExamRegistrationId DESC
                )
                WHERE u.[Role] = 'Registrant'
                GROUP BY er.RegistrationStatus
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String status = rs.getString("RegistrationStatus");
                int total = rs.getInt("Total");
                counts.merge("all", total, Integer::sum);
                String key = switch (status == null ? "" : status) {
                    case "Pending", "Submitted" -> "pending";
                    case "NeedSupplement" -> "supplement";
                    case "Approved" -> "approved";
                    case "Rejected" -> "rejected";
                    case "Present", "CheckedIn" -> "present";
                    case "Completed" -> "completed";
                    default -> "draft";
                };
                counts.merge(key, total, Integer::sum);
            }
            return counts;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể thống kê trạng thái hồ sơ", e);
        }
    }

    private RegistrantFilter buildRegistrantFilter(String statusFilter, String licenceClass,
            String keyword, String accountStatus) {
        StringBuilder where = new StringBuilder(" WHERE u.[Role] = 'Registrant'");
        List<Object> parameters = new ArrayList<>();
        String status = statusFilter == null
                ? "all" : statusFilter.trim().toLowerCase(Locale.ROOT);
        where.append(switch (status) {
            case "draft" -> " AND (er.RegistrationStatus = 'Draft' OR er.RegistrationStatus IS NULL)";
            case "pending" -> " AND er.RegistrationStatus IN ('Pending','Submitted')";
            case "submitted" -> " AND er.RegistrationStatus = 'Submitted'";
            case "supplement", "needsupplement" -> " AND er.RegistrationStatus = 'NeedSupplement'";
            case "approved" -> " AND er.RegistrationStatus = 'Approved'";
            case "rejected" -> " AND er.RegistrationStatus = 'Rejected'";
            case "present" -> " AND er.RegistrationStatus IN ('Present','CheckedIn')";
            case "completed" -> " AND er.RegistrationStatus = 'Completed'";
            default -> "";
        });

        String licence = licenceClass == null
                ? "" : licenceClass.trim().toUpperCase(Locale.ROOT);
        if (java.util.Set.of("A1", "A", "B1").contains(licence)) {
            where.append(" AND l.LicenceClass = ?");
            parameters.add(licence);
        }

        String search = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        if (!search.isBlank()) {
            where.append("""
                     AND (LOWER(ISNULL(p.FullName, N'')) LIKE ?
                       OR LOWER(ISNULL(p.GovernmentIdNumber, N'')) LIKE ?
                       OR LOWER(ISNULL(p.PhoneNumber, N'')) LIKE ?
                       OR LOWER(ISNULL(u.Username, N'')) LIKE ?
                       OR LOWER(ISNULL(u.Email, N'')) LIKE ?
                       OR CONVERT(varchar(20), u.UserId) = ?)
                    """);
            String like = "%" + search + "%";
            for (int i = 0; i < 5; i++) parameters.add(like);
            parameters.add(search);
        }

        String account = accountStatus == null
                ? "" : accountStatus.trim().toLowerCase(Locale.ROOT);
        if ("active".equals(account)) {
            where.append(" AND ISNULL(u.[Status], 0) = 1");
        } else if ("locked".equals(account)) {
            where.append(" AND ISNULL(u.[Status], 0) = 0");
        }
        return new RegistrantFilter(where.toString(), parameters);
    }

    private List<DossierDTO> executeDossierList(String sql, List<Object> parameters) {
        List<DossierDTO> dossiers = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            bindParameters(ps, parameters);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DossierDTO dossier = mapDossier(rs);
                    loadDocuments(dossier);
                    dossiers.add(dossier);
                }
            }
            return dossiers;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tải danh sách hồ sơ", e);
        }
    }

    private static void bindParameters(PreparedStatement ps, List<Object> parameters)
            throws SQLException {
        for (int index = 0; index < parameters.size(); index++) {
            ps.setObject(index + 1, parameters.get(index));
        }
    }

    private record RegistrantFilter(String whereClause, List<Object> parameters) {}

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
        String sql = DOSSIER_SELECT
                + " WHERE u.[Role] = 'Registrant' AND er.RegistrationStatus IN "
                + "('Draft','Pending','Submitted','NeedSupplement','Rejected')"
                + " ORDER BY CASE er.RegistrationStatus"
                + " WHEN 'Submitted' THEN 1"
                + " WHEN 'Pending' THEN 2"
                + " WHEN 'NeedSupplement' THEN 3"
                + " WHEN 'Draft' THEN 4"
                + " WHEN 'Rejected' THEN 5"
                + " ELSE 6 END, er.ExamRegistrationId DESC";
        return executeDossierList(sql, List.of());
    }

    @Override
    public List<DossierDTO> findSubmittedPage(int page, int pageSize) {
        int safePage = Math.max(page, 1);
        int safePageSize = Math.max(1, Math.min(pageSize, 100));
        String sql = DOSSIER_SELECT
                + " WHERE u.[Role] = 'Registrant' AND er.RegistrationStatus IN "
                + "('Draft','Pending','Submitted','NeedSupplement','Rejected')"
                + " ORDER BY CASE er.RegistrationStatus"
                + " WHEN 'Submitted' THEN 1"
                + " WHEN 'Pending' THEN 2"
                + " WHEN 'NeedSupplement' THEN 3"
                + " WHEN 'Draft' THEN 4"
                + " WHEN 'Rejected' THEN 5"
                + " ELSE 6 END, er.ExamRegistrationId DESC"
                + " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        return executeDossierList(sql,
                List.of((safePage - 1) * safePageSize, safePageSize));
    }

    @Override
    public int countSubmitted() {
        String sql = """
                SELECT COUNT(*)
                FROM [User] u
                JOIN Profile p ON p.UserId = u.UserId
                JOIN ExamRegistration er ON er.ExamRegistrationId = (
                    SELECT TOP 1 er2.ExamRegistrationId
                    FROM ExamRegistration er2
                    WHERE er2.ProfileId = p.ProfileId
                    ORDER BY er2.ExamRegistrationId DESC
                )
                WHERE u.[Role] = 'Registrant'
                  AND er.RegistrationStatus IN ('Draft','Pending','Submitted','NeedSupplement','Rejected')
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể đếm hồ sơ cần thẩm định", e);
        }
    }

    @Override
    public int ensureRegistration(int profileId, String licenceClass, String source, String applicantType) {
        String normalizedClass = licenceClass == null
                ? ""
                : licenceClass.trim().toUpperCase(java.util.Locale.ROOT);
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
        String ascii = removeAccents(normalized);

        if (normalized.equals("PORTRAIT")
                || ascii.contains("ANH")
                || ascii.contains("CHAN DUNG")) {
            return "PORTRAIT";
        }
        if (normalized.equals("ID_FRONT")
                || ascii.contains("ID FRONT")
                || ascii.contains("MAT TRUOC")) {
            return "ID_FRONT";
        }
        if (normalized.equals("ID_BACK")
                || ascii.contains("ID BACK")
                || ascii.contains("MAT SAU")) {
            return "ID_BACK";
        }
        if (normalized.equals("CCCD")) {
            return dossier.getDocuments().containsKey("ID_FRONT") ? "ID_BACK" : "ID_FRONT";
        }
        if (normalized.equals("HEALTH_CERTIFICATE")
                || ascii.contains("KHAM SK")
                || ascii.contains("KHAM SUC KHOE")
                || ascii.contains("SUC KHOE")) {
            return "HEALTH_CERTIFICATE";
        }
        if (normalized.equals("GRADUATION_CERTIFICATE")
                || ascii.contains("TOT NGHIEP")
                || ascii.contains("CHUNG CHI")
                || ascii.contains("DAO TAO")) {
            return "GRADUATION_CERTIFICATE";
        }
        return documentType;
    }

    private String removeAccents(String value) {
        if (value == null) return "";
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return normalized.replace('\u0110', 'D').replace('\u0111', 'd');
    }
}
