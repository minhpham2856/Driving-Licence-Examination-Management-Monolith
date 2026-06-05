package registrant.service;

import auth.dto.UserDTO;
import jakarta.servlet.http.HttpServletRequest;

public interface RegistrantTrackProfileService {
    void copyTrackingToRequest(UserDTO user, HttpServletRequest request);
}
