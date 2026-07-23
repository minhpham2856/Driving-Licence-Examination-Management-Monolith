package registrant.dao.impl;

import registrant.dao.DocumentDAO;
import registrant.dao.RegistrantDAO;
import shared.dbconnection.DBContext;
import registrant.dto.RegistrantDashboardActivity;
import registrant.dto.RegistrantExamSessionOption;
import registrant.dto.RegistrantLicenceOption;
import registrant.dto.RegistrantMyExamRow;
import registrant.dto.RegistrantRegisteredExamRow;
import registrant.dto.RegistrantTrackingLog;
import examstaff.dao.Db2ExamSchemaSql;
import examstaff.enums.PaymentStatus;
import registrant.enums.ExamRegistrationLifecycleStatus;
import registrant.util.RegistrantDocumentHelper;
import registrant.enums.ProfileRegistrationStatus;
import registrant.util.RegistrantDocumentStatusHelper;
import registrant.util.RegistrantExamSupport;
import registrant.util.RegistrantTrackingCategories;
import java.sql.Connection;
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
 * DAO cổng thí sinh: hạng GPLX, đợt thi mở, đăng ký ExamRegistration, dashboard/tracking.
 * <p>
 * <b>Cách tìm:</b> Ctrl+F {@code REGION:} hoặc dùng <b>LOC index</b> bên dưới
 * (số dòng gần đúng — cập nhật lại khi sửa lớn). Cùng thứ tự {@link registrant.dao.RegistrantDAO}.
 * <p>
 * <b>LOC index (Impl):</b>
 * <ul>
 *   <li><b>~L121</b> Hạng GPLX —
 *       {@code listOpenLicenceOptions} ~L125;
 *       {@code resolveLicenceIdByUiCode} ~L153;
 *       {@code resolveLatestLicenceClassByProfileId} ~L175</li>
 *   <li><b>~L220</b> ExamDates / nguyện vọng —
 *       {@code listOpenExamSessionsByLicenceCode} ~L224;
 *       {@code findExamSessionByCode} ~L258;
 *       {@code registerPreferredExamDate} ~L296;
 *       {@code hasActivePreferredExamDate} ~L331</li>
 *   <li><b>~L486</b> Danh sách đăng ký thi —
 *       {@code listRegisteredExamsByUserId} ~L488;
 *       {@code listRegisteredExamsByProfileId} ~L498;
 *       {@code listActiveExamRegistrationsByProfileId} ~L625</li>
 *   <li><b>~L656</b> Dashboard —
 *       {@code loadDashboardStats} ~L658;
 *       {@code findUpcomingExamByUserId} ~L708;
 *       {@code findUpcomingExamByProfileId} ~L716;
 *       {@code listRecentActivities} ~L776;
 *       {@code daysUntil} ~L807</li>
 *   <li><b>~L823</b> My-exams —
 *       {@code listMyExamsByUserId} ~L831;
 *       {@code findMyExamByCandidateId} ~L951</li>
 *   <li><b>~L999</b> Hồ sơ tài liệu —
 *       {@code findProfileDocumentRegistrationStatus} ~L1003;
 *       {@code listApprovedDocumentLicenceCodes} ~L1031;
 *       {@code hasOpenSupplementPending} ~L1099;
 *       {@code insertSupplementDocumentRegistration} ~L1132;
 *       {@code insertLicenceDocumentRegistration} ~L1167;
 *       {@code mapSupplementRegistrationStatuses} ~L1205;
 *       {@code syncProfileDocumentRegistration} ~L1237</li>
 *   <li><b>~L1346</b> Track-profile — {@code buildProfileTrackingLogs} ~L1348</li>
 *   <li><b>~L1371</b> Helpers mapper; activity ~L1506; tracking ~L1623</li>
 * </ul>
 */
public class RegistrantDAOImpl extends DBContext implements RegistrantDAO {
    private static final Logger LOG = Logger.getLogger(RegistrantDAOImpl.class.getName());

    private final DocumentDAO documentdao = new DocumentDAOImpl();

    // Một ExamArea đại diện / kỳ thi (tránh nhân dòng khi nhiều area)
    private static final String SESSION_AREA_JOIN = """
            LEFT JOIN (
                SELECT eea2.ExamId, MIN(eea2.ExamAreaId) AS ExamAreaId
                FROM Exam_ExamArea eea2
                GROUP BY eea2.ExamId
            ) eea ON eea.ExamId = e.ExamId
            LEFT JOIN ExamArea ea ON ea.ExamAreaId = eea.ExamAreaId
            """;

    // Payment đã hoàn tất theo ExamEnrollmentId (optional — chỉ có sau khi staff enroll + thu phí desk).
    // Dùng cho my-exams / registered exams: pay.PaymentId != null ⇒ không còn pendingPayment.
    private static final String PAYMENT_COMPLETED_JOIN = """
            LEFT JOIN (
                SELECT p1.ExamEnrollmentId, MIN(p1.PaymentId) AS PaymentId
                FROM Payment p1
                WHERE p1.PaymentStatus IN (""" + PaymentStatus.sqlInClause() + """
                )
                GROUP BY p1.ExamEnrollmentId
            ) pay ON pay.ExamEnrollmentId = ee.ExamEnrollmentId
            """;

    private static final String EXAM_AREA_JOIN_EX = SESSION_AREA_JOIN.replace("e.ExamId", "ex.ExamId");

    private static final String PAYMENT_COMPLETED_STATUS_FILTER =
            "p.PaymentStatus IN (" + PaymentStatus.sqlInClause() + ")";

    private static final String SQL_AND_PAYMENT_COMPLETED =
            " AND " + PAYMENT_COMPLETED_STATUS_FILTER;

    // Exam còn mở đăng ký (VN seed + EN legacy + biến thể thường gặp)
    private static final String OPEN_EXAM_STATUS =
            "e.[Status] IN ("
                    + "N'Chưa diễn ra', N'Đang diễn ra', N'Mở đăng ký', N'Dang mo dang ky',"
                    + " N'Open', N'Scheduled', N'InProgress', N'RegistrationOpen'"
                    + ")";

