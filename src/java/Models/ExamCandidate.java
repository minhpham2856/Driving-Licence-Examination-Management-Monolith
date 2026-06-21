package Models;

public class ExamCandidate {

    private int examCandidateId;
    private int examId;
    private int candidateId;
    private int sessionId;
    private String sectionStatus;
    private boolean signaturePrinted;
    private Integer examDeviceId;

    public ExamCandidate() {
    }

    public ExamCandidate(int examCandidateId, int examId, int candidateId, int sessionId, String sectionStatus, boolean signaturePrinted, Integer examDeviceId) {
        this.examCandidateId = examCandidateId;
        this.examId = examId;
        this.candidateId = candidateId;
        this.sessionId = sessionId;
        this.sectionStatus = sectionStatus;
        this.signaturePrinted = signaturePrinted;
        this.examDeviceId = examDeviceId;
    }

    public int getExamCandidateId() {
        return examCandidateId;
    }

    public void setExamCandidateId(int examCandidateId) {
        this.examCandidateId = examCandidateId;
    }

    public int getExamId() {
        return examId;
    }

    public void setExamId(int examId) {
        this.examId = examId;
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
}
