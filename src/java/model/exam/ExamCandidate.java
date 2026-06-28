package model.exam;

public class ExamCandidate {
    private int examCandidateId;
    private int examId;
    private int candidateId;
    private int sessionId;

    public int getExamCandidateId() { return examCandidateId; }
    public void setExamCandidateId(int examCandidateId) { this.examCandidateId = examCandidateId; }
    public int getExamId() { return examId; }
    public void setExamId(int examId) { this.examId = examId; }
    public int getCandidateId() { return candidateId; }
    public void setCandidateId(int candidateId) { this.candidateId = candidateId; }
    public int getSessionId() { return sessionId; }
    public void setSessionId(int sessionId) { this.sessionId = sessionId; }
}
