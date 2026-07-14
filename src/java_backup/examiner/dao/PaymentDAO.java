package examiner.dao;



import shared.model.Payment;



public interface PaymentDAO {



    boolean insert(Payment payment);



    boolean hasCompletedPayment(int examEnrollmentId);

}


