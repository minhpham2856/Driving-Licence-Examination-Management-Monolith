package DAOs.Impl;

import DBConnection.DBContext;
import DAOs.ExamDeviceManageDAO;
import DTOs.ExamDeviceViewDTO;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ExamDeviceManageDAOImpl implements ExamDeviceManageDAO {

    private final DBContext ctx;

    public ExamDeviceManageDAOImpl() {
        this.ctx = new DBContext();
    }

    @Override
    public List<ExamDeviceViewDTO> search(String keyword, String status) {
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

        List<ExamDeviceViewDTO> list = new ArrayList<>();

        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public ExamDeviceViewDTO getById(int examDeviceId) {
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
                    return map(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public int insert(ExamDeviceViewDTO d, Integer createdBy) {
        String sql = """
                insert into ExamDevice (DeviceName, DeviceType, [Status], ExamAreaId,
                    CreatedByUserId, UpdatedByUserId)
                values (?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, d.getDeviceName());
            ps.setString(2, d.getDeviceType());
            ps.setString(3, d.getStatus());
            ps.setInt(4, d.getExamAreaId());
            setNullableInt(ps, 5, createdBy);
            setNullableInt(ps, 6, createdBy);

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
    public boolean update(ExamDeviceViewDTO d, Integer updatedBy) {
        String sql = """
                update ExamDevice
                set DeviceName = ?, DeviceType = ?, [Status] = ?, ExamAreaId = ?,
                    UpdatedAt = GETDATE(), UpdatedByUserId = ?
                where ExamDeviceId = ?
                """;

        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
            ps.setString(1, d.getDeviceName());
            ps.setString(2, d.getDeviceType());
            ps.setString(3, d.getStatus());
            ps.setInt(4, d.getExamAreaId());
            setNullableInt(ps, 5, updatedBy);
            ps.setInt(6, d.getExamDeviceId());

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
        try (PreparedStatement ps = ctx.getConnection().prepareStatement("select count(*) from ExamDevice");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    @Override
    public int countByStatus(String status) {
        try (PreparedStatement ps = ctx.getConnection().prepareStatement("select count(*) from ExamDevice where [Status] = ?")) {
            ps.setString(1, status);

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

    private void setNullableInt(PreparedStatement ps, int idx, Integer val) throws SQLException {
        if (val == null) {
            ps.setNull(idx, java.sql.Types.INTEGER);
        } else {
            ps.setInt(idx, val);
        }
    }

    private ExamDeviceViewDTO map(ResultSet rs) throws SQLException {
        ExamDeviceViewDTO d = new ExamDeviceViewDTO();
        d.setExamDeviceId(rs.getInt("ExamDeviceId"));
        d.setDeviceName(rs.getString("DeviceName"));
        d.setDeviceType(rs.getString("DeviceType"));
        d.setStatus(rs.getString("Status"));
        d.setExamAreaId(rs.getInt("ExamAreaId"));
        d.setAreaName(rs.getString("AreaName"));
        return d;
    }
}
