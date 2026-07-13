package auth.dto;

public class RegisterResultDTO {
    private String username;
    private String password;
    private boolean emailSent;
    private int userId;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public boolean isEmailSent() { return emailSent; }
    public void setEmailSent(boolean emailSent) { this.emailSent = emailSent; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
}
