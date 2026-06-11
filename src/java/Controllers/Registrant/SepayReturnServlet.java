package Controllers.Registrant;

import Models.User;
import Utils.SepayReturnHtml;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * <b>Trang trình duyệt quay về</b> sau khi thí sinh xong/hủy/lỗi trên cổng thanh toán.
 *
 * <p><b>Quan trọng:</b> Servlet này <u>không</u> đánh dấu đã thanh toán trong database.
 * SEPay redirect browser tới success_url ngay cả khi IPN chưa kịp tới — trạng thái thật
 * chỉ đổi khi {@link SepayIpnServlet} xử lý ORDER_PAID.</p>
 *
 * <p>URL mapping (cùng một class, khác path):</p>
 * <ul>
 *   <li>{@code /registrant/payment/sepay-success} — thanh toán OK trên cổng</li>
 *   <li>{@code /registrant/payment/sepay-cancel} — thí sinh hủy</li>
 *   <li>{@code /registrant/payment/sepay-error} — lỗi giao dịch</li>
 * </ul>
 *
 * <p>Các URL trên được gắn vào form checkout qua {@code sepay.returnBaseUrl} trong .env.</p>
 * <p>Không dùng {@link RegistrantAuth#requireUser} — trang public, có link đăng nhập.</p>
 */
@WebServlet(urlPatterns = {
        "/registrant/payment/sepay-success",
        "/registrant/payment/sepay-error",
        "/registrant/payment/sepay-cancel"
})
public class SepayReturnServlet extends HttpServlet {

    /** SEPay kiểm tra URL tồn tại (HEAD) trước khi chấp nhận success_url trong merchant config. */
    @Override
    protected void doHead(HttpServletRequest request, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_OK);
    }

    /**
     * Chọn title/message theo path; nếu vẫn còn session login thì set flash success cho my-exams.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String servletPath = request.getServletPath();
        String title;
        String message;

        if (servletPath.endsWith("sepay-success")) {
            title = "Thanh toán thành công";
            message = "Giao dịch đã được ghi nhận. Hệ thống cập nhật trạng thái đăng ký trong vài giây. "
                    + "Vui lòng đăng nhập và kiểm tra mục Kỳ thi của tôi.";
            // Cùng browser còn cookie session → gắn flash cho trang my-exams sau khi vào lại
            User user = getOptionalUser(request);
            if (user != null) {
                request.getSession().setAttribute("successMessage",
                        "Thanh toán thành công. Đăng ký sẽ hiển thị sau khi hệ thống xác nhận giao dịch.");
            }
        } else if (servletPath.endsWith("sepay-cancel")) {
            title = "Đã hủy thanh toán";
            message = "Bạn đã hủy thanh toán. Đăng ký vẫn ở trạng thái chờ thanh toán trong thời gian quy định. "
                    + "Đăng nhập và vào Kỳ thi của tôi để hoàn tất thanh toán.";
        } else {
            title = "Thanh toán thất bại";
            message = "Giao dịch không thành công. Bạn có thể đăng nhập và thử thanh toán lại.";
        }

        SepayReturnHtml.write(response, title, message, request.getContextPath());
    }

    /** Không bắt buộc login — chỉ đọc user nếu session còn để gắn flash message. */
    private static User getOptionalUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return (User) session.getAttribute("user");
    }
}
