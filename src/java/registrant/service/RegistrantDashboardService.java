package registrant.service;

import auth.dto.UserDTO;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

public interface RegistrantDashboardService {
    /** Xây model dashboard (stats, ca thi, hoạt động, CTA) cho thí sinh. */
    Map<String, Object> buildDashboardModel(UserDTO user, HttpServletRequest request);

    /** Đẩy các key của model lên request attribute để JSP render. */
    void copyToRequest(Map<String, Object> model, HttpServletRequest request);
}
