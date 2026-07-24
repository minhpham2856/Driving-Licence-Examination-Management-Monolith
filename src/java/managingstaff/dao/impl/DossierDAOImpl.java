package managingstaff.dao.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import managingstaff.dao.DossierDAO;
import managingstaff.dto.DossierDTO;
import managingstaff.dto.DossierDTO.DocumentView;
import managingstaff.dto.DossierDTO.ProfileView;
import managingstaff.dto.DossierDTO.UserView;
import shared.dbconnection.DBContext;

public class DossierDAOImpl extends DBContext implements DossierDAO {

    private static final String REGISTRANT_ROLE =
            "r.RoleName IN (N'Người đăng ký thi', N'Registrant')";
    private static final String DERIVED_STATUS = """
            CASE
              WHEN EXISTS (
                SELECT 1 FROM Candidate c
                JOIN ExamEnrollment ee ON ee.CandidateId = c.CandidateId
                JOIN ExamResult xr ON xr.ExamEnrollmentId = ee.ExamEnrollmentId
                WHERE c.GovernmentIdNumber = p.GovernmentIdNumber
              ) THEN 'Completed'
              WHEN EXISTS (
                SELECT 1 FROM Candidate c
                JOIN ExamEnrollment ee ON ee.CandidateId = c.CandidateId
                WHERE c.GovernmentIdNumber = p.GovernmentIdNumber
              ) THEN 'Present'
              ELSE CASE ISNULL(er.RegistrationStatus, 'Draft')
                WHEN N'Duyệt' THEN 'Approved'
                WHEN N'Đã duyệt' THEN 'Approved'
                WHEN N'Chờ duyệt' THEN 'Pending'
                WHEN N'Từ chối' THEN 'Rejected'
                WHEN N'Đã từ chối' THEN 'Rejected'
                ELSE ISNULL(er.RegistrationStatus, 'Draft')
              END
            END
            """;
    private static final String DOSSIER_SELECT = """
            SELECT u.UserId, u.Username, u.Email, u.IsActive,
                   p.ProfileId, p.FullName, p.DateOfBirth, p.PhoneNumber,
                   p.Sex, p.GovernmentIdNumber, p.Address,
                   er.ExamRegistrationId, er.RegistrationStatus, er.Notes,
                   l.LicenceClass,
                   """ + DERIVED_STATUS + " AS DerivedStatus " + """
            FROM [User] u
            JOIN [Role] r ON r.RoleId = u.RoleId
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
    public List<DossierDTO> findRegistrantsByFilters(String statusFilter, String licenceClass) {
        Filter filter = buildFilter(statusFilter, licenceClass, "", "", "", "", "");
        return executeList(DOSSIER_SELECT + filter.where()
                + " ORDER BY p.FullName, u.UserId", filter.parameters());
    }

    @Override
    public List<DossierDTO> findRegistrantPage(String statusFilter, String licenceClass,
            String fullName, String govIdNo, String email, String phoneNo,
            String accountStatus, int page, int pageSize) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(1, Math.min(pageSize, 100));
        Filter filter = buildFilter(statusFilter, licenceClass, fullName, govIdNo,
                email, phoneNo, accountStatus);
        List<Object> parameters = new ArrayList<>(filter.parameters());
        parameters.add((safePage - 1) * safeSize);
        parameters.add(safeSize);
        return executeList(DOSSIER_SELECT + filter.where()
                + " ORDER BY p.FullName, u.UserId OFFSET ? ROWS FETCH NEXT ? ROWS ONLY",
                parameters);
    }

    @Override
    public int countRegistrants(String statusFilter, String licenceClass,
            String fullName, String govIdNo, String email, String phoneNo,
            String accountStatus) {
        Filter filter = buildFilter(statusFilter, licenceClass, fullName, govIdNo,
                email, phoneNo, accountStatus);
        String sql = "SELECT COUNT(*) FROM (" + DOSSIER_SELECT + filter.where() + ") q";
        return queryCount(sql, filter.parameters());
    }

    @Override
    public Map<String, Integer> countRegistrantStatuses() {
        Map<String, Integer> counts = emptyStatusCounts();
        String sql = "SELECT q.DerivedStatus, COUNT(*) Total FROM (" + DOSSIER_SELECT
                + " WHERE " + REGISTRANT_ROLE + ") q GROUP BY q.DerivedStatus";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String key = statusKey(rs.getString("DerivedStatus"));
                int total = rs.getInt("Total");
                counts.merge("all", total, Integer::sum);
                counts.merge(key, total, Integer::sum);
            }
            return counts;
        } catch (SQLException ex) {
            throw new IllegalStateException("Không thể thống kê trạng thái hồ sơ", ex);
        }
    }

    @Override
    public Map<String, Integer> countApprovedByLicence() {
        return countByLicence(
                "AND er.RegistrationStatus IN ('Approved',N'Duyệt',N'Đã duyệt')");
    }

    @Override
    public Map<String, Integer> countRegistrantsByLicence() {
        return countByLicence("");
    }

    private Map<String, Integer> countByLicence(String extraCondition) {
        Map<String, Integer> counts = emptyLicenceCounts();
        String sql = """
                SELECT l.LicenceClass, COUNT(*) Total
                FROM [User] u
                JOIN [Role] r ON r.RoleId = u.RoleId
                JOIN Profile p ON p.UserId = u.UserId
                JOIN ExamRegistration er ON er.ExamRegistrationId = (
                    SELECT TOP 1 er2.ExamRegistrationId FROM ExamRegistration er2
                    WHERE er2.ProfileId = p.ProfileId ORDER BY er2.ExamRegistrationId DESC
                )
                JOIN Licence l ON l.LicenceId = er.LicenceId
                """ + " WHERE " + REGISTRANT_ROLE
                + " AND l.LicenceClass IN ('A1','A','B1') " + extraCondition
                + " GROUP BY l.LicenceClass";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) counts.put(rs.getString(1), rs.getInt(2));
            return counts;
        } catch (SQLException ex) {
            throw new IllegalStateException("Không thể thống kê thí sinh theo hạng", ex);
        }
    }

    @Override
    public int countLockedRegistrants() {
        String sql = "SELECT COUNT(*) FROM [User] u JOIN [Role] r ON r.RoleId=u.RoleId"
                + " WHERE " + REGISTRANT_ROLE + " AND u.IsActive=0";
        return queryCount(sql, List.of());
    }

    @Override
    public int countCompleteRegistrants() {
        String sql = """
                SELECT COUNT(*) FROM [User] u
                JOIN [Role] r ON r.RoleId=u.RoleId
                JOIN Profile p ON p.UserId=u.UserId
                """ + " WHERE " + REGISTRANT_ROLE + """
                  AND (SELECT COUNT(DISTINCT
                        CASE
                          WHEN UPPER(dt.[Type]) LIKE '%PORTRAIT%' OR dt.[Type] LIKE N'%chân dung%' THEN 'PORTRAIT'
                          WHEN UPPER(dt.[Type]) LIKE '%ID_FRONT%' OR dt.[Type] LIKE N'%mặt trước%' THEN 'ID_FRONT'
                          WHEN UPPER(dt.[Type]) LIKE '%ID_BACK%' OR dt.[Type] LIKE N'%mặt sau%' THEN 'ID_BACK'
                          WHEN UPPER(dt.[Type]) LIKE '%HEALTH%' OR dt.[Type] LIKE N'%sức khỏe%' THEN 'HEALTH'
                        END)
                       FROM Document d JOIN DocumentType dt ON dt.DocumentTypeId=d.DocumentTypeId
                       WHERE d.ProfileId=p.ProfileId) = 4
                """;
        return queryCount(sql, List.of());
    }

    @Override
    public List<DossierDTO> findSubmittedPage(int page, int pageSize) {
        return findRegistrantPage("pending", "", "", "", "", "", "", page, pageSize);
    }

    @Override
    public int countSubmitted() {
        return countRegistrants("pending", "", "", "", "", "", "");
    }

    @Override
    public int ensureRegistration(int profileId, String licenceClass, String source, String applicantType) {
        String licence = normalizeLicence(licenceClass);
        if (!List.of("A1", "A", "B1").contains(licence)) return 0;
        int licenceId = findLicenceId(licence);
        if (licenceId <= 0) return 0;
        String check = "SELECT TOP 1 ExamRegistrationId FROM ExamRegistration"
                + " WHERE ProfileId=? AND LicenceId=? ORDER BY ExamRegistrationId DESC";
        try (PreparedStatement ps = getConnection().prepareStatement(check)) {
            ps.setInt(1, profileId);
            ps.setInt(2, licenceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
            String notes = "SOURCE=" + (source == null ? "STAFF" : source)
                    + ";APPLICANT=" + (applicantType == null ? "managed" : applicantType);
            try (PreparedStatement insert = getConnection().prepareStatement(
                    "INSERT INTO ExamRegistration (RegistrationStatus,Notes,ProfileId,LicenceId)"
                    + " VALUES ('Pending',?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                insert.setString(1, notes);
                insert.setInt(2, profileId);
                insert.setInt(3, licenceId);
                insert.executeUpdate();
                try (ResultSet keys = insert.getGeneratedKeys()) {
                    return keys.next() ? keys.getInt(1) : 0;
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Không thể tạo hồ sơ đăng ký", ex);
        }
    }

    @Override
    public boolean saveDocument(int profileId, String documentType, String documentUrl) {
        String displayType = documentTypeLabel(documentType);
        try {
            int typeId = ensureDocumentType(displayType);
            String lookup = "SELECT TOP 1 DocumentId FROM Document WHERE ProfileId=? AND DocumentTypeId=?";
            try (PreparedStatement ps = getConnection().prepareStatement(lookup)) {
                ps.setInt(1, profileId);
                ps.setInt(2, typeId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        try (PreparedStatement update = getConnection().prepareStatement(
                                "UPDATE Document SET DocumentUrl=?, Notes=N'Đã tải lên' WHERE DocumentId=?")) {
                            update.setString(1, documentUrl);
                            update.setInt(2, rs.getInt(1));
                            return update.executeUpdate() == 1;
                        }
                    }
                }
            }
            try (PreparedStatement insert = getConnection().prepareStatement(
                    "INSERT INTO Document (DocumentTypeId,DocumentUrl,Notes,ProfileId) VALUES (?,?,N'Đã tải lên',?)")) {
                insert.setInt(1, typeId);
                insert.setString(2, documentUrl);
                insert.setInt(3, profileId);
                return insert.executeUpdate() == 1;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Không thể lưu tài liệu hồ sơ", ex);
        }
    }

    @Override
    public DocumentView findDocumentById(int documentId) {
        if (documentId <= 0) return null;
        String sql = "SELECT d.DocumentId,dt.[Type] DocumentType,d.DocumentUrl,d.Notes,d.ProfileId "
                + "FROM Document d JOIN DocumentType dt ON dt.DocumentTypeId=d.DocumentTypeId "
                + "WHERE d.DocumentId=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, documentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? new DocumentView(rs.getInt("DocumentId"),
                        rs.getString("DocumentType"), rs.getString("DocumentUrl"),
                        rs.getString("Notes"), rs.getInt("ProfileId")) : null;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Không thể tải thông tin tài liệu", ex);
        }
    }

    @Override
    public boolean updateStatus(int registrationId, String status, String message, int actorUserId) {
        String sql = "UPDATE ExamRegistration SET RegistrationStatus=?, Notes="
                + "CONCAT(ISNULL(Notes,''), ';MESSAGE=',?) WHERE ExamRegistrationId=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, message == null ? "" : message.replace(';', ','));
            ps.setInt(3, registrationId);
            return ps.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw new IllegalStateException("Không thể cập nhật trạng thái hồ sơ", ex);
        }
    }

    @Override
    public boolean setUserActive(int userId, boolean active) {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "UPDATE [User] SET IsActive=? WHERE UserId=?")) {
            ps.setBoolean(1, active);
            ps.setInt(2, userId);
            return ps.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw new IllegalStateException("Không thể cập nhật tài khoản", ex);
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
        } catch (SQLException ex) {
            throw new IllegalStateException("Không thể tải hồ sơ", ex);
        }
    }

    private List<DossierDTO> executeList(String sql, List<Object> parameters) {
        List<DossierDTO> dossiers = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            bind(ps, parameters);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DossierDTO dossier = mapDossier(rs);
                    loadDocuments(dossier);
                    dossiers.add(dossier);
                }
            }
            return dossiers;
        } catch (SQLException ex) {
            throw new IllegalStateException("Không thể tải danh sách hồ sơ", ex);
        }
    }

    private DossierDTO mapDossier(ResultSet rs) throws SQLException {
        UserView user = new UserView();
        user.setId(rs.getInt("UserId"));
        user.setUsername(rs.getString("Username"));
        user.setEmail(rs.getString("Email"));
        user.setActive(rs.getBoolean("IsActive"));

        ProfileView profile = new ProfileView();
        profile.setId(rs.getInt("ProfileId"));
        profile.setUserId(user.getId());
        profile.setFullName(rs.getString("FullName"));
        profile.setDateOfBirth(rs.getTimestamp("DateOfBirth"));
        profile.setPhoneNo(rs.getString("PhoneNumber"));
        profile.setGender(rs.getBoolean("Sex") ? "Nam" : "Nữ");
        profile.setGovIdNo(rs.getString("GovernmentIdNumber"));
        profile.setAddress(rs.getString("Address"));

        DossierDTO dossier = new DossierDTO();
        dossier.setUser(user);
        dossier.setProfile(profile);
        dossier.setRegistrationId(rs.getInt("ExamRegistrationId"));
        dossier.setStatus(rs.getString("DerivedStatus"));
        dossier.setNotes(rs.getString("Notes"));
        dossier.setLicenceClass(rs.getString("LicenceClass"));
        return dossier;
    }

    private void loadDocuments(DossierDTO dossier) throws SQLException {
        String sql = "SELECT d.DocumentId,dt.[Type] DocumentType,d.DocumentUrl,d.Notes,d.ProfileId"
                + " FROM Document d JOIN DocumentType dt ON dt.DocumentTypeId=d.DocumentTypeId"
                + " WHERE d.ProfileId=? ORDER BY d.DocumentId";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, dossier.getProfile().getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DocumentView doc = new DocumentView(rs.getInt("DocumentId"),
                            rs.getString("DocumentType"),
                            rs.getString("DocumentUrl"),
                            rs.getString("Notes"), rs.getInt("ProfileId"));
                    dossier.getDocuments().put(normalizeDocumentType(doc.getDocumentType(), dossier), doc);
                }
            }
        }
    }

    private Filter buildFilter(String statusFilter, String licenceClass,
            String fullName, String govIdNo, String email, String phoneNo,
            String accountStatus) {
        StringBuilder where = new StringBuilder(" WHERE ").append(REGISTRANT_ROLE);
        List<Object> params = new ArrayList<>();
        String derived = "(" + DERIVED_STATUS + ")";
        String status = safe(statusFilter).toLowerCase(Locale.ROOT);
        switch (status) {
            case "draft" -> where.append(" AND ").append(derived).append("='Draft'");
            case "pending" -> where.append(" AND ").append(derived).append(" IN ('Pending','Submitted')");
            case "supplement", "needsupplement" -> where.append(" AND ").append(derived).append("='NeedSupplement'");
            case "approved" -> where.append(" AND ").append(derived).append("='Approved'")
                    .append(" AND NOT EXISTS (SELECT 1 FROM RegistrationDates rd WHERE rd.ExamRegistrationId=er.ExamRegistrationId AND rd.IsActive=1)");
            case "waitingexam" -> where.append(" AND NOT EXISTS (SELECT 1 FROM Candidate c JOIN ExamEnrollment ee ON ee.CandidateId=c.CandidateId WHERE c.GovernmentIdNumber=p.GovernmentIdNumber)")
                    .append(" AND (er.RegistrationStatus IN ('WaitingExam',N'Chờ thi') OR EXISTS (SELECT 1 FROM RegistrationDates rd WHERE rd.ExamRegistrationId=er.ExamRegistrationId AND rd.IsActive=1))");
            case "officialscheduled" -> where.append(" AND EXISTS (SELECT 1 FROM Candidate c JOIN ExamEnrollment ee ON ee.CandidateId=c.CandidateId WHERE c.GovernmentIdNumber=p.GovernmentIdNumber)")
                    .append(" AND NOT EXISTS (SELECT 1 FROM Candidate c JOIN ExamEnrollment ee ON ee.CandidateId=c.CandidateId JOIN ExamResult xr ON xr.ExamEnrollmentId=ee.ExamEnrollmentId WHERE c.GovernmentIdNumber=p.GovernmentIdNumber)");
            case "rejected" -> where.append(" AND ").append(derived).append("='Rejected'");
            case "present" -> where.append(" AND ").append(derived).append("='Present'");
            case "completed" -> where.append(" AND ").append(derived).append("='Completed'");
            case "reviewable" -> where.append(" AND ISNULL(er.RegistrationStatus,'Draft')")
                    .append(" IN ('Draft','Pending','Submitted','NeedSupplement')");
            default -> { }
        }
        String licence = normalizeLicence(licenceClass);
        if (List.of("A1", "A", "B1").contains(licence)) {
            where.append(" AND l.LicenceClass=?");
            params.add(licence);
        }
        appendTextFilter(where, params, "p.FullName", fullName);
        appendTextFilter(where, params, "p.GovernmentIdNumber", govIdNo);
        appendTextFilter(where, params, "u.Email", email);
        appendTextFilter(where, params, "p.PhoneNumber", phoneNo);
        if ("active".equalsIgnoreCase(accountStatus)) where.append(" AND u.IsActive=1");
        if ("locked".equalsIgnoreCase(accountStatus)) where.append(" AND u.IsActive=0");
        return new Filter(where.toString(), params);
    }

    private static void appendTextFilter(StringBuilder where, List<Object> params,
            String column, String value) {
        String search = safe(value).toLowerCase(Locale.ROOT);
        if (!search.isBlank()) {
            where.append(" AND LOWER(ISNULL(").append(column).append(",'')) LIKE ?");
            params.add("%" + search + "%");
        }
    }

    private int queryCount(String sql, List<Object> parameters) {
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            bind(ps, parameters);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        } catch (SQLException ex) {
            throw new IllegalStateException("Không thể đếm dữ liệu hồ sơ", ex);
        }
    }

    private int findLicenceId(String licenceClass) {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "SELECT LicenceId FROM Licence WHERE LicenceClass=?")) {
            ps.setString(1, licenceClass);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        } catch (SQLException ex) {
            throw new IllegalStateException("Không thể tìm hạng GPLX", ex);
        }
    }

    private int ensureDocumentType(String type) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "SELECT DocumentTypeId FROM DocumentType WHERE [Type]=?")) {
            ps.setString(1, type);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getInt(1); }
        }
        try (PreparedStatement ps = getConnection().prepareStatement(
                "INSERT INTO DocumentType ([Type]) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, type);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("Không tạo được loại tài liệu");
    }

    private static String documentTypeLabel(String key) {
        return switch (safe(key).toUpperCase(Locale.ROOT)) {
            case "PORTRAIT" -> "Ảnh chân dung";
            case "ID_FRONT" -> "Căn cước công dân (mặt trước)";
            case "ID_BACK" -> "Căn cước công dân (mặt sau)";
            case "HEALTH_CERTIFICATE" -> "Giấy khám sức khỏe";
            default -> key;
        };
    }

    private static String normalizeDocumentType(String value, DossierDTO dossier) {
        String type = safe(value).toLowerCase(Locale.ROOT);
        if (type.contains("portrait") || type.contains("chân dung")) return "PORTRAIT";
        if (type.contains("id_front") || type.contains("mặt trước")) return "ID_FRONT";
        if (type.contains("id_back") || type.contains("mặt sau")) return "ID_BACK";
        if (type.contains("health") || type.contains("sức khỏe")) return "HEALTH_CERTIFICATE";
        if (type.contains("graduation") || type.contains("tốt nghiệp")) return "GRADUATION_CERTIFICATE";
        return "OTHER_" + dossier.getDocuments().size();
    }

    private static String statusKey(String status) {
        return switch (safe(status)) {
            case "Pending", "Submitted" -> "pending";
            case "NeedSupplement" -> "supplement";
            case "Approved" -> "approved";
            case "Rejected" -> "rejected";
            case "Present", "CheckedIn" -> "present";
            case "Completed" -> "completed";
            default -> "draft";
        };
    }

    private static Map<String, Integer> emptyStatusCounts() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (String key : List.of("all", "draft", "pending", "supplement",
                "approved", "rejected", "present", "completed")) counts.put(key, 0);
        return counts;
    }

    private static Map<String, Integer> emptyLicenceCounts() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put("A1", 0); counts.put("A", 0); counts.put("B1", 0);
        return counts;
    }

    private static void bind(PreparedStatement ps, List<Object> parameters) throws SQLException {
        for (int i = 0; i < parameters.size(); i++) ps.setObject(i + 1, parameters.get(i));
    }

    private static String normalizeLicence(String value) {
        return safe(value).toUpperCase(Locale.ROOT);
    }

    private static String safe(String value) { return value == null ? "" : value.trim(); }

    private record Filter(String where, List<Object> parameters) { }
}
