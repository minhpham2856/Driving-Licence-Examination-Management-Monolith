package Services.Impl;

import Config.PaymentExpiryConfig;
import Controllers.Registrant.SepayCheckoutServlet;
import DAO.PaymentDAO;
import DAO.Impl.PaymentDAOImpl;
import Models.PendingRegistrationContext;
import Models.User;
import Services.PendingPaymentService;
import Services.SepayPaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

public class PendingPaymentServiceImpl implements PendingPaymentService {

    private static final ZoneId DISPLAY_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter DEADLINE_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy").withZone(DISPLAY_ZONE);

    private final PaymentDAO paymentDAO = new PaymentDAOImpl();
    private final SepayPaymentService sepayPaymentService = new SepayPaymentServiceImpl();

    @Override
    public void expireOverdueForPerson(int personId) {
        paymentDAO.cancelOverduePendingForPerson(personId);
        paymentDAO.cancelRegistrationsForOverduePayments(personId);
    }

    @Override
    public Optional<PendingRegistrationContext> findResumable(int personId, int registrationId) {
        expireOverdueForPerson(personId);
        return paymentDAO.findResumablePending(personId, registrationId);
    }

    @Override
    public String prepareResumeCheckout(HttpServletRequest request, User user, PendingRegistrationContext pending) {
        if (pending == null || user.getPersonId() == null || pending.getPersonId() != user.getPersonId()) {
            return "Không tìm thấy đơn chờ thanh toán.";
        }
        if (!isStillResumable(pending)) {
            return "Phiên thanh toán đã hết hạn. Vui lòng đăng ký lại.";
        }

        String configError = sepayPaymentService.configurationErrorForUser();
        if (configError != null) {
            return configError;
        }

        try {
            Map<String, String> checkoutFields = sepayPaymentService.buildCheckoutFields(
                    request,
                    user,
                    pending.getInvoiceNumber(),
                    pending.getLicenceCode(),
                    pending.getSessionName(),
                    pending.getAmount());

            request.setAttribute("sepayRedirect", Boolean.TRUE);
            request.setAttribute("sepayCheckoutUrl", sepayPaymentService.getCheckoutUrl());
            request.setAttribute("sepayCheckoutFields", checkoutFields);
            storeCheckoutSession(request, sepayPaymentService.getCheckoutUrl(), checkoutFields);
            return null;
        } catch (Exception ex) {
            return "Không thể khởi tạo thanh toán. Vui lòng thử lại sau.";
        }
    }

    @Override
    public void storeCheckoutSession(HttpServletRequest request, String checkoutUrl, Map<String, String> fields) {
        HttpSession session = request.getSession();
        session.setAttribute(SepayCheckoutServlet.SESSION_CHECKOUT_URL, checkoutUrl);
        session.setAttribute(SepayCheckoutServlet.SESSION_CHECKOUT_FIELDS, fields);
    }

    public static Timestamp expiryFromNow() {
        int minutes = PaymentExpiryConfig.getWindowMinutes();
        return Timestamp.from(Instant.now().plusSeconds(minutes * 60L));
    }

    public static boolean isStillResumable(PendingRegistrationContext pending) {
        if (pending == null || pending.getPaymentExpiresAt() == null) {
            return false;
        }
        return pending.getPaymentExpiresAt().toInstant().isAfter(Instant.now());
    }

    public static String formatDeadline(Timestamp expiresAt) {
        if (expiresAt == null) {
            return "";
        }
        return DEADLINE_FORMAT.format(expiresAt.toInstant());
    }
}
