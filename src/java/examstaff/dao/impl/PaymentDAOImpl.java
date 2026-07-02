package examstaff.dao.impl;

import dbconnection.DBContext;
import examstaff.dao.PaymentDAO;
import examstaff.enums.PaymentStatus;
import examstaff.model.Payment;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PaymentDAOImpl extends DBContext implements PaymentDAO {

    private static final Logger LOG = Logger.getLogger(PaymentDAOImpl.class.getName());

    @Override
    public boolean insert(Payment payment) {
        int enrollmentId = payment.getExamEnrollmentId();
        if (enrollmentId <= 0) {
            return false;
        }
        String sql = """
                INSERT INTO Payment (PaymentStatus, PaymentMethod, TransactionReference, TotalAmount, PaidAt, ExamEnrollmentId)
                VALUES (?, ?, ?, ?, GETDATE(), ?)
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, payment.getPaymentStatus() != null ? payment.getPaymentStatus()
                    : PaymentStatus.HOAN_TAT.getDisplayName());
            ps.setString(2, payment.getPaymentMethod() != null ? payment.getPaymentMethod() : "Cash");
            if (payment.getTransactionReference() == null) {
                ps.setNull(3, Types.NVARCHAR);
            } else {
                ps.setString(3, payment.getTransactionReference());
            }
            ps.setDouble(4, payment.getTotalAmount());
            ps.setInt(5, enrollmentId);
            if (ps.executeUpdate() > 0) {
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) {
                        payment.setPaymentId(gk.getInt(1));
                        payment.setExamEnrollmentId(enrollmentId);
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Failed to insert payment", e);
        }
        return false;
    }

    @Override
    public Payment getByCandidateId(int candidateId) {
        String sql = """
                SELECT TOP 1 p.PaymentId, p.PaymentStatus, p.PaymentMethod, p.TransactionReference,
                       p.TotalAmount, p.PaidAt, p.ExamEnrollmentId
                FROM Payment p
                INNER JOIN ExamEnrollment ee ON ee.ExamEnrollmentId = p.ExamEnrollmentId
                WHERE ee.CandidateId = ?
                ORDER BY p.PaidAt DESC, p.PaymentId DESC
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Payment payment = new Payment();
                    payment.setPaymentId(rs.getInt("PaymentId"));
                    payment.setExamEnrollmentId(rs.getInt("ExamEnrollmentId"));
                    payment.setTotalAmount(rs.getDouble("TotalAmount"));
                    payment.setPaymentStatus(rs.getString("PaymentStatus"));
                    payment.setPaymentMethod(rs.getString("PaymentMethod"));
                    payment.setTransactionReference(rs.getString("TransactionReference"));
                    payment.setPaidAt(rs.getTimestamp("PaidAt"));
                    return payment;
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Failed to load payment for candidate " + candidateId, e);
        }
        return null;
    }

    @Override
    public int resolveEnrollmentId(int candidateId) {
        String sql = """
                SELECT TOP 1 ExamEnrollmentId
                FROM ExamEnrollment
                WHERE CandidateId = ?
                ORDER BY ExamEnrollmentId DESC
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ExamEnrollmentId");
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Failed to resolve enrollment for candidate " + candidateId, e);
        }
        return -1;
    }

    // --- mainTest-only method ---

    @Override
    public boolean hasCompletedPayment(int examEnrollmentId) {
        String sql = "SELECT COUNT(*) FROM Payment WHERE ExamEnrollmentId = ? AND PaymentStatus = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, examEnrollmentId);
            ps.setString(2, PaymentStatus.HOAN_TAT.getValue());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            LOG.log(java.util.logging.Level.SEVERE, "hasCompletedPayment error", e);
        }
        return false;
    }
}

