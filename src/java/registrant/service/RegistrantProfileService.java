package registrant.service;

import shared.model.Profile;
import auth.dto.UserDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public interface RegistrantProfileService {
    void copyProfileToRequest(UserDTO user, HttpServletRequest request);

    boolean updateProfile(UserDTO user, Profile updated, HttpSession session);

    String validateProfileUpdate(UserDTO user, Profile updated);

    boolean hasRejectedHealthDocument(int profileId);
}
