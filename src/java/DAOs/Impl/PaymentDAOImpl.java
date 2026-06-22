package DAOs.Impl;

import DBConnection.DBContext;
import DAOs.PaymentDAO;
import Models.Payment;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

public class PaymentDAOImpl implements PaymentDAO {

    private final DBContext ctx;

    public PaymentDAOImpl() {
        this.ctx = new DBContext();
    }

    @Override
    public boolean insert(Payment payment) {
        String sql = """
                insert into Payment (PaymentStatus, PaymentMethod, TransactionReference, TotalAmount, PaidAt, ExamEnrollmentId)
                values (?, ?, ?, ?, GETDATE(), ?)
                """;

        try {
            try (PreparedStatement ps = ctx.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, payment.getPaymentStatus() != null ? payment.getPaymentStatus() : "Completed");
                ps.setString(2, payment.getPaymentMethod() != null ? payment.getPaymentMethod() : "Cash");

                if (payment.getTransactionReference() == null) {
                    ps.setNull(3, Types.NVARCHAR);
                } else {
                    ps.setString(3, payment.getTransactionReference());
                }

                ps.setDouble(4, payment.getTotalAmount());
                ps.setInt(5, payment.getExamEnrollmentId());

                if (ps.executeUpdate() == 0) {
                    return false;
                }

                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        payment.setPaymentId(generatedKeys.getInt(1));
                    }
                }

                return payment.getPaymentId() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
}
