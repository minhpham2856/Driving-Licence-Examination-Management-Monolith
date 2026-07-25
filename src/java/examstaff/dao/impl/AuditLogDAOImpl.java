package examstaff.dao.impl;


import shared.dbconnection.DBContext;

import examstaff.dao.AuditLogDAO;

import shared.model.Audit;
import examstaff.dto.AuditDTO;
import examstaff.dto.CandidateCallDTO;
import examstaff.dto.StaffProcedureKpiDTO;
import examstaff.util.ExamStaffFormat;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Triển khai JDBC của AuditLogDAO — ghi/đọc nhật ký kiểm tra trên bảng Audit.
 *
 * Hai nhóm SQL chính:
 * - AUDIT_SELECT — list nhật ký + tên người đổi (JOIN User/Profile)
 * - STAFF_PROCEDURE_KPI_SQL / STAFF_PROCEDURE_KPI_BY_DATE_SQL —
 *       KPI thủ tục (số hồ sơ đã thu lệ phí + tổng tiền). Hai hằng <b>đầy đủ</b>,
 *       không sql += runtime: chọn theo có/không filter ngày
 *
 * KPI khớp thế nào?:
 * Join Audit → Candidate (EntityId hoặc Reason chứa SBD) → ExamEnrollment → Payment đã thanh toán;
 * lọc Action/Reason kiểu “thu lệ phí”. Dùng trên màn /examstaff/audit.
 */
public class AuditLogDAOImpl extends DBContext implements AuditLogDAO {

    /** SELECT nhật ký kèm tên người thay đổi từ Audit JOIN User, Profile. */
    private static final String AUDIT_SELECT = """
            SELECT a.AuditId AS id,
                   a.EntityName AS tableName,
                   TRY_CAST(a.EntityId AS INT) AS recordId,
                   a.Action AS action,
                   a.OldValue AS oldValue,
                   a.NewValue AS newValue,
                   ISNULL(a.Details, a.Reason) AS details,
                   a.Reason AS reason,
                   a.UserId AS changedBy,
                   a.CreatedAt AS changedAt,
                   NULL AS ipAddress,
                   NULL AS examId,
                   ISNULL(u.Username, p.FullName) AS changerName
            FROM Audit a
            LEFT JOIN [User] u ON u.UserId = a.UserId
            LEFT JOIN Profile p ON p.UserId = u.UserId
            """;

    /**
 * KPI thủ tục: đếm thí sinh đã thu lệ phí và tổng tiền (mọi ngày).
 * Tham số bind: userId.
 * <p>
 * Bản “không filter ngày” — đối chiếu STAFF_PROCEDURE_KPI_BY_DATE_SQL
 * (thêm AND CAST(a.CreatedAt AS DATE) = ?). Chọn hằng nào ở runtime theo
 * hasDate, không nối chuỗi SQL.
 */
    private static final String STAFF_PROCEDURE_KPI_SQL = """
            SELECT COUNT(DISTINCT x.candidateId) AS completedCount,
                   ISNULL(SUM(x.TotalAmount), 0) AS totalFees
            FROM (
                SELECT DISTINCT
                    c.CandidateId AS candidateId,
                    p.PaymentId,
                    p.TotalAmount
                FROM Audit a
                INNER JOIN Candidate c ON (
                    c.CandidateId = TRY_CAST(NULLIF(NULLIF(LTRIM(RTRIM(a.EntityId)), ''), '0') AS INT)
                    OR a.Reason LIKE N'%' + c.CandidateNumber + N'%'
                )
                INNER JOIN ExamEnrollment ee ON ee.CandidateId = c.CandidateId
                INNER JOIN Payment p ON p.ExamEnrollmentId = ee.ExamEnrollmentId
                    AND p.PaymentStatus IN ("""
            + examstaff.enums.PaymentStatus.sqlInClause()
            + """
                    )
                WHERE a.UserId = ?
                  AND (
                        a.EntityName IN (N'Thanh toán', N'Payment')
                        OR UPPER(ISNULL(a.Reason, N'')) LIKE N'%THU LỆ PHÍ%'
                        OR UPPER(ISNULL(a.Reason, N'')) LIKE N'%THU PHI%'
                      )
                  AND (
                        UPPER(ISNULL(a.Action, N'')) IN (
                            N'INSERT', N'UPDATE', N'THÊM', N'NHẬP', N'CẬP NHẬT'
                        )
                        OR UPPER(ISNULL(a.Reason, N'')) LIKE N'%THU LỆ PHÍ%'
                      )
            ) x
            WHERE x.candidateId IS NOT NULL
            """;

