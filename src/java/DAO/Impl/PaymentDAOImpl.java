package DAO.Impl;

import DBConnection.DBContext;
import DAO.PaymentDAO;
import Models.Fee;
import Models.Payment;
import java.sql.*;
import java.util.List;

public class PaymentDAOImpl extends DBContext implements PaymentDAO {

    @Override
    public boolean insert(Payment payment) {
        String sql = """
                INSERT INTO Payment (PaymentStatus, PaymentMethod, TransactionReference, TotalAmount, PaidAt, CandidateId, ExamId)
                VALUES (?, ?, ?, ?, GETDATE(), ?, ?)
                """;
        try {
            int examId = resolveExamId(payment.getExamRegistrationId());
            if (examId <= 0) {
                return false;
            }
            try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, payment.getPaymentStatus() != null ? payment.getPaymentStatus() : "Completed");
                ps.setString(2, payment.getPaymentMethod() != null ? payment.getPaymentMethod() : "Cash");
                if (payment.getTransactionReference() == null) {
                    ps.setNull(3, Types.NVARCHAR);
                } else {
                    ps.setString(3, payment.getTransactionReference());
                }
                ps.setDouble(4, payment.getAmount());
                ps.setInt(5, payment.getExamRegistrationId());
                ps.setInt(6, examId);
                int affected = ps.executeUpdate();
                if (affected > 0) {
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
    public boolean insertWithFees(Payment payment, List<Fee> fees) {
        boolean previousAutoCommit = true;
        try {
            previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            if (!insert(payment)) {
                connection.rollback();
                return false;
            }
            if (fees != null && !fees.isEmpty()) {
                String sql = "INSERT INTO Payment_Fee (PaymentId, FeeId) VALUES (?, ?)";
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    for (Fee fee : fees) {
                        ps.setInt(1, payment.getId());
                        ps.setInt(2, fee.getId());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }
            connection.commit();
            return true;
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                connection.setAutoCommit(previousAutoCommit);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Payment getByCandidateId(int candidateId) {
        String sql = """
                SELECT TOP 1 PaymentId, PaymentStatus, PaymentMethod, TransactionReference,
                       TotalAmount, PaidAt, CandidateId
                FROM Payment
                WHERE CandidateId = ?
                ORDER BY PaidAt DESC, PaymentId DESC
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Payment payment = new Payment();
                    payment.setId(rs.getInt("PaymentId"));
                    payment.setExamRegistrationId(rs.getInt("CandidateId"));
                    payment.setAmount(rs.getDouble("TotalAmount"));
                    payment.setPaymentStatus(rs.getString("PaymentStatus"));
                    payment.setPaymentMethod(rs.getString("PaymentMethod"));
                    payment.setTransactionReference(rs.getString("TransactionReference"));
                    payment.setPaymentDate(rs.getTimestamp("PaidAt"));
                    return payment;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean cancelCompletedByCandidateId(int candidateId) {
        String sql = """
                UPDATE Payment
                SET PaymentStatus = N'Cancelled'
                WHERE CandidateId = ?
                  AND PaymentStatus IN (N'Completed', N'Paid')
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private int resolveExamId(int candidateId) throws SQLException {
        String sql = "SELECT TOP 1 ExamId FROM Exam_Candidate WHERE CandidateId = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ExamId");
                }
            }
        }
        return -1;
    }
}
