package shared.dto;

public class ExamAccessOtpDTO {

    private String code;
    private long expiresAtEpochSecond;

    public ExamAccessOtpDTO() {
    }

    public ExamAccessOtpDTO(String code, long expiresAtEpochSecond) {
        this.code = code;
        this.expiresAtEpochSecond = expiresAtEpochSecond;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public long getExpiresAtEpochSecond() {
        return expiresAtEpochSecond;
    }

    public void setExpiresAtEpochSecond(long expiresAtEpochSecond) {
        this.expiresAtEpochSecond = expiresAtEpochSecond;
    }
}
