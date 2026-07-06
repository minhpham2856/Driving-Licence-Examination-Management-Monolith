package dto.payload;

public class CreateUserData {

    private Integer profileId;
    private Integer userId;
    private String username;
    private String password;

    public CreateUserData() {
    }

    public CreateUserData(Integer profileId, Integer userId, String username, String password) {
        this.profileId = profileId;
        this.userId = userId;
        this.username = username;
        this.password = password;
    }

    public Integer getProfileId() {
        return profileId;
    }

    public void setProfileId(Integer profileId) {
        this.profileId = profileId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
