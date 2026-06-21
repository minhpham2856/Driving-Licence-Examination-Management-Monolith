package DAOs;

import Models.Payment;

/**
 * DAO cho thao tác với thanh toán (Payment) trong hệ thống.
 * Cung cấp phương thức ghi nhận giao dịch thanh toán của thí sinh.
 */
public interface PaymentDAO {

    /**
     * Thêm mới một bản ghi thanh toán.
     *
     * @param payment đối tượng Payment chứa thông tin thanh toán
     * @return true nếu thêm thành công
     */
    boolean insert(Payment payment);
}
