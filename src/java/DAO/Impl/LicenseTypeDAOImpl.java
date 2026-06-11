package DAO.Impl;

import DAO.LicenseTypeDAO;
import DBConnection.DBContext;
import Models.LicenseType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LicenseTypeDAOImpl extends DBContext implements LicenseTypeDAO {

    @Override
    public List<LicenseType> findAll() {
        String sql = """
                select id, licenseCode, minAge
                from LicenseType
                order by licenseCode
                """;

        List<LicenseType> types = new ArrayList<>();

        try {
            ensureConnection();
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        LicenseType type = new LicenseType();
                        type.setId(rs.getInt("id"));
                        type.setLicenseCode(rs.getString("licenseCode"));
                        type.setMinAge(rs.getInt("minAge"));
                        types.add(type);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return types;
    }

    @Override
    public LicenseType findByCode(String licenseCode) {
        if (licenseCode == null || licenseCode.isBlank()) {
            return null;
        }

        String sql = """
                select id, licenseCode, minAge
                from LicenseType
                where licenseCode = ?
                """;

        try {
            ensureConnection();
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, licenseCode.trim());

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        LicenseType type = new LicenseType();
                        type.setId(rs.getInt("id"));
                        type.setLicenseCode(rs.getString("licenseCode"));
                        type.setMinAge(rs.getInt("minAge"));
                        return type;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}
