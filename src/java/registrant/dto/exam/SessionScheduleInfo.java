package registrant.dto.exam;

import java.util.Date;

/** Ngày thi đăng ký - dùng kiểm tra trùng ngày giữa các hạng GPLX. */
public class SessionScheduleInfo {

    private int sessionId;
    private int licenceId;
    private String uiLicenceCode;
    private String sessionName;
    private Date examDate;

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public int getLicenceId() {
        return licenceId;
    }

    public void setLicenceId(int licenceId) {
        this.licenceId = licenceId;
    }

    public String getUiLicenceCode() {
        return uiLicenceCode;
    }

    public void setUiLicenceCode(String uiLicenceCode) {
        this.uiLicenceCode = uiLicenceCode;
    }

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public Date getExamDate() {
        return examDate;
    }

    public void setExamDate(Date examDate) {
        this.examDate = examDate;
    }
}
