package service;

import dto.RegisterResultDTO;
import dto.ServiceResult;
import model.Profile;
import model.User;

public interface AuthService {

    ServiceResult<RegisterResultDTO> register(Profile profile, String email);

    User login(String identifier, String password);

    String forgotPassword(String email);

    ServiceResult<Void> changePassword(int userId, String currentPassword, String newPassword,
            String confirmPassword);
}
