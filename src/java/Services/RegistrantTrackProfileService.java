package Services;

import Models.User;
import jakarta.servlet.http.HttpServletRequest;

public interface RegistrantTrackProfileService {

    void populateTrackProfile(HttpServletRequest request, User user);
}
