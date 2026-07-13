package payment.dao.impl;

import shared.dbconnection.DBContext;
import payment.dao.PaymentDAO;
import payment.dto.PaymentRecord;
import java.sql.*;

public class PaymentDAOImpl extends DBContext implements PaymentDAO {

    @Override
    public boolean insert(PaymentRecord payment) {
        String sql = """
                INSERT INTO RegistrantPayment (PaymentStatus, PaymentMethod, TransactionReference, TotalAmount, PaidAt, CandidateId, ExamId)
                VALUES (?, ?, ?, ?, GETDATE(), ?, ?)
                """;
        try {
            int examId = resolveExamId(payment.getCandidateId());
            if (examId <= 0) {
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
                ps.setDouble(4, payment.getAmount());
                ps.setInt(5, payment.getCandidateId());
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
    public double sumCompletedPaymentsByUserId(int userId) {
        String sql = """
                SELECT ISNULL(SUM(p.TotalAmount), 0) AS totalPaid
                FROM RegistrantPayment p
                INNER JOIN Candidate c ON c.CandidateId = p.CandidateId
                WHERE c.UserId = ?
                  AND p.PaymentStatus IN (N'Completed', N'Paid')
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("totalPaid");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public boolean existsCompletedByTransactionReference(String transactionReference) {
        if (transactionReference == null || transactionReference.isBlank()) {
            return false;
        }
        String sql = """
                SELECT TOP 1 1
                FROM RegistrantPayment
                WHERE TransactionReference = ?
                  AND PaymentStatus IN (N'Completed', N'Paid')
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, transactionReference.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private int resolveExamId(int candidateId) throws SQLException {
        String sql = "SELECT TOP 1 ExamId FROM Exam_Candidate WHERE CandidateId = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
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
