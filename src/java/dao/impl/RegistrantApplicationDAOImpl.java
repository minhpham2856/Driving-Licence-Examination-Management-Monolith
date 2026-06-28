package dao.impl;


import dbconnection.DBContext;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class RegistrantApplicationDAOImpl extends DBContext {

    private static final Logger LOG = Logger.getLogger(RegistrantApplicationDAOImpl.class.getName());

    
    public boolean insertPending(int profileId, String licenseClass, String userType) {
        String databaseLicenseClass = switch (licenseClass) {
            case "A2" -> "A";
            case "B2" -> "B";
            default -> licenseClass;
        };
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
            case "A1" -> new LicenseDefaults("Xe mô tô hai bánh đến 125 cm3", 18, 0);
            case "A" -> new LicenseDefaults("Xe mô tô hai bánh trên 125 cm3", 18, 0);
            case "B1" -> new LicenseDefaults("Ô tô số tự động", 18, 0);
            case "B" -> new LicenseDefaults("Ô tô chở người đến 8 chỗ và ô tô tải đến 3.500 kg", 18, 10);
            case "C" -> new LicenseDefaults("Ô tô tải trên 7.500 kg", 21, 5);
            default -> null;
        };
    }

    private record LicenseDefaults(String description, int minimumAge, int validForYears) {
    }
}
