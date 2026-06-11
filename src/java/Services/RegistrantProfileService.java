package Services;

import Models.User;
import jakarta.servlet.http.HttpServletRequest;

public interface RegistrantProfileService {

    void populateProfile(HttpServletRequest request, User user);

    String saveProfile(HttpServletRequest request, User user);

    User reloadUser(int userId);
}
