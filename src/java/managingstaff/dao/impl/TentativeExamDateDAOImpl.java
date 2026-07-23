package managingstaff.dao.impl;

import java.sql.Date;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import managingstaff.dao.TentativeExamDateDAO;
import managingstaff.dto.TentativeExamDateDTO;
import shared.dbconnection.DBContext;

public class TentativeExamDateDAOImpl extends DBContext implements TentativeExamDateDAO {

    private static final String SELECT = """
        SELECT ed.ExamDateId,ed.ExamDate,ed.LicenceId,l.LicenceClass,
               CASE WHEN COALESCE(ed.Status,'Open')='Cancelled'
                    THEN COALESCE(ed.CancelledRegistrationCount,0)
                    ELSE (SELECT COUNT(*) FROM RegistrationDates rd
                          WHERE rd.ExamDateId=ed.ExamDateId AND rd.IsActive=1)
               END RegisteredCount,
               COALESCE(ed.Status,'Open') Status,ed.CancelReason,ed.CancelledAt,
               COALESCE(ed.CancelledBy,0) CancelledBy,
               COALESCE(ed.CancelledRegistrationCount,0) CancelledRegistrationCount
        FROM ExamDates ed JOIN Licence l ON l.LicenceId=ed.LicenceId
        """;

    @Override
    public List<TentativeExamDateDTO> findPage(String tab, int page, int size) {
        return query(SELECT + dateFilter(tab) + " ORDER BY ed.ExamDate DESC,ed.ExamDateId DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY",
                List.of(Math.max(0, (page - 1) * size), size));
    }

    @Override
    public int countAll(String tab) {
        return scalar("SELECT COUNT(*) FROM ExamDates ed" + dateFilter(tab), 0);
    }

    @Override
    public TentativeExamDateDTO findById(int id) {
        List<TentativeExamDateDTO> rows = query(SELECT + " WHERE ed.ExamDateId=?", List.of(id));
        return rows.isEmpty() ? null : rows.get(0);
    }

