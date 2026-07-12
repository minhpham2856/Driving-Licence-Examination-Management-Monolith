package shared.model;
import java.sql.Timestamp;
public class Exam {
    private int examId;
    private String examCode;
    private Timestamp examDate;
    private Timestamp startTime;
    private Timestamp endTime;
    private String centreName;
    private String status;
    private int licenceId;
    public Exam() {
    }
    public Exam(int examId, String examCode, Timestamp examDate, String centreName, String status, int licenceId) {
        this.examId = examId;
        this.examCode = examCode;
        this.examDate = examDate;
        this.centreName = centreName;
        this.status = status;
        this.licenceId = licenceId;
    }
    public int getExamId() {
        return examId;
    }
    public void setExamId(int examId) {
        this.examId = examId;
    }
    public String getExamCode() {
        return examCode;
    }
    public void setExamCode(String examCode) {
        this.examCode = examCode;
    }
    public Timestamp getExamDate() {
        return examDate;
    }
    public void setExamDate(Timestamp examDate) {
        this.examDate = examDate;
    }
    public Timestamp getStartTime() {
        return startTime;
    }
    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }
    public Timestamp getEndTime() {
        return endTime;
    }
    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }
    public String getCentreName() {
        return centreName;
    }
    public void setCentreName(String centreName) {
        this.centreName = centreName;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public int getLicenceId() {
        return licenceId;
    }
    public void setLicenceId(int licenceId) {
        this.licenceId = licenceId;
    }
}