    /**
     * KPI thủ tục lọc theo ngày (yyyy-MM-dd).
     * Tham số: userId, filterDate.
     */
    private static final String STAFF_PROCEDURE_KPI_BY_DATE_SQL = """
            SELECT COUNT(DISTINCT x.candidateId) AS completedCount,
                   ISNULL(SUM(x.TotalAmount), 0) AS totalFees
            FROM (
                SELECT DISTINCT
                    c.CandidateId AS candidateId,
                    p.PaymentId,
                    p.TotalAmount
                FROM Audit a
                INNER JOIN Candidate c ON (
                    c.CandidateId = TRY_CAST(NULLIF(NULLIF(LTRIM(RTRIM(a.EntityId)), ''), '0') AS INT)
                    OR a.Reason LIKE N'%' + c.CandidateNumber + N'%'
                )
                INNER JOIN ExamEnrollment ee ON ee.CandidateId = c.CandidateId
                INNER JOIN Payment p ON p.ExamEnrollmentId = ee.ExamEnrollmentId
                    AND p.PaymentStatus IN ("""
            + examstaff.enums.PaymentStatus.sqlInClause()
            + """
                    )
                WHERE a.UserId = ?
                  AND (
                        a.EntityName IN (N'Thanh toán', N'Payment')
                        OR UPPER(ISNULL(a.Reason, N'')) LIKE N'%THU LỆ PHÍ%'
                        OR UPPER(ISNULL(a.Reason, N'')) LIKE N'%THU PHI%'
                      )
                  AND (
                        UPPER(ISNULL(a.Action, N'')) IN (
                            N'INSERT', N'UPDATE', N'THÊM', N'NHẬP', N'CẬP NHẬT'
                        )
                        OR UPPER(ISNULL(a.Reason, N'')) LIKE N'%THU LỆ PHÍ%'
                      )
                  AND CAST(a.CreatedAt AS DATE) = ?
            ) x
            WHERE x.candidateId IS NOT NULL
            """;

