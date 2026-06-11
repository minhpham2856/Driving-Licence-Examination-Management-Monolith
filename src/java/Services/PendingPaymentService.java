package Services;

import Models.PendingRegistrationContext;
import Models.User;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;

public interface PendingPaymentService {

    /** Hủy các đăng ký Pending quá hạn của thí sinh (gọi trước đăng ký / my-exams / resume). */
    void expireOverdueForPerson(int personId);

    Optional<PendingRegistrationContext> findResumable(int personId, int registrationId);

    /**
     * Chuẩn bị form checkout cho đăng ký đang chờ thanh toán.
     *
     * @return null nếu OK và đã set attribute sepayRedirect; message lỗi nếu không resume được
     */
    String prepareResumeCheckout(HttpServletRequest request, User user, PendingRegistrationContext pending);

    void storeCheckoutSession(HttpServletRequest request, String checkoutUrl, Map<String, String> fields);
}
