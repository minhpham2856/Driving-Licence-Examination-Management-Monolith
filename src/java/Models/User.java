package Models;

import java.sql.Timestamp;

public class User {

    private int id;
    private Integer profileId;
    private String username;
    private String email;
    private String passwordHash;
    private int roleId;
    private boolean isActive;
    private Timestamp lastLoginAt;
    private Timestamp createdAt;
    private Profile profile;
    private Role role;

    public User() {
    }

    public User(int id, Integer profileId, String username, String passwordHash, int roleId,
            boolean isActive, Timestamp lastLoginAt, Timestamp createdAt) {
        this.id = id;
        this.profileId = profileId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.roleId = roleId;
        this.isActive = isActive;
        this.lastLoginAt = lastLoginAt;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getProfileId() {
        return profileId;
    }

    public void setProfileId(Integer profileId) {
        this.profileId = profileId;
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

    public boolean isIsActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public Timestamp getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(Timestamp lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
