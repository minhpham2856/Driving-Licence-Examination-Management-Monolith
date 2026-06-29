package dao.impl;


import dbconnection.DBContext;

import dao.PaymentDAO;

import model.Payment;
import java.sql.*;


public class PaymentDAOImpl extends DBContext implements PaymentDAO {

    
    @Override
    public boolean insert(Payment payment) {
        String sql = """
                INSERT INTO Payment (PaymentStatus, PaymentMethod, TransactionReference, TotalAmount, PaidAt, CandidateId, ExamId)
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
                ps.setDouble(4, payment.getTotalAmount());
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
