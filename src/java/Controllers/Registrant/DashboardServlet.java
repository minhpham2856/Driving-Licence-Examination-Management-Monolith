package Controllers.Registrant;

import Models.User;
import Services.Impl.RegistrantDashboardServiceImpl;
import Services.RegistrantDashboardService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Trang chủ thí sinh sau đăng nhập.
 * <p>URL: GET /registrant/dashboard</p>
 * <p>Service: RegistrantDashboardServiceImpl — thống kê đăng ký, kỳ thi sắp tới, phí đã nộp, hoạt động.</p>
 * <p>JSP: views/registrant/dashboard.jsp</p>
 */
@WebServlet("/registrant/dashboard")
public class DashboardServlet extends HttpServlet {

    private final RegistrantDashboardService dashboardService = new RegistrantDashboardServiceImpl();

    /** Tải widget thống kê, kỳ thi sắp tới, feed hoạt động rồi forward dashboard.jsp. */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = RegistrantAuth.requireUser(request, response, "Bạn cần đăng nhập để truy cập dashboard.");
        if (user == null) {
            return;
        }

        dashboardService.populateDashboard(request, user);
        request.getRequestDispatcher("/views/registrant/dashboard.jsp").forward(request, response);
    }
}
