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
 * Trang tổng quan cổng thí sinh — GET /registrant/dashboard, forward dashboard.jsp.
 * Luồng: RegistrantAuth.requireRegistrant → RegistrantDashboardService.buildDashboardModel → copyToRequest → forward JSP.
 * Service gom stats hồ sơ/ca thi, totalFee (Payment đã hoàn tất), registeredExamList, hoạt động gần đây, ca sắp tới và panel việc cần làm.
 * Thí sinh chỉ đọc tiền đã thu tại bàn thủ tục — không checkout SePay từ đây.
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
