package examstaff.dao.impl;

import shared.dbconnection.DBContext;
import examstaff.dao.PaymentDAO;
import examstaff.enums.PaymentStatus;
import shared.model.Payment;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Triển khai JDBC của {@link PaymentDAO} — ghi/đọc bảng {@code Payment}. */
public class PaymentDAOImpl extends DBContext implements PaymentDAO {

    private static final Logger LOG = Logger.getLogger(PaymentDAOImpl.class.getName());

    /**
     * Thêm bản ghi thanh toán mới vào bảng {@code Payment}.
     * Ghi {@code PaymentStatus}, {@code PaymentMethod}, {@code TransactionReference},
     * {@code TotalAmount}, {@code PaidAt} (GETDATE), {@code ExamEnrollmentId}.
     *
     * @param payment entity thanh toán (bắt buộc có {@code ExamEnrollmentId} hợp lệ)
     * @return {@code true} nếu INSERT thành công và lấy được {@code PaymentId} sinh ra
     */
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
        // Chuẩn bị PreparedStatement với SQL INSERT Payment
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // Gán tham số truy vấn
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
            // Thực thi INSERT và lấy khóa sinh
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

    /**
     * Lấy thanh toán mới nhất của thí sinh từ {@code Payment} JOIN {@code ExamEnrollment}.
     *
     * @param candidateId mã thí sinh ({@code CandidateId})
     * @return entity {@link Payment} hoặc {@code null} nếu không có bản ghi
     */
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
        // Chuẩn bị PreparedStatement với SQL SELECT thanh toán theo CandidateId
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            // Gán tham số truy vấn
            ps.setInt(1, candidateId);
            // Thực thi và lấy ResultSet
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Ánh xạ ResultSet → đối tượng Payment
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
        // Không tìm thấy bản ghi
        return null;
    }

    /**
     * Tra {@code ExamEnrollmentId} mới nhất của thí sinh từ bảng {@code ExamEnrollment}.
     *
     * @param candidateId mã thí sinh
     * @return mã ghi danh mới nhất, hoặc {@code -1} nếu không có
     */
    @Override
    public int resolveEnrollmentId(int candidateId) {
        String sql = """
                SELECT TOP 1 ExamEnrollmentId
                FROM ExamEnrollment
                WHERE CandidateId = ?
                ORDER BY ExamEnrollmentId DESC
                """;
        // Chuẩn bị PreparedStatement với SQL SELECT ExamEnrollmentId
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            // Gán tham số truy vấn
            ps.setInt(1, candidateId);
            // Thực thi và lấy ResultSet
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ExamEnrollmentId");
                }
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Failed to resolve enrollment for candidate " + candidateId, e);
        }
        // Không tìm thấy bản ghi
        return -1;
    }
}
