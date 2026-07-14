package auth.service;

import auth.dto.ServiceResult;
import auth.dto.AccountDTO;
import auth.dto.UpdateProfileDTO;
import shared.model.Profile;

public interface ProfileService {

    Profile getByUserId(int userId);

    AccountDTO getAccount(int userId);

    ServiceResult<Profile> updateProfile(int userId, UpdateProfileDTO input);
}
