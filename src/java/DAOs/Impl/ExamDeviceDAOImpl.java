package DAOs.Impl;

import DBConnection.DBContext;
import DAOs.ExamDeviceDAO;
import Models.ExamDevice;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ExamDeviceDAOImpl implements ExamDeviceDAO {

    private final DBContext ctx;

    public ExamDeviceDAOImpl() {
        this.ctx = new DBContext();
    }

    @Override
    public List<ExamDevice> search(String keyword, Integer roomId, String status) {
        StringBuilder sql = new StringBuilder("""
                select d.ExamDeviceId, d.DeviceName, d.DeviceType, d.[Status], d.ExamAreaId,
                       a.AreaName
                from ExamDevice d
                left join ExamArea a on d.ExamAreaId = a.ExamAreaId
                where 1=1
                """);
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" and d.DeviceName like ? ");
            params.add("%" + keyword.trim() + "%");
        }

        if (status != null && !status.isBlank()) {
            sql.append(" and d.[Status] = ? ");
            params.add(status.trim());
        }

        sql.append(" order by d.ExamDeviceId desc ");

        List<ExamDevice> list = new ArrayList<>();

        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapToExamDevice(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public ExamDevice getById(int examDeviceId) {
        String sql = """
                select d.ExamDeviceId, d.DeviceName, d.DeviceType, d.[Status], d.ExamAreaId,
                       a.AreaName
                from ExamDevice d
                left join ExamArea a on d.ExamAreaId = a.ExamAreaId
                where d.ExamDeviceId = ?
                """;

        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
            ps.setInt(1, examDeviceId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapToExamDevice(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public int insert(ExamDevice d) {
        String sql = """
                insert into ExamDevice (DeviceName, DeviceType, [Status], ExamAreaId)
                values (?, ?, ?, ?)
                """;

        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, d.getDeviceName());
            ps.setString(2, d.getDeviceType());
            ps.setString(3, d.getStatus());
            ps.setInt(4, d.getExamAreaId());

            if (ps.executeUpdate() == 0) {
                return 0;
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    @Override
    public boolean update(ExamDevice d) {
        String sql = """
                update ExamDevice
                set DeviceName = ?, DeviceType = ?, [Status] = ?, ExamAreaId = ?
                where ExamDeviceId = ?
                """;

        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
            ps.setString(1, d.getDeviceName());
            ps.setString(2, d.getDeviceType());
            ps.setString(3, d.getStatus());
            ps.setInt(4, d.getExamAreaId());
            ps.setInt(5, d.getExamDeviceId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean delete(int examDeviceId) {
        String sql = "delete from ExamDevice where ExamDeviceId = ?";

        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
            ps.setInt(1, examDeviceId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public int countAll() {
        return countWhere(null, null);
    }

    @Override
    public int countByStatus(String status) {
        return countWhere("[Status]", status);
    }

    @Override
    public boolean updateStatus(int examDeviceId, String status) {
        String sql = "update ExamDevice set [Status] = ? where ExamDeviceId = ?";

        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, examDeviceId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public List<ExamDevice> getByAreaId(int examAreaId) {
        List<ExamDevice> list = new ArrayList<>();
        String sql = """
                select d.ExamDeviceId, d.DeviceName, d.DeviceType, d.[Status], d.ExamAreaId,
                       a.AreaName
                from ExamDevice d
                left join ExamArea a on d.ExamAreaId = a.ExamAreaId
                where d.ExamAreaId = ?
                order by d.DeviceName
                """;

        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
            ps.setInt(1, examAreaId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapToExamDevice(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    private int countWhere(String col, String val) {
        String sql = "select count(*) from ExamDevice" + (col != null ? " where " + col + " = ?" : "");

        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
            if (col != null) {
                ps.setString(1, val);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    private ExamDevice mapToExamDevice(ResultSet rs) throws SQLException {
        ExamDevice d = new ExamDevice();
        d.setExamDeviceId(rs.getInt("ExamDeviceId"));
        d.setDeviceName(rs.getString("DeviceName"));
        d.setDeviceType(rs.getString("DeviceType"));
        d.setStatus(rs.getString("Status"));
        d.setExamAreaId(rs.getInt("ExamAreaId"));
        d.setAreaName(rs.getString("AreaName"));
        return d;
    }
}
