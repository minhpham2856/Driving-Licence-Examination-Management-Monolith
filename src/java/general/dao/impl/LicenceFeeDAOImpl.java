package general.dao.impl;

import general.dao.LicenceFeeDAO;
import shared.dbconnection.DBContext;
import shared.model.Fee;
import shared.model.LicenceFee;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LicenceFeeDAOImpl implements LicenceFeeDAO {

    @Override
    public Map<Integer, List<LicenceFee>> getAllGroupedByLicenceId() {
        Map<Integer, List<LicenceFee>> map = new LinkedHashMap<>();
        String sql = """
                SELECT lf.LicenceFeeId, lf.LicenceId, lf.FeeId, lf.Amount,
                       f.FeeName, f.FeeType, f.IsActive
                FROM Licence_Fee lf
                JOIN Fee f ON f.FeeId = lf.FeeId
                WHERE f.IsActive = 1
                  AND lf.LicenceId IS NOT NULL
                  AND f.FeeType = N'Lệ phí thi'
                ORDER BY lf.LicenceId, f.FeeName
                """;
        try (Connection c = new DBContext().getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                LicenceFee lf = new LicenceFee();
                lf.setLicenceFeeId(rs.getInt("LicenceFeeId"));
                int licenceId = rs.getInt("LicenceId");
                if (rs.wasNull()) {
                    lf.setLicenceId(null);
                    licenceId = 0;
                } else {
                    lf.setLicenceId(licenceId);
                }
                lf.setFeeId(rs.getInt("FeeId"));
                double amount = rs.getDouble("Amount");
                lf.setAmount(rs.wasNull() ? null : amount);

                Fee fee = new Fee();
                fee.setFeeId(rs.getInt("FeeId"));
                fee.setFeeName(rs.getString("FeeName"));
                fee.setFeeType(rs.getString("FeeType"));
                fee.setActive(rs.getBoolean("IsActive"));
                if (lf.getAmount() != null) {
                    fee.setAmount(lf.getAmount());
                }
                lf.setFee(fee);

                List<LicenceFee> list = map.get(licenceId);
                if (list == null) {
                    list = new ArrayList<>();
                    map.put(licenceId, list);
                }
                list.add(lf);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }
}
