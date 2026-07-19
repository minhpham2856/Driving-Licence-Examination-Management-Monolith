package registrant.service;

import auth.dto.UserDTO;
import jakarta.servlet.http.HttpServletRequest;

public interface RegistrantTrackProfileService {
    /** Gom audit + tài liệu thành timeline theo dõi hồ sơ và đẩy lên request. */
    void copyTrackingToRequest(UserDTO user, HttpServletRequest request);
}
