package model;

public class ExamEnrollment {

    private int examEnrollmentId;
    private int candidateId;
    private int sessionId;
    private String sectionStatus;
    private boolean signaturePrinted;
    private Integer examDeviceId;
    private Candidate candidate;
    private Session session;
    private ExamDevice examDevice;

    public ExamEnrollment() {
    }

    public ExamEnrollment(int examEnrollmentId, int candidateId, int sessionId, String sectionStatus,
            boolean signaturePrinted, Integer examDeviceId) {
        this.examEnrollmentId = examEnrollmentId;
        this.candidateId = candidateId;
        this.sessionId = sessionId;
        this.sectionStatus = sectionStatus;
        this.signaturePrinted = signaturePrinted;
        this.examDeviceId = examDeviceId;
    }

    public int getExamEnrollmentId() {
        return examEnrollmentId;
    }

    public void setExamEnrollmentId(int examEnrollmentId) {
        this.examEnrollmentId = examEnrollmentId;
    }

    public int getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(int candidateId) {
        this.candidateId = candidateId;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public String getSectionStatus() {
        return sectionStatus;
    }

    public void setSectionStatus(String sectionStatus) {
        this.sectionStatus = sectionStatus;
    }

    public boolean isSignaturePrinted() {
        return signaturePrinted;
    }

    public void setSignaturePrinted(boolean signaturePrinted) {
        this.signaturePrinted = signaturePrinted;
    }

    public Integer getExamDeviceId() {
        return examDeviceId;
    }

    public void setExamDeviceId(Integer examDeviceId) {
        this.examDeviceId = examDeviceId;
    }

    public Candidate getCandidate() {
        return candidate;
    }

    public void setCandidate(Candidate candidate) {
        this.candidate = candidate;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public ExamDevice getExamDevice() {
        return examDevice;
    }

    public void setExamDevice(ExamDevice examDevice) {
        this.examDevice = examDevice;
    }
}
