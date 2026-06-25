package registrant.util;

import auth.dto.UserDTO;
import shared.model.Profile;

/** Session helpers for registrant portal (UserDTO + profile context). */
public final class RegistrantSessionSupport {

    private RegistrantSessionSupport() {
    }

    public static int profileId(UserDTO user) {
        if (user == null) {
            return 0;
        }
        if (user.getProfileId() != null && user.getProfileId() > 0) {
            return user.getProfileId();
        }
        Profile profile = user.getProfile();
        return profile != null && profile.getProfileId() > 0 ? profile.getProfileId() : 0;
    }

    public static void setProfileId(UserDTO user, int profileId) {
        if (user == null) {
            return;
        }
        user.setProfileId(profileId);
        if (user.getProfile() != null) {
            user.getProfile().setProfileId(profileId);
        }
    }

    public static void attachProfile(UserDTO user, Profile profile) {
        if (user == null) {
            return;
        }
        user.setProfile(profile);
        if (profile != null && profile.getProfileId() > 0) {
            RegistrantSessionSupport.setProfileId(user, profile.getProfileId());
        }
    }
}