    // =========================================================================
    // REGION: Hạng GPLX / resolve LicenceId  (~L121)
    // =========================================================================
    /** Query hạng đang mở đăng ký từ LicenceClass/ExamDates. */
    @Override
    public List<RegistrantLicenceOption> listOpenLicenceOptions() {
        /* Lấy hạng GPLX từ bảng Licence (seed: A, A1, B1) — hiển thị đúng mã DB qua toUiLicenceCode. */
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

    /** Resolve LicenceId từ mã hạng UI. */
    @Override
    public int resolveLicenceIdByUiCode(String uiLicenceCode) {
        String[] licenceCodes = RegistrantExamSupport.licenceClassLookupCodes(uiLicenceCode);
        String placeholders = String.join(", ", java.util.Collections.nCopies(licenceCodes.length, "?"));
        String sql = "SELECT TOP 1 LicenceId FROM Licence WHERE UPPER(LTRIM(RTRIM(LicenceClass))) IN ("
                + placeholders + ")";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            for (int i = 0; i < licenceCodes.length; i++) {
                ps.setString(i + 1, licenceCodes[i]);
            }
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

    /** Mã hạng UI mới nhất từ đăng ký/hồ sơ. */
    @Override
    public String resolveLatestLicenceClassByProfileId(int profileId) {
        if (profileId <= 0) {
            return null;
        }
        String fromExam = queryLicenceClass("""
                SELECT TOP 1 l.LicenceClass
                FROM Profile prof
                INNER JOIN Candidate c ON c.GovernmentIdNumber = prof.GovernmentIdNumber
                INNER JOIN ExamEnrollment ee ON ee.CandidateId = c.CandidateId
                INNER JOIN Exam ex ON ex.ExamId = ee.ExamId
                INNER JOIN Licence l ON l.LicenceId = ex.LicenceId
                INNER JOIN ExamRegistration er ON er.ProfileId = prof.ProfileId
                  AND er.RegistrationStatus NOT IN (N'Draft', N'Pending', N'Approved', N'Rejected')
                WHERE prof.ProfileId = ?
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

    // =========================================================================
    // REGION: Ngày thi dự kiến (ExamDates) & nguyện vọng (RegistrationDates)  (~L220)
    // =========================================================================
    /** Liệt kê ExamDates mở theo mã hạng UI. */
    @Override
    public List<RegistrantExamSessionOption> listOpenExamSessionsByLicenceCode(String uiLicenceCode) {
        String[] licenceCodes = RegistrantExamSupport.licenceClassLookupCodes(uiLicenceCode);
        String placeholders = String.join(", ", java.util.Collections.nCopies(licenceCodes.length, "?"));
        // Nguồn: ExamDates (ngày dự kiến managing staff), không lấy từ bảng Exam kỳ thi chính thức
        String sql = """
                SELECT ed.ExamDateId,
                       ed.ExamDate,
                       l.LicenceClass
                FROM ExamDates ed
                INNER JOIN Licence l ON l.LicenceId = ed.LicenceId
                WHERE UPPER(LTRIM(RTRIM(l.LicenceClass))) IN (""" + placeholders + """
                )
                  AND ed.ExamDate >= CAST(GETDATE() AS DATE)
                ORDER BY ed.ExamDate, ed.ExamDateId
                """;
        List<RegistrantExamSessionOption> options = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            for (int i = 0; i < licenceCodes.length; i++) {
                ps.setString(i + 1, licenceCodes[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    options.add(mapExamDateOption(rs));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Không tải ngày thi dự kiến hạng {0}: {1}",
                    new Object[] { uiLicenceCode, e.getMessage() });
        }
        return options;
    }

    /** Tìm ExamDate theo id/code từ tham số form. */
    @Override
    public RegistrantExamSessionOption findExamSessionByCode(String examDateIdOrCode) {
        Integer examDateId = parseExamDateId(examDateIdOrCode);
        if (examDateId == null || examDateId <= 0) {
            return null;
        }
        String sql = """
                SELECT ed.ExamDateId,
                       ed.ExamDate,
                       l.LicenceClass
                FROM ExamDates ed
                INNER JOIN Licence l ON l.LicenceId = ed.LicenceId
                WHERE ed.ExamDateId = ?
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examDateId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapExamDateOption(rs);
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Không tìm thấy ngày thi dự kiến {0}: {1}",
                    new Object[] { examDateIdOrCode, e.getMessage() });
        }
        return null;
    }

    /**
     * Ghi nhận ngày thi mong muốn của thí sinh (portal).
     * <p>
     * Đọc: ExamDates khớp Licence + còn mở; ExamRegistration Approved theo profile+licence.
     * Ghi: INSERT/MERGE {@code RegistrationDates} IsActive=1.
     * Mỗi profile+hạng chỉ một nguyện vọng active — không deactivate/đổi ngày đã chọn.
     * Không tạo Candidate / ExamEnrollment / Payment.
     *
     * @return {@code null} nếu OK; ngược lại message lỗi tiếng Việt
     */
    @Override
    public String registerPreferredExamDate(int profileId, int examDateId, int licenceId) {
        if (profileId <= 0 || examDateId <= 0 || licenceId <= 0 || getConnection() == null) {
            return "Không thể ghi nhận lựa chọn ngày thi. Vui lòng thử lại.";
        }
        try {
            if (!examDateMatchesLicence(examDateId, licenceId)) {
                return "Ngày thi không khớp hạng bằng đã chọn.";
            }
            if (!examDateIsOpen(examDateId)) {
                return "Ngày thi này không còn mở đăng ký.";
            }
            int examRegistrationId = resolveExamRegistrationForPreferredDate(profileId, licenceId);
            if (examRegistrationId <= 0) {
                return "Chưa có hồ sơ được duyệt cho hạng này. Vui lòng gửi yêu cầu duyệt trước.";
            }
            if (hasActiveSameExamDate(examRegistrationId, examDateId)) {
                return "Bạn đã chọn ngày thi này rồi.";
            }
            if (hasActivePreferredExamDate(profileId, licenceId)) {
                return "Bạn đã đăng ký ngày thi dự kiến cho hạng này. "
                        + "Không thể đổi sang ngày khác — vui lòng theo dõi tại Lịch thi & kết quả.";
            }
            if (!insertRegistrationDate(examRegistrationId, examDateId)) {
                return "Không thể ghi nhận lựa chọn ngày thi. Vui lòng thử lại sau.";
            }
            return null;
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Đăng ký ngày dự kiến thất bại profile {0} date {1}: {2}",
                    new Object[] { profileId, examDateId, e.getMessage() });
            return "Không thể ghi nhận lựa chọn ngày thi. Vui lòng thử lại sau.";
        }
    }

