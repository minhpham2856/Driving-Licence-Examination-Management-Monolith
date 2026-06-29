package dto;

import model.Profile;

public class UserDTO {

    private int userId;
    private String username;
    private String email;
    private String passwordHash;
    private int roleId;
    private boolean isActive;
    
    private Profile profile;

    public UserDTO() {
    }

    public UserDTO(int userId, String username, String email, String passwordHash, int roleId, boolean isActive, Profile profile) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.roleId = roleId;
        this.isActive = isActive;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }
}
