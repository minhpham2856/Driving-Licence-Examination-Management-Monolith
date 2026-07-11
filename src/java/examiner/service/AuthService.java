package examiner.service;

import examiner.dto.RegisterResultDTO;
import examiner.dto.ServiceResult;
import examiner.model.Profile;
import examiner.model.User;

public interface AuthService {

    ServiceResult<RegisterResultDTO> register(Profile profile, String email);

    User login(String identifier, String password);

    String forgotPassword(String email);

    ServiceResult<Void> changePassword(int userId, String currentPassword, String newPassword,
            String confirmPassword);
}
