package DTOs;

public class RegisterResult {

    private final boolean success;
    private final String errorMessage;
    private final String username;
    private final String password;
    private final boolean emailSent;

    private RegisterResult(boolean success, String errorMessage, String username, String password,
            boolean emailSent) {
        this.success = success;
        this.errorMessage = errorMessage;
        this.username = username;
        this.password = password;
        this.emailSent = emailSent;
    }

    public static RegisterResult failed(String message) {
        return new RegisterResult(false, message, null, null, false);
    }

    public static RegisterResult succeeded(String username, String password, boolean emailSent) {
        return new RegisterResult(true, null, username, password, emailSent);
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
