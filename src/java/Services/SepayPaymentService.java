package Services;

import Models.User;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

public interface SepayPaymentService {

    boolean isReady();

    /** Chi tiết kỹ thuật — chỉ ghi log, không hiển thị cho thí sinh. */
    String configurationError();

    /** Thông báo ngắn, an toàn hiển thị trên UI đăng ký thi. */
    String configurationErrorForUser();

    Map<String, String> buildCheckoutFields(
            HttpServletRequest request,
            User user,
            String orderInvoiceNumber,
            String licenceCode,
            String sessionName,
            BigDecimal amount);

    String getCheckoutUrl();

    boolean verifyIpnSecret(String headerSecret);

    boolean handleOrderPaid(String orderInvoiceNumber, String orderAmountRaw);
}
