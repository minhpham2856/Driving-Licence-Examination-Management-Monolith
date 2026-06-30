package dao.impl;

import dbconnection.DBContext;

import dao.PaymentDAO;

import model.Payment;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PaymentDAOImpl extends DBContext implements PaymentDAO {

    @Override
    public boolean insert(Payment payment) {
        int enrollmentId = payment.getExamEnrollmentId();
        if (enrollmentId <= 0) {
            try {
                enrollmentId = resolveExamEnrollmentId(payment.getCandidateId());
            } catch (SQLException ex) {
                Logger.getLogger(PaymentDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (enrollmentId <= 0) {
            return false;
        }

        String sql = """
                INSERT INTO Payment (PaymentStatus, PaymentMethod, TransactionReference, TotalAmount, PaidAt, ExamEnrollmentId)
                VALUES (?, ?, ?, ?, GETDATE(), ?)
                """;
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
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) {
                        payment.setId(gk.getInt(1));
                        payment.setExamEnrollmentId(enrollmentId);
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private int resolveExamEnrollmentId(int candidateId) throws SQLException {
        if (candidateId <= 0) {
            return -1;
        }
        String sql = "SELECT TOP 1 ExamEnrollmentId FROM ExamEnrollment WHERE CandidateId = ? ORDER BY ExamEnrollmentId DESC";
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
