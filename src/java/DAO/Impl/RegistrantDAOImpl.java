package dao.impl;

import dao.DocumentDAO;
import dao.RegistrantDAO;
import dao.impl.DocumentDAOImpl;
import dbconnection.DBContext;
import dto.registrant.RegistrantDashboardActivity;
import dto.registrant.RegistrantExamSessionOption;
import dto.registrant.RegistrantLicenceOption;
import dto.registrant.RegistrantMyExamRow;
import dto.registrant.RegistrantRegisteredExamRow;
import dto.registrant.RegistrantTrackingLog;
import util.registrant.RegistrantDocumentHelper;
import enums.registrant.ProfileRegistrationStatus;
import util.registrant.RegistrantDocumentStatusHelper;
import util.registrant.RegistrantExamSupport;
import util.registrant.RegistrantTrackingCategories;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Triển khai truy vấn dữ liệu cho cổng thí sinh.
 * Các câu SQL JOIN nhiều bảng (Candidate, Exam, Payment, ExamResult...)
 * được gom tại đây để servlet/service chỉ gọi một lớp dao.
 */
public class RegistrantDAOImpl extends DBContext implements RegistrantDAO {

    private static final Logger LOG = Logger.getLogger(RegistrantDAOImpl.class.getName());

    private final DocumentDAO documentdao = new DocumentDAOImpl();

    /** JOIN lấy một phòng thi đại diện mỗi ca — tránh nhân bản dòng khi ca có nhiều ExamArea. */
    private static final String SESSION_AREA_JOIN = """
            LEFT JOIN (
                SELECT sea2.SessionId, MIN(sea2.ExamAreaId) AS ExamAreaId
                FROM Session_ExamArea sea2
                GROUP BY sea2.SessionId
            ) sea ON sea.SessionId = s.SessionId
            LEFT JOIN ExamArea ea ON ea.ExamAreaId = sea.ExamAreaId
            """;

    /** Subquery đánh dấu thí sinh đã thanh toán — tái sử dụng cho mọi truy vấn registrant. */
    private static final String PAYMENT_COMPLETED_JOIN = """
            LEFT JOIN (
                SELECT p1.CandidateId, MIN(p1.PaymentId) AS PaymentId
                FROM Payment p1
                WHERE p1.PaymentStatus IN (N'Completed', N'Paid')
                GROUP BY p1.CandidateId
            ) pay ON pay.CandidateId = c.CandidateId
            """;

