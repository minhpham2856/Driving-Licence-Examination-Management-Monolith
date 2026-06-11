package DAO.Impl;

import DBConnection.DBContext;
import DAO.CandidateDAO;
import DAO.ExamRegistrationDAO;
import Models.DashboardActivity;
import Models.MyExamDetailView;
import Models.MyExamRowView;
import Models.MyExamScoreSection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Truy vấn và ghi {@code ExamRegistration} — nguồn duy nhất cho lịch thi (trừ SBD).
 * <p>SBD: {@link ExamSbdEnricher} đọc từ bảng {@code Candidate}. Không đọc/ghi {@code ExamRegistration.candidateNo}.</p>
 */
public class ExamRegistrationDAOImpl extends DBContext implements ExamRegistrationDAO {

    private static final Logger LOG = Logger.getLogger(ExamRegistrationDAOImpl.class.getName());

    private final ExamSbdEnricher sbdEnricher = new ExamSbdEnricher(new CandidateDAOImpl());

    @Override
    public int countByPersonId(int personId) {
        String sql = "select count(*) from ExamRegistration where personId = ? and isCancelled = 0";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, personId);

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

    @Override
    public int countResultsByPersonId(int personId) {
        String sql = """
                select count(*)
                from ExamResult er
                join ExamRegistration reg on er.examRegistrationId = reg.id
                where reg.personId = ? and er.isCancelled = 0
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, personId);

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

    @Override
    public int countDocumentsByPersonId(int personId) {
        String sql = "select count(*) from CandidateDocument where personId = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, personId);

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

    @Override
    public List<DashboardActivity> findRecentRegistrationActivitiesByPersonId(int personId, int limit) {
        String sql = """
                select * from (
                    select 'registration' as activityType,
                           es.createdAt as occurredAt,
                           es.sessionName,
                           lt.licenseCode,
                           null as amount,
                           null as finalScore,
                           null as examTypeName
                    from ExamRegistration er
                    join ExamSession es on er.examSessionId = es.id
                    join LicenseType lt on es.licenseTypeId = lt.id
                    where er.personId = ?

                    union all

                    select 'result' as activityType,
                           res.endTime as occurredAt,
                           es.sessionName,
                           lt.licenseCode,
                           null as amount,
                           coalesce(ts.finalScore, ps.finalScore) as finalScore,
                           et.typeName as examTypeName
                    from ExamResult res
                    join ExamRegistration er on res.examRegistrationId = er.id
                    join ExamSession es on er.examSessionId = es.id
                    join LicenseType lt on es.licenseTypeId = lt.id
                    join ExamSection sec on res.examSectionId = sec.id
                    join ExamType et on sec.examTypeId = et.id
                    left join TheoryScore ts on res.theoryScoreId = ts.id
                    left join PracticalScore ps on res.practicalScoreId = ps.id
                    where er.personId = ? and res.isCancelled = 0
                ) activities
                order by occurredAt desc
                offset 0 rows fetch next ? rows only
                """;

        List<DashboardActivity> activities = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, personId);
            ps.setInt(2, personId);
            ps.setInt(3, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    activities.add(mapActivity(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return activities;
    }

    @Override
    public String findLatestLicenseCodeByPersonId(int personId) {
        String sql = """
                select top 1 lt.licenseCode
                from ExamRegistration er
                join ExamSession es on er.examSessionId = es.id
                join LicenseType lt on es.licenseTypeId = lt.id
                where er.personId = ?
                order by es.examDate desc, es.shiftStartTime desc
                """;

        try {
            ensureConnection();
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, personId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("licenseCode");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public boolean existsActiveByPersonAndSession(int personId, int examSessionId) {
        String sql = """
                select 1
                from ExamRegistration er
                where er.personId = ?
                  and er.examSessionId = ?
                  and er.isCancelled = 0
                """;

        try {
            ensureConnection();
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, personId);
                ps.setInt(2, examSessionId);

                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public int insertRegistration(int examSessionId, int personId) {
        String sql = """
                insert into ExamRegistration (examSessionId, personId, registrationType, isPaymentCompleted, isPresent)
                output inserted.id
                values (?, ?, 'PreRegistered', 0, 0)
                """;

        try {
            ensureConnection();
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, examSessionId);
                ps.setInt(2, personId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    @Override
    public boolean markPaymentCompleted(int registrationId) {
        String sql = """
                update ExamRegistration
                set isPaymentCompleted = 1
                where id = ?
                  and isPaymentCompleted = 0
                """;

        try {
            ensureConnection();
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, registrationId);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean markCancelled(int registrationId) {
        String sql = """
                update ExamRegistration
                set isCancelled = 1
                where id = ?
                  and isCancelled = 0
                  and isPaymentCompleted = 0
                """;

        try {
            ensureConnection();
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, registrationId);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean deleteById(int registrationId) {
        String sql = "delete from ExamRegistration where id = ?";

        try {
            ensureConnection();
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, registrationId);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public List<MyExamRowView> findExamRowsByPersonId(int personId) {
        String sql = """
                select er.id as registrationId,
                       er.examSessionId,
                       es.sessionName,
                       es.examDate,
                       es.shiftStartTime,
                       es.shiftEndTime,
                       lt.licenseCode,
                       et.typeName as examTypeName,
                       coalesce(ea.areaName, ea.location, N'Đang cập nhật') as roomLabel,
                       coalesce(ea.location, ea.areaName, N'Đang cập nhật') as location,
                       er.isPaymentCompleted,
                       er.isCancelled,
                       er.isPresent,
                       es.status as sessionStatus,
                       (select count(*)
                        from ExamResult res
                        where res.examRegistrationId = er.id and res.isCancelled = 0) as resultCount,
                       (select min(coalesce(ts.finalScore, ps.finalScore))
                        from ExamResult res
                        left join TheoryScore ts on res.theoryScoreId = ts.id
                        left join PracticalScore ps on res.practicalScoreId = ps.id
                        where res.examRegistrationId = er.id and res.isCancelled = 0) as minScore
                from ExamRegistration er
                join ExamSession es on er.examSessionId = es.id
                join LicenseType lt on es.licenseTypeId = lt.id
                join ExamType et on es.examTypeId = et.id
                join ExamArea ea on es.areaId = ea.id
                where er.personId = ?
                order by es.examDate desc, es.shiftStartTime desc
                """;

        List<MyExamRowView> rows = new ArrayList<>();

        try {
            ensureConnection();
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, personId);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        rows.add(mapExamRow(rs));
                    }
                }
            }
            sbdEnricher.enrichRows(rows, personId);
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "findExamRowsByPersonId failed personId=" + personId, e);
        }

        return rows;
    }

    @Override
    public Optional<MyExamDetailView> findExamDetailByRegistrationId(int personId, int registrationId) {
        String sql = """
                select er.id as registrationId,
                       lt.licenseCode,
                       es.id as examSessionId,
                       es.sessionName,
                       es.examDate,
                       es.shiftStartTime,
                       coalesce(ea.areaName, ea.location, N'Đang cập nhật') as roomLabel,
                       er.isPaymentCompleted,
                       er.isCancelled,
                       er.isPresent,
                       es.status as sessionStatus,
                       ep.examComputerId,
                       ec.computerCode
                from ExamRegistration er
                join ExamSession es on er.examSessionId = es.id
                join LicenseType lt on es.licenseTypeId = lt.id
                join ExamArea ea on es.areaId = ea.id
                left join ExamPaper ep on ep.examRegistrationId = er.id
                left join ExamComputer ec on ep.examComputerId = ec.id
                where er.personId = ? and er.id = ?
                """;

        try {
            ensureConnection();
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, personId);
                ps.setInt(2, registrationId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        MyExamDetailView detail = mapExamDetail(rs);
                        sbdEnricher.enrichDetail(detail, registrationId, personId, rs.getInt("examSessionId"));
                        detail.setScoreSections(findScoreSections(registrationId));
                        return Optional.of(detail);
                    }
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "findExamDetailByRegistrationId failed registrationId=" + registrationId, e);
        }

        return Optional.empty();
    }

    @Override
    public int countPassedRegistrationsByPersonId(int personId) {
        String sql = """
                select count(*) from (
                    select er.id
                    from ExamRegistration er
                    join ExamResult res on res.examRegistrationId = er.id and res.isCancelled = 0
                    left join TheoryScore ts on res.theoryScoreId = ts.id
                    left join PracticalScore ps on res.practicalScoreId = ps.id
                    where er.personId = ?
                    group by er.id
                    having min(coalesce(ts.finalScore, ps.finalScore)) >= 80
                ) passed
                """;

        try {
            ensureConnection();
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, personId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    private List<MyExamScoreSection> findScoreSections(int registrationId) {
        String sql = """
                select et.typeName,
                       coalesce(ts.finalScore, ps.finalScore) as finalScore
                from ExamResult res
                join ExamSection sec on res.examSectionId = sec.id
                join ExamType et on sec.examTypeId = et.id
                left join TheoryScore ts on res.theoryScoreId = ts.id
                left join PracticalScore ps on res.practicalScoreId = ps.id
                where res.examRegistrationId = ? and res.isCancelled = 0
                order by et.id
                """;

        List<MyExamScoreSection> sections = new ArrayList<>();

        try {
            ensureConnection();
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, registrationId);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        sections.add(mapScoreSection(rs));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return sections;
    }

    private MyExamRowView mapExamRow(ResultSet rs) throws SQLException {
        MyExamRowView row = new MyExamRowView();
        row.setRegistrationId(rs.getInt("registrationId"));
        row.setExamSessionId(rs.getInt("examSessionId"));
        row.setTitle(rs.getString("sessionName"));
        row.setExamDate(rs.getDate("examDate"));
        row.setLicenceCode(rs.getString("licenseCode"));
        row.setExamTypeName(rs.getString("examTypeName"));
        row.setRoomLabel(rs.getString("roomLabel"));
        row.setLocation(rs.getString("location"));
        row.setShiftStartTime(rs.getTime("shiftStartTime"));
        row.setShiftEndTime(rs.getTime("shiftEndTime"));

        applyRowStatus(
                row,
                rs.getBoolean("isPaymentCompleted"),
                rs.getBoolean("isCancelled"),
                rs.getBoolean("isPresent"),
                rs.getString("sessionStatus"),
                rs.getInt("resultCount"),
                rs.getObject("minScore") != null ? rs.getInt("minScore") : null,
                rs.getDate("examDate"));
        return row;
    }

    private void applyRowStatus(MyExamRowView row, boolean paymentCompleted, boolean isCancelled,
            boolean isPresent, String sessionStatus, int resultCount, Integer minScore,
            java.sql.Date examDate) {
        if (isCancelled) {
            row.setCancelled(true);
            row.setStatusLabel("Đã hủy");
            row.setStatusClass("gray");
            return;
        }

        if (!paymentCompleted) {
            row.setStatusLabel("Chờ thanh toán tại quầy");
            row.setStatusClass("pending");
            row.setPendingRow(true);
            return;
        }

        if ("Completed".equalsIgnoreCase(sessionStatus) && !isPresent) {
            row.setStatusLabel("Vắng thi");
            row.setStatusClass("gray");
            return;
        }

        if (resultCount > 0) {
            if (minScore != null && minScore >= 80) {
                row.setStatusLabel("Đạt");
                row.setStatusClass("approved");
            } else {
                row.setStatusLabel("Không đạt");
                row.setStatusClass("rejected");
            }
            return;
        }

        if (isUpcoming(examDate, sessionStatus)) {
            row.setStatusLabel("Chờ thi");
            row.setStatusClass("pending");
            row.setPendingRow(true);
            return;
        }

        row.setStatusLabel("Đã đăng ký");
        row.setStatusClass("info");
    }

    private boolean isUpcoming(java.sql.Date examDate, String sessionStatus) {
        if (examDate == null || "Completed".equalsIgnoreCase(sessionStatus)
                || "Cancelled".equalsIgnoreCase(sessionStatus)) {
            return false;
        }
        return !examDate.toLocalDate().isBefore(LocalDate.now());
    }

    private MyExamDetailView mapExamDetail(ResultSet rs) throws SQLException {
        MyExamDetailView detail = new MyExamDetailView();
        detail.setRegistrationId(rs.getInt("registrationId"));
        detail.setLicenceLabel("Hạng " + rs.getString("licenseCode"));
        detail.setSessionCode("SH-" + rs.getInt("examSessionId"));
        detail.setExamDate(rs.getDate("examDate"));

        java.sql.Time shiftStart = rs.getTime("shiftStartTime");
        if (shiftStart != null) {
            detail.setGatherTimeLabel(shiftStart.toLocalTime().toString().substring(0, 5));
        } else {
            detail.setGatherTimeLabel("—");
        }

        detail.setRoomLabel(rs.getString("roomLabel"));
        String computerCode = rs.getString("computerCode");
        detail.setMachineLabel(computerCode != null ? computerCode : "—");

        boolean paymentCompleted = rs.getBoolean("isPaymentCompleted");
        boolean isCancelled = rs.getBoolean("isCancelled");
        String sessionStatus = rs.getString("sessionStatus");

        detail.setCancelled(isCancelled);
        detail.setPaymentPending(!paymentCompleted && !isCancelled);
        detail.setQrAvailable(paymentCompleted && !isCancelled
                && !"Cancelled".equalsIgnoreCase(sessionStatus));
        return detail;
    }

    private MyExamScoreSection mapScoreSection(ResultSet rs) throws SQLException {
        MyExamScoreSection section = new MyExamScoreSection();
        String typeName = rs.getString("typeName");
        section.setSectionTitle(translateSectionTitle(typeName));

        Integer finalScore = (Integer) rs.getObject("finalScore");
        if (finalScore == null) {
            section.setShowPlaceholder(true);
            section.setPlaceholderText("Chưa có điểm cho phần thi này.");
            section.setStatusLabel("Chờ thi");
            section.setStatusClass("warning");
            section.setBadgeClass("warning");
            return section;
        }

        section.setFinalScore(finalScore);
        boolean passed = finalScore >= 80;
        section.setStatusLabel(passed ? "Đạt" : "Không đạt");
        section.setStatusClass(passed ? "success" : "danger");
        section.setBadgeClass(passed ? "success" : "danger");
        return section;
    }

    private String translateSectionTitle(String typeName) {
        return switch (typeName != null ? typeName : "") {
            case "Theory" -> "Sát hạch Lý thuyết";
            case "Practical" -> "Sát hạch Thực hành";
            case "RoadLayout" -> "Sát hạch Sa hình";
            case "OnRoad" -> "Sát hạch Đường trường";
            default -> "Sát hạch " + typeName;
        };
    }

    private DashboardActivity mapActivity(ResultSet rs) throws SQLException {
        DashboardActivity activity = new DashboardActivity();
        String type = rs.getString("activityType");
        Timestamp occurredAt = rs.getTimestamp("occurredAt");
        String sessionName = rs.getString("sessionName");
        String licenseCode = rs.getString("licenseCode");

        activity.setOccurredAt(occurredAt);

        if ("result".equals(type)) {
            activity.setTitle("Kết quả thi được cập nhật");
            activity.setDesc(String.format(
                    "%s — Hạng %s: Điểm %d",
                    sessionName,
                    licenseCode,
                    rs.getInt("finalScore")));
            activity.setColorClass("amber");
            activity.setIconPath("M4 2h16v20H4z M8 7h8M8 11h8M8 15h5");
        } else {
            activity.setTitle("Đăng ký đợt thi thành công");
            activity.setDesc(String.format(
                    "Đã đăng ký tham gia %s — Hạng %s",
                    sessionName,
                    licenseCode));
            activity.setColorClass("green");
            activity.setIconPath("M20 6L9 17l-5-5");
        }

        return activity;
    }

}
