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
import shared.util.TentativeExamDatePolicy;

public class TentativeExamDateDAOImpl extends DBContext implements TentativeExamDateDAO {

    // Chỉ đếm/liệt kê RegistrationDates active và đúng hạng với ExamDates.
    private static final String ACTIVE_MATCHING_REG_COUNT = """
            (SELECT COUNT(*) FROM RegistrationDates rd
             JOIN ExamRegistration er ON er.ExamRegistrationId = rd.ExamRegistrationId
             WHERE rd.ExamDateId = ed.ExamDateId
               AND rd.IsActive = 1
               AND er.LicenceId = ed.LicenceId)
            """;

    private static final String SELECT = """
        SELECT ed.ExamDateId,ed.ExamDate,ed.LicenceId,l.LicenceClass,
               CASE WHEN COALESCE(ed.Status,'Open')='Cancelled'
                    THEN COALESCE(ed.CancelledRegistrationCount,0)
                    ELSE """ + ACTIVE_MATCHING_REG_COUNT + """
               END RegisteredCount,
               COALESCE(ed.Status,'Open') Status,ed.CancelReason,ed.CancelledAt,
               COALESCE(ed.CancelledBy,0) CancelledBy,
               COALESCE(ed.CancelledRegistrationCount,0) CancelledRegistrationCount,
               COALESCE(ed.PoliceStatus,'NOT_SENT') PoliceStatus,
               (SELECT COUNT(*) FROM OfficialExamCandidate o
                WHERE o.ExamDateId=ed.ExamDateId) OfficialCandidateCount
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
        if (date.toLocalDate().isBefore(TentativeExamDatePolicy.earliestCreatableDate(LocalDate.now()))) {
            throw new IllegalArgumentException(
                    "Ngày dự kiến phải cách hôm nay hơn 07 ngày làm việc để có thời gian đăng ký.");
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
        return scalar("""
                SELECT COUNT(*) FROM RegistrationDates rd
                JOIN ExamRegistration er ON er.ExamRegistrationId = rd.ExamRegistrationId
                JOIN ExamDates ed ON ed.ExamDateId = rd.ExamDateId
                WHERE rd.ExamDateId = ? AND rd.IsActive = 1 AND er.LicenceId = ed.LicenceId
                """, id);
    }

    @Override
    public int cancel(int id, String reason, int cancelledByUserId) {
        return cancelInternal(id, reason, cancelledByUserId, false);
    }

    @Override
    public List<TentativeExamDateDTO> findDeadlineReviewDates() {
        return query(SELECT + """
                 WHERE COALESCE(ed.Status,'Open') IN ('Open','Locked')
                   AND COALESCE(ed.PoliceStatus,'NOT_SENT')='NOT_SENT'
                 ORDER BY ed.ExamDate,ed.ExamDateId
                """, List.of());
    }

    @Override
    public int autoCancelInsufficient(int id, String reason) {
        return cancelInternal(id, reason, 0, true);
    }

    private int cancelInternal(int id, String reason, int cancelledByUserId, boolean automatic) {
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
            String policeStatus;
            try (PreparedStatement lock = connection.prepareStatement(
                    "SELECT ExamDate,COALESCE(Status,'Open') Status,"
                            + "COALESCE(PoliceStatus,'NOT_SENT') PoliceStatus "
                            + "FROM ExamDates WITH (UPDLOCK,HOLDLOCK) WHERE ExamDateId=?")) {
                lock.setInt(1, id);
                try (ResultSet rs = lock.executeQuery()) {
                    if (!rs.next()) {
                        throw new IllegalArgumentException("Không tìm thấy ngày thi dự kiến.");
                    }
                    examDate = rs.getDate("ExamDate");
                    status = rs.getString("Status");
                    policeStatus = rs.getString("PoliceStatus");
                }
            }
            if ("Cancelled".equalsIgnoreCase(status)) {
                throw new IllegalArgumentException("Ngày thi dự kiến này đã được hủy trước đó.");
            }
            if (!"Open".equalsIgnoreCase(status)
                    && !(automatic && "Locked".equalsIgnoreCase(status))) {
                throw new IllegalArgumentException("Ngày thi dự kiến đã khóa nên không thể hủy.");
            }
            if (!"NOT_SENT".equalsIgnoreCase(policeStatus)) {
                throw new IllegalArgumentException(
                        "Danh sách đã gửi CSGT nên trung tâm không thể tự hủy.");
            }
            boolean reachedDeadline = TentativeExamDatePolicy.shouldBeLocked(
                    examDate.toLocalDate(), LocalDate.now());
            if (!automatic && reachedDeadline) {
                throw new IllegalArgumentException(
                        "Ngày thi dự kiến đã đến mốc khóa trước 07 ngày làm việc nên không thể hủy.");
            }
            if (automatic && !reachedDeadline) {
                throw new IllegalArgumentException("Ngày thi dự kiến chưa đến hạn tự động kiểm tra.");
            }
            int affected;
            try (PreparedStatement count = connection.prepareStatement(
                    "SELECT COUNT(*) FROM RegistrationDates rd WITH (UPDLOCK,HOLDLOCK) "
                            + "JOIN ExamRegistration er ON er.ExamRegistrationId = rd.ExamRegistrationId "
                            + "JOIN ExamDates ed ON ed.ExamDateId = rd.ExamDateId "
                            + "WHERE rd.ExamDateId=? AND rd.IsActive=1 AND er.LicenceId=ed.LicenceId")) {
                count.setInt(1, id);
                try (ResultSet rs = count.executeQuery()) {
                    affected = rs.next() ? rs.getInt(1) : 0;
                }
            }
            if (automatic && affected >= TentativeExamDatePolicy.MIN_REGISTRATIONS) {
                throw new IllegalArgumentException(
                        "Ngày thi dự kiến đã đủ số lượng tối thiểu nên không tự động hủy.");
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

    @Override
    public List<Integer> lockDueDates() {
        List<Integer> dueIds = new ArrayList<>();
        List<Integer> lockedIds = new ArrayList<>();
        String select = """
                SELECT ed.ExamDateId,ed.ExamDate
                FROM ExamDates ed
                WHERE COALESCE(ed.Status,'Open')='Open'
                  AND COALESCE(ed.PoliceStatus,'NOT_SENT')='NOT_SENT'
                  AND (SELECT COUNT(*) FROM RegistrationDates rd
                       JOIN ExamRegistration er ON er.ExamRegistrationId = rd.ExamRegistrationId
                       WHERE rd.ExamDateId=ed.ExamDateId
                         AND rd.IsActive=1
                         AND er.LicenceId=ed.LicenceId) >= ?
                """;
        String update = "UPDATE ExamDates SET Status='Locked' "
                + "WHERE ExamDateId=? AND COALESCE(Status,'Open')='Open'";
        try (PreparedStatement find = getConnection().prepareStatement(select)) {
            find.setInt(1, TentativeExamDatePolicy.MIN_REGISTRATIONS);
            try (ResultSet rs = find.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("ExamDateId");
                    Date date = rs.getDate("ExamDate");
                    if (date != null && TentativeExamDatePolicy.shouldBeLocked(date.toLocalDate(), LocalDate.now())) {
                        dueIds.add(id);
                    }
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tìm ngày thi dự kiến cần khóa: " + e.getMessage(), e);
        }
        try (PreparedStatement lock = getConnection().prepareStatement(update)) {
            for (Integer id : dueIds) {
                    lock.setInt(1, id);
                    if (lock.executeUpdate() == 1) {
                        lockedIds.add(id);
                    }
            }
            return lockedIds;
        } catch (SQLException e) {
            throw new IllegalStateException("Không thể tự động khóa ngày thi dự kiến: " + e.getMessage(), e);
        }
    }

    @Override
    public int submitToPolice(int id) {
        Connection connection = getConnection();
        if (connection == null) {
            throw new IllegalStateException("Không thể kết nối cơ sở dữ liệu.");
        }
        boolean previousAutoCommit = true;
        try {
            previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            String status;
            String policeStatus;
            Date examDate;
            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT ExamDate,COALESCE(Status,'Open') Status,COALESCE(PoliceStatus,'NOT_SENT') PoliceStatus "
                    + "FROM ExamDates WITH (UPDLOCK,HOLDLOCK) WHERE ExamDateId=?")) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) throw new IllegalArgumentException("Không tìm thấy ngày thi dự kiến.");
                    examDate = rs.getDate("ExamDate");
                    status = rs.getString("Status");
                    policeStatus = rs.getString("PoliceStatus");
                }
            }
            if (!"Open".equalsIgnoreCase(status) && !"Locked".equalsIgnoreCase(status)) {
                throw new IllegalArgumentException("Ngày thi dự kiến đã hủy nên không thể gửi CSGT.");
            }
            if (examDate == null || examDate.toLocalDate().isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("Ngày thi dự kiến đã qua nên không thể gửi CSGT.");
            }
            if (!"NOT_SENT".equalsIgnoreCase(policeStatus)) {
                throw new IllegalArgumentException("Danh sách này đã được gửi CSGT trước đó.");
            }
            int count;
            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT COUNT(*) FROM RegistrationDates rd "
                    + "JOIN ExamRegistration er ON er.ExamRegistrationId = rd.ExamRegistrationId "
                    + "JOIN ExamDates ed ON ed.ExamDateId = rd.ExamDateId "
                    + "WHERE rd.ExamDateId=? AND rd.IsActive=1 AND er.LicenceId=ed.LicenceId")) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) { count = rs.next() ? rs.getInt(1) : 0; }
            }
            if (count < TentativeExamDatePolicy.MIN_REGISTRATIONS) {
                throw new IllegalArgumentException("Cần tối thiểu "
                        + TentativeExamDatePolicy.MIN_REGISTRATIONS
                        + " thí sinh mới được gửi danh sách tới CSGT. Hiện có " + count + " thí sinh.");
            }
            if (count > TentativeExamDatePolicy.MAX_REGISTRATIONS) {
                throw new IllegalArgumentException("Danh sách vượt quá giới hạn "
                        + TentativeExamDatePolicy.MAX_REGISTRATIONS + " thí sinh.");
            }
            try (PreparedStatement ps = connection.prepareStatement(
                    "UPDATE rd SET PoliceStatus=N'PENDING',PoliceReason=NULL "
                    + "FROM RegistrationDates rd "
                    + "JOIN ExamRegistration er ON er.ExamRegistrationId = rd.ExamRegistrationId "
                    + "JOIN ExamDates ed ON ed.ExamDateId = rd.ExamDateId "
                    + "WHERE rd.ExamDateId=? AND rd.IsActive=1 AND er.LicenceId=ed.LicenceId")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = connection.prepareStatement(
                    "UPDATE ExamDates SET Status=N'Locked',PoliceStatus=N'PENDING' "
                    + "WHERE ExamDateId=? AND PoliceStatus=N'NOT_SENT' AND Status IN(N'Open',N'Locked')")) {
                ps.setInt(1, id);
                if (ps.executeUpdate() != 1) throw new SQLException("Không cập nhật được trạng thái gửi CSGT.");
            }
            connection.commit();
            return count;
        } catch (IllegalArgumentException ex) {
            rollback(connection);
            throw ex;
        } catch (SQLException ex) {
            rollback(connection);
            throw new IllegalStateException("Không thể gửi danh sách tới CSGT: " + ex.getMessage(), ex);
        } finally {
            try { connection.setAutoCommit(previousAutoCommit); } catch (SQLException ignored) { }
        }
    }

    @Override
    public List<TentativeExamDateDTO> findPoliceCompletedUnlinked() {
        return query(SELECT + " WHERE ed.PoliceStatus=N'COMPLETED' "
                + "AND ed.Status<>N'Cancelled' AND NOT EXISTS(SELECT 1 FROM Exam e WHERE e.SourceExamDateId=ed.ExamDateId) "
                + "ORDER BY ed.ExamDate,ed.ExamDateId", List.of());
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
            JOIN ExamDates ed ON ed.ExamDateId=rd.ExamDateId
            WHERE rd.ExamDateId=? AND rd.IsActive=1 AND er.LicenceId=ed.LicenceId
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
                    x.setPoliceStatus(rs.getString(11));
                    x.setOfficialCandidateCount(rs.getInt(12));
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
