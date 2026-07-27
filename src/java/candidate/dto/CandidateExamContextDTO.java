package candidate.dto;

import java.util.List;
import shared.model.Question;

public class CandidateExamContextDTO {

    private int candidateId;
    private String candidateNumber;
    private String fullName;
    private String governmentIdNumber;
    private int examId;
    private int examEnrollmentId;
    private int examEnrollmentSectionId;
    private int examSectionId;
    private int examAreaId;
    private int licenceId;
    private String examCode;
    private String licenceClass;
    private String examDateDisplay;
    private String sectionName;
    private int durationMinutes;
    private int theoryPaperId;
    private long startedAtMillis;
    private List<Question> questions;

    public int getCandidateId() { return candidateId; }
    public void setCandidateId(int value) { candidateId = value; }
    public String getCandidateNumber() { return candidateNumber; }
    public void setCandidateNumber(String value) { candidateNumber = value; }
    public String getFullName() { return fullName; }
    public void setFullName(String value) { fullName = value; }
    public String getGovernmentIdNumber() { return governmentIdNumber; }
    public void setGovernmentIdNumber(String value) { governmentIdNumber = value; }
    public int getExamId() { return examId; }
    public void setExamId(int value) { examId = value; }
    public int getExamEnrollmentId() { return examEnrollmentId; }
    public void setExamEnrollmentId(int value) { examEnrollmentId = value; }
    public int getExamEnrollmentSectionId() { return examEnrollmentSectionId; }
    public void setExamEnrollmentSectionId(int value) { examEnrollmentSectionId = value; }
    public int getExamSectionId() { return examSectionId; }
    public void setExamSectionId(int value) { examSectionId = value; }
    public int getExamAreaId() { return examAreaId; }
    public void setExamAreaId(int value) { examAreaId = value; }
    public int getLicenceId() { return licenceId; }
    public void setLicenceId(int value) { licenceId = value; }
    public String getExamCode() { return examCode; }
    public void setExamCode(String value) { examCode = value; }
    public String getLicenceClass() { return licenceClass; }
    public void setLicenceClass(String value) { licenceClass = value; }
    public String getExamDateDisplay() { return examDateDisplay; }
    public void setExamDateDisplay(String value) { examDateDisplay = value; }
    public String getSectionName() { return sectionName; }
    public void setSectionName(String value) { sectionName = value; }
    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int value) { durationMinutes = value; }
    public int getTheoryPaperId() { return theoryPaperId; }
    public void setTheoryPaperId(int value) { theoryPaperId = value; }
    public long getStartedAtMillis() { return startedAtMillis; }
    public void setStartedAtMillis(long value) { startedAtMillis = value; }
    public List<Question> getQuestions() { return questions; }
    public void setQuestions(List<Question> value) { questions = value; }
}
