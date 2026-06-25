package DTOs;

import Models.User;
import Models.Profile;
import Models.Role;

public class UserDTO extends User {
    private Profile profile;

    public UserDTO() {
        super();
    }

    public UserDTO(int id, String username, String email, String passwordHash, Role role, int roleId, boolean isActive, Profile profile) {
        super(id, username, email, passwordHash, role, roleId, isActive);
        this.profile = profile;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }
}
