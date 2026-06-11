package DAO.Impl;

import DAO.PaymentDAO;
import DBConnection.DBContext;
import Models.DashboardActivity;
import Models.PaymentRecord;
import Models.PendingRegistrationContext;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PaymentDAOImpl extends DBContext implements PaymentDAO {

    @Override
    public int insertPending(int examRegistrationId, BigDecimal amount, String paymentMethod,
            String transactionReference, Timestamp paymentExpiresAt) {
        String sql = """
                insert into Payment (examRegistrationId, amount, paymentStatus, paymentMethod, transactionReference, paymentExpiresAt)
                output inserted.id
                values (?, ?, 'Pending', ?, ?, ?)
                """;

        try {
            ensureConnection();
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, examRegistrationId);
                ps.setBigDecimal(2, amount);
                ps.setString(3, paymentMethod);
                ps.setString(4, transactionReference);
                ps.setTimestamp(5, paymentExpiresAt);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    @Override
    public boolean deleteByRegistrationId(int examRegistrationId) {
        String sql = "delete from Payment where examRegistrationId = ?";

        try {
            ensureConnection();
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, examRegistrationId);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public Optional<PaymentRecord> findByTransactionReference(String transactionReference) {
        String sql = """
                select p.id,
                       p.examRegistrationId,
                       er.examSessionId,
                       p.amount,
                       p.paymentStatus,
                       p.paymentMethod,
                       p.transactionReference,
                       p.paymentExpiresAt,
                       er.isCancelled as registrationCancelled
                from Payment p
                join ExamRegistration er on p.examRegistrationId = er.id
                where p.transactionReference = ?
                """;

        try {
            ensureConnection();
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, transactionReference);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapRow(rs));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    @Override
    public boolean markCompleted(int paymentId) {
        String sql = """
                update Payment
                set paymentStatus = 'Completed',
                    paymentDate = getutcdate()
                where id = ?
                  and paymentStatus = 'Pending'
                """;

        try {
            ensureConnection();
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, paymentId);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean markCancelled(int paymentId) {
        String sql = """
                update Payment
                set paymentStatus = 'Cancelled'
                where id = ?
                  and paymentStatus = 'Pending'
                """;

        try {
            ensureConnection();
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, paymentId);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public int cancelOverduePendingForPerson(int personId) {
        String sql = """
                update Payment
                set paymentStatus = 'Cancelled'
                from Payment p
                inner join ExamRegistration er on p.examRegistrationId = er.id
                where er.personId = ?
                  and er.isCancelled = 0
                  and er.isPaymentCompleted = 0
                  and p.paymentStatus = 'Pending'
                  and p.paymentExpiresAt is not null
                  and p.paymentExpiresAt < getutcdate()
                """;

        try {
            ensureConnection();
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, personId);
                return ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    @Override
    public BigDecimal sumCompletedByPersonId(int personId) {
        String sql = """
                select coalesce(sum(p.amount), 0)
                from Payment p
                join ExamRegistration er on p.examRegistrationId = er.id
                where er.personId = ? and p.paymentStatus = 'Completed'
                """;

        try {
            ensureConnection();
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, personId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getBigDecimal(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return BigDecimal.ZERO;
    }

    @Override
    public List<DashboardActivity> findRecentPaymentActivitiesByPersonId(int personId, int limit) {
        String sql = """
                select p.paymentDate as occurredAt,
                       es.sessionName,
                       lt.licenseCode,
                       p.amount
                from Payment p
                join ExamRegistration er on p.examRegistrationId = er.id
                join ExamSession es on er.examSessionId = es.id
                join LicenseType lt on es.licenseTypeId = lt.id
                where er.personId = ? and p.paymentStatus = 'Completed'
                order by p.paymentDate desc
                offset 0 rows fetch next ? rows only
                """;

        List<DashboardActivity> activities = new ArrayList<>();

        try {
            ensureConnection();
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, personId);
                ps.setInt(2, limit);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        activities.add(mapPaymentActivity(rs));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return activities;
    }

    @Override
    public Optional<PendingRegistrationContext> findResumablePending(int personId, int registrationId) {
        return findPendingContext("""
                where er.id = ? and er.personId = ?
                """, registrationId, personId);
    }

    @Override
    public int cancelRegistrationsForOverduePayments(int personId) {
        String sql = """
                update ExamRegistration
                set isCancelled = 1
                from ExamRegistration er
                inner join Payment p on p.examRegistrationId = er.id
                where er.personId = ?
                  and er.isCancelled = 0
                  and er.isPaymentCompleted = 0
                  and p.paymentStatus = 'Cancelled'
                """;

        try {
            ensureConnection();
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, personId);
                return ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    private Optional<PendingRegistrationContext> findPendingContext(String whereClause, int... params) {
        String sql = """
                select er.id as registrationId,
                       er.personId,
                       er.examSessionId,
                       p.transactionReference as invoiceNumber,
                       p.amount,
                       p.paymentExpiresAt,
                       lt.licenseCode,
                       es.sessionName
                from ExamRegistration er
                join Payment p on p.examRegistrationId = er.id and p.paymentStatus = 'Pending'
                join ExamSession es on er.examSessionId = es.id
                join LicenseType lt on es.licenseTypeId = lt.id
                """
                + whereClause + """
                  and er.isCancelled = 0
                  and er.isPaymentCompleted = 0
                  and p.paymentExpiresAt > getutcdate()
                """;

        try {
            ensureConnection();
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                for (int i = 0; i < params.length; i++) {
                    ps.setInt(i + 1, params[i]);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapPendingContext(rs));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    private PendingRegistrationContext mapPendingContext(ResultSet rs) throws SQLException {
        PendingRegistrationContext ctx = new PendingRegistrationContext();
        ctx.setRegistrationId(rs.getInt("registrationId"));
        ctx.setPersonId(rs.getInt("personId"));
        ctx.setExamSessionId(rs.getInt("examSessionId"));
        ctx.setInvoiceNumber(rs.getString("invoiceNumber"));
        ctx.setAmount(rs.getBigDecimal("amount"));
        ctx.setPaymentExpiresAt(rs.getTimestamp("paymentExpiresAt"));
        ctx.setLicenceCode(rs.getString("licenseCode"));
        ctx.setSessionName(rs.getString("sessionName"));
        return ctx;
    }

    private DashboardActivity mapPaymentActivity(ResultSet rs) throws SQLException {
        DashboardActivity activity = new DashboardActivity();
        activity.setOccurredAt(rs.getTimestamp("occurredAt"));
        activity.setTitle("Thanh toán lệ phí thành công");
        activity.setDesc(String.format(
                "Lệ phí thi Hạng %s — %s VNĐ đã được xử lý",
                rs.getString("licenseCode"),
                formatAmount(rs.getBigDecimal("amount"))));
        activity.setColorClass("blue");
        activity.setIconPath("M2 10h20");
        return activity;
    }

    private String formatAmount(BigDecimal amount) {
        if (amount == null) {
            return "0";
        }
        return String.format("%,.0f", amount);
    }

    private PaymentRecord mapRow(ResultSet rs) throws SQLException {
        PaymentRecord record = new PaymentRecord();
        record.setId(rs.getInt("id"));
        record.setExamRegistrationId(rs.getInt("examRegistrationId"));
        record.setExamSessionId(rs.getInt("examSessionId"));
        record.setAmount(rs.getBigDecimal("amount"));
        record.setPaymentStatus(rs.getString("paymentStatus"));
        record.setPaymentMethod(rs.getString("paymentMethod"));
        record.setTransactionReference(rs.getString("transactionReference"));
        record.setPaymentExpiresAt(rs.getTimestamp("paymentExpiresAt"));
        try {
            record.setRegistrationCancelled(rs.getBoolean("registrationCancelled"));
        } catch (SQLException ignored) {
            record.setRegistrationCancelled(false);
        }
        return record;
    }
}
