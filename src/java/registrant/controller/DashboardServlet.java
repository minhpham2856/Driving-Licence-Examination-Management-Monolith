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

/** Dashboard GET /registrant/dashboard — thống kê ca thi, tổng SePay, tiến độ hồ sơ, action items. */
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