    /** True nếu hồ sơ đã có RegistrationDates IsActive=1 cho hạng này. */
    @Override
    public boolean hasActivePreferredExamDate(int profileId, int licenceId) {
        if (profileId <= 0 || licenceId <= 0 || getConnection() == null) {
            return false;
        }
        String sql = """
                SELECT TOP 1 1
                FROM RegistrationDates rd
                INNER JOIN ExamRegistration er ON er.ExamRegistrationId = rd.ExamRegistrationId
                WHERE er.ProfileId = ?
                  AND er.LicenceId = ?
                  AND rd.IsActive = 1
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, profileId);
            ps.setInt(2, licenceId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Kiểm tra nguyện vọng ngày thi thất bại profile {0}: {1}",
                    new Object[] { profileId, e.getMessage() });
            return false;
        }
    }

    private static Integer parseExamDateId(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String value = raw.trim();
        if (value.regionMatches(true, 0, "DK-", 0, 3)) {
            value = value.substring(3).trim();
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private RegistrantExamSessionOption mapExamDateOption(ResultSet rs) throws SQLException {
        RegistrantExamSessionOption opt = new RegistrantExamSessionOption();
        int examDateId = rs.getInt("ExamDateId");
        String code = "DK-" + examDateId;
        opt.setId(String.valueOf(examDateId));
        opt.setExamCode(code);
        opt.setExamName("Ngày thi dự kiến");
        opt.setLicenceClass(RegistrantExamSupport.toUiLicenceCode(rs.getString("LicenceClass")));
        Date examDate = rs.getDate("ExamDate");
        opt.setExamDate(examDate);
        opt.setLocation("Theo lịch trung tâm");
        opt.setSlotsRemaining(-1);
        opt.setSessionId(examDateId);
        return opt;
    }

    private boolean examDateMatchesLicence(int examDateId, int licenceId) throws SQLException {
        String sql = "SELECT 1 FROM ExamDates WHERE ExamDateId = ? AND LicenceId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examDateId);
            ps.setInt(2, licenceId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean examDateIsOpen(int examDateId) throws SQLException {
        String sql = """
                SELECT 1 FROM ExamDates
                WHERE ExamDateId = ?
                  AND ExamDate >= CAST(GETDATE() AS DATE)
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examDateId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private int resolveExamRegistrationForPreferredDate(int profileId, int licenceId) throws SQLException {
        String sql = """
                SELECT TOP 1 ExamRegistrationId
                FROM ExamRegistration
                WHERE ProfileId = ?
                  AND LicenceId = ?
                  AND RegistrationStatus = N'Approved'
                  AND (
                        Notes LIKE N'%#PROFILE_DOC#%'
                     OR Notes LIKE N'%#LICENCE_DOC#%'
                     OR Notes LIKE N'%#SUPPLEMENT_DOC#%'
                     OR Notes IS NULL
                     OR (
                            Notes NOT LIKE N'%#EXAM_ID#%'
                        AND Notes NOT LIKE N'%#SUPPLEMENT_DOC#%'
                        )
                  )
                ORDER BY ExamRegistrationId DESC
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, profileId);
            ps.setInt(2, licenceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ExamRegistrationId");
                }
            }
        }
        return 0;
    }

    private boolean hasActiveSameExamDate(int examRegistrationId, int examDateId) throws SQLException {
        String sql = """
                SELECT 1 FROM RegistrationDates
                WHERE ExamRegistrationId = ?
                  AND ExamDateId = ?
                  AND IsActive = 1
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examRegistrationId);
            ps.setInt(2, examDateId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * MERGE RegistrationDates: khóa (ExamRegistrationId, ExamDateId).
     * MATCHED → IsActive=1; NOT MATCHED → INSERT IsActive=1.
     * (Chỉ gọi khi chưa có nguyện vọng active khác cho cùng hạng.)
     */
    private boolean insertRegistrationDate(int examRegistrationId, int examDateId) throws SQLException {
        String sql = """
                MERGE RegistrationDates AS target
                USING (SELECT ? AS ExamRegistrationId, ? AS ExamDateId) AS src
                ON target.ExamRegistrationId = src.ExamRegistrationId
                   AND target.ExamDateId = src.ExamDateId
                WHEN MATCHED THEN
                    UPDATE SET IsActive = 1
                WHEN NOT MATCHED THEN
                    INSERT (ExamRegistrationId, ExamDateId, IsActive)
                    VALUES (src.ExamRegistrationId, src.ExamDateId, 1);
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examRegistrationId);
            ps.setInt(2, examDateId);
            return ps.executeUpdate() >= 0;
        }
    }

    /** Gộp nguyện vọng + lịch chính thức theo UserId. */

    // =========================================================================
    // REGION: Danh sách đăng ký thi (dashboard / hồ sơ)  (~L486)
    // =========================================================================
    public List<RegistrantRegisteredExamRow> listRegisteredExamsByUserId(int userId, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 50));
        List<RegistrantRegisteredExamRow> rows = new ArrayList<>();
        rows.addAll(listPreferredRegisteredExams("prof.UserId = ?", userId, safeLimit));
        rows.addAll(queryRegisteredExamsOfficial("prof.UserId = ?", userId, safeLimit));
        return trimByExamDateDesc(rows, safeLimit);
    }

    /** Gộp nguyện vọng + lịch chính thức theo ProfileId. */
    @Override
    public List<RegistrantRegisteredExamRow> listRegisteredExamsByProfileId(int profileId, int limit) {
        if (profileId <= 0) {
            return List.of();
        }
        int safeLimit = Math.max(1, Math.min(limit, 50));
        List<RegistrantRegisteredExamRow> rows = new ArrayList<>();
        rows.addAll(listPreferredRegisteredExams("prof.ProfileId = ?", profileId, safeLimit));
        rows.addAll(queryRegisteredExamsOfficial("prof.ProfileId = ?", profileId, safeLimit));
        return trimByExamDateDesc(rows, safeLimit);
    }

    private List<RegistrantRegisteredExamRow> queryRegisteredExamsOfficial(String ownerPredicate, int ownerId, int limit) {
        String sql = REGISTERED_EXAM_SELECT + REGISTERED_EXAM_FROM
                + " WHERE " + ownerPredicate
                + ExamRegistrationLifecycleStatus.SQL_ACTIVE_EXAM_REGISTRATION_FILTER
                + """
                ORDER BY ex.ExamDate DESC
                """;
        List<RegistrantRegisteredExamRow> rows = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, limit);
            ps.setInt(2, ownerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(mapRegisteredExamRow(rs));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Không tải đợt thi chính thức: {0}", e.getMessage());
        }
        return rows;
    }

    private List<RegistrantRegisteredExamRow> listPreferredRegisteredExams(String ownerPredicate, int ownerId, int limit) {
        String sql = """
                SELECT TOP (?)
                       rd.RegistrationDateId,
                       ed.ExamDateId,
                       CAST(ed.ExamDate AS DATE) AS examDate,
                       l.LicenceClass
                FROM RegistrationDates rd
                INNER JOIN ExamDates ed ON ed.ExamDateId = rd.ExamDateId
                INNER JOIN ExamRegistration er ON er.ExamRegistrationId = rd.ExamRegistrationId
                INNER JOIN Profile prof ON prof.ProfileId = er.ProfileId
                INNER JOIN Licence l ON l.LicenceId = ed.LicenceId
                """ + " WHERE " + ownerPredicate + """
                  AND rd.IsActive = 1
                ORDER BY ed.ExamDate DESC, rd.RegistrationDateId DESC
                """;
        List<RegistrantRegisteredExamRow> rows = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, limit);
            ps.setInt(2, ownerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(mapPreferredRegisteredExamRow(rs));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Không tải ngày thi nguyện vọng: {0}", e.getMessage());
        }
        return rows;
    }

    private static List<RegistrantRegisteredExamRow> trimByExamDateDesc(List<RegistrantRegisteredExamRow> rows, int limit) {
        rows.sort((a, b) -> {
            java.util.Date da = a.getExamDate();
            java.util.Date db = b.getExamDate();
            if (da == null && db == null) {
                return Integer.compare(b.getId(), a.getId());
            }
            if (da == null) {
                return 1;
            }
            if (db == null) {
                return -1;
            }
            int cmp = db.compareTo(da);
            return cmp != 0 ? cmp : Integer.compare(Math.abs(b.getId()), Math.abs(a.getId()));
        });
        if (rows.size() <= limit) {
            return rows;
        }
        return new ArrayList<>(rows.subList(0, limit));
    }

    private static final String SESSION_SCHEDULE_COLUMNS = """
                       ex.[Status] AS sessionStatus,
                       ex.StartTime,
                       ex.EndTime,
            """;

    // Portal: ExamRegistration + Exam (#EXAM_ID# trong Notes). CandidateId cột = ExamRegistrationId.
    // LEFT JOIN enrollment/payment chỉ khi staff đã tạo Candidate ngày thi.
    private static final String REGISTERED_EXAM_FROM = """
                FROM ExamRegistration er
                INNER JOIN Profile prof ON prof.ProfileId = er.ProfileId
                INNER JOIN Licence l ON l.LicenceId = er.LicenceId
                LEFT JOIN Exam ex ON er.Notes LIKE N'%#EXAM_ID#' + CAST(ex.ExamId AS NVARCHAR(20)) + N'#%'
                LEFT JOIN ExamEnrollment ee ON ee.ExamId = ex.ExamId
                  AND EXISTS (
                      SELECT 1 FROM Candidate c2
                      WHERE c2.CandidateId = ee.CandidateId
                        AND c2.GovernmentIdNumber = prof.GovernmentIdNumber
                  )
                """
            + Db2ExamSchemaSql.JOIN_THEORY_SECTION + """
                """
            + PAYMENT_COMPLETED_JOIN + """
            """;

    private static final String REGISTERED_EXAM_SELECT = """
                SELECT TOP (?) er.ExamRegistrationId AS CandidateId,
                       CAST(NULL AS NVARCHAR(50)) AS CandidateNumber,
                       ex.ExamCode,
                       ex.ExamCode AS SessionName,
                       l.LicenceClass,
                       CAST(ex.ExamDate AS DATE) AS examDate,
                """ + SESSION_SCHEDULE_COLUMNS + """
                       ex.CentreName,
                       er.RegistrationStatus,
                       theoryEes.Status AS sectionStatus,
                       CASE WHEN pay.PaymentId IS NULL THEN 0 ELSE 1 END AS paid
                """;

    /** ER lifecycle còn hiệu lực (không hủy/từ chối/hồ sơ doc). */
    @Override
    public List<RegistrantRegisteredExamRow> listActiveExamRegistrationsByProfileId(int profileId, int limit) {
        if (profileId <= 0) {
            return List.of();
        }
        int safeLimit = Math.max(1, Math.min(limit, 50));
        List<RegistrantRegisteredExamRow> rows = new ArrayList<>();
        rows.addAll(listPreferredRegisteredExams("prof.ProfileId = ?", profileId, safeLimit));
        rows.addAll(queryRegisteredExamsOfficial("prof.ProfileId = ?", profileId, safeLimit));
        rows.sort((a, b) -> {
            java.util.Date da = a.getExamDate();
            java.util.Date db = b.getExamDate();
            if (da == null && db == null) {
                return 0;
            }
            if (da == null) {
                return 1;
            }
            if (db == null) {
                return -1;
            }
            return da.compareTo(db);
        });
        if (rows.size() > safeLimit) {
            return new ArrayList<>(rows.subList(0, safeLimit));
        }
        return rows;
    }

    /** Tính số liệu thống kê cho dashboard thí sinh. */

    // =========================================================================
    // REGION: Dashboard — stats, upcoming, activity, daysUntil  (~L656)
    // =========================================================================
    public Map<String, Object> loadDashboardStats(int userId, int profileId) {
        /*
         * Gộp COUNT vào một round-trip: nguyện vọng + lifecycle ER + kết quả chính thức.
         */
        String sql = """
                SELECT
                  (
                    (SELECT COUNT(*)
                     FROM ExamRegistration er
                     WHERE er.ProfileId = ?
                """ + ExamRegistrationLifecycleStatus.SQL_AND_LIFECYCLE_ONLY
                + ExamRegistrationLifecycleStatus.SQL_AND_EXCLUDE_PROFILE_DOC + """
                    )
                    +
                    (SELECT COUNT(*)
                     FROM RegistrationDates rd
                     INNER JOIN ExamRegistration er2 ON er2.ExamRegistrationId = rd.ExamRegistrationId
                     WHERE er2.ProfileId = ?
                       AND rd.IsActive = 1)
                  ) AS registeredExams,
                  (SELECT COUNT(DISTINCT ee.ExamEnrollmentId)
                   FROM Profile prof
                   INNER JOIN Candidate c ON c.GovernmentIdNumber = prof.GovernmentIdNumber
                   INNER JOIN ExamEnrollment ee ON ee.CandidateId = c.CandidateId
                   INNER JOIN ExamResult er2 ON er2.ExamEnrollmentId = ee.ExamEnrollmentId
                   WHERE prof.UserId = ?) AS examResults
                """;
        Map<String, Object> stats = new HashMap<>();
        stats.put("registeredExams", 0);
        stats.put("examResults", 0);
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            int pid = profileId > 0 ? profileId : 0;
            ps.setInt(1, pid);
            ps.setInt(2, pid);
            ps.setInt(3, userId);
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

    /** Chọn kỳ thi sắp tới theo UserId. */
    @Override
    public RegistrantRegisteredExamRow findUpcomingExamByUserId(int userId) {
        return pickUpcomingExam(
                listPreferredRegisteredExams("prof.UserId = ?", userId, 20),
                queryUpcomingOfficial("prof.UserId = ?", userId));
    }

    /** Chọn kỳ thi sắp tới theo ProfileId. */
    @Override
    public RegistrantRegisteredExamRow findUpcomingExamByProfileId(int profileId) {
        if (profileId <= 0) {
            return null;
        }
        return pickUpcomingExam(
                listPreferredRegisteredExams("prof.ProfileId = ?", profileId, 20),
                queryUpcomingOfficial("prof.ProfileId = ?", profileId));
    }

    private RegistrantRegisteredExamRow queryUpcomingOfficial(String ownerPredicate, int ownerId) {
        String sql = REGISTERED_EXAM_SELECT.replace("SELECT TOP (?)", "SELECT TOP 1")
                + REGISTERED_EXAM_FROM
                + " WHERE " + ownerPredicate
                + """
                  AND CAST(ex.ExamDate AS DATE) >= CAST(GETDATE() AS DATE)
                """
                + ExamRegistrationLifecycleStatus.SQL_ACTIVE_EXAM_REGISTRATION_FILTER
                + """
                ORDER BY ex.ExamDate ASC
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, ownerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRegisteredExamRow(rs);
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Không tải kỳ thi sắp tới: {0}", e.getMessage());
        }
        return null;
    }

    private static RegistrantRegisteredExamRow pickUpcomingExam(
            List<RegistrantRegisteredExamRow> preferred, RegistrantRegisteredExamRow official) {
        java.util.Date today = Date.valueOf(LocalDate.now());
        RegistrantRegisteredExamRow bestPreferred = null;
        for (RegistrantRegisteredExamRow row : preferred) {
            if (row.getExamDate() == null || row.getExamDate().before(today)) {
                continue;
            }
            if (bestPreferred == null || row.getExamDate().before(bestPreferred.getExamDate())) {
                bestPreferred = row;
            }
        }
        if (official == null) {
            return bestPreferred;
        }
        if (bestPreferred == null) {
            return official;
        }
        // Ưu tiên kỳ chính thức nếu cùng ngày hoặc sớm hơn; ngược lại lấy ngày sớm nhất
        if (official.getExamDate() == null) {
            return bestPreferred;
        }
        return official.getExamDate().compareTo(bestPreferred.getExamDate()) <= 0 ? official : bestPreferred;
    }

    /** Xây danh sách hoạt động gần đây từ audit/đăng ký. */
    @Override
    public List<RegistrantDashboardActivity> listRecentActivities(int profileId, int limit) {
        /*
         * Tổng hợp hoạt động từ Payment + ExamEnrollment + nguyện vọng RegistrationDates
         * thay vì chỉ Audit (seed data có thể ít bản ghi Audit cho thí sinh).
         */
        List<RegistrantDashboardActivity> activities = new ArrayList<>();
        appendPaymentActivities(profileId, activities, limit);
        appendRegistrationActivities(profileId, activities, limit);
        appendPreferredDateActivities(profileId, activities, limit);
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

    /** Số ngày còn lại tới examDate; null nếu không hợp lệ. */
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

    // =========================================================================
    // REGION: Lịch thi & kết quả (my-exams)  (~L823)
    // =========================================================================

    /**
     * Danh sách “ca của tôi”: ngày mong muốn (RegistrationDates) + ca chính thức
     * (Candidate/Enrollment qua CCCD) kèm điểm và cờ thanh toán.
     */
    @Override
    public List<RegistrantMyExamRow> listMyExamsByUserId(int userId) {
        List<RegistrantMyExamRow> rows = new ArrayList<>();
        rows.addAll(listPreferredMyExamsByUserId(userId, null));
        rows.addAll(queryMyExams(userId, null));
        rows.sort((a, b) -> {
            java.util.Date da = a.getExamDate();
            java.util.Date db = b.getExamDate();
            if (da == null && db == null) {
                return Integer.compare(Math.abs(b.getCandidateId()), Math.abs(a.getCandidateId()));
            }
            if (da == null) {
                return 1;
            }
            if (db == null) {
                return -1;
            }
            int cmp = db.compareTo(da);
            return cmp != 0 ? cmp
                    : Integer.compare(Math.abs(b.getCandidateId()), Math.abs(a.getCandidateId()));
        });
        return rows;
    }

    /** SQL danh sách kỳ thi; extraPredicate thêm sau UserId (vd. AND c.CandidateId = ?) hoặc rỗng. */
    private String buildMyExamsSql(String extraPredicate) {
        return """
                SELECT c.CandidateId,
                       c.CandidateNumber,
                       ex.ExamCode AS SessionName,
                       CAST(ex.ExamDate AS DATE) AS examDate,
                """ + SESSION_SCHEDULE_COLUMNS + """
                       l.LicenceClass,
                       ea.AreaName,
                       CAST(NULL AS NVARCHAR(50)) AS RegistrationStatus,
                       theoryEes.Status AS sectionStatus,
                       (SELECT TOP 1 sec.SectionType
                        FROM ExamSection sec
                        WHERE sec.ExamId = ex.ExamId
                        ORDER BY sec.ExamSectionId) AS sectionName,
                       CASE WHEN pay.PaymentId IS NULL THEN 0 ELSE 1 END AS paid,
                       theory.scoreVal AS theoryScore,
                       practical.scoreVal AS practicalScore,
                       road.scoreVal AS roadScore,
                       erOverall.IsPassed AS overallPassed
                FROM Profile prof
                INNER JOIN Candidate c ON c.GovernmentIdNumber = prof.GovernmentIdNumber
                INNER JOIN ExamEnrollment ee ON ee.CandidateId = c.CandidateId
                INNER JOIN Exam ex ON ex.ExamId = ee.ExamId
                INNER JOIN Licence l ON l.LicenceId = ex.LicenceId
                """
            + Db2ExamSchemaSql.JOIN_THEORY_SECTION + """
                """
            + EXAM_AREA_JOIN_EX + PAYMENT_COMPLETED_JOIN + """
                LEFT JOIN (
                    SELECT er2.ExamEnrollmentId, CAST(MAX(es.Score) AS INT) AS scoreVal
                    FROM ExamResult er2
                    JOIN ExamScore es ON es.ExamResultId = er2.ExamResultId
                    JOIN ExamSection sec ON sec.ExamSectionId = es.ExamSectionId
                    WHERE sec.SectionType IN ("""
            + Db2ExamSchemaSql.THEORY_SECTION_TYPES + """
                    )
                    GROUP BY er2.ExamEnrollmentId
                ) theory ON theory.ExamEnrollmentId = ee.ExamEnrollmentId
                LEFT JOIN (
                    SELECT er2.ExamEnrollmentId, CAST(MAX(es.Score) AS INT) AS scoreVal
                    FROM ExamResult er2
                    JOIN ExamScore es ON es.ExamResultId = er2.ExamResultId
                    JOIN ExamSection sec ON sec.ExamSectionId = es.ExamSectionId
                    WHERE sec.SectionType IN ("""
            + Db2ExamSchemaSql.PRACTICAL_SECTION_TYPES + """
                    )
                    GROUP BY er2.ExamEnrollmentId
                ) practical ON practical.ExamEnrollmentId = ee.ExamEnrollmentId
                LEFT JOIN (
                    SELECT er2.ExamEnrollmentId, CAST(MAX(es.Score) AS INT) AS scoreVal
                    FROM ExamResult er2
                    JOIN ExamScore es ON es.ExamResultId = er2.ExamResultId
                    JOIN ExamSection sec ON sec.ExamSectionId = es.ExamSectionId
                    WHERE sec.SectionType IN (N'Road', N'Đường', N'RoadTest')
                    GROUP BY er2.ExamEnrollmentId
                ) road ON road.ExamEnrollmentId = ee.ExamEnrollmentId
                LEFT JOIN (
                    SELECT er3.ExamEnrollmentId, MAX(CAST(er3.IsPassed AS INT)) AS IsPassed
                    FROM ExamResult er3
                    GROUP BY er3.ExamEnrollmentId
                ) erOverall ON erOverall.ExamEnrollmentId = ee.ExamEnrollmentId
                WHERE prof.UserId = ?
                """ + extraPredicate + """
                ORDER BY ex.ExamDate DESC
                """;
    }

    private List<RegistrantMyExamRow> queryMyExams(int userId, Integer candidateId) {
        String extra = candidateId != null ? " AND c.CandidateId = ?" : "";
        String sql = buildMyExamsSql(extra);
        List<RegistrantMyExamRow> rows = new ArrayList<>();
        Connection conn = getConnection();
        if (conn == null) {
            LOG.log(Level.SEVERE, "Không tải kỳ thi user {0}: DB connection null", userId);
            return rows;
        }
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
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
            LOG.log(Level.SEVERE, "Không tải kỳ thi chính thức user " + userId
                    + " (kiểm tra Candidate/ExamEnrollment qua CCCD): " + e.getMessage(), e);
        }
        return rows;
    }

    /** Load một dòng my-exam theo candidateId thuộc user. */
    @Override
    public RegistrantMyExamRow findMyExamByCandidateId(int userId, int candidateId) {
        if (candidateId < 0) {
            List<RegistrantMyExamRow> preferred = listPreferredMyExamsByUserId(userId, -candidateId);
            return preferred.isEmpty() ? null : preferred.get(0);
        }
        List<RegistrantMyExamRow> rows = queryMyExams(userId, candidateId);
        return rows.isEmpty() ? null : rows.get(0);
    }

    private List<RegistrantMyExamRow> listPreferredMyExamsByUserId(int userId, Integer registrationDateId) {
        String sql = """
                SELECT rd.RegistrationDateId,
                       ed.ExamDateId,
                       CAST(ed.ExamDate AS DATE) AS examDate,
                       l.LicenceClass
                FROM RegistrationDates rd
                INNER JOIN ExamDates ed ON ed.ExamDateId = rd.ExamDateId
                INNER JOIN ExamRegistration er ON er.ExamRegistrationId = rd.ExamRegistrationId
                INNER JOIN Profile prof ON prof.ProfileId = er.ProfileId
                INNER JOIN Licence l ON l.LicenceId = ed.LicenceId
                WHERE prof.UserId = ?
                  AND rd.IsActive = 1
                """
                + (registrationDateId != null ? " AND rd.RegistrationDateId = ?" : "")
                + """
                ORDER BY ed.ExamDate DESC, rd.RegistrationDateId DESC
                """;
        List<RegistrantMyExamRow> rows = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            if (registrationDateId != null) {
                ps.setInt(2, registrationDateId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(mapPreferredMyExamRow(rs));
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Không tải lịch thi nguyện vọng user {0}: {1}",
                    new Object[] { userId, e.getMessage() });
        }
        return rows;
    }

    /** Ghép log theo dõi hồ sơ rồi sort mới nhất trước. */

    // =========================================================================
    // REGION: Hồ sơ tài liệu — ExamRegistration workflow (#PROFILE_DOC# …)  (~L999)
    // =========================================================================
    /** Đọc RegistrationStatus ER hồ sơ gốc (#PROFILE_DOC#). */
    @Override
    public String findProfileDocumentRegistrationStatus(int profileId) {
        String sql = """
                SELECT TOP 1 RegistrationStatus
                FROM ExamRegistration
                WHERE ProfileId = ?
                  AND RegistrationStatus IN (N'Draft', N'Pending', N'Approved', N'Rejected')
                  AND (Notes IS NULL OR (
                        Notes NOT LIKE N'%#SUPPLEMENT_DOC#%'
                        AND Notes NOT LIKE N'%#LICENCE_DOC#%'
                      ))
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

    /** Các hạng UI đã được duyệt kèm hồ sơ. */
    @Override
    public List<String> listApprovedDocumentLicenceCodes(int profileId) {
        List<String> codes = new ArrayList<>();
        if (profileId <= 0 || getConnection() == null) {
            return codes;
        }
        // Hồ sơ gốc Approved + các dòng xin duyệt hạng / bổ sung đã Approved
        String sql = """
                SELECT DISTINCT l.LicenceClass
                FROM ExamRegistration er
                INNER JOIN Licence l ON l.LicenceId = er.LicenceId
                WHERE er.ProfileId = ?
                  AND er.RegistrationStatus = N'Approved'
                  AND (
                        er.Notes LIKE N'%#PROFILE_DOC#%'
                     OR er.Notes LIKE N'%#LICENCE_DOC#%'
                     OR er.Notes LIKE N'%#SUPPLEMENT_DOC#%'
                     OR (
                            er.Notes IS NULL
                         OR (
                                er.Notes NOT LIKE N'%#SUPPLEMENT_DOC#%'
                            AND er.Notes NOT LIKE N'%#LICENCE_DOC#%'
                            AND er.Notes NOT LIKE N'%#EXAM_ID#%'
                            )
                        )
                  )
                ORDER BY l.LicenceClass
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, profileId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String ui = RegistrantExamSupport.toUiLicenceCode(rs.getString("LicenceClass"));
                    if (ui != null && !ui.isBlank() && !codes.contains(ui)) {
                        codes.add(ui);
                    }
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Không tải hạng đã duyệt hồ sơ profile {0}: {1}",
                    new Object[] { profileId, e.getMessage() });
        }
        return codes;
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

