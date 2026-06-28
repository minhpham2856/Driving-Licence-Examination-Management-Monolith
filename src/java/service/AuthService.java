package service;

import dto.auth.ChangePasswordResultDTO;
import dto.auth.RegisterResultDTO;

import model.user.User;

public interface AuthService {

    RegisterResultDTO register(String govIdNo, String fullName, String phoneNo, String dateOfBirth,
            String address, String email, boolean sex);

    User login(String identifier, String password);

    String forgotPassword(String email);

    ChangePasswordResultDTO changePassword(int userId, String currentPwd, String newPwd, String confirmPwd);

}
