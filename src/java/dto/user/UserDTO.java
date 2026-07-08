package dto.user;


import model.User;
import model.Profile;
import model.Role;

public class UserDTO extends User {
    private Profile profile;

    public UserDTO() {
        super();
    }

    public UserDTO(int id, String username, String email, String passwordHash, int roleId, boolean isActive, Profile profile) {
        super(id, username, email, passwordHash, roleId, isActive);
        this.profile = profile;
    }

    public int getId() {
        return getUserId();
    }

    public void setId(int id) {
        setUserId(id);
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }
}
