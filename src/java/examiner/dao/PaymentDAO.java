package examiner.dao;



import examiner.model.Payment;



public interface PaymentDAO {



    boolean insert(Payment payment);



    boolean hasCompletedPayment(int examEnrollmentId);

}

