package Services;

import Models.User;
import jakarta.servlet.http.HttpServletRequest;

public interface RegistrantTrackProfileService {
    void copyTrackingToRequest(User user, HttpServletRequest request);
}