    /**
     * Ghi một bản ghi nhật ký kiểm tra mới vào bảng Audit.
     * INSERT các trường: UserId, Action, Reason, EntityName,
     * EntityId, OldValue, NewValue, Details, CreatedAt.
     * @param log đối tượng Audit chứa thông tin nhật ký; AuditId được gán sau INSERT
     * @return true nếu ghi thành công và lấy được khóa sinh
     */
    @Override
    public boolean insert(Audit log) {
        String sql = """
                INSERT INTO Audit (UserId, Action, Reason, EntityName, EntityId, OldValue, NewValue, Details, CreatedAt)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        // Chuẩn bị PreparedStatement với SQL INSERT Audit
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            String tbl = log.getEntityName();
            if (tbl == null || tbl.trim().isEmpty()) {
                tbl = "Profile";
            }
            String act = log.getAction() != null ? log.getAction() : "UPDATE";
            int userId = log.getUserId() != null && log.getUserId() > 0 ? log.getUserId() : 3;
            String recId = log.getEntityId() != null ? log.getEntityId() : "0";

            // Gán tham số truy vấn
            ps.setInt(1, userId);
            ps.setString(2, act);
            if (log.getReason() != null) {
                ps.setString(3, log.getReason());
            } else {
                ps.setNull(3, Types.NVARCHAR);
            }
            ps.setString(4, tbl);
            ps.setString(5, recId);
            if (log.getOldValue() != null) {
                ps.setString(6, log.getOldValue());
            } else {
                ps.setNull(6, Types.NVARCHAR);
            }
            if (log.getNewValue() != null) {
                ps.setString(7, log.getNewValue());
            } else {
                ps.setNull(7, Types.NVARCHAR);
            }
            if (log.getDetails() != null) {
                ps.setString(8, log.getDetails());
            } else {
                ps.setNull(8, Types.NVARCHAR);
            }
            ps.setTimestamp(9, log.getCreatedAt() != null ? log.getCreatedAt() : new Timestamp(System.currentTimeMillis()));

            // Thực thi INSERT và lấy AuditId sinh ra
            if (ps.executeUpdate() > 0) {
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) {
                        log.setAuditId(gk.getLong(1));
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("AuditLogDAOImpl insert failed: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Ghi nhật ký cuộc gọi thí sinh vào bảng Audit với Action='CALL'.
     * INSERT EntityName='Candidate', EntityId=examId-candidateNo.
     * @param call thông tin cuộc gọi (CandidateCallDTO)
     * @return true nếu INSERT thành công; false nếu call null hoặc lỗi SQL
     */
    @Override
    public boolean insertCall(CandidateCallDTO call) {
        if (call == null) {
            return false;
        }
        String sql = """
                INSERT INTO Audit (UserId, Action, Reason, EntityName, EntityId, NewValue, CreatedAt)
                VALUES (?, 'CALL', ?, 'Candidate', ?, ?, GETDATE())
                """;
        // Chuẩn bị PreparedStatement với SQL INSERT nhật ký cuộc gọi
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            int userId = call.getCalledBy() != 0 ? call.getCalledBy() : 3;
            String entityId = call.getExamId() + "-" + call.getCandidateNo();
            String detail = ExamStaffFormat.formatDetail(call.getCalledTo(), call.getResult());
            // Gán tham số truy vấn
            ps.setInt(1, userId);
            ps.setString(2, detail);
            ps.setString(3, entityId);
            ps.setString(4, detail);
            // Thực thi INSERT
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("AuditLogDAOImpl insertCall failed: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Lấy danh sách nhật ký của người dùng theo ngày cụ thể từ bảng Audit.
     * Giới hạn tối đa 200 bản ghi gần nhất.
     * @param userId  mã người dùng (UserId)
     * @param dateStr ngày cần lọc (định dạng yyyy-MM-dd); null/rỗng → lấy mọi ngày
     * @return danh sách AuditDTO
     */
    @Override
    public List<AuditDTO> getLogsByUserAndDate(int userId, String dateStr) {
        if (dateStr != null && !dateStr.trim().isEmpty()) {
            return queryLogs(AUDIT_SELECT + " WHERE a.UserId = ? AND CAST(a.CreatedAt AS DATE) = ? ORDER BY a.CreatedAt DESC",
                    ps -> {
                        ps.setInt(1, userId);
                        ps.setString(2, dateStr);
                    }, true);
        }
        return queryLogs(AUDIT_SELECT + " WHERE a.UserId = ? ORDER BY a.CreatedAt DESC",
                ps -> ps.setInt(1, userId), true);
    }

    /**
     * Lấy danh sách nhật ký của người dùng theo ngày có phân trang (OFFSET/FETCH).
     * @param userId   mã người dùng
     * @param dateStr  ngày cần lọc (yyyy-MM-dd); null/rỗng → mọi ngày
     * @param page     số trang (bắt đầu từ 1)
     * @param pageSize số bản ghi mỗi trang
     * @return danh sách AuditDTO theo trang
     */
    @Override
    public List<AuditDTO> getLogsByUserAndDatePaginated(int userId, String dateStr, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        if (dateStr != null && !dateStr.trim().isEmpty()) {
            return queryLogs(AUDIT_SELECT + " WHERE a.UserId = ? AND CAST(a.CreatedAt AS DATE) = ? ORDER BY a.CreatedAt DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY",
                    ps -> {
                        ps.setInt(1, userId);
                        ps.setString(2, dateStr);
                        ps.setInt(3, offset);
                        ps.setInt(4, pageSize);
                    }, false);
        }
        return queryLogs(AUDIT_SELECT + " WHERE a.UserId = ? ORDER BY a.CreatedAt DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY",
                ps -> {
                    ps.setInt(1, userId);
                    ps.setInt(2, offset);
                    ps.setInt(3, pageSize);
                }, false);
    }

    /**
     * Đếm số lượng nhật ký của người dùng theo ngày từ bảng Audit.
     * @param userId  mã người dùng
     * @param dateStr ngày cần lọc (yyyy-MM-dd); null/rỗng → đếm mọi ngày
     * @return số bản ghi nhật ký; 0 nếu lỗi hoặc không có
     */
    @Override
    public int getLogsCountByUserAndDate(int userId, String dateStr) {
        if (dateStr != null && !dateStr.trim().isEmpty()) {
            return count("SELECT COUNT(*) FROM Audit WHERE UserId = ? AND CAST(CreatedAt AS DATE) = ?",
                    ps -> {
                        ps.setInt(1, userId);
                        ps.setString(2, dateStr);
                    });
        }
        return count("SELECT COUNT(*) FROM Audit WHERE UserId = ?", ps -> ps.setInt(1, userId));
    }

    /**
     * Chạy SELECT nhật ký với binder tùy chỉnh; limited=true thêm TOP 200.
     * @param sql     câu SELECT (từ AUDIT_SELECT + WHERE/ORDER BY)
     * @param binder  lambda gán tham số PreparedStatement
     * @param limited true giới hạn 200 dòng
     * @return danh sách AuditDTO
     */
    private List<AuditDTO> queryLogs(String sql, SqlBinder binder, boolean limited) {
        List<AuditDTO> list = new ArrayList<>();
        String finalSql = limited ? sql.replaceFirst("SELECT", "SELECT TOP 200") : sql;
        // Chuẩn bị PreparedStatement với SQL SELECT nhật ký
        try (PreparedStatement ps = getConnection().prepareStatement(finalSql)) {
            // Gán tham số truy vấn qua binder
            binder.bind(ps);
            // Thực thi và lấy ResultSet
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // Ánh xạ ResultSet → đối tượng domain
                    list.add(mapResultSetToAuditLog(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Chạy truy vấn COUNT với binder tham số.
     * @param sql    câu SELECT COUNT(*)
     * @param binder lambda gán tham số
     * @return giá trị đếm; 0 nếu không có dòng hoặc lỗi
     */
    private int count(String sql, SqlBinder binder) {
        // Chuẩn bị PreparedStatement với SQL COUNT
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            // Gán tham số truy vấn
            binder.bind(ps);
            // Thực thi và lấy ResultSet
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

    /**
     * Lấy chỉ số KPI thủ tục của cán bộ: số thí sinh đã thu lệ phí và tổng tiền.
     * Truy vấn Audit JOIN Candidate, ExamEnrollment, Payment
     * lọc theo hành động thu phí của userId.
     * @param userId     mã cán bộ
     * @param filterDate ngày lọc (yyyy-MM-dd) hoặc null để lấy tất cả
     * @return StaffProcedureKpiDTO chứa completedCount và totalFees
     */
    @Override
    public StaffProcedureKpiDTO getStaffProcedureKpi(int userId, String filterDate) {
        boolean hasDate = filterDate != null && !filterDate.trim().isEmpty();
        String sql = hasDate ? STAFF_PROCEDURE_KPI_BY_DATE_SQL : STAFF_PROCEDURE_KPI_SQL;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            if (hasDate) {
                ps.setString(2, filterDate);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new StaffProcedureKpiDTO(rs.getInt("completedCount"), rs.getDouble("totalFees"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new StaffProcedureKpiDTO(0, 0);
    }

    /**
     * Ánh xạ một dòng ResultSet (alias từ AUDIT_SELECT) sang AuditDTO.
     * @param rs ResultSet đang trỏ tại dòng cần đọc
     * @return DTO nhật ký kiểm tra
     * @throws SQLException nếu đọc cột thất bại
     */
    private AuditDTO mapResultSetToAuditLog(ResultSet rs) throws SQLException {
        AuditDTO log = new AuditDTO();
        log.setTableName(rs.getString("tableName"));
        log.setAction(rs.getString("action"));
        log.setOldValue(rs.getString("oldValue"));
        log.setNewValue(rs.getString("newValue"));
        log.setDetails(rs.getString("details"));
        log.setReason(rs.getString("reason"));
        log.setChangedAt(rs.getTimestamp("changedAt"));
        return log;
    }

    /** Giao diện functional gán tham số cho PreparedStatement. */
    @FunctionalInterface
    private interface SqlBinder {
        /**
         * Gán các placeholder ? trên PreparedStatement.
         * @param ps PreparedStatement cần bind
         * @throws SQLException nếu set tham số thất bại
         */
        void bind(PreparedStatement ps) throws SQLException;
    }
}
