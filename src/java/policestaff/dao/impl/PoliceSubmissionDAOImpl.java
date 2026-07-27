package policestaff.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import policestaff.dao.PoliceSubmissionDAO;
import policestaff.dto.PoliceSubmissionDTO;
import policestaff.dto.PoliceCandidateDTO;
import policestaff.dto.OfficialExamCandidateDTO;
import shared.dbconnection.DBContext;

public class PoliceSubmissionDAOImpl extends DBContext implements PoliceSubmissionDAO {

    private static final String SUMMARY_SELECT = """
            SELECT ed.ExamDateId,ed.ExamDate,l.LicenceClass,ed.PoliceStatus,
                   SUM(CASE WHEN rd.PoliceStatus IN(N'PENDING',N'APPROVED',N'REJECTED')
                            THEN 1 ELSE 0 END) TotalCandidates,
                   SUM(CASE WHEN rd.IsActive=1 AND rd.PoliceStatus=N'PENDING' THEN 1 ELSE 0 END) PendingCandidates,
                   SUM(CASE WHEN rd.IsActive=1 AND rd.PoliceStatus=N'APPROVED' THEN 1 ELSE 0 END) ApprovedCandidates,
                   SUM(CASE WHEN rd.PoliceStatus=N'REJECTED' THEN 1 ELSE 0 END) RejectedCandidates
            FROM ExamDates ed
            JOIN Licence l ON l.LicenceId=ed.LicenceId
            LEFT JOIN RegistrationDates rd ON rd.ExamDateId=ed.ExamDateId
            """;

