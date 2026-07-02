package controller.registrant;

import model.user.User;
import service.RegistrantDashboardService;
import service.impl.RegistrantDashboardServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/registrant/dashboard")
public class DashboardServlet extends HttpServlet {

    private static final String VIEW = "/views/registrant/dashboard.jsp";

    private final RegistrantDashboardService dashboardService = new RegistrantDashboardServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = RegistrantAuth.requireRegistrant(request, response);
        if (user == null) {
            return;
        }
        dashboardService.copyToRequest(dashboardService.buildDashboardModel(user, request), request);
        RegistrantServletSupport.forwardView(request, response, VIEW);
    }
}
