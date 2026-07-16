package examstaff.dto;

import shared.model.Profile;

public class UserDTO {
    private int userId;
    private String username;
    private Profile profile;

    public UserDTO() {
    }

    public UserDTO(int userId, String username, Profile profile) {
        this.userId = userId;
        this.username = username;
        this.profile = profile;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getId() {
        return userId;
    }

    public void setId(int id) {
        this.userId = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }
}
