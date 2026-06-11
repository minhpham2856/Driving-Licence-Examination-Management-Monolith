package Services;

import Models.User;
import jakarta.servlet.http.HttpServletRequest;

public interface RegistrantSettingsService {

    void populateSettings(HttpServletRequest request, User user);

    String changePassword(HttpServletRequest request, User user);

    String deactivateAccount(HttpServletRequest request, User user);
}