    @Override
    public List<PoliceSubmissionDTO> findRecentSubmissions(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        String sql = "SELECT TOP (?) * FROM (" + SUMMARY_SELECT
                + " WHERE ed.PoliceStatus IN (N'PENDING',N'COMPLETED')"
                + " GROUP BY ed.ExamDateId,ed.ExamDate,l.LicenceClass,ed.PoliceStatus"
                + ") q ORDER BY CASE WHEN q.PoliceStatus=N'PENDING' THEN 0 ELSE 1 END,"
                + " q.ExamDate DESC,q.ExamDateId DESC";
        List<PoliceSubmissionDTO> rows = new ArrayList<>();
        if (getConnection() == null) {
            return rows;
        }
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, safeLimit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(mapSummary(rs));
                }
            }
            return rows;
        } catch (SQLException ex) {
            throw new IllegalStateException("Không thể tải danh sách gửi CSGT: " + ex.getMessage(), ex);
        }
    }

    @Override
    public List<PoliceSubmissionDTO> findSubmissions(String policeStatus, Integer year, int offset, int limit) {
        StringBuilder where = new StringBuilder(" WHERE ed.PoliceStatus IN (N'PENDING',N'COMPLETED')");
        if (policeStatus != null) where.append(" AND ed.PoliceStatus=?");
        if (year != null) where.append(" AND YEAR(ed.ExamDate)=?");
        String sql = "SELECT * FROM (" + SUMMARY_SELECT + where
                + " GROUP BY ed.ExamDateId,ed.ExamDate,l.LicenceClass,ed.PoliceStatus) q"
                + " ORDER BY CASE WHEN q.PoliceStatus=N'PENDING' THEN 0 ELSE 1 END,q.ExamDate DESC,q.ExamDateId DESC"
                + " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        List<PoliceSubmissionDTO> rows = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            int index = 1;
            if (policeStatus != null) ps.setString(index++, policeStatus);
            if (year != null) ps.setInt(index++, year);
            ps.setInt(index++, Math.max(0, offset));
            ps.setInt(index, Math.max(1, Math.min(limit, 100)));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) rows.add(mapSummary(rs));
            }
            return rows;
        } catch (SQLException ex) { throw failure(ex); }
    }

    @Override
    public int countSubmissions(String policeStatus, Integer year) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM ExamDates WHERE PoliceStatus IN(N'PENDING',N'COMPLETED')");
        if (policeStatus != null) sql.append(" AND PoliceStatus=?");
        if (year != null) sql.append(" AND YEAR(ExamDate)=?");
        try (PreparedStatement ps = getConnection().prepareStatement(sql.toString())) {
            int index = 1;
            if (policeStatus != null) ps.setString(index++, policeStatus);
            if (year != null) ps.setInt(index, year);
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt(1); }
        } catch (SQLException ex) { throw failure(ex); }
    }

    @Override
    public int countPendingCandidates() {
        String sql = "SELECT COUNT(*) FROM RegistrationDates rd JOIN ExamDates ed "
                + "ON ed.ExamDateId=rd.ExamDateId WHERE rd.IsActive=1 "
                + "AND rd.PoliceStatus=N'PENDING' AND ed.PoliceStatus=N'PENDING'";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt(1); }
        catch (SQLException ex) { throw failure(ex); }
    }

    @Override
    public List<Integer> findCompletedYears() {
        List<Integer> years = new ArrayList<>();
        String sql = "SELECT DISTINCT YEAR(ExamDate) ReportYear FROM ExamDates "
                + "WHERE PoliceStatus=N'COMPLETED' ORDER BY ReportYear DESC";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) years.add(rs.getInt(1));
            return years;
        } catch (SQLException ex) { throw failure(ex); }
    }

    @Override
    public PoliceSubmissionDTO findById(int id) {
        String sql = SUMMARY_SELECT + " WHERE ed.ExamDateId=? AND ed.PoliceStatus IN (N'PENDING',N'COMPLETED') "
                + "GROUP BY ed.ExamDateId,ed.ExamDate,l.LicenceClass,ed.PoliceStatus";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? mapSummary(rs) : null; }
        } catch (SQLException ex) { throw failure(ex); }
    }

    @Override
    public List<PoliceCandidateDTO> findCandidates(int examDateId) {
        return findCandidates(examDateId, 0, Integer.MAX_VALUE);
    }

    @Override
    public List<PoliceCandidateDTO> findCandidates(int examDateId, int offset, int limit) {
        String sql = "SELECT rd.RegistrationDateId,rd.ExamRegistrationId,rd.PoliceStatus,"
                + "rd.PoliceReason,rd.OfficialCandidateNumber,er.IsRetake FROM RegistrationDates rd "
                + "JOIN ExamDates ed ON ed.ExamDateId=rd.ExamDateId "
                + "JOIN ExamRegistration er ON er.ExamRegistrationId=rd.ExamRegistrationId "
                + "WHERE rd.ExamDateId=? AND (rd.IsActive=1 OR rd.PoliceStatus=N'REJECTED') "
                + "AND ed.PoliceStatus IN(N'PENDING',N'COMPLETED') ORDER BY rd.RegistrationDateId "
                + "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        List<PoliceCandidateDTO> rows = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examDateId);
            ps.setInt(2, Math.max(0, offset));
            ps.setInt(3, limit == Integer.MAX_VALUE ? Integer.MAX_VALUE : Math.max(1, Math.min(limit, 100)));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PoliceCandidateDTO row = new PoliceCandidateDTO();
                    row.setRegistrationDateId(rs.getInt(1));
                    row.setExamRegistrationId(rs.getInt(2));
                    row.setPoliceStatus(rs.getString(3));
                    row.setPoliceReason(rs.getString(4));
                    row.setOfficialCandidateNumber(rs.getString(5));
                    row.setRetake(rs.getBoolean(6));
                    rows.add(row);
                }
            }
            return rows;
        } catch (SQLException ex) { throw failure(ex); }
    }

    @Override
    public int countCandidates(int examDateId) {
        String sql = "SELECT COUNT(*) FROM RegistrationDates rd JOIN ExamDates ed "
                + "ON ed.ExamDateId=rd.ExamDateId WHERE rd.ExamDateId=? "
                + "AND (rd.IsActive=1 OR rd.PoliceStatus=N'REJECTED') "
                + "AND ed.PoliceStatus IN(N'PENDING',N'COMPLETED')";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examDateId);
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt(1); }
        } catch (SQLException ex) { throw failure(ex); }
    }

    @Override
    public int reviewCandidate(int registrationDateId, String decision, String reason,
            String participationType) {
        String normalized = "APPROVED".equalsIgnoreCase(decision) ? "APPROVED" : "REJECTED";
        if ("REJECTED".equals(normalized) && (reason == null || reason.trim().length() < 3)) {
            throw new IllegalArgumentException("Phải nhập lý do khi từ chối hồ sơ.");
        }
        Connection connection = getConnection();
        boolean previousAutoCommit = true;
        try {
            previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            int registrationId = 0;
            String sql = "UPDATE rd SET PoliceStatus=?,PoliceReason=?,"
                    + "IsActive=CASE WHEN ?=N'REJECTED' THEN 0 ELSE IsActive END "
                    + "OUTPUT inserted.ExamRegistrationId "
                    + "FROM RegistrationDates rd JOIN ExamDates ed ON ed.ExamDateId=rd.ExamDateId "
                    + "WHERE rd.RegistrationDateId=? AND rd.IsActive=1 "
                    + "AND rd.PoliceStatus=N'PENDING' AND ed.PoliceStatus=N'PENDING'";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, normalized);
                if ("REJECTED".equals(normalized)) ps.setString(2, reason.trim());
                else ps.setNull(2, java.sql.Types.NVARCHAR);
                ps.setString(3, normalized);
                ps.setInt(4, registrationDateId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) registrationId = rs.getInt(1);
                }
            }
            if (registrationId <= 0) {
                connection.rollback();
                return 0;
            }

            if ("APPROVED".equals(normalized)) {
                saveOfficialDecision(connection, registrationDateId, participationType);
            } else {
                try (PreparedStatement ps = connection.prepareStatement("""
                        DELETE official
                        FROM OfficialExamCandidate official
                        JOIN RegistrationDates selected
                          ON selected.ExamDateId=official.ExamDateId
                         AND selected.ExamRegistrationId=official.ExamRegistrationId
                        WHERE selected.RegistrationDateId=?
                        """)) {
                    ps.setInt(1, registrationDateId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = connection.prepareStatement("""
                        UPDATE ExamRegistration
                        SET RegistrationStatus=N'Rejected',
                            Notes=CONCAT(
                                CASE WHEN Notes IS NULL OR LTRIM(RTRIM(Notes))='' THEN ''
                                     ELSE Notes + ';' END,
                                N'#POLICE_REJECT# ',?
                            )
                        WHERE ExamRegistrationId=?
                        """)) {
                    ps.setString(1, reason.trim());
                    ps.setInt(2, registrationId);
                    if (ps.executeUpdate() != 1) {
                        throw new SQLException("Không cập nhật được trạng thái hồ sơ bị CSGT từ chối.");
                    }
                }
            }

            connection.commit();
            return registrationId;
        } catch (SQLException ex) {
            try {
                connection.rollback();
            } catch (SQLException ignored) {
                // Giữ lỗi nghiệp vụ gốc.
            }
            throw failure(ex);
        } catch (RuntimeException ex) {
            try {
                connection.rollback();
            } catch (SQLException ignored) {
                // Giữ lỗi nghiệp vụ gốc.
            }
            throw ex;
        } finally {
            try {
                connection.setAutoCommit(previousAutoCommit);
            } catch (SQLException ignored) {
                // Connection sẽ được DBContext xử lý ở lần truy cập tiếp theo.
            }
        }
    }

    private static void saveOfficialDecision(Connection connection, int registrationDateId,
            String requestedType) throws SQLException {
        String selectedType;
        boolean retake;
        try (PreparedStatement ps = connection.prepareStatement("""
                SELECT er.IsRetake
                FROM RegistrationDates rd
                JOIN ExamRegistration er ON er.ExamRegistrationId=rd.ExamRegistrationId
                WHERE rd.RegistrationDateId=?
                """)) {
            ps.setInt(1, registrationDateId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Không tìm thấy đăng ký cần thẩm định.");
                }
                retake = rs.getBoolean("IsRetake");
            }
        }
        if (!retake) {
            selectedType = "FULL_EXAM";
        } else if ("FULL_EXAM".equalsIgnoreCase(requestedType)
                || "PRACTICAL_ONLY".equalsIgnoreCase(requestedType)) {
            selectedType = requestedType.toUpperCase(java.util.Locale.ROOT);
        } else {
            throw new IllegalArgumentException(
                    "Hồ sơ thi lại phải được xác định thi toàn bộ hoặc chỉ thi thực hành.");
        }

        String update = """
                UPDATE official SET ExamParticipationType=?
                FROM OfficialExamCandidate official
                JOIN RegistrationDates selected
                  ON selected.ExamDateId=official.ExamDateId
                 AND selected.ExamRegistrationId=official.ExamRegistrationId
                WHERE selected.RegistrationDateId=?
                """;
        int updated;
        try (PreparedStatement ps = connection.prepareStatement(update)) {
            ps.setString(1, selectedType);
            ps.setInt(2, registrationDateId);
            updated = ps.executeUpdate();
        }
        if (updated > 0) {
            return;
        }

        String insert = """
                INSERT INTO OfficialExamCandidate
                  (ExamDateId,ExamRegistrationId,LicenceId,FullName,DateOfBirth,
                   GovernmentIdNumber,PhoneNumber,Email,SourceUnitCode,SourceUnitName,
                   ExamParticipationType)
                SELECT rd.ExamDateId,er.ExamRegistrationId,ed.LicenceId,p.FullName,
                       CAST(p.DateOfBirth AS date),p.GovernmentIdNumber,p.PhoneNumber,u.Email,
                       N'LAIVUI',N'Trung tâm sát hạch Lái Vui',?
                FROM RegistrationDates rd
                JOIN ExamDates ed ON ed.ExamDateId=rd.ExamDateId
                JOIN ExamRegistration er ON er.ExamRegistrationId=rd.ExamRegistrationId
                JOIN Profile p ON p.ProfileId=er.ProfileId
                JOIN [User] u ON u.UserId=p.UserId
                WHERE rd.RegistrationDateId=?
                """;
        try (PreparedStatement ps = connection.prepareStatement(insert)) {
            ps.setString(1, selectedType);
            ps.setInt(2, registrationDateId);
            if (ps.executeUpdate() != 1) {
                throw new SQLException("Không lưu được nội dung thi do CSGT quyết định.");
            }
        }
    }

    @Override
    public int completeSubmission(int examDateId) {
        java.sql.Connection c = getConnection();
        boolean auto = true;
        try {
            auto = c.getAutoCommit(); c.setAutoCommit(false);
            int pending;
            int approved;
            try (PreparedStatement ps = c.prepareStatement("SELECT "
                    + "SUM(CASE WHEN PoliceStatus=N'PENDING' THEN 1 ELSE 0 END),"
                    + "SUM(CASE WHEN PoliceStatus=N'APPROVED' THEN 1 ELSE 0 END) "
                    + "FROM RegistrationDates WITH(UPDLOCK,HOLDLOCK) WHERE ExamDateId=? "
                    + "AND (IsActive=1 OR PoliceStatus=N'REJECTED')")) {
                ps.setInt(1, examDateId);
                try (ResultSet rs = ps.executeQuery()) { rs.next(); pending=rs.getInt(1); approved=rs.getInt(2); }
            }
            if (pending > 0) throw new IllegalArgumentException("Vẫn còn " + pending + " hồ sơ chưa thẩm định.");
            if (approved == 0) throw new IllegalArgumentException("Danh sách không có hồ sơ nào được duyệt.");
            String syncSql = """
                    INSERT INTO OfficialExamCandidate
                      (ExamDateId,ExamRegistrationId,LicenceId,FullName,DateOfBirth,
                       GovernmentIdNumber,PhoneNumber,Email,SourceUnitCode,SourceUnitName,
                       ExamParticipationType)
                    SELECT rd.ExamDateId,er.ExamRegistrationId,ed.LicenceId,p.FullName,
                           CAST(p.DateOfBirth AS date),p.GovernmentIdNumber,p.PhoneNumber,u.Email,
                           N'LAIVUI',N'Trung tâm sát hạch Lái Vui',
                           CASE WHEN er.IsRetake=1 THEN N'PRACTICAL_ONLY' ELSE N'FULL_EXAM' END
                    FROM RegistrationDates rd
                    JOIN ExamDates ed ON ed.ExamDateId=rd.ExamDateId
                    JOIN ExamRegistration er ON er.ExamRegistrationId=rd.ExamRegistrationId
                    JOIN Profile p ON p.ProfileId=er.ProfileId
                    JOIN [User] u ON u.UserId=p.UserId
                    WHERE rd.ExamDateId=? AND rd.IsActive=1 AND rd.PoliceStatus=N'APPROVED'
                      AND NOT EXISTS(SELECT 1 FROM OfficialExamCandidate o
                        WHERE o.ExamDateId=rd.ExamDateId AND o.GovernmentIdNumber=p.GovernmentIdNumber)
                    """;
            try (PreparedStatement ps = c.prepareStatement(syncSql)) { ps.setInt(1, examDateId); ps.executeUpdate(); }
            String numberSql = """
                    WITH numbered AS (
                      SELECT OfficialExamCandidateId,ROW_NUMBER() OVER(
                        ORDER BY CASE WHEN ExamRegistrationId IS NOT NULL THEN 0 ELSE 1 END,
                                 OfficialExamCandidateId) RowNo
                      FROM OfficialExamCandidate
                      WHERE ExamDateId=? AND ExamRegistrationId IS NOT NULL
                    )
                    UPDATE o SET CandidateNumber=RIGHT('000'+CAST(numbered.RowNo AS varchar(10)),3)
                    FROM OfficialExamCandidate o JOIN numbered
                      ON numbered.OfficialExamCandidateId=o.OfficialExamCandidateId
                    """;
            try (PreparedStatement ps = c.prepareStatement(numberSql)) { ps.setInt(1, examDateId); ps.executeUpdate(); }
            try (PreparedStatement ps = c.prepareStatement("UPDATE rd SET OfficialCandidateNumber=o.CandidateNumber "
                    + "FROM RegistrationDates rd JOIN OfficialExamCandidate o "
                    + "ON o.ExamDateId=rd.ExamDateId AND o.ExamRegistrationId=rd.ExamRegistrationId "
                    + "WHERE rd.ExamDateId=?")) { ps.setInt(1, examDateId); ps.executeUpdate(); }
            try (PreparedStatement ps = c.prepareStatement("UPDATE ExamDates SET PoliceStatus=N'COMPLETED' "
                    + "WHERE ExamDateId=? AND PoliceStatus=N'PENDING'")) {
                ps.setInt(1, examDateId);
                if (ps.executeUpdate()!=1) throw new IllegalArgumentException("Danh sách không còn ở trạng thái chờ xử lý.");
            }
            int total;
            try (PreparedStatement ps=c.prepareStatement("SELECT COUNT(*) FROM OfficialExamCandidate "
                    + "WHERE ExamDateId=? AND ExamRegistrationId IS NOT NULL")) {
                ps.setInt(1,examDateId); try(ResultSet rs=ps.executeQuery()){rs.next();total=rs.getInt(1);}
            }
            c.commit(); return total;
        } catch (SQLException ex) {
            try { c.rollback(); } catch (SQLException ignored) { }
            throw failure(ex);
        } catch (RuntimeException ex) {
            try { c.rollback(); } catch (SQLException ignored) { }
            throw ex;
        } finally { try { c.setAutoCommit(auto); } catch (SQLException ignored) { } }
    }

    @Override
    public List<OfficialExamCandidateDTO> findOfficialCandidates(int examDateId) {
        return findOfficialCandidates(examDateId, 0, Integer.MAX_VALUE);
    }

    @Override
    public List<OfficialExamCandidateDTO> findOfficialCandidates(int examDateId, int offset, int limit) {
        String sql = """
                SELECT q.* FROM (
                  SELECT o.OfficialExamCandidateId,o.ExamDateId,o.ExamRegistrationId,
                         rd.RegistrationDateId,o.LicenceId,o.CandidateNumber,o.FullName,o.DateOfBirth,
                         o.GovernmentIdNumber,o.PhoneNumber,o.Email,
                         o.SourceUnitCode,o.SourceUnitName,l.LicenceClass,o.ExamParticipationType
                  FROM OfficialExamCandidate o
                  JOIN ExamDates ed ON ed.ExamDateId=o.ExamDateId
                  JOIN Licence l ON l.LicenceId=o.LicenceId
                  LEFT JOIN RegistrationDates rd ON rd.ExamDateId=o.ExamDateId
                    AND rd.ExamRegistrationId=o.ExamRegistrationId AND rd.IsActive=1
                  WHERE o.ExamDateId=? AND o.ExamRegistrationId IS NOT NULL
                    AND ed.PoliceStatus=N'COMPLETED'
                  UNION ALL
                  SELECT 0,rd.ExamDateId,er.ExamRegistrationId,rd.RegistrationDateId,ed.LicenceId,NULL,
                         p.FullName,CAST(p.DateOfBirth AS date),p.GovernmentIdNumber,
                         p.PhoneNumber,u.Email,N'LAIVUI',N'Trung tâm sát hạch Lái Vui',
                         l.LicenceClass,
                         COALESCE(o.ExamParticipationType,
                           CASE WHEN er.IsRetake=1 THEN N'PRACTICAL_ONLY' ELSE N'FULL_EXAM' END)
                  FROM RegistrationDates rd
                  JOIN ExamDates ed ON ed.ExamDateId=rd.ExamDateId
                  JOIN Licence l ON l.LicenceId=ed.LicenceId
                  JOIN ExamRegistration er ON er.ExamRegistrationId=rd.ExamRegistrationId
                  JOIN Profile p ON p.ProfileId=er.ProfileId
                  JOIN [User] u ON u.UserId=p.UserId
                  LEFT JOIN OfficialExamCandidate o ON o.ExamDateId=rd.ExamDateId
                    AND o.ExamRegistrationId=rd.ExamRegistrationId
                  WHERE rd.ExamDateId=? AND rd.IsActive=1
                    AND rd.PoliceStatus=N'APPROVED' AND ed.PoliceStatus=N'PENDING'
                ) q
                ORDER BY CASE WHEN q.CandidateNumber IS NULL THEN 1 ELSE 0 END,
                         q.CandidateNumber,q.ExamRegistrationId
                OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
                """;
        List<OfficialExamCandidateDTO> rows=new ArrayList<>();
        try(PreparedStatement ps=getConnection().prepareStatement(sql)){
            ps.setInt(1,examDateId);
            ps.setInt(2,examDateId);
            ps.setInt(3, Math.max(0, offset));
            ps.setInt(4, limit == Integer.MAX_VALUE ? Integer.MAX_VALUE : Math.max(1, Math.min(limit, 200)));
            try(ResultSet rs=ps.executeQuery()){while(rs.next()) rows.add(mapOfficial(rs));}
            return rows;
        }catch(SQLException ex){throw failure(ex);}
    }

    @Override
    public int countOfficialCandidates(int examDateId) {
        String sql = """
                SELECT COUNT(*) FROM (
                  SELECT o.ExamRegistrationId
                  FROM OfficialExamCandidate o JOIN ExamDates ed ON ed.ExamDateId=o.ExamDateId
                  WHERE o.ExamDateId=? AND o.ExamRegistrationId IS NOT NULL AND ed.PoliceStatus=N'COMPLETED'
                  UNION ALL
                  SELECT rd.ExamRegistrationId
                  FROM RegistrationDates rd JOIN ExamDates ed ON ed.ExamDateId=rd.ExamDateId
                  WHERE rd.ExamDateId=? AND rd.IsActive=1 AND rd.PoliceStatus=N'APPROVED'
                    AND ed.PoliceStatus=N'PENDING'
                ) q
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examDateId); ps.setInt(2, examDateId);
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt(1); }
        } catch (SQLException ex) { throw failure(ex); }
    }

    @Override
    public List<String> findActiveManagingStaffEmails() {
        String sql = "SELECT DISTINCT u.Email FROM [User] u JOIN [Role] r ON r.RoleId=u.RoleId "
                + "WHERE u.IsActive=1 AND u.Email IS NOT NULL AND LTRIM(RTRIM(u.Email))<>'' "
                + "AND r.RoleName IN(N'Cán bộ quản lý',N'ManagingStaff') ORDER BY u.Email";
        List<String> emails = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) emails.add(rs.getString(1));
            return emails;
        } catch (SQLException ex) { throw failure(ex); }
    }

    @Override
    public boolean canAccessDocument(int documentId) {
        String sql = "SELECT 1 FROM Document d JOIN ExamRegistration er ON er.ProfileId=d.ProfileId "
                + "JOIN RegistrationDates rd ON rd.ExamRegistrationId=er.ExamRegistrationId "
                + "JOIN ExamDates ed ON ed.ExamDateId=rd.ExamDateId WHERE d.DocumentId=? "
                + "AND (rd.IsActive=1 OR rd.PoliceStatus=N'REJECTED') "
                + "AND ed.PoliceStatus IN(N'PENDING',N'COMPLETED')";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, documentId);
            try (ResultSet rs=ps.executeQuery()) { return rs.next(); }
        } catch (SQLException ex) { throw failure(ex); }
    }

    private static PoliceSubmissionDTO mapSummary(ResultSet rs) throws SQLException {
        PoliceSubmissionDTO row = new PoliceSubmissionDTO();
        row.setExamDateId(rs.getInt("ExamDateId")); row.setExamDate(rs.getDate("ExamDate"));
        row.setLicenceClass(rs.getString("LicenceClass")); row.setPoliceStatus(rs.getString("PoliceStatus"));
        row.setTotalCandidates(rs.getInt("TotalCandidates")); row.setPendingCandidates(rs.getInt("PendingCandidates"));
        row.setApprovedCandidates(rs.getInt("ApprovedCandidates")); row.setRejectedCandidates(rs.getInt("RejectedCandidates"));
        return row;
    }

    private static OfficialExamCandidateDTO mapOfficial(ResultSet rs)throws SQLException{
        OfficialExamCandidateDTO x=new OfficialExamCandidateDTO();
        x.setId(rs.getInt("OfficialExamCandidateId")); x.setExamDateId(rs.getInt("ExamDateId"));
        int registrationId=rs.getInt("ExamRegistrationId"); x.setExamRegistrationId(rs.wasNull()?null:registrationId);
        int registrationDateId=rs.getInt("RegistrationDateId");
        x.setRegistrationDateId(rs.wasNull()?null:registrationDateId);
        x.setCandidateNumber(rs.getString("CandidateNumber")); x.setFullName(rs.getString("FullName"));
        x.setDateOfBirth(rs.getDate("DateOfBirth")); x.setGovernmentIdNumber(rs.getString("GovernmentIdNumber"));
        x.setLicenceClass(rs.getString("LicenceClass")); x.setPhoneNumber(rs.getString("PhoneNumber"));
        x.setEmail(rs.getString("Email")); x.setSourceUnitCode(rs.getString("SourceUnitCode"));
        x.setSourceUnitName(rs.getString("SourceUnitName"));
        x.setExamParticipationType(rs.getString("ExamParticipationType")); return x;
    }

    private static IllegalStateException failure(SQLException ex) {
        return new IllegalStateException("Không thể xử lý danh sách CSGT: " + ex.getMessage(), ex);
    }
}
