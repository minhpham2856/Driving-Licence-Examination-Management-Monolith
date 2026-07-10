package Models;

import java.util.List;


public class CandidateExamContext {
    private int candidateId;
    private int examCandidateId;
    private int sessionId;
    private int licenceId;
    private String licenceClass;

    private String candidateNumber;
    private String fullName;
    private String dobDisplay;   // dd/MM/yyyy
    private String citizenId;
    private String address;
    private String photoUrl;
    private String examLocation;

    private int theoryPaperId;
    private int deviceId;

    private List<Question> questions;
    private int numQuestions;
    private int passThreshold;
    private int durationMinutes;
    private long startedAtMillis;

    // ---- getters/setters ----
    public int getCandidateId() { return candidateId; }
    public void setCandidateId(int v) { this.candidateId = v; }
    public int getExamCandidateId() { return examCandidateId; }
    public void setExamCandidateId(int v) { this.examCandidateId = v; }
    public int getSessionId() { return sessionId; }
    public void setSessionId(int v) { this.sessionId = v; }
    public int getLicenceId() { return licenceId; }
    public void setLicenceId(int v) { this.licenceId = v; }
    public String getLicenceClass() { return licenceClass; }
    public void setLicenceClass(String v) { this.licenceClass = v; }

    public String getCandidateNumber() { return candidateNumber; }
    public void setCandidateNumber(String v) { this.candidateNumber = v; }
    public String getFullName() { return fullName; }
    public void setFullName(String v) { this.fullName = v; }
    public String getDobDisplay() { return dobDisplay; }
    public void setDobDisplay(String v) { this.dobDisplay = v; }
    public String getCitizenId() { return citizenId; }
    public void setCitizenId(String v) { this.citizenId = v; }
    public String getAddress() { return address; }
    public void setAddress(String v) { this.address = v; }
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String v) { this.photoUrl = v; }
    public String getExamLocation() { return examLocation; }
    public void setExamLocation(String v) { this.examLocation = v; }

    public int getTheoryPaperId() { return theoryPaperId; }
    public void setTheoryPaperId(int v) { this.theoryPaperId = v; }
    public int getDeviceId() { return deviceId; }
    public void setDeviceId(int v) { this.deviceId = v; }

    public List<Question> getQuestions() { return questions; }
    public void setQuestions(List<Question> v) { this.questions = v; }
    public int getNumQuestions() { return numQuestions; }
    public void setNumQuestions(int v) { this.numQuestions = v; }
    public int getPassThreshold() { return passThreshold; }
    public void setPassThreshold(int v) { this.passThreshold = v; }
    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int v) { this.durationMinutes = v; }
    public long getStartedAtMillis() { return startedAtMillis; }
    public void setStartedAtMillis(long v) { this.startedAtMillis = v; }

    public String getSbd() { return candidateNumber; }
    public String getLicenseClass() { return licenceClass; }
    public String getDob() { return dobDisplay; }
}
