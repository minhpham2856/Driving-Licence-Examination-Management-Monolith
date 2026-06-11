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
import java.io.IOException;
import java.util.Map;

/**
 * Tiếp tục thanh toán đăng ký đang ở trạng thái chờ (trong vòng 10–15 phút).
 * URL: GET /registrant/payment/resume?registrationId={id}
 */
@WebServlet("/registrant/payment/resume")
public class ResumePaymentServlet extends HttpServlet {

    private final PendingPaymentService pendingPaymentService = new PendingPaymentServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = RegistrantAuth.requireUser(request, response, "Bạn cần đăng nhập để thanh toán.");
        if (user == null) {
            return;
        }

        String rawId = request.getParameter("registrationId");
        if (rawId == null || rawId.isBlank()) {
            redirectMyExams(request, response, "Không xác định được đơn đăng ký.");
            return;
        }

        int registrationId;
        try {
            registrationId = Integer.parseInt(rawId.trim());
        } catch (NumberFormatException ex) {
            redirectMyExams(request, response, "Mã đăng ký không hợp lệ.");
            return;
        }

        if (user.getPersonId() == null) {
            redirectMyExams(request, response, "Vui lòng hoàn thiện hồ sơ trước khi thanh toán.");
            return;
        }

        var pendingOpt = pendingPaymentService.findResumable(user.getPersonId(), registrationId);
        if (pendingOpt.isEmpty()) {
            redirectMyExams(request, response,
                    "Đơn thanh toán không còn hiệu lực. Có thể đã hết hạn hoặc đã được thanh toán.");
            return;
        }

        String error = pendingPaymentService.prepareResumeCheckout(request, user, pendingOpt.get());
        if (error != null) {
            redirectMyExams(request, response, error);
            return;
        }

        String checkoutUrl = (String) request.getAttribute("sepayCheckoutUrl");
        @SuppressWarnings("unchecked")
        Map<String, String> checkoutFields = (Map<String, String>) request.getAttribute("sepayCheckoutFields");
        SepayCheckoutHtml.writeAutoSubmitForm(response, checkoutUrl, checkoutFields);
    }

    private void redirectMyExams(HttpServletRequest request, HttpServletResponse response, String message)
            throws IOException {
        request.getSession().setAttribute("errorMessage", message);
        response.sendRedirect(request.getContextPath() + "/registrant/my-exams");
    }
}