    @Override
    public List<RegistrantLicenceOption> listOpenLicenceOptions() {
        /*
         * Lấy toàn bộ hạng GPLX từ bảng Licence — không giới hạn A1/B2 cố định.
         * Mã UI (A2/B2) được map từ mã DB (A/B) qua RegistrantExamSupport.
         */
        String sql = """
                SELECT LicenceClass, Description
                FROM Licence
                ORDER BY LicenceClass
                """;
        List<RegistrantLicenceOption> options = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String dbCode = rs.getString("LicenceClass");
                RegistrantLicenceOption opt = new RegistrantLicenceOption();
                String uiCode = RegistrantExamSupport.toUiLicenceCode(dbCode);
                opt.setCode(uiCode);
                opt.setName(rs.getString("Description"));
                opt.setExamFee(RegistrantExamSupport.defaultExamFee(uiCode));
                opt.setVehicleType(RegistrantExamSupport.inferVehicleType(uiCode));
                options.add(opt);
            }
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Không tải danh sách hạng GPLX: {0}", e.getMessage());
        }
        return options;
    }

    @Override
    public List<RegistrantExamSessionOption> listOpenExamSessionsByLicenceCode(String uiLicenceCode) {
        String dbCode = RegistrantExamSupport.toDbLicenceCode(uiLicenceCode);
        /*
         * Mỗi Exam (đợt thi) có thể có nhiều Session (ca).
         * UI chỉ hiển thị một dòng/ExamCode; sessionId lấy ca đầu tiên còn mở.
         */
        String sql = """
                SELECT e.ExamId,
                       e.ExamCode,
                       e.ExamDate,
                       e.CentreName,
                       l.LicenceClass,
                       s.SessionId,
                       s.SessionName,
                       s.[Status] AS sessionStatus,
                       ISNULL(ea.Capacity, 100) AS capacity,
                       (SELECT COUNT(*) FROM Exam_Candidate ec WHERE ec.SessionId = s.SessionId) AS registeredCount
                FROM Exam e
                INNER JOIN Licence l ON l.LicenceId = e.LicenceId
                INNER JOIN [Session] s ON s.ExamId = e.ExamId
                """ + SESSION_AREA_JOIN + """
                WHERE l.LicenceClass = ?
                  AND e.[Status] IN (N'Open', N'Scheduled')
                  AND s.[Status] IN (N'Open', N'Scheduled', N'InProgress')
                  AND CAST(e.ExamDate AS DATE) >= CAST(GETDATE() AS DATE)
                ORDER BY e.ExamDate, s.SessionId
                """;
        Map<String, RegistrantExamSessionOption> uniqueByExamCode = new LinkedHashMap<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, dbCode);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RegistrantExamSessionOption opt = mapSessionOption(rs);
                    uniqueByExamCode.putIfAbsent(opt.getExamCode(), opt);
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Không tải đợt thi hạng {0}: {1}",
                    new Object[] { uiLicenceCode, e.getMessage() });
        }
        return new ArrayList<>(uniqueByExamCode.values());
    }

    @Override
    public RegistrantExamSessionOption findExamSessionByCode(String examCode) {
        if (examCode == null || examCode.isBlank()) {
            return null;
        }
        String sql = """
                SELECT TOP 1 e.ExamId,
                       e.ExamCode,
                       e.ExamDate,
                       e.CentreName,
                       l.LicenceClass,
                       s.SessionId,
                       s.SessionName,
                       s.[Status] AS sessionStatus,
                       ISNULL(ea.Capacity, 100) AS capacity,
                       (SELECT COUNT(*) FROM Exam_Candidate ec WHERE ec.SessionId = s.SessionId) AS registeredCount
                FROM Exam e
                INNER JOIN Licence l ON l.LicenceId = e.LicenceId
                INNER JOIN [Session] s ON s.ExamId = e.ExamId
                """ + SESSION_AREA_JOIN + """
                WHERE e.ExamCode = ?
                ORDER BY e.ExamDate
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, examCode.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapSessionOption(rs);
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Không tìm thấy đợt thi {0}: {1}", new Object[] { examCode, e.getMessage() });
        }
        return null;
    }

    @Override
    public List<RegistrantRegisteredExamRow> listRegisteredExamsByUserId(int userId, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 50));
        String sql = """
                SELECT TOP (?) c.CandidateId,
                       c.CandidateNumber,
                       e.ExamCode,
                       s.SessionName,
                       l.LicenceClass,
                       CAST(e.ExamDate AS DATE) AS examDate,
                """ + SESSION_SCHEDULE_COLUMNS + """
                       e.CentreName,
                       er.RegistrationStatus,
                       ec.SectionStatus,
                       CASE WHEN pay.PaymentId IS NULL THEN 0 ELSE 1 END AS paid
                FROM Candidate c
                INNER JOIN ExamRegistration er ON er.ExamRegistrationId = c.ExamRegistrationId
                INNER JOIN Exam_Candidate ec ON ec.CandidateId = c.CandidateId
                INNER JOIN [Session] s ON s.SessionId = ec.SessionId
                INNER JOIN Exam e ON e.ExamId = ec.ExamId
                INNER JOIN Licence l ON l.LicenceId = e.LicenceId
                """ + PAYMENT_COMPLETED_JOIN + """
                WHERE c.UserId = ?
                ORDER BY e.ExamDate DESC
                """;
        List<RegistrantRegisteredExamRow> rows = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, safeLimit);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(mapRegisteredExamRow(rs));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Không tải đợt thi đã đăng ký user {0}: {1}",
                    new Object[] { userId, e.getMessage() });
        }
        return rows;
    }

    @Override
    public List<RegistrantRegisteredExamRow> listRegisteredExamsByProfileId(int profileId, int limit) {
        if (profileId <= 0) {
            return List.of();
        }
        int safeLimit = Math.max(1, Math.min(limit, 50));
        String sql = """
                SELECT TOP (?) c.CandidateId,
                       c.CandidateNumber,
                       e.ExamCode,
                       s.SessionName,
                       l.LicenceClass,
                       CAST(e.ExamDate AS DATE) AS examDate,
                """ + SESSION_SCHEDULE_COLUMNS + """
                       e.CentreName,
                       er.RegistrationStatus,
                       ec.SectionStatus,
                       CASE WHEN pay.PaymentId IS NULL THEN 0 ELSE 1 END AS paid
                FROM Candidate c
                INNER JOIN ExamRegistration er ON er.ExamRegistrationId = c.ExamRegistrationId
                INNER JOIN Exam_Candidate ec ON ec.CandidateId = c.CandidateId
                INNER JOIN [Session] s ON s.SessionId = ec.SessionId
                INNER JOIN Exam e ON e.ExamId = ec.ExamId
                INNER JOIN Licence l ON l.LicenceId = e.LicenceId
                """ + PAYMENT_COMPLETED_JOIN + """
                WHERE er.ProfileId = ?
                ORDER BY e.ExamDate DESC
                """;
        List<RegistrantRegisteredExamRow> rows = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, safeLimit);
            ps.setInt(2, profileId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(mapRegisteredExamRow(rs));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Không tải đợt thi đã đăng ký profile {0}: {1}",
                    new Object[] { profileId, e.getMessage() });
        }
        return rows;
    }

    private static final String ACTIVE_EXAM_REGISTRATION_FILTER = """
                  AND er.RegistrationStatus NOT IN (N'Draft', N'Pending', N'Approved', N'Rejected',
                      N'RegistrationRejected', N'Cancelled')
            """;

    private static final String SESSION_SCHEDULE_COLUMNS = """
                       s.[Status] AS sessionStatus,
                       s.StartTime,
                       s.EndTime,
            """;

    @Override
    public List<RegistrantRegisteredExamRow> listActiveExamRegistrationsByProfileId(int profileId, int limit) {
        if (profileId <= 0) {
            return List.of();
        }
        int safeLimit = Math.max(1, Math.min(limit, 50));
        String sql = """
                SELECT TOP (?) c.CandidateId,
                       c.CandidateNumber,
                       e.ExamCode,
                       s.SessionName,
                       l.LicenceClass,
                       CAST(e.ExamDate AS DATE) AS examDate,
                """ + SESSION_SCHEDULE_COLUMNS + """
                       e.CentreName,
                       er.RegistrationStatus,
                       ec.SectionStatus,
                       CASE WHEN pay.PaymentId IS NULL THEN 0 ELSE 1 END AS paid
                FROM Candidate c
                INNER JOIN ExamRegistration er ON er.ExamRegistrationId = c.ExamRegistrationId
                INNER JOIN Exam_Candidate ec ON ec.CandidateId = c.CandidateId
                INNER JOIN [Session] s ON s.SessionId = ec.SessionId
                INNER JOIN Exam e ON e.ExamId = ec.ExamId
                INNER JOIN Licence l ON l.LicenceId = e.LicenceId
                """ + PAYMENT_COMPLETED_JOIN + """
                WHERE er.ProfileId = ?
                """ + ACTIVE_EXAM_REGISTRATION_FILTER + """
                ORDER BY e.ExamDate ASC
                """;
        List<RegistrantRegisteredExamRow> rows = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, safeLimit);
            ps.setInt(2, profileId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(mapRegisteredExamRow(rs));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Không tải đăng ký sát hạch đang hiệu lực profile {0}: {1}",
                    new Object[] { profileId, e.getMessage() });
        }
        return rows;
    }

    @Override
    public Map<String, Object> loadDashboardStats(int userId, int profileId) {
        /*
         * Gộp 3 COUNT vào một round-trip thay vì 3 query riêng lẻ.
         */
        String sql = """
                SELECT
                  (SELECT COUNT(DISTINCT ec.ExamCandidateId)
                   FROM ExamRegistration er
                   INNER JOIN Candidate c ON c.ExamRegistrationId = er.ExamRegistrationId
                   INNER JOIN Exam_Candidate ec ON ec.CandidateId = c.CandidateId
                   WHERE er.ProfileId = ?) AS registeredExams,
                  (SELECT COUNT(DISTINCT c.CandidateId)
                   FROM Candidate c
                   INNER JOIN Exam_Candidate ec ON ec.CandidateId = c.CandidateId
                   INNER JOIN ExamResult er ON er.ExamCandidateId = ec.ExamCandidateId
                   WHERE c.UserId = ?) AS examResults
                """;
        Map<String, Object> stats = new HashMap<>();
        stats.put("registeredExams", 0);
        stats.put("examResults", 0);
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, profileId > 0 ? profileId : 0);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    stats.put("registeredExams", rs.getInt("registeredExams"));
                    stats.put("examResults", rs.getInt("examResults"));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Không tải thống kê dashboard: {0}", e.getMessage());
        }
        stats.put("profileDocumentStatus", findProfileDocumentRegistrationStatus(profileId));
        return stats;
    }

    @Override
    public RegistrantRegisteredExamRow findUpcomingExamByUserId(int userId) {
        String sql = """
                SELECT TOP 1 c.CandidateId,
                       c.CandidateNumber,
                       e.ExamCode,
                       s.SessionName,
                       l.LicenceClass,
                       CAST(e.ExamDate AS DATE) AS examDate,
                """ + SESSION_SCHEDULE_COLUMNS + """
                       e.CentreName,
                       er.RegistrationStatus,
                       ec.SectionStatus,
                       CASE WHEN pay.PaymentId IS NULL THEN 0 ELSE 1 END AS paid
                FROM Candidate c
                INNER JOIN ExamRegistration er ON er.ExamRegistrationId = c.ExamRegistrationId
                INNER JOIN Exam_Candidate ec ON ec.CandidateId = c.CandidateId
                INNER JOIN [Session] s ON s.SessionId = ec.SessionId
                INNER JOIN Exam e ON e.ExamId = ec.ExamId
                INNER JOIN Licence l ON l.LicenceId = e.LicenceId
                """ + PAYMENT_COMPLETED_JOIN + """
                WHERE c.UserId = ?
                  AND CAST(e.ExamDate AS DATE) >= CAST(GETDATE() AS DATE)
                ORDER BY e.ExamDate ASC
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRegisteredExamRow(rs);
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Không tải kỳ thi sắp tới user {0}: {1}",
                    new Object[] { userId, e.getMessage() });
        }
        return null;
    }

    @Override
    public RegistrantRegisteredExamRow findUpcomingExamByProfileId(int profileId) {
        if (profileId <= 0) {
            return null;
        }
        String sql = """
                SELECT TOP 1 c.CandidateId,
                       c.CandidateNumber,
                       e.ExamCode,
                       s.SessionName,
                       l.LicenceClass,
                       CAST(e.ExamDate AS DATE) AS examDate,
                """ + SESSION_SCHEDULE_COLUMNS + """
                       e.CentreName,
                       er.RegistrationStatus,
                       ec.SectionStatus,
                       CASE WHEN pay.PaymentId IS NULL THEN 0 ELSE 1 END AS paid
                FROM Candidate c
                INNER JOIN ExamRegistration er ON er.ExamRegistrationId = c.ExamRegistrationId
                INNER JOIN Exam_Candidate ec ON ec.CandidateId = c.CandidateId
                INNER JOIN [Session] s ON s.SessionId = ec.SessionId
                INNER JOIN Exam e ON e.ExamId = ec.ExamId
                INNER JOIN Licence l ON l.LicenceId = e.LicenceId
                """ + PAYMENT_COMPLETED_JOIN + """
                WHERE er.ProfileId = ?
                  AND CAST(e.ExamDate AS DATE) >= CAST(GETDATE() AS DATE)
                ORDER BY e.ExamDate ASC
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, profileId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRegisteredExamRow(rs);
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Không tải kỳ thi sắp tới profile {0}: {1}",
                    new Object[] { profileId, e.getMessage() });
        }
        return null;
    }

    @Override
    public List<RegistrantDashboardActivity> listRecentActivities(int profileId, int limit) {
        /*
         * Tổng hợp hoạt động từ Audit + Payment + ExamRegistration
         * thay vì chỉ Audit (seed data có thể ít bản ghi Audit cho thí sinh).
         */
        List<RegistrantDashboardActivity> activities = new ArrayList<>();
        appendPaymentActivities(profileId, activities, limit);
        appendRegistrationActivities(profileId, activities, limit);
        activities.sort((a, b) -> {
            java.util.Date ta = a.getOccurredAt();
            java.util.Date tb = b.getOccurredAt();
            if (ta == null && tb == null) {
                return 0;
            }
            if (ta == null) {
                return 1;
            }
            if (tb == null) {
                return -1;
            }
            return tb.compareTo(ta);
        });
        if (activities.size() > limit) {
            return new ArrayList<>(activities.subList(0, limit));
        }
        return activities;
    }

    @Override
    public List<RegistrantMyExamRow> listMyExamsByUserId(int userId) {
        return queryMyExams(userId, null);
    }

    /**
     * SQL danh sách kỳ thi.
     * @param extraPredicate điều kiện bổ sung sau {@code UserId} (vd: {@code AND c.CandidateId = ?}), hoặc rỗng.
     */
    private String buildMyExamsSql(String extraPredicate) {
        return """
                SELECT c.CandidateId,
                       c.CandidateNumber,
                       s.SessionName,
                       CAST(e.ExamDate AS DATE) AS examDate,
                """ + SESSION_SCHEDULE_COLUMNS + """
                       l.LicenceClass,
                       ea.AreaName,
                       er.RegistrationStatus,
                       ec.SectionStatus,
                       (SELECT TOP 1 es.SectionName
                        FROM Session_ExamSection ses2
                        INNER JOIN ExamSection es ON es.ExamSectionId = ses2.ExamSectionId
                        WHERE ses2.SessionId = s.SessionId
                        ORDER BY ses2.ExamSectionId) AS sectionName,
                       CASE WHEN pay.PaymentId IS NULL THEN 0 ELSE 1 END AS paid,
                       theory.scoreVal AS theoryScore,
                       practical.scoreVal AS practicalScore,
                       road.scoreVal AS roadScore,
                       erOverall.IsPassed AS overallPassed
                FROM Candidate c
                INNER JOIN ExamRegistration er ON er.ExamRegistrationId = c.ExamRegistrationId
                INNER JOIN Exam_Candidate ec ON ec.CandidateId = c.CandidateId
                INNER JOIN [Session] s ON s.SessionId = ec.SessionId
                INNER JOIN Exam e ON e.ExamId = ec.ExamId
                INNER JOIN Licence l ON l.LicenceId = e.LicenceId
                """ + SESSION_AREA_JOIN + PAYMENT_COMPLETED_JOIN + """
                LEFT JOIN (
                    SELECT ec3.CandidateId, CAST(MAX(es.Score) AS INT) AS scoreVal
                    FROM Exam_Candidate ec3
                    JOIN ExamResult er2 ON er2.ExamCandidateId = ec3.ExamCandidateId
                    JOIN ExamScore es ON es.ExamResultId = er2.ExamResultId
                    JOIN ExamSection sec ON sec.ExamSectionId = es.ExamSectionId
                    WHERE sec.SectionName LIKE N'%Lý thuyết%' OR sec.SectionName LIKE '%Theory%'
                    GROUP BY ec3.CandidateId
                ) theory ON theory.CandidateId = c.CandidateId
                LEFT JOIN (
                    SELECT ec3.CandidateId, CAST(MAX(es.Score) AS INT) AS scoreVal
                    FROM Exam_Candidate ec3
                    JOIN ExamResult er2 ON er2.ExamCandidateId = ec3.ExamCandidateId
                    JOIN ExamScore es ON es.ExamResultId = er2.ExamResultId
                    JOIN ExamSection sec ON sec.ExamSectionId = es.ExamSectionId
                    WHERE sec.SectionName LIKE N'%Thực hành%' OR sec.SectionName LIKE '%Practical%'
                       OR sec.SectionName LIKE N'%Sa hình%'
                    GROUP BY ec3.CandidateId
                ) practical ON practical.CandidateId = c.CandidateId
                LEFT JOIN (
                    SELECT ec3.CandidateId, CAST(MAX(es.Score) AS INT) AS scoreVal
                    FROM Exam_Candidate ec3
                    JOIN ExamResult er2 ON er2.ExamCandidateId = ec3.ExamCandidateId
                    JOIN ExamScore es ON es.ExamResultId = er2.ExamResultId
                    JOIN ExamSection sec ON sec.ExamSectionId = es.ExamSectionId
                    WHERE sec.SectionName LIKE N'%Đường%' OR sec.SectionName LIKE '%Road%'
                    GROUP BY ec3.CandidateId
                ) road ON road.CandidateId = c.CandidateId
                LEFT JOIN (
                    SELECT ec4.CandidateId, MAX(CAST(er3.IsPassed AS INT)) AS IsPassed
                    FROM Exam_Candidate ec4
                    JOIN ExamResult er3 ON er3.ExamCandidateId = ec4.ExamCandidateId
                    GROUP BY ec4.CandidateId
                ) erOverall ON erOverall.CandidateId = c.CandidateId
                WHERE c.UserId = ?
                """ + extraPredicate + """
                ORDER BY e.ExamDate DESC
                """;
    }

    private List<RegistrantMyExamRow> queryMyExams(int userId, Integer candidateId) {
        String extra = candidateId != null ? " AND c.CandidateId = ?" : "";
        String sql = buildMyExamsSql(extra);
        List<RegistrantMyExamRow> rows = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            if (candidateId != null) {
                ps.setInt(2, candidateId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(mapMyExamRow(rs));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Không tải kỳ thi user {0}: {1}",
                    new Object[] { userId, e.getMessage() });
        }
        return rows;
    }

    @Override
    public RegistrantMyExamRow findMyExamByCandidateId(int userId, int candidateId) {
        List<RegistrantMyExamRow> rows = queryMyExams(userId, candidateId);
        return rows.isEmpty() ? null : rows.get(0);
    }

    @Override
    public Integer resolveUserIdByCandidateId(int candidateId) {
        if (candidateId <= 0) {
            return null;
        }
        String sql = "SELECT UserId FROM Candidate WHERE CandidateId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("UserId");
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Không tải UserId candidate {0}: {1}",
                    new Object[] { candidateId, e.getMessage() });
        }
        return null;
    }

    @Override
    public List<RegistrantTrackingLog> buildProfileTrackingLogs(int profileId, int userId) {
        List<RegistrantTrackingLog> logs = new ArrayList<>();

        // Bước 1: đăng ký tài khoản / hồ sơ
        appendTrackingLog(logs, "Tạo hồ sơ đăng ký", "Thí sinh", "approved", "Thành công",
                "Hồ sơ cá nhân đã được tạo trên hệ thống.", queryProfileCreatedAt(profileId),
                RegistrantTrackingCategories.PROFILE);

        // Bước 2: trạng thái ExamRegistration mới nhất
        loadRegistrationTracking(profileId, logs);

        // Bước 3: thanh toán gần nhất
        loadPaymentTracking(userId, logs);

        // Bước 4: nhật ký tài liệu đính kèm (upload / gửi duyệt / phê duyệt / từ chối)
        java.util.Date profileCreated = queryProfileCreatedAt(profileId);
        loadDocumentTracking(profileId, profileCreated, logs);

        logs.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));
        return logs;
    }

    @Override
    public int countExamResultsByUserId(int userId) {
        String sql = """
                SELECT COUNT(DISTINCT c.CandidateId) AS cnt
                FROM Candidate c
                INNER JOIN Exam_Candidate ec ON ec.CandidateId = c.CandidateId
                INNER JOIN ExamResult er ON er.ExamCandidateId = ec.ExamCandidateId
                WHERE c.UserId = ?
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("cnt");
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Đếm kết quả thi thất bại: {0}", e.getMessage());
        }
        return 0;
    }

    @Override
    public int getNextCandidateSequence(String dbLicenceClass) {
        /*
         * Chỉ đếm SBD theo format mới: {LicenceClass}-{4 chữ số} (khớp Db2Mappings.buildCandidateNumber).
         * Bỏ qua legacy seed kiểu "045", "123" không có tiền tố hạng.
         */
        String sql = """
                SELECT ISNULL(MAX(
                    TRY_CAST(SUBSTRING(c.CandidateNumber,
                        CHARINDEX('-', c.CandidateNumber) + 1, 10) AS INT)), 0) + 1 AS nextNo
                FROM Candidate c
                WHERE c.CandidateNumber LIKE ?
                  AND CHARINDEX('-', c.CandidateNumber) > 0
                """;
        String prefix = dbLicenceClass + "-%";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, prefix);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("nextNo");
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Không sinh số thứ tự SBD: {0}", e.getMessage());
        }
        return (int) (System.currentTimeMillis() % 9000) + 1000;
    }

    @Override
    public int resolveLicenceIdByUiCode(String uiLicenceCode) {
        String dbCode = RegistrantExamSupport.toDbLicenceCode(uiLicenceCode);
        String sql = "SELECT LicenceId FROM Licence WHERE LicenceClass = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, dbCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("LicenceId");
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Không resolve LicenceId: {0}", e.getMessage());
        }
        return -1;
    }

    @Override
    public String resolveLatestLicenceClassByProfileId(int profileId) {
        if (profileId <= 0) {
            return null;
        }
        String fromExam = queryLicenceClass("""
                SELECT TOP 1 l.LicenceClass
                FROM Candidate c
                INNER JOIN ExamRegistration er ON er.ExamRegistrationId = c.ExamRegistrationId
                INNER JOIN Exam_Candidate ec ON ec.CandidateId = c.CandidateId
                INNER JOIN Exam e ON e.ExamId = ec.ExamId
                INNER JOIN Licence l ON l.LicenceId = e.LicenceId
                WHERE er.ProfileId = ?
                  AND er.RegistrationStatus NOT IN (N'Draft', N'Pending', N'Approved', N'Rejected')
                ORDER BY c.CandidateId DESC
                """, profileId);
        if (fromExam != null) {
            return fromExam;
        }
        return queryLicenceClass("""
                SELECT TOP 1 l.LicenceClass
                FROM ExamRegistration er
                INNER JOIN Licence l ON l.LicenceId = er.LicenceId
                WHERE er.ProfileId = ?
                  AND er.RegistrationStatus NOT IN (N'Draft', N'Pending', N'Approved', N'Rejected')
                ORDER BY er.ExamRegistrationId DESC
                """, profileId);
    }

    private String queryLicenceClass(String sql, int profileId) {
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, profileId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return RegistrantExamSupport.toUiLicenceCode(rs.getString("LicenceClass"));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Không tải hạng GPLX hồ sơ {0}: {1}",
                    new Object[] { profileId, e.getMessage() });
        }
        return null;
    }

    @Override
    public String findProfileDocumentRegistrationStatus(int profileId) {
        String sql = """
                SELECT TOP 1 RegistrationStatus
                FROM ExamRegistration
                WHERE ProfileId = ?
                  AND RegistrationStatus IN (N'Draft', N'Pending', N'Approved', N'Rejected')
                  AND (Notes IS NULL OR Notes NOT LIKE N'%#SUPPLEMENT_DOC#%')
                ORDER BY ExamRegistrationId ASC
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, profileId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("RegistrationStatus");
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Không đọc RegistrationStatus hồ sơ gốc {0}: {1}",
                    new Object[] { profileId, e.getMessage() });
        }
        return fallbackLegacyProfileDocumentStatus(profileId);
    }

    private String fallbackLegacyProfileDocumentStatus(int profileId) {
        String sql = """
                SELECT TOP 1 RegistrationStatus
                FROM ExamRegistration
                WHERE ProfileId = ?
                  AND RegistrationStatus IN (N'Draft', N'Pending', N'Approved', N'Rejected')
                ORDER BY ExamRegistrationId DESC
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, profileId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("RegistrationStatus");
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Không đọc RegistrationStatus legacy {0}: {1}",
                    new Object[] { profileId, e.getMessage() });
        }
        return ProfileRegistrationStatus.DRAFT;
    }

    @Override
    public boolean hasOpenSupplementPending(int profileId) {
        return findPendingSupplementExamRegistrationId(profileId) != null;
    }

    @Override
    public Integer findPendingSupplementExamRegistrationId(int profileId) {
        String sql = """
                SELECT TOP 1 ExamRegistrationId
                FROM ExamRegistration
                WHERE ProfileId = ?
                  AND RegistrationStatus = N'Pending'
                  AND Notes LIKE N'%#SUPPLEMENT_DOC#%'
                ORDER BY ExamRegistrationId DESC
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, profileId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ExamRegistrationId");
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Không tải request bổ sung chờ duyệt profile {0}: {1}",
                    new Object[] { profileId, e.getMessage() });
        }
        return null;
    }

    @Override
    public int insertSupplementDocumentRegistration(int profileId, int licenceId, String status, String notes) {
        if (profileId <= 0 || licenceId <= 0 || status == null || status.isBlank()) {
            return 0;
        }
        String mergedNotes = RegistrantDocumentHelper.buildSupplementExamRegistrationNotes(
                stripSupplementMarkerPrefix(notes));
        if (notes != null && notes.contains(RegistrantDocumentHelper.MARK_SUPPLEMENT_DOC)) {
            mergedNotes = notes.trim();
        }
        String ins = """
                INSERT INTO ExamRegistration (RegistrationStatus, Notes, ProfileId, LicenceId)
                VALUES (?, ?, ?, ?)
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(ins, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, status.trim());
            ps.setString(2, mergedNotes);
            ps.setInt(3, profileId);
            ps.setInt(4, licenceId);
            if (ps.executeUpdate() <= 0) {
                return 0;
            }
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Không tạo ExamRegistration bổ sung profile {0}: {1}",
                    new Object[] { profileId, e.getMessage() });
        }
        return 0;
    }

    private static String stripSupplementMarkerPrefix(String notes) {
        if (notes == null || notes.isBlank()) {
            return null;
        }
        return notes.replace(RegistrantDocumentHelper.MARK_SUPPLEMENT_DOC, "").trim();
    }

    @Override
    public Map<Integer, String> mapSupplementRegistrationStatuses(int profileId) {
        Map<Integer, String> map = new java.util.LinkedHashMap<>();
        if (profileId <= 0) {
            return map;
        }
        String sql = """
                SELECT ExamRegistrationId, RegistrationStatus
                FROM ExamRegistration
                WHERE ProfileId = ?
                  AND Notes LIKE N'%#SUPPLEMENT_DOC#%'
                  AND RegistrationStatus IN (N'Draft', N'Pending', N'Approved', N'Rejected')
                ORDER BY ExamRegistrationId ASC
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, profileId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getInt("ExamRegistrationId"), rs.getString("RegistrationStatus"));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Không tải trạng thái request bổ sung profile {0}: {1}",
                    new Object[] { profileId, e.getMessage() });
        }
        return map;
    }

    @Override
    public boolean syncSupplementDocumentRegistration(int examRegistrationId, String status, String notes) {
        if (examRegistrationId <= 0 || status == null || status.isBlank()) {
            return false;
        }
        String sql = """
                UPDATE ExamRegistration
                SET RegistrationStatus = ?, Notes = ?
                WHERE ExamRegistrationId = ?
                  AND Notes LIKE N'%#SUPPLEMENT_DOC#%'
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, status.trim());
            ps.setString(2, notes != null ? notes : "");
            ps.setInt(3, examRegistrationId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Không cập nhật request bổ sung ER {0}: {1}",
                    new Object[] { examRegistrationId, e.getMessage() });
        }
        return false;
    }

    @Override
    public boolean syncProfileDocumentRegistration(int profileId, String status, String notes) {
        if (profileId <= 0 || status == null || status.isBlank()) {
            return false;
        }
        if (getConnection() == null) {
            LOG.log(Level.WARNING, "Không đồng bộ RegistrationStatus — mất kết nối DB (profile {0})", profileId);
            return false;
        }
        try {
            String markedNotes = RegistrantDocumentHelper.ensureProfileDocMarker(notes);
            int rows = updatePrimaryWorkflowRegistrationRows(profileId, status.trim(), markedNotes);
            if (rows > 0) {
                return true;
            }
            int licenceId = resolveLicenceIdForNewRegistration(profileId);
            if (licenceId <= 0) {
                return false;
            }
            return insertPrimaryRegistrationRow(profileId, licenceId, status.trim(), markedNotes);
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Không đồng bộ RegistrationStatus profile {0}: {1}",
                    new Object[] { profileId, e.getMessage() });
        }
        return false;
    }

    private int updatePrimaryWorkflowRegistrationRows(int profileId, String status, String notes)
            throws SQLException {
        String sql = """
                UPDATE ExamRegistration
                SET RegistrationStatus = ?, Notes = ?
                WHERE ProfileId = ?
                  AND RegistrationStatus IN (N'Draft', N'Pending', N'Approved', N'Rejected')
                  AND (Notes IS NULL OR Notes NOT LIKE N'%#SUPPLEMENT_DOC#%')
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, notes != null ? notes : "");
            ps.setInt(3, profileId);
            return ps.executeUpdate();
        }
    }

    private boolean insertPrimaryRegistrationRow(int profileId, int licenceId, String status, String notes)
            throws SQLException {
        String ins = """
                INSERT INTO ExamRegistration (RegistrationStatus, Notes, ProfileId, LicenceId)
                VALUES (?, ?, ?, ?)
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(ins)) {
            ps.setString(1, status);
            ps.setString(2, notes != null ? notes : "");
            ps.setInt(3, profileId);
            ps.setInt(4, licenceId);
            return ps.executeUpdate() > 0;
        }
    }

    private int resolveLicenceIdForNewRegistration(int profileId) throws SQLException {
        String sql = """
                SELECT TOP 1 er.LicenceId
                FROM ExamRegistration er
                WHERE er.ProfileId = ?
                ORDER BY er.ExamRegistrationId DESC
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, profileId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("LicenceId");
                }
            }
        }
        return resolveLicenceIdByUiCode("B2");
    }

    @Override
    public Integer daysUntil(java.util.Date examDate) {
        if (examDate == null) {
            return null;
        }
        // Interface khai báo java.util.Date; impl dùng java.sql.Date ở chỗ khác — chuyển an toàn sang LocalDate.
        LocalDate target;
        if (examDate instanceof Date) {
            target = ((Date) examDate).toLocalDate();
        } else {
            target = new Date(examDate.getTime()).toLocalDate();
        }
        long days = ChronoUnit.DAYS.between(LocalDate.now(), target);
        return (int) Math.max(0, days);
    }

    // ======================== Hàm map & helper nội bộ ========================

    private RegistrantExamSessionOption mapSessionOption(ResultSet rs) throws SQLException {
        RegistrantExamSessionOption opt = new RegistrantExamSessionOption();
        String examCode = rs.getString("ExamCode");
        opt.setId(examCode);
        opt.setExamCode(examCode);
        opt.setExamName(rs.getString("SessionName"));
        opt.setLicenceClass(RegistrantExamSupport.toUiLicenceCode(rs.getString("LicenceClass")));
        Date examDate = rs.getDate("ExamDate");
        if (examDate == null) {
            Timestamp examTs = rs.getTimestamp("ExamDate");
            if (examTs != null) {
                examDate = new Date(examTs.getTime());
            }
        }
        opt.setExamDate(examDate);
        opt.setLocation(rs.getString("CentreName"));
        int capacity = rs.getInt("capacity");
        int registered = rs.getInt("registeredCount");
        opt.setSlotsRemaining(Math.max(0, capacity - registered));
        opt.setSessionId(rs.getInt("SessionId"));
        return opt;
    }

    private RegistrantRegisteredExamRow mapRegisteredExamRow(ResultSet rs) throws SQLException {
        RegistrantRegisteredExamRow row = new RegistrantRegisteredExamRow();
        row.setId(rs.getInt("CandidateId"));
        row.setExamCode(rs.getString("ExamCode"));
        row.setExamName(rs.getString("SessionName"));
        row.setLicenceClass(RegistrantExamSupport.toUiLicenceCode(rs.getString("LicenceClass")));
        row.setLicenceClassDescription(
                RegistrantExamSupport.licenceClassDescription(row.getLicenceClass()));

        Date examDate = rs.getDate("examDate");
        if (examDate == null) {
            examDate = rs.getDate("ExamDate");
        }
        row.setExamDate(examDate);

        String sessionStatus = rs.getString("sessionStatus");
        Timestamp sessionStart = readOptionalTimestamp(rs, "StartTime");
        Timestamp sessionEnd = readOptionalTimestamp(rs, "EndTime");
        java.util.Date start = sessionStart != null ? new java.util.Date(sessionStart.getTime()) : null;
        java.util.Date end = sessionEnd != null ? new java.util.Date(sessionEnd.getTime()) : null;
        RegistrantExamSupport.applyPublishedSessionSchedule(row, sessionStatus, start, end);

        row.setLocation(rs.getString("CentreName"));

        String candidateNumber = rs.getString("CandidateNumber");
        row.setCandidateNumber(candidateNumber);
        row.setSbdPending(RegistrantExamSupport.isSbdPending(candidateNumber));

        String regStatus = rs.getString("RegistrationStatus");
        String sectionStatus = rs.getString("SectionStatus");
        RegistrantExamSupport.applyExamStatusBadge(row, candidateNumber, regStatus, sectionStatus);
        return row;
    }

    private static Timestamp readOptionalTimestamp(ResultSet rs, String column) throws SQLException {
        try {
            return rs.getTimestamp(column);
        } catch (SQLException e) {
            return null;
        }
    }

    private RegistrantMyExamRow mapMyExamRow(ResultSet rs) throws SQLException {
        RegistrantMyExamRow row = new RegistrantMyExamRow();
        row.setCandidateId(rs.getInt("CandidateId"));
        row.setExamTitle(rs.getString("SessionName"));
        row.setExamDate(rs.getDate("examDate"));
        String sessionStatus = rs.getString("sessionStatus");
        Timestamp sessionStart = readOptionalTimestamp(rs, "StartTime");
        Timestamp sessionEnd = readOptionalTimestamp(rs, "EndTime");
        java.util.Date start = sessionStart != null ? new java.util.Date(sessionStart.getTime()) : null;
        java.util.Date end = sessionEnd != null ? new java.util.Date(sessionEnd.getTime()) : null;
        RegistrantExamSupport.applyPublishedSessionSchedule(row, sessionStatus, start, end);
        row.setLicenceClass(RegistrantExamSupport.toUiLicenceCode(rs.getString("LicenceClass")));
        String rawSbd = rs.getString("CandidateNumber");
        row.setSbd(rawSbd);
        boolean sbdPending = RegistrantExamSupport.isSbdPending(rawSbd);
        row.setSbdPending(sbdPending);
        row.setSbdDisplay(RegistrantExamSupport.formatSbdForDisplay(rawSbd));
        row.setRoomName(rs.getString("AreaName") != null ? rs.getString("AreaName") : "Chưa xếp phòng");

        row.setPendingPayment(sbdPending);

        row.setTheoryScore(RegistrantExamSupport.toInteger(rs.getObject("theoryScore")));
        row.setPracticalScore(RegistrantExamSupport.toInteger(rs.getObject("practicalScore")));
        row.setRoadScore(RegistrantExamSupport.toInteger(rs.getObject("roadScore")));

        Integer overallPassed = RegistrantExamSupport.toInteger(rs.getObject("overallPassed"));
        String regStatus = rs.getString("RegistrationStatus");
        String sectionStatus = rs.getString("SectionStatus");
        row.setRegistrationStatus(regStatus);
        row.setExamSectionName(rs.getString("sectionName"));
        RegistrantExamSupport.applyMyExamStatus(row, rawSbd, regStatus, sectionStatus, overallPassed);
        RegistrantExamSupport.applyScorePresentation(row, null, null);
        RegistrantExamSupport.finalizeSessionTimeDisplay(row, sessionStatus, start, end, sectionStatus);
        return row;
    }

    private void appendPaymentActivities(int profileId, List<RegistrantDashboardActivity> out, int limit) {
        String sql = """
                SELECT TOP (?) p.TotalAmount, p.PaidAt, l.LicenceClass
                FROM Payment p
                INNER JOIN Candidate c ON c.CandidateId = p.CandidateId
                INNER JOIN ExamRegistration er ON er.ExamRegistrationId = c.ExamRegistrationId
                INNER JOIN Licence l ON l.LicenceId = er.LicenceId
                WHERE er.ProfileId = ?
                  AND p.PaymentStatus IN (N'Completed', N'Paid')
                ORDER BY p.PaidAt DESC
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, limit);
            ps.setInt(2, profileId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp paidAt = rs.getTimestamp("PaidAt");
                    RegistrantDashboardActivity act = new RegistrantDashboardActivity();
                    act.setColorClass("blue");
                    act.setIconPath("M2 10h20");
                    act.setTitle("Thanh toán lệ phí thành công");
                    act.setDesc(String.format("Lệ phí thi Hạng %s — %,.0f VNĐ đã được xử lý",
                            RegistrantExamSupport.toUiLicenceCode(rs.getString("LicenceClass")),
                            rs.getDouble("TotalAmount")));
                    act.setTime(RegistrantExamSupport.formatActivityTime(paidAt));
                    act.setOccurredAt(paidAt);
                    out.add(act);
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Không tải hoạt động thanh toán: {0}", e.getMessage());
        }
    }

    private void appendRegistrationActivities(int profileId, List<RegistrantDashboardActivity> out, int limit) {
        String sql = """
                SELECT TOP (?) s.SessionName, l.LicenceClass, s.StartTime
                FROM Candidate c
                INNER JOIN ExamRegistration er ON er.ExamRegistrationId = c.ExamRegistrationId
                INNER JOIN Exam_Candidate ec ON ec.CandidateId = c.CandidateId
                INNER JOIN [Session] s ON s.SessionId = ec.SessionId
                INNER JOIN Exam e ON e.ExamId = ec.ExamId
                INNER JOIN Licence l ON l.LicenceId = e.LicenceId
                WHERE er.ProfileId = ?
                ORDER BY c.CandidateId DESC
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, limit);
            ps.setInt(2, profileId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp startTime = rs.getTimestamp("StartTime");
                    RegistrantDashboardActivity act = new RegistrantDashboardActivity();
                    act.setColorClass("green");
                    act.setIconPath("M20 6L9 17l-5-5");
                    act.setTitle("Đăng ký đợt thi thành công");
                    act.setDesc(String.format("Đã đăng ký tham gia %s — Hạng %s",
                            rs.getString("SessionName"),
                            RegistrantExamSupport.toUiLicenceCode(rs.getString("LicenceClass"))));
                    act.setTime(RegistrantExamSupport.formatActivityTime(startTime));
                    act.setOccurredAt(startTime);
                    out.add(act);
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Không tải hoạt động đăng ký: {0}", e.getMessage());
        }
    }

    private java.util.Date queryProfileCreatedAt(int profileId) {
        // Profile không có CreatedAt — dùng ExamRegistration đầu tiên làm mốc
        String sql = """
                SELECT TOP 1 er.ExamRegistrationId
                FROM ExamRegistration er WHERE er.ProfileId = ?
                ORDER BY er.ExamRegistrationId ASC
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, profileId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new java.util.Date();
                }
            }
        } catch (SQLException ignored) {
        }
        return new java.util.Date();
    }

    private void appendTrackingLog(List<RegistrantTrackingLog> logs, String title, String actor,
            String statusClass, String statusLabel, String remarks, java.util.Date ts, String category) {
        RegistrantTrackingLog log = new RegistrantTrackingLog();
        log.setEventTitle(title);
        log.setActorRole(actor);
        log.setStatusClass(statusClass);
        log.setStatusLabel(statusLabel);
        log.setRemarks(remarks);
        log.setTimestamp(ts);
        log.setCategory(category);
        logs.add(log);
    }

    private void loadRegistrationTracking(int profileId, List<RegistrantTrackingLog> logs) {
        String sql = """
                SELECT TOP 1 RegistrationStatus, Notes
                FROM ExamRegistration
                WHERE ProfileId = ?
                  AND RegistrationStatus IN (N'Draft', N'Pending', N'Approved', N'Rejected')
                ORDER BY ExamRegistrationId DESC
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, profileId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    appendRegistrationTrackingLog(logs, rs.getString("RegistrationStatus"), rs.getString("Notes"));
                    return;
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Không tải tracking registration: {0}", e.getMessage());
        }

        String fallback = """
                SELECT TOP 1 RegistrationStatus, Notes
                FROM ExamRegistration
                WHERE ProfileId = ?
                ORDER BY ExamRegistrationId DESC
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(fallback)) {
            ps.setInt(1, profileId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    appendRegistrationTrackingLog(logs, rs.getString("RegistrationStatus"), rs.getString("Notes"));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Không tải tracking registration (fallback): {0}", e.getMessage());
        }
    }

    private void appendRegistrationTrackingLog(List<RegistrantTrackingLog> logs, String status, String notes) {
        if ("Rejected".equalsIgnoreCase(status)) {
            appendTrackingLog(logs, "Kiểm duyệt hồ sơ tài liệu", "Ban quản lý",
                    "rejected", "Từ chối", notes != null ? notes : "Hồ sơ không đạt yêu cầu.",
                    new java.util.Date(), RegistrantTrackingCategories.DOCUMENT_REJECT);
        } else if ("Pending".equalsIgnoreCase(status)) {
            appendTrackingLog(logs, "Gửi hồ sơ chờ duyệt", "Thí sinh",
                    "pending", "Đang xử lý", notes != null ? notes : "Hồ sơ đang chờ duyệt.",
                    new java.util.Date(), RegistrantTrackingCategories.DOCUMENT_SUBMIT);
        } else if ("Approved".equalsIgnoreCase(status)) {
            appendTrackingLog(logs, "Phê duyệt hồ sơ tài liệu", "Ban quản lý",
                    "approved", "Thành công",
                    notes != null ? notes : "Tất cả giấy tờ bắt buộc đã được phê duyệt.",
                    new java.util.Date(), RegistrantTrackingCategories.DOCUMENT_APPROVE);
        } else {
            appendTrackingLog(logs, "Bổ sung hồ sơ tài liệu", "Thí sinh",
                    "info", "Đang bổ sung",
                    notes != null ? notes : "Đang tải và hoàn thiện giấy tờ bắt buộc.",
                    new java.util.Date(), RegistrantTrackingCategories.PROFILE);
        }
    }

    private void loadPaymentTracking(int userId, List<RegistrantTrackingLog> logs) {
        String sql = """
                SELECT TOP 1 p.TotalAmount, p.PaidAt, p.PaymentMethod, l.LicenceClass
                FROM Payment p
                INNER JOIN Candidate c ON c.CandidateId = p.CandidateId
                INNER JOIN ExamRegistration er ON er.ExamRegistrationId = c.ExamRegistrationId
                INNER JOIN Licence l ON l.LicenceId = er.LicenceId
                WHERE c.UserId = ?
                  AND p.PaymentStatus IN (N'Completed', N'Paid')
                ORDER BY p.PaidAt DESC
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    appendTrackingLog(logs, "Thanh toán lệ phí đăng ký",
                            rs.getString("PaymentMethod") != null ? rs.getString("PaymentMethod") : "Cổng thanh toán",
                            "approved", "Thành công",
                            String.format("Lệ phí sát hạch GPLX Hạng %s: %,.0f VNĐ đã được nhận.",
                                    RegistrantExamSupport.toUiLicenceCode(rs.getString("LicenceClass")),
                                    rs.getDouble("TotalAmount")),
                            rs.getTimestamp("PaidAt"), RegistrantTrackingCategories.PAYMENT);
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Không tải tracking payment: {0}", e.getMessage());
        }
    }

    private void loadDocumentTracking(int profileId, java.util.Date profileCreated,
            List<RegistrantTrackingLog> logs) {
        var docs = documentdao.listByProfileIdWithDocumentId(profileId);
        String regStatus = findProfileDocumentRegistrationStatus(profileId);
        logs.addAll(RegistrantDocumentStatusHelper.buildDocumentTrackingLogs(
                docs, documentdao.typeLabels(), profileCreated, regStatus));
    }
}
