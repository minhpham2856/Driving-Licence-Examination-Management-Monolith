package dao.impl;

import dbconnection.DBContext;
import dao.PaymentDAO;
import model.payment.Payment;
import java.sql.*;

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
            try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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
    public double sumCompletedPaymentsByUserId(int userId) {
        /*
         * Gộp tổng tiền qua chuỗi Candidate -> Payment vì bảng Payment
         * chỉ liên kết CandidateId, không trực tiếp ProfileId.
         */
        String sql = """
                SELECT ISNULL(SUM(p.TotalAmount), 0) AS totalPaid
                FROM Payment p
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
