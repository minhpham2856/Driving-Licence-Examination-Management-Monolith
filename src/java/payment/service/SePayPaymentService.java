package payment.service;

import payment.dto.sepay.SePayCheckoutRequest;
import payment.dto.sepay.SePayCheckoutSession;
import payment.dto.sepay.SePayIpnResult;
import payment.dto.sepay.SePayPaymentException;

/** Facade SePay: checkout, IPN, invoice DLEM. */
public interface SePayPaymentService {

    boolean isConfigured();

    boolean sandbox();

    SePayCheckoutSession createCheckout(SePayCheckoutRequest request) throws SePayPaymentException;

    String buildAutoSubmitHtml(SePayCheckoutSession session);

    /** DLEM-{prefix}-{candidateId}-{timestamp} */
    String generateInvoiceNumber(String businessPrefix, long candidateId);

    /** DLEM-{prefix}-{candidateId}-{enrollmentId}-{timestamp} */
    String generateInvoiceNumber(String businessPrefix, long candidateId, long enrollmentId);

    /** {SEPAY_APP_BASE_URL}/payment/sepay/ipn */
    String ipnCallbackUrl();

    SePayIpnResult handleIpn(String rawBody, String secretHeader,
            String signatureHeader, String timestampHeader);
}
