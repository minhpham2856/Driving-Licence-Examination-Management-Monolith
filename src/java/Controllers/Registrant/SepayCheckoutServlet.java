package Controllers.Registrant;

import Models.User;
import Services.Impl.PendingPaymentServiceImpl;
import Services.PendingPaymentService;
import Utils.SepayCheckoutHtml;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

/**
 * <b>Thanh toán lại</b> khi lần auto-submit từ RegisterExamServlet bị gián đoạn.
 *
 * <p>Sau POST đăng ký thành công, {@link RegisterExamServlet} lưu URL + field checkout vào session.
 * Nếu trình duyệt chặn redirect hoặc mạng lỗi, thí sinh có thể mở URL này (cần đăng nhập) để
 * phát lại cùng form thanh toán.</p>
 *
 * <p>Session keys (public để RegisterExamServlet ghi cùng tên):</p>
 * <ul>
 *   <li>{@link #SESSION_CHECKOUT_URL} — action form POST</li>
 *   <li>{@link #SESSION_CHECKOUT_FIELDS} — hidden fields + signature</li>
 * </ul>
 */
@WebServlet("/registrant/payment/sepay-checkout")
public class SepayCheckoutServlet extends HttpServlet {

    private final PendingPaymentService pendingPaymentService = new PendingPaymentServiceImpl();

    /** Tên attribute session — URL pay.sepay.vn hoặc sandbox. */
    public static final String SESSION_CHECKOUT_URL = "sepayCheckoutUrl";
    /** Map field checkout đã ký — tái sử dụng không cần build lại từ DB. */
    public static final String SESSION_CHECKOUT_FIELDS = "sepayCheckoutFields";

    /**
     * Đọc checkout từ session → render lại {@link SepayCheckoutHtml}.
     * Session hết hạn (~30 phút Tomcat) → redirect register-exam với thông báo.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = RegistrantAuth.requireUser(request, response, "Bạn cần đăng nhập để thanh toán.");
        if (user == null) {
            return;
        }
        if (user.getPersonId() != null) {
            pendingPaymentService.expireOverdueForPerson(user.getPersonId());
        }

        HttpSession session = request.getSession(false);
        String checkoutUrl = session != null ? (String) session.getAttribute(SESSION_CHECKOUT_URL) : null;
        @SuppressWarnings("unchecked")
        Map<String, String> fields = session != null
                ? (Map<String, String>) session.getAttribute(SESSION_CHECKOUT_FIELDS)
                : null;

        if (checkoutUrl == null || checkoutUrl.isBlank() || fields == null || fields.isEmpty()) {
            request.getSession().setAttribute("errorMessage",
                    "Phiên thanh toán đã hết hạn. Vui lòng đăng ký và thanh toán lại.");
            response.sendRedirect(request.getContextPath() + "/registrant/register-exam");
            return;
        }

        SepayCheckoutHtml.writeAutoSubmitForm(response, checkoutUrl, fields);
    }
}