    @Override
    public int create(Date date, int licenceId) {
        if (date == null || date.toLocalDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Ngày dự kiến không được ở trong quá khứ.");
        }
        // Một ngày chỉ được mở một đợt dự kiến, không phụ thuộc hạng GPLX.
        String duplicate = "SELECT 1 FROM ExamDates WHERE CAST(ExamDate AS date)=?";
        try (PreparedStatement check = getConnection().prepareStatement(duplicate)) {
            check.setDate(1, date);
            try (ResultSet rs = check.executeQuery()) {
                if (rs.next()) {
                    throw new IllegalArgumentException("Ngày thi dự kiến này đã tồn tại. Vui lòng chọn ngày khác.");
                }
            }
            try (PreparedStatement ps = getConnection().prepareStatement(
                    "INSERT INTO ExamDates(ExamDate,LicenceId) VALUES(?,?)", Statement.RETURN_GENERATED_KEYS)) {
                ps.setDate(1, date);
                ps.setInt(2, licenceId);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    return keys.next() ? keys.getInt(1) : 0;
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tạo ngày thi dự kiến: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Integer> findRegistrationIds(int dateId, int page, int size) {
        return ids(dateId, " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY", List.of(Math.max(0, (page - 1) * size), size));
    }

    @Override
    public List<Integer> findAllRegistrationIds(int dateId) {
        return ids(dateId, "", List.of());
    }

    @Override
    public int countRegistrations(int id) {
        return scalar("SELECT COUNT(*) FROM RegistrationDates WHERE ExamDateId=? AND IsActive=1", id);
    }

    @Override
    public int cancel(int id, String reason, int cancelledByUserId) {
        String normalizedReason = reason == null ? "" : reason.trim();
        if (normalizedReason.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập lý do hủy ngày thi dự kiến.");
        }
        if (normalizedReason.length() > 500) {
            throw new IllegalArgumentException("Lý do hủy không được vượt quá 500 ký tự.");
        }
        Connection connection = getConnection();
        if (connection == null) {
            throw new IllegalStateException("Không thể kết nối cơ sở dữ liệu.");
        }
        boolean previousAutoCommit = true;
        try {
            previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            Date examDate;
            String status;
            try (PreparedStatement lock = connection.prepareStatement(
                    "SELECT ExamDate,COALESCE(Status,'Open') Status FROM ExamDates WITH (UPDLOCK,HOLDLOCK) WHERE ExamDateId=?")) {
                lock.setInt(1, id);
                try (ResultSet rs = lock.executeQuery()) {
                    if (!rs.next()) {
                        throw new IllegalArgumentException("Không tìm thấy ngày thi dự kiến.");
                    }
                    examDate = rs.getDate("ExamDate");
                    status = rs.getString("Status");
                }
            }
            if ("Cancelled".equalsIgnoreCase(status)) {
                throw new IllegalArgumentException("Ngày thi dự kiến này đã được hủy trước đó.");
            }
            LocalDate deadline = examDate.toLocalDate().minusDays(7);
            if (LocalDate.now().isAfter(deadline)) {
                throw new IllegalArgumentException(
                        "Chỉ được hủy trước ngày thi dự kiến ít nhất 7 ngày.");
            }
            int affected;
            try (PreparedStatement count = connection.prepareStatement(
                    "SELECT COUNT(*) FROM RegistrationDates WHERE ExamDateId=? AND IsActive=1")) {
                count.setInt(1, id);
                try (ResultSet rs = count.executeQuery()) {
                    affected = rs.next() ? rs.getInt(1) : 0;
                }
            }
            try (PreparedStatement updateDate = connection.prepareStatement("""
                    UPDATE ExamDates
                    SET Status='Cancelled',CancelReason=?,CancelledAt=SYSDATETIME(),
                        CancelledBy=?,CancelledRegistrationCount=?
                    WHERE ExamDateId=?
                    """)) {
                updateDate.setString(1, normalizedReason);
                if (cancelledByUserId > 0) updateDate.setInt(2, cancelledByUserId);
                else updateDate.setNull(2, java.sql.Types.INTEGER);
                updateDate.setInt(3, affected);
                updateDate.setInt(4, id);
                if (updateDate.executeUpdate() != 1) {
                    throw new SQLException("Không cập nhật được ngày thi dự kiến.");
                }
            }
            try (PreparedStatement deactivate = connection.prepareStatement(
                    "UPDATE RegistrationDates SET IsActive=0 WHERE ExamDateId=? AND IsActive=1")) {
                deactivate.setInt(1, id);
                deactivate.executeUpdate();
            }
            connection.commit();
            return affected;
        } catch (IllegalArgumentException e) {
            rollback(connection);
            throw e;
        } catch (SQLException e) {
            rollback(connection);
            throw new IllegalStateException("Không thể hủy ngày thi dự kiến: " + e.getMessage(), e);
        } finally {
            try { connection.setAutoCommit(previousAutoCommit); } catch (SQLException ignored) { }
        }
    }

    private static String dateFilter(String tab) {
        if ("cancelled".equalsIgnoreCase(tab)) {
            return " WHERE COALESCE(ed.Status,'Open')='Cancelled'";
        }
        if ("expired".equalsIgnoreCase(tab)) {
            return " WHERE COALESCE(ed.Status,'Open')<>'Cancelled'"
                    + " AND CAST(ed.ExamDate AS date)<CAST(GETDATE() AS date)";
        }
        return " WHERE COALESCE(ed.Status,'Open')<>'Cancelled'"
                + " AND CAST(ed.ExamDate AS date)>=CAST(GETDATE() AS date)";
    }

    private List<Integer> ids(int dateId, String suffix, List<Integer> tail) {
        String sql = """
            SELECT rd.ExamRegistrationId FROM RegistrationDates rd
            JOIN ExamRegistration er ON er.ExamRegistrationId=rd.ExamRegistrationId
            WHERE rd.ExamDateId=? AND rd.IsActive=1
            ORDER BY rd.RegistrationDateId
            """ + suffix;
        List<Integer> out = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, dateId);
            for (int i = 0; i < tail.size(); i++) {
                ps.setInt(i + 2, tail.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(rs.getInt(1));
                }
            }
            return out;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private int scalar(String sql, int parameter) {
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            if (sql.contains("?")) {
                ps.setInt(1, parameter);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private List<TentativeExamDateDTO> query(String sql, List<Integer> params) {
        List<TentativeExamDateDTO> out = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                ps.setInt(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TentativeExamDateDTO x = new TentativeExamDateDTO();
                    x.setId(rs.getInt(1));
                    x.setExamDate(rs.getDate(2));
                    x.setLicenceId(rs.getInt(3));
                    x.setLicenceClass(rs.getString(4));
                    x.setRegisteredCount(rs.getInt(5));
                    x.setStatus(rs.getString(6));
                    x.setCancelReason(rs.getString(7));
                    x.setCancelledAt(rs.getTimestamp(8));
                    x.setCancelledBy(rs.getInt(9));
                    x.setCancelledRegistrationCount(rs.getInt(10));
                    out.add(x);
                }
            }
            return out;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void rollback(Connection connection) {
        try { connection.rollback(); } catch (SQLException ignored) { }
    }
}
