package auth.dto;

import shared.model.Profile;

/** View tài khoản nội bộ: UserDTO + Profile để bind JSP. */
public class StaffAccountViewDTO {

    private UserDTO user;
    private Profile profile;

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }
}
