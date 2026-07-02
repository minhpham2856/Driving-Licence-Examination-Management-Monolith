package service;

import model.user.User;
import jakarta.servlet.http.HttpServletRequest;

public interface RegistrantTrackProfileService {
    void copyTrackingToRequest(User user, HttpServletRequest request);
}
