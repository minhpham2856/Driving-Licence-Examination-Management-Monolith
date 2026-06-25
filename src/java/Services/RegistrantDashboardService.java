package Services;

import Models.User;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

public interface RegistrantDashboardService {
    Map<String, Object> buildDashboardModel(User user, HttpServletRequest request);

    void copyToRequest(Map<String, Object> model, HttpServletRequest request);
}
