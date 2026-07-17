package registrant.service;

import auth.dto.UserDTO;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

public interface RegistrantDashboardService {
    Map<String, Object> buildDashboardModel(UserDTO user, HttpServletRequest request);

    void copyToRequest(Map<String, Object> model, HttpServletRequest request);
}
