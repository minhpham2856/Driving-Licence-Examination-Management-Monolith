package dto.user;

public class CreateUserResultDTO {

    private final boolean success;
    private final String message;
    private final String username;
    private final String password;

    public CreateUserResultDTO(boolean success, String message, String username, String password) {
        this.success = success;
        this.message = message;
        this.username = username;
        this.password = password;
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
}
