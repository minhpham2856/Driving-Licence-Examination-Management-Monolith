package Services;

import Models.Profile;
import Models.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public interface RegistrantProfileService {
    void copyProfileToRequest(User user, HttpServletRequest request);

    boolean updateProfile(User user, Profile updated, HttpSession session);

    String validateProfileUpdate(User user, Profile updated);

    boolean hasRejectedHealthDocument(int profileId);
}
