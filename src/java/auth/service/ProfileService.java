package auth.service;

import auth.dto.ServiceResult;
import auth.dto.StaffAccountViewDTO;
import auth.dto.UpdateProfileDTO;
import shared.model.Profile;

public interface ProfileService {

    Profile getByUserId(int userId);

    StaffAccountViewDTO getAccountView(int userId);

    ServiceResult<Profile> updateMyProfile(int userId, UpdateProfileDTO input);
}
