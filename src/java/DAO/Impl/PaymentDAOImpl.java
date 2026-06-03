package DAO.Impl;

import DBConnection.DBContext;
import DAO.PaymentDAO;
import Models.Payment;
import java.sql.*;

public class PaymentDAOImpl extends DBContext implements PaymentDAO {

    @Override
    public boolean insert(Payment payment) {
        String sql = """
                     insert into Payment (examRegistrationId, amount, paymentStatus, paymentMethod, paymentDate, transactionReference, notes)
                     values (?, ?, ?, ?, getutcdate(), ?, ?)
                     """;
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, payment.getExamRegistrationId());
            ps.setDouble(2, payment.getAmount());
            ps.setString(3, payment.getPaymentStatus() != null ? payment.getPaymentStatus() : "Completed");
            ps.setString(4, payment.getPaymentMethod() != null ? payment.getPaymentMethod() : "Cash");
            
            if (payment.getTransactionReference() == null) {
                ps.setNull(5, Types.NVARCHAR);
            } else {
                ps.setString(5, payment.getTransactionReference());
            }

            if (payment.getNotes() == null) {
                ps.setNull(6, Types.NVARCHAR);
            } else {
                ps.setString(6, payment.getNotes());
            }

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) {
                        payment.setId(gk.getInt(1));
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
