package service;

import dto.ServiceResult;
import dto.payload.ChangePasswordCommand;
import dto.payload.RegisterData;
import model.Profile;
import model.User;

public interface AuthService {

    ServiceResult<RegisterData> register(Profile profile, String email);

    User login(String identifier, String password);

    String forgotPassword(String email);

    ServiceResult<Void> changePassword(ChangePasswordCommand command);
}
