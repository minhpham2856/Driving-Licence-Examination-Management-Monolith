package registrant.dto;

import java.util.Date;

/**
 * DTO một ngày thi dự kiến ({@code ExamDates}) trên wizard đăng ký ({@code register-exam.jsp}).
 * <p>
 * {@code id}/{@code sessionId} = ExamDateId; form POST vẫn dùng tham số {@code sessionSelect}.
 * Gồm tên đợt, hạng, địa điểm và số chỗ còn trống.
 */
public class RegistrantExamSessionOption {

    private String id;
    private String examName;
    private String examCode;
    private String licenceClass;
    private Date examDate;
    private String location;
    private int slotsRemaining;
    /** SessionId thực tế trong DB - dùng nội bộ khi đăng ký. */
    private int sessionId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getExamName() {
        return examName;
    }

    public void setExamName(String examName) {
        this.examName = examName;
    }

    public String getExamCode() {
        return examCode;
    }

    public void setExamCode(String examCode) {
        this.examCode = examCode;
    }

    public String getLicenceClass() {
        return licenceClass;
    }

    public void setLicenceClass(String licenceClass) {
        this.licenceClass = licenceClass;
    }

    public Date getExamDate() {
        return examDate;
    }

    public void setExamDate(Date examDate) {
        this.examDate = examDate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getSlotsRemaining() {
        return slotsRemaining;
    }

    public void setSlotsRemaining(int slotsRemaining) {
        this.slotsRemaining = slotsRemaining;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }
}
