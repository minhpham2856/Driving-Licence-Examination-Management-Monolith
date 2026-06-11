package Services;

import Models.User;
import jakarta.servlet.http.HttpServletRequest;

public interface RegistrantDashboardService {

    void populateDashboard(HttpServletRequest request, User user);
}
