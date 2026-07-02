package service;
import dto.ChangePasswordResultDTO;
import dto.RegisterResultDTO;
import model.User;
public interface AuthService {
    RegisterResultDTO register(String govIdNo, String fullName, String phoneNo, String dateOfBirth,
            String address, String email, boolean sex);
    User login(String identifier, String password);
    String forgotPassword(String email);
    ChangePasswordResultDTO changePassword(int userId, String currentPwd, String newPwd, String confirmPwd);
}
