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
 * Licence records on-the-fly for unsupported licence classes.
 * Note: This class does not implement a formal DAO interface.
 */
public class RegistrantApplicationDAOImpl extends DBContext {

    private static final Logger LOG = Logger.getLogger(RegistrantApplicationDAOImpl.class.getName());

    /**
     * Creates a pending exam registration for a registrant.
     * Maps license class codes (A2->A, B2->B) to database values and
     * sets Vietnamese notes based on user type (student vs independent).
     *
     * @param profileId    the ProfileId of the applicant
     * @param licenseClass the requested licence class (e.g. "A1", "B2")
     * @param userType     "student" for chÃ­nh khoÃ¡, otherwise tá»± do
     * @return true if the pending registration was created
     */
    public boolean insertPending(int profileId, String licenseClass, String userType) {
        String databaseLicenseClass = switch (licenseClass) {
            case "A" -> "A2";
            case "B" -> "B2";
            default -> licenseClass;
        };
        String notes = "student".equals(userType)
                ? "Há»c viÃªn chÃ­nh khÃ³a - chá» bá»• sung vÃ  duyá»‡t há»“ sÆ¡"
                : "ThÃ­ sinh tá»± do - chá» bá»• sung vÃ  duyá»‡t há»“ sÆ¡";

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
            case "A1" -> new LicenseDefaults("Xe mÃ´ tÃ´ hai bÃ¡nh Ä‘áº¿n 125 cm3", 18, 0);
            case "A2" -> new LicenseDefaults("Xe mÃ´ tÃ´ hai bÃ¡nh trÃªn 125 cm3", 18, 0);
            case "B1" -> new LicenseDefaults("Ã” tÃ´ sá»‘ tá»± Ä‘á»™ng", 18, 0);
            case "B2" -> new LicenseDefaults("Ã” tÃ´ chá»Ÿ ngÆ°á»i Ä‘áº¿n 8 chá»— vÃ  Ã´ tÃ´ táº£i Ä‘áº¿n 3.500 kg", 18, 10);
            case "C1" -> new LicenseDefaults("Ô tô tải từ 3.500 kg đến 7.500 kg", 21, 10);
            case "C" -> new LicenseDefaults("Ã” tÃ´ táº£i trÃªn 7.500 kg", 21, 5);
            default -> null;
        };
    }

    private record LicenseDefaults(String description, int minimumAge, int validForYears) {
    }
}