    /** True nếu còn ER bổ sung đang Pending. */
    @Override
    public boolean hasOpenSupplementPending(int profileId) {
        return findPendingSupplementExamRegistrationId(profileId) != null;
    }

    /** Id ER bổ sung Pending; null nếu không có. */
    private Integer findPendingSupplementExamRegistrationId(int profileId) {
        String sql = """
                SELECT TOP 1 ExamRegistrationId
                FROM ExamRegistration
                WHERE ProfileId = ?
                  AND RegistrationStatus = N'Pending'
                  AND (
                        Notes LIKE N'%#SUPPLEMENT_DOC#%'
                     OR Notes LIKE N'%#LICENCE_DOC#%'
                  )
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

    /** Tạo ER hồ sơ bổ sung với Notes #SUPPLEMENT_DOC# — trả ExamRegistrationId (>0) hoặc 0. */
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

    /** Tạo ER #LICENCE_DOC# xin duyệt thêm hạng. */
    @Override
    public int insertLicenceDocumentRegistration(int profileId, int licenceId, String status, String notes) {
        if (profileId <= 0 || licenceId <= 0 || status == null || status.isBlank()) {
            return 0;
        }
        String mergedNotes = RegistrantDocumentHelper.buildLicenceDocExamRegistrationNotes(notes);
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
            LOG.log(Level.WARNING, "Không tạo ExamRegistration xin duyệt hạng profile {0}: {1}",
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

    /** Map trạng thái các ER #SUPPLEMENT_DOC# của hồ sơ. */
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
                  AND (
                        Notes LIKE N'%#SUPPLEMENT_DOC#%'
                     OR Notes LIKE N'%#LICENCE_DOC#%'
                  )
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

    /** Đồng bộ ER hồ sơ gốc (không gắn LicenceId). */
    @Override
    public boolean syncProfileDocumentRegistration(int profileId, String status, String notes) {
        return syncProfileDocumentRegistration(profileId, status, notes, 0);
    }

    /** Đồng bộ ER hồ sơ gốc kèm LicenceId khi gửi duyệt. */
    @Override
    public boolean syncProfileDocumentRegistration(int profileId, String status, String notes, int licenceId) {
        if (profileId <= 0 || status == null || status.isBlank()) {
            return false;
        }
        if (getConnection() == null) {
            LOG.log(Level.WARNING, "Không đồng bộ RegistrationStatus — mất kết nối DB (profile {0})", profileId);
            return false;
        }
        try {
            String markedNotes = RegistrantDocumentHelper.ensureProfileDocMarker(notes);
            int resolvedLicenceId = licenceId > 0 ? licenceId : 0;
            int rows = updatePrimaryWorkflowRegistrationRows(
                    profileId, status.trim(), markedNotes, resolvedLicenceId);
            if (rows > 0) {
                return true;
            }
            int insertLicenceId = resolvedLicenceId > 0
                    ? resolvedLicenceId
                    : resolveLicenceIdForNewRegistration(profileId);
            if (insertLicenceId <= 0) {
                return false;
            }
            return insertPrimaryRegistrationRow(profileId, insertLicenceId, status.trim(), markedNotes);
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Không đồng bộ RegistrationStatus profile {0}: {1}",
                    new Object[] { profileId, e.getMessage() });
        }
        return false;
    }

    private int updatePrimaryWorkflowRegistrationRows(int profileId, String status, String notes,
            int licenceId) throws SQLException {
        if (licenceId > 0) {
            String sql = """
                    UPDATE ExamRegistration
                    SET RegistrationStatus = ?, Notes = ?, LicenceId = ?
                    WHERE ProfileId = ?
                      AND RegistrationStatus IN (N'Draft', N'Pending', N'Approved', N'Rejected')
                      AND (Notes IS NULL OR (
                            Notes NOT LIKE N'%#SUPPLEMENT_DOC#%'
                            AND Notes NOT LIKE N'%#LICENCE_DOC#%'
                          ))
                    """;
            try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
                ps.setString(1, status);
                ps.setString(2, notes != null ? notes : "");
                ps.setInt(3, licenceId);
                ps.setInt(4, profileId);
                return ps.executeUpdate();
            }
        }
        String sql = """
                UPDATE ExamRegistration
                SET RegistrationStatus = ?, Notes = ?
                WHERE ProfileId = ?
                  AND RegistrationStatus IN (N'Draft', N'Pending', N'Approved', N'Rejected')
                  AND (Notes IS NULL OR (
                        Notes NOT LIKE N'%#SUPPLEMENT_DOC#%'
                        AND Notes NOT LIKE N'%#LICENCE_DOC#%'
                      ))
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
        return resolveLicenceIdByUiCode("B1");
    }

