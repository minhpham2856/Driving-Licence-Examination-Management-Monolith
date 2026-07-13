package auth.service;

import auth.dto.RegisterResultDTO;
import auth.model.Profile;
import auth.model.User;
import dto.ServiceResult;

public interface AuthService {

    ServiceResult<RegisterResultDTO> register(Profile profile, String email);

    User login(String identifier, String password);

    ServiceResult<Void> forgotPassword(String email);

    ServiceResult<Void> changePassword(int userId, String currentPassword, String newPassword, String confirmPassword);
}
