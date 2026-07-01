package dto;
public class CreateUserResultDTO {
    private final boolean success;
    private final String message;
    private final String username;
    private final String password;
    private final Integer profileId;
    private final Integer userId;
    public CreateUserResultDTO(boolean success, String message, String username, String password) {
        this(success, message, username, password, null, null);
    }
    public CreateUserResultDTO(boolean success, String message, String username, String password,
            Integer profileId, Integer userId) {
        this.success = success;
        this.message = message;
        this.username = username;
        this.password = password;
        this.profileId = profileId;
        this.userId = userId;
    }
    public boolean isSuccess() {
        return success;
    }
    public String getMessage() {
        return message;
    }
    public String getUsername() {
        return username;
    }
    public String getPassword() {
        return password;
    }
    public Integer getProfileId() {
        return profileId;
    }
    public Integer getUserId() {
        return userId;
    }
}
