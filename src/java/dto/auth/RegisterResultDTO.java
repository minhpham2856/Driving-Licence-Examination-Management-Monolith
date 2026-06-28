package dto.auth;


public class RegisterResultDTO {

    private final boolean success;
    private final String errorMessage;
    private final String username;
    private final String password;
    private final boolean emailSent;

    private RegisterResultDTO(boolean success, String errorMessage,
            String username, String password, boolean emailSent) {
        this.success = success;
        this.errorMessage = errorMessage;
        this.username = username;
        this.password = password;
        this.emailSent = emailSent;
    }

    public static RegisterResultDTO failed(String message) {
        return new RegisterResultDTO(false, message, null, null, false);
    }

    public static RegisterResultDTO succeeded(String username, String password, boolean emailSent) {
        return new RegisterResultDTO(true, null, username, password, emailSent);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isEmailSent() {
        return emailSent;
    }
}
