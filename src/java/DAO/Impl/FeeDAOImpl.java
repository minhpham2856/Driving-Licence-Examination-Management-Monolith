package DAO.Impl;

import DBConnection.DBContext;
import DAO.FeeDAO;
import Models.Fee;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FeeDAOImpl extends DBContext implements FeeDAO {

    @Override
    public List<Fee> getActiveFees() {
        List<Fee> fees = new ArrayList<>();
        String sql = """
                SELECT FeeId, FeeName, FeeType, Amount, IsActive
                FROM Fee
                WHERE IsActive = 1
                ORDER BY FeeType, FeeName
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                fees.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return fees;
    }

    @Override
    public List<Fee> getProcedureFees(String licenseCode, boolean requiresRoadTest) {
        boolean motorcycle = isMotorcycleGroup(licenseCode);
        List<Fee> applicable = new ArrayList<>();
        for (Fee fee : getActiveFees()) {
            if (appliesToProcedure(fee, motorcycle, requiresRoadTest)) {
                applicable.add(fee);
            }
        }
        return applicable;
    }

    @Override
    public double sumProcedureFees(String licenseCode, boolean requiresRoadTest) {
        return getProcedureFees(licenseCode, requiresRoadTest).stream()
                .mapToDouble(Fee::getAmount)
                .sum();
    }

    @Override
    public List<Fee> getFeesByPaymentId(int paymentId) {
        List<Fee> fees = new ArrayList<>();
        String sql = """
                SELECT f.FeeId, f.FeeName, f.FeeType, f.Amount, f.IsActive
                FROM Payment_Fee pf
                INNER JOIN Fee f ON f.FeeId = pf.FeeId
                WHERE pf.PaymentId = ?
                ORDER BY f.FeeType, f.FeeName
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, paymentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    fees.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return fees;
    }

    private boolean appliesToProcedure(Fee fee, boolean motorcycle, boolean requiresRoadTest) {
        String name = fee.getFeeName() != null ? fee.getFeeName().toLowerCase(Locale.ROOT) : "";
        String type = fee.getFeeType() != null ? fee.getFeeType().toLowerCase(Locale.ROOT) : "";

        if (name.contains("lý thuyết") || name.contains("ly thuyet")) {
            return true;
        }
        if (name.contains("sa hình") || name.contains("sa hinh")) {
            return !motorcycle;
        }
        if (name.contains("đường trường") || name.contains("duong truong")) {
            return requiresRoadTest && !motorcycle;
        }
        if (name.contains("hồ sơ") || name.contains("ho so") || "admin".equals(type)) {
            return true;
        }
        if (name.contains("gplx") || "license".equals(type)) {
            return true;
        }
        return "exam".equals(type);
    }

    static boolean isMotorcycleGroup(String licenseCode) {
        if (licenseCode == null || licenseCode.isBlank()) {
            return false;
        }
        String lc = licenseCode.toUpperCase(Locale.ROOT).trim();
        return lc.equals("A1") || lc.equals("A") || lc.equals("A2") || lc.equals("B1");
    }

    private Fee mapRow(ResultSet rs) throws SQLException {
        Fee fee = new Fee();
        fee.setId(rs.getInt("FeeId"));
        fee.setFeeName(rs.getString("FeeName"));
        fee.setFeeType(rs.getString("FeeType"));
        fee.setAmount(rs.getDouble("Amount"));
        fee.setActive(rs.getBoolean("IsActive"));
        return fee;
    }
}