    // =========================================================================
    // REGION: Theo dõi hồ sơ (track-profile)  (~L1346)
    // =========================================================================
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

    // =========================================================================
    // REGION: Helpers — mapper ResultSet (private)  (~L1371)
    // =========================================================================
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
        String sectionStatus = rs.getString("sectionStatus");
        RegistrantExamSupport.applyExamStatusBadge(row, candidateNumber, regStatus, sectionStatus);
        return row;
    }

    private RegistrantRegisteredExamRow mapPreferredRegisteredExamRow(ResultSet rs) throws SQLException {
        RegistrantRegisteredExamRow row = new RegistrantRegisteredExamRow();
        int registrationDateId = rs.getInt("RegistrationDateId");
        int examDateId = rs.getInt("ExamDateId");
        row.setId(-registrationDateId);
        row.setExamName("Ngày thi nguyện vọng");
        row.setExamCode("DK-" + examDateId);
        row.setLicenceClass(RegistrantExamSupport.toUiLicenceCode(rs.getString("LicenceClass")));
        row.setLicenceClassDescription(
                RegistrantExamSupport.licenceClassDescription(row.getLicenceClass()));
        row.setExamDate(rs.getDate("examDate"));
        row.setCandidateNumber(null);
        RegistrantExamSupport.applyPreferredDateStatus(row);
        return row;
    }

    private RegistrantMyExamRow mapPreferredMyExamRow(ResultSet rs) throws SQLException {
        RegistrantMyExamRow row = new RegistrantMyExamRow();
        int registrationDateId = rs.getInt("RegistrationDateId");
        int examDateId = rs.getInt("ExamDateId");
        row.setCandidateId(-registrationDateId);
        row.setExamTitle("Ngày thi nguyện vọng");
        row.setExamDate(rs.getDate("examDate"));
        row.setLicenceClass(RegistrantExamSupport.toUiLicenceCode(rs.getString("LicenceClass")));
        row.setSbd(null);
        row.setExamSectionName("Nguyện vọng");
        RegistrantExamSupport.applyPreferredDateStatus(row);
        row.setSessionTimeDisplay("Chờ trung tâm công bố");
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

        row.setPendingPayment(rs.getInt("paid") == 0);

        row.setTheoryScore(RegistrantExamSupport.toInteger(rs.getObject("theoryScore")));
        row.setPracticalScore(RegistrantExamSupport.toInteger(rs.getObject("practicalScore")));
        row.setRoadScore(RegistrantExamSupport.toInteger(rs.getObject("roadScore")));

        Integer overallPassed = RegistrantExamSupport.toInteger(rs.getObject("overallPassed"));
        String regStatus = rs.getString("RegistrationStatus");
        String sectionStatus = rs.getString("sectionStatus");
        row.setRegistrationStatus(regStatus);
        row.setExamSectionName(rs.getString("sectionName"));
        RegistrantExamSupport.applyMyExamStatus(row, rawSbd, regStatus, sectionStatus, overallPassed);
        RegistrantExamSupport.applyScorePresentation(row, null, null);
        RegistrantExamSupport.finalizeSessionTimeDisplay(row, sessionStatus, start, end, sectionStatus);
        return row;
    }

    // -------------------------------------------------------------------------
    // Helpers: dashboard activity appenders (private)  (~L1506)
    // -------------------------------------------------------------------------
    private void appendPaymentActivities(int profileId, List<RegistrantDashboardActivity> out, int limit) {
        String sql = """
                SELECT TOP (?) p.TotalAmount, p.PaidAt, l.LicenceClass
                FROM Payment p
                INNER JOIN ExamEnrollment ee ON ee.ExamEnrollmentId = p.ExamEnrollmentId
                INNER JOIN Candidate c ON c.CandidateId = ee.CandidateId
                INNER JOIN Profile prof ON prof.GovernmentIdNumber = c.GovernmentIdNumber
                INNER JOIN Exam ex ON ex.ExamId = ee.ExamId
                INNER JOIN Licence l ON l.LicenceId = ex.LicenceId
                WHERE prof.ProfileId = ?
                """ + SQL_AND_PAYMENT_COMPLETED + """
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
                SELECT TOP (?) ex.ExamCode AS SessionName, l.LicenceClass, ex.StartTime
                FROM Profile prof
                INNER JOIN Candidate c ON c.GovernmentIdNumber = prof.GovernmentIdNumber
                INNER JOIN ExamEnrollment ee ON ee.CandidateId = c.CandidateId
                INNER JOIN Exam ex ON ex.ExamId = ee.ExamId
                INNER JOIN Licence l ON l.LicenceId = ex.LicenceId
                WHERE prof.ProfileId = ?
                ORDER BY ee.ExamEnrollmentId DESC
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

    private void appendPreferredDateActivities(int profileId, List<RegistrantDashboardActivity> out, int limit) {
        String sql = """
                SELECT TOP (?)
                       ed.ExamDate,
                       l.LicenceClass,
                       rd.RegistrationDateId
                FROM RegistrationDates rd
                INNER JOIN ExamDates ed ON ed.ExamDateId = rd.ExamDateId
                INNER JOIN ExamRegistration er ON er.ExamRegistrationId = rd.ExamRegistrationId
                INNER JOIN Licence l ON l.LicenceId = ed.LicenceId
                WHERE er.ProfileId = ?
                  AND rd.IsActive = 1
                ORDER BY rd.RegistrationDateId DESC
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, limit);
            ps.setInt(2, profileId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Date examDate = rs.getDate("ExamDate");
                    Timestamp occurredAt = examDate != null
                            ? new Timestamp(examDate.getTime())
                            : new Timestamp(System.currentTimeMillis());
                    String licence = RegistrantExamSupport.toUiLicenceCode(rs.getString("LicenceClass"));
                    RegistrantDashboardActivity act = new RegistrantDashboardActivity();
                    act.setColorClass("green");
                    act.setIconPath("M20 6L9 17l-5-5");
                    act.setTitle("Đã gửi nguyện vọng ngày thi");
                    act.setDesc(String.format(
                            "Nguyện vọng hạng %s — ngày %s. Trạng thái: chờ lịch chính thức từ trung tâm.",
                            licence,
                            examDate != null
                                    ? new java.text.SimpleDateFormat("dd/MM/yyyy").format(examDate)
                                    : "—"));
                    act.setTime(RegistrantExamSupport.formatActivityTime(occurredAt));
                    act.setOccurredAt(occurredAt);
                    out.add(act);
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Không tải hoạt động nguyện vọng ngày thi: {0}", e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Helpers: track-profile builders (private)  (~L1623)
    // -------------------------------------------------------------------------
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
                INNER JOIN ExamEnrollment ee ON ee.ExamEnrollmentId = p.ExamEnrollmentId
                INNER JOIN Candidate c ON c.CandidateId = ee.CandidateId
                INNER JOIN Profile prof ON prof.GovernmentIdNumber = c.GovernmentIdNumber
                INNER JOIN Exam ex ON ex.ExamId = ee.ExamId
                INNER JOIN Licence l ON l.LicenceId = ex.LicenceId
                WHERE prof.UserId = ?
                """ + SQL_AND_PAYMENT_COMPLETED + """
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
                            rs.getTimestamp("PaidAt"), RegistrantTrackingCategories.RegistrantPayment);
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Không tải tracking Payment: {0}", e.getMessage());
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
