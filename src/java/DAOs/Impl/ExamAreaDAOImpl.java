package DAOs.Impl;

import DBConnection.DBContext;
import DAOs.ExamAreaDAO;
import Models.ExamArea;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ExamAreaDAOImpl implements ExamAreaDAO {

    private final DBContext ctx;

    public ExamAreaDAOImpl() {
        this.ctx = new DBContext();
    }

    @Override
    public List<ExamArea> search(String keyword, String areaType) {
        List<ExamArea> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("select * from ExamArea where 1=1");
        boolean hasKw = keyword != null && !keyword.trim().isEmpty();
        boolean hasType = areaType != null && !areaType.trim().isEmpty();

        if (hasKw) {
            sql.append(" and (AreaName like ? or Location like ? or AreaType like ?)");
        }
        if (hasType) {
            sql.append(" and AreaType = ?");
        }
        sql.append(" order by ExamAreaId desc");

        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql.toString())) {
            int i = 1;

            if (hasKw) {
                String like = "%" + keyword.trim() + "%";
                ps.setString(i++, like);
                ps.setString(i++, like);
                ps.setString(i++, like);
            }
            if (hasType) {
                ps.setString(i++, areaType.trim());
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapToExamArea(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public int insert(ExamArea a) {
        String sql = """
                insert into ExamArea (AreaName, AreaType, Capacity, Location)
                values (?, ?, ?, ?)
                """;

        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, a.getAreaName());
            ps.setString(2, a.getAreaType());
            ps.setInt(3, a.getCapacity());
            ps.setString(4, a.getLocation());

            if (ps.executeUpdate() > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getInt(1);
                    }
                }
                return 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    @Override
    public boolean update(ExamArea a) {
        String sql = """
                update ExamArea
                set AreaName = ?, AreaType = ?, Capacity = ?, Location = ?
                where ExamAreaId = ?
                """;

        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
            ps.setString(1, a.getAreaName());
            ps.setString(2, a.getAreaType());
            ps.setInt(3, a.getCapacity());
            ps.setString(4, a.getLocation());
            ps.setInt(5, a.getExamAreaId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean delete(int examAreaId) {
        String sql = "delete from ExamArea where ExamAreaId = ?";

        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
            ps.setInt(1, examAreaId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public int countAll() {
        String sql = "select count(*) from ExamArea";

        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql);
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
    public ExamArea getById(int examAreaId) {
        String sql = "select * from ExamArea where ExamAreaId = ?";

        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
            ps.setInt(1, examAreaId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapToExamArea(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public List<ExamArea> getActiveTheoryRooms() {
        List<ExamArea> list = new ArrayList<>();
        String sql = "select * from ExamArea where AreaType = N'Lý thuyết' order by AreaName";

        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapToExamArea(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public List<ExamArea> getBySessionId(int sessionId) {
        List<ExamArea> list = new ArrayList<>();
        String sql = """
                select ea.* from ExamArea ea
                join Session_ExamArea sea on ea.ExamAreaId = sea.ExamAreaId
                where sea.SessionId = ?
                order by ea.AreaName
                """;

        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
            ps.setInt(1, sessionId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapToExamArea(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public boolean isAreaInSession(int sessionId, int examAreaId) {
        String sql = "select count(*) from Session_ExamArea where SessionId = ? and ExamAreaId = ?";

        try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            ps.setInt(2, examAreaId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    private ExamArea mapToExamArea(ResultSet rs) throws SQLException {
        ExamArea a = new ExamArea();
        a.setExamAreaId(rs.getInt("ExamAreaId"));
        a.setAreaName(rs.getString("AreaName"));
        a.setAreaType(rs.getString("AreaType"));
        a.setCapacity(rs.getInt("Capacity"));
        a.setLocation(rs.getString("Location"));
        return a;
    }

    private void setIntOrNull(PreparedStatement ps, int idx, Integer val) throws SQLException {
        if (val == null) {
            ps.setNull(idx, java.sql.Types.INTEGER);
        } else {
            ps.setInt(idx, val);
        }
    }
}
