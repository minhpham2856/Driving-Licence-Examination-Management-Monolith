package registrant.controller;

import auth.dto.UserDTO;
import registrant.service.RegistrantDashboardService;
import registrant.service.impl.RegistrantDashboardServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Dashboard Registrant — {@code GET /registrant/dashboard}.
 * <p>
 * Service build model rồi copy sang request:
 * <ul>
 *   <li>Stats ca thi / hồ sơ từ {@code RegistrantDAO}</li>
 *   <li>{@code totalFee} = {@link payment.dao.PaymentDAO#sumCompletedPaymentsByUserId}
 *       (SUM Payment hoàn tất — Cash <b>và</b> SePay — join Profile↔Candidate qua CCCD)</li>
 *   <li>CTA / upcoming / filter danh sách</li>
 * </ul>
 * Registrant <b>không</b> tự checkout SePay; chỉ <b>đọc</b> tiền đã thu tại bàn thủ tục.
 */
@WebServlet("/registrant/dashboard")
public class DashboardServlet extends HttpServlet {

    private static final String VIEW = "/views/registrant/dashboard.jsp";

    private final RegistrantDashboardService dashboardService = new RegistrantDashboardServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        UserDTO user = RegistrantAuth.requireRegistrant(request, response);
        if (user == null) {
            return;
        }
        dashboardService.copyToRequest(dashboardService.buildDashboardModel(user, request), request);
        RegistrantServletSupport.forwardView(request, response, VIEW);
    }
}
