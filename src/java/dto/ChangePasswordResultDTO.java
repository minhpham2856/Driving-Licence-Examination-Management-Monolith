package dto;
public class ChangePasswordResultDTO {
    public final boolean success;
    public final String message;
    public ChangePasswordResultDTO(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
