package payment.dao.impl;

import examstaff.enums.PaymentStatus;
import payment.dao.PaymentDAO;
import payment.dto.PaymentRecord;
import shared.dbconnection.DBContext;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Ghi/đọc bảng Payment — cần ExamEnrollmentId (staff tạo enrollment trước). */
public class PaymentDAOImpl extends DBContext implements PaymentDAO {

    private static final Logger LOG = Logger.getLogger(PaymentDAOImpl.class.getName());

    @Override
    public boolean insert(PaymentRecord payment) {
        // Ưu tiên enrollmentId có sẵn; nếu thiếu thì suy từ CandidateId
        int enrollmentId = payment.getExamEnrollmentId();
        if (enrollmentId <= 0 && payment.getCandidateId() > 0) {
            enrollmentId = resolveEnrollmentId(payment.getCandidateId());
        }
        if (enrollmentId <= 0) {
            return false; // chưa enroll ngày thi → không ghi Payment
        }

        String sql = """
                INSERT INTO Payment (PaymentStatus, PaymentMethod, TransactionReference, TotalAmount, PaidAt, ExamEnrollmentId)
                VALUES (?, ?, ?, ?, GETDATE(), ?)
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, payment.getPaymentStatus() != null
                    ? payment.getPaymentStatus()
                    : PaymentStatus.HOAN_TAT.getDisplayName());
            ps.setString(2, payment.getPaymentMethod() != null ? payment.getPaymentMethod() : "SePay");
            if (payment.getTransactionReference() == null) {
                ps.setNull(3, Types.NVARCHAR);
            } else {
                ps.setString(3, payment.getTransactionReference());
            }
            ps.setDouble(4, payment.getAmount());
            ps.setInt(5, enrollmentId);

            if (ps.executeUpdate() > 0) {
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) {
                        payment.setId(gk.getInt(1));
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
    public double sumCompletedPaymentsByUserId(int userId) {
        // Profile ↔ Candidate qua CCCD (không có Candidate.UserId)
        String sql = """
                SELECT ISNULL(SUM(p.TotalAmount), 0) AS totalPaid
                FROM Payment p
                INNER JOIN ExamEnrollment ee ON ee.ExamEnrollmentId = p.ExamEnrollmentId
                INNER JOIN Candidate c ON c.CandidateId = ee.CandidateId
                INNER JOIN Profile prof ON prof.GovernmentIdNumber = c.GovernmentIdNumber
                WHERE prof.UserId = ?
                  AND p.PaymentStatus IN (""" + PaymentStatus.sqlInClause() + """
                )
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("totalPaid");
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Failed to sum payments for user " + userId, e);
        }
        return 0;
    }

    @Override
    public boolean existsCompletedByTransactionReference(String transactionReference) {
        if (transactionReference == null || transactionReference.isBlank()) {
            return false;
        }
        // Idempotent IPN: cùng TransactionReference đã Paid thì bỏ qua
        String sql = """
                SELECT TOP 1 1
                FROM Payment
                WHERE TransactionReference = ?
                  AND PaymentStatus IN (""" + PaymentStatus.sqlInClause() + """
                )
                """;
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, transactionReference.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Failed to check payment reference", e);
        }
        return false;
    }

    private int resolveEnrollmentId(int candidateId) {
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
}
