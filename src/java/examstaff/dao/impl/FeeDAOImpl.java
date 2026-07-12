package examstaff.dao.impl;

import shared.dbconnection.DBContext;
import examstaff.dao.FeeDAO;
import shared.model.Fee;
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
                SELECT f.FeeId, f.FeeName, f.FeeType, f.IsActive,
                       COALESCE(lf.Amount, 0) AS Amount
                FROM Fee f
                OUTER APPLY (
                    SELECT TOP 1 lf.Amount
                    FROM Licence_Fee lf
                    WHERE lf.FeeId = f.FeeId
                    ORDER BY CASE WHEN lf.LicenceId IS NULL THEN 1 ELSE 0 END
                ) lf
                WHERE f.IsActive = 1
                ORDER BY f.FeeType, f.FeeName
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
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
        if (licenseCode == null || licenseCode.isBlank()) {
            return List.of();
        }
        String licenceClass = licenseCode.trim().toUpperCase(Locale.ROOT);
        boolean motorcycle = isMotorcycleGroup(licenceClass);
        List<Fee> applicable = new ArrayList<>();
        String sql = """
                SELECT f.FeeId, f.FeeName, f.FeeType, f.IsActive,
                       COALESCE(lf_pick.Amount, 0) AS Amount
                FROM Fee f
                OUTER APPLY (
                    SELECT TOP 1 lf.Amount
                    FROM Licence_Fee lf
                    LEFT JOIN Licence l ON lf.LicenceId = l.LicenceId
                    WHERE lf.FeeId = f.FeeId
                      AND (lf.LicenceId IS NULL OR l.LicenceClass = ?)
                    ORDER BY CASE
                        WHEN l.LicenceClass = ? THEN 0
                        WHEN lf.LicenceId IS NULL THEN 1
                        ELSE 2
                    END
                ) lf_pick
                WHERE f.IsActive = 1
                ORDER BY f.FeeType, f.FeeName
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, licenceClass);
            ps.setString(2, licenceClass);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Fee fee = mapRow(rs);
                    if (appliesToProcedure(fee, motorcycle, requiresRoadTest)) {
                        applicable.add(fee);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
                SELECT f.FeeId, f.FeeName, f.FeeType, f.IsActive,
                       COALESCE(lf_spec.Amount, lf_common.Amount, 0) AS Amount
                FROM Payment_Fee pf
                INNER JOIN Fee f ON f.FeeId = pf.FeeId
                INNER JOIN Payment p ON p.PaymentId = pf.PaymentId
                INNER JOIN ExamEnrollment ee ON ee.ExamEnrollmentId = p.ExamEnrollmentId
                INNER JOIN Exam e ON e.ExamId = ee.ExamId
                INNER JOIN Licence lic ON lic.LicenceId = e.LicenceId
                LEFT JOIN Licence_Fee lf_spec
                    ON lf_spec.FeeId = f.FeeId AND lf_spec.LicenceId = lic.LicenceId
                LEFT JOIN Licence_Fee lf_common
                    ON lf_common.FeeId = f.FeeId AND lf_common.LicenceId IS NULL
                WHERE pf.PaymentId = ?
                ORDER BY f.FeeType, f.FeeName
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
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
        String name = normalize(fee.getFeeName());
        String type = normalize(fee.getFeeType());

        if (type.contains("hoc phi") || name.startsWith("hoc phi")) {
            return false;
        }
        if (type.contains("phi cap bang") || name.contains("gplx")) {
            return true;
        }
        if (type.contains("phi hanh chinh") || name.contains("ho so") || name.contains("dang ky truc tuyen")) {
            return true;
        }
        if (type.contains("le phi thi") || "exam".equals(type)) {
            if (containsAny(name, "ly thuyet")) {
                return true;
            }
            if (containsAny(name, "trong hinh", "sa hinh", "thuc hanh trong")) {
                return !motorcycle;
            }
            return true;
        }
        return "admin".equals(type) || "license".equals(type);
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return java.text.Normalizer.normalize(value, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .trim();
    }

    private static boolean containsAny(String haystack, String... needles) {
        for (String needle : needles) {
            if (haystack.contains(needle)) {
                return true;
            }
        }
        return false;
    }

    static boolean isMotorcycleGroup(String licenseCode) {
        if (licenseCode == null || licenseCode.isBlank()) {
            return false;
        }
        String lc = licenseCode.toUpperCase(Locale.ROOT).trim();
        return lc.equals("A1") || lc.equals("A");
    }

    private Fee mapRow(ResultSet rs) throws SQLException {
        Fee fee = new Fee();
        fee.setFeeId(rs.getInt("FeeId"));
        fee.setFeeName(rs.getString("FeeName"));
        fee.setFeeType(rs.getString("FeeType"));
        fee.setAmount(rs.getDouble("Amount"));
        fee.setActive(rs.getBoolean("IsActive"));
        return fee;
    }
}


