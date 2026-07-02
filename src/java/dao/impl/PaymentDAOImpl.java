package dao.impl;


import dbconnection.DBContext;

import dao.PaymentDAO;

import model.payment.Payment;
import java.sql.*;

/**
 * JDBC implementation of PaymentDAO — payments keyed by ExamEnrollmentId.
 */
public class PaymentDAOImpl extends DBContext implements PaymentDAO {

    @Override
    public boolean insert(Payment payment) {
        String sql = """
                INSERT INTO Payment (PaymentStatus, PaymentMethod, TransactionReference, TotalAmount, PaidAt, ExamEnrollmentId)
                VALUES (?, ?, ?, ?, GETDATE(), ?)
                """;
        try {
            int enrollmentId = resolveEnrollmentId(payment.getCandidateId());
            if (enrollmentId <= 0) {
                return false;
            }
            try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, payment.getPaymentStatus() != null ? payment.getPaymentStatus() : "Completed");
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
                            payment.setId(gk.getInt(1));
                            return true;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Payment getByCandidateId(int candidateId) {
        String sql = """
                SELECT TOP 1 p.PaymentId, p.PaymentStatus, p.PaymentMethod, p.TransactionReference,
                       p.TotalAmount, p.PaidAt, p.ExamEnrollmentId, ee.CandidateId
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
                    payment.setId(rs.getInt("PaymentId"));
                    payment.setCandidateId(rs.getInt("CandidateId"));
                    payment.setTotalAmount(rs.getDouble("TotalAmount"));
                    payment.setPaymentStatus(rs.getString("PaymentStatus"));
                    payment.setPaymentMethod(rs.getString("PaymentMethod"));
                    payment.setTransactionReference(rs.getString("TransactionReference"));
                    payment.setPaidAt(rs.getTimestamp("PaidAt"));
                    return payment;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private int resolveEnrollmentId(int candidateId) throws SQLException {
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
        }
        return -1;
    }
}
