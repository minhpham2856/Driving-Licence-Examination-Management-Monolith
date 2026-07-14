package payment.service;

import payment.dto.sepay.SePayCheckoutRequest;
import payment.dto.sepay.SePayCheckoutSession;
import payment.dto.sepay.SePayIpnResult;
import payment.dto.sepay.SePayPaymentException;

/** Facade SePay: checkout (createCheckout → buildAutoSubmitHtml), IPN handleIpn, invoice nhúng CandidateId. */
public interface SePayPaymentService {

    boolean isConfigured();

    boolean sandbox();

    SePayCheckoutSession createCheckout(SePayCheckoutRequest request) throws SePayPaymentException;

    String buildAutoSubmitHtml(SePayCheckoutSession session);

    String generateInvoiceNumber(String businessPrefix, long internalOrderId);

    SePayIpnResult handleIpn(String rawBody, String secretHeader,
            String signatureHeader, String timestampHeader);
}
