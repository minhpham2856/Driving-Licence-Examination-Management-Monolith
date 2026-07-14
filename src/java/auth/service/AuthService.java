package auth.service;

import auth.dto.RegisterResultDTO;
import shared.model.Profile;
import shared.model.User;
import auth.dto.ServiceResult;

public interface AuthService {

    ServiceResult<RegisterResultDTO> register(Profile profile, String email);

    User login(String identifier, String password);

    ServiceResult<Void> forgotPassword(String email);

    ServiceResult<Void> changePassword(int userId, String currentPassword, String newPassword, String confirmPassword);

    boolean verifyPassword(int userId, String rawPassword);
}

