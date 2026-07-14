package DAOs.Impl;

import DBConnection.DBContext;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO for registrant self-service application creation.
 * Handles initial pending registration insertion and auto-creates
 * Licence records on-the-fly for the supported licence classes.
 * Note: This class does not implement a formal DAO interface.
 */
public class RegistrantApplicationDAOImpl extends DBContext {

    private static final Logger LOG = Logger.getLogger(RegistrantApplicationDAOImpl.class.getName());

    /**
     * Creates a pending exam registration for a registrant.
     * Validates the requested class against the A1/A/B1 scope and sets
     * Vietnamese notes based on user type (student vs independent).
     *
     * @param profileId    the ProfileId of the applicant
     * @param licenseClass the requested licence class (A1, A or B1)
     * @param userType     "student" for chính khoá, otherwise tự do
     * @return true if the pending registration was created
     */
    public boolean insertPending(int profileId, String licenseClass, String userType) {
        String databaseLicenseClass = licenseClass == null
                ? ""
                : licenseClass.trim().toUpperCase(java.util.Locale.ROOT);
        String notes = "student".equals(userType)
                ? "Học viên chính khóa - chờ bổ sung và duyệt hồ sơ"
                : "Thí sinh tự do - chờ bổ sung và duyệt hồ sơ";

        String insertSql = """
                insert into ExamRegistration (RegistrationStatus, Notes, ProfileId, LicenceId)
                values ('Pending', ?, ?, ?)
                """;

        try {
            int licenseId = findOrCreateLicense(databaseLicenseClass);
            if (licenseId <= 0) {
                return false;
            }
            try (PreparedStatement insert = getConnection().prepareStatement(insertSql)) {
                insert.setString(1, notes);
                insert.setInt(2, profileId);
                insert.setInt(3, licenseId);
                return insert.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Failed to create pending registration for profile " + profileId, e);
            return false;
        }
    }

    private int findOrCreateLicense(String licenseClass) throws SQLException {
        String selectSql = "select LicenceId from Licence where LicenceClass = ?";
        try (PreparedStatement select = getConnection().prepareStatement(selectSql)) {
            select.setString(1, licenseClass);
            try (ResultSet rs = select.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("LicenceId");
                }
            }
        }

        LicenseDefaults defaults = defaultsFor(licenseClass);
        if (defaults == null) {
            LOG.log(Level.WARNING, "Unsupported licence class: {0}", licenseClass);
            return 0;
        }

        String insertSql = """
                if not exists (select 1 from Licence where LicenceClass = ?)
                    insert into Licence (LicenceClass, Description, MinimumAge, ValidForYears, UpgradeFromLicenceId)
                    values (?, ?, ?, ?, null)
                """;
        try (PreparedStatement insert = getConnection().prepareStatement(insertSql)) {
            insert.setString(1, licenseClass);
            insert.setString(2, licenseClass);
            insert.setString(3, defaults.description());
            insert.setInt(4, defaults.minimumAge());
            insert.setInt(5, defaults.validForYears());
            insert.executeUpdate();
        }

        try (PreparedStatement select = getConnection().prepareStatement(selectSql)) {
            select.setString(1, licenseClass);
            try (ResultSet rs = select.executeQuery()) {
                return rs.next() ? rs.getInt("LicenceId") : 0;
            }
        }
    }

    private LicenseDefaults defaultsFor(String licenseClass) {
        return switch (licenseClass) {
            case "A1" -> new LicenseDefaults("Xe mô tô hai bánh đến 125 cm³ hoặc động cơ điện đến 11 kW", 18, 0);
            case "A" -> new LicenseDefaults("Xe mô tô hai bánh trên 125 cm³ hoặc động cơ điện trên 11 kW; bao gồm xe hạng A1", 18, 0);
            case "B1" -> new LicenseDefaults("Xe mô tô ba bánh và các loại xe thuộc hạng A1", 18, 0);
            default -> null;
        };
    }

    private record LicenseDefaults(String description, int minimumAge, int validForYears) {
    }
}

