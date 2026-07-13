package auth.dto;

import shared.model.Profile;
import shared.model.Role;
import shared.model.User;

public class UserDTO {

    private int userId;
    private String username;
    private String email;
    private int roleId;
    private boolean active;
    private Role role;
    private Profile profile;

    public UserDTO() {
    }

    // Map authenticated User to session DTO without password hash
    public static UserDTO fromUser(User user) {
        if (user == null) {
            return null;
        }
        UserDTO dto = new UserDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRoleId(user.getRoleId());
        dto.setActive(user.isActive());
        dto.setRole(user.getRole());
        return dto;
    }

    // Build User for service APIs that still expect shared.model.User (no passwordHash)
    public User toUser() {
        User user = new User();
        user.setUserId(userId);
        user.setUsername(username);
        user.setEmail(email);
        user.setRoleId(roleId);
        user.setActive(active);
        user.setRole(role);
        return user;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
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

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }
}
