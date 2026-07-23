package managingstaff.dao.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import managingstaff.dao.LicenceDAO;
import shared.dbconnection.DBContext;
import shared.model.Licence;

public class LicenceDAOImpl extends DBContext implements LicenceDAO {
    @Override
    public List<Licence> findAll() {
        List<Licence> rows = new ArrayList<>();
        String sql = "SELECT * FROM Licence WHERE LicenceClass IN ('A1','A','B1') ORDER BY LicenceClass";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) rows.add(map(rs));
            return rows;
        } catch (SQLException ex) {
            throw new IllegalStateException("Không thể tải danh sách hạng GPLX", ex);
        }
    }

    @Override
    public Licence findById(int licenceId) {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "SELECT * FROM Licence WHERE LicenceId=? AND LicenceClass IN ('A1','A','B1')")) {
            ps.setInt(1, licenceId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? map(rs) : null; }
        } catch (SQLException ex) {
            throw new IllegalStateException("Không thể tải hạng GPLX", ex);
        }
    }

    private Licence map(ResultSet rs) throws SQLException {
        return new Licence(rs.getInt("LicenceId"), rs.getString("LicenceClass"),
                rs.getString("Description"), rs.getInt("MinimumAge"),
                rs.getInt("ValidForYears"), (Integer) rs.getObject("UpgradeFromLicenceId"));
    }
}
